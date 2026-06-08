package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("biz_sale_item")
public class BizSaleItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long goodsId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal costPrice; // 成本价(发货时记录)

    @TableField(exist = false)
    private String goodsCode;
    @TableField(exist = false)
    private String goodsName;
    @TableField(exist = false)
    private String unit;
    @TableField(exist = false)
    private BigDecimal purchasePrice;
}
