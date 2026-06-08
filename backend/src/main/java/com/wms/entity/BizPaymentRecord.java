package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收款记录实体
 * 用于记录每一笔收款的详细信息，支持分多次收款
 */
@Data
@TableName("biz_payment_record")
public class BizPaymentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String paymentNo; // 收款单号
    private Long orderId; // 关联销售单ID
    private String orderNo; // 关联销售单号
    private BigDecimal amount; // 收款金额
    private String paymentMethod; // 收款方式: CASH现金 BANK银行转账 ALIPAY支付宝 WECHAT微信
    private String remark; // 备注
    private Long operatorId; // 操作人ID
    private LocalDateTime createTime; // 收款时间

    @TableField(exist = false)
    private String operatorName; // 操作人姓名
    @TableField(exist = false)
    private String customerName; // 客户名称
}
