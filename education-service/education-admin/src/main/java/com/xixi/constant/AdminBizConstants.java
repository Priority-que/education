package com.xixi.constant;

/**
 * 管理域业务常量。
 */
public final class AdminBizConstants {
    public static final String AUDIT_STATUS_PENDING = "PENDING";
    public static final String AUDIT_STATUS_APPROVED = "APPROVED";
    public static final String AUDIT_STATUS_REJECTED = "REJECTED";

    public static final String AUDIT_TYPE_ENTERPRISE = "ENTERPRISE";
    public static final String AUDIT_TYPE_TEACHER = "TEACHER";
    public static final String AUDIT_TYPE_COURSE = "COURSE";
    public static final String AUDIT_TYPE_COURSE_REPORT = "COURSE_REPORT";
    public static final String AUDIT_TYPE_CERTIFICATE = "CERTIFICATE";
    public static final String AUDIT_TYPE_CERTIFICATE_APPEAL = "CERTIFICATE_APPEAL";

    public static final String USER_STATUS_ENABLED = "1";
    public static final String USER_STATUS_DISABLED = "0";

    private AdminBizConstants() {
    }
}
