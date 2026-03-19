package com.xixi.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.TalentContact;
import com.xixi.exception.BizException;
import com.xixi.mapper.TalentContactMapper;
import com.xixi.pojo.dto.talent.TalentContactCreateDto;
import com.xixi.pojo.dto.talent.TalentContactPageQueryDto;
import com.xixi.pojo.dto.talent.TalentContactUpdateDto;
import com.xixi.pojo.vo.talent.TalentContactPageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 联系人管理服务。
 */
@Service
@RequiredArgsConstructor
public class TalentContactService {
    private static final String SOURCE_TYPE_MANUAL = "MANUAL";
    private static final String CONTACT_STATUS_ACTIVE = "ACTIVE";
    private static final String CONTACT_STATUS_INACTIVE = "INACTIVE";

    private final EnterpriseIdentityService enterpriseIdentityService;
    private final TalentContactMapper talentContactMapper;

    @MethodPurpose("分页查询企业联系人")
    public IPage<TalentContactPageVo> page(TalentContactPageQueryDto query, Long userId) {
        TalentContactPageQueryDto safeQuery = query == null ? new TalentContactPageQueryDto() : query;
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        IPage<Map<String, Object>> rawPage = talentContactMapper.selectContactPage(
                new Page<>(normalizePageNum(safeQuery.getPageNum()), normalizePageSize(safeQuery.getPageSize())),
                enterpriseId,
                trimToNull(safeQuery.getKeyword()),
                trimToNull(safeQuery.getStatus()),
                safeQuery.getStudentId(),
                safeQuery.getApplicationId(),
                safeQuery.getJobId(),
                trimToNull(safeQuery.getSourceType())
        );
        Page<TalentContactPageVo> targetPage = new Page<>(rawPage.getCurrent(), rawPage.getSize(), rawPage.getTotal());
        targetPage.setRecords(rawPage.getRecords().stream().map(this::toPageVo).toList());
        return targetPage;
    }

    @MethodPurpose("创建联系人")
    @Transactional(rollbackFor = Exception.class)
    public Long create(TalentContactCreateDto dto, Long userId) {
        if (dto == null || !StringUtils.hasText(dto.getName())) {
            throw new BizException(400, "联系人姓名不能为空");
        }
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        TalentContact contact = BeanUtil.copyProperties(dto, TalentContact.class);
        contact.setEnterpriseId(enterpriseId);
        contact.setSourceType(StringUtils.hasText(dto.getSourceType()) ? dto.getSourceType().trim() : SOURCE_TYPE_MANUAL);
        contact.setName(dto.getName().trim());
        contact.setStatus(normalizeContactStatus(dto.getStatus(), CONTACT_STATUS_ACTIVE));
        contact.setLatestStatus(trimToNull(dto.getLatestStatus()));
        contact.setLastContactTime(dto.getLastContactTime() == null ? LocalDateTime.now() : dto.getLastContactTime());
        contact.setCreatedTime(LocalDateTime.now());
        contact.setUpdatedTime(LocalDateTime.now());
        talentContactMapper.insert(contact);
        return contact.getId();
    }

