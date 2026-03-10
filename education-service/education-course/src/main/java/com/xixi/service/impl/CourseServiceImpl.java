package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.constant.PageConstant;
import com.xixi.entity.Course;
import com.xixi.entity.CourseAccess;
import com.xixi.entity.CourseCategory;
import com.xixi.exception.BizException;
import com.xixi.mapper.CourseAccessMapper;
import com.xixi.mapper.CourseCategoryMapper;
import com.xixi.mapper.CourseChapterMapper;
import com.xixi.mapper.CourseMapper;
import com.xixi.mapper.CourseMaterialMapper;
import com.xixi.mapper.CourseVideoMapper;
import com.xixi.openfeign.user.EducationUserTeacherClient;
import com.xixi.pojo.dto.CourseAccessVerifyDto;
import com.xixi.pojo.dto.CourseDto;
import com.xixi.pojo.query.CourseQuery;
import com.xixi.pojo.vo.CourseChapterVo;
import com.xixi.pojo.vo.CourseDetailVo;
import com.xixi.pojo.vo.CourseMaterialVo;
import com.xixi.pojo.vo.CourseVideoVo;
import com.xixi.pojo.vo.CourseVo;
import com.xixi.service.CourseService;
import com.xixi.web.Result;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class CourseServiceImpl
implements CourseService {
    private static final String ACCESS_FREE = "FREE";
    private static final String ACCESS_PAID = "PAID";
    private static final String ACCESS_PASSWORD = "PASSWORD";
    private static final String ACCESS_PAID_AND_PASSWORD = "PAID_AND_PASSWORD";
    private static final BigDecimal MIN_RATING = BigDecimal.ZERO;
    private static final BigDecimal MAX_RATING = new BigDecimal("5");
    private final CourseMapper courseMapper;
    private final CourseAccessMapper courseAccessMapper;
    private final CourseCategoryMapper courseCategoryMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final CourseVideoMapper courseVideoMapper;
    private final CourseMaterialMapper courseMaterialMapper;
    private final EducationUserTeacherClient educationUserTeacherClient;

    @Override
    public IPage<CourseVo> getCourseList(CourseQuery courseQuery) {
        if (courseQuery == null) {
            courseQuery = new CourseQuery();
        }
        Page page = new Page((long)(courseQuery.getPageNum() != null ? courseQuery.getPageNum() : PageConstant.PAGE_NUM).intValue(), (long)(courseQuery.getPageSize() != null ? courseQuery.getPageSize() : PageConstant.PAGE_SIZE).intValue());
        Page<CourseVo> resultPage = this.courseMapper.selectCoursePage((Page<CourseVo>)page, courseQuery);
        List records = resultPage.getRecords();
        this.fillCourseTeacherName(records);
        this.fillCourseAccessInfo(records);
        return resultPage;
    }

    @Override
    public IPage<CourseVo> getTeacherCourseList(CourseQuery courseQuery, Long operatorUserId) {
        if (courseQuery == null) {
            courseQuery = new CourseQuery();
        }
        if (operatorUserId == null) {
            throw new BizException(Integer.valueOf(401), "未登录或用户ID缺失");
        }
        Long teacherId = this.findTeacherIdByUserId(operatorUserId);
        if (teacherId == null) {
            throw new BizException(Integer.valueOf(404), "当前用户未绑定教师信息");
        }
        courseQuery.setTeacherId(teacherId);
        return this.getCourseList(courseQuery);
    }

    @Override
    public Result addCourse(CourseDto courseDto, Long operatorUserId) {
        if (courseDto == null) {
            return Result.error((String)"课程信息不能为空");
        }
        Long teacherId = this.resolveTeacherIdForCreate(courseDto, operatorUserId);
        if (teacherId == null) {
            return Result.error((String)"无法解析教师ID或教师名称");
        }
        Long categoryId = this.resolveCategoryIdForCreate(courseDto);
        if (categoryId == null) {
            return Result.error((String)"无法解析分类ID或分类名称");
        }
        try {
            Course course = (Course)BeanUtil.copyProperties((Object)courseDto, Course.class, (String[])new String[0]);
            course.setTeacherId(teacherId);
            course.setCategoryId(categoryId);
            course.setCreatedTime(LocalDateTime.now());
            course.setUpdatedTime(LocalDateTime.now());
            if (course.getCurrentStudents() == null) {
                course.setCurrentStudents(0);
            }
            if (course.getViewCount() == null) {
                course.setViewCount(0);
            }
            if (course.getLikeCount() == null) {
                course.setLikeCount(0);
            }
            if (course.getRating() == null) {
                course.setRating(BigDecimal.ZERO);
            }
            if (course.getRatingCount() == null) {
                course.setRatingCount(0);
            }
            if (course.getPublishedTime() == null) {
                course.setPublishedTime(LocalDateTime.now());
            }
            if (course.getStatus() == null) {
                course.setStatus("DRAFT");
            }
            this.courseMapper.insert(course);
            this.saveOrUpdateCourseAccess(course.getId(), course, courseDto);
            return Result.success((String)"添加课程成功");
        }
        catch (Exception e) {
            return Result.error((String)("添加课程失败: " + e.getMessage()));
        }
    }

    @Override
    public Result updateCourse(CourseDto courseDto, Long operatorUserId) {
        if (courseDto == null || courseDto.getId() == null) {
            return Result.error((String)"课程ID不能为空");
        }
        Course existed = (Course)this.courseMapper.selectById(courseDto.getId());
        if (existed == null) {
            return Result.error((String)"课程不存在");
        }
        Long teacherId = this.resolveTeacherIdForUpdate(courseDto, existed, operatorUserId);
        if (teacherId == null) {
            return Result.error((String)"无法解析教师ID或教师名称");
        }
        Long categoryId = this.resolveCategoryIdForUpdate(courseDto, existed);
        if (categoryId == null) {
            return Result.error((String)"无法解析分类ID或分类名称");
        }
        try {
            BeanUtil.copyProperties((Object)courseDto, (Object)existed, (CopyOptions)CopyOptions.create().ignoreNullValue());
            existed.setTeacherId(teacherId);
            existed.setCategoryId(categoryId);
            existed.setUpdatedTime(LocalDateTime.now());
            this.courseMapper.updateById(existed);
            this.saveOrUpdateCourseAccess(existed.getId(), existed, courseDto);
            return Result.success((String)"修改课程成功");
        }
        catch (Exception e) {
            return Result.error((String)("修改课程失败: " + e.getMessage()));
        }
    }

    @Override
    public Result deleteCourse(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.error((String)"请选择要删除的课程");
        }
        try {
            this.courseMapper.deleteByIds(ids);
            return Result.success((String)"删除课程成功");
        }
        catch (Exception e) {
            return Result.error((String)"删除课程失败");
        }
    }

    @Override
    public CourseVo getCourseById(Long id) {
        CourseVo courseVo = this.courseMapper.getCourseById(id);
        if (courseVo == null) {
            return null;
        }
        this.fillCourseTeacherName(List.of(courseVo));
        this.fillCourseAccessInfo(List.of(courseVo));
        return courseVo;
    }

    @Override
    public CourseDetailVo getCourseDetail(Long id) {
        CourseVo courseVo = this.getCourseById(id);
        if (courseVo == null) {
            return null;
        }
        CourseDetailVo.CourseBaseInfo courseInfo = new CourseDetailVo.CourseBaseInfo();
        BeanUtil.copyProperties((Object)courseVo, (Object)courseInfo, (String[])new String[0]);
        List<CourseChapterVo> chapters = this.courseChapterMapper.selectChaptersByCourseId(id);
        ArrayList<CourseDetailVo.ChapterDetail> chapterDetails = new ArrayList<CourseDetailVo.ChapterDetail>();
        if (!CollectionUtils.isEmpty(chapters)) {
            List<Long> chapterIds = chapters.stream().map(CourseChapterVo::getId).collect(Collectors.toList());
            List<CourseVideoVo> allVideos = this.courseVideoMapper.selectVideosByChapterIds(chapterIds);
            Map<Long, List<CourseVideoVo>> videosByChapter = allVideos.stream().collect(Collectors.groupingBy(CourseVideoVo::getChapterId));
            List<CourseMaterialVo> allMaterials = this.courseMaterialMapper.selectMaterialsByChapterIds(chapterIds);
            Map<Long, List<CourseMaterialVo>> materialsByChapter = allMaterials.stream().collect(Collectors.groupingBy(CourseMaterialVo::getChapterId));
            for (CourseChapterVo chapter : chapters) {
                CourseDetailVo.ChapterDetail chapterDetail = new CourseDetailVo.ChapterDetail();
                BeanUtil.copyProperties((Object)chapter, (Object)chapterDetail, (String[])new String[0]);
                List<CourseVideoVo> videos = videosByChapter.getOrDefault(chapter.getId(), new ArrayList<>());
                videos.sort((v1, v2) -> {
                    Integer sort1 = v1.getSortOrder() != null ? v1.getSortOrder() : 0;
                    Integer sort2 = v2.getSortOrder() != null ? v2.getSortOrder() : 0;
                    return sort1.compareTo(sort2);
                });
                List<CourseDetailVo.VideoInfo> videoInfos = videos.stream().map(video -> {
                    CourseDetailVo.VideoInfo videoInfo = new CourseDetailVo.VideoInfo();
                    BeanUtil.copyProperties((Object)video, (Object)videoInfo, (String[])new String[0]);
                    return videoInfo;
                }).collect(Collectors.toList());
                chapterDetail.setVideos(videoInfos);
                List<CourseMaterialVo> materials = materialsByChapter.getOrDefault(chapter.getId(), new ArrayList<>());
                materials.sort((m1, m2) -> {
                    Integer sort1 = m1.getSortOrder() != null ? m1.getSortOrder() : 0;
                    Integer sort2 = m2.getSortOrder() != null ? m2.getSortOrder() : 0;
                    return sort1.compareTo(sort2);
                });
                List<CourseDetailVo.MaterialInfo> materialInfos = materials.stream().map(material -> {
                    CourseDetailVo.MaterialInfo materialInfo = new CourseDetailVo.MaterialInfo();
                    BeanUtil.copyProperties((Object)material, (Object)materialInfo, (String[])new String[0]);
                    return materialInfo;
                }).collect(Collectors.toList());
                chapterDetail.setMaterials(materialInfos);
                chapterDetails.add(chapterDetail);
            }
        }
        CourseDetailVo courseDetailVo = new CourseDetailVo();
        courseDetailVo.setCourseInfo(courseInfo);
        courseDetailVo.setChapters(chapterDetails);
        return courseDetailVo;
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public Result updateCourseCurrentStudentNumber(Long id, Integer status) {
        if (id == null) {
            throw new BizException("课程ID不能为空");
        }
        if (this.courseMapper.selectById(id) == null) {
            throw new BizException("课程不存在");
        }
        if (status == null || status != 1 && status != 0) {
            throw new BizException("status参数非法，只允许1(增加)或0(减少)");
        }
        if (status == 1) {
            int affectedRows = this.courseMapper.addStudentNumber(id);
            if (affectedRows <= 0) {
                throw new BizException("课程人数更新失败：课程已满");
            }
        } else {
            int affectedRows = this.courseMapper.reduceStudentNumber(id);
            if (affectedRows <= 0) {
                throw new BizException("课程人数更新失败：当前人数已为0");
            }
        }
        return Result.success((String)"更新成功");
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public Result updateCourseViewCount(Long id, Integer status) {
        if (id == null) {
            throw new BizException("courseId cannot be null");
        }
        if (this.courseMapper.selectById(id) == null) {
            throw new BizException("course not found");
        }
        if (status == null || status != 1 && status != 0) {
            throw new BizException("status must be 1(add) or 0(reduce)");
        }
        if (status == 1) {
            int affectedRows = this.courseMapper.addViewCount(id);
            if (affectedRows <= 0) {
                throw new BizException("failed to increase course view count");
            }
        } else {
            int affectedRows = this.courseMapper.reduceViewCount(id);
            if (affectedRows <= 0) {
                throw new BizException("failed to reduce course view count: already zero");
            }
        }
        return Result.success((String)"update success");
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public Result updateCourseLikeCount(Long id, Integer status) {
        if (id == null) {
            throw new BizException("courseId cannot be null");
        }
        if (this.courseMapper.selectById(id) == null) {
            throw new BizException("course not found");
        }
        if (status == null || status != 1 && status != 0) {
            throw new BizException("status must be 1(add) or 0(reduce)");
        }
        if (status == 1) {
            int affectedRows = this.courseMapper.addLikeCount(id);
            if (affectedRows <= 0) {
                throw new BizException("failed to increase course like count");
            }
        } else {
            int affectedRows = this.courseMapper.reduceLikeCount(id);
            if (affectedRows <= 0) {
                throw new BizException("failed to reduce course like count: already zero");
            }
        }
        return Result.success((String)"update success");
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public Result updateCourseRating(Long id, BigDecimal rating) {
        if (id == null) {
            throw new BizException("courseId cannot be null");
        }
        if (this.courseMapper.selectById(id) == null) {
            throw new BizException("course not found");
        }
        if (rating == null) {
            throw new BizException("rating cannot be null");
        }
        BigDecimal normalizedRating = rating.setScale(2, RoundingMode.HALF_UP);
        if (normalizedRating.compareTo(MIN_RATING) < 0 || normalizedRating.compareTo(MAX_RATING) > 0) {
            throw new BizException("rating must be between 0 and 5");
        }
        int affectedRows = this.courseMapper.addCourseRating(id, normalizedRating);
        if (affectedRows <= 0) {
            throw new BizException("failed to update course rating");
        }
        return Result.success((String)"update success");
    }

    @Override
    public Result verifyAccess(CourseAccessVerifyDto dto) {
        if (dto == null || dto.getCourseId() == null) {
            return Result.error((Integer)400, (String)"courseId不能为空");
        }
        Course course = (Course)this.courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            return Result.error((Integer)404, (String)"课程不存在");
        }
        CourseAccess access = this.courseAccessMapper.selectByCourseId(dto.getCourseId());
        String accessType = this.resolveAccessType(access, course);
        boolean passwordRequired = this.isPasswordRequired(accessType);
        if (!passwordRequired) {
            return Result.success((String)"验证通过", Map.of("pass", Boolean.TRUE));
        }
        if (!StringUtils.hasText((String)dto.getPassword())) {
            return Result.success((String)"需要输入访问密码", Map.of("pass", Boolean.FALSE));
        }
        if (access == null || !StringUtils.hasText((String)access.getPasswordHash())) {
            return Result.success((String)"课程未设置访问密码", Map.of("pass", Boolean.FALSE));
        }
        boolean pass = DigestUtil.sha256Hex((String)dto.getPassword().trim()).equals(access.getPasswordHash());
        if (pass) {
            return Result.success((String)"验证通过", Map.of("pass", Boolean.TRUE));
        }
        return Result.success((String)"密码错误", Map.of("pass", Boolean.FALSE));
    }

    private void fillCourseAccessInfo(List<CourseVo> courseList) {
        if (CollectionUtils.isEmpty(courseList)) {
            return;
        }
        List<Long> courseIds = courseList.stream().map(CourseVo::getId).filter(id -> id != null && id > 0L).distinct().collect(Collectors.toList());
        if (courseIds.isEmpty()) {
            return;
        }
        List<CourseAccess> accessList = this.courseAccessMapper.selectByCourseIds(courseIds);
        HashMap<Long, CourseAccess> accessMap = new HashMap<Long, CourseAccess>();
        if (accessList != null) {
            for (CourseAccess access : accessList) {
                if (access == null || access.getCourseId() == null) continue;
                accessMap.put(access.getCourseId(), access);
            }
        }
        for (CourseVo vo : courseList) {
            if (vo.getId() == null) continue;
            CourseAccess access = (CourseAccess)accessMap.get(vo.getId());
            String accessType = this.resolveAccessType(access, vo.getIsFree());
            vo.setAccessType(accessType);
            vo.setPasswordHint(access == null ? null : access.getPasswordHint());
            vo.setPasswordRequired(this.isPasswordRequired(accessType));
        }
    }

    private void fillCourseTeacherName(List<CourseVo> courseList) {
        if (CollectionUtils.isEmpty(courseList)) {
            return;
        }
        HashMap<Long, String> teacherNameCache = new HashMap<Long, String>();
        for (CourseVo vo : courseList) {
            if (vo == null || vo.getTeacherId() == null) continue;
            Long teacherId = vo.getTeacherId();
            String teacherName = teacherNameCache.computeIfAbsent(teacherId, this::resolveTeacherName);
            vo.setTeacherName(teacherName);
        }
    }

    private void saveOrUpdateCourseAccess(Long courseId, Course course, CourseDto dto) {
        String requestedAccessType;
        String accessType;
        if (courseId == null) {
            return;
        }
        CourseAccess access = this.courseAccessMapper.selectByCourseId(courseId);
        if (access == null) {
            access = new CourseAccess();
            access.setCourseId(courseId);
            access.setCreatedTime(LocalDateTime.now());
        }
        if ((accessType = this.normalizeAccessType(requestedAccessType = this.trimToNull(dto.getAccessType()))) == null) {
            accessType = this.resolveDefaultAccessType(course.getIsFree());
        }
        access.setAccessType(accessType);
        if (StringUtils.hasText((String)dto.getCoursePassword())) {
            access.setPasswordHash(DigestUtil.sha256Hex((String)dto.getCoursePassword().trim()));
        } else if (this.isPasswordRequired(accessType) && !StringUtils.hasText((String)access.getPasswordHash())) {
            throw new BizException("需要设置访问密码但未提供coursePassword参数");
        }
        String passwordHint = this.trimToNull(dto.getPasswordHint());
        if (passwordHint != null) {
            access.setPasswordHint(passwordHint);
        }
        access.setUpdatedTime(LocalDateTime.now());
        if (access.getId() == null) {
            this.courseAccessMapper.insert(access);
        } else {
            this.courseAccessMapper.updateById(access);
        }
    }

    private String resolveAccessType(CourseAccess access, Course course) {
        return this.resolveAccessType(access, course == null ? null : course.getIsFree());
    }

    private String resolveAccessType(CourseAccess access, Boolean isFree) {
        String accessType = access == null ? null : this.normalizeAccessType(access.getAccessType());
        return accessType != null ? accessType : this.resolveDefaultAccessType(isFree);
    }

    private String resolveDefaultAccessType(Boolean isFree) {
        return Boolean.TRUE.equals(isFree) ? ACCESS_FREE : ACCESS_PAID;
    }

    private String normalizeAccessType(String accessType) {
        if (!StringUtils.hasText((String)accessType)) {
            return null;
        }
        String normalized = accessType.trim().toUpperCase();
        if (ACCESS_FREE.equals(normalized) || ACCESS_PAID.equals(normalized) || ACCESS_PASSWORD.equals(normalized) || ACCESS_PAID_AND_PASSWORD.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private boolean isPasswordRequired(String accessType) {
        return ACCESS_PASSWORD.equals(accessType) || ACCESS_PAID_AND_PASSWORD.equals(accessType);
    }

    private Long resolveTeacherIdForCreate(CourseDto dto, Long operatorUserId) {
        Long operatorTeacherId = this.findTeacherIdByUserId(operatorUserId);
        if (operatorTeacherId != null) {
            return operatorTeacherId;
        }
        if (dto.getTeacherId() != null) {
            return this.teacherExists(dto.getTeacherId()) ? dto.getTeacherId() : null;
        }
        if (StringUtils.hasText((String)dto.getTeacherName())) {
            return this.findTeacherIdByName(dto.getTeacherName());
        }
        return null;
    }

    private Long resolveTeacherIdForUpdate(CourseDto dto, Course existed, Long operatorUserId) {
        Long operatorTeacherId = this.findTeacherIdByUserId(operatorUserId);
        if (operatorTeacherId != null) {
            return operatorTeacherId;
        }
        if (dto.getTeacherId() != null) {
            return this.teacherExists(dto.getTeacherId()) ? dto.getTeacherId() : null;
        }
        if (StringUtils.hasText((String)dto.getTeacherName())) {
            return this.findTeacherIdByName(dto.getTeacherName());
        }
        return existed.getTeacherId();
    }

    private Long resolveCategoryIdForCreate(CourseDto dto) {
        if (dto.getCategoryId() != null) {
            return this.categoryExists(dto.getCategoryId()) ? dto.getCategoryId() : null;
        }
        if (StringUtils.hasText((String)dto.getCategoryName())) {
            return this.courseCategoryMapper.getIdByName(dto.getCategoryName());
        }
        return null;
    }

    private Long resolveCategoryIdForUpdate(CourseDto dto, Course existed) {
        if (dto.getCategoryId() != null) {
            return this.categoryExists(dto.getCategoryId()) ? dto.getCategoryId() : null;
        }
        if (StringUtils.hasText((String)dto.getCategoryName())) {
            return this.courseCategoryMapper.getIdByName(dto.getCategoryName());
        }
        return existed.getCategoryId();
    }

    private Long findTeacherIdByName(String teacherName) {
        Result remoteResult = this.educationUserTeacherClient.getTeachersIdByName(teacherName);
        return this.parseLongData(remoteResult);
    }

    private Long findTeacherIdByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        Result remoteResult = this.educationUserTeacherClient.getTeacherIdByUserId(userId);
        return this.parseLongData(remoteResult);
    }

    private String resolveTeacherName(Long teacherIdOrUserId) {
        String teacherName = this.findTeacherNameById(teacherIdOrUserId);
        if (StringUtils.hasText((String)teacherName)) {
            return teacherName;
        }
        Long teacherId = this.findTeacherIdByUserId(teacherIdOrUserId);
        if (teacherId == null || teacherId.equals(teacherIdOrUserId)) {
            return null;
        }
        return this.findTeacherNameById(teacherId);
    }

    private String findTeacherNameById(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        Result remoteResult = this.educationUserTeacherClient.getTeachersNameById(teacherId);
        return this.trimToNull(this.extractStringData(remoteResult));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText((String)value)) {
            return null;
        }
        return value.trim();
    }

    private boolean teacherExists(Long teacherId) {
        return StringUtils.hasText((String)this.findTeacherNameById(teacherId));
    }

    private boolean categoryExists(Long categoryId) {
        CourseCategory category = (CourseCategory)this.courseCategoryMapper.selectById(categoryId);
        return category != null;
    }

    private Long parseLongData(Result result) {
        if (result == null || result.getData() == null) {
            return null;
        }
        Object data = result.getData();
        if (data instanceof Long) {
            Long value = (Long)data;
            return value;
        }
        if (data instanceof Integer) {
            Integer value = (Integer)data;
            return value.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(data));
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String extractStringData(Result result) {
        if (result == null || result.getData() == null) {
            return null;
        }
        return String.valueOf(result.getData());
    }

    public CourseServiceImpl(CourseMapper courseMapper, CourseAccessMapper courseAccessMapper, CourseCategoryMapper courseCategoryMapper, CourseChapterMapper courseChapterMapper, CourseVideoMapper courseVideoMapper, CourseMaterialMapper courseMaterialMapper, EducationUserTeacherClient educationUserTeacherClient) {
        this.courseMapper = courseMapper;
        this.courseAccessMapper = courseAccessMapper;
        this.courseCategoryMapper = courseCategoryMapper;
        this.courseChapterMapper = courseChapterMapper;
        this.courseVideoMapper = courseVideoMapper;
        this.courseMaterialMapper = courseMaterialMapper;
        this.educationUserTeacherClient = educationUserTeacherClient;
    }
}
