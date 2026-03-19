package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.HomeworkSubmissionAnnotation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作业批注 Mapper。
 */
@Mapper
public interface HomeworkSubmissionAnnotationMapper extends BaseMapper<HomeworkSubmissionAnnotation> {
    HomeworkSubmissionAnnotation selectBySubmissionId(@Param("submissionId") Long submissionId);

    List<HomeworkSubmissionAnnotation> selectBySubmissionIds(@Param("submissionIds") List<Long> submissionIds);
}
