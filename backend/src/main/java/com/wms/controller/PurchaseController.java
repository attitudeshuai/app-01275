package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.dto.PurchaseApproveDTO;
import com.wms.dto.PurchaseInboundDTO;
import com.wms.dto.PurchaseOrderDTO;
import com.wms.entity.BizPurchaseOrder;
import com.wms.service.PurchaseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/biz/purchase")
@RequiredArgsConstructor
@Validated
public class PurchaseController {

    private final PurchaseService purchaseService;

    @RequirePermission("biz:purchase:list")
    @GetMapping("/page")
    public Result<PageResult<BizPurchaseOrder>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数必须大于0") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status) {
        return Result.success(purchaseService.page(page, size, orderNo, supplierId, status));
    }

    @RequirePermission("biz:purchase:list")
    @GetMapping("/{id}")
    public Result<BizPurchaseOrder> getById(@PathVariable Long id) {
        return Result.success(purchaseService.getById(id));
    }

    @RequirePermission("biz:purchase:apply")
    @Log(module = "采购管理", operation = "创建采购单")
    @PostMapping
    public Result<Void> save(@RequestBody @Valid PurchaseOrderDTO dto) {
        purchaseService.save(dto);
        return Result.success();
    }

    @RequirePermission("biz:purchase:apply")
    @Log(module = "采购管理", operation = "更新采购单")
    @PutMapping
    public Result<Void> update(@RequestBody @Valid PurchaseOrderDTO dto) {
        purchaseService.update(dto);
        return Result.success();
    }

    @RequirePermission("biz:purchase:apply")
    @Log(module = "采购管理", operation = "删除采购单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseService.delete(id);
        return Result.success();
    }

    @RequirePermission("biz:purchase:approve")
    @Log(module = "采购管理", operation = "审批采购单")
    @PutMapping("/approve")
    public Result<Void> approve(@RequestBody @Valid PurchaseApproveDTO dto) {
        purchaseService.approve(dto.getId(), dto.getApproved(), dto.getRemark());
        return Result.success();
    }

    @RequirePermission("biz:purchase:inbound")
    @Log(module = "采购管理", operation = "采购入库")
    @PutMapping("/inbound")
    public Result<Void> inbound(@RequestBody @Valid PurchaseInboundDTO dto) {
        purchaseService.inbound(dto.getId());
        return Result.success();
    }
}
