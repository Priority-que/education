package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.Homework;
import com.xixi.entity.HomeworkSubmission;
import com.xixi.entity.HomeworkSubmissionAnnotation;
import com.xixi.entity.StudentCourse;
import com.xixi.exception.BizException;
import com.xixi.mapper.HomeworkMapper;
import com.xixi.mapper.HomeworkSubmissionAnnotationMapper;
import com.xixi.mapper.HomeworkSubmissionMapper;
import com.xixi.mapper.StudentCourseMapper;
import com.xixi.openfeign.course.EducationCourseClient;
import com.xixi.openfeign.message.EducationMessageInternalClient;
import com.xixi.openfeign.message.dto.TemplateTriggerEventRequest;
import com.xixi.openfeign.user.EducationUserStudentClient;
import com.xixi.openfeign.user.EducationUserTeacherClient;
import com.xixi.pojo.dto.HomeworkCreateDto;
import com.xixi.pojo.dto.HomeworkSubmissionDto;
import com.xixi.pojo.dto.HomeworkSubmissionGradeDto;
import com.xixi.pojo.dto.HomeworkUpdateDto;
import com.xixi.pojo.query.HomeworkQuery;
import com.xixi.pojo.query.HomeworkSubmissionQuery;
import com.xixi.pojo.vo.HomeworkSubmissionAnalysisVo;
import com.xixi.pojo.vo.HomeworkSubmissionAttachmentVo;
import com.xixi.pojo.vo.HomeworkSubmissionStatisticsVo;
import com.xixi.pojo.vo.HomeworkSubmissionVo;
import com.xixi.pojo.vo.HomeworkVo;
import com.xixi.service.HomeworkService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 作业服务实现，统一使用 UTF-8 注释与文案。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeworkServiceImpl implements HomeworkService {

    private static final String HOMEWORK_STATUS_DRAFT = "DRAFT";
    private static final String HOMEWORK_STATUS_PUBLISHED = "PUBLISHED";
    private static final String HOMEWORK_STATUS_CLOSED = "CLOSED";

    private static final String MESSAGE_TYPE_COURSE = "COURSE";
    private static final String MESSAGE_RELATED_TYPE_HOMEWORK = "HOMEWORK";
    private static final String EVENT_CODE_HOMEWORK_PUBLISHED = "HOMEWORK_PUBLISHED";
    private static final String EVENT_CODE_HOMEWORK_GRADED = "HOMEWORK_GRADED";
    private static final String TARGET_TYPE_USER = "USER";
    private static final String DELIVER_MODE_MQ = "MQ";

    private static final String LEARNING_STATUS_DROPPED = "DROPPED";

    private static final int ROLE_TEACHER = 3;
    private static final int MESSAGE_PRIORITY_NORMAL = 0;

    private static final String SUBMISSION_STATUS_SUBMITTED = "SUBMITTED";
    private static final String SUBMISSION_STATUS_LATE = "LATE";
    private static final String SUBMISSION_STATUS_GRADED = "GRADED";
    private static final String SUBMISSION_STATUS_DRAFT = "DRAFT";

    private final HomeworkMapper homeworkMapper;
    private final HomeworkSubmissionMapper homeworkSubmissionMapper;
    private final HomeworkSubmissionAnnotationMapper homeworkSubmissionAnnotationMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final EducationCourseClient educationCourseClient;
    private final EducationMessageInternalClient educationMessageInternalClient;
    private final EducationUserStudentClient educationUserStudentClient;
    private final EducationUserTeacherClient educationUserTeacherClient;

    // ========================== 学生端：作业列表与详情 ==========================

    @Override
    public IPage<HomeworkVo> getCourseHomeworkList(HomeworkQuery query) {
        if (query == null) {
            query = new HomeworkQuery();
        }

        IPage<HomeworkVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<HomeworkVo> resultPage = homeworkMapper.selectHomeworkPage((Page<HomeworkVo>) page, query);

        if (query.getStudentId() != null) {
            for (HomeworkVo vo : resultPage.getRecords()) {
                HomeworkSubmission submission = homeworkSubmissionMapper.selectByHomeworkAndStudent(
                        vo.getId(), query.getStudentId());
                if (submission != null) {
                    vo.setSubmissionStatus(submission.getStatus());
                    vo.setScore(submission.getScore());
                } else {
                    vo.setSubmissionStatus("NOT_SUBMITTED");
                }
            }
        }

        fillCourseAndTeacherInfo(resultPage.getRecords());
        return resultPage;
    }

    @Override
    public HomeworkVo getHomeworkDetail(Long homeworkId) {
        HomeworkVo vo = homeworkMapper.selectHomeworkDetail(homeworkId);
        if (vo == null) {
            throw new BizException("作业不存在");
        }

        fillCourseAndTeacherInfo(List.of(vo));
        return vo;
    }

    // ========================== 学生端：提交与修改作业 ==========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result submitHomework(HomeworkSubmissionDto dto) {
        try {
            Homework homework = homeworkMapper.selectById(dto.getHomeworkId());
            if (homework == null) {
                return Result.error("作业不存在");
            }
            if (!HOMEWORK_STATUS_PUBLISHED.equals(homework.getStatus())) {
                return Result.error("作业未发布，无法提交");
            }

            LocalDateTime now = LocalDateTime.now();
            String submissionStatus = SUBMISSION_STATUS_SUBMITTED;
            if (homework.getDeadline() != null && now.isAfter(homework.getDeadline())) {
                submissionStatus = SUBMISSION_STATUS_LATE;
            }

            HomeworkSubmission existingSubmission = homeworkSubmissionMapper.selectByHomeworkAndStudent(
                    dto.getHomeworkId(), dto.getStudentId());

            if (existingSubmission != null) {
                existingSubmission.setSubmissionContent(dto.getSubmissionContent());
                existingSubmission.setAttachmentUrl(dto.getAttachmentUrl());
                existingSubmission.setSubmissionTime(now);
                existingSubmission.setStatus(submissionStatus);
                existingSubmission.setUpdatedTime(now);
                homeworkSubmissionMapper.updateById(existingSubmission);
                return Result.success("作业提交更新成功");
            }

            HomeworkSubmission submission = new HomeworkSubmission();
            submission.setHomeworkId(dto.getHomeworkId());
            submission.setStudentId(dto.getStudentId());
            submission.setSubmissionContent(dto.getSubmissionContent());
            submission.setAttachmentUrl(dto.getAttachmentUrl());
            submission.setSubmissionTime(now);
            submission.setStatus(submissionStatus);
            homeworkSubmissionMapper.insert(submission);
            return Result.success("作业提交成功");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("提交作业失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateHomeworkSubmission(HomeworkSubmissionDto dto) {
        try {
            Homework homework = homeworkMapper.selectById(dto.getHomeworkId());
            if (homework == null) {
                return Result.error("作业不存在");
            }

            LocalDateTime now = LocalDateTime.now();
            if (homework.getDeadline() != null && now.isAfter(homework.getDeadline())) {
                return Result.error("作业已截止，无法修改");
            }

            HomeworkSubmission submission = homeworkSubmissionMapper.selectByHomeworkAndStudent(
                    dto.getHomeworkId(), dto.getStudentId());
            if (submission == null) {
                return Result.error("未找到提交记录");
            }

            submission.setSubmissionContent(dto.getSubmissionContent());
            submission.setAttachmentUrl(dto.getAttachmentUrl());
            submission.setSubmissionTime(now);
            submission.setUpdatedTime(now);
            homeworkSubmissionMapper.updateById(submission);

            return Result.success("作业修改成功");
        } catch (Exception e) {
            return Result.error("修改作业失败：" + e.getMessage());
        }
    }

    @Override
    public IPage<HomeworkSubmissionVo> getMySubmissions(HomeworkSubmissionQuery query) {
        if (query == null) {
            query = new HomeworkSubmissionQuery();
        }
        IPage<HomeworkSubmissionVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<HomeworkSubmissionVo> resultPage = homeworkSubmissionMapper.selectHomeworkSubmissionPage(
                (Page<HomeworkSubmissionVo>) page, query);
        fillCourseAndStudentInfo(resultPage.getRecords());
        fillSubmissionAnnotationInfo(resultPage.getRecords());
        return resultPage;
    }

    // ========================== 教师端：作业提交列表与详情 ==========================

    @Override
    public IPage<HomeworkSubmissionVo> getTeacherSubmissions(HomeworkSubmissionQuery query) {
        if (query == null) {
            query = new HomeworkSubmissionQuery();
        }
        if (query.getTeacherId() == null) {
            throw new BizException("teacherId 不能为空");
        }

        IPage<HomeworkSubmissionVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<HomeworkSubmissionVo> resultPage = homeworkSubmissionMapper.selectTeacherHomeworkSubmissionPage(
                (Page<HomeworkSubmissionVo>) page, query);
        fillCourseAndStudentInfo(resultPage.getRecords());
        fillSubmissionAnnotationInfo(resultPage.getRecords());
        return resultPage;
    }

    @Override
    public HomeworkSubmissionVo getSubmissionResult(Long submissionId) {
        HomeworkSubmissionVo vo = homeworkSubmissionMapper.selectSubmissionDetail(submissionId);
        if (vo == null) {
            throw new BizException("提交记录不存在");
        }
        fillCourseAndStudentInfo(List.of(vo));
        fillSubmissionAnnotationInfo(List.of(vo));
        return vo;
    }

    // ========================== 教师端：作业评分与统计 ==========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result gradeHomeworkSubmission(HomeworkSubmissionGradeDto dto) {
        try {
            if (dto == null || dto.getSubmissionId() == null || dto.getTeacherId() == null || dto.getScore() == null) {
                return Result.error("submissionId、teacherId、score 不能为空");
            }
            if (dto.getScore().compareTo(BigDecimal.ZERO) < 0) {
                return Result.error("得分不能小于 0");
            }

            HomeworkSubmissionVo teacherSubmission = homeworkSubmissionMapper.selectTeacherSubmissionDetail(
                    dto.getSubmissionId(), dto.getTeacherId());
            if (teacherSubmission == null) {
                return Result.error("提交记录不存在或无权限查看");
            }

            Integer homeworkTotalScore = teacherSubmission.getHomeworkTotalScore();
            if (homeworkTotalScore != null) {
                BigDecimal maxScore = BigDecimal.valueOf(homeworkTotalScore);
                if (dto.getScore().compareTo(maxScore) > 0) {
                    return Result.error("得分不能超过作业总分：" + homeworkTotalScore);
                }
            }

            HomeworkSubmission submission = homeworkSubmissionMapper.selectById(dto.getSubmissionId());
            if (submission == null) {
                return Result.error("提交记录不存在");
            }
            if (submission.getSubmissionTime() == null || SUBMISSION_STATUS_DRAFT.equals(submission.getStatus())) {
                return Result.error("作业尚未提交，无法评阅");
            }

            LocalDateTime now = LocalDateTime.now();
            submission.setScore(dto.getScore());
            submission.setFeedback(dto.getFeedback());
            submission.setGradedBy(dto.getTeacherId());
            submission.setGradedTime(now);
            submission.setStatus(SUBMISSION_STATUS_GRADED);
            submission.setUpdatedTime(now);
            homeworkSubmissionMapper.updateById(submission);
            saveOrUpdateSubmissionAnnotation(dto.getSubmissionId(), dto);
            notifyHomeworkSubmissionGraded(submission, teacherSubmission, dto.getTeacherId());

            return Result.success("作业评分成功");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("作业评分失败：" + e.getMessage());
        }
    }

    @Override
    public HomeworkSubmissionStatisticsVo getHomeworkSubmissionStatistics(Long homeworkId) {
        if (homeworkId == null) {
            throw new BizException("homeworkId 不能为空");
        }

        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new BizException("作业不存在");
        }

        Map<String, Object> summary = homeworkSubmissionMapper.selectHomeworkSubmissionStatistics(homeworkId);
        int totalSubmissions = safeInt(getIntegerValue(summary == null ? null : summary.get("totalSubmissions")));
        int gradedCount = safeInt(getIntegerValue(summary == null ? null : summary.get("gradedCount")));
        int lateCount = safeInt(getIntegerValue(summary == null ? null : summary.get("lateCount")));
        BigDecimal averageScore = getBigDecimalValue(summary == null ? null : summary.get("averageScore"))
                .setScale(2, RoundingMode.HALF_UP);

        HomeworkSubmissionStatisticsVo vo = new HomeworkSubmissionStatisticsVo();
        vo.setHomeworkId(homeworkId);
        vo.setHomeworkTitle(homework.getHomeworkTitle());
        vo.setTotalSubmissions(totalSubmissions);
        vo.setGradedCount(gradedCount);
        vo.setUngradedCount(Math.max(0, totalSubmissions - gradedCount));
        vo.setLateCount(lateCount);
        vo.setAverageScore(averageScore);

        Map<String, Integer> scoreDistribution = new LinkedHashMap<>();
        scoreDistribution.put("0-59", 0);
        scoreDistribution.put("60-69", 0);
        scoreDistribution.put("70-79", 0);
        scoreDistribution.put("80-89", 0);
        scoreDistribution.put("90-100", 0);

        List<Map<String, Object>> distributionRows = homeworkSubmissionMapper.selectHomeworkScoreDistribution(homeworkId);
        if (distributionRows != null) {
            for (Map<String, Object> row : distributionRows) {
                String range = getStringValue(row == null ? null : row.get("scoreRange"));
                if (range == null || !scoreDistribution.containsKey(range)) {
                    continue;
                }
                scoreDistribution.put(range, safeInt(getIntegerValue(row.get("scoreCount"))));
            }
        }
        vo.setScoreDistribution(scoreDistribution);
        return vo;
    }

    @Override
    public HomeworkSubmissionAnalysisVo getHomeworkAnalysis(Long courseId) {
        if (courseId == null) {
            throw new BizException("courseId 不能为空");
        }

        HomeworkSubmissionAnalysisVo vo = new HomeworkSubmissionAnalysisVo();
        vo.setCourseId(courseId);
        vo.setCourseName(resolveCourseName(courseId));

        List<Map<String, Object>> completionRows = homeworkSubmissionMapper.selectCourseHomeworkCompletionAnalysis(courseId);
        if (completionRows == null) {
            completionRows = new ArrayList<>();
        }

        List<HomeworkSubmissionAnalysisVo.HomeworkCompletionItem> completionList = new ArrayList<>();
        int totalSubmitted = 0;
        int totalLate = 0;
        int totalScored = 0;
        BigDecimal totalScore = BigDecimal.ZERO;

        for (Map<String, Object> row : completionRows) {
            HomeworkSubmissionAnalysisVo.HomeworkCompletionItem item =
                    new HomeworkSubmissionAnalysisVo.HomeworkCompletionItem();
            item.setHomeworkId(getLongValue(row.get("homeworkId")));
            item.setHomeworkTitle(getStringValue(row.get("homeworkTitle")));

            int totalStudents = safeInt(getIntegerValue(row.get("totalStudents")));
            int submittedCount = safeInt(getIntegerValue(row.get("submittedCount")));
            int lateCount = safeInt(getIntegerValue(row.get("lateCount")));
            int scoredCount = safeInt(getIntegerValue(row.get("scoredCount")));
            BigDecimal averageScore = getBigDecimalValue(row.get("averageScore"));

            item.setTotalStudents(totalStudents);
            item.setSubmittedCount(submittedCount);
            item.setAverageScore(averageScore);
            if (totalStudents <= 0) {
                item.setCompletionRate(BigDecimal.ZERO);
            } else {
                item.setCompletionRate(BigDecimal.valueOf(submittedCount)
                        .multiply(new BigDecimal("100"))
                        .divide(BigDecimal.valueOf(totalStudents), 2, RoundingMode.HALF_UP));
            }
            completionList.add(item);

            totalSubmitted += submittedCount;
            totalLate += lateCount;
            totalScored += scoredCount;
            if (scoredCount > 0) {
                totalScore = totalScore.add(averageScore.multiply(BigDecimal.valueOf(scoredCount)));
            }
        }
        vo.setHomeworkCompletionList(completionList);

        if (totalScored <= 0) {
            vo.setAverageScore(BigDecimal.ZERO);
        } else {
            vo.setAverageScore(totalScore.divide(BigDecimal.valueOf(totalScored), 2, RoundingMode.HALF_UP));
        }
        if (totalSubmitted <= 0) {
            vo.setLateRate(BigDecimal.ZERO);
        } else {
            vo.setLateRate(BigDecimal.valueOf(totalLate)
                    .multiply(new BigDecimal("100"))
                    .divide(BigDecimal.valueOf(totalSubmitted), 2, RoundingMode.HALF_UP));
        }

        List<Map<String, Object>> timeRows = homeworkSubmissionMapper.selectCourseHomeworkSubmissionTimeDistribution(courseId);
        Map<Integer, Integer> hourCountMap = new HashMap<>();
        if (timeRows != null) {
            for (Map<String, Object> row : timeRows) {
                Integer hour = getIntegerValue(row.get("hour"));
                if (hour == null) {
                    continue;
                }
                hourCountMap.put(hour, safeInt(getIntegerValue(row.get("submissionCount"))));
            }
        }

        List<HomeworkSubmissionAnalysisVo.SubmissionTimeDistributionItem> distributionList = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            HomeworkSubmissionAnalysisVo.SubmissionTimeDistributionItem item =
                    new HomeworkSubmissionAnalysisVo.SubmissionTimeDistributionItem();
            item.setHour(hour);
            item.setTimeRange(String.format("%02d:00-%02d:59", hour, hour));
            item.setSubmissionCount(hourCountMap.getOrDefault(hour, 0));
            distributionList.add(item);
        }
        vo.setSubmissionTimeDistribution(distributionList);

        return vo;
    }

    // ========================== 教师端：作业 CRUD 与列表 ==========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createHomework(HomeworkCreateDto dto) {
        try {
            validateCreateDto(dto);
            verifyCourseAndTeacherMatch(dto.getCourseId(), dto.getTeacherId());

            Homework homework = BeanUtil.copyProperties(dto, Homework.class);
            homework.setStatus(HOMEWORK_STATUS_DRAFT);
            homework.setCreatedTime(LocalDateTime.now());
            homework.setUpdatedTime(LocalDateTime.now());
            homeworkMapper.insert(homework);
            return Result.success("创建作业成功");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("创建作业失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateHomework(HomeworkUpdateDto dto) {
        try {
            if (dto == null || dto.getId() == null || dto.getTeacherId() == null) {
                return Result.error("作业 ID 和教师 ID 不能为空");
            }

            Homework existing = homeworkMapper.selectTeacherHomeworkById(dto.getId(), dto.getTeacherId());
            if (existing == null) {
                return Result.error("未找到作业或无权限操作");
            }
            if (HOMEWORK_STATUS_CLOSED.equals(existing.getStatus())) {
                return Result.error("已截止作业无法编辑");
            }

            Homework toUpdate = new Homework();
            toUpdate.setId(existing.getId());
            toUpdate.setUpdatedTime(LocalDateTime.now());

            if (HOMEWORK_STATUS_PUBLISHED.equals(existing.getStatus())) {
                toUpdate.setHomeworkDescription(defaultString(dto.getHomeworkDescription(), existing.getHomeworkDescription()));
                toUpdate.setAttachmentUrl(defaultString(dto.getAttachmentUrl(), existing.getAttachmentUrl()));
                toUpdate.setDeadline(dto.getDeadline() == null ? existing.getDeadline() : dto.getDeadline());
            } else {
                Long targetCourseId = dto.getCourseId() == null ? existing.getCourseId() : dto.getCourseId();
                verifyCourseAndTeacherMatch(targetCourseId, dto.getTeacherId());

                toUpdate.setCourseId(targetCourseId);
                toUpdate.setTeacherId(dto.getTeacherId());
                toUpdate.setHomeworkTitle(defaultString(dto.getHomeworkTitle(), existing.getHomeworkTitle()));
                toUpdate.setHomeworkDescription(defaultString(dto.getHomeworkDescription(), existing.getHomeworkDescription()));
                toUpdate.setHomeworkType(defaultString(dto.getHomeworkType(), existing.getHomeworkType()));
                toUpdate.setAttachmentUrl(defaultString(dto.getAttachmentUrl(), existing.getAttachmentUrl()));
                toUpdate.setTotalScore(dto.getTotalScore() == null ? existing.getTotalScore() : dto.getTotalScore());
                toUpdate.setDeadline(dto.getDeadline() == null ? existing.getDeadline() : dto.getDeadline());
            }

            homeworkMapper.updateById(toUpdate);
            return Result.success("编辑作业成功");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("编辑作业失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result publishHomework(Long homeworkId, Long teacherId) {
        try {
            Homework homework = homeworkMapper.selectTeacherHomeworkById(homeworkId, teacherId);
            if (homework == null) {
                return Result.error("未找到作业或无权限操作");
            }
            if (HOMEWORK_STATUS_CLOSED.equals(homework.getStatus())) {
                return Result.error("已截止作业无法发布");
            }
            if (HOMEWORK_STATUS_PUBLISHED.equals(homework.getStatus())) {
                return Result.error("作业已发布");
            }

            validateHomeworkForPublish(homework);

            Homework toUpdate = new Homework();
            toUpdate.setId(homeworkId);
            toUpdate.setStatus(HOMEWORK_STATUS_PUBLISHED);
            toUpdate.setUpdatedTime(LocalDateTime.now());
            homeworkMapper.updateById(toUpdate);
            try {
                notifyCourseStudentsHomeworkPublished(homework, teacherId);
            } catch (Exception notifyException) {
                log.warn("作业发布后通知学生失败，homeworkId={}， courseId={}， error={}",
                        homeworkId, homework.getCourseId(), notifyException.getMessage());
            }
            return Result.success("发布作业成功");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("发布作业失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result closeHomework(Long homeworkId, Long teacherId) {
        try {
            Homework homework = homeworkMapper.selectTeacherHomeworkById(homeworkId, teacherId);
            if (homework == null) {
                return Result.error("未找到作业或无权限操作");
            }
            if (HOMEWORK_STATUS_CLOSED.equals(homework.getStatus())) {
                return Result.success("作业已处于截止状态");
            }
            if (!HOMEWORK_STATUS_PUBLISHED.equals(homework.getStatus())) {
                return Result.error("仅已发布作业可截止");
            }

            Homework toUpdate = new Homework();
            toUpdate.setId(homeworkId);
            toUpdate.setStatus(HOMEWORK_STATUS_CLOSED);
            toUpdate.setUpdatedTime(LocalDateTime.now());
            homeworkMapper.updateById(toUpdate);

            homeworkSubmissionMapper.markLateSubmissionsByHomeworkId(homeworkId);
            return Result.success("截止作业成功");
        } catch (Exception e) {
            return Result.error("截止作业失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteHomework(Long homeworkId, Long teacherId) {
        try {
            Homework homework = homeworkMapper.selectTeacherHomeworkById(homeworkId, teacherId);
            if (homework == null) {
                return Result.error("未找到作业或无权限操作");
            }

            List<Long> submissionIds = homeworkSubmissionMapper.selectList(
                            new LambdaQueryWrapper<HomeworkSubmission>()
                                    .select(HomeworkSubmission::getId)
                                    .eq(HomeworkSubmission::getHomeworkId, homeworkId)
                    ).stream()
                    .map(HomeworkSubmission::getId)
                    .collect(Collectors.toList());
            if (!submissionIds.isEmpty()) {
                homeworkSubmissionAnnotationMapper.delete(
                        new LambdaQueryWrapper<HomeworkSubmissionAnnotation>()
                                .in(HomeworkSubmissionAnnotation::getSubmissionId, submissionIds)
                );
            }

            homeworkSubmissionMapper.delete(
                    new LambdaQueryWrapper<HomeworkSubmission>()
                            .eq(HomeworkSubmission::getHomeworkId, homeworkId)
            );

            int affected = homeworkMapper.deleteTeacherHomework(homeworkId, teacherId);
            if (affected <= 0) {
                return Result.error("未找到作业或无权限操作");
            }
            return Result.success("删除作业成功");
        } catch (Exception e) {
            return Result.error("删除作业失败：" + e.getMessage());
        }
    }

    @Override
    public IPage<HomeworkVo> getTeacherHomeworkList(HomeworkQuery query) {
        if (query == null) {
            query = new HomeworkQuery();
        }
        if (query.getTeacherId() == null) {
            throw new BizException("teacherId 不能为空");
        }

        IPage<HomeworkVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<HomeworkVo> resultPage = homeworkMapper.selectTeacherHomeworkPage((Page<HomeworkVo>) page, query);
        fillCourseAndTeacherInfo(resultPage.getRecords());
        return resultPage;
    }

    // ========================== 校验与通用工具 ==========================

    private void validateHomeworkForPublish(Homework homework) {
        if (homework.getCourseId() == null) {
            throw new BizException("发布失败：课程 ID 不能为空");
        }
        if (homework.getTeacherId() == null) {
            throw new BizException("发布失败：教师 ID 不能为空");
        }
        if (isBlank(homework.getHomeworkTitle())) {
            throw new BizException("发布失败：作业标题不能为空");
        }
        if (isBlank(homework.getHomeworkType())) {
            throw new BizException("发布失败：作业类型不能为空");
        }
        if (homework.getTotalScore() == null || homework.getTotalScore() <= 0) {
            throw new BizException("发布失败：总分必须大于 0");
        }
        if (homework.getDeadline() == null) {
            throw new BizException("发布失败：截止时间不能为空");
        }
    }

    private void validateCreateDto(HomeworkCreateDto dto) {
        if (dto == null) {
            throw new BizException("创建参数不能为空");
        }
        if (dto.getCourseId() == null) {
            throw new BizException("courseId 不能为空");
        }
        if (dto.getTeacherId() == null) {
            throw new BizException("teacherId 不能为空");
        }
        if (isBlank(dto.getHomeworkTitle())) {
            throw new BizException("作业标题不能为空");
        }
        if (isBlank(dto.getHomeworkType())) {
            throw new BizException("作业类型不能为空");
        }
        if (dto.getTotalScore() == null || dto.getTotalScore() <= 0) {
            throw new BizException("总分必须大于 0");
        }
        if (dto.getDeadline() == null) {
            throw new BizException("截止时间不能为空");
        }
    }

    private void verifyCourseAndTeacherMatch(Long courseId, Long teacherId) {
        try {
            Result courseResult = educationCourseClient.getCourseById(courseId);
            if (courseResult == null || courseResult.getCode() != 200 || courseResult.getData() == null) {
                throw new BizException("课程不存在");
            }

            Map<String, Object> courseMap = toMap(courseResult.getData());
            Long courseTeacherId = getLongValue(courseMap == null ? null : courseMap.get("teacherId"));
            if (courseTeacherId != null && !courseTeacherId.equals(teacherId)) {
                throw new BizException("教师与课程归属不匹配");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("验证课程信息失败：" + e.getMessage());
        }
    }

    private void notifyCourseStudentsHomeworkPublished(Homework homework, Long teacherId) {
        if (homework == null || homework.getCourseId() == null || homework.getId() == null || teacherId == null) {
            return;
        }

        List<Long> studentIds = studentCourseMapper.selectList(new LambdaQueryWrapper<StudentCourse>()
                        .select(StudentCourse::getStudentId)
                        .eq(StudentCourse::getCourseId, homework.getCourseId())
                        .ne(StudentCourse::getLearningStatus, LEARNING_STATUS_DROPPED))
                .stream()
                .map(StudentCourse::getStudentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (studentIds.isEmpty()) {
            return;
        }

        Set<Long> userIds = new LinkedHashSet<>();
        for (Long studentId : studentIds) {
            try {
                Result studentResult = educationUserStudentClient.getStudentById(studentId);
                if (studentResult == null || studentResult.getCode() != 200 || studentResult.getData() == null) {
                    continue;
                }
                Map<String, Object> studentMap = toMap(studentResult.getData());
                Long userId = getLongValue(studentMap == null ? null : studentMap.get("userId"));
                if (userId != null && userId > 0) {
                    userIds.add(userId);
                }
            } catch (Exception e) {
                log.warn("查询学生用户映射失败，studentId={}， error={}", studentId, e.getMessage());
            }
        }
        if (userIds.isEmpty()) {
            return;
        }

        TemplateTriggerEventRequest payload = new TemplateTriggerEventRequest();
        payload.setEventId(buildHomeworkPublishedEventId(homework.getId()));
        payload.setEventCode(EVENT_CODE_HOMEWORK_PUBLISHED);
        payload.setTargetType(TARGET_TYPE_USER);
        payload.setTargetValue(new ArrayList<>(userIds));
        String courseName = resolveCourseName(homework.getCourseId());
        payload.setParams(Map.of(
                "homework_title", isBlank(homework.getHomeworkTitle()) ? "课程作业" : homework.getHomeworkTitle().trim(),
                "course_name", isBlank(courseName) ? "课程" : courseName,
                "deadline", homework.getDeadline() == null ? "" : homework.getDeadline()
        ));
        payload.setMessageType(MESSAGE_TYPE_COURSE);
        payload.setRelatedId(homework.getId());
        payload.setRelatedType(MESSAGE_RELATED_TYPE_HOMEWORK);
        payload.setPriority(MESSAGE_PRIORITY_NORMAL);
        payload.setDeliverMode(DELIVER_MODE_MQ);
        try {
            Result triggerResult = educationMessageInternalClient.triggerEvent(teacherId, ROLE_TEACHER, payload);
            if (triggerResult == null || triggerResult.getCode() == null || triggerResult.getCode() != 200) {
                log.warn("发送作业发布事件失败，homeworkId={}， message={}",
                        homework.getId(), triggerResult == null ? null : triggerResult.getMessage());
            }
        } catch (Exception e) {
            log.warn("发送作业发布事件异常，homeworkId={}， error={}", homework.getId(), e.getMessage());
        }
    }

    private void notifyHomeworkSubmissionGraded(
            HomeworkSubmission submission,
            HomeworkSubmissionVo teacherSubmission,
            Long teacherId
    ) {
        if (submission == null || submission.getId() == null || submission.getStudentId() == null || teacherId == null) {
            return;
        }
        Long studentUserId = resolveStudentUserId(submission.getStudentId());
        if (studentUserId == null || studentUserId <= 0) {
            return;
        }

        String homeworkTitle = teacherSubmission == null ? null : teacherSubmission.getHomeworkTitle();
        if (isBlank(homeworkTitle) && submission.getHomeworkId() != null) {
            Homework homework = homeworkMapper.selectById(submission.getHomeworkId());
            homeworkTitle = homework == null ? null : homework.getHomeworkTitle();
        }
        if (isBlank(homeworkTitle)) {
            homeworkTitle = "课程作业";
        }

        TemplateTriggerEventRequest payload = new TemplateTriggerEventRequest();
        payload.setEventId(buildHomeworkGradedEventId(submission.getId()));
        payload.setEventCode(EVENT_CODE_HOMEWORK_GRADED);
        payload.setTargetType(TARGET_TYPE_USER);
        payload.setTargetValue(List.of(studentUserId));
        payload.setParams(Map.of(
                "homework_title", homeworkTitle,
                "score", submission.getScore() == null ? "" : submission.getScore()
        ));
        payload.setMessageType(MESSAGE_TYPE_COURSE);
        payload.setRelatedId(submission.getHomeworkId());
        payload.setRelatedType(MESSAGE_RELATED_TYPE_HOMEWORK);
        payload.setDeliverMode(DELIVER_MODE_MQ);

        try {
            Result triggerResult = educationMessageInternalClient.triggerEvent(teacherId, ROLE_TEACHER, payload);
            if (triggerResult == null || triggerResult.getCode() == null || triggerResult.getCode() != 200) {
                log.warn("发送作业评分完成通知失败，submissionId={}， studentUserId={}， message={}",
                        submission.getId(), studentUserId, triggerResult == null ? null : triggerResult.getMessage());
            }
        } catch (Exception e) {
            log.warn("发送作业评分完成通知异常，submissionId={}， studentUserId={}， error={}",
                    submission.getId(), studentUserId, e.getMessage());
        }
    }

    private Long resolveStudentUserId(Long studentId) {
        if (studentId == null || studentId <= 0) {
            return null;
        }
        try {
            Result studentResult = educationUserStudentClient.getStudentById(studentId);
            if (studentResult == null || studentResult.getCode() == null || studentResult.getCode() != 200 || studentResult.getData() == null) {
                return null;
            }
            Map<String, Object> studentMap = toMap(studentResult.getData());
            return getLongValue(studentMap == null ? null : studentMap.get("userId"));
        } catch (Exception e) {
            log.warn("查询学生用户映射失败，studentId={}， error={}", studentId, e.getMessage());
            return null;
        }
    }

    private String buildHomeworkGradedEventId(Long submissionId) {
        return EVENT_CODE_HOMEWORK_GRADED + "_" + submissionId + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String buildHomeworkPublishedEventId(Long homeworkId) {
        return EVENT_CODE_HOMEWORK_PUBLISHED + "_" + homeworkId + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    private void fillCourseAndTeacherInfo(List<HomeworkVo> homeworkList) {
        if (homeworkList == null || homeworkList.isEmpty()) {
            return;
        }

        Set<Long> courseIds = new HashSet<>();
        Set<Long> teacherIds = new HashSet<>();
        for (HomeworkVo vo : homeworkList) {
            if (vo.getCourseId() != null) {
                courseIds.add(vo.getCourseId());
            }
            if (vo.getTeacherId() != null) {
                teacherIds.add(vo.getTeacherId());
            }
        }

        Map<Long, String> courseNameMap = new HashMap<>();
        for (Long courseId : courseIds) {
            try {
                Result courseResult = educationCourseClient.getCourseById(courseId);
                if (courseResult != null && courseResult.getCode() == 200 && courseResult.getData() != null) {
                    Map<String, Object> map = toMap(courseResult.getData());
                    String courseName = getStringValue(map == null ? null : map.get("courseName"));
                    if (!isBlank(courseName)) {
                        courseNameMap.put(courseId, courseName);
                    }
                }
            } catch (Exception e) {
                log.warn("获取课程信息失败，courseId={}， error={}", courseId, e.getMessage());
            }
        }

        Map<Long, String> teacherNameMap = new HashMap<>();
        for (Long teacherId : teacherIds) {
            try {
                Result teacherResult = educationUserTeacherClient.getTeachersNameById(teacherId);
                if (teacherResult != null && teacherResult.getCode() == 200 && teacherResult.getData() != null) {
                    String teacherName = String.valueOf(teacherResult.getData());
                    if (!isBlank(teacherName) && !"null".equalsIgnoreCase(teacherName)) {
                        teacherNameMap.put(teacherId, teacherName);
                    }
                }
            } catch (Exception e) {
                log.warn("获取教师信息失败，teacherId={}， error={}", teacherId, e.getMessage());
            }
        }

        for (HomeworkVo vo : homeworkList) {
            if (vo.getCourseId() != null && courseNameMap.containsKey(vo.getCourseId())) {
                vo.setCourseName(courseNameMap.get(vo.getCourseId()));
            }
            if (vo.getTeacherId() != null && teacherNameMap.containsKey(vo.getTeacherId())) {
                vo.setTeacherName(teacherNameMap.get(vo.getTeacherId()));
            }
        }
    }

    private void fillCourseAndStudentInfo(List<HomeworkSubmissionVo> submissionList) {
        if (submissionList == null || submissionList.isEmpty()) {
            return;
        }

        Set<Long> courseIds = new HashSet<>();
        Set<Long> studentIds = new HashSet<>();
        for (HomeworkSubmissionVo vo : submissionList) {
            if (vo.getCourseId() != null) {
                courseIds.add(vo.getCourseId());
            }
            if (vo.getStudentId() != null) {
                studentIds.add(vo.getStudentId());
            }
        }

        Map<Long, String> courseNameMap = new HashMap<>();
        for (Long courseId : courseIds) {
            try {
                Result courseResult = educationCourseClient.getCourseById(courseId);
                if (courseResult != null && courseResult.getCode() == 200 && courseResult.getData() != null) {
                    Map<String, Object> courseMap = toMap(courseResult.getData());
                    String courseName = getStringValue(courseMap == null ? null : courseMap.get("courseName"));
                    if (!isBlank(courseName)) {
                        courseNameMap.put(courseId, courseName);
                    }
                }
            } catch (Exception e) {
                log.warn("获取课程信息失败，courseId={}， error={}", courseId, e.getMessage());
            }
        }

        Map<Long, Map<String, Object>> studentMap = new HashMap<>();
        for (Long studentId : studentIds) {
            try {
                Result studentResult = educationUserStudentClient.getStudentById(studentId);
                if (studentResult != null && studentResult.getCode() == 200 && studentResult.getData() != null) {
                    Map<String, Object> dataMap = toMap(studentResult.getData());
                    if (dataMap != null) {
                        studentMap.put(studentId, dataMap);
                    }
                }
            } catch (Exception e) {
                log.warn("获取学生信息失败，studentId={}， error={}", studentId, e.getMessage());
            }
        }

        for (HomeworkSubmissionVo vo : submissionList) {
            if (vo.getCourseId() != null && courseNameMap.containsKey(vo.getCourseId())) {
                vo.setCourseName(courseNameMap.get(vo.getCourseId()));
            }
            if (vo.getStudentId() != null && studentMap.containsKey(vo.getStudentId())) {
                Map<String, Object> studentInfo = studentMap.get(vo.getStudentId());
                vo.setStudentName(getStringValue(studentInfo.get("realName")));
                vo.setStudentNumber(getStringValue(studentInfo.get("studentNumber")));
            }
        }
    }

    private void fillSubmissionAnnotationInfo(List<HomeworkSubmissionVo> submissionList) {
        if (submissionList == null || submissionList.isEmpty()) {
            return;
        }
        List<Long> submissionIds = submissionList.stream()
                .map(HomeworkSubmissionVo::getId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (submissionIds.isEmpty()) {
            return;
        }

        List<HomeworkSubmissionAnnotation> annotations =
                homeworkSubmissionAnnotationMapper.selectBySubmissionIds(submissionIds);
        Map<Long, HomeworkSubmissionAnnotation> annotationMap = new HashMap<>();
        if (annotations != null) {
            for (HomeworkSubmissionAnnotation annotation : annotations) {
                if (annotation == null || annotation.getSubmissionId() == null) {
                    continue;
                }
                annotationMap.put(annotation.getSubmissionId(), annotation);
            }
        }

        for (HomeworkSubmissionVo vo : submissionList) {
            if (vo.getId() == null) {
                vo.setHasAnnotation(Boolean.FALSE);
                continue;
            }
            HomeworkSubmissionAnnotation annotation = annotationMap.get(vo.getId());
            if (annotation == null) {
                vo.setHasAnnotation(Boolean.FALSE);
                continue;
            }
            vo.setAnnotationMode(annotation.getAnnotationMode());
            vo.setAnnotationContent(annotation.getAnnotationContent());
            vo.setAnnotationDataJson(annotation.getAnnotationDataJson());
            vo.setAnnotationAttachments(parseAttachmentList(annotation.getAnnotationAttachments()));
            vo.setHasAnnotation(Boolean.TRUE);
        }
    }

    private void saveOrUpdateSubmissionAnnotation(Long submissionId, HomeworkSubmissionGradeDto dto) {
        if (submissionId == null || dto == null) {
            return;
        }
        if (!hasAnnotationPayload(dto)) {
            return;
        }

        HomeworkSubmissionAnnotation annotation =
                homeworkSubmissionAnnotationMapper.selectBySubmissionId(submissionId);
        if (annotation == null) {
            annotation = new HomeworkSubmissionAnnotation();
            annotation.setSubmissionId(submissionId);
            annotation.setCreatedTime(LocalDateTime.now());
        }
        annotation.setAnnotationMode(isBlank(dto.getAnnotationMode()) ? null : dto.getAnnotationMode().trim());
        annotation.setAnnotationContent(isBlank(dto.getAnnotationContent()) ? null : dto.getAnnotationContent().trim());
        annotation.setAnnotationDataJson(isBlank(dto.getAnnotationDataJson()) ? null : dto.getAnnotationDataJson().trim());
        annotation.setAnnotationAttachments(toAttachmentJson(dto.getAnnotationAttachments()));
        annotation.setUpdatedTime(LocalDateTime.now());

        if (annotation.getId() == null) {
            homeworkSubmissionAnnotationMapper.insert(annotation);
        } else {
            homeworkSubmissionAnnotationMapper.updateById(annotation);
        }
    }

    private boolean hasAnnotationPayload(HomeworkSubmissionGradeDto dto) {
        if (dto == null) {
            return false;
        }
        return !isBlank(dto.getAnnotationMode())
                || !isBlank(dto.getAnnotationContent())
                || !isBlank(dto.getAnnotationDataJson())
                || (dto.getAnnotationAttachments() != null && !dto.getAnnotationAttachments().isEmpty());
    }

    private String toAttachmentJson(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        List<String> filtered = attachments.stream()
                .filter(item -> !isBlank(item))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (filtered.isEmpty()) {
            return null;
        }
        return JSONUtil.toJsonStr(filtered);
    }

    private List<String> parseAttachmentList(String json) {
        if (isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            List<String> list = JSONUtil.toList(JSONUtil.parseArray(json), String.class);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception ignore) {
            return new ArrayList<>();
        }
    }

    private String resolveCourseName(Long courseId) {
        if (courseId == null) {
            return null;
        }
        try {
            Result courseResult = educationCourseClient.getCourseById(courseId);
            if (courseResult == null || courseResult.getCode() != 200 || courseResult.getData() == null) {
                return null;
            }
            Map<String, Object> courseMap = toMap(courseResult.getData());
            return getStringValue(courseMap == null ? null : courseMap.get("courseName"));
        } catch (Exception e) {
            log.warn("获取课程名称失败，courseId={}， error={}", courseId, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        try {
            return JSONUtil.toBean(JSONUtil.toJsonStr(data), Map.class);
        } catch (Exception e) {
            return null;
        }
    }

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

    private BigDecimal getBigDecimalValue(Object obj) {
        if (obj == null) {
            return BigDecimal.ZERO;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        if (obj instanceof Number) {
            return BigDecimal.valueOf(((Number) obj).doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(obj));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String getStringValue(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    // ========================== 附件下载相关 ==========================

    @Override
    public HomeworkSubmissionAttachmentVo downloadSubmissionAttachment(Long submissionId) {
        if (submissionId == null) {
            throw new BizException("submissionId 不能为空");
        }

        HomeworkSubmissionVo submissionVo = homeworkSubmissionMapper.selectSubmissionAttachment(submissionId);
        if (submissionVo == null) {
            throw new BizException("提交记录不存在");
        }
        if (isBlank(submissionVo.getAttachmentUrl())) {
            throw new BizException("该提交没有附件");
        }

        fillCourseAndStudentInfo(Collections.singletonList(submissionVo));
        return buildAttachmentVo(submissionVo);
    }

    @Override
    public List<HomeworkSubmissionAttachmentVo> batchDownloadAttachments(Long homeworkId) {
        if (homeworkId == null) {
            throw new BizException("homeworkId 不能为空");
        }

        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new BizException("作业不存在");
        }

        List<HomeworkSubmissionVo> attachmentRows = homeworkSubmissionMapper.selectHomeworkAttachments(homeworkId);
        if (attachmentRows == null || attachmentRows.isEmpty()) {
            return new ArrayList<>();
        }

        fillCourseAndStudentInfo(attachmentRows);
        List<HomeworkSubmissionAttachmentVo> list = new ArrayList<>(attachmentRows.size());
        for (HomeworkSubmissionVo row : attachmentRows) {
            list.add(buildAttachmentVo(row));
        }
        return list;
    }

    private HomeworkSubmissionAttachmentVo buildAttachmentVo(HomeworkSubmissionVo submissionVo) {
        HomeworkSubmissionAttachmentVo vo = new HomeworkSubmissionAttachmentVo();
        vo.setSubmissionId(submissionVo.getId());
        vo.setHomeworkId(submissionVo.getHomeworkId());
        vo.setHomeworkTitle(submissionVo.getHomeworkTitle());
        vo.setCourseId(submissionVo.getCourseId());
        vo.setCourseName(submissionVo.getCourseName());
        vo.setStudentId(submissionVo.getStudentId());
        vo.setStudentName(submissionVo.getStudentName());
        vo.setStudentNumber(submissionVo.getStudentNumber());
        vo.setAttachmentUrl(submissionVo.getAttachmentUrl());
        vo.setFileName(extractFileName(submissionVo.getAttachmentUrl()));
        vo.setSubmissionTime(submissionVo.getSubmissionTime());
        return vo;
    }

    private String extractFileName(String attachmentUrl) {
        if (attachmentUrl == null || attachmentUrl.trim().isEmpty()) {
            return null;
        }
        String url = attachmentUrl.trim();
        int queryIndex = url.indexOf("?");
        if (queryIndex > -1) {
            url = url.substring(0, queryIndex);
        }
        int slashIndex = url.lastIndexOf("/");
        String fileName = slashIndex >= 0 ? url.substring(slashIndex + 1) : url;
        if (fileName.isEmpty()) {
            return null;
        }
        try {
            return URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return fileName;
        }
    }

    private String defaultString(String newValue, String oldValue) {
        return newValue == null ? oldValue : newValue;
    }
}