    @MethodPurpose("更新联系人")
    @Transactional(rollbackFor = Exception.class)
    public void update(Long contactId, TalentContactUpdateDto dto, Long userId) {
        if (contactId == null || dto == null) {
            throw new BizException(400, "contactId和请求体不能为空");
        }
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        TalentContact existed = requireContact(enterpriseId, contactId);

        if (dto.getStudentId() != null) {
            existed.setStudentId(dto.getStudentId());
        }
        if (dto.getSourceType() != null) {
            existed.setSourceType(trimToNull(dto.getSourceType()));
        }
        if (dto.getApplicationId() != null) {
            existed.setApplicationId(dto.getApplicationId());
        }
        if (dto.getJobId() != null) {
            existed.setJobId(dto.getJobId());
        }
        if (dto.getName() != null) {
            existed.setName(trimToNull(dto.getName()));
        }
        if (dto.getPhone() != null) {
            existed.setPhone(trimToNull(dto.getPhone()));
        }
        if (dto.getEmail() != null) {
            existed.setEmail(trimToNull(dto.getEmail()));
        }
        if (dto.getWechat() != null) {
            existed.setWechat(trimToNull(dto.getWechat()));
        }
        if (dto.getPosition() != null) {
            existed.setPosition(trimToNull(dto.getPosition()));
        }
        if (dto.getStatus() != null) {
            existed.setStatus(normalizeContactStatus(dto.getStatus(), existed.getStatus()));
        }
        if (dto.getLatestStatus() != null) {
            existed.setLatestStatus(trimToNull(dto.getLatestStatus()));
        }
        if (dto.getLastContactTime() != null) {
            existed.setLastContactTime(dto.getLastContactTime());
        }
        if (dto.getRemark() != null) {
            existed.setRemark(trimToNull(dto.getRemark()));
        }
        existed.setUpdatedTime(LocalDateTime.now());
        talentContactMapper.updateById(existed);
    }

    @MethodPurpose("删除联系人")
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long contactId, Long userId) {
        if (contactId == null) {
            throw new BizException(400, "contactId不能为空");
        }
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        int affected = talentContactMapper.deleteByEnterpriseAndId(enterpriseId, contactId);
        if (affected <= 0) {
            throw new BizException(404, "联系人不存在");
        }
    }

    @MethodPurpose("查询并校验联系人")
    private TalentContact requireContact(Long enterpriseId, Long contactId) {
        TalentContact contact = talentContactMapper.selectByEnterpriseAndId(enterpriseId, contactId);
        if (contact == null) {
            throw new BizException(404, "联系人不存在");
        }
        return contact;
    }

    private TalentContactPageVo toPageVo(Map<String, Object> row) {
        TalentContactPageVo vo = new TalentContactPageVo();
        vo.setContactId(toLong(row.get("contactId")));
        vo.setEnterpriseId(toLong(row.get("enterpriseId")));
        vo.setStudentId(toLong(row.get("studentId")));
        vo.setSourceType(toString(row.get("sourceType")));
        vo.setApplicationId(toLong(row.get("applicationId")));
        vo.setJobId(toLong(row.get("jobId")));
        vo.setJobTitle(toString(row.get("jobTitle")));
        vo.setName(toString(row.get("name")));
        vo.setPhone(toString(row.get("phone")));
        vo.setEmail(toString(row.get("email")));
        vo.setWechat(toString(row.get("wechat")));
        vo.setPosition(toString(row.get("position")));
        vo.setStatus(normalizeContactStatus(toString(row.get("status")), CONTACT_STATUS_ACTIVE));
        vo.setLatestStatus(toString(row.get("latestStatus")));
        vo.setLatestCommunicationTime(toDateTime(row.get("latestCommunicationTime")));
        vo.setRemark(toString(row.get("remark")));
        vo.setCreatedTime(toDateTime(row.get("createdTime")));
        vo.setUpdatedTime(toDateTime(row.get("updatedTime")));
        return vo;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum <= 0 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeContactStatus(String status, String fallback) {
        if (!StringUtils.hasText(status)) {
            return StringUtils.hasText(fallback) ? fallback : CONTACT_STATUS_ACTIVE;
        }
        String normalized = status.trim().toUpperCase();
        if (CONTACT_STATUS_ACTIVE.equals(normalized) || CONTACT_STATUS_INACTIVE.equals(normalized)) {
            return normalized;
        }
        if ("有效".equals(status.trim())) {
            return CONTACT_STATUS_ACTIVE;
        }
        if ("无效".equals(status.trim())) {
            return CONTACT_STATUS_INACTIVE;
        }
        return StringUtils.hasText(fallback) ? fallback : CONTACT_STATUS_ACTIVE;
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime toDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        try {
            return LocalDateTime.parse(String.valueOf(value).replace(" ", "T"));
        } catch (Exception e) {
            return null;
        }
    }
}
