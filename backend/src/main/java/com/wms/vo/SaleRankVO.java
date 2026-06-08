package com.wms.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SaleRankVO {
    private Integer rank;
    private String goodsCode;
    private String goodsName;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
}
