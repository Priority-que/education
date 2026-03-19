package com.xixi.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.TalentFavorite;
import com.xixi.entity.TalentTag;
import com.xixi.exception.BizException;
import com.xixi.mapper.TalentFavoriteMapper;
import com.xixi.mapper.TalentTagMapper;
import com.xixi.mq.TalentDomainEventProducer;
import com.xixi.pojo.dto.talent.TalentFavoriteCreateDto;
import com.xixi.pojo.dto.talent.TalentFavoritePageQueryDto;
import com.xixi.pojo.dto.talent.TalentFavoriteStatusUpdateDto;
import com.xixi.pojo.dto.talent.TalentFavoriteUpdateDto;
import com.xixi.pojo.dto.talent.TalentTagCreateDto;
import com.xixi.pojo.dto.talent.TalentTagUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人才收藏与标签服务。
 */
@Service
@RequiredArgsConstructor
public class TalentFavoriteService {
    private static final String STATUS_INTERESTED = "INTERESTED";
    private static final String STATUS_CONTACTED = "CONTACTED";
    private static final String STATUS_INTERVIEWED = "INTERVIEWED";
    private static final String STATUS_OFFERED = "OFFERED";
    private static final String STATUS_HIRED = "HIRED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final EnterpriseIdentityService enterpriseIdentityService;
    private final TalentFavoriteMapper talentFavoriteMapper;
    private final TalentTagMapper talentTagMapper;
    private final TalentDomainEventProducer talentDomainEventProducer;

