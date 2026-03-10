package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.LearningRecord;
import com.xixi.entity.StudentCourse;
import com.xixi.entity.StudyNote;
import com.xixi.exception.BizException;
import com.xixi.mapper.ExamSubmissionMapper;
import com.xixi.mapper.GradeMapper;
import com.xixi.mapper.HomeworkSubmissionMapper;
import com.xixi.mapper.LearningRecordMapper;
import com.xixi.mapper.StudyNoteMapper;
import com.xixi.mapper.StudentCourseMapper;
import com.xixi.mq.CourseStudentCountEventProducer;
import com.xixi.openfeign.course.EducationCourseClient;
import com.xixi.openfeign.user.EducationUserStudentClient;
import com.xixi.pojo.dto.StudentCourseJoinDto;
import com.xixi.pojo.query.StudentCourseQuery;
import com.xixi.pojo.query.TeacherStudentCourseQuery;
import com.xixi.pojo.vo.StudentCourseDetailVo;
import com.xixi.pojo.vo.StudentCourseVo;
import com.xixi.pojo.vo.TeacherStudentDetailVo;
import com.xixi.pojo.vo.TeacherStudentCourseVo;
import com.xixi.service.StudentCourseService;
import com.xixi.web.Result;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生选课服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentCourseServiceImpl implements StudentCourseService {
    private static final BigDecimal CHAPTER_COMPLETE_THRESHOLD = new BigDecimal("80");
    private static final String TX_MODE_MQ = "mq";

    private final StudentCourseMapper studentCourseMapper;
    private final LearningRecordMapper learningRecordMapper;
    private final StudyNoteMapper studyNoteMapper;
    private final HomeworkSubmissionMapper homeworkSubmissionMapper;
    private final ExamSubmissionMapper examSubmissionMapper;
    private final GradeMapper gradeMapper;
    private final EducationCourseClient educationCourseClient;
    private final EducationUserStudentClient educationUserStudentClient;
    private final CourseStudentCountEventProducer courseStudentCountEventProducer;
    @Value("${education.tx.mode:seata}")
    private String txMode;
    
    /**
     * 学生加入课程，校验课程可用性、容量与重复选课。
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Result joinCourse(StudentCourseJoinDto dto) {
        if (dto == null || dto.getStudentId() == null || dto.getCourseId() == null) {
            throw new BizException("studentId和courseId不能为空");
        }

        // 0. 校验学生存在
        Result studentResult = educationUserStudentClient.getStudentById(dto.getStudentId());
        if (studentResult == null || studentResult.getCode() != 200 || studentResult.getData() == null) {
            throw new BizException("学生不存在");
        }

        // 1. 检查课程是否存在且可加入
        Result courseResult = educationCourseClient.getCourseDetail(dto.getCourseId());
        if (courseResult == null || courseResult.getCode() != 200 || courseResult.getData() == null) {
            throw new BizException("课程不存在或不可用");
        }

        // 解析CourseDetailVo结构：Result.data -> CourseDetailVo -> courseInfo -> CourseBaseInfo
        Map<String, Object> courseDetailMap;
        if (courseResult.getData() instanceof Map) {
            courseDetailMap = (Map<String, Object>) courseResult.getData();
        } else {
            String jsonStr = JSONUtil.toJsonStr(courseResult.getData());
            courseDetailMap = JSONUtil.toBean(jsonStr, Map.class);
        }

        // 从CourseDetailVo中获取courseInfo
        Map<String, Object> courseInfoMap = null;
        if (courseDetailMap != null && courseDetailMap.containsKey("courseInfo")) {
            Object courseInfoObj = courseDetailMap.get("courseInfo");
            if (courseInfoObj instanceof Map) {
                courseInfoMap = (Map<String, Object>) courseInfoObj;
            } else {
                String courseInfoJson = JSONUtil.toJsonStr(courseInfoObj);
                courseInfoMap = JSONUtil.toBean(courseInfoJson, Map.class);
            }
        }

        if (courseInfoMap == null) {
            throw new BizException("课程信息格式错误，无法解析courseInfo");
        }

        // 检查课程状态
        String courseStatus = (String) courseInfoMap.get("status");
        if (courseStatus == null || !"PUBLISHED".equals(courseStatus)) {
            throw new BizException("课程未发布，无法加入");
        }

        // 检查课程人数限制（前置快速失败，最终以课程服务原子更新为准）
        Integer maxStudents = getIntegerValue(courseInfoMap.get("maxStudents"));
        Integer currentStudents = getIntegerValue(courseInfoMap.get("currentStudents"));
        if (maxStudents != null && maxStudents > 0 && currentStudents != null && currentStudents >= maxStudents) {
            throw new BizException("课程人数已满，无法加入");
        }

        // 2. 检查学生是否已选过该课程
        StudentCourse existingCourse = studentCourseMapper.selectByStudentIdAndCourseId(
                dto.getStudentId(), dto.getCourseId());
        if (existingCourse != null) {
            throw new BizException("您已经选过该课程");
        }

        // 3. 创建选课记录
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentId(dto.getStudentId());
        studentCourse.setCourseId(dto.getCourseId());

        // 从courseInfo中获取课程基本信息
        studentCourse.setCourseName(getStringValue(courseInfoMap.get("courseName")));
        studentCourse.setTeacherId(getLongValue(courseInfoMap.get("teacherId")));
        studentCourse.setTeacherName(getStringValue(courseInfoMap.get("teacherName")));
        studentCourse.setSelectedTime(LocalDateTime.now());
        studentCourse.setLearningStatus("STUDYING");
        studentCourse.setProgressPercentage(BigDecimal.ZERO);
        studentCourse.setTotalStudyTime(0);
        studentCourse.setCreatedTime(LocalDateTime.now());
        studentCourse.setUpdatedTime(LocalDateTime.now());

        studentCourseMapper.insert(studentCourse);

        // 4. 同步更新课程人数（Seata）或发送MQ事件（兼容旧模式）
        syncCourseStudentNumber(dto.getCourseId(), dto.getStudentId(), 1);
        return Result.success("加入课程成功");
    }
    
    /**
     * 学生退出课程，更新选课状态并同步课程人数。
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Result quitCourse(Long studentId, Long courseId) {
        if (studentId == null || courseId == null) {
            throw new BizException("studentId和courseId不能为空");
        }

        // 1. 检查选课记录是否存在
        StudentCourse studentCourse = studentCourseMapper.selectByStudentIdAndCourseId(studentId, courseId);
        if (studentCourse == null) {
            throw new BizException("未找到选课记录");
        }

        // 2. 更新选课状态为"已退课"
        studentCourse.setLearningStatus("DROPPED");
        studentCourse.setUpdatedTime(LocalDateTime.now());
        studentCourseMapper.updateById(studentCourse);

        // 3. 同步更新课程人数（Seata）或发送MQ事件（兼容旧模式）
        syncCourseStudentNumber(courseId, studentId, 0);
        return Result.success("退出课程成功");
    }
    
    /**
     * 查询学生课程列表并补全课程封面等远程信息。
     */
    @Override
    @SuppressWarnings("unchecked")
    public IPage<StudentCourseVo> getMyCourses(StudentCourseQuery query) {
        // 1. 先查询选课记录（只包含student_course表的数据）
        IPage<StudentCourseVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<StudentCourseVo> resultPage = studentCourseMapper.selectMyCoursesPage((Page<StudentCourseVo>) page, query);
        
        if (resultPage.getRecords().isEmpty()) {
            return resultPage;
        }
        
        // 2. 收集所有课程ID
        List<Long> courseIds = resultPage.getRecords().stream()
                .map(StudentCourseVo::getCourseId)
                .distinct()
                .collect(Collectors.toList());
        
        // 3. 批量获取课程信息（通过OpenFeign调用课程服务）
        Map<Long, Map<String, Object>> courseInfoMap = new HashMap<>();
        for (Long courseId : courseIds) {
            try {
                Result courseResult = educationCourseClient.getCourseDetail(courseId);
                if (courseResult != null && courseResult.getCode() == 200 && courseResult.getData() != null) {
                    // 解析CourseDetailVo结构
                    Map<String, Object> courseDetailMap = null;
                    if (courseResult.getData() instanceof Map) {
                        courseDetailMap = (Map<String, Object>) courseResult.getData();
                    } else {
                        String jsonStr = JSONUtil.toJsonStr(courseResult.getData());
                        courseDetailMap = JSONUtil.toBean(jsonStr, Map.class);
                    }
                    
                    // 从CourseDetailVo中获取courseInfo
                    if (courseDetailMap != null && courseDetailMap.containsKey("courseInfo")) {
                        Object courseInfoObj = courseDetailMap.get("courseInfo");
                        Map<String, Object> courseInfo = null;
                        if (courseInfoObj instanceof Map) {
                            courseInfo = (Map<String, Object>) courseInfoObj;
                        } else {
                            String courseInfoJson = JSONUtil.toJsonStr(courseInfoObj);
                            courseInfo = JSONUtil.toBean(courseInfoJson, Map.class);
                        }
                        if (courseInfo != null) {
                            courseInfoMap.put(courseId, courseInfo);
                        }
                    }
                }
            } catch (Exception e) {
                // 如果获取课程信息失败，记录日志但不影响主流程
                log.warn("查询我的课程时获取课程信息失败，courseId={}, error={}", courseId, e.getMessage());
            }
        }
        
        // 4. 填充课程信息到VO中（主要是课程封面）
        for (StudentCourseVo vo : resultPage.getRecords()) {
            Map<String, Object> courseInfo = courseInfoMap.get(vo.getCourseId());
            if (courseInfo != null) {
                vo.setCourseCover((String) courseInfo.get("courseCover"));
            }
        }
        
        return resultPage;
    }
    
    /**
     * 查询学生课程学习详情（章节进度、作业、测验、成绩）。
     */
    @Override
    public StudentCourseDetailVo getCourseDetail(Long studentId, Long courseId) {
        if (studentId == null || courseId == null) {
            throw new BizException("studentId和courseId不能为空");
        }

        // 1. 查询选课基本信息
        StudentCourseVo studentCourseVo = studentCourseMapper.selectCourseDetail(studentId, courseId);
        if (studentCourseVo == null) {
            throw new BizException("未找到选课记录");
        }
        
        // 2. 转换为详情VO
        StudentCourseDetailVo detailVo = BeanUtil.copyProperties(studentCourseVo, StudentCourseDetailVo.class);
        
        // 3. 获取课程详细信息
        Map<String, Object> courseDetailMap = getCourseDetailMap(courseId);
        Map<String, Object> courseInfoMap = extractCourseInfoMap(courseDetailMap);
        if (courseInfoMap != null) {
            detailVo.setCourseCover(getStringValue(courseInfoMap.get("courseCover")));
            detailVo.setCourseDescription(getStringValue(courseInfoMap.get("fullDescription")));
        }
        
        // 4. 获取学习进度详情（各章节完成情况）
        List<Map<String, Object>> chapterList = extractChapterList(courseDetailMap);
        List<LearningRecord> latestLearningRecords = learningRecordMapper.selectLatestByStudentAndCourse(studentId, courseId);
        List<LearningRecord> allLearningRecords = learningRecordMapper.selectByStudentAndCourse(studentId, courseId);
        detailVo.setChapterProgressList(buildChapterProgressList(chapterList, latestLearningRecords, allLearningRecords));
        
        // 5. 获取作业完成情况
        detailVo.setHomeworkProgressList(
                homeworkSubmissionMapper.selectHomeworkProgressByStudentAndCourse(studentId, courseId));
        
        // 6. 获取测验完成情况
        detailVo.setExamProgressList(
                examSubmissionMapper.selectExamProgressByStudentAndCourse(studentId, courseId));
        
        // 7. 获取成绩信息
        com.xixi.pojo.vo.GradeVo publishedGrade = gradeMapper.selectGradeByCourseAndStudent(courseId, studentId);
        detailVo.setGradeInfo(convertToCourseDetailGrade(publishedGrade));
        
        return detailVo;
    }
    
    /**
     * 教师端分页查询课程学生列表并补全学生信息。
     */
    @Override
    @SuppressWarnings("unchecked")
    public IPage<TeacherStudentCourseVo> getTeacherStudentList(TeacherStudentCourseQuery query) {
        // 注意：由于学生信息需要通过OpenFeign跨服务获取，如果查询条件中包含学生姓名或学号，
        // 需要先查询所有数据，获取学生信息后再进行内存过滤，这会影响分页准确性
        // 建议：如果查询条件中包含学生姓名或学号，先查询所有数据（不分页），过滤后再分页
        
        // 1. 先查询选课记录（只包含student_course表的数据）
        // 如果查询条件中包含学生姓名或学号，需要查询所有数据
        boolean needFilterByNameOrNumber = (query.getStudentName() != null && !query.getStudentName().isEmpty()) ||
                (query.getStudentNumber() != null && !query.getStudentNumber().isEmpty());
        
        IPage<TeacherStudentCourseVo> resultPage;
        if (needFilterByNameOrNumber) {
            // 查询所有数据（不分页）
            TeacherStudentCourseQuery allQuery = new TeacherStudentCourseQuery();
            allQuery.setCourseId(query.getCourseId());
            allQuery.setLearningStatus(query.getLearningStatus());
            allQuery.setSortBy(query.getSortBy());
            allQuery.setSortOrder(query.getSortOrder());
            allQuery.setPageNum(1);
            allQuery.setPageSize(Integer.MAX_VALUE);
            resultPage = studentCourseMapper.selectTeacherStudentListPage(
                    new Page<>(1, Integer.MAX_VALUE), allQuery);
        } else {
            // 正常分页查询
            IPage<TeacherStudentCourseVo> page = new Page<>(query.getPageNum(), query.getPageSize());
            resultPage = studentCourseMapper.selectTeacherStudentListPage(
                    (Page<TeacherStudentCourseVo>) page, query);
        }
        
        // 2. 收集所有学生ID
        List<Long> studentIds = resultPage.getRecords().stream()
                .map(TeacherStudentCourseVo::getStudentId)
                .distinct()
                .collect(Collectors.toList());
        
        if (studentIds.isEmpty()) {
            return resultPage;
        }
        
        // 3. 批量获取学生信息（通过OpenFeign调用用户服务）
        Map<Long, Map<String, Object>> studentInfoMap = new HashMap<>();
        for (Long studentId : studentIds) {
            try {
                Result studentResult = educationUserStudentClient.getStudentById(studentId);
                if (studentResult != null && studentResult.getCode() == 200 && studentResult.getData() != null) {
                    Map<String, Object> studentInfo = null;
                    if (studentResult.getData() instanceof Map) {
                        studentInfo = (Map<String, Object>) studentResult.getData();
                    } else {
                        String jsonStr = JSONUtil.toJsonStr(studentResult.getData());
                        studentInfo = JSONUtil.toBean(jsonStr, Map.class);
                    }
                    if (studentInfo != null) {
                        studentInfoMap.put(studentId, studentInfo);
                    }
                }
            } catch (Exception e) {
                // 如果获取学生信息失败，记录日志但不影响主流程
                log.warn("教师端查询选课学生时获取学生信息失败，studentId={}, error={}", studentId, e.getMessage());
            }
        }
        
        // 4. 填充学生信息到VO中
        for (TeacherStudentCourseVo vo : resultPage.getRecords()) {
            Map<String, Object> studentInfo = studentInfoMap.get(vo.getStudentId());
            if (studentInfo != null) {
                vo.setUserId(getLongValue(studentInfo.get("userId")));
                vo.setStudentNumber((String) studentInfo.get("studentNumber"));
                vo.setStudentName((String) studentInfo.get("realName"));
                vo.setNickname((String) studentInfo.get("nickname"));
                vo.setAvatar((String) studentInfo.get("avatar"));
                vo.setEmail((String) studentInfo.get("email"));
                vo.setPhone((String) studentInfo.get("phone"));
                vo.setSchool((String) studentInfo.get("school"));
                vo.setCollege((String) studentInfo.get("college"));
                vo.setMajor((String) studentInfo.get("major"));
            }
        }
        
        // 5. 如果查询条件中包含学生姓名或学号，进行内存过滤和分页
        if (needFilterByNameOrNumber) {
            List<TeacherStudentCourseVo> filteredList = resultPage.getRecords().stream()
                    .filter(vo -> {
                        boolean matchName = true;
                        boolean matchNumber = true;
                        if (query.getStudentName() != null && !query.getStudentName().isEmpty()) {
                            String studentName = vo.getStudentName();
                            matchName = studentName != null && studentName.contains(query.getStudentName());
                        }
                        if (query.getStudentNumber() != null && !query.getStudentNumber().isEmpty()) {
                            String studentNumber = vo.getStudentNumber();
                            matchNumber = studentNumber != null && studentNumber.contains(query.getStudentNumber());
                        }
                        return matchName && matchNumber;
                    })
                    .collect(Collectors.toList());
            
            // 手动分页
            int pageNum = query.getPageNum();
            int pageSize = query.getPageSize();
            int total = filteredList.size();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            
            List<TeacherStudentCourseVo> pagedList = start < total ? filteredList.subList(start, end) : new ArrayList<>();
            
            // 重新设置分页结果
            IPage<TeacherStudentCourseVo> pagedResult = new Page<>(pageNum, pageSize, total);
            pagedResult.setRecords(pagedList);
            return pagedResult;
        }
        
        return resultPage;
    }
    
    /**
     * 课程人数变更：Seata模式走同步Feign，MQ模式走事件。
     */
    private void syncCourseStudentNumber(Long courseId, Long studentId, Integer status) {
        if (isMqMode()) {
            courseStudentCountEventProducer.publish(courseId, studentId, status);
            return;
        }
        Result updateResult = educationCourseClient.updateCourseCurrentStudentNumber(courseId, status);
        if (updateResult == null || updateResult.getCode() == null || updateResult.getCode() != 200) {
            String errorMsg = updateResult == null ? "课程服务调用失败" : updateResult.getMessage();
            throw new BizException("更新课程人数失败：" + errorMsg);
        }
    }

    /**
     * 是否启用MQ链路。
     */
    private boolean isMqMode() {
        return TX_MODE_MQ.equalsIgnoreCase(txMode);
    }

    /**
     * 安全地获取Long值
     */
    private Long getLongValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(obj));
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 教师端查看指定学生在课程下的学习详情。
     */
    @Override
    @SuppressWarnings("unchecked")
    public TeacherStudentDetailVo getTeacherStudentDetail(Long courseId, Long studentId) {
        if (courseId == null || studentId == null) {
            throw new BizException("courseId和studentId不能为空");
        }

        // 1. 查询选课基本信息（只包含student_course表的数据）
        TeacherStudentCourseVo studentCourseVo = studentCourseMapper.selectTeacherStudentDetail(courseId, studentId);
        if (studentCourseVo == null) {
            throw new BizException("未找到选课记录");
        }
        
        // 2. 通过OpenFeign获取学生信息
        Map<String, Object> studentInfoMap = null;
        try {
            Result studentResult = educationUserStudentClient.getStudentById(studentId);
            if (studentResult != null && studentResult.getCode() == 200 && studentResult.getData() != null) {
                if (studentResult.getData() instanceof Map) {
                    studentInfoMap = (Map<String, Object>) studentResult.getData();
                } else {
                    String jsonStr = JSONUtil.toJsonStr(studentResult.getData());
                    studentInfoMap = JSONUtil.toBean(jsonStr, Map.class);
                }
            }
        } catch (Exception e) {
            // 如果获取学生信息失败，记录日志但不影响主流程
            log.warn("教师端查询学生学习详情时获取学生信息失败，studentId={}, error={}", studentId, e.getMessage());
        }

        // 3. 解析课程章节和视频映射
        Map<String, Object> courseDetailMap = getCourseDetailMap(courseId);
        Map<Long, String> chapterNameMap = buildChapterNameMap(courseDetailMap);
        Map<Long, String> videoNameMap = buildVideoNameMap(courseDetailMap);
        
        // 4. 转换为详情VO
        TeacherStudentDetailVo detailVo = new TeacherStudentDetailVo();
        detailVo.setId(studentCourseVo.getId());
        detailVo.setStudentId(studentCourseVo.getStudentId());
        detailVo.setCourseId(studentCourseVo.getCourseId());
        detailVo.setCourseName(studentCourseVo.getCourseName());
        detailVo.setSelectedTime(studentCourseVo.getSelectedTime());
        detailVo.setLearningStatus(studentCourseVo.getLearningStatus());
        detailVo.setProgressPercentage(studentCourseVo.getProgressPercentage());
        detailVo.setTotalStudyTime(studentCourseVo.getTotalStudyTime());
        detailVo.setLastStudyTime(studentCourseVo.getLastStudyTime());
        detailVo.setCompletedTime(studentCourseVo.getCompletedTime());
        
        // 5. 设置学生基本信息
        TeacherStudentDetailVo.StudentInfo studentInfo = new TeacherStudentDetailVo.StudentInfo();
        studentInfo.setStudentId(studentCourseVo.getStudentId());
        if (studentInfoMap != null) {
            studentInfo.setUserId(getLongValue(studentInfoMap.get("userId")));
            studentInfo.setStudentNumber((String) studentInfoMap.get("studentNumber"));
            studentInfo.setStudentName((String) studentInfoMap.get("realName"));
            studentInfo.setNickname((String) studentInfoMap.get("nickname"));
            studentInfo.setAvatar((String) studentInfoMap.get("avatar"));
            studentInfo.setEmail((String) studentInfoMap.get("email"));
            studentInfo.setPhone((String) studentInfoMap.get("phone"));
            studentInfo.setSchool((String) studentInfoMap.get("school"));
            studentInfo.setCollege((String) studentInfoMap.get("college"));
            studentInfo.setMajor((String) studentInfoMap.get("major"));
        }
        detailVo.setStudentInfo(studentInfo);
        
        // 6. 获取学习记录明细
        List<LearningRecord> learningRecords = learningRecordMapper.selectByStudentAndCourse(studentId, courseId);
        detailVo.setLearningRecords(convertLearningRecordInfos(learningRecords, chapterNameMap, videoNameMap));
        
        // 7. 获取笔记列表
        List<StudyNote> noteList = studyNoteMapper.selectByStudentAndCourse(studentId, courseId);
        detailVo.setNotes(convertNoteInfos(noteList, chapterNameMap, videoNameMap));
        
        // 8. 获取作业提交情况
        detailVo.setHomeworkSubmissions(
                homeworkSubmissionMapper.selectTeacherStudentHomeworkSubmissions(courseId, studentId));
        
        // 9. 获取测验完成情况
        detailVo.setExamSubmissions(
                examSubmissionMapper.selectTeacherStudentExamSubmissions(courseId, studentId));
        
        return detailVo;
    }
    
    /**
     * 教师端导出课程学生名单。
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<TeacherStudentCourseVo> exportStudentList(Long courseId) {
        // 1. 先查询选课记录（只包含student_course表的数据）
        List<TeacherStudentCourseVo> studentList = studentCourseMapper.selectAllStudentsByCourseId(courseId);
        
        if (studentList.isEmpty()) {
            return studentList;
        }
        
        // 2. 收集所有学生ID
        List<Long> studentIds = studentList.stream()
                .map(TeacherStudentCourseVo::getStudentId)
                .distinct()
                .collect(Collectors.toList());
        
        // 3. 批量获取学生信息（通过OpenFeign调用用户服务）
        Map<Long, Map<String, Object>> studentInfoMap = new HashMap<>();
        for (Long studentId : studentIds) {
            try {
                Result studentResult = educationUserStudentClient.getStudentById(studentId);
                if (studentResult != null && studentResult.getCode() == 200 && studentResult.getData() != null) {
                    Map<String, Object> studentInfo = null;
                    if (studentResult.getData() instanceof Map) {
                        studentInfo = (Map<String, Object>) studentResult.getData();
                    } else {
                        String jsonStr = JSONUtil.toJsonStr(studentResult.getData());
                        studentInfo = JSONUtil.toBean(jsonStr, Map.class);
                    }
                    if (studentInfo != null) {
                        studentInfoMap.put(studentId, studentInfo);
                    }
                }
            } catch (Exception e) {
                // 如果获取学生信息失败，记录日志但不影响主流程
                log.warn("导出学生名单时获取学生信息失败，studentId={}, error={}", studentId, e.getMessage());
            }
        }
        
        // 4. 填充学生信息到VO中
        for (TeacherStudentCourseVo vo : studentList) {
            Map<String, Object> studentInfo = studentInfoMap.get(vo.getStudentId());
            if (studentInfo != null) {
                vo.setUserId(getLongValue(studentInfo.get("userId")));
                vo.setStudentNumber((String) studentInfo.get("studentNumber"));
                vo.setStudentName((String) studentInfo.get("realName"));
                vo.setNickname((String) studentInfo.get("nickname"));
                vo.setAvatar((String) studentInfo.get("avatar"));
                vo.setEmail((String) studentInfo.get("email"));
                vo.setPhone((String) studentInfo.get("phone"));
                vo.setSchool((String) studentInfo.get("school"));
                vo.setCollege((String) studentInfo.get("college"));
                vo.setMajor((String) studentInfo.get("major"));
            }
        }
        
        return studentList;
    }

    /**
     * 构建章节学习进度列表。
     */
    private List<StudentCourseDetailVo.ChapterProgressVo> buildChapterProgressList(
            List<Map<String, Object>> chapterList,
            List<LearningRecord> latestLearningRecords,
            List<LearningRecord> allLearningRecords) {
        List<StudentCourseDetailVo.ChapterProgressVo> chapterProgressList = new ArrayList<>();
        if (chapterList == null || chapterList.isEmpty()) {
            return chapterProgressList;
        }

        Map<Long, BigDecimal> videoProgressMap = new HashMap<>();
        if (latestLearningRecords != null) {
            for (LearningRecord record : latestLearningRecords) {
                if (record.getVideoId() != null) {
                    videoProgressMap.put(record.getVideoId(),
                            record.getVideoProgress() == null ? BigDecimal.ZERO : record.getVideoProgress());
                }
            }
        }

        Map<Long, Integer> chapterStudyTimeMap = new HashMap<>();
        if (allLearningRecords != null) {
            for (LearningRecord record : allLearningRecords) {
                if (record.getChapterId() == null) {
                    continue;
                }
                int duration = record.getDuration() == null ? 0 : record.getDuration();
                chapterStudyTimeMap.put(record.getChapterId(),
                        chapterStudyTimeMap.getOrDefault(record.getChapterId(), 0) + duration);
            }
        }

        for (Map<String, Object> chapter : chapterList) {
            Long chapterId = getLongValue(chapter.get("id"));
            String chapterName = getStringValue(chapter.get("chapterName"));
            List<Map<String, Object>> videoList = toMapList(chapter.get("videos"));

            int totalVideoCount = videoList.size();
            int completedVideoCount = 0;
            for (Map<String, Object> video : videoList) {
                Long videoId = getLongValue(video.get("id"));
                BigDecimal progress = videoProgressMap.get(videoId);
                if (progress != null && progress.compareTo(CHAPTER_COMPLETE_THRESHOLD) >= 0) {
                    completedVideoCount++;
                }
            }

            BigDecimal chapterProgress = BigDecimal.ZERO;
            if (totalVideoCount > 0) {
                chapterProgress = BigDecimal.valueOf((double) completedVideoCount * 100 / totalVideoCount)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            StudentCourseDetailVo.ChapterProgressVo progressVo = new StudentCourseDetailVo.ChapterProgressVo();
            progressVo.setChapterId(chapterId);
            progressVo.setChapterName(chapterName);
            progressVo.setProgressPercentage(chapterProgress);
            progressVo.setStudyTime(chapterStudyTimeMap.getOrDefault(chapterId, 0));
            chapterProgressList.add(progressVo);
        }

        return chapterProgressList;
    }

    /**
     * 将成绩VO转换为课程详情中的成绩结构。
     */
    private StudentCourseDetailVo.GradeVo convertToCourseDetailGrade(com.xixi.pojo.vo.GradeVo publishedGrade) {
        if (publishedGrade == null) {
            return null;
        }
        StudentCourseDetailVo.GradeVo gradeVo = new StudentCourseDetailVo.GradeVo();
        gradeVo.setAttendanceScore(publishedGrade.getAttendanceScore());
        gradeVo.setHomeworkScore(publishedGrade.getHomeworkScore());
        gradeVo.setExamScore(publishedGrade.getExamScore() != null
                ? publishedGrade.getExamScore()
                : publishedGrade.getQuizScore());
        gradeVo.setFinalScore(publishedGrade.getFinalScore());
        gradeVo.setGradeLevel(publishedGrade.getGradeLevel());
        gradeVo.setGpa(publishedGrade.getGpa());
        return gradeVo;
    }

    /**
     * 转换学习记录明细。
     */
    private List<TeacherStudentDetailVo.LearningRecordInfo> convertLearningRecordInfos(
            List<LearningRecord> records,
            Map<Long, String> chapterNameMap,
            Map<Long, String> videoNameMap) {
        List<TeacherStudentDetailVo.LearningRecordInfo> result = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return result;
        }

        for (LearningRecord record : records) {
            TeacherStudentDetailVo.LearningRecordInfo info = new TeacherStudentDetailVo.LearningRecordInfo();
            info.setId(record.getId());
            info.setChapterId(record.getChapterId());
            info.setChapterName(chapterNameMap.get(record.getChapterId()));
            info.setVideoId(record.getVideoId());
            info.setVideoName(videoNameMap.get(record.getVideoId()));
            info.setStudyTime(record.getEndTime() != null ? record.getEndTime() : record.getCreatedTime());
            info.setDuration(record.getDuration());
            info.setProgress(record.getVideoProgress());
            result.add(info);
        }
        return result;
    }

    /**
     * 转换笔记明细。
     */
    private List<TeacherStudentDetailVo.NoteInfo> convertNoteInfos(
            List<StudyNote> notes,
            Map<Long, String> chapterNameMap,
            Map<Long, String> videoNameMap) {
        List<TeacherStudentDetailVo.NoteInfo> result = new ArrayList<>();
        if (notes == null || notes.isEmpty()) {
            return result;
        }

        for (StudyNote note : notes) {
            TeacherStudentDetailVo.NoteInfo info = new TeacherStudentDetailVo.NoteInfo();
            info.setId(note.getId());
            info.setChapterId(note.getChapterId());
            info.setChapterName(chapterNameMap.get(note.getChapterId()));
            info.setVideoId(note.getVideoId());
            info.setVideoName(videoNameMap.get(note.getVideoId()));
            info.setContent(note.getNoteContent());
            info.setCreatedTime(note.getCreatedTime());
            result.add(info);
        }
        return result;
    }

    /**
     * 获取课程详情Map。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getCourseDetailMap(Long courseId) {
        try {
            Result courseResult = educationCourseClient.getCourseDetail(courseId);
            if (courseResult == null || courseResult.getCode() != 200 || courseResult.getData() == null) {
                return null;
            }
            if (courseResult.getData() instanceof Map) {
                return (Map<String, Object>) courseResult.getData();
            }
            return JSONUtil.toBean(JSONUtil.toJsonStr(courseResult.getData()), Map.class);
        } catch (Exception e) {
            log.warn("获取课程详情失败，courseId={}, error={}", courseId, e.getMessage());
            return null;
        }
    }

    /**
     * 提取课程基础信息。
     */
    private Map<String, Object> extractCourseInfoMap(Map<String, Object> courseDetailMap) {
        if (courseDetailMap == null) {
            return null;
        }
        return toMap(courseDetailMap.get("courseInfo"));
    }

    /**
     * 提取章节列表。
     */
    private List<Map<String, Object>> extractChapterList(Map<String, Object> courseDetailMap) {
        if (courseDetailMap == null) {
            return new ArrayList<>();
        }
        return toMapList(courseDetailMap.get("chapters"));
    }

    /**
     * 构建章节名称映射。
     */
    private Map<Long, String> buildChapterNameMap(Map<String, Object> courseDetailMap) {
        Map<Long, String> chapterNameMap = new HashMap<>();
        for (Map<String, Object> chapter : extractChapterList(courseDetailMap)) {
            Long chapterId = getLongValue(chapter.get("id"));
            if (chapterId != null) {
                chapterNameMap.put(chapterId, getStringValue(chapter.get("chapterName")));
            }
        }
        return chapterNameMap;
    }

    /**
     * 构建视频名称映射。
     */
    private Map<Long, String> buildVideoNameMap(Map<String, Object> courseDetailMap) {
        Map<Long, String> videoNameMap = new HashMap<>();
        for (Map<String, Object> chapter : extractChapterList(courseDetailMap)) {
            for (Map<String, Object> video : toMapList(chapter.get("videos"))) {
                Long videoId = getLongValue(video.get("id"));
                if (videoId != null) {
                    videoNameMap.put(videoId, getStringValue(video.get("videoName")));
                }
            }
        }
        return videoNameMap;
    }

    /**
     * 安全转Map。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        try {
            return JSONUtil.toBean(JSONUtil.toJsonStr(obj), Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 安全转List<Map>。
     */
    private List<Map<String, Object>> toMapList(Object obj) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (obj == null) {
            return result;
        }
        try {
            if (obj instanceof List<?>) {
                for (Object item : (List<?>) obj) {
                    Map<String, Object> map = toMap(item);
                    if (map != null) {
                        result.add(map);
                    }
                }
                return result;
            }
            List<Object> list = JSONUtil.toList(JSONUtil.toJsonStr(obj), Object.class);
            for (Object item : list) {
                Map<String, Object> map = toMap(item);
                if (map != null) {
                    result.add(map);
                }
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return result;
    }

    /**
     * 安全获取字符串值。
     */
    private String getStringValue(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    /**
     * 安全获取Integer值。
     */
    private Integer getIntegerValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (Exception e) {
            return null;
        }
    }
}
