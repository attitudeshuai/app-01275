package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("biz_purchase_item")
public class BizPurchaseItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long goodsId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal amount;

    @TableField(exist = false)
    private String goodsCode;
    @TableField(exist = false)
    private String goodsName;
    @TableField(exist = false)
    private String unit;
}
