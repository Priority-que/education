package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseMaterialDto;
import com.xixi.pojo.query.CourseMaterialQuery;
import com.xixi.pojo.vo.CourseMaterialVo;
import com.xixi.web.Result;

import java.util.List;

/**
 * 课程资料业务接口
 */
public interface CourseMaterialService {

    /**
     * 根据ID查询资料
     */
    CourseMaterialVo getMaterialById(Long id);

    /**
     * 分页查询资料列表
     */
    IPage<CourseMaterialVo> getMaterialList(CourseMaterialQuery query);

    /**
     * 新增资料
     */
    Result addMaterial(CourseMaterialDto dto);

    /**
     * 更新资料
     */
    Result updateMaterial(CourseMaterialDto dto);

    /**
     * 批量删除资料
     */
    Result deleteMaterial(List<Long> ids);
}