    @MethodPurpose("创建企业人才收藏记录")
    @Transactional(rollbackFor = Exception.class)
    public Long createFavorite(TalentFavoriteCreateDto dto, Long userId) {
        if (dto == null || dto.getStudentId() == null || dto.getResumeId() == null) {
            throw new BizException(400, "resumeId不能为空");
        }
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        Long duplicated = talentFavoriteMapper.countByEnterpriseAndStudent(enterpriseId, dto.getStudentId());
        if (duplicated != null && duplicated > 0) {
            throw new BizException(409, "该候选人已在收藏库中");
        }
        Integer rating = normalizeRating(dto.getRating());

        TalentFavorite favorite = new TalentFavorite();
        favorite.setEnterpriseId(enterpriseId);
        favorite.setResumeId(dto.getResumeId());
        favorite.setStudentId(dto.getStudentId());
        favorite.setTags(toJsonTags(dto.getTags()));
        favorite.setRating(rating);
        favorite.setNotes(trimToNull(dto.getNotes()));
        favorite.setStatus(STATUS_INTERESTED);
        favorite.setCreatedTime(LocalDateTime.now());
        favorite.setUpdatedTime(LocalDateTime.now());
        talentFavoriteMapper.insert(favorite);

        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_FAVORITE_CREATED,
                enterpriseId,
                favorite.getId(),
                Map.of("status", favorite.getStatus())
        );
        return favorite.getId();
    }

    @MethodPurpose("分页查询企业收藏列表")
    public IPage<TalentFavorite> pageFavorites(TalentFavoritePageQueryDto query, Long userId) {
        TalentFavoritePageQueryDto safeQuery = query == null ? new TalentFavoritePageQueryDto() : query;
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        long pageNum = normalizePageNum(safeQuery.getPageNum());
        long pageSize = normalizePageSize(safeQuery.getPageSize());
        return talentFavoriteMapper.selectFavoritePage(
                new Page<>(pageNum, pageSize),
                enterpriseId,
                normalizeStatus(safeQuery.getStatus()),
                trimToNull(safeQuery.getTagName()),
                trimToNull(safeQuery.getKeyword())
        );
    }

    @MethodPurpose("查询企业收藏详情")
    public TalentFavorite getFavoriteDetail(Long favoriteId, Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        return requireFavorite(enterpriseId, favoriteId);
    }

    @MethodPurpose("更新收藏标签、评分与备注")
    @Transactional(rollbackFor = Exception.class)
    public void updateFavorite(Long favoriteId, TalentFavoriteUpdateDto dto, Long userId) {
        if (dto == null) {
            throw new BizException(400, "请求体不能为空");
        }
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        requireFavorite(enterpriseId, favoriteId);
        int affected = talentFavoriteMapper.updateFavoriteContent(
                favoriteId,
                enterpriseId,
                toJsonTags(dto.getTags()),
                normalizeRating(dto.getRating()),
                trimToNull(dto.getNotes()),
                LocalDateTime.now()
        );
        if (affected <= 0) {
            throw new BizException(409, "收藏更新失败");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("rating", dto.getRating());
        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_FAVORITE_UPDATED,
                enterpriseId,
                favoriteId,
                payload
        );
    }

    @MethodPurpose("更新收藏候选人跟进状态")
    @Transactional(rollbackFor = Exception.class)
    public void updateFavoriteStatus(Long favoriteId, TalentFavoriteStatusUpdateDto dto, Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        updateFavoriteStatusInternal(enterpriseId, favoriteId, dto == null ? null : dto.getStatus());
    }

    @MethodPurpose("删除企业收藏记录")
    @Transactional(rollbackFor = Exception.class)
    public void deleteFavorite(Long favoriteId, Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        requireFavorite(enterpriseId, favoriteId);
        int affected = talentFavoriteMapper.deleteByEnterpriseAndId(enterpriseId, favoriteId);
        if (affected <= 0) {
            throw new BizException(409, "收藏删除失败");
        }
        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_FAVORITE_DELETED,
                enterpriseId,
                favoriteId,
                Map.of()
        );
    }

    @MethodPurpose("查询企业标签列表")
    public List<TalentTag> listTags(Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        return talentTagMapper.selectByEnterpriseId(enterpriseId);
    }

    @MethodPurpose("创建企业人才标签")
    @Transactional(rollbackFor = Exception.class)
    public Long createTag(TalentTagCreateDto dto, Long userId) {
        if (dto == null || !StringUtils.hasText(dto.getTagName())) {
            throw new BizException(400, "tagName不能为空");
        }
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        String tagName = dto.getTagName().trim();
        Long duplicated = talentTagMapper.countByEnterpriseAndName(enterpriseId, tagName, null);
        if (duplicated != null && duplicated > 0) {
            throw new BizException(409, "标签名称已存在");
        }
        TalentTag tag = new TalentTag();
        tag.setEnterpriseId(enterpriseId);
        tag.setTagName(tagName);
        tag.setTagColor(trimToNull(dto.getTagColor()));
        tag.setDescription(trimToNull(dto.getDescription()));
        tag.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        tag.setCreatedTime(LocalDateTime.now());
        talentTagMapper.insert(tag);
        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_TAG_CHANGED,
                enterpriseId,
                tag.getId(),
                Map.of("action", "CREATE", "tagName", tagName)
        );
        return tag.getId();
    }

    @MethodPurpose("更新企业人才标签")
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(Long tagId, TalentTagUpdateDto dto, Long userId) {
        if (dto == null || !StringUtils.hasText(dto.getTagName())) {
            throw new BizException(400, "tagName不能为空");
        }
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        String tagName = dto.getTagName().trim();
        Long duplicated = talentTagMapper.countByEnterpriseAndName(enterpriseId, tagName, tagId);
        if (duplicated != null && duplicated > 0) {
            throw new BizException(409, "标签名称已存在");
        }
        int affected = talentTagMapper.updateTagByEnterprise(
                tagId,
                enterpriseId,
                tagName,
                trimToNull(dto.getTagColor()),
                trimToNull(dto.getDescription()),
                dto.getSortOrder() == null ? 0 : dto.getSortOrder()
        );
        if (affected <= 0) {
            throw new BizException(404, "标签不存在");
        }
        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_TAG_CHANGED,
                enterpriseId,
                tagId,
                Map.of("action", "UPDATE", "tagName", tagName)
        );
    }

    @MethodPurpose("删除企业人才标签")
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long tagId, Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        int affected = talentTagMapper.deleteByEnterpriseAndId(enterpriseId, tagId);
        if (affected <= 0) {
            throw new BizException(404, "标签不存在");
        }
        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_TAG_CHANGED,
                enterpriseId,
                tagId,
                Map.of("action", "DELETE")
        );
    }

    @MethodPurpose("内部接口：按企业ID同步收藏状态")
    @Transactional(rollbackFor = Exception.class)
    public void syncFavoriteStatus(Long enterpriseId, Long favoriteId, String status) {
        if (enterpriseId == null || favoriteId == null) {
            throw new BizException(400, "enterpriseId和favoriteId不能为空");
        }
        updateFavoriteStatusInternal(enterpriseId, favoriteId, status);
    }

    @MethodPurpose("执行收藏状态更新并校验状态机约束")
    private void updateFavoriteStatusInternal(Long enterpriseId, Long favoriteId, String targetStatus) {
        String normalizedTargetStatus = normalizeStatus(targetStatus);
        if (normalizedTargetStatus == null) {
            throw new BizException(400, "status不能为空");
        }
        TalentFavorite favorite = requireFavorite(enterpriseId, favoriteId);
        validateStatusTransition(favorite.getStatus(), normalizedTargetStatus);

        int affected = talentFavoriteMapper.updateFavoriteStatus(
                favoriteId,
                enterpriseId,
                normalizedTargetStatus,
                LocalDateTime.now()
        );
        if (affected <= 0) {
            throw new BizException(409, "状态更新失败");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("oldStatus", favorite.getStatus());
        payload.put("newStatus", normalizedTargetStatus);
        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_FAVORITE_STATUS_CHANGED,
                enterpriseId,
                favoriteId,
                payload
        );
    }

    @MethodPurpose("查询并校验收藏是否存在")
    private TalentFavorite requireFavorite(Long enterpriseId, Long favoriteId) {
        if (favoriteId == null) {
            throw new BizException(400, "favoriteId不能为空");
        }
        TalentFavorite favorite = talentFavoriteMapper.selectByEnterpriseAndId(enterpriseId, favoriteId);
        if (favorite == null) {
            throw new BizException(404, "收藏记录不存在");
        }
        return favorite;
    }

    @MethodPurpose("校验收藏状态流转合法性")
    private void validateStatusTransition(String fromStatus, String toStatus) {
        String normalizedFrom = normalizeStatus(fromStatus);
        String normalizedTo = normalizeStatus(toStatus);
        if (normalizedTo == null) {
            throw new BizException(400, "status不合法");
        }
        if (normalizedFrom != null && normalizedFrom.equals(normalizedTo)) {
            throw new BizException(409, "状态未发生变化");
        }
        if (STATUS_HIRED.equals(normalizedFrom) || STATUS_REJECTED.equals(normalizedFrom)) {
            throw new BizException(409, "终态不可继续流转");
        }
        if (STATUS_REJECTED.equals(normalizedTo)) {
            return;
        }
        Integer fromRank = statusRank(normalizedFrom);
        Integer toRank = statusRank(normalizedTo);
        if (fromRank == null || toRank == null) {
            throw new BizException(400, "status不合法");
        }
        if (toRank <= fromRank) {
            throw new BizException(409, "状态流转方向不合法");
        }
    }

    @MethodPurpose("获取收藏状态流转序位")
    private Integer statusRank(String status) {
        if (STATUS_INTERESTED.equals(status)) {
            return 1;
        }
        if (STATUS_CONTACTED.equals(status)) {
            return 2;
        }
        if (STATUS_INTERVIEWED.equals(status)) {
            return 3;
        }
        if (STATUS_OFFERED.equals(status)) {
            return 4;
        }
        if (STATUS_HIRED.equals(status)) {
            return 5;
        }
        return null;
    }

    @MethodPurpose("归一化收藏状态字符串")
    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (STATUS_INTERESTED.equals(normalized)
                || STATUS_CONTACTED.equals(normalized)
                || STATUS_INTERVIEWED.equals(normalized)
                || STATUS_OFFERED.equals(normalized)
                || STATUS_HIRED.equals(normalized)
                || STATUS_REJECTED.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    @MethodPurpose("归一化评分参数")
    private Integer normalizeRating(Integer rating) {
        if (rating == null) {
            return 0;
        }
        if (rating < 0 || rating > 5) {
            throw new BizException(400, "rating必须在0-5之间");
        }
        return rating;
    }

    @MethodPurpose("列表标签转JSON字符串")
    private String toJsonTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return JSONUtil.toJsonStr(tags);
    }

    @MethodPurpose("归一化页码参数")
    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum <= 0 ? 1 : pageNum;
    }

    @MethodPurpose("归一化分页大小参数")
    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 200);
    }

    @MethodPurpose("字符串去空并返回null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
