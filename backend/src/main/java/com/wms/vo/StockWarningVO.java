package com.wms.vo;

import lombok.Data;

@Data
public class StockWarningVO {
    private String goodsCode;
    private String goodsName;
    private String warehouseName;
    private Integer quantity;
    private Integer safetyStock;
    private Integer maxStock;
    private String warningType;
}
