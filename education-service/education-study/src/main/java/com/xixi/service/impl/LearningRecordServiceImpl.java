package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.LearningRecord;
import com.xixi.entity.StudentCourse;
import com.xixi.exception.BizException;
import com.xixi.mapper.LearningRecordMapper;
import com.xixi.mapper.StudentCourseMapper;
import com.xixi.openfeign.course.EducationCourseClient;
import com.xixi.openfeign.user.EducationUserStudentClient;
import com.xixi.pojo.dto.LearningRecordSaveDto;
import com.xixi.pojo.query.LearningRecordQuery;
import com.xixi.pojo.query.TeacherLearningMonitorQuery;
import com.xixi.pojo.vo.LearningRecordVo;
import com.xixi.pojo.vo.TeacherLearningMonitorVo;
import com.xixi.pojo.vo.TeacherStudentCourseVo;
import com.xixi.pojo.vo.VideoProgressVo;
import com.xixi.service.LearningRecordService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 学习记录服务实现类。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningRecordServiceImpl implements LearningRecordService {

    private static final BigDecimal CHAPTER_COMPLETE_THRESHOLD = new BigDecimal("80");
    private static final BigDecimal FULL_PROGRESS = new BigDecimal("100");
    private static final String LEARNING_STATUS_STUDYING = "STUDYING";
    private static final String LEARNING_STATUS_COMPLETED = "COMPLETED";

    private final LearningRecordMapper learningRecordMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final EducationCourseClient educationCourseClient;
    private final EducationUserStudentClient educationUserStudentClient;

    /**
     * 保存学习记录并更新学生课程学习时长。
     * @param dto 学习记录DTO
     * @return 保存结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveLearningRecord(LearningRecordSaveDto dto) {
        try {
            StudentCourse studentCourse = studentCourseMapper.selectByStudentIdAndCourseId(
                    dto.getStudentId(), dto.getCourseId());
            if (studentCourse == null) {
                return Result.error("未找到选课记录，请先加入课程");
            }

            LearningRecord learningRecord = new LearningRecord();
            learningRecord.setStudentId(dto.getStudentId());
            learningRecord.setCourseId(dto.getCourseId());
            learningRecord.setChapterId(dto.getChapterId());
            learningRecord.setVideoId(dto.getVideoId());
            learningRecord.setVideoProgress(dto.getVideoProgress());
            learningRecord.setDuration(dto.getDuration());
            learningRecord.setLastPosition(dto.getLastPosition());

            LocalDateTime now = LocalDateTime.now();
            learningRecord.setStartTime(now.minusSeconds(dto.getDuration() == null ? 0 : dto.getDuration()));
            learningRecord.setEndTime(now);

            learningRecordMapper.insert(learningRecord);

            int currentTotalStudyTime = safeInt(studentCourse.getTotalStudyTime());
            int duration = dto.getDuration() == null ? 0 : dto.getDuration();
            studentCourse.setTotalStudyTime(currentTotalStudyTime + Math.max(duration, 0));
            studentCourse.setLastStudyTime(now);
            BigDecimal calculatedProgress = calculateCourseProgress(dto.getStudentId(), dto.getCourseId());
            BigDecimal currentProgress = defaultBigDecimal(studentCourse.getProgressPercentage());
            if (calculatedProgress.compareTo(currentProgress) < 0) {
                calculatedProgress = currentProgress;
            }
            studentCourse.setProgressPercentage(calculatedProgress);
            if (calculatedProgress.compareTo(FULL_PROGRESS) >= 0) {
                studentCourse.setLearningStatus(LEARNING_STATUS_COMPLETED);
                if (studentCourse.getCompletedTime() == null) {
                    studentCourse.setCompletedTime(now);
                }
            } else if (!"DROPPED".equalsIgnoreCase(studentCourse.getLearningStatus())) {
                studentCourse.setLearningStatus(LEARNING_STATUS_STUDYING);
                studentCourse.setCompletedTime(null);
            }
            studentCourse.setUpdatedTime(now);
            studentCourseMapper.updateById(studentCourse);

            return Result.success("学习记录保存成功");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("保存学习记录失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生某视频最新学习进度。
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param videoId 视频ID
     * @return 视频学习进度
     */
    @Override
    public VideoProgressVo getVideoProgress(Long studentId, Long courseId, Long videoId) {
        LearningRecord latestRecord = learningRecordMapper.selectLatestByStudentAndVideo(
                studentId, courseId, videoId);

        VideoProgressVo vo = new VideoProgressVo();
        vo.setVideoId(videoId);

        if (latestRecord != null) {
            vo.setVideoProgress(latestRecord.getVideoProgress());
            vo.setLastPosition(safeInt(latestRecord.getLastPosition()));
        } else {
            vo.setVideoProgress(BigDecimal.ZERO);
            vo.setLastPosition(0);
        }

        return vo;
    }

    /**
     * 分页查询学习记录列表。
     * @param query 查询条件
     * @return 学习记录分页结果
     */
    @Override
    public IPage<LearningRecordVo> getLearningRecordList(LearningRecordQuery query) {
        IPage<LearningRecordVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        return learningRecordMapper.selectLearningRecordPage((Page<LearningRecordVo>) page, query);
    }

    /**
     * 教师端查询学生学习进度监控。
     * @param query 查询条件
     * @return 学习监控结果
     */
    @Override
    public TeacherLearningMonitorVo getTeacherLearningMonitor(TeacherLearningMonitorQuery query) {
        if (query == null || query.getCourseId() == null) {
            throw new BizException("courseId不能为空");
        }

        TeacherLearningMonitorVo monitorVo = new TeacherLearningMonitorVo();
        monitorVo.setCourseId(query.getCourseId());

        List<TeacherStudentCourseVo> studentCourses = studentCourseMapper.selectTeacherMonitorStudentCourses(
                query.getCourseId(), query.getStudentId());
        if (studentCourses == null) {
            studentCourses = new ArrayList<>();
        }

        Map<String, Object> courseDetailMap = getCourseDetailMap(query.getCourseId());
        monitorVo.setCourseName(resolveCourseName(courseDetailMap, studentCourses));

        List<ChapterMeta> chapterMetaList = buildChapterMetaList(courseDetailMap, query.getChapterId());

        List<LearningRecord> latestRecords = learningRecordMapper.selectLatestMonitorRecords(
                query.getCourseId(), query.getStudentId(), query.getChapterId());
        List<Map<String, Object>> studentStudyRows = learningRecordMapper.selectStudentStudyTimeStats(
                query.getCourseId(), query.getStudentId(), query.getChapterId());
        List<Map<String, Object>> chapterStudyRows = learningRecordMapper.selectChapterStudyTimeStats(
                query.getCourseId(), query.getStudentId(), query.getChapterId());

        Map<Long, Map<Long, Map<Long, BigDecimal>>> studentChapterVideoProgress =
                buildStudentChapterVideoProgressMap(latestRecords);
        Map<Long, Integer> studentStudyTimeMap = new HashMap<>();
        Map<Long, LocalDateTime> studentLastStudyTimeMap = new HashMap<>();
        for (Map<String, Object> row : studentStudyRows) {
            Long studentId = getLongValue(row.get("studentId"));
            if (studentId == null) {
                continue;
            }
            studentStudyTimeMap.put(studentId, safeInt(getIntegerValue(row.get("totalStudyTime"))));
            studentLastStudyTimeMap.put(studentId, getLocalDateTimeValue(row.get("lastStudyTime")));
        }

        Map<Long, Integer> chapterStudyTimeMap = new HashMap<>();
        for (Map<String, Object> row : chapterStudyRows) {
            Long chapterId = getLongValue(row.get("chapterId"));
            if (chapterId == null) {
                continue;
            }
            chapterStudyTimeMap.put(chapterId, safeInt(getIntegerValue(row.get("totalStudyTime"))));
        }

        Set<Long> allStudentIds = new HashSet<>();
        for (TeacherStudentCourseVo studentCourse : studentCourses) {
            if (studentCourse.getStudentId() != null) {
                allStudentIds.add(studentCourse.getStudentId());
            }
        }
        Map<Long, Map<String, Object>> studentInfoMap = fetchStudentInfoMap(allStudentIds);

        List<TeacherLearningMonitorVo.StudentProgressItem> studentProgressList = new ArrayList<>();
        List<Long> chapterIds = new ArrayList<>();
        Map<Long, Set<Long>> chapterVideoMap = new HashMap<>();
        Map<Long, String> chapterNameMap = new HashMap<>();
        for (ChapterMeta chapterMeta : chapterMetaList) {
            chapterIds.add(chapterMeta.getChapterId());
            chapterVideoMap.put(chapterMeta.getChapterId(), chapterMeta.getVideoIds());
            chapterNameMap.put(chapterMeta.getChapterId(), chapterMeta.getChapterName());
        }

        for (TeacherStudentCourseVo studentCourse : studentCourses) {
            Long studentId = studentCourse.getStudentId();
            if (studentId == null) {
                continue;
            }

            TeacherLearningMonitorVo.StudentProgressItem item = new TeacherLearningMonitorVo.StudentProgressItem();
            item.setStudentId(studentId);
            item.setLearningStatus(studentCourse.getLearningStatus());

            Map<String, Object> studentInfo = studentInfoMap.get(studentId);
            fillStudentInfo(item, studentInfo);

            BigDecimal progressPercentage;
            Integer totalStudyTime;
            LocalDateTime lastStudyTime;

            if (query.getChapterId() == null) {
                progressPercentage = defaultBigDecimal(studentCourse.getProgressPercentage());
                totalStudyTime = safeInt(studentCourse.getTotalStudyTime());
                lastStudyTime = studentCourse.getLastStudyTime();
            } else {
                progressPercentage = calculateChapterProgress(
                        studentId,
                        query.getChapterId(),
                        studentChapterVideoProgress,
                        chapterVideoMap
                );
                totalStudyTime = studentStudyTimeMap.getOrDefault(studentId, 0);
                lastStudyTime = studentLastStudyTimeMap.get(studentId);
            }

            item.setProgressPercentage(progressPercentage);
            item.setTotalStudyTime(totalStudyTime);
            item.setLastStudyTime(lastStudyTime);

            int totalChapterCount = chapterIds.size();
            int completedChapterCount = countCompletedChapters(
                    studentId,
                    chapterIds,
                    studentChapterVideoProgress,
                    chapterVideoMap
            );
            item.setTotalChapterCount(totalChapterCount);
            item.setCompletedChapterCount(completedChapterCount);
            studentProgressList.add(item);
        }

        List<TeacherLearningMonitorVo.ChapterCompletionItem> chapterCompletionList = new ArrayList<>();
        for (Long chapterId : chapterIds) {
            TeacherLearningMonitorVo.ChapterCompletionItem chapterItem = new TeacherLearningMonitorVo.ChapterCompletionItem();
            chapterItem.setChapterId(chapterId);
            chapterItem.setChapterName(chapterNameMap.get(chapterId));
            chapterItem.setTotalStudents(studentProgressList.size());
            chapterItem.setTotalStudyTime(chapterStudyTimeMap.getOrDefault(chapterId, 0));

            int completedStudents = 0;
            BigDecimal progressSum = BigDecimal.ZERO;

            for (TeacherLearningMonitorVo.StudentProgressItem studentItem : studentProgressList) {
                BigDecimal chapterProgress = calculateChapterProgress(
                        studentItem.getStudentId(),
                        chapterId,
                        studentChapterVideoProgress,
                        chapterVideoMap
                );
                progressSum = progressSum.add(chapterProgress);
                if (chapterProgress.compareTo(CHAPTER_COMPLETE_THRESHOLD) >= 0) {
                    completedStudents++;
                }
            }

            chapterItem.setCompletedStudents(completedStudents);
            chapterItem.setCompletionRate(calculateRate(completedStudents, studentProgressList.size()));
            chapterItem.setAverageProgress(calculateAverage(progressSum, studentProgressList.size()));
            chapterCompletionList.add(chapterItem);
        }

        TeacherLearningMonitorVo.StudyDurationStatistics durationStatistics = buildDurationStatistics(studentProgressList);

        monitorVo.setTotalStudentCount(studentProgressList.size());
        monitorVo.setActiveStudentCount(countActiveStudents(studentProgressList));
        monitorVo.setStudentProgressList(studentProgressList);
        monitorVo.setChapterCompletionList(chapterCompletionList);
        monitorVo.setStudyDurationStatistics(durationStatistics);

        return monitorVo;
    }

    /**
     * 统计活跃学生数。
     * @param studentProgressList 学生进度列表
     * @return 活跃学生数
     */
    private Integer countActiveStudents(List<TeacherLearningMonitorVo.StudentProgressItem> studentProgressList) {
        int count = 0;
        for (TeacherLearningMonitorVo.StudentProgressItem item : studentProgressList) {
            if (safeInt(item.getTotalStudyTime()) > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * 构建学习时长统计。
     * @param studentProgressList 学生进度列表
     * @return 学习时长统计
     */
    private TeacherLearningMonitorVo.StudyDurationStatistics buildDurationStatistics(
            List<TeacherLearningMonitorVo.StudentProgressItem> studentProgressList) {
        TeacherLearningMonitorVo.StudyDurationStatistics statistics =
                new TeacherLearningMonitorVo.StudyDurationStatistics();
        if (studentProgressList == null || studentProgressList.isEmpty()) {
            statistics.setTotalStudyTime(0);
            statistics.setAverageStudyTime(0);
            statistics.setMaxStudyTime(0);
            statistics.setMinStudyTime(0);
            return statistics;
        }

        int total = 0;
        Integer max = null;
        Integer min = null;

        for (TeacherLearningMonitorVo.StudentProgressItem item : studentProgressList) {
            int studyTime = safeInt(item.getTotalStudyTime());
            total += studyTime;
            max = max == null ? studyTime : Math.max(max, studyTime);
            min = min == null ? studyTime : Math.min(min, studyTime);
        }

        statistics.setTotalStudyTime(total);
        statistics.setAverageStudyTime(total / studentProgressList.size());
        statistics.setMaxStudyTime(max == null ? 0 : max);
        statistics.setMinStudyTime(min == null ? 0 : min);
        return statistics;
    }

    /**
     * 计算学生已完成章节数。
     * @param studentId 学生ID
     * @param chapterIds 章节ID列表
     * @param studentChapterVideoProgress 学生章节视频进度
     * @param chapterVideoMap 章节视频映射
     * @return 已完成章节数
     */
    private int countCompletedChapters(
            Long studentId,
            List<Long> chapterIds,
            Map<Long, Map<Long, Map<Long, BigDecimal>>> studentChapterVideoProgress,
            Map<Long, Set<Long>> chapterVideoMap) {
        int completed = 0;
        for (Long chapterId : chapterIds) {
            BigDecimal progress = calculateChapterProgress(studentId, chapterId, studentChapterVideoProgress, chapterVideoMap);
            if (progress.compareTo(CHAPTER_COMPLETE_THRESHOLD) >= 0) {
                completed++;
            }
        }
        return completed;
    }

    /**
     * 计算章节进度百分比。
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @param studentChapterVideoProgress 学生章节视频进度
     * @param chapterVideoMap 章节视频映射
     * @return 章节进度百分比
     */
    private BigDecimal calculateChapterProgress(
            Long studentId,
            Long chapterId,
            Map<Long, Map<Long, Map<Long, BigDecimal>>> studentChapterVideoProgress,
            Map<Long, Set<Long>> chapterVideoMap) {
        Map<Long, Map<Long, BigDecimal>> chapterMap = studentChapterVideoProgress.get(studentId);
        Map<Long, BigDecimal> videoProgressMap = chapterMap == null ? null : chapterMap.get(chapterId);
        Set<Long> chapterVideos = chapterVideoMap.get(chapterId);

        if (chapterVideos != null && !chapterVideos.isEmpty()) {
            int completedVideos = 0;
            for (Long videoId : chapterVideos) {
                BigDecimal progress = videoProgressMap == null ? null : videoProgressMap.get(videoId);
                if (progress != null && progress.compareTo(CHAPTER_COMPLETE_THRESHOLD) >= 0) {
                    completedVideos++;
                }
            }
            return calculateRate(completedVideos, chapterVideos.size());
        }

        if (videoProgressMap == null || videoProgressMap.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int completedVideos = 0;
        for (BigDecimal progress : videoProgressMap.values()) {
            if (progress != null && progress.compareTo(CHAPTER_COMPLETE_THRESHOLD) >= 0) {
                completedVideos++;
            }
        }
        return calculateRate(completedVideos, videoProgressMap.size());
    }

    /**
     * 计算百分比。
     * @param numerator 分子
     * @param denominator 分母
     * @return 百分比
     */
    private BigDecimal calculateRate(int numerator, int denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf((double) numerator * 100 / denominator).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算平均值。
     * @param total 总和
     * @param count 数量
     * @return 平均值
     */
    private BigDecimal calculateAverage(BigDecimal total, int count) {
        if (count <= 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    /**
     * 填充学生基础信息。
     * @param item 学生进度项
     * @param studentInfo 学生信息
     */
    private void fillStudentInfo(TeacherLearningMonitorVo.StudentProgressItem item, Map<String, Object> studentInfo) {
        if (item == null || studentInfo == null) {
            return;
        }
        item.setUserId(getLongValue(studentInfo.get("userId")));
        item.setStudentNumber(getStringValue(studentInfo.get("studentNumber")));
        item.setStudentName(getStringValue(studentInfo.get("realName")));
        item.setNickname(getStringValue(studentInfo.get("nickname")));
        item.setAvatar(getStringValue(studentInfo.get("avatar")));
    }

    /**
     * 构建学生-章节-视频最新进度映射。
     * @param latestRecords 最新学习记录
     * @return 学生章节视频进度映射
     */
    private Map<Long, Map<Long, Map<Long, BigDecimal>>> buildStudentChapterVideoProgressMap(
            List<LearningRecord> latestRecords) {
        Map<Long, Map<Long, Map<Long, BigDecimal>>> result = new HashMap<>();
        if (latestRecords == null || latestRecords.isEmpty()) {
            return result;
        }

        for (LearningRecord record : latestRecords) {
            if (record.getStudentId() == null || record.getChapterId() == null || record.getVideoId() == null) {
                continue;
            }
            result
                    .computeIfAbsent(record.getStudentId(), key -> new HashMap<>())
                    .computeIfAbsent(record.getChapterId(), key -> new HashMap<>())
                    .put(record.getVideoId(), defaultBigDecimal(record.getVideoProgress()));
        }
        return result;
    }

    /**
     * 构建章节元数据。
     * @param courseDetailMap 课程详情
     * @param chapterId 章节ID（可选）
     * @return 章节元数据列表
     */
    private List<ChapterMeta> buildChapterMetaList(Map<String, Object> courseDetailMap, Long chapterId) {
        List<ChapterMeta> result = new ArrayList<>();
        List<Map<String, Object>> chapterList = extractChapterList(courseDetailMap);

        for (Map<String, Object> chapter : chapterList) {
            Long currentChapterId = getLongValue(chapter.get("id"));
            if (currentChapterId == null) {
                continue;
            }
            if (chapterId != null && !chapterId.equals(currentChapterId)) {
                continue;
            }

            ChapterMeta chapterMeta = new ChapterMeta();
            chapterMeta.setChapterId(currentChapterId);
            chapterMeta.setChapterName(getStringValue(chapter.get("chapterName")));

            Set<Long> videoIds = new HashSet<>();
            List<Map<String, Object>> videos = toMapList(chapter.get("videos"));
            for (Map<String, Object> video : videos) {
                Long videoId = getLongValue(video.get("id"));
                if (videoId != null) {
                    videoIds.add(videoId);
                }
            }
            chapterMeta.setVideoIds(videoIds);
            result.add(chapterMeta);
        }

        if (result.isEmpty() && chapterId != null) {
            ChapterMeta chapterMeta = new ChapterMeta();
            chapterMeta.setChapterId(chapterId);
            chapterMeta.setChapterName("章节" + chapterId);
            chapterMeta.setVideoIds(new HashSet<>());
            result.add(chapterMeta);
        }

        return result;
    }

    /**
     * 查询课程详情Map。
     * @param courseId 课程ID
     * @return 课程详情Map
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
     * 提取课程名称。
     * @param courseDetailMap 课程详情Map
     * @param studentCourses 选课列表
     * @return 课程名称
     */
    private String resolveCourseName(Map<String, Object> courseDetailMap, List<TeacherStudentCourseVo> studentCourses) {
        Map<String, Object> courseInfo = toMap(courseDetailMap == null ? null : courseDetailMap.get("courseInfo"));
        String courseName = getStringValue(courseInfo == null ? null : courseInfo.get("courseName"));
        if (courseName != null && !courseName.isEmpty()) {
            return courseName;
        }
        if (studentCourses != null && !studentCourses.isEmpty()) {
            return studentCourses.get(0).getCourseName();
        }
        return null;
    }

    /**
     * 计算课程学习进度（按视频完成率统计）。
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 课程进度百分比
     */
    private BigDecimal calculateCourseProgress(Long studentId, Long courseId) {
        List<LearningRecord> latestRecords = learningRecordMapper.selectLatestByStudentAndCourse(studentId, courseId);
        Map<Long, BigDecimal> videoProgressMap = buildVideoProgressMap(latestRecords);

        Set<Long> courseVideoIds = extractCourseVideoIds(getCourseDetailMap(courseId));
        if (!courseVideoIds.isEmpty()) {
            int completedVideos = 0;
            for (Long videoId : courseVideoIds) {
                BigDecimal progress = videoProgressMap.get(videoId);
                if (progress != null && progress.compareTo(CHAPTER_COMPLETE_THRESHOLD) >= 0) {
                    completedVideos++;
                }
            }
            return calculateRate(completedVideos, courseVideoIds.size());
        }

        if (videoProgressMap.isEmpty()) {
            return BigDecimal.ZERO;
        }
        int completedVideos = 0;
        for (BigDecimal progress : videoProgressMap.values()) {
            if (progress != null && progress.compareTo(CHAPTER_COMPLETE_THRESHOLD) >= 0) {
                completedVideos++;
            }
        }
        return calculateRate(completedVideos, videoProgressMap.size());
    }

    /**
     * 提取课程下全部视频ID。
     * @param courseDetailMap 课程详情
     * @return 视频ID集合
     */
    private Set<Long> extractCourseVideoIds(Map<String, Object> courseDetailMap) {
        Set<Long> videoIds = new HashSet<>();
        List<Map<String, Object>> chapterList = extractChapterList(courseDetailMap);
        for (Map<String, Object> chapter : chapterList) {
            List<Map<String, Object>> videos = toMapList(chapter.get("videos"));
            for (Map<String, Object> video : videos) {
                Long videoId = getLongValue(video.get("id"));
                if (videoId != null) {
                    videoIds.add(videoId);
                }
            }
        }
        return videoIds;
    }

    /**
     * 构建视频进度映射。
     * @param records 学习记录
     * @return 视频ID-进度映射
     */
    private Map<Long, BigDecimal> buildVideoProgressMap(List<LearningRecord> records) {
        Map<Long, BigDecimal> videoProgressMap = new HashMap<>();
        if (records == null || records.isEmpty()) {
            return videoProgressMap;
        }
        for (LearningRecord record : records) {
            if (record.getVideoId() == null) {
                continue;
            }
            BigDecimal progress = normalizeProgress(record.getVideoProgress());
            videoProgressMap.merge(record.getVideoId(), progress, BigDecimal::max);
        }
        return videoProgressMap;
    }

    /**
     * 归一化进度值到[0, 100]。
     * @param progress 原始进度
     * @return 归一化进度
     */
    private BigDecimal normalizeProgress(BigDecimal progress) {
        if (progress == null || progress.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (progress.compareTo(FULL_PROGRESS) > 0) {
            return FULL_PROGRESS;
        }
        return progress;
    }

    /**
     * 提取章节列表。
     * @param courseDetailMap 课程详情Map
     * @return 章节列表
     */
    private List<Map<String, Object>> extractChapterList(Map<String, Object> courseDetailMap) {
        if (courseDetailMap == null) {
            return new ArrayList<>();
        }
        return toMapList(courseDetailMap.get("chapters"));
    }

    /**
     * 通过OpenFeign批量查询学生信息。
     * @param studentIds 学生ID集合
     * @return 学生信息映射
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
     * 将对象安全转为Map。
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
     * 将对象安全转为List<Map>。
     * @param obj 对象
     * @return List<Map>
     */
    private List<Map<String, Object>> toMapList(Object obj) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (obj == null) {
            return result;
        }

        if (obj instanceof List<?>) {
            for (Object item : (List<?>) obj) {
                Map<String, Object> map = toMap(item);
                if (map != null) {
                    result.add(map);
                }
            }
            return result;
        }

        try {
            List<Object> list = JSONUtil.toList(JSONUtil.toJsonStr(obj), Object.class);
            for (Object item : list) {
                Map<String, Object> map = toMap(item);
                if (map != null) {
                    result.add(map);
                }
            }
        } catch (Exception ignored) {
            return new ArrayList<>();
        }

        return result;
    }

    /**
     * 安全获取字符串值。
     * @param obj 对象
     * @return 字符串
     */
    private String getStringValue(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    /**
     * 安全获取Long值。
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
     * 安全获取Integer值。
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
     * 安全获取LocalDateTime值。
     * @param obj 对象
     * @return LocalDateTime值
     */
    private LocalDateTime getLocalDateTimeValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        }
        if (obj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) obj).toLocalDateTime();
        }
        try {
            return LocalDateTime.parse(String.valueOf(obj).replace(" ", "T"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将可空整数转换为安全整数。
     * @param value 可空整数
     * @return 安全整数
     */
    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 将可空BigDecimal转换为默认值。
     * @param value 可空BigDecimal
     * @return BigDecimal值
     */
    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 章节元数据。
     */
    private static class ChapterMeta {
        private Long chapterId;
        private String chapterName;
        private Set<Long> videoIds;

        public Long getChapterId() {
            return chapterId;
        }

        public void setChapterId(Long chapterId) {
            this.chapterId = chapterId;
        }

        public String getChapterName() {
            return chapterName;
        }

        public void setChapterName(String chapterName) {
            this.chapterName = chapterName;
        }

        public Set<Long> getVideoIds() {
            return videoIds;
        }

        public void setVideoIds(Set<Long> videoIds) {
            this.videoIds = videoIds;
        }
    }
}
