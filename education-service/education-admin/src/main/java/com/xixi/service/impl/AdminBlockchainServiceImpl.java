package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.BlockchainNode;
import com.xixi.entity.OperationLog;
import com.xixi.exception.BizException;
import com.xixi.mapper.BlockchainNodeMapper;
import com.xixi.mapper.OperationLogMapper;
import com.xixi.mq.AdminDomainEventProducer;
import com.xixi.openfeign.certificate.EducationCertificateAdminClient;
import com.xixi.pojo.dto.admin.AdminBlockchainBatchAnchorDto;
import com.xixi.pojo.dto.admin.BlockchainNodeStatusUpdateDto;
import com.xixi.pojo.query.admin.BlockchainCertificatePageQuery;
import com.xixi.pojo.query.admin.BlockchainNodePageQuery;
import com.xixi.pojo.vo.admin.BlockchainCertificatePageVo;
import com.xixi.pojo.vo.admin.BlockchainNodeVo;
import com.xixi.pojo.vo.admin.BlockchainStatusVo;
import com.xixi.service.AdminBlockchainService;
import com.xixi.service.support.AdminOperationLogger;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * 区块链管理编排服务实现。
 */
@Service
@RequiredArgsConstructor
public class AdminBlockchainServiceImpl implements AdminBlockchainService {
    private static final String BIZ_TYPE_BLOCKCHAIN = "BLOCKCHAIN";
    private static final String NODE_STATUS_ONLINE = "ONLINE";
    private static final String NODE_STATUS_OFFLINE = "OFFLINE";
    private static final String NODE_STATUS_SYNCING = "SYNCING";

    private final OperationLogMapper operationLogMapper;
    private final BlockchainNodeMapper blockchainNodeMapper;
    private final EducationCertificateAdminClient educationCertificateAdminClient;
    private final AdminDomainEventProducer adminDomainEventProducer;
    private final AdminOperationLogger adminOperationLogger;

