package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_stock_adjust")
public class BizStockAdjust {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String adjustNo;
    private Long warehouseId;
    private Long goodsId;
    private String adjustType; // LOSS报损 OVERFLOW报溢
    private Integer quantity;
    private String reason;
    private Long operatorId;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private String goodsName;
    @TableField(exist = false)
    private String operatorName;
}
