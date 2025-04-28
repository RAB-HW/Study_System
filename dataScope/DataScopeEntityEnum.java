package com.hzb.erp.api.base.dataScope;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限类型
 * 数据权限实际上就是通过拼接sql语句实现的，查询个人的用defaultField 字段 查询机构的用defaultOrgField 字段，具体实现见：
 * com.hzb.erp.api.base.dataScope.DataScopeInterceptor#makeCondition(java.lang.String, java.lang.String)
 *
 * 枚举中的表是可以受数据权限管控的，如果需要自行增加即可
 */
@Getter
@AllArgsConstructor
public enum DataScopeEntityEnum {

    /**
     * 数据权限相关表定义如下，也可以根据需求自定义
     * 带(*)的表示不支持'自己的数据'
     */
    STUDENT_LEAVE("student_leave", "学生请假", "counselor", "school_id"),
    STUDENT_COURSE("student_course", "报名表", "creator","org_id"),
    STAFF("staff", "员工账号表(*)", null,"org_id"),
    STUDENT("student", "学生表", "counselor","org_id"),
    USER("user", "学生账号表(*)", null,"org_id"),
    CLASS("class", "班级表(*)", null,"school_id"),
    CLASSROOM("classroom", "教室表(*)", null,"school_id"),
    COURSE("course", "课程表", "creator","org_id"),
    LESSON("lesson", "课时表", "teacher_id","org_id"),
    COURSE_TRIAL("course_trial", "课程试听卡表", "creator","org_id"),
    COURSE_TRIAL_RECORD("course_trial_record", "试听卡领取记录", "counselor", "school_id"),
    APPOINTMENT("appointment", "上课预约表", "counselor", "school_id"),
    LESSON_SCHEDULE("lesson_schedule", "排课计划表", "creator","org_id"),
    HOMEWORK("homework", "作业表", "creator","org_id"),
    GRADE("grade", "成绩表", "creator","org_id"),
    CONTACT_RECORD("contact_record", "跟进记录表", "creator","org_id"),
    SHOP_ORDER("shop_order", "在线购课订单表(*)", null, "school_id"), // 学校id,
    SHOP_ORDER_REFUND("shop_order_refund", "购课退款表(*)", null, "school_id"),
    CREDIT_MALL("credit_mall", "积分商品表(*)", null, "school_id"), // 学校id,
    STUDENT_CREDIT_LOG("student_credit_log", "学生积分记录表(*)", null, "school_id"), // 学校id,
    CREDIT_EXCHANGE("credit_exchange", "积分兑换记录表(*)", null, "school_id"), // 学校id,
    LESSON_STUDENT("lesson_student", "教评学表", "teacher_id","org_id"),
    TEACH_EVALUATION("teach_evaluation", "学评教表", "teacher_id","org_id"),
    MATERIAL("material", "物料表", "creator", "school_id"),
    MATERIAL_RECORD("material_record", "物料出入库", "creator", "school_id"),
    FINANCE_RECORD("finance_record", "财务审核表", "operator","org_id"),
    CASHOUT("cashout", "请款表", "creator","org_id"),
    SYS_LOG("sys_log", "系统日志表", "operator","org_id"),
    OPERATION_RECORD("operation_record", "操作记录表", "creator", "org_id");

    /**
     * 数据表名
     */
    private final String code;

    /**
     * 描述
     */
    private final String dist;

    /**
     * 表中数据默认负责人字段,用于约束只查看自己数据的查询语句,会记录到DataPermission实体的ownerField属性里
     * 为null的话表示是不支持按老师ID查询的，因为没有有效的创建者字段
     */
    private final String defaultField;

    /**
     * 表中数据默认负责机构字段,用于按组织查询,会记录到DataPermission实体的ownerOrgField属性里
     */
    private final String defaultOrgField;

}
