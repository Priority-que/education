package com.xixi.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.Exam;
import com.xixi.entity.ExamQuestion;
import com.xixi.entity.ExamSubmission;
import com.xixi.entity.StudentCourse;
import com.xixi.exception.BizException;
import com.xixi.mapper.ExamMapper;
import com.xixi.mapper.ExamQuestionMapper;
import com.xixi.mapper.ExamSubmissionMapper;
import com.xixi.mapper.StudentCourseMapper;
import com.xixi.openfeign.course.EducationCourseClient;
import com.xixi.openfeign.message.EducationMessageInternalClient;
import com.xixi.openfeign.message.dto.TemplateTriggerEventRequest;
import com.xixi.openfeign.user.EducationUserStudentClient;
import com.xixi.openfeign.user.EducationUserTeacherClient;
import com.xixi.pojo.dto.ExamCreateDto;
import com.xixi.pojo.dto.ExamQuestionBatchImportDto;
import com.xixi.pojo.dto.ExamQuestionCreateDto;
import com.xixi.pojo.dto.ExamQuestionUpdateDto;
import com.xixi.pojo.dto.ExamSubmissionDto;
import com.xixi.pojo.dto.ExamSubmissionGradeDto;
import com.xixi.pojo.dto.ExamUpdateDto;
import com.xixi.pojo.query.ExamQuery;
import com.xixi.pojo.query.ExamSubmissionQuery;
import com.xixi.pojo.vo.CourseExamAnalysisVo;
import com.xixi.pojo.vo.ExamQuestionVo;
import com.xixi.pojo.vo.ExamResultVo;
import com.xixi.pojo.vo.ExamSubmissionStatisticsVo;
import com.xixi.pojo.vo.ExamSubmissionVo;
import com.xixi.pojo.vo.ExamVo;
import com.xixi.service.ExamService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测验服务实现类（学生端 + 教师端），统一使用 UTF-8 注释与文案。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private static final String EXAM_STATUS_DRAFT = "DRAFT";
    private static final String EXAM_STATUS_PUBLISHED = "PUBLISHED";
    private static final String EXAM_STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String EXAM_STATUS_ENDED = "ENDED";

    private static final String SUBMISSION_STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String SUBMISSION_STATUS_SUBMITTED = "SUBMITTED";
    private static final String SUBMISSION_STATUS_AUTO_GRADED = "AUTO_GRADED";
    private static final String SUBMISSION_STATUS_MANUAL_GRADED = "MANUAL_GRADED";
    private static final String SUBMISSION_STATUS_PENDING_REVIEW = "PENDING_REVIEW";
    private static final String SUBMISSION_STATUS_GRADED = "GRADED";

    private static final String EXAM_TYPE_SINGLE_CHOICE = "SINGLE_CHOICE";
    private static final String EXAM_TYPE_MULTI_CHOICE = "MULTI_CHOICE";
    private static final String EXAM_TYPE_TRUE_FALSE = "TRUE_FALSE";
    private static final String EXAM_TYPE_FILL_BLANK = "FILL_BLANK";
    private static final String EXAM_TYPE_ESSAY = "ESSAY";

    /**
     * 存放在答题 JSON 中的人工主观题得分键。
     */
    private static final String MANUAL_SCORES_KEY = "__manualScores";

    private static final String PARTICIPATION_NOT_PARTICIPATED = "NOT_PARTICIPATED";
    private static final String LEARNING_STATUS_DROPPED = "DROPPED";

    private static final String MESSAGE_TYPE_COURSE = "COURSE";
    private static final String MESSAGE_RELATED_TYPE_EXAM = "EXAM";
    private static final String EVENT_CODE_EXAM_PUBLISHED = "EXAM_PUBLISHED";
    private static final String EVENT_CODE_EXAM_SUBMISSION_GRADED = "EXAM_SUBMISSION_GRADED";
    private static final String TARGET_TYPE_USER = "USER";
    private static final String DELIVER_MODE_MQ = "MQ";

    private static final int ROLE_TEACHER = 3;
    private static final int MESSAGE_PRIORITY_NORMAL = 0;

    private final ExamMapper examMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamSubmissionMapper examSubmissionMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final EducationCourseClient educationCourseClient;
    private final EducationMessageInternalClient educationMessageInternalClient;
    private final EducationUserStudentClient educationUserStudentClient;
    private final EducationUserTeacherClient educationUserTeacherClient;

    // ========================= 教师端：测验基础管理 =========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createExam(ExamCreateDto dto) {
        try {
            validateCreateExamDto(dto);
            verifyTeacherAndCourse(dto.getTeacherId(), dto.getCourseId());

            Exam exam = new Exam();
            exam.setCourseId(dto.getCourseId());
            exam.setTeacherId(dto.getTeacherId());
            exam.setExamTitle(dto.getExamTitle());
            exam.setExamDescription(dto.getExamDescription());
            exam.setExamType(dto.getExamType());
            exam.setTotalScore(dto.getTotalScore());
            exam.setPassScore(dto.getPassScore());
            exam.setTimeLimit(dto.getTimeLimit());
            exam.setStartTime(dto.getStartTime());
            exam.setEndTime(dto.getEndTime());
            exam.setStatus(EXAM_STATUS_DRAFT);
            exam.setCreatedTime(LocalDateTime.now());
            exam.setUpdatedTime(LocalDateTime.now());
            examMapper.insert(exam);
            return Result.success("创建测验成功");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("创建测验失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateExam(ExamUpdateDto dto) {
        try {
            if (dto == null || dto.getId() == null || dto.getTeacherId() == null) {
                return Result.error("测验 ID 和教师 ID 不能为空");
            }

            Exam existing = examMapper.selectTeacherExamById(dto.getId(), dto.getTeacherId());
            if (existing == null) {
                return Result.error("未找到该测验或无权限操作");
            }
            if (EXAM_STATUS_ENDED.equals(existing.getStatus())) {
                return Result.error("已结束测验不允许编辑");
            }

            Exam toUpdate = new Exam();
            toUpdate.setId(existing.getId());
            toUpdate.setUpdatedTime(LocalDateTime.now());

            if (EXAM_STATUS_DRAFT.equals(existing.getStatus())) {
                Long targetCourseId = dto.getCourseId() == null ? existing.getCourseId() : dto.getCourseId();
                verifyTeacherAndCourse(dto.getTeacherId(), targetCourseId);

                toUpdate.setCourseId(targetCourseId);
                toUpdate.setTeacherId(dto.getTeacherId());
                toUpdate.setExamTitle(defaultString(dto.getExamTitle(), existing.getExamTitle()));
                toUpdate.setExamDescription(defaultString(dto.getExamDescription(), existing.getExamDescription()));
                toUpdate.setExamType(defaultString(dto.getExamType(), existing.getExamType()));
                toUpdate.setTotalScore(dto.getTotalScore() == null ? existing.getTotalScore() : dto.getTotalScore());
                toUpdate.setPassScore(dto.getPassScore() == null ? existing.getPassScore() : dto.getPassScore());
                toUpdate.setTimeLimit(dto.getTimeLimit() == null ? existing.getTimeLimit() : dto.getTimeLimit());
                toUpdate.setStartTime(dto.getStartTime() == null ? existing.getStartTime() : dto.getStartTime());
                toUpdate.setEndTime(dto.getEndTime() == null ? existing.getEndTime() : dto.getEndTime());
            } else {
                // 已发布或进行中的测验，只允许修改描述、分数与时间等少量字段
                toUpdate.setExamDescription(defaultString(dto.getExamDescription(), existing.getExamDescription()));
                toUpdate.setPassScore(dto.getPassScore() == null ? existing.getPassScore() : dto.getPassScore());
                toUpdate.setTimeLimit(dto.getTimeLimit() == null ? existing.getTimeLimit() : dto.getTimeLimit());
                toUpdate.setStartTime(dto.getStartTime() == null ? existing.getStartTime() : dto.getStartTime());
                toUpdate.setEndTime(dto.getEndTime() == null ? existing.getEndTime() : dto.getEndTime());
            }

            validateExamTime(toUpdate.getStartTime(), toUpdate.getEndTime());
            examMapper.updateById(toUpdate);
            return Result.success("编辑测验成功");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("编辑测验失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result publishExam(Long examId, Long teacherId) {
        try {
            Exam exam = examMapper.selectTeacherExamById(examId, teacherId);
            if (exam == null) {
                return Result.error("未找到该测验或无权限操作");
            }
            if (EXAM_STATUS_ENDED.equals(exam.getStatus())) {
                return Result.error("已结束测验不允许发布");
            }
            if (EXAM_STATUS_PUBLISHED.equals(exam.getStatus()) || EXAM_STATUS_IN_PROGRESS.equals(exam.getStatus())) {
                return Result.error("测验已发布");
            }

            validateExamForPublish(exam);
            int questionCount = examQuestionMapper.countByExamId(examId);
            if (questionCount <= 0) {
                return Result.error("发布失败：请至少添加一道题目");
            }

            Exam toUpdate = new Exam();
            toUpdate.setId(examId);
            toUpdate.setStatus(EXAM_STATUS_PUBLISHED);
            toUpdate.setUpdatedTime(LocalDateTime.now());
            examMapper.updateById(toUpdate);

            try {
                notifyCourseStudentsExamPublished(exam, teacherId);
            } catch (Exception notifyException) {
                log.warn("测验发布后通知学生失败，examId={}， courseId={}， error={}",
                        examId, exam.getCourseId(), notifyException.getMessage());
            }
            return Result.success("发布测验成功");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("发布测验失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result closeExam(Long examId, Long teacherId) {
        try {
            Exam exam = examMapper.selectTeacherExamById(examId, teacherId);
            if (exam == null) {
                return Result.error("未找到该测验或无权限操作");
            }
            if (EXAM_STATUS_ENDED.equals(exam.getStatus())) {
                return Result.success("测验已处于结束状态");
            }
            if (EXAM_STATUS_DRAFT.equals(exam.getStatus())) {
                return Result.error("草稿测验不允许关闭");
            }

            Exam toUpdate = new Exam();
            toUpdate.setId(examId);
            toUpdate.setStatus(EXAM_STATUS_ENDED);
            toUpdate.setUpdatedTime(LocalDateTime.now());
            examMapper.updateById(toUpdate);
            return Result.success("关闭测验成功");
        } catch (Exception e) {
            return Result.error("关闭测验失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteExam(Long examId, Long teacherId) {
        try {
            Exam exam = examMapper.selectTeacherExamById(examId, teacherId);
            if (exam == null) {
                return Result.error("未找到该测验或无权限操作");
            }
            if (!EXAM_STATUS_DRAFT.equals(exam.getStatus())) {
                return Result.error("仅草稿状态的测验允许删除");
            }

            examQuestionMapper.deleteByExamId(examId);
            examMapper.deleteById(examId);
            return Result.success("删除测验成功");
        } catch (Exception e) {
            return Result.error("删除测验失败：" + e.getMessage());
        }
    }

    @Override
    public IPage<ExamVo> getTeacherExamList(ExamQuery query) {
        if (query == null) {
            query = new ExamQuery();
        }
        if (query.getTeacherId() == null) {
            throw new BizException("teacherId 不能为空");
        }

        IPage<ExamVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<ExamVo> resultPage = examMapper.selectTeacherExamPage((Page<ExamVo>) page, query);
        fillCourseAndTeacherInfo(resultPage.getRecords());
        return resultPage;
    }

    // ========================= 教师端：题目管理 =========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addExamQuestion(ExamQuestionCreateDto dto) {
        try {
            validateExamQuestionCreateDto(dto);

            Exam exam = examMapper.selectTeacherExamById(dto.getExamId(), dto.getTeacherId());
            if (exam == null) {
                return Result.error("未找到该测验或无权限操作");
            }
            if (!EXAM_STATUS_DRAFT.equals(exam.getStatus())) {
                return Result.error("仅草稿状态测验允许添加题目");
            }

            ExamQuestion question = new ExamQuestion();
            question.setExamId(dto.getExamId());
            question.setQuestionType(dto.getQuestionType());
            question.setQuestionContent(dto.getQuestionContent());
            question.setOptions(dto.getOptions());
            question.setCorrectAnswer(dto.getCorrectAnswer());
            question.setScore(dto.getScore());
            question.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
            question.setCreatedTime(LocalDateTime.now());
            examQuestionMapper.insert(question);
            return Result.success("添加题目成功");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("添加题目失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateExamQuestion(ExamQuestionUpdateDto dto) {
        try {
            if (dto == null || dto.getId() == null || dto.getTeacherId() == null) {
                return Result.error("题目 ID 和教师 ID 不能为空");
            }

            ExamQuestion existingQuestion = examQuestionMapper.selectTeacherQuestionById(dto.getId(), dto.getTeacherId());
            if (existingQuestion == null) {
                return Result.error("未找到该题目或无权限操作");
            }

            Exam exam = examMapper.selectTeacherExamById(existingQuestion.getExamId(), dto.getTeacherId());
            if (exam == null || !EXAM_STATUS_DRAFT.equals(exam.getStatus())) {
                return Result.error("仅草稿状态测验允许编辑题目");
            }

            ExamQuestion toUpdate = new ExamQuestion();
            toUpdate.setId(existingQuestion.getId());
            toUpdate.setQuestionType(defaultString(dto.getQuestionType(), existingQuestion.getQuestionType()));
            toUpdate.setQuestionContent(defaultString(dto.getQuestionContent(), existingQuestion.getQuestionContent()));
            toUpdate.setOptions(defaultString(dto.getOptions(), existingQuestion.getOptions()));
            toUpdate.setCorrectAnswer(defaultString(dto.getCorrectAnswer(), existingQuestion.getCorrectAnswer()));
            toUpdate.setScore(dto.getScore() == null ? existingQuestion.getScore() : dto.getScore());
            toUpdate.setSortOrder(dto.getSortOrder() == null ? existingQuestion.getSortOrder() : dto.getSortOrder());
            examQuestionMapper.updateById(toUpdate);
            return Result.success("编辑题目成功");
        } catch (Exception e) {
            return Result.error("编辑题目失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteExamQuestion(Long questionId, Long teacherId) {
        try {
            if (questionId == null || teacherId == null) {
                return Result.error("questionId 和 teacherId 不能为空");
            }
            ExamQuestion question = examQuestionMapper.selectTeacherQuestionById(questionId, teacherId);
            if (question == null) {
                return Result.error("未找到该题目或无权限操作");
            }
            Exam exam = examMapper.selectTeacherExamById(question.getExamId(), teacherId);
            if (exam == null || !EXAM_STATUS_DRAFT.equals(exam.getStatus())) {
                return Result.error("仅草稿状态测验允许删除题目");
            }
            examQuestionMapper.deleteById(questionId);
            return Result.success("删除题目成功");
        } catch (Exception e) {
            return Result.error("删除题目失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result batchImportExamQuestion(ExamQuestionBatchImportDto dto) {
        try {
            if (dto == null || dto.getExamId() == null || dto.getTeacherId() == null) {
                return Result.error("examId 和 teacherId 不能为空");
            }
            if (dto.getQuestionList() == null || dto.getQuestionList().isEmpty()) {
                return Result.error("导入题目列表不能为空");
            }

            Exam exam = examMapper.selectTeacherExamById(dto.getExamId(), dto.getTeacherId());
            if (exam == null) {
                return Result.error("未找到该测验或无权限操作");
            }
            if (!EXAM_STATUS_DRAFT.equals(exam.getStatus())) {
                return Result.error("仅草稿状态测验允许导入题目");
            }

            int count = 0;
            for (ExamQuestionCreateDto item : dto.getQuestionList()) {
                if (item == null) {
                    continue;
                }
                item.setExamId(dto.getExamId());
                item.setTeacherId(dto.getTeacherId());
                validateExamQuestionCreateDto(item);

                ExamQuestion question = new ExamQuestion();
                question.setExamId(dto.getExamId());
                question.setQuestionType(item.getQuestionType());
                question.setQuestionContent(item.getQuestionContent());
                question.setOptions(item.getOptions());
                question.setCorrectAnswer(item.getCorrectAnswer());
                question.setScore(item.getScore());
                question.setSortOrder(item.getSortOrder() == null ? 0 : item.getSortOrder());
                question.setCreatedTime(LocalDateTime.now());
                examQuestionMapper.insert(question);
                count++;
            }
            return Result.success("批量导入题目成功，共导入 " + count + " 条");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("批量导入题目失败：" + e.getMessage());
        }
    }

    @Override
    public List<ExamQuestionVo> getExamQuestionList(Long examId, Long userId) {
        if (examId == null || userId == null) {
            throw new BizException("examId 和 userId 不能为空");
        }

        // 先判断是否为任课教师，教师可查看包含正确答案的数据
        Exam teacherExam = examMapper.selectTeacherExamById(examId, userId);
        if (teacherExam != null) {
            return mapQuestionVo(examQuestionMapper.selectQuestionsWithAnswerByExamId(examId), true);
        }

        // 非教师按学生身份校验，并返回不含正确答案的数据
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BizException("测验不存在");
        }
        if (!EXAM_STATUS_PUBLISHED.equals(exam.getStatus())
                && !EXAM_STATUS_IN_PROGRESS.equals(exam.getStatus())
                && !EXAM_STATUS_ENDED.equals(exam.getStatus())) {
            throw new BizException("测验未发布或已结束");
        }

        Long studentId = resolveStudentIdByUserId(userId);
        StudentCourse studentCourse = studentCourseMapper.selectByStudentIdAndCourseId(studentId, exam.getCourseId());
        if (studentCourse == null) {
            throw new BizException("您未加入该课程，无法查看测验题目");
        }

        return mapQuestionVo(examQuestionMapper.selectQuestionsByExamId(examId), false);
    }

    private List<ExamQuestionVo> mapQuestionVo(List<ExamQuestion> questionList, boolean includeCorrectAnswer) {
        List<ExamQuestionVo> voList = new ArrayList<>();
        if (questionList == null || questionList.isEmpty()) {
            return voList;
        }
        for (ExamQuestion question : questionList) {
            ExamQuestionVo vo = new ExamQuestionVo();
            vo.setId(question.getId());
            vo.setExamId(question.getExamId());
            vo.setQuestionType(question.getQuestionType());
            vo.setQuestionContent(question.getQuestionContent());
            vo.setOptions(question.getOptions());
            if (includeCorrectAnswer) {
                vo.setCorrectAnswer(question.getCorrectAnswer());
            }
            vo.setScore(question.getScore());
            vo.setSortOrder(question.getSortOrder());
            voList.add(vo);
        }
        return voList;
    }

    // ========================= 教师端：提交记录与批改 =========================

    @Override
    public IPage<ExamSubmissionVo> getTeacherExamSubmissions(ExamSubmissionQuery query) {
        if (query == null) {
            query = new ExamSubmissionQuery();
        }
        if (query.getTeacherId() == null) {
            throw new BizException("teacherId 不能为空");
        }

        IPage<ExamSubmissionVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<ExamSubmissionVo> resultPage = examSubmissionMapper.selectTeacherExamSubmissionPage(
                (Page<ExamSubmissionVo>) page, query);
        fillCourseInfoForSubmissions(resultPage.getRecords());
        fillStudentInfoForSubmissions(resultPage.getRecords());
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result gradeExamSubmission(ExamSubmissionGradeDto dto) {
        try {
            if (dto == null || dto.getSubmissionId() == null || dto.getTeacherId() == null) {
                return Result.error("submissionId 和 teacherId 不能为空");
            }

            ExamSubmissionVo submissionVo = examSubmissionMapper.selectTeacherExamSubmissionDetail(
                    dto.getSubmissionId(), dto.getTeacherId());
            if (submissionVo == null) {
                return Result.error("提交记录不存在或无权限操作");
            }

            ExamSubmission submission = examSubmissionMapper.selectById(dto.getSubmissionId());
            if (submission == null) {
                return Result.error("提交记录不存在");
            }
            if (!isPendingReviewStatus(submission.getStatus())
                    && !isGradedStatus(submission.getStatus())) {
                return Result.error("仅待批改或已人工批改记录可执行主观题批改");
            }

            List<ExamQuestion> questionList = examQuestionMapper.selectQuestionsWithAnswerByExamId(submission.getExamId());
            List<ExamQuestion> essayQuestionList = questionList.stream()
                    .filter(q -> EXAM_TYPE_ESSAY.equals(q.getQuestionType()))
                    .collect(Collectors.toList());
            if (essayQuestionList.isEmpty()) {
                return Result.error("该测验无主观题，无需人工批改");
            }

            Map<Long, BigDecimal> essayScoreMap =
                    dto.getEssayScoreMap() == null ? new HashMap<>() : dto.getEssayScoreMap();
            BigDecimal objectiveScore = calcObjectiveScore(submission.getAnswers(), questionList);
            BigDecimal essayScore = BigDecimal.ZERO;
            JSONObject answersJson = parseAnswerJson(submission.getAnswers());
            JSONObject manualScoresJson = JSONUtil.createObj();

            for (ExamQuestion question : essayQuestionList) {
                BigDecimal questionScore = essayScoreMap.get(question.getId());
                if (questionScore == null) {
                    return Result.error("题目 " + question.getId() + " 缺少得分");
                }
                BigDecimal maxScore = BigDecimal.valueOf(question.getScore() == null ? 0 : question.getScore());
                if (questionScore.compareTo(BigDecimal.ZERO) < 0 || questionScore.compareTo(maxScore) > 0) {
                    return Result.error("题目 " + question.getId() + " 得分必须在 0-" + maxScore + " 之间");
                }

                BigDecimal normalized = questionScore.setScale(2, RoundingMode.HALF_UP);
                essayScore = essayScore.add(normalized);
                manualScoresJson.set(String.valueOf(question.getId()), normalized);
            }

            answersJson.set(MANUAL_SCORES_KEY, manualScoresJson);
            submission.setAnswers(answersJson.toString());
            submission.setTotalScore(objectiveScore.add(essayScore).setScale(2, RoundingMode.HALF_UP));
            submission.setStatus(SUBMISSION_STATUS_GRADED);
            submission.setUpdatedTime(LocalDateTime.now());
            examSubmissionMapper.updateById(submission);

            try {
                notifyExamSubmissionGraded(submission, submissionVo, dto.getTeacherId());
            } catch (Exception notifyException) {
                log.warn("测验批改事件上报失败，submissionId={}， examId={}， error={}",
                        submission.getId(), submission.getExamId(), notifyException.getMessage());
            }
            return Result.success("批改成功，得分：" + submission.getTotalScore());
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("批改失败：" + e.getMessage());
        }
    }

    @Override
    public ExamSubmissionStatisticsVo getExamSubmissionStatistics(Long examId, Long teacherId) {
        if (examId == null || teacherId == null) {
            throw new BizException("examId 和 teacherId 不能为空");
        }

        Exam exam = examMapper.selectTeacherExamById(examId, teacherId);
        if (exam == null) {
            throw new BizException("未找到该测验或无权限操作");
        }

        List<ExamSubmission> submissionList = examSubmissionMapper.selectCompletedByExamId(examId);
        int total = submissionList.size();
        BigDecimal averageScore = BigDecimal.ZERO;
        BigDecimal passRate = BigDecimal.ZERO;
        Map<String, Integer> scoreRange = buildDefaultScoreRangeDistribution();

        if (total > 0) {
            BigDecimal sum = BigDecimal.ZERO;
            int passCount = 0;
            BigDecimal passScore = BigDecimal.valueOf(exam.getPassScore() == null ? 60 : exam.getPassScore());
            for (ExamSubmission submission : submissionList) {
                BigDecimal score = submission.getTotalScore() == null ? BigDecimal.ZERO : submission.getTotalScore();
                sum = sum.add(score);
                if (score.compareTo(passScore) >= 0) {
                    passCount++;
                }
                accumulateScoreRange(scoreRange, score);
            }
            averageScore = sum.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            passRate = BigDecimal.valueOf(passCount)
                    .multiply(HUNDRED)
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        }

        List<ExamQuestion> questionList = examQuestionMapper.selectQuestionsWithAnswerByExamId(examId);
        List<ExamSubmissionStatisticsVo.QuestionAccuracyVo> questionAccuracyList =
                buildQuestionAccuracy(questionList, submissionList);

        ExamSubmissionStatisticsVo vo = new ExamSubmissionStatisticsVo();
        vo.setExamId(examId);
        vo.setExamTitle(exam.getExamTitle());
        vo.setTotalParticipants(total);
        vo.setAverageScore(averageScore);
        vo.setPassRate(passRate);
        vo.setScoreRangeDistribution(scoreRange);
        vo.setQuestionAccuracyList(questionAccuracyList);
        return vo;
    }

    @Override
    public CourseExamAnalysisVo getCourseExamAnalysis(Long courseId) {
        if (courseId == null) {
            throw new BizException("courseId 不能为空");
        }

        CourseExamAnalysisVo vo = new CourseExamAnalysisVo();
        vo.setCourseId(courseId);

        try {
            Result courseResult = educationCourseClient.getCourseById(courseId);
            if (courseResult != null && courseResult.getCode() == 200 && courseResult.getData() != null) {
                vo.setCourseName(extractCourseName(courseResult.getData()));
            }
        } catch (Exception e) {
            log.warn("获取课程信息失败，courseId={}， error={}", courseId, e.getMessage());
        }

        List<Map<String, Object>> examAverageRows = examSubmissionMapper.selectCourseExamAverageScores(courseId);
        if (examAverageRows == null) {
            examAverageRows = new ArrayList<>();
        }
        List<CourseExamAnalysisVo.ExamAverageScoreItem> averageScoreList = new ArrayList<>();
        for (Map<String, Object> row : examAverageRows) {
            CourseExamAnalysisVo.ExamAverageScoreItem item = new CourseExamAnalysisVo.ExamAverageScoreItem();
            item.setExamId(getLongValue(row.get("examId")));
            item.setExamTitle(safeString(row.get("examTitle")));
            BigDecimal averageScore = getBigDecimalValue(row.get("averageScore"));
            item.setAverageScore(averageScore == null ? BigDecimal.ZERO : averageScore.setScale(2, RoundingMode.HALF_UP));
            averageScoreList.add(item);
        }
        vo.setExamAverageScoreList(averageScoreList);

        Map<String, Integer> scoreRangeDistribution = buildDefaultScoreRangeDistribution();
        List<Map<String, Object>> scoreRangeRows = examSubmissionMapper.selectCourseExamScoreRangeDistribution(courseId);
        if (scoreRangeRows == null) {
            scoreRangeRows = new ArrayList<>();
        }
        for (Map<String, Object> row : scoreRangeRows) {
            String scoreRange = safeString(row.get("scoreRange"));
            if (!scoreRangeDistribution.containsKey(scoreRange)) {
                continue;
            }
            Long count = getLongValue(row.get("count"));
            scoreRangeDistribution.put(scoreRange, count == null ? 0 : count.intValue());
        }
        vo.setScoreRangeDistribution(scoreRangeDistribution);

        List<Map<String, Object>> questionRows = examSubmissionMapper.selectCourseObjectiveQuestionList(courseId);
        if (questionRows == null) {
            questionRows = new ArrayList<>();
        }
        List<ExamSubmission> submissionList = examSubmissionMapper.selectCompletedByCourseId(courseId);
        if (submissionList == null) {
            submissionList = new ArrayList<>();
        }
        Map<Long, List<ExamSubmission>> examSubmissionMap = submissionList.stream()
                .filter(item -> item.getExamId() != null)
                .collect(Collectors.groupingBy(ExamSubmission::getExamId));

        List<CourseExamAnalysisVo.QuestionAccuracyItem> questionAccuracyList = new ArrayList<>();
        for (Map<String, Object> row : questionRows) {
            Long examId = getLongValue(row.get("examId"));
            Long questionId = getLongValue(row.get("questionId"));
            String questionType = safeString(row.get("questionType"));
            String correctAnswer = safeString(row.get("correctAnswer"));

            CourseExamAnalysisVo.QuestionAccuracyItem item = new CourseExamAnalysisVo.QuestionAccuracyItem();
            item.setExamId(examId);
            item.setExamTitle(safeString(row.get("examTitle")));
            item.setQuestionId(questionId);
            item.setQuestionType(questionType);
            item.setQuestionContent(safeString(row.get("questionContent")));

            List<ExamSubmission> currentExamSubmissions = examSubmissionMap.getOrDefault(examId, new ArrayList<>());
            int total = currentExamSubmissions.size();
            if (total <= 0 || questionId == null) {
                item.setCorrectRate(BigDecimal.ZERO);
                questionAccuracyList.add(item);
                continue;
            }

            int correctCount = 0;
            for (ExamSubmission submission : currentExamSubmissions) {
                JSONObject answerJson = parseAnswerJson(submission.getAnswers());
                String studentAnswer = getStudentAnswer(answerJson, questionId);
                if (isAnswerCorrect(questionType, correctAnswer, studentAnswer)) {
                    correctCount++;
                }
            }

            BigDecimal correctRate = BigDecimal.valueOf(correctCount)
                    .multiply(HUNDRED)
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            item.setCorrectRate(correctRate);
            questionAccuracyList.add(item);
        }
        vo.setQuestionAccuracyList(questionAccuracyList);
        return vo;
    }

    // ========================= 学生端：测验列表与详情 =========================

    @Override
    public IPage<ExamVo> getCourseExamList(ExamQuery query) {
        if (query == null) {
            query = new ExamQuery();
        }

        IPage<ExamVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<ExamVo> resultPage = examMapper.selectExamPage((Page<ExamVo>) page, query);

        if (resultPage.getRecords() == null || resultPage.getRecords().isEmpty()) {
            return resultPage;
        }

        if (query.getStudentId() != null) {
            for (ExamVo vo : resultPage.getRecords()) {
                ExamSubmission submission = examSubmissionMapper.selectByExamAndStudent(
                        vo.getId(), query.getStudentId());
                if (submission != null) {
                    vo.setParticipationStatus(submission.getStatus());
                } else {
                    vo.setParticipationStatus(PARTICIPATION_NOT_PARTICIPATED);
                }
            }
        }

        fillCourseAndTeacherInfo(resultPage.getRecords());
        return resultPage;
    }

    @Override
    public ExamVo getExamDetail(Long examId) {
        ExamVo vo = examMapper.selectExamDetail(examId);
        if (vo == null) {
            throw new BizException("测验不存在");
        }
        fillCourseAndTeacherInfo(List.of(vo));
        return vo;
    }

    // ========================= 学生端：开始测验 / 保存答案 / 提交测验 =========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startExam(Long examId, Long studentId) {
        try {
            if (examId == null || studentId == null) {
                return Result.error("examId 和 studentId 不能为空");
            }

            if (!checkStudentExists(studentId)) {
                return Result.error("学生不存在");
            }

            Exam exam = examMapper.selectById(examId);
            if (exam == null) {
                return Result.error("测验不存在");
            }
            if (!EXAM_STATUS_PUBLISHED.equals(exam.getStatus())
                    && !EXAM_STATUS_IN_PROGRESS.equals(exam.getStatus())) {
                return Result.error("测验未发布或已结束");
            }

            LocalDateTime now = LocalDateTime.now();
            if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
                return Result.error("测验尚未开始");
            }
            if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
                return Result.error("测验已结束");
            }

            StudentCourse studentCourse = studentCourseMapper.selectByStudentIdAndCourseId(studentId, exam.getCourseId());
            if (studentCourse == null) {
                return Result.error("您未加入该课程，无法参加测验");
            }

            List<ExamQuestion> questions = examQuestionMapper.selectQuestionsByExamId(examId);
            if (questions.isEmpty()) {
                return Result.error("测验暂时无题目");
            }

            ExamSubmission existingSubmission = examSubmissionMapper.selectByExamAndStudent(examId, studentId);
            if (existingSubmission != null && !SUBMISSION_STATUS_IN_PROGRESS.equals(existingSubmission.getStatus())) {
                return Result.error("您已经完成过本次测验");
            }

            if (existingSubmission != null && isTimeLimitExceeded(existingSubmission.getStartTime(), exam.getTimeLimit(), now)) {
                return Result.error("作答时间已结束，请直接提交测验");
            }

            ExamSubmission submission;
            if (existingSubmission != null) {
                submission = existingSubmission;
            } else {
                submission = new ExamSubmission();
                submission.setExamId(examId);
                submission.setStudentId(studentId);
                submission.setStartTime(now);
                submission.setStatus(SUBMISSION_STATUS_IN_PROGRESS);
                submission.setAnswers("{}");
                examSubmissionMapper.insert(submission);
            }

            List<ExamQuestionVo> questionVoList = new ArrayList<>();
            for (ExamQuestion question : questions) {
                ExamQuestionVo vo = new ExamQuestionVo();
                vo.setId(question.getId());
                vo.setExamId(question.getExamId());
                vo.setQuestionType(question.getQuestionType());
                vo.setQuestionContent(question.getQuestionContent());
                vo.setOptions(question.getOptions());
                vo.setScore(question.getScore());
                vo.setSortOrder(question.getSortOrder());
                questionVoList.add(vo);
            }

            Integer remainingMinutes = calculateRemainingMinutes(submission.getStartTime(), exam.getTimeLimit(), now);

            Map<String, Object> result = new HashMap<>();
            result.put("submissionId", submission.getId());
            result.put("questions", questionVoList);
            result.put("timeLimit", exam.getTimeLimit());
            result.put("startTime", submission.getStartTime());
            result.put("remainingMinutes", remainingMinutes);

            return Result.success(result);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("开始测验失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result submitAnswer(ExamSubmissionDto dto) {
        try {
            if (dto == null || dto.getExamId() == null || dto.getStudentId() == null) {
                return Result.error("examId 和 studentId 不能为空");
            }
            if (dto.getAnswers() == null || dto.getAnswers().isBlank()) {
                return Result.error("答案不能为空");
            }

            JSONObject incomingAnswerJson = parseAnswerJson(dto.getAnswers());
            if (incomingAnswerJson.isEmpty()) {
                return Result.error("答案内容不能为空");
            }

            ExamSubmission submission = examSubmissionMapper.selectByExamAndStudent(
                    dto.getExamId(), dto.getStudentId());
            if (submission == null || !SUBMISSION_STATUS_IN_PROGRESS.equals(submission.getStatus())) {
                return Result.error("未找到进行中的测验提交记录");
            }

            Exam exam = examMapper.selectById(dto.getExamId());
            if (exam == null) {
                return Result.error("测验不存在");
            }
            LocalDateTime now = LocalDateTime.now();
            if (isTimeLimitExceeded(submission.getStartTime(), exam.getTimeLimit(), now)) {
                return Result.error("测验时间已到，请提交");
            }

            JSONObject mergedAnswerJson = parseAnswerJson(submission.getAnswers());
            for (String key : incomingAnswerJson.keySet()) {
                mergedAnswerJson.set(key, incomingAnswerJson.get(key));
            }
            submission.setAnswers(mergedAnswerJson.toString());
            submission.setUpdatedTime(LocalDateTime.now());
            examSubmissionMapper.updateById(submission);

            return Result.success("答案保存成功");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("保存答案失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result submitExam(Long examId, Long studentId) {
        try {
            if (examId == null || studentId == null) {
                return Result.error("examId 和 studentId 不能为空");
            }

            ExamSubmission submission = examSubmissionMapper.selectByExamAndStudent(examId, studentId);
            if (submission == null || !SUBMISSION_STATUS_IN_PROGRESS.equals(submission.getStatus())) {
                return Result.error("未找到进行中的测验提交记录");
            }

            Exam exam = examMapper.selectById(examId);
            if (exam == null) {
                return Result.error("测验不存在");
            }

            LocalDateTime now = LocalDateTime.now();
            if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
                return Result.error("测验已结束");
            }

            boolean isTimeoutSubmit = isTimeLimitExceeded(submission.getStartTime(), exam.getTimeLimit(), now);

            AutoGradeResult autoGradeResult = autoGrade(submission.getAnswers(), examId);
            BigDecimal totalScore = autoGradeResult.getScore();
            submission.setTotalScore(totalScore);
            submission.setSubmitTime(now);
            submission.setStatus(autoGradeResult.isContainEssayQuestion()
                    ? SUBMISSION_STATUS_PENDING_REVIEW
                    : SUBMISSION_STATUS_GRADED);
            submission.setUpdatedTime(now);
            examSubmissionMapper.updateById(submission);

            if (autoGradeResult.isContainEssayQuestion()) {
                return Result.success("测验已提交，客观题得分：" + totalScore + "，主观题等待教师批改");
            }
            try {
                notifyExamSubmissionGraded(submission, null, exam.getTeacherId());
            } catch (Exception notifyException) {
                log.warn("测验自动批改事件上报失败，submissionId={}， examId={}， error={}",
                        submission.getId(), examId, notifyException.getMessage());
            }
            if (isTimeoutSubmit) {
                return Result.success("测验已超时，系统按当前答案自动提交，得分：" + totalScore);
            }
            return Result.success("测验提交成功，得分：" + totalScore);
        } catch (Exception e) {
            return Result.error("提交测验失败：" + e.getMessage());
        }
    }

    private AutoGradeResult autoGrade(String answersJson, Long examId) {
        BigDecimal totalScore = BigDecimal.ZERO;

        List<ExamQuestion> questions = examQuestionMapper.selectQuestionsWithAnswerByExamId(examId);
        JSONObject studentAnswers = parseAnswerJson(answersJson);
        boolean containEssayQuestion = false;

        for (ExamQuestion question : questions) {
            String questionType = question.getQuestionType();
            if (EXAM_TYPE_ESSAY.equals(questionType)) {
                containEssayQuestion = true;
                continue;
            }
            if (isObjectiveQuestion(questionType)) {
                String studentAnswer = getStudentAnswer(studentAnswers, question.getId());
                if (isAnswerCorrect(questionType, question.getCorrectAnswer(), studentAnswer)) {
                    totalScore = totalScore.add(BigDecimal.valueOf(
                            question.getScore() == null ? 0 : question.getScore()));
                }
            }
        }

        return new AutoGradeResult(totalScore, containEssayQuestion);
    }

    @Override
    public ExamResultVo getExamResult(Long submissionId) {
        ExamSubmissionVo submissionVo = examSubmissionMapper.selectSubmissionDetail(submissionId);
        if (submissionVo == null) {
            throw new BizException("提交记录不存在");
        }

        ExamResultVo resultVo = new ExamResultVo();
        resultVo.setSubmissionId(submissionId);
        resultVo.setExamTitle(submissionVo.getExamTitle());
        resultVo.setExamId(submissionVo.getExamId());
        resultVo.setTotalScore(submissionVo.getTotalScore());
        resultVo.setSubmitTime(submissionVo.getSubmitTime());

        ExamSubmission submission = examSubmissionMapper.selectById(submissionId);
        if (submission != null && submission.getStartTime() != null && submission.getSubmitTime() != null) {
            long minutes = ChronoUnit.MINUTES.between(submission.getStartTime(), submission.getSubmitTime());
            resultVo.setAnswerTime((int) minutes);
        }

        List<ExamQuestion> questions = examQuestionMapper.selectQuestionsWithAnswerByExamId(submissionVo.getExamId());
        JSONObject studentAnswers = parseAnswerJson(submissionVo.getAnswers());
        Map<Long, BigDecimal> manualScoreMap = parseManualScoreMap(studentAnswers);
        boolean pendingReview = isPendingReviewStatus(submissionVo.getStatus());

        List<ExamResultVo.QuestionResult> questionResults = new ArrayList<>();
        for (ExamQuestion question : questions) {
            ExamResultVo.QuestionResult questionResult = new ExamResultVo.QuestionResult();
            questionResult.setQuestionId(question.getId());
            questionResult.setQuestionContent(question.getQuestionContent());
            questionResult.setQuestionType(question.getQuestionType());
            questionResult.setCorrectAnswer(question.getCorrectAnswer());
            Integer maxScore = question.getScore() == null ? 0 : question.getScore();
            questionResult.setFullScore(maxScore);
            questionResult.setMaxScore(maxScore);

            String studentAnswer = getStudentAnswer(studentAnswers, question.getId());
            questionResult.setStudentAnswer(studentAnswer);

            BigDecimal questionScore = BigDecimal.ZERO;
            Boolean isCorrect = null;
            String reviewStatus = "PENDING_REVIEW";
            if (isObjectiveQuestion(question.getQuestionType())) {
                boolean correct = isAnswerCorrect(question.getQuestionType(), question.getCorrectAnswer(), studentAnswer);
                isCorrect = correct;
                reviewStatus = "AUTO_GRADED";
                if (correct) {
                    questionScore = BigDecimal.valueOf(maxScore);
                }
            } else if (EXAM_TYPE_ESSAY.equals(question.getQuestionType())) {
                BigDecimal manualScore = manualScoreMap.get(question.getId());
                if (manualScore != null) {
                    questionScore = manualScore;
                    reviewStatus = "REVIEWED";
                } else if (!pendingReview) {
                    reviewStatus = "REVIEWED";
                }
            }
            questionResult.setScore(questionScore.setScale(2, RoundingMode.HALF_UP));
            questionResult.setIsCorrect(isCorrect);
            questionResult.setReviewStatus(reviewStatus);

            questionResults.add(questionResult);
        }

        resultVo.setQuestionResults(questionResults);
        resultVo.setQuestionResultList(questionResults);
        return resultVo;
    }

    @Override
    public IPage<ExamSubmissionVo> getMySubmissions(ExamSubmissionQuery query) {
        if (query == null) {
            query = new ExamSubmissionQuery();
        }
        if (query.getStudentId() == null) {
            throw new BizException("studentId 不能为空");
        }

        IPage<ExamSubmissionVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<ExamSubmissionVo> resultPage = examSubmissionMapper.selectExamSubmissionPage(
                (Page<ExamSubmissionVo>) page, query);

        fillCourseInfoForSubmissions(resultPage.getRecords());
        return resultPage;
    }

    // ========================= VO 填充与通用工具 =========================

    private void fillCourseAndTeacherInfo(List<ExamVo> examVoList) {
        if (examVoList == null || examVoList.isEmpty()) {
            return;
        }

        Set<Long> courseIds = examVoList.stream()
                .map(ExamVo::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> teacherIds = examVoList.stream()
                .map(ExamVo::getTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> courseNameMap = new HashMap<>();
        for (Long courseId : courseIds) {
            try {
                Result courseResult = educationCourseClient.getCourseById(courseId);
                if (courseResult != null && courseResult.getCode() == 200 && courseResult.getData() != null) {
                    String courseName = extractCourseName(courseResult.getData());
                    if (courseName != null && !courseName.isBlank()) {
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
                    if (teacherName != null && !teacherName.isBlank() && !"null".equalsIgnoreCase(teacherName)) {
                        teacherNameMap.put(teacherId, teacherName);
                    }
                }
            } catch (Exception e) {
                log.warn("获取教师姓名失败，teacherId={}， error={}", teacherId, e.getMessage());
            }
        }

        for (ExamVo vo : examVoList) {
            if (vo.getCourseId() != null) {
                String courseName = courseNameMap.get(vo.getCourseId());
                if (courseName != null) {
                    vo.setCourseName(courseName);
                }
            }
            if (vo.getTeacherId() != null) {
                String teacherName = teacherNameMap.get(vo.getTeacherId());
                if (teacherName != null) {
                    vo.setTeacherName(teacherName);
                }
            }
        }
    }

    private void fillCourseInfoForSubmissions(List<ExamSubmissionVo> submissionVoList) {
        if (submissionVoList == null || submissionVoList.isEmpty()) {
            return;
        }

        Set<Long> courseIds = submissionVoList.stream()
                .map(ExamSubmissionVo::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> courseNameMap = new HashMap<>();
        for (Long courseId : courseIds) {
            try {
                Result courseResult = educationCourseClient.getCourseById(courseId);
                if (courseResult != null && courseResult.getCode() == 200 && courseResult.getData() != null) {
                    String courseName = extractCourseName(courseResult.getData());
                    if (courseName != null && !courseName.isBlank()) {
                        courseNameMap.put(courseId, courseName);
                    }
                }
            } catch (Exception e) {
                log.warn("获取课程信息失败，courseId={}， error={}", courseId, e.getMessage());
            }
        }

        for (ExamSubmissionVo vo : submissionVoList) {
            if (vo.getCourseId() != null) {
                String courseName = courseNameMap.get(vo.getCourseId());
                if (courseName != null) {
                    vo.setCourseName(courseName);
                }
            }
        }
    }

    private void fillStudentInfoForSubmissions(List<ExamSubmissionVo> submissionVoList) {
        if (submissionVoList == null || submissionVoList.isEmpty()) {
            return;
        }

        Set<Long> studentIds = submissionVoList.stream()
                .map(ExamSubmissionVo::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Map<String, Object>> studentInfoMap = new HashMap<>();
        for (Long studentId : studentIds) {
            try {
                Result studentResult = educationUserStudentClient.getStudentById(studentId);
                if (studentResult != null && studentResult.getCode() == 200 && studentResult.getData() != null) {
                    Map<String, Object> studentMap = toMap(studentResult.getData());
                    if (studentMap != null) {
                        studentInfoMap.put(studentId, studentMap);
                    }
                }
            } catch (Exception e) {
                log.warn("获取学生信息失败，studentId={}， error={}", studentId, e.getMessage());
            }
        }

        for (ExamSubmissionVo vo : submissionVoList) {
            Map<String, Object> studentMap = studentInfoMap.get(vo.getStudentId());
            if (studentMap == null) {
                continue;
            }
            Object studentName = studentMap.get("realName");
            if (studentName == null) {
                studentName = studentMap.get("nickname");
            }
            Object studentNumber = studentMap.get("studentNumber");
            if (studentName != null) {
                vo.setStudentName(String.valueOf(studentName));
            }
            if (studentNumber != null) {
                vo.setStudentNumber(String.valueOf(studentNumber));
            }
        }
    }

    private void validateCreateExamDto(ExamCreateDto dto) {
        if (dto == null) {
            throw new BizException("创建参数不能为空");
        }
        if (dto.getCourseId() == null) {
            throw new BizException("courseId 不能为空");
        }
        if (dto.getTeacherId() == null) {
            throw new BizException("teacherId 不能为空");
        }
        if (StringUtils.isBlank(dto.getExamTitle())) {
            throw new BizException("测验标题不能为空");
        }
        if (StringUtils.isBlank(dto.getExamType())) {
            throw new BizException("测验类型不能为空");
        }
        if (dto.getTotalScore() == null || dto.getTotalScore() <= 0) {
            throw new BizException("总分必须大于 0");
        }
        if (dto.getPassScore() == null || dto.getPassScore() < 0 || dto.getPassScore() > dto.getTotalScore()) {
            throw new BizException("及格分必须在 0 与总分之间");
        }
        if (dto.getTimeLimit() != null && dto.getTimeLimit() < 0) {
            throw new BizException("时间限制不能小于 0");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new BizException("开始时间和结束时间不能为空");
        }
        validateExamTime(dto.getStartTime(), dto.getEndTime());
    }

    private void validateExamTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new BizException("结束时间不能早于开始时间");
        }
    }

    private void validateExamForPublish(Exam exam) {
        if (exam.getCourseId() == null) {
            throw new BizException("发布失败：courseId 不能为空");
        }
        if (exam.getTeacherId() == null) {
            throw new BizException("发布失败：teacherId 不能为空");
        }
        if (StringUtils.isBlank(exam.getExamTitle())) {
            throw new BizException("发布失败：测验标题不能为空");
        }
        if (StringUtils.isBlank(exam.getExamType())) {
            throw new BizException("发布失败：测验类型不能为空");
        }
        if (exam.getTotalScore() == null || exam.getTotalScore() <= 0) {
            throw new BizException("发布失败：总分必须大于 0");
        }
        if (exam.getPassScore() == null || exam.getPassScore() < 0 || exam.getPassScore() > exam.getTotalScore()) {
            throw new BizException("发布失败：及格分必须在 0 与总分之间");
        }
        if (exam.getTimeLimit() != null && exam.getTimeLimit() < 0) {
            throw new BizException("发布失败：时间限制不能小于 0");
        }
        if (exam.getStartTime() == null || exam.getEndTime() == null) {
            throw new BizException("发布失败：开始时间和结束时间不能为空");
        }
        validateExamTime(exam.getStartTime(), exam.getEndTime());
    }

    private void verifyTeacherAndCourse(Long teacherId, Long courseId) {
        if (teacherId == null || courseId == null) {
            throw new BizException("teacherId 和 courseId 不能为空");
        }
        checkTeacherExists(teacherId);

        try {
            Result courseResult = educationCourseClient.getCourseById(courseId);
            if (courseResult == null || courseResult.getCode() != 200 || courseResult.getData() == null) {
                throw new BizException("课程不存在");
            }
            Map<String, Object> courseMap = toMap(courseResult.getData());
            Long courseTeacherId = getLongValue(courseMap == null ? null : courseMap.get("teacherId"));
            if (courseTeacherId == null && courseMap != null) {
                courseTeacherId = getLongValue(courseMap.get("teacher_id"));
            }
            if (courseTeacherId != null && !courseTeacherId.equals(teacherId)) {
                throw new BizException("教师与课程归属不匹配");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("校验课程信息失败：" + e.getMessage());
        }
    }

    private void checkTeacherExists(Long teacherId) {
        try {
            Result teacherResult = educationUserTeacherClient.getTeachersNameById(teacherId);
            if (teacherResult == null || teacherResult.getCode() != 200 || teacherResult.getData() == null) {
                throw new BizException("教师不存在");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("校验教师信息失败：" + e.getMessage());
        }
    }

    private void validateExamQuestionCreateDto(ExamQuestionCreateDto dto) {
        if (dto == null) {
            throw new BizException("题目参数不能为空");
        }
        if (dto.getExamId() == null) {
            throw new BizException("examId 不能为空");
        }
        if (dto.getTeacherId() == null) {
            throw new BizException("teacherId 不能为空");
        }
        if (StringUtils.isBlank(dto.getQuestionType())) {
            throw new BizException("题目类型不能为空");
        }
        if (StringUtils.isBlank(dto.getQuestionContent())) {
            throw new BizException("题目内容不能为空");
        }
        if (dto.getScore() == null || dto.getScore() <= 0) {
            throw new BizException("题目分值必须大于 0");
        }
    }

    private BigDecimal calcObjectiveScore(String answersJson, List<ExamQuestion> questionList) {
        if (questionList == null || questionList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        JSONObject studentAnswers = parseAnswerJson(answersJson);
        BigDecimal score = BigDecimal.ZERO;
        for (ExamQuestion question : questionList) {
            if (!isObjectiveQuestion(question.getQuestionType())) {
                continue;
            }
            String studentAnswer = getStudentAnswer(studentAnswers, question.getId());
            if (isAnswerCorrect(question.getQuestionType(), question.getCorrectAnswer(), studentAnswer)) {
                score = score.add(BigDecimal.valueOf(
                        question.getScore() == null ? 0 : question.getScore()));
            }
        }
        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private List<ExamSubmissionStatisticsVo.QuestionAccuracyVo> buildQuestionAccuracy(
            List<ExamQuestion> questionList,
            List<ExamSubmission> submissionList) {
        List<ExamSubmissionStatisticsVo.QuestionAccuracyVo> result = new ArrayList<>();
        if (questionList == null || questionList.isEmpty()) {
            return result;
        }

        int total = submissionList == null ? 0 : submissionList.size();
        List<ExamQuestion> sortedQuestions = new ArrayList<>(questionList);
        sortedQuestions.sort(Comparator
                .comparing((ExamQuestion q) -> q.getSortOrder() == null ? 0 : q.getSortOrder())
                .thenComparing(ExamQuestion::getId));

        for (ExamQuestion question : sortedQuestions) {
            ExamSubmissionStatisticsVo.QuestionAccuracyVo vo = new ExamSubmissionStatisticsVo.QuestionAccuracyVo();
            vo.setQuestionId(question.getId());
            vo.setQuestionType(question.getQuestionType());
            vo.setQuestionContent(question.getQuestionContent());

            if (total <= 0 || !isObjectiveQuestion(question.getQuestionType())) {
                vo.setCorrectRate(BigDecimal.ZERO);
                result.add(vo);
                continue;
            }

            int correctCount = 0;
            for (ExamSubmission submission : submissionList) {
                JSONObject answerJson = parseAnswerJson(submission.getAnswers());
                String studentAnswer = getStudentAnswer(answerJson, question.getId());
                if (isAnswerCorrect(question.getQuestionType(), question.getCorrectAnswer(), studentAnswer)) {
                    correctCount++;
                }
            }

            BigDecimal correctRate = BigDecimal.valueOf(correctCount)
                    .multiply(HUNDRED)
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            vo.setCorrectRate(correctRate);
            result.add(vo);
        }
        return result;
    }

    private Map<Long, BigDecimal> parseManualScoreMap(JSONObject answerJson) {
        Map<Long, BigDecimal> map = new HashMap<>();
        if (answerJson == null || !answerJson.containsKey(MANUAL_SCORES_KEY)) {
            return map;
        }

        Object raw = answerJson.get(MANUAL_SCORES_KEY);
        JSONObject manualJson;
        if (raw instanceof JSONObject) {
            manualJson = (JSONObject) raw;
        } else {
            try {
                Object parsed = JSONUtil.parse(raw);
                if (parsed instanceof JSONObject) {
                    manualJson = (JSONObject) parsed;
                } else {
                    return map;
                }
            } catch (Exception e) {
                return map;
            }
        }

        for (String key : manualJson.keySet()) {
            Long questionId = getLongValue(key);
            if (questionId == null) {
                continue;
            }
            BigDecimal score = getBigDecimalValue(manualJson.get(key));
            if (score != null) {
                map.put(questionId, score);
            }
        }
        return map;
    }

    private Map<String, Integer> buildDefaultScoreRangeDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("90-100", 0);
        map.put("80-89", 0);
        map.put("70-79", 0);
        map.put("60-69", 0);
        map.put("0-59", 0);
        return map;
    }

    private void accumulateScoreRange(Map<String, Integer> map, BigDecimal score) {
        if (map == null || score == null) {
            return;
        }
        if (score.compareTo(new BigDecimal("90")) >= 0) {
            map.put("90-100", map.get("90-100") + 1);
            return;
        }
        if (score.compareTo(new BigDecimal("80")) >= 0) {
            map.put("80-89", map.get("80-89") + 1);
            return;
        }
        if (score.compareTo(new BigDecimal("70")) >= 0) {
            map.put("70-79", map.get("70-79") + 1);
            return;
        }
        if (score.compareTo(new BigDecimal("60")) >= 0) {
            map.put("60-69", map.get("60-69") + 1);
            return;
        }
        map.put("0-59", map.get("0-59") + 1);
    }

    private Long getLongValue(Object obj) {
        if (obj == null) {
            return null;
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

    private BigDecimal getBigDecimalValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        try {
            return new BigDecimal(String.valueOf(obj));
        } catch (Exception e) {
            return null;
        }
    }

    private String safeString(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    private String defaultString(String newValue, String oldValue) {
        return newValue == null ? oldValue : newValue;
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

    private boolean checkStudentExists(Long studentId) {
        try {
            Result result = educationUserStudentClient.getStudentById(studentId);
            return result != null && result.getCode() == 200 && result.getData() != null;
        } catch (Exception e) {
            log.warn("校验学生信息失败，studentId={}， error={}", studentId, e.getMessage());
            return false;
        }
    }

    private Long resolveStudentIdByUserId(Long userId) {
        Result result = educationUserStudentClient.getStudentByUserId(userId);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BizException("当前用户未绑定学生信息");
        }

        Map<String, Object> studentMap = toMap(result.getData());
        Long studentId = studentMap == null ? null : getLongValue(studentMap.get("id"));
        if (studentId == null) {
            throw new BizException("解析学生 ID 失败");
        }
        return studentId;
    }

    private Integer calculateRemainingMinutes(LocalDateTime startTime, Integer timeLimit, LocalDateTime now) {
        if (timeLimit == null || timeLimit <= 0) {
            return null;
        }
        if (startTime == null) {
            return timeLimit;
        }

        long usedMinutes = Math.max(0L, ChronoUnit.MINUTES.between(startTime, now));
        long remaining = timeLimit - usedMinutes;
        return (int) Math.max(0L, remaining);
    }

    private boolean isTimeLimitExceeded(LocalDateTime startTime, Integer timeLimit, LocalDateTime now) {
        if (startTime == null || timeLimit == null || timeLimit <= 0) {
            return false;
        }
        long minutes = ChronoUnit.MINUTES.between(startTime, now);
        return minutes > timeLimit;
    }

    private JSONObject parseAnswerJson(String answerJson) {
        if (answerJson == null || answerJson.isBlank()) {
            return JSONUtil.createObj();
        }
        try {
            Object parsed = JSONUtil.parse(answerJson);
            if (!(parsed instanceof JSONObject)) {
                throw new BizException("答案格式错误，必须是 JSON 对象");
            }
            return (JSONObject) parsed;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("答案格式错误，必须是 JSON 对象");
        }
    }

    private String getStudentAnswer(JSONObject studentAnswers, Long questionId) {
        if (studentAnswers == null || questionId == null) {
            return null;
        }
        Object value = studentAnswers.get(String.valueOf(questionId));
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private boolean isPendingReviewStatus(String status) {
        return SUBMISSION_STATUS_PENDING_REVIEW.equals(status)
                || SUBMISSION_STATUS_SUBMITTED.equals(status);
    }

    private boolean isGradedStatus(String status) {
        return SUBMISSION_STATUS_GRADED.equals(status)
                || SUBMISSION_STATUS_AUTO_GRADED.equals(status)
                || SUBMISSION_STATUS_MANUAL_GRADED.equals(status);
    }

    private boolean isObjectiveQuestion(String questionType) {
        return EXAM_TYPE_SINGLE_CHOICE.equals(questionType)
                || EXAM_TYPE_MULTI_CHOICE.equals(questionType)
                || EXAM_TYPE_TRUE_FALSE.equals(questionType)
                || EXAM_TYPE_FILL_BLANK.equals(questionType);
    }

    private boolean isAnswerCorrect(String questionType, String correctAnswer, String studentAnswer) {
        if (correctAnswer == null || studentAnswer == null) {
            return false;
        }

        if (EXAM_TYPE_MULTI_CHOICE.equals(questionType)) {
            return normalizeMultiChoiceAnswer(correctAnswer).equals(normalizeMultiChoiceAnswer(studentAnswer));
        }
        if (EXAM_TYPE_TRUE_FALSE.equals(questionType)) {
            return normalizeSimpleAnswer(correctAnswer).equalsIgnoreCase(normalizeSimpleAnswer(studentAnswer));
        }
        if (EXAM_TYPE_FILL_BLANK.equals(questionType)) {
            return normalizeSimpleAnswer(correctAnswer).equals(normalizeSimpleAnswer(studentAnswer));
        }
        return normalizeSimpleAnswer(correctAnswer).equals(normalizeSimpleAnswer(studentAnswer));
    }

    private String normalizeSimpleAnswer(String answer) {
        return answer == null ? "" : answer.trim().toUpperCase();
    }

    private String normalizeMultiChoiceAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            return "";
        }

        Set<String> answerSet = new HashSet<>();
        String text = answer.trim();

        if (text.startsWith("[") && text.endsWith("]")) {
            try {
                List<Object> list = JSONUtil.parseArray(text).toList(Object.class);
                for (Object item : list) {
                    if (item != null) {
                        answerSet.add(String.valueOf(item).trim().toUpperCase());
                    }
                }
            } catch (Exception e) {
                String[] parts = text.replace("[", "").replace("]", "").split("[,，]");
                for (String part : parts) {
                    if (part != null && !part.isBlank()) {
                        answerSet.add(part.trim().replace("\"", "").toUpperCase());
                    }
                }
            }
        } else {
            String[] parts = text.split("[,，]");
            for (String part : parts) {
                if (part != null && !part.isBlank()) {
                    answerSet.add(part.trim().toUpperCase());
                }
            }
        }

        List<String> sorted = new ArrayList<>(answerSet);
        sorted.sort(String::compareTo);
        return String.join(",", sorted);
    }

    private String resolveCourseName(Long courseId) {
        if (courseId == null) {
            return null;
        }
        try {
            Result courseResult = educationCourseClient.getCourseById(courseId);
            if (courseResult == null || courseResult.getCode() == null || courseResult.getCode() != 200
                    || courseResult.getData() == null) {
                return null;
            }
            return extractCourseName(courseResult.getData());
        } catch (Exception e) {
            log.warn("查询课程名称失败，courseId={}， error={}", courseId, e.getMessage());
            return null;
        }
    }

    private String extractCourseName(Object data) {
        Map<String, Object> courseData = toMap(data);
        if (courseData == null) {
            return null;
        }
        Object courseName = courseData.get("courseName");
        if (courseName == null) {
            courseName = courseData.get("course_name");
        }
        return courseName == null ? null : String.valueOf(courseName);
    }

    private void notifyCourseStudentsExamPublished(Exam exam, Long teacherId) {
        if (exam == null || exam.getId() == null || exam.getCourseId() == null || teacherId == null) {
            return;
        }

        List<Long> studentIds = studentCourseMapper.selectList(new LambdaQueryWrapper<StudentCourse>()
                        .select(StudentCourse::getStudentId)
                        .eq(StudentCourse::getCourseId, exam.getCourseId())
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
            Long userId = resolveStudentUserId(studentId);
            if (userId != null && userId > 0) {
                userIds.add(userId);
            }
        }
        if (userIds.isEmpty()) {
            return;
        }

        TemplateTriggerEventRequest payload = new TemplateTriggerEventRequest();
        payload.setEventId(buildExamEventId(EVENT_CODE_EXAM_PUBLISHED, exam.getId()));
        payload.setEventCode(EVENT_CODE_EXAM_PUBLISHED);
        payload.setTargetType(TARGET_TYPE_USER);
        payload.setTargetValue(new ArrayList<>(userIds));
        payload.setParams(buildExamPublishParams(exam));
        payload.setMessageType(MESSAGE_TYPE_COURSE);
        payload.setRelatedId(exam.getId());
        payload.setRelatedType(MESSAGE_RELATED_TYPE_EXAM);
        payload.setPriority(MESSAGE_PRIORITY_NORMAL);
        payload.setDeliverMode(DELIVER_MODE_MQ);

        Result triggerResult = educationMessageInternalClient.triggerEvent(teacherId, ROLE_TEACHER, payload);
        if (triggerResult == null || triggerResult.getCode() == null || triggerResult.getCode() != 200) {
            log.warn("测验发布通知触发失败，examId={}， message={}",
                    exam.getId(), triggerResult == null ? null : triggerResult.getMessage());
        }
    }

    private void notifyExamSubmissionGraded(ExamSubmission submission, ExamSubmissionVo submissionVo, Long teacherId) {
        if (submission == null || submission.getId() == null || submission.getStudentId() == null || teacherId == null) {
            return;
        }

        Long studentUserId = resolveStudentUserId(submission.getStudentId());
        if (studentUserId == null || studentUserId <= 0) {
            return;
        }

        Exam exam = submission.getExamId() == null ? null : examMapper.selectById(submission.getExamId());
        String examTitle = submissionVo == null ? null : submissionVo.getExamTitle();
        if (StringUtils.isBlank(examTitle) && exam != null) {
            examTitle = exam.getExamTitle();
        }
        if (StringUtils.isBlank(examTitle)) {
            examTitle = "课程测验";
        }

        Integer passScore = exam == null ? null : exam.getPassScore();
        BigDecimal score = submission.getTotalScore() == null ? BigDecimal.ZERO : submission.getTotalScore();
        Boolean isPass = passScore == null ? null : score.compareTo(BigDecimal.valueOf(passScore)) >= 0;

        TemplateTriggerEventRequest payload = new TemplateTriggerEventRequest();
        payload.setEventId(buildExamEventId(EVENT_CODE_EXAM_SUBMISSION_GRADED, submission.getId()));
        payload.setEventCode(EVENT_CODE_EXAM_SUBMISSION_GRADED);
        payload.setTargetType(TARGET_TYPE_USER);
        payload.setTargetValue(List.of(studentUserId));
        payload.setParams(buildExamSubmissionGradedParams(submission, examTitle, passScore, isPass));
        payload.setMessageType(MESSAGE_TYPE_COURSE);
        payload.setRelatedId(submission.getExamId());
        payload.setRelatedType(MESSAGE_RELATED_TYPE_EXAM);
        payload.setPriority(MESSAGE_PRIORITY_NORMAL);
        payload.setDeliverMode(DELIVER_MODE_MQ);

        Result triggerResult = educationMessageInternalClient.triggerEvent(teacherId, ROLE_TEACHER, payload);
        if (triggerResult == null || triggerResult.getCode() == null || triggerResult.getCode() != 200) {
            log.warn("测验批改通知触发失败，submissionId={}， studentUserId={}， message={}",
                    submission.getId(), studentUserId, triggerResult == null ? null : triggerResult.getMessage());
        }
    }

    private Map<String, Object> buildExamPublishParams(Exam exam) {
        Map<String, Object> params = new HashMap<>();
        String courseName = resolveCourseName(exam.getCourseId());
        params.put("exam_title", StringUtils.isBlank(exam.getExamTitle()) ? "课程测验" : exam.getExamTitle().trim());
        params.put("course_name", StringUtils.isBlank(courseName) ? "课程" : courseName);
        params.put("start_time", exam.getStartTime() == null ? "" : exam.getStartTime());
        params.put("end_time", exam.getEndTime() == null ? "" : exam.getEndTime());
        return params;
    }

    private Map<String, Object> buildExamSubmissionGradedParams(
            ExamSubmission submission,
            String examTitle,
            Integer passScore,
            Boolean isPass
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("exam_title", examTitle);
        params.put("total_score", submission.getTotalScore() == null ? "" : submission.getTotalScore());
        params.put("pass_score", passScore == null ? "" : passScore);
        params.put("is_pass", toPassText(isPass));
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
            log.warn("查询学生用户映射失败，studentId={}， error={}", studentId, e.getMessage());
            return null;
        }
    }

    private String buildExamEventId(String eventCode, Long bizId) {
        String safeEventCode = StringUtils.isBlank(eventCode) ? "EXAM_EVENT" : eventCode.trim().toUpperCase();
        return safeEventCode + "_" + bizId + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 自动批改结果封装。
     */
    private static class AutoGradeResult {
        private final BigDecimal score;
        private final boolean containEssayQuestion;

        private AutoGradeResult(BigDecimal score, boolean containEssayQuestion) {
            this.score = score;
            this.containEssayQuestion = containEssayQuestion;
        }

        public BigDecimal getScore() {
            return score;
        }

        public boolean isContainEssayQuestion() {
            return containEssayQuestion;
        }
    }
}

