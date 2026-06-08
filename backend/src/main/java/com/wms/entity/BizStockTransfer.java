package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_stock_transfer")
public class BizStockTransfer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String transferNo;
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private Long goodsId;
    private Integer quantity;
    private Integer status;
    private Long operatorId;
    private String remark;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String fromWarehouseName;
    @TableField(exist = false)
    private String toWarehouseName;
    @TableField(exist = false)
    private String goodsName;
    @TableField(exist = false)
    private String operatorName;
}
