package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.xixi.entity.StudentCourse;
import com.xixi.exception.BizException;
import com.xixi.mapper.LearningStatisticsMapper;
import com.xixi.mapper.StudyNoteMapper;
import com.xixi.mapper.StudentCourseMapper;
import com.xixi.openfeign.course.EducationCourseClient;
import com.xixi.openfeign.user.EducationUserStudentClient;
import com.xixi.pojo.query.LearningStatisticsQuery;
import com.xixi.pojo.vo.LearningStatisticsOverviewVo;
import com.xixi.pojo.vo.LearningStatisticsProgressVo;
import com.xixi.pojo.vo.LearningStatisticsSummaryVo;
import com.xixi.pojo.vo.LearningStatisticsTimeVo;
import com.xixi.pojo.vo.TeacherCourseActivityVo;
import com.xixi.pojo.vo.TeacherCourseProgressAnalysisVo;
import com.xixi.pojo.vo.TeacherCourseTimeAnalysisVo;
import com.xixi.service.LearningStatisticsService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *学统计服务实现类。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningStatisticsServiceImpl implements LearningStatisticsService {

    private final LearningStatisticsMapper learningStatisticsMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final StudyNoteMapper studyNoteMapper;
    private final EducationCourseClient educationCourseClient;
    private final EducationUserStudentClient educationUserStudentClient;

    /**
     * 查询学生学习统计概览。
     * @param studentId学ID
     * @return学统计概览
     */
    @Override
    public LearningStatisticsOverviewVo getOverview(Long studentId) {
        if (studentId == null) {
            throw new BizException("studentId不能为空");
        }

        LearningStatisticsOverviewVo vo = new LearningStatisticsOverviewVo();

        List<StudentCourse> studentCourses = studentCourseMapper.selectByStudentId(studentId);
        if (studentCourses == null) {
            studentCourses = new ArrayList<>();
        }

        int completedCourses = 0;
        int studyingCourses = 0;

        for (StudentCourse sc : studentCourses) {
            if ("COMPLETED".equals(sc.getLearningStatus())) {
                completedCourses++;
            } else if ("STUDYING".equals(sc.getLearningStatus())) {
                studyingCourses++;
            }
        }

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int totalStudyTimeSeconds = safeInt(learningStatisticsMapper.selectStudyTimeInRange(
                studentId, weekStart.toString(), today.toString()));

        BigDecimal totalStudyHours = BigDecimal.valueOf(totalStudyTimeSeconds)
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);

        vo.setTotalStudyHours(totalStudyHours);
        vo.setCompletedCourses(completedCourses);
        vo.setStudyingCourses(studyingCourses);

        Long totalNotes = studyNoteMapper.selectCountByStudentId(studentId);
        vo.setTotalNotes(totalNotes == null ? 0 : totalNotes.intValue());

        vo.setTotalHomeworkSubmissions(safeInt(learningStatisticsMapper.selectHomeworkSubmissionCount(studentId)));
        vo.setTotalExamSubmissions(safeInt(learningStatisticsMapper.selectExamSubmissionCount(studentId)));

        int totalPublishedHomeworkCount = safeInt(learningStatisticsMapper.selectStudentPublishedHomeworkCount(studentId));
        int submittedHomeworkCount = safeInt(learningStatisticsMapper.selectStudentSubmittedHomeworkCount(studentId));
        vo.setPendingHomeworkCount(Math.max(totalPublishedHomeworkCount - submittedHomeworkCount, 0));

        return vo;
    }

    /**
     * 查询学生学习时长统计。
     * @param query 查询条件
     * @return学习时长统计
     */
    @Override
    public LearningStatisticsTimeVo getStudyTime(LearningStatisticsQuery query) {
        if (query == null) {
            query = new LearningStatisticsQuery();
        }
        if (query.getStudentId() == null) {
            throw new BizException("studentId不能为空");
        }
        applyDefaultDateRangeByPeriod(query);

        LearningStatisticsTimeVo vo = new LearningStatisticsTimeVo();
        List<LearningStatisticsTimeVo.TimeData> timeDataList = new ArrayList<>();

        List<Map<String, Object>> dataList = learningStatisticsMapper.selectStudyTimeByDate(
                query.getStudentId(), query.getStartDate(), query.getEndDate());

        for (Map<String, Object> data : dataList) {
            LearningStatisticsTimeVo.TimeData timeData = new LearningStatisticsTimeVo.TimeData();
            timeData.setDate(getStringValue(data.get("date")));
            Integer seconds = getIntegerValue(data.get("studyTime"));
            timeData.setStudyHours(seconds == null ? 0.0 : seconds / 3600.0);
            timeDataList.add(timeData);
        }

        vo.setTimeDataList(timeDataList);
        return vo;
    }

    /**
     * 查询学生学习进度统计。
     * @param studentId学ID
     * @return学习进度统计
     */
    @Override
    public LearningStatisticsProgressVo getProgress(Long studentId) {
        LearningStatisticsProgressVo vo = new LearningStatisticsProgressVo();
        List<LearningStatisticsProgressVo.CourseProgress> courseProgressList = new ArrayList<>();

        List<Map<String, Object>> progressList = learningStatisticsMapper.selectCourseProgress(studentId);

        for (Map<String, Object> progress : progressList) {
            LearningStatisticsProgressVo.CourseProgress courseProgress =
                    new LearningStatisticsProgressVo.CourseProgress();
            courseProgress.setCourseId(getLongValue(progress.get("courseId")));
            courseProgress.setCourseName(getStringValue(progress.get("courseName")));
            courseProgress.setProgressPercentage(getBigDecimalValue(progress.get("progressPercentage")));
            courseProgressList.add(courseProgress);
        }

        vo.setCourseProgressList(courseProgressList);
        return vo;
    }

    /**
     * 查询学生学习活跃度统计。
     * @param studentId学ID
     * @return学习活跃度统计（按时段分布）
     */
    @Override
    public List<Map<String, Object>> getActivity(Long studentId) {
        return learningStatisticsMapper.selectStudyTimeByHour(studentId);
    }

    /**
     *学生学习数据。
     * @param studentId 学生ID
     * @return学习数据汇总
     */
    @Override
    public LearningStatisticsSummaryVo getSummary(Long studentId) {
        if (studentId == null) {
            throw new BizException("studentId不能为空");
        }

        Map<String, Object> summaryMap = learningStatisticsMapper.selectLearningSummary(studentId);
        List<String> dateList = learningStatisticsMapper.selectStudyDateList(studentId);
        if (dateList == null) {
            dateList = new ArrayList<>();
        }

        int totalStudyTimeSeconds = safeInt(getIntegerValue(summaryMap == null ? null : summaryMap.get("totalStudyTime")));
        int studyDays = safeInt(getIntegerValue(summaryMap == null ? null : summaryMap.get("studyDays")));
        if (studyDays == 0 && !dateList.isEmpty()) {
            studyDays = dateList.size();
        }

        BigDecimal totalStudyHours = BigDecimal.valueOf(totalStudyTimeSeconds)
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
        BigDecimal averageDailyStudyHours = BigDecimal.ZERO;
        if (studyDays > 0) {
            averageDailyStudyHours = totalStudyHours
                    .divide(BigDecimal.valueOf(studyDays), 2, RoundingMode.HALF_UP);
        }

        List<LocalDate> studyDates = new ArrayList<>();
        for (String dateText : dateList) {
            LocalDate date = toLocalDate(dateText);
            if (date != null) {
                studyDates.add(date);
            }
        }

        int maxStreakDays = calculateMaxStreakDays(studyDates);
        int currentStreakDays = calculateCurrentStreakDays(studyDates);

        BigDecimal consistencyRate = BigDecimal.ZERO;
        if (!studyDates.isEmpty()) {
            LocalDate first = studyDates.get(0);
            LocalDate last = studyDates.get(studyDates.size() - 1);
            long spanDays = ChronoUnit.DAYS.between(first, last) + 1;
            if (spanDays > 0) {
                consistencyRate = BigDecimal.valueOf(studyDates.size())
                        .multiply(new BigDecimal("100"))
                        .divide(BigDecimal.valueOf(spanDays), 2, RoundingMode.HALF_UP);
            }
        }

        LearningStatisticsSummaryVo vo = new LearningStatisticsSummaryVo();
        vo.setStudentId(studentId);
        vo.setTotalStudyHours(totalStudyHours);
        vo.setStudyDays(studyDays);
        vo.setAverageDailyStudyHours(averageDailyStudyHours);
        vo.setCurrentStreakDays(currentStreakDays);
        vo.setMaxStreakDays(maxStreakDays);
        vo.setConsistencyRate(consistencyRate);
        return vo;
    }

    /**
     *教端查询课程学生活跃度统计。
     * @param courseId 课程ID
     * @return度统计结果
     */
    @Override
    public TeacherCourseActivityVo getTeacherCourseActivity(Long courseId) {
        if (courseId == null) {
            throw new BizException("courseId不能为空");
        }

        TeacherCourseActivityVo vo = new TeacherCourseActivityVo();
        vo.setCourseId(courseId);
        vo.setCourseName(resolveCourseName(courseId));

        Integer activeStudentCount = learningStatisticsMapper.selectTeacherCourseActiveStudentCount(courseId);
        vo.setActiveStudentCount(activeStudentCount == null ? 0 : activeStudentCount);

        List<Map<String, Object>> studyTimeRows = learningStatisticsMapper.selectTeacherCourseStudyTimeRanking(courseId);
        List<Map<String, Object>> progressRows = learningStatisticsMapper.selectTeacherCourseProgressRanking(courseId);

        Set<Long> studentIds = new HashSet<>();
        for (Map<String, Object> row : studyTimeRows) {
            Long studentId = getLongValue(row.get("studentId"));
            if (studentId != null) {
                studentIds.add(studentId);
            }
        }
        for (Map<String, Object> row : progressRows) {
            Long studentId = getLongValue(row.get("studentId"));
            if (studentId != null) {
                studentIds.add(studentId);
            }
        }

        Map<Long, Map<String, Object>> studentInfoMap = fetchStudentInfoMap(studentIds);

        List<TeacherCourseActivityVo.StudyTimeRankItem> studyTimeRanking = new ArrayList<>();
        int studyRank = 1;
        for (Map<String, Object> row : studyTimeRows) {
            Long studentId = getLongValue(row.get("studentId"));
            if (studentId == null) {
                continue;
            }
            TeacherCourseActivityVo.StudyTimeRankItem item = new TeacherCourseActivityVo.StudyTimeRankItem();
            item.setRank(studyRank++);
            item.setStudentId(studentId);
            item.setTotalStudyTime(safeInt(getIntegerValue(row.get("totalStudyTime"))));
            fillStudentBaseInfo(studentInfoMap.get(studentId), item);
            studyTimeRanking.add(item);
        }
        vo.setStudyTimeRanking(studyTimeRanking);

        List<TeacherCourseActivityVo.ProgressRankItem> progressRanking = new ArrayList<>();
        int progressRank = 1;
        for (Map<String, Object> row : progressRows) {
            Long studentId = getLongValue(row.get("studentId"));
            if (studentId == null) {
                continue;
            }
            TeacherCourseActivityVo.ProgressRankItem item = new TeacherCourseActivityVo.ProgressRankItem();
            item.setRank(progressRank++);
            item.setStudentId(studentId);
            item.setProgressPercentage(getBigDecimalValue(row.get("progressPercentage")));
            fillStudentBaseInfo(studentInfoMap.get(studentId), item);
            progressRanking.add(item);
        }
        vo.setProgressRanking(progressRanking);

        List<Map<String, Object>> distributionRows = learningStatisticsMapper.selectTeacherCourseActiveTimeDistribution(courseId);
        List<TeacherCourseActivityVo.ActiveTimeDistributionItem> distributions = new ArrayList<>();
        for (Map<String, Object> row : distributionRows) {
            Integer hour = getIntegerValue(row.get("hour"));
            if (hour == null) {
                continue;
            }
            TeacherCourseActivityVo.ActiveTimeDistributionItem item = new TeacherCourseActivityVo.ActiveTimeDistributionItem();
            item.setHour(hour);
            item.setTimeRange(buildTimeRange(hour));
            item.setStudyTime(safeInt(getIntegerValue(row.get("studyTime"))));
            distributions.add(item);
        }
        vo.setActiveTimeDistribution(distributions);

        return vo;
    }

    /**
     *教师端查询课程学习进度分析。
     * @param courseId 课程ID
     * @return学习进度分析结果
     */
    @Override
    public TeacherCourseProgressAnalysisVo getTeacherCourseProgressAnalysis(Long courseId) {
        if (courseId == null) {
            throw new BizException("courseId不能为空");
        }

        TeacherCourseProgressAnalysisVo vo = new TeacherCourseProgressAnalysisVo();
        vo.setCourseId(courseId);
        vo.setCourseName(resolveCourseName(courseId));

        Map<String, Object> summary = learningStatisticsMapper.selectTeacherCourseProgressSummary(courseId);
        int totalStudents = safeInt(getIntegerValue(summary == null ? null : summary.get("totalStudents")));
        BigDecimal averageProgress = getBigDecimalValue(summary == null ? null : summary.get("averageProgress"))
                .setScale(2, RoundingMode.HALF_UP);
        vo.setTotalStudents(totalStudents);
        vo.setAverageProgress(averageProgress);

        Map<String, Integer> distributionMap = new LinkedHashMap<>();
        distributionMap.put("0-20", 0);
        distributionMap.put("21-40", 0);
        distributionMap.put("41-60", 0);
        distributionMap.put("61-80", 0);
        distributionMap.put("81-100", 0);

        List<Map<String, Object>> distributionRows = learningStatisticsMapper.selectTeacherCourseProgressDistribution(courseId);
        if (distributionRows == null) {
            distributionRows = new ArrayList<>();
        }
        for (Map<String, Object> row : distributionRows) {
            String rangeLabel = getStringValue(row == null ? null : row.get("rangeLabel"));
            if (rangeLabel == null || !distributionMap.containsKey(rangeLabel)) {
                continue;
            }
            distributionMap.put(rangeLabel, safeInt(getIntegerValue(row.get("studentCount"))));
        }

        List<TeacherCourseProgressAnalysisVo.ProgressDistributionItem> distributionList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : distributionMap.entrySet()) {
            TeacherCourseProgressAnalysisVo.ProgressDistributionItem item =
                    new TeacherCourseProgressAnalysisVo.ProgressDistributionItem();
            item.setRangeLabel(entry.getKey());
            item.setStudentCount(entry.getValue());
            distributionList.add(item);
        }
        vo.setProgressDistribution(distributionList);

        List<Map<String, Object>> trendRows = learningStatisticsMapper.selectTeacherCourseProgressTrend(courseId);
        if (trendRows == null) {
            trendRows = new ArrayList<>();
        }
        List<TeacherCourseProgressAnalysisVo.ProgressTrendItem> trendList = new ArrayList<>();
        for (Map<String, Object> row : trendRows) {
            TeacherCourseProgressAnalysisVo.ProgressTrendItem item =
                    new TeacherCourseProgressAnalysisVo.ProgressTrendItem();
            item.setDate(getStringValue(row == null ? null : row.get("statDate")));
            item.setAverageProgress(getBigDecimalValue(row == null ? null : row.get("averageProgress"))
                    .setScale(2, RoundingMode.HALF_UP));
            item.setActiveStudents(safeInt(getIntegerValue(row == null ? null : row.get("activeStudents"))));
            trendList.add(item);
        }
        vo.setProgressTrend(trendList);

        return vo;
    }

    /**
     *教师端查询课程活跃时间段分析。
     * @param courseId 课程ID
     * @return时间段分析结果
     */
    @Override
    public TeacherCourseTimeAnalysisVo getTeacherCourseTimeAnalysis(Long courseId) {
        if (courseId == null) {
            throw new BizException("courseId不能为空");
        }

        TeacherCourseTimeAnalysisVo vo = new TeacherCourseTimeAnalysisVo();
        vo.setCourseId(courseId);
        vo.setCourseName(resolveCourseName(courseId));

        List<Map<String, Object>> rows = learningStatisticsMapper.selectTeacherCourseTimeAnalysis(courseId);
        Map<Integer, Map<String, Object>> rowMap = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Integer hour = getIntegerValue(row == null ? null : row.get("hour"));
                if (hour != null) {
                    rowMap.put(hour, row);
                }
            }
        }

        List<TeacherCourseTimeAnalysisVo.TimeAnalysisItem> items = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            Map<String, Object> row = rowMap.get(hour);
            int studentCount = safeInt(getIntegerValue(row == null ? null : row.get("studentCount")));
            int studyTime = safeInt(getIntegerValue(row == null ? null : row.get("studyTime")));

            TeacherCourseTimeAnalysisVo.TimeAnalysisItem item = new TeacherCourseTimeAnalysisVo.TimeAnalysisItem();
            item.setHour(hour);
            item.setTimeRange(buildTimeRange(hour));
            item.setStudentCount(studentCount);
            item.setStudyTime(studyTime);
            item.setHeatValue(studyTime);
            items.add(item);
        }

        vo.setTimeAnalysis(items);
        return vo;
    }

    /**
     *学生基础信息（用于学习时长排名）。
     * @param studentInfo学信息Map
     * @param item 排名项
     */
    private void fillStudentBaseInfo(Map<String, Object> studentInfo, TeacherCourseActivityVo.StudyTimeRankItem item) {
        if (studentInfo == null || item == null) {
            return;
        }
        item.setUserId(getLongValue(studentInfo.get("userId")));
        item.setStudentNumber(getStringValue(studentInfo.get("studentNumber")));
        item.setStudentName(getStringValue(studentInfo.get("realName")));
    }

    /**
     *学生基础信息（用于学习进度排名）。
     * @param studentInfo学信息Map
     * @param item 排名项
     */
    private void fillStudentBaseInfo(Map<String, Object> studentInfo, TeacherCourseActivityVo.ProgressRankItem item) {
        if (studentInfo == null || item == null) {
            return;
        }
        item.setUserId(getLongValue(studentInfo.get("userId")));
        item.setStudentNumber(getStringValue(studentInfo.get("studentNumber")));
        item.setStudentName(getStringValue(studentInfo.get("realName")));
    }

    /**
     * 通过OpenFeign批量补全学生信息。
     * @param studentIds 学生ID集合
     * @return学生信息映射
     */
    @SuppressWarnings("unchecked")
    private Map<Long, Map<String, Object>> fetchStudentInfoMap(Collection<Long> studentIds) {
        Map<Long, Map<String, Object>> result = new HashMap<>();
        if (studentIds == null || studentIds.isEmpty()) {
            return result;
        }

        for (Long studentId : studentIds) {
            if (studentId == null) {
                continue;
            }
            try {
                Result studentResult = educationUserStudentClient.getStudentById(studentId);
                if (studentResult == null || studentResult.getCode() != 200 || studentResult.getData() == null) {
                    continue;
                }
                Map<String, Object> studentInfo;
                if (studentResult.getData() instanceof Map) {
                    studentInfo = (Map<String, Object>) studentResult.getData();
                } else {
                    studentInfo = JSONUtil.toBean(JSONUtil.toJsonStr(studentResult.getData()), Map.class);
                }
                if (studentInfo != null) {
                    result.put(studentId, studentInfo);
                }
            } catch (Exception e) {
                log.warn("获取学生信息失败，studentId={}, error={}", studentId, e.getMessage());
            }
        }
        return result;
    }

    /**
     * 查询课程名称。
     * @param courseId 课程ID
     * @return 课程名称
     */
    private String resolveCourseName(Long courseId) {
        try {
            Result result = educationCourseClient.getCourseById(courseId);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                Map<String, Object> map = toMap(result.getData());
                String courseName = getStringValue(map == null ? null : map.get("courseName"));
                if (courseName != null && !courseName.isEmpty()) {
                    return courseName;
                }
            }
        } catch (Exception e) {
            log.warn("通过getCourseById获取课程名称失败，courseId={}, error={}", courseId, e.getMessage());
        }

        try {
            Result result = educationCourseClient.getCourseDetail(courseId);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                Map<String, Object> detailMap = toMap(result.getData());
                Map<String, Object> courseInfo = toMap(detailMap == null ? null : detailMap.get("courseInfo"));
                return getStringValue(courseInfo == null ? null : courseInfo.get("courseName"));
            }
        } catch (Exception e) {
            log.warn("通过getCourseDetail获取课程名称失败，courseId={}, error={}", courseId, e.getMessage());
        }

        return null;
    }

    /**
     *构建小时区间文本。
     * @param hour小
     * @return区文本
     */
    private String buildTimeRange(Integer hour) {
        return String.format("%02d:00-%02d:59", hour, hour);
    }

    /**
     *根据 period补时间范围
     * DAY: 今天
     * WEEK: 本周（周一到今天）
     * MONTH: 本月（1号到今天）
     */
    private void applyDefaultDateRangeByPeriod(LearningStatisticsQuery query) {
        if (query == null) {
            return;
        }
        String period = query.getPeriod();
        if (period == null || period.trim().isEmpty()) {
            return;
        }

        boolean hasStartDate = query.getStartDate() != null && !query.getStartDate().trim().isEmpty();
        boolean hasEndDate = query.getEndDate() != null && !query.getEndDate().trim().isEmpty();
        if (hasStartDate && hasEndDate) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate;
        switch (period.trim().toUpperCase()) {
            case "DAY":
                startDate = today;
                break;
            case "WEEK":
                startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                break;
            case "MONTH":
                startDate = today.withDayOfMonth(1);
                break;
            default:
                return;
        }

        if (!hasStartDate) {
            query.setStartDate(startDate.toString());
        }
        if (!hasEndDate) {
            query.setEndDate(today.toString());
        }
    }

    /**
     * 解析日期文本
     * @param text 日期文本
     * @return 日期
     */
    private LocalDate toLocalDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *计算最长连续学习天数
     * @param dateList已排序学习日期列表
     * @return 最长连续天数
     */
    private int calculateMaxStreakDays(List<LocalDate> dateList) {
        if (dateList == null || dateList.isEmpty()) {
            return 0;
        }
        int maxStreak = 1;
        int currentStreak = 1;
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate currentDate = dateList.get(i);
            LocalDate previousDate = dateList.get(i - 1);
            if (previousDate.plusDays(1).equals(currentDate)) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }
            if (currentStreak > maxStreak) {
                maxStreak = currentStreak;
            }
        }
        return maxStreak;
    }

    /**
     *计算当前连续学习天数
     * @param dateList已排序学习日期列表
     * @return 当前连续天数
     */
    private int calculateCurrentStreakDays(List<LocalDate> dateList) {
        if (dateList == null || dateList.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate lastStudyDate = dateList.get(dateList.size() - 1);
        if (lastStudyDate.isBefore(today.minusDays(1))) {
            return 0;
        }

        int streak = 1;
        for (int i = dateList.size() - 1; i > 0; i--) {
            LocalDate currentDate = dateList.get(i);
            LocalDate previousDate = dateList.get(i - 1);
            if (previousDate.plusDays(1).equals(currentDate)) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    /**
     *安全获取字符串值。
     * @param obj 对象
     * @return 字符串
     */
    private String getStringValue(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    /**
     *安全获取Long值。
     * @param obj 对象
     * @return Long值
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
     *安全获取Integer值。
     * @param obj 对象
     * @return Integer值
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

    /**
     *安全获取BigDecimal值。
     * @param obj 对象
     * @return BigDecimal值
     */
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

    /**
     *将对象安全转为Map。
     * @param obj 对象
     * @return Map
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
     *将可空整数转为安全整数（默认0）。
     * @param value可空整数
     * @return安全整数
     */
    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}

