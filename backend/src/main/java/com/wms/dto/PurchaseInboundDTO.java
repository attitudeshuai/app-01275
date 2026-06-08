package com.wms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 采购入库 DTO
 */
@Data
public class PurchaseInboundDTO {

    /**
     * 采购单ID
     */
    @NotNull(message = "采购单ID不能为空")
    private Long id;
}
