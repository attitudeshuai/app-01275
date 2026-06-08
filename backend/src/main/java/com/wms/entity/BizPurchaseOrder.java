package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("biz_purchase_order")
public class BizPurchaseOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long supplierId;
    private Long warehouseId;
    private Long applicantId;
    private Long deptId; // 部门ID，用于按部门统计
    private BigDecimal totalAmount;
    private Integer status; // 0待审批 1已通过 2已拒绝 3已入库
    private String remark;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
    private Long approverId;
    private String approveRemark;
    private LocalDateTime inboundTime;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String supplierName;
    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private String applicantName;
    @TableField(exist = false)
    private String approverName;
    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private List<BizPurchaseItem> items;
}
