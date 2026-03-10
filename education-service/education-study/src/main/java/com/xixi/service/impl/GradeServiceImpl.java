package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.Grade;
import com.xixi.entity.StudentCourse;
import com.xixi.exception.BizException;
import com.xixi.mapper.GradeMapper;
import com.xixi.mapper.StudentCourseMapper;
import com.xixi.openfeign.course.EducationCourseClient;
import com.xixi.openfeign.message.EducationMessageInternalClient;
import com.xixi.openfeign.message.dto.TemplateTriggerEventRequest;
import com.xixi.openfeign.user.EducationUserStudentClient;
import com.xixi.openfeign.user.EducationUserTeacherClient;
import com.xixi.pojo.dto.GradeBatchCreateDto;
import com.xixi.pojo.dto.GradeCreateDto;
import com.xixi.pojo.dto.GradePublishDto;
import com.xixi.pojo.dto.GradeUnpublishDto;
import com.xixi.pojo.dto.GradeUpdateDto;
import com.xixi.pojo.dto.GradeWeightDto;
import com.xixi.pojo.query.GradeQuery;
import com.xixi.pojo.vo.CreditSummaryVo;
import com.xixi.pojo.vo.GpaVo;
import com.xixi.pojo.vo.GradeStatisticsVo;
import com.xixi.pojo.vo.GradeVo;
import com.xixi.pojo.vo.TeacherCourseGradeStatisticsVo;
import com.xixi.service.GradeService;
import com.xixi.web.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 成绩服务实现类（学生端 + 教师端）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal PASS_SCORE = new BigDecimal("60");
    private static final String MESSAGE_TYPE_COURSE = "COURSE";
    private static final String MESSAGE_RELATED_TYPE_GRADE = "GRADE";
    private static final String EVENT_CODE_GRADE_PUBLISHED = "GRADE_PUBLISHED";
    private static final String EVENT_CODE_GRADE_UNPUBLISHED = "GRADE_UNPUBLISHED";
    private static final String TARGET_TYPE_USER = "USER";
    private static final String DELIVER_MODE_MQ = "MQ";
    private static final int ROLE_TEACHER = 3;
    private static final int MESSAGE_PRIORITY_NORMAL = 0;
    private static final GradeWeightConfig DEFAULT_WEIGHT_CONFIG = new GradeWeightConfig(
            new BigDecimal("25"),
            new BigDecimal("25"),
            new BigDecimal("25"),
            new BigDecimal("25")
    );

    private final GradeMapper gradeMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final EducationCourseClient educationCourseClient;
    private final EducationMessageInternalClient educationMessageInternalClient;
    private final EducationUserStudentClient educationUserStudentClient;
    private final EducationUserTeacherClient educationUserTeacherClient;

    /**
     * 课程权重配置（当前为内存配置）
     */
    private final Map<Long, GradeWeightConfig> courseWeightConfigMap = new ConcurrentHashMap<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createGrade(GradeCreateDto dto) {
        try {
            Grade grade = buildNewGradeEntity(dto, null);
            gradeMapper.insert(grade);

            Map<String, Object> data = new HashMap<>();
            data.put("gradeId", grade.getId());
            return Result.success("录入成绩成功", data);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("录入成绩失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result batchCreateGrade(GradeBatchCreateDto dto) {
        try {
            if (dto == null || dto.getGradeList() == null || dto.getGradeList().isEmpty()) {
                return Result.error("批量录入列表不能为空");
            }

            Map<Long, CourseBasicInfo> courseInfoCache = new HashMap<>();
            List<Long> gradeIds = new ArrayList<>();
            for (GradeCreateDto item : dto.getGradeList()) {
                if (item == null) {
                    continue;
                }

                CourseBasicInfo courseInfo = courseInfoCache.computeIfAbsent(item.getCourseId(), this::getCourseBasicInfo);
                Grade grade = buildNewGradeEntity(item, courseInfo);
                gradeMapper.insert(grade);
                gradeIds.add(grade.getId());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("count", gradeIds.size());
            data.put("gradeIds", gradeIds);
            return Result.success("批量录入成绩成功", data);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("批量录入成绩失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateGrade(GradeUpdateDto dto) {
        try {
            if (dto == null || dto.getId() == null) {
                return Result.error("成绩ID不能为空");
            }
            if (dto.getTeacherId() == null) {
                return Result.error("教师ID不能为空");
            }

            Grade grade = gradeMapper.selectById(dto.getId());
            if (grade == null) {
                return Result.error("成绩记录不存在");
            }

            CourseBasicInfo courseInfo = getCourseBasicInfo(grade.getCourseId());
            validateTeacherAuthority(dto.getTeacherId(), courseInfo.getTeacherId());
            if (grade.getTeacherId() != null && !Objects.equals(grade.getTeacherId(), dto.getTeacherId())) {
                return Result.error("仅录入该成绩的教师可修改");
            }

            if (dto.getAttendanceScore() != null) {
                grade.setAttendanceScore(normalizeScore(dto.getAttendanceScore(), "考勤成绩"));
            }
            if (dto.getHomeworkScore() != null) {
                grade.setHomeworkScore(normalizeScore(dto.getHomeworkScore(), "作业成绩"));
            }
            if (dto.getQuizScore() != null) {
                grade.setQuizScore(normalizeScore(dto.getQuizScore(), "测验成绩"));
            }
            if (dto.getExamScore() != null) {
                grade.setExamScore(normalizeScore(dto.getExamScore(), "考试成绩"));
            }

            grade.setTeacherId(dto.getTeacherId());
            if (StringUtils.hasText(courseInfo.getCourseName())) {
                grade.setCourseName(courseInfo.getCourseName());
            }

            fillCalculatedFields(grade, courseInfo, getWeightConfig(grade.getCourseId()));
            grade.setUpdatedTime(LocalDateTime.now());
            gradeMapper.updateById(grade);
            return Result.success("修改成绩成功");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("修改成绩失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result setGradeWeight(Long courseId, GradeWeightDto dto) {
        try {
            if (courseId == null) {
                return Result.error("课程ID不能为空");
            }
            if (dto == null || dto.getTeacherId() == null) {
                return Result.error("教师ID不能为空");
            }

            CourseBasicInfo courseInfo = getCourseBasicInfo(courseId);
            validateTeacherAuthority(dto.getTeacherId(), courseInfo.getTeacherId());

            GradeWeightConfig weightConfig = buildWeightConfig(dto);
            courseWeightConfigMap.put(courseId, weightConfig);

            List<Grade> gradeList = gradeMapper.selectEntityListByCourseId(courseId);
            int updatedCount = 0;
            for (Grade grade : gradeList) {
                fillCalculatedFields(grade, courseInfo, weightConfig);
                grade.setUpdatedTime(LocalDateTime.now());
                updatedCount += gradeMapper.updateById(grade);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("courseId", courseId);
            data.put("updatedCount", updatedCount);
            return Result.success("设置成绩权重成功", data);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("设置成绩权重失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result publishGrade(GradePublishDto dto) {
        try {
            if (dto == null || dto.getTeacherId() == null) {
                return Result.error("教师ID不能为空");
            }

            List<Long> gradeIds = sanitizeGradeIds(dto.getGradeIds());
            if (gradeIds.isEmpty()) {
                return Result.error("成绩ID列表不能为空");
            }

            List<Grade> gradeList = gradeMapper.selectBatchIds(gradeIds);
            if (gradeList.size() != gradeIds.size()) {
                return Result.error("部分成绩记录不存在");
            }
            for (Grade grade : gradeList) {
                if (!Objects.equals(grade.getTeacherId(), dto.getTeacherId())) {
                    return Result.error("包含非当前教师的成绩记录，无法发布");
                }
            }

            List<Grade> publishedGradeList = gradeList.stream()
                    .filter(grade -> grade.getPublishedTime() == null)
                    .collect(Collectors.toList());
            int affected = gradeMapper.batchPublish(gradeIds, dto.getTeacherId(), LocalDateTime.now());
            if (affected > 0 && !publishedGradeList.isEmpty()) {
                try {
                    notifyGradeEvent(EVENT_CODE_GRADE_PUBLISHED, publishedGradeList, dto.getTeacherId());
                } catch (Exception notifyException) {
                    log.warn("成绩发布事件上报失败，teacherId={}, gradeIds={}, error={}",
                            dto.getTeacherId(), gradeIds, notifyException.getMessage());
                }
            }
            Map<String, Object> data = new HashMap<>();
            data.put("count", affected);
            return Result.success("发布成绩成功", data);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("发布成绩失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result unpublishGrade(GradeUnpublishDto dto) {
        try {
            if (dto == null || dto.getTeacherId() == null) {
                return Result.error("教师ID不能为空");
            }

            List<Long> gradeIds = sanitizeGradeIds(dto.getGradeIds());
            if (gradeIds.isEmpty()) {
                return Result.error("成绩ID列表不能为空");
            }

            List<Grade> gradeList = gradeMapper.selectBatchIds(gradeIds);
            if (gradeList.size() != gradeIds.size()) {
                return Result.error("部分成绩记录不存在");
            }
            for (Grade grade : gradeList) {
                if (!Objects.equals(grade.getTeacherId(), dto.getTeacherId())) {
                    return Result.error("包含非当前教师的成绩记录，无法撤销发布");
                }
            }

            List<Grade> unpublishedGradeList = gradeList.stream()
                    .filter(grade -> grade.getPublishedTime() != null)
                    .collect(Collectors.toList());
            int affected = gradeMapper.batchUnpublish(gradeIds);
            if (affected > 0 && !unpublishedGradeList.isEmpty()) {
                try {
                    notifyGradeEvent(EVENT_CODE_GRADE_UNPUBLISHED, unpublishedGradeList, dto.getTeacherId());
                } catch (Exception notifyException) {
                    log.warn("成绩撤销发布事件上报失败，teacherId={}, gradeIds={}, error={}",
                            dto.getTeacherId(), gradeIds, notifyException.getMessage());
                }
            }
            Map<String, Object> data = new HashMap<>();
            data.put("count", affected);
            return Result.success("撤销成绩发布成功", data);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("撤销成绩发布失败：" + e.getMessage());
        }
    }
    
    @Override
    public IPage<GradeVo> getTeacherCourseGrades(Long courseId, Long teacherId, GradeQuery query) {
        // 参数校验：课程ID、教师ID不能为空
        if (courseId == null) {
            throw new BizException("课程ID不能为空");
        }
        if (teacherId == null) {
            throw new BizException("教师ID不能为空");
        }

        // 权限校验：仅课程所属教师可查看该课程成绩
        CourseBasicInfo courseInfo = getCourseBasicInfo(courseId);
        validateTeacherAuthority(teacherId, courseInfo.getTeacherId());

        // 分页查询课程成绩（支持按是否通过、是否发布、学生ID筛选）
        GradeQuery safeQuery = query == null ? new GradeQuery() : query;
        Page<GradeVo> page = new Page<>(safeQuery.getPageNum(), safeQuery.getPageSize());
        Page<GradeVo> resultPage = gradeMapper.selectTeacherCourseGradePage(page, courseId, safeQuery);

        // 回填课程名/教师名/学生学号姓名/发布标记，便于前端直接展示
        fillCourseAndTeacherInfo(resultPage.getRecords());
        fillStudentInfo(resultPage.getRecords());
        fillPublishedFlag(resultPage.getRecords());
        return resultPage;
    }

    @Override
    public List<GradeVo> exportCourseGrades(Long courseId, Long teacherId) {
        // 参数校验
        if (courseId == null) {
            throw new BizException("课程ID不能为空");
        }
        if (teacherId == null) {
            throw new BizException("教师ID不能为空");
        }

        // 权限校验：仅课程所属教师可导出成绩
        CourseBasicInfo courseInfo = getCourseBasicInfo(courseId);
        validateTeacherAuthority(teacherId, courseInfo.getTeacherId());

        // 查询导出数据（当前返回列表，后续可扩展为 Excel 文件流）
        List<GradeVo> gradeList = gradeMapper.selectTeacherCourseGradeList(courseId);
        // 回填展示字段
        fillCourseAndTeacherInfo(gradeList);
        fillStudentInfo(gradeList);
        fillPublishedFlag(gradeList);
        return gradeList;
    }

    @Override
    public TeacherCourseGradeStatisticsVo getTeacherCourseStatistics(Long courseId, Long teacherId) {
        // 参数校验
        if (courseId == null) {
            throw new BizException("课程ID不能为空");
        }
        if (teacherId == null) {
            throw new BizException("教师ID不能为空");
        }

        // 权限校验
        CourseBasicInfo courseInfo = getCourseBasicInfo(courseId);
        validateTeacherAuthority(teacherId, courseInfo.getTeacherId());

        // 读取统计原始数据：概览 + 等级分布 + 分数段分布
        Map<String, Object> statisticsMap = gradeMapper.selectTeacherCourseStatistics(courseId);
        List<Map<String, Object>> gradeDistributionRows = gradeMapper.selectTeacherCourseGradeDistribution(courseId);
        List<Map<String, Object>> scoreRangeRows = gradeMapper.selectTeacherCourseScoreRangeDistribution(courseId);
        if (gradeDistributionRows == null) {
            gradeDistributionRows = new ArrayList<>();
        }
        if (scoreRangeRows == null) {
            scoreRangeRows = new ArrayList<>();
        }

        int totalCount = getIntegerValue(statisticsMap == null ? null : statisticsMap.get("totalCount"));
        int passCount = getIntegerValue(statisticsMap == null ? null : statisticsMap.get("passCount"));

        // 构建统计VO
        TeacherCourseGradeStatisticsVo vo = new TeacherCourseGradeStatisticsVo();
        vo.setCourseId(courseId);
        vo.setCourseName(courseInfo.getCourseName());
        vo.setTotalStudents(totalCount);
        vo.setPassCount(passCount);
        vo.setAverageScore(getBigDecimalValue(statisticsMap == null ? null : statisticsMap.get("averageScore")));
        vo.setMaxScore(getBigDecimalValue(statisticsMap == null ? null : statisticsMap.get("maxScore")));
        vo.setMinScore(getBigDecimalValue(statisticsMap == null ? null : statisticsMap.get("minScore")));

        // 计算及格率（百分比）
        if (totalCount <= 0) {
            vo.setPassRate(BigDecimal.ZERO);
        } else {
            vo.setPassRate(new BigDecimal(passCount)
                    .multiply(HUNDRED)
                    .divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP));
        }

        // 等级分布回填（A/B/C/D/F）
        Map<String, Integer> gradeDistribution = buildDefaultGradeDistribution();
        for (Map<String, Object> row : gradeDistributionRows) {
            String gradeLevel = getStringValue(row, "gradeLevel", "grade_level");
            if (!StringUtils.hasText(gradeLevel)) {
                continue;
            }
            gradeDistribution.put(gradeLevel, getIntegerValue(row.get("count")));
        }
        vo.setGradeDistribution(gradeDistribution);

        // 分数段分布回填（90-100/80-89/70-79/60-69/0-59）
        Map<String, Integer> scoreRangeDistribution = buildDefaultScoreRangeDistribution();
        for (Map<String, Object> row : scoreRangeRows) {
            String scoreRange = getStringValue(row, "scoreRange", "score_range");
            if (!StringUtils.hasText(scoreRange)) {
                continue;
            }
            scoreRangeDistribution.put(scoreRange, getIntegerValue(row.get("count")));
        }
        vo.setScoreRangeDistribution(scoreRangeDistribution);
        return vo;
    }

    @Override
    public IPage<GradeVo> getMyGrades(GradeQuery query) {
        IPage<GradeVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<GradeVo> resultPage = gradeMapper.selectGradePage((Page<GradeVo>) page, query);

        fillCourseAndTeacherInfo(resultPage.getRecords());
        
        return resultPage;
    }
    
    @Override
    public GradeVo getCourseGrade(Long courseId, Long studentId) {
        GradeVo vo = gradeMapper.selectGradeByCourseAndStudent(courseId, studentId);
        if (vo == null) {
            throw new BizException("未找到该课程的成绩记录");
        }

        fillCourseAndTeacherInfo(List.of(vo));
        
        return vo;
    }
    
    @Override
    public GradeStatisticsVo getStatistics(Long studentId) {
        GradeStatisticsVo vo = new GradeStatisticsVo();
        
        // 1. 统计总学分
        BigDecimal totalCredits = gradeMapper.selectTotalCredits(studentId);
        vo.setTotalCredits(totalCredits != null ? totalCredits : BigDecimal.ZERO);
        
        // 2. 统计平均GPA
        BigDecimal averageGpa = gradeMapper.selectAverageGpa(studentId);
        vo.setAverageGpa(averageGpa != null ? averageGpa.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        
        // 3. 统计成绩分布
        Map<String, Integer> gradeDistribution = buildDefaultGradeDistribution();
        List<Map<String, Object>> distributionList = gradeMapper.selectGradeDistribution(studentId);
        for (Map<String, Object> item : distributionList) {
            String gradeLevel = (String) item.get("gradeLevel");
            if (!StringUtils.hasText(gradeLevel)) {
                continue;
            }
            gradeDistribution.put(gradeLevel, getIntegerValue(item.get("count")));
        }
        vo.setGradeDistribution(gradeDistribution);
        
        // 4. 查询各课程成绩趋势
        List<Map<String, Object>> trendList = gradeMapper.selectCourseGradeTrends(studentId);
        fillCourseNameForMapList(trendList);
        List<GradeStatisticsVo.CourseGradeTrend> courseGradeTrends = new ArrayList<>();
        for (Map<String, Object> trend : trendList) {
            GradeStatisticsVo.CourseGradeTrend courseTrend = new GradeStatisticsVo.CourseGradeTrend();
            courseTrend.setCourseId(getLongValue(trend.get("courseId")));
            courseTrend.setCourseName((String) trend.get("courseName"));
            courseTrend.setFinalScore(getBigDecimalValue(trend.get("finalScore")));
            courseTrend.setGradeLevel((String) trend.get("gradeLevel"));
            if (trend.get("publishedTime") != null) {
                courseTrend.setPublishedTime(trend.get("publishedTime").toString());
            }
            courseGradeTrends.add(courseTrend);
        }
        vo.setCourseGradeTrends(courseGradeTrends);
        
        return vo;
    }
    
    @Override
    public CreditSummaryVo getCreditSummary(Long studentId) {
        CreditSummaryVo vo = new CreditSummaryVo();
        
        // 1. 统计总学分
        BigDecimal totalCredits = gradeMapper.selectTotalCredits(studentId);
        vo.setTotalCredits(totalCredits != null ? totalCredits : BigDecimal.ZERO);
        
        // 2. 查询各课程学分明细
        List<Map<String, Object>> creditList = gradeMapper.selectCourseCredits(studentId);
        fillCourseNameForMapList(creditList);
        List<CreditSummaryVo.CourseCredit> courseCredits = new ArrayList<>();
        for (Map<String, Object> credit : creditList) {
            CreditSummaryVo.CourseCredit courseCredit = new CreditSummaryVo.CourseCredit();
            courseCredit.setCourseId(getLongValue(credit.get("courseId")));
            courseCredit.setCourseName((String) credit.get("courseName"));
            BigDecimal creditEarned = getBigDecimalValue(credit.get("creditEarned"));
            courseCredit.setCreditEarned(creditEarned != null ? creditEarned : BigDecimal.ZERO);
            courseCredit.setGradeLevel((String) credit.get("gradeLevel"));
            courseCredits.add(courseCredit);
        }
        vo.setCourseCredits(courseCredits);
        
        // 3. 按学期/学年分组统计
        List<Map<String, Object>> semesterList = gradeMapper.selectSemesterCredits(studentId);
        List<CreditSummaryVo.SemesterCredit> semesterCredits = new ArrayList<>();
        for (Map<String, Object> item : semesterList) {
            CreditSummaryVo.SemesterCredit semesterCredit = new CreditSummaryVo.SemesterCredit();
            semesterCredit.setSemester((String) item.get("semester"));
            BigDecimal semesterTotalCredits = getBigDecimalValue(item.get("totalCredits"));
            semesterCredit.setTotalCredits(semesterTotalCredits != null ? semesterTotalCredits : BigDecimal.ZERO);
            semesterCredit.setCourseCount(getIntegerValue(item.get("courseCount")));
            semesterCredits.add(semesterCredit);
        }
        vo.setSemesterCredits(semesterCredits);
        
        return vo;
    }
    
    @Override
    public GpaVo getGpa(Long studentId) {
        GpaVo vo = new GpaVo();
        
        // 1. 统计总GPA（加权平均）
        BigDecimal totalGpa = gradeMapper.selectAverageGpa(studentId);
        vo.setTotalGpa(totalGpa != null ? totalGpa.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        
        // 2. 查询各课程GPA明细
        List<Map<String, Object>> gpaList = gradeMapper.selectGpaDetails(studentId);
        fillCourseNameForMapList(gpaList);
        List<GpaVo.CourseGpa> courseGpaList = new ArrayList<>();
        for (Map<String, Object> gpa : gpaList) {
            GpaVo.CourseGpa courseGpa = new GpaVo.CourseGpa();
            courseGpa.setCourseId(getLongValue(gpa.get("courseId")));
            courseGpa.setCourseName((String) gpa.get("courseName"));
            courseGpa.setGradeLevel((String) gpa.get("gradeLevel"));
            courseGpa.setGpa(getBigDecimalValue(gpa.get("gpa")));
            courseGpa.setCreditEarned(getBigDecimalValue(gpa.get("creditEarned")));
            courseGpaList.add(courseGpa);
        }
        vo.setCourseGpaList(courseGpaList);
        
        // 3. GPA趋势
        List<Map<String, Object>> gpaTrends = gradeMapper.selectGpaTrends(studentId);
        List<GpaVo.GpaTrend> gpaTrendList = new ArrayList<>();
        for (Map<String, Object> trend : gpaTrends) {
            GpaVo.GpaTrend gpaTrend = new GpaVo.GpaTrend();
            gpaTrend.setPeriod((String) trend.get("period"));
            BigDecimal trendGpa = getBigDecimalValue(trend.get("gpa"));
            gpaTrend.setGpa(trendGpa != null ? trendGpa.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            gpaTrendList.add(gpaTrend);
        }
        vo.setGpaTrends(gpaTrendList);
        
        return vo;
    }

    private Grade buildNewGradeEntity(GradeCreateDto dto, CourseBasicInfo cachedCourseInfo) {
        validateCreateDto(dto);

        CourseBasicInfo courseInfo = cachedCourseInfo != null ? cachedCourseInfo : getCourseBasicInfo(dto.getCourseId());
        validateTeacherAuthority(dto.getTeacherId(), courseInfo.getTeacherId());
        validateStudentExists(dto.getStudentId());

        StudentCourse studentCourse = studentCourseMapper.selectByStudentIdAndCourseId(dto.getStudentId(), dto.getCourseId());
        if (studentCourse == null) {
            throw new BizException("学生未选该课程，无法录入成绩");
        }

        Grade existing = gradeMapper.selectEntityByCourseAndStudent(dto.getCourseId(), dto.getStudentId());
        if (existing != null) {
            throw new BizException("该学生在当前课程已有成绩记录，请使用修改接口");
        }

        Grade grade = new Grade();
        grade.setStudentId(dto.getStudentId());
        grade.setCourseId(dto.getCourseId());
        grade.setTeacherId(dto.getTeacherId());
        grade.setCourseName(StringUtils.hasText(courseInfo.getCourseName()) ? courseInfo.getCourseName() : studentCourse.getCourseName());
        grade.setAttendanceScore(normalizeScore(dto.getAttendanceScore(), "考勤成绩"));
        grade.setHomeworkScore(normalizeScore(dto.getHomeworkScore(), "作业成绩"));
        grade.setQuizScore(normalizeScore(dto.getQuizScore(), "测验成绩"));
        grade.setExamScore(normalizeScore(dto.getExamScore(), "考试成绩"));
        fillCalculatedFields(grade, courseInfo, getWeightConfig(dto.getCourseId()));
        grade.setPublishedTime(null);
        grade.setPublishedBy(null);
        return grade;
    }

    private void validateCreateDto(GradeCreateDto dto) {
        if (dto == null) {
            throw new BizException("录入参数不能为空");
        }
        if (dto.getStudentId() == null) {
            throw new BizException("学生ID不能为空");
        }
        if (dto.getCourseId() == null) {
            throw new BizException("课程ID不能为空");
        }
        if (dto.getTeacherId() == null) {
            throw new BizException("教师ID不能为空");
        }
        normalizeScore(dto.getAttendanceScore(), "考勤成绩");
        normalizeScore(dto.getHomeworkScore(), "作业成绩");
        normalizeScore(dto.getQuizScore(), "测验成绩");
        normalizeScore(dto.getExamScore(), "考试成绩");
    }

    private GradeWeightConfig buildWeightConfig(GradeWeightDto dto) {
        if (dto.getAttendanceWeight() == null || dto.getHomeworkWeight() == null
                || dto.getQuizWeight() == null || dto.getExamWeight() == null) {
            throw new BizException("成绩权重不能为空");
        }

        GradeWeightConfig config = new GradeWeightConfig(
                dto.getAttendanceWeight(),
                dto.getHomeworkWeight(),
                dto.getQuizWeight(),
                dto.getExamWeight()
        );

        if (config.getAttendanceWeight().compareTo(BigDecimal.ZERO) < 0
                || config.getHomeworkWeight().compareTo(BigDecimal.ZERO) < 0
                || config.getQuizWeight().compareTo(BigDecimal.ZERO) < 0
                || config.getExamWeight().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("成绩权重不能为负数");
        }

        BigDecimal sum = config.getAttendanceWeight()
                .add(config.getHomeworkWeight())
                .add(config.getQuizWeight())
                .add(config.getExamWeight());
        if (sum.compareTo(HUNDRED) != 0) {
            throw new BizException("权重总和必须为100%");
        }
        return config;
    }

    private void fillCalculatedFields(Grade grade, CourseBasicInfo courseInfo, GradeWeightConfig weightConfig) {
        BigDecimal attendanceScore = safeScore(grade.getAttendanceScore());
        BigDecimal homeworkScore = safeScore(grade.getHomeworkScore());
        BigDecimal quizScore = safeScore(grade.getQuizScore());
        BigDecimal examScore = safeScore(grade.getExamScore());

        GradeWeightConfig config = weightConfig == null ? DEFAULT_WEIGHT_CONFIG : weightConfig;

        BigDecimal finalScore = attendanceScore.multiply(config.getAttendanceWeight())
                .add(homeworkScore.multiply(config.getHomeworkWeight()))
                .add(quizScore.multiply(config.getQuizWeight()))
                .add(examScore.multiply(config.getExamWeight()))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);

        String gradeLevel = toGradeLevel(finalScore);
        BigDecimal gpa = toGpa(gradeLevel);
        boolean pass = finalScore.compareTo(PASS_SCORE) >= 0;

        BigDecimal courseCredit = courseInfo != null && courseInfo.getCredit() != null
                ? courseInfo.getCredit()
                : BigDecimal.ZERO;

        grade.setFinalScore(finalScore);
        grade.setGradeLevel(gradeLevel);
        grade.setGpa(gpa);
        grade.setIsPass(pass);
        grade.setCreditEarned(pass ? courseCredit : BigDecimal.ZERO);
    }

    private GradeWeightConfig getWeightConfig(Long courseId) {
        if (courseId == null) {
            return DEFAULT_WEIGHT_CONFIG;
        }
        return courseWeightConfigMap.getOrDefault(courseId, DEFAULT_WEIGHT_CONFIG);
    }

    private String toGradeLevel(BigDecimal finalScore) {
        if (finalScore.compareTo(new BigDecimal("90")) >= 0) {
            return "A";
        }
        if (finalScore.compareTo(new BigDecimal("80")) >= 0) {
            return "B";
        }
        if (finalScore.compareTo(new BigDecimal("70")) >= 0) {
            return "C";
        }
        if (finalScore.compareTo(new BigDecimal("60")) >= 0) {
            return "D";
        }
        return "F";
    }

    private BigDecimal toGpa(String gradeLevel) {
        if (!StringUtils.hasText(gradeLevel)) {
            return BigDecimal.ZERO;
        }
        return switch (gradeLevel) {
            case "A" -> new BigDecimal("4.0");
            case "B" -> new BigDecimal("3.0");
            case "C" -> new BigDecimal("2.0");
            case "D" -> new BigDecimal("1.0");
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal normalizeScore(BigDecimal score, String fieldName) {
        if (score == null) {
            return BigDecimal.ZERO;
        }
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(HUNDRED) > 0) {
            throw new BizException(fieldName + "必须在0-100之间");
        }
        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeScore(BigDecimal score) {
        if (score == null) {
            return BigDecimal.ZERO;
        }
        return score;
    }

    private List<Long> sanitizeGradeIds(List<Long> gradeIds) {
        if (gradeIds == null) {
            return new ArrayList<>();
        }
        return gradeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private CourseBasicInfo getCourseBasicInfo(Long courseId) {
        if (courseId == null) {
            throw new BizException("课程ID不能为空");
        }

        try {
            Result result = educationCourseClient.getCourseById(courseId);
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                throw new BizException("课程不存在或不可用");
            }

            Map<String, Object> courseData = toMap(result.getData());
            if (courseData == null || courseData.isEmpty()) {
                throw new BizException("课程信息格式错误");
            }

            CourseBasicInfo info = new CourseBasicInfo();
            info.setCourseId(courseId);
            info.setCourseName(getStringValue(courseData, "courseName", "course_name"));
            info.setTeacherId(getLongValue(firstNonNull(courseData.get("teacherId"), courseData.get("teacher_id"))));
            info.setCredit(getBigDecimalValue(firstNonNull(courseData.get("credit"), courseData.get("courseCredit"))));
            if (info.getCredit() == null) {
                info.setCredit(BigDecimal.ZERO);
            }
            return info;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("获取课程信息失败：" + e.getMessage());
        }
    }

    private void validateStudentExists(Long studentId) {
        if (studentId == null) {
            throw new BizException("学生ID不能为空");
        }
        try {
            Result result = educationUserStudentClient.getStudentById(studentId);
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                throw new BizException("学生不存在");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("获取学生信息失败：" + e.getMessage());
        }
    }

    private void validateTeacherAuthority(Long requestTeacherId, Long courseTeacherId) {
        if (requestTeacherId == null) {
            throw new BizException("教师ID不能为空");
        }

        try {
            Result teacherResult = educationUserTeacherClient.getTeachersNameById(requestTeacherId);
            if (teacherResult == null || teacherResult.getCode() != 200 || teacherResult.getData() == null) {
                throw new BizException("教师不存在");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("获取教师信息失败：" + e.getMessage());
        }

        if (courseTeacherId != null && !Objects.equals(requestTeacherId, courseTeacherId)) {
            throw new BizException("仅课程所属教师可操作成绩");
        }
    }

    /**
     * 批量填充课程名称和教师姓名
     */
    private void fillCourseAndTeacherInfo(List<GradeVo> gradeVoList) {
        if (gradeVoList == null || gradeVoList.isEmpty()) {
            return;
        }

        Set<Long> courseIds = gradeVoList.stream()
                .map(GradeVo::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> teacherIds = gradeVoList.stream()
                .map(GradeVo::getTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> courseNameMap = fetchCourseNameMap(courseIds);
        Map<Long, String> teacherNameMap = fetchTeacherNameMap(teacherIds);

        for (GradeVo vo : gradeVoList) {
            if (!StringUtils.hasText(vo.getCourseName()) && vo.getCourseId() != null) {
                String courseName = courseNameMap.get(vo.getCourseId());
                if (StringUtils.hasText(courseName)) {
                    vo.setCourseName(courseName);
                }
            }

            if (vo.getTeacherId() != null) {
                String teacherName = teacherNameMap.get(vo.getTeacherId());
                if (StringUtils.hasText(teacherName)) {
                    vo.setTeacherName(teacherName);
                }
            }
        }
    }

    /**
     * 批量填充学生信息
     */
    private void fillStudentInfo(List<GradeVo> gradeVoList) {
        if (gradeVoList == null || gradeVoList.isEmpty()) {
            return;
        }

        Set<Long> studentIds = gradeVoList.stream()
                .map(GradeVo::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 通过 openfeign 调用 education-user 服务补齐学生信息（跨服务不直连实体）
        Map<Long, Map<String, Object>> studentMap = new HashMap<>();
        for (Long studentId : studentIds) {
            try {
                Result studentResult = educationUserStudentClient.getStudentById(studentId);
                if (studentResult == null || studentResult.getCode() != 200 || studentResult.getData() == null) {
                    continue;
                }
                Map<String, Object> studentInfo = toMap(studentResult.getData());
                if (studentInfo != null && !studentInfo.isEmpty()) {
                    studentMap.put(studentId, studentInfo);
                }
            } catch (Exception e) {
                log.warn("获取学生信息失败，studentId: {}, error: {}", studentId, e.getMessage());
            }
        }

        for (GradeVo vo : gradeVoList) {
            Map<String, Object> studentInfo = studentMap.get(vo.getStudentId());
            if (studentInfo == null) {
                continue;
            }
            vo.setStudentNumber(getStringValue(studentInfo, "studentNumber", "student_number"));
            String studentName = getStringValue(studentInfo, "realName", "studentName");
            if (!StringUtils.hasText(studentName)) {
                studentName = getStringValue(studentInfo, "nickname", "name");
            }
            vo.setStudentName(studentName);
        }
    }

    /**
     * 回填是否发布标记（publishedTime 非空即视为已发布）
     */
    private void fillPublishedFlag(List<GradeVo> gradeVoList) {
        if (gradeVoList == null || gradeVoList.isEmpty()) {
            return;
        }
        for (GradeVo vo : gradeVoList) {
            vo.setPublished(vo.getPublishedTime() != null);
        }
    }

    /**
     * 为Map结构的结果列表补全课程名称
     */
    private void fillCourseNameForMapList(List<Map<String, Object>> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        Set<Long> courseIds = dataList.stream()
                .filter(item -> !hasText(item.get("courseName")))
                .map(item -> getLongValue(item.get("courseId")))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (courseIds.isEmpty()) {
            return;
        }

        Map<Long, String> courseNameMap = fetchCourseNameMap(courseIds);
        for (Map<String, Object> item : dataList) {
            if (hasText(item.get("courseName"))) {
                continue;
            }
            Long courseId = getLongValue(item.get("courseId"));
            if (courseId == null) {
                continue;
            }
            String courseName = courseNameMap.get(courseId);
            if (StringUtils.hasText(courseName)) {
                item.put("courseName", courseName);
            }
        }
    }

    private Map<Long, String> fetchCourseNameMap(Set<Long> courseIds) {
        Map<Long, String> courseNameMap = new HashMap<>();
        for (Long courseId : courseIds) {
            try {
                Result courseResult = educationCourseClient.getCourseById(courseId);
                if (courseResult == null || courseResult.getCode() != 200 || courseResult.getData() == null) {
                    continue;
                }

                Map<String, Object> courseData = toMap(courseResult.getData());
                if (courseData == null || courseData.isEmpty()) {
                    continue;
                }

                Object courseNameObj = courseData.get("courseName");
                if (courseNameObj == null) {
                    courseNameObj = courseData.get("course_name");
                }

                if (courseNameObj != null && StringUtils.hasText(courseNameObj.toString())) {
                    courseNameMap.put(courseId, courseNameObj.toString());
                }
            } catch (Exception e) {
                log.warn("获取课程信息失败，courseId: {}, error: {}", courseId, e.getMessage());
            }
        }
        return courseNameMap;
    }

    private Map<Long, String> fetchTeacherNameMap(Set<Long> teacherIds) {
        Map<Long, String> teacherNameMap = new HashMap<>();
        for (Long teacherId : teacherIds) {
            try {
                Result teacherResult = educationUserTeacherClient.getTeachersNameById(teacherId);
                if (teacherResult == null || teacherResult.getCode() != 200 || teacherResult.getData() == null) {
                    continue;
                }

                Object teacherData = teacherResult.getData();
                String teacherName = null;
                if (teacherData instanceof Map<?, ?> teacherMap) {
                    Object name = teacherMap.get("teacherName");
                    if (name == null) {
                        name = teacherMap.get("name");
                    }
                    if (name == null) {
                        name = teacherMap.get("realName");
                    }
                    if (name != null) {
                        teacherName = name.toString();
                    }
                } else {
                    teacherName = teacherData.toString();
                }

                if (StringUtils.hasText(teacherName)) {
                    teacherNameMap.put(teacherId, teacherName);
                }
            } catch (Exception e) {
                log.warn("获取教师姓名失败，teacherId: {}, error: {}", teacherId, e.getMessage());
            }
        }
        return teacherNameMap;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        String jsonStr = JSONUtil.toJsonStr(data);
        return JSONUtil.toBean(jsonStr, Map.class);
    }

    private Map<String, Integer> buildDefaultGradeDistribution() {
        Map<String, Integer> gradeDistribution = new LinkedHashMap<>();
        gradeDistribution.put("A", 0);
        gradeDistribution.put("B", 0);
        gradeDistribution.put("C", 0);
        gradeDistribution.put("D", 0);
        gradeDistribution.put("F", 0);
        return gradeDistribution;
    }

    private Map<String, Integer> buildDefaultScoreRangeDistribution() {
        // 保证分数段字段完整返回，避免前端缺 key 额外判空
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("90-100", 0);
        distribution.put("80-89", 0);
        distribution.put("70-79", 0);
        distribution.put("60-69", 0);
        distribution.put("0-59", 0);
        return distribution;
    }

    private Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }

    private String getStringValue(Map<String, Object> map, String firstKey, String secondKey) {
        Object value = map.get(firstKey);
        if (value == null) {
            value = map.get(secondKey);
        }
        return value == null ? null : value.toString();
    }

    private Long getLongValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getIntegerValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private BigDecimal getBigDecimalValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private void notifyGradeEvent(String eventCode, List<Grade> gradeList, Long teacherId) {
        if (!StringUtils.hasText(eventCode) || gradeList == null || gradeList.isEmpty() || teacherId == null) {
            return;
        }

        for (Grade grade : gradeList) {
            if (grade == null || grade.getId() == null || grade.getStudentId() == null) {
                continue;
            }

            Long studentUserId = resolveStudentUserId(grade.getStudentId());
            if (studentUserId == null || studentUserId <= 0) {
                continue;
            }

            TemplateTriggerEventRequest payload = new TemplateTriggerEventRequest();
            payload.setEventId(buildGradeEventId(eventCode, grade.getId()));
            payload.setEventCode(eventCode);
            payload.setTargetType(TARGET_TYPE_USER);
            payload.setTargetValue(List.of(studentUserId));
            payload.setParams(buildGradeEventParams(grade));
            payload.setMessageType(MESSAGE_TYPE_COURSE);
            payload.setRelatedId(grade.getId());
            payload.setRelatedType(MESSAGE_RELATED_TYPE_GRADE);
            payload.setPriority(MESSAGE_PRIORITY_NORMAL);
            payload.setDeliverMode(DELIVER_MODE_MQ);

            try {
                Result triggerResult = educationMessageInternalClient.triggerEvent(teacherId, ROLE_TEACHER, payload);
                if (triggerResult == null || triggerResult.getCode() == null || triggerResult.getCode() != 200) {
                    log.warn("成绩事件触发失败，eventCode={}, gradeId={}, studentUserId={}, message={}",
                            eventCode, grade.getId(), studentUserId,
                            triggerResult == null ? null : triggerResult.getMessage());
                }
            } catch (Exception e) {
                log.warn("成绩事件触发异常，eventCode={}, gradeId={}, studentUserId={}, error={}",
                        eventCode, grade.getId(), studentUserId, e.getMessage());
            }
        }
    }

    private Map<String, Object> buildGradeEventParams(Grade grade) {
        Map<String, Object> params = new HashMap<>();
        params.put("course_name", StringUtils.hasText(grade.getCourseName()) ? grade.getCourseName().trim() : "课程成绩");
        params.put("final_score", grade.getFinalScore() == null ? "" : grade.getFinalScore());
        params.put("grade_level", StringUtils.hasText(grade.getGradeLevel()) ? grade.getGradeLevel() : "");
        params.put("is_pass", toPassText(grade.getIsPass()));
        return params;
    }

    private String toPassText(Boolean isPass) {
        if (isPass == null) {
            return "";
        }
        return Boolean.TRUE.equals(isPass) ? "通过" : "未通过";
    }

    private Long resolveStudentUserId(Long studentId) {
        if (studentId == null || studentId <= 0) {
            return null;
        }
        try {
            Result studentResult = educationUserStudentClient.getStudentById(studentId);
            if (studentResult == null || studentResult.getCode() == null || studentResult.getCode() != 200
                    || studentResult.getData() == null) {
                return null;
            }
            Map<String, Object> studentMap = toMap(studentResult.getData());
            if (studentMap == null || studentMap.isEmpty()) {
                return null;
            }
            Object userIdValue = studentMap.get("userId");
            if (userIdValue == null) {
                userIdValue = studentMap.get("user_id");
            }
            return getLongValue(userIdValue);
        } catch (Exception e) {
            log.warn("查询学生用户映射失败，studentId={}, error={}", studentId, e.getMessage());
            return null;
        }
    }

    private String buildGradeEventId(String eventCode, Long gradeId) {
        String safeEventCode = StringUtils.hasText(eventCode) ? eventCode.trim().toUpperCase() : "GRADE_EVENT";
        return safeEventCode + "_" + gradeId + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    private boolean hasText(Object value) {
        return value != null && StringUtils.hasText(value.toString());
    }

    @Data
    private static class CourseBasicInfo {
        private Long courseId;
        private String courseName;
        private Long teacherId;
        private BigDecimal credit;
    }

    @Data
    @AllArgsConstructor
    private static class GradeWeightConfig {
        /**
         * 考勤权重（百分比）
         */
        private BigDecimal attendanceWeight;
        /**
         * 作业权重（百分比）
         */
        private BigDecimal homeworkWeight;
        /**
         * 测验权重（百分比）
         */
        private BigDecimal quizWeight;
        /**
         * 考试权重（百分比）
         */
        private BigDecimal examWeight;
    }
}
















