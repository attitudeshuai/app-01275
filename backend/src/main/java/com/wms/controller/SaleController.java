package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.BizSaleOrder;
import com.wms.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/biz/sale")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @RequirePermission("biz:sale:list")
    @GetMapping("/page")
    public Result<PageResult<BizSaleOrder>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer status) {
        return Result.success(saleService.page(page, size, orderNo, customerId, status));
    }

    @RequirePermission("biz:sale:list")
    @GetMapping("/{id}")
    public Result<BizSaleOrder> getById(@PathVariable Long id) {
        return Result.success(saleService.getById(id));
    }

    @RequirePermission("biz:sale:quote")
    @Log(module = "销售管理", operation = "创建销售报价")
    @PostMapping("/quote")
    public Result<Void> quote(@RequestBody BizSaleOrder order) {
        saleService.quote(order);
        return Result.success();
    }

    @RequirePermission("biz:sale:quote")
    @Log(module = "销售管理", operation = "确认报价转销售单")
    @PutMapping("/confirm/{id}")
    public Result<Void> confirmQuote(@PathVariable Long id) {
        saleService.confirmQuote(id);
        return Result.success();
    }

    @RequirePermission("biz:sale:quote")
    @Log(module = "销售管理", operation = "创建销售单")
    @PostMapping
    public Result<Void> save(@RequestBody BizSaleOrder order) {
        saleService.save(order);
        return Result.success();
    }

    @RequirePermission("biz:sale:quote")
    @Log(module = "销售管理", operation = "更新销售单")
    @PutMapping
    public Result<Void> update(@RequestBody BizSaleOrder order) {
        saleService.update(order);
        return Result.success();
    }

    @RequirePermission("biz:sale:quote")
    @Log(module = "销售管理", operation = "删除销售单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        saleService.delete(id);
        return Result.success();
    }

    @RequirePermission("biz:sale:approve")
    @Log(module = "销售管理", operation = "审核销售单")
    @PutMapping("/approve")
    public Result<Void> approve(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        boolean approved = Boolean.parseBoolean(params.get("approved").toString());
        saleService.approve(id, approved);
        return Result.success();
    }

    @RequirePermission("biz:sale:ship")
    @Log(module = "销售管理", operation = "发货出库")
    @PutMapping("/ship")
    public Result<Void> ship(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long warehouseId = Long.valueOf(params.get("warehouseId").toString());
        saleService.ship(id, warehouseId);
        return Result.success();
    }

    @RequirePermission("biz:sale:receive")
    @Log(module = "销售管理", operation = "收款确认")
    @PutMapping("/receive")
    public Result<Void> receive(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String paymentMethod = params.get("paymentMethod") != null ? params.get("paymentMethod").toString() : "BANK";
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        saleService.receive(id, amount, paymentMethod, remark);
        return Result.success();
    }

    @RequirePermission("biz:sale:receive")
    @GetMapping("/payment/{orderId}")
    public Result<?> getPaymentRecords(@PathVariable Long orderId) {
        return Result.success(saleService.getPaymentRecords(orderId));
    }
}
