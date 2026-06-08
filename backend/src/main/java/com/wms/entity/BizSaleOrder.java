package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("biz_sale_order")
public class BizSaleOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long customerId;
    private Long warehouseId;
    private Long salesmanId;
    private Long deptId; // 部门ID，用于按部门统计
    private BigDecimal totalAmount;
    private BigDecimal receivedAmount;
    // 状态: -1报价中 0待审核 1已审核 2已发货 3已收款 4已取消
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime approveTime;
    private Long approverId;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
    private LocalDateTime quoteTime; // 报价时间
    private LocalDateTime confirmTime; // 客户确认时间
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private String salesmanName;
    @TableField(exist = false)
    private String approverName;
    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private List<BizSaleItem> items;
    @TableField(exist = false)
    private List<BizPaymentRecord> paymentRecords; // 收款记录
}
