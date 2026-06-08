package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_stock")
public class BizStock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Long warehouseId;
    private Integer quantity;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String goodsCode;
    @TableField(exist = false)
    private String goodsName;
    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private Integer safetyStock;
    @TableField(exist = false)
    private Integer maxStock;
}