    @Override
    @MethodPurpose("分页查询区块链证书记录（代理证书服务）")
    public IPage<BlockchainCertificatePageVo> getBlockchainCertificatePage(BlockchainCertificatePageQuery query, Long adminId) {
        Long validAdminId = requireAdmin(adminId);
        BlockchainCertificatePageQuery safeQuery = query == null ? new BlockchainCertificatePageQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        Result result = educationCertificateAdminClient.getAdminBlockchainCertificatePage(
                pageNum,
                pageSize,
                trimToNull(safeQuery.getCertificateNumber()),
                normalizeUpper(safeQuery.getStatus()),
                safeQuery.getBlockHeight(),
                String.valueOf(validAdminId),
                "1"
        );
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            throw new BizException(result == null ? 500 : result.getCode(), result == null ? "证书服务返回为空" : result.getMessage());
        }
        return parseCertificatePage(result.getData(), pageNum, pageSize);
    }

    @Override
    @MethodPurpose("查询单证书区块链详情（代理证书服务）")
    public Object getBlockchainCertificateDetail(Long certificateId, Long adminId) {
        Long validAdminId = requireAdmin(adminId);
        if (certificateId == null) {
            throw new BizException(400, "certificateId不能为空");
        }
        Result result = educationCertificateAdminClient.getCertificateBlockchainDetail(
                certificateId,
                String.valueOf(validAdminId),
                "1"
        );
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            throw new BizException(result == null ? 500 : result.getCode(), result == null ? "证书服务返回为空" : result.getMessage());
        }
        return result.getData();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("触发单证书上链（代理证书服务）")
    public Result anchorCertificate(Long certificateId, Long adminId) {
        Long validAdminId = requireAdmin(adminId);
        if (certificateId == null) {
            throw new BizException(400, "certificateId不能为空");
        }
        Result result = educationCertificateAdminClient.anchorCertificate(certificateId, String.valueOf(validAdminId), "1");
        boolean success = result != null && result.getCode() != null && result.getCode() == 200;
        adminOperationLogger.log(
                validAdminId,
                "管理员" + validAdminId,
                "ADMIN",
                "BLOCKCHAIN_ANCHOR",
                "触发单证书上链",
                "POST",
                "/admin/blockchain/certificate/anchor/" + certificateId,
                null,
                JSONUtil.toJsonStr(result),
                null,
                null,
                0,
                success,
                success ? null : (result == null ? "证书服务返回为空" : result.getMessage())
        );
        if (!success) {
            throw new BizException(result == null ? 500 : result.getCode(), result == null ? "证书服务返回为空" : result.getMessage());
        }
        adminDomainEventProducer.publish(
                "ANCHOR",
                BIZ_TYPE_BLOCKCHAIN,
                certificateId,
                JSONUtil.toJsonStr(result.getData()),
                validAdminId
        );
        return Result.success("上链触发成功", result.getData());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("触发批量证书上链（代理证书服务）")
    public Result anchorBatchCertificates(AdminBlockchainBatchAnchorDto dto, Long adminId) {
        Long validAdminId = requireAdmin(adminId);
        if (dto == null || dto.getCertificateIds() == null || dto.getCertificateIds().isEmpty()) {
            throw new BizException(400, "certificateIds不能为空");
        }
        Result result = educationCertificateAdminClient.anchorBatchCertificates(dto, String.valueOf(validAdminId), "1");
        boolean success = result != null && result.getCode() != null && result.getCode() == 200;
        adminOperationLogger.log(
                validAdminId,
                "管理员" + validAdminId,
                "ADMIN",
                "BLOCKCHAIN_ANCHOR_BATCH",
                "触发批量证书上链",
                "POST",
                "/admin/blockchain/certificate/anchor/batch",
                JSONUtil.toJsonStr(dto),
                JSONUtil.toJsonStr(result),
                null,
                null,
                0,
                success,
                success ? null : (result == null ? "证书服务返回为空" : result.getMessage())
        );
        if (!success) {
            throw new BizException(result == null ? 500 : result.getCode(), result == null ? "证书服务返回为空" : result.getMessage());
        }
        adminDomainEventProducer.publish(
                "ANCHOR_BATCH",
                BIZ_TYPE_BLOCKCHAIN,
                null,
                JSONUtil.toJsonStr(result.getData()),
                validAdminId
        );
        return Result.success("批量上链触发成功", result.getData());
    }

    @Override
    @MethodPurpose("查询区块链状态")
    public BlockchainStatusVo getBlockchainStatus() {
        BlockchainStatusVo vo = new BlockchainStatusVo();
        int singleCount = nullToZero(operationLogMapper.countByOperationType("BLOCKCHAIN_ANCHOR"));
        int batchCount = nullToZero(operationLogMapper.countByOperationType("BLOCKCHAIN_ANCHOR_BATCH"));
        int singleSuccess = nullToZero(operationLogMapper.countByOperationTypeAndStatus("BLOCKCHAIN_ANCHOR", 1));
        int batchSuccess = nullToZero(operationLogMapper.countByOperationTypeAndStatus("BLOCKCHAIN_ANCHOR_BATCH", 1));
        vo.setTotalAttempts(singleCount + batchCount);
        vo.setSuccessCount(singleSuccess + batchSuccess);
        vo.setFailedCount(vo.getTotalAttempts() - vo.getSuccessCount());

        OperationLog latestSuccess = operationLogMapper.selectLatestSuccessBlockchainLog();
        vo.setLatestBlockHeight(extractLatestBlockHeight(latestSuccess == null ? null : latestSuccess.getResponseResult()));
        return vo;
    }

    @Override
    @MethodPurpose("分页查询区块链节点")
    public IPage<BlockchainNodeVo> getBlockchainNodePage(BlockchainNodePageQuery query, Long adminId) {
        requireAdmin(adminId);
        BlockchainNodePageQuery safeQuery = query == null ? new BlockchainNodePageQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        return blockchainNodeMapper.selectNodePage(
                new Page<>(pageNum, pageSize),
                trimToNull(safeQuery.getKeyword()),
                normalizeNodeStatus(safeQuery.getStatus(), false)
        );
    }

    @Override
    @MethodPurpose("查询区块链节点详情")
    public BlockchainNodeVo getBlockchainNodeDetail(Long nodeId, Long adminId) {
        requireAdmin(adminId);
        if (nodeId == null) {
            throw new BizException(400, "nodeId不能为空");
        }
        BlockchainNodeVo vo = blockchainNodeMapper.selectNodeDetail(nodeId);
        if (vo == null) {
            throw new BizException(404, "节点不存在");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("更新区块链节点状态")
    public Result updateBlockchainNodeStatus(Long nodeId, BlockchainNodeStatusUpdateDto dto, Long adminId) {
        Long validAdminId = requireAdmin(adminId);
        if (nodeId == null) {
            throw new BizException(400, "nodeId不能为空");
        }
        if (dto == null) {
            throw new BizException(400, "请求体不能为空");
        }
        String targetStatus = normalizeNodeStatus(dto.getStatus(), true);
        BlockchainNode node = blockchainNodeMapper.selectById(nodeId);
        if (node == null) {
            throw new BizException(404, "节点不存在");
        }
        if (targetStatus.equalsIgnoreCase(node.getStatus())) {
            return Result.success("状态未变化");
        }

        node.setStatus(targetStatus);
        node.setUpdatedTime(LocalDateTime.now());
        blockchainNodeMapper.updateById(node);

        adminOperationLogger.log(
                validAdminId,
                "管理员" + validAdminId,
                "ADMIN",
                "BLOCKCHAIN_NODE_STATUS",
                "更新区块链节点状态",
                "PUT",
                "/admin/blockchain/node/status/" + nodeId,
                JSONUtil.toJsonStr(dto),
                JSONUtil.toJsonStr(Map.of("nodeId", nodeId, "status", targetStatus)),
                null,
                null,
                0,
                Boolean.TRUE,
                null
        );
        adminDomainEventProducer.publish(
                "NODE_STATUS",
                BIZ_TYPE_BLOCKCHAIN,
                nodeId,
                JSONUtil.toJsonStr(Map.of("status", targetStatus)),
                validAdminId
        );
        return Result.success("节点状态更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("触发区块链节点同步")
    public Result syncBlockchainNode(Long nodeId, Long adminId) {
        Long validAdminId = requireAdmin(adminId);
        if (nodeId == null) {
            throw new BizException(400, "nodeId不能为空");
        }
        BlockchainNode node = blockchainNodeMapper.selectById(nodeId);
        if (node == null) {
            throw new BizException(404, "节点不存在");
        }

        long beforeHeight = node.getBlockHeight() == null ? 0L : node.getBlockHeight();
        long newHeight = beforeHeight + 1L;
        LocalDateTime now = LocalDateTime.now();
        node.setStatus(NODE_STATUS_SYNCING);
        node.setUpdatedTime(now);
        blockchainNodeMapper.updateById(node);

        node.setBlockHeight(newHeight);
        node.setLastSyncTime(now);
        node.setStatus(NODE_STATUS_ONLINE);
        node.setUpdatedTime(now);
        blockchainNodeMapper.updateById(node);

        adminOperationLogger.log(
                validAdminId,
                "管理员" + validAdminId,
                "ADMIN",
                "BLOCKCHAIN_NODE_SYNC",
                "触发区块链节点同步",
                "POST",
                "/admin/blockchain/node/sync/" + nodeId,
                null,
                JSONUtil.toJsonStr(Map.of("nodeId", nodeId, "blockHeight", newHeight)),
                null,
                null,
                0,
                Boolean.TRUE,
                null
        );
        adminDomainEventProducer.publish(
                "NODE_SYNC",
                BIZ_TYPE_BLOCKCHAIN,
                nodeId,
                JSONUtil.toJsonStr(Map.of("blockHeight", newHeight, "syncTime", now.toString())),
                validAdminId
        );
        return Result.success("同步任务已受理", Map.of(
                "accepted", Boolean.TRUE,
                "nodeId", nodeId,
                "blockHeight", newHeight,
                "lastSyncTime", now
        ));
    }

    @MethodPurpose("校验管理员ID")
    private Long requireAdmin(Long adminId) {
        if (adminId == null) {
            throw new BizException(401, "未登录或管理员ID缺失");
        }
        return adminId;
    }

    @MethodPurpose("解析证书服务返回的分页结果")
    private IPage<BlockchainCertificatePageVo> parseCertificatePage(Object data, long fallbackPageNum, long fallbackPageSize) {
        Map<String, Object> dataMap = toMap(data);
        long current = toLong(dataMap.get("current"), fallbackPageNum);
        long size = toLong(dataMap.get("size"), fallbackPageSize);
        long total = toLong(dataMap.get("total"), 0L);
        long pages = toLong(dataMap.get("pages"), 0L);

        Page<BlockchainCertificatePageVo> page = new Page<>(current, size, total);
        page.setPages(pages);

        List<BlockchainCertificatePageVo> records = new ArrayList<>();
        Object recordsObj = dataMap.get("records");
        if (recordsObj instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> row = toMap(item);
                BlockchainCertificatePageVo vo = new BlockchainCertificatePageVo();
                Long certificateId = toLongOrNull(row.get("certificateId"));
                if (certificateId == null) {
                    certificateId = toLongOrNull(row.get("id"));
                }
                vo.setCertificateId(certificateId);
                vo.setCertificateNumber(toText(row.get("certificateNumber")));
                vo.setStatus(toText(row.get("status")));
                vo.setBlockHeight(toLongOrNull(row.get("blockHeight")));
                vo.setTransactionHash(toText(row.get("transactionHash")));
                Boolean anchored = toBoolean(row.get("anchored"));
                if (anchored == null) {
                    anchored = vo.getBlockHeight() != null && StringUtils.hasText(vo.getTransactionHash());
                }
                vo.setAnchored(anchored);
                records.add(vo);
            }
        }
        page.setRecords(records);
        return page;
    }

    @MethodPurpose("对象转Map")
    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object source) {
        if (source instanceof Map<?, ?> sourceMap) {
            return (Map<String, Object>) sourceMap;
        }
        if (source == null) {
            return Map.of();
        }
        return JSONUtil.toBean(JSONUtil.toJsonStr(source), Map.class);
    }

    @MethodPurpose("解析Long，可指定默认值")
    private long toLong(Object value, long defaultValue) {
        Long parsed = toLongOrNull(value);
        return parsed == null ? defaultValue : parsed;
    }

    @MethodPurpose("解析Long")
    private Long toLongOrNull(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    @MethodPurpose("解析字符串")
    private String toText(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @MethodPurpose("解析布尔值")
    private Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = String.valueOf(value).trim().toLowerCase();
        if ("true".equals(text) || "1".equals(text)) {
            return Boolean.TRUE;
        }
        if ("false".equals(text) || "0".equals(text)) {
            return Boolean.FALSE;
        }
        return null;
    }

    @MethodPurpose("提取最近区块高度")
    private Long extractLatestBlockHeight(String responseResult) {
        if (!StringUtils.hasText(responseResult)) {
            return null;
        }
        try {
            Object dataObj = JSONUtil.parseObj(responseResult).get("data");
            if (!(dataObj instanceof Map<?, ?> dataMap)) {
                return null;
            }
            Object blockHeight = dataMap.get("blockHeight");
            return blockHeight == null ? null : Long.parseLong(String.valueOf(blockHeight));
        } catch (Exception ignore) {
            return null;
        }
    }

    @MethodPurpose("空值转0")
    private Integer nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    @MethodPurpose("字符串去空并返回null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    @MethodPurpose("字符串标准化为大写")
    private String normalizeUpper(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    @MethodPurpose("校验并标准化节点状态")
    private String normalizeNodeStatus(String status, boolean required) {
        if (!StringUtils.hasText(status)) {
            if (required) {
                throw new BizException(400, "status不能为空");
            }
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (NODE_STATUS_ONLINE.equals(normalized)
                || NODE_STATUS_OFFLINE.equals(normalized)
                || NODE_STATUS_SYNCING.equals(normalized)) {
            return normalized;
        }
        if (required) {
            throw new BizException(400, "status不合法，仅支持ONLINE/OFFLINE/SYNCING");
        }
        return null;
    }
}
