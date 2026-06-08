package com.wms.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 采购单审批 DTO
 */
@Data
public class PurchaseApproveDTO {

    /**
     * 采购单ID
     */
    @NotNull(message = "采购单ID不能为空")
    private Long id;

    /**
     * 是否通过审批
     */
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    /**
     * 审批备注
     */
    @Size(max = 500, message = "审批备注长度不能超过500字符")
    private String remark;
}
