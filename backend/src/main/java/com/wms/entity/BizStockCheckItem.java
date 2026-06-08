package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("biz_stock_check_item")
public class BizStockCheckItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long checkId;
    private Long goodsId;
    private Integer systemQty;
    private Integer actualQty;
    private Integer diffQty;

    @TableField(exist = false)
    private String goodsCode;
    @TableField(exist = false)
    private String goodsName;
}
