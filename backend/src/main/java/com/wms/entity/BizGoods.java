package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_goods")
public class BizGoods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String goodsCode;
    private String goodsName;
    private Long categoryId;
    private String unit;
    private String spec;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Integer safetyStock;
    private Integer maxStock;
    private String remark;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private Integer totalStock;
}
