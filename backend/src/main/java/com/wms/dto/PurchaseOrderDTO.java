package com.wms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 采购单创建/更新 DTO
 */
@Data
public class PurchaseOrderDTO {

    /**
     * 采购单ID（更新时必填）
     */
    private Long id;

    /**
     * 供应商ID
     */
    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    /**
     * 入库仓库ID
     */
    @NotNull(message = "入库仓库不能为空")
    private Long warehouseId;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;

    /**
     * 采购明细列表
     */
    @NotEmpty(message = "采购明细不能为空")
    @Valid
    private List<PurchaseItemDTO> items;

    /**
     * 采购明细 DTO
     */
    @Data
    public static class PurchaseItemDTO {

        /**
         * 商品ID
         */
        @NotNull(message = "商品不能为空")
        private Long goodsId;

        /**
         * 采购数量
         */
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须大于0")
        private Integer quantity;

        /**
         * 采购单价
         */
        @NotNull(message = "单价不能为空")
        @DecimalMin(value = "0.01", message = "单价必须大于0")
        private BigDecimal price;
    }
}
