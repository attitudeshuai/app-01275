package com.wms.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProfitVO {
    private String goodsCode;
    private String goodsName;
    private Integer quantity;
    private BigDecimal saleAmount;
    private BigDecimal costAmount;
    private BigDecimal profit;
    private BigDecimal profitRate;
}
