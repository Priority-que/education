package com.xixi.pojo.query.certificate;

import lombok.Data;

/**
 * 8.3 教师已颁发证书分页查询参数。
 */
@Data
public class CertificateTeacherIssuedQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private Long courseId;
    private String status;
}

