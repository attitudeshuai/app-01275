package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.*;
import com.wms.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/biz/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @RequirePermission("biz:stock:list")
    @GetMapping("/page")
    public Result<PageResult<BizStock>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String goodsName,
            @RequestParam(required = false) Long warehouseId) {
        return Result.success(stockService.page(page, size, goodsName, warehouseId));
    }

    @RequirePermission("biz:stock:warning")
    @GetMapping("/warning")
    public Result<PageResult<BizStock>> warningList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "low") String type) {
        return Result.success(stockService.warningPage(page, size, type));
    }

    @RequirePermission("biz:stock:list")
    @GetMapping("/record")
    public Result<PageResult<BizStockRecord>> recordPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long goodsId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String recordType) {
        return Result.success(stockService.recordPage(page, size, goodsId, warehouseId, recordType));
    }

    // 盘点
    @RequirePermission("biz:stock:check")
    @GetMapping("/check/page")
    public Result<PageResult<BizStockCheck>> checkPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String checkNo,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer status) {
        return Result.success(stockService.checkPage(page, size, checkNo, warehouseId, status));
    }

    @RequirePermission("biz:stock:check")
    @GetMapping("/check/{id}")
    public Result<BizStockCheck> getCheckById(@PathVariable Long id) {
        return Result.success(stockService.getCheckById(id));
    }

    @RequirePermission("biz:stock:check")
    @Log(module = "库存管理", operation = "创建盘点单")
    @PostMapping("/check")
    public Result<Void> createCheck(@RequestBody BizStockCheck check) {
        stockService.createCheck(check);
        return Result.success();
    }

    @RequirePermission("biz:stock:check")
    @Log(module = "库存管理", operation = "确认盘点")
    @PutMapping("/check/confirm/{id}")
    public Result<Void> confirmCheck(@PathVariable Long id) {
        stockService.confirmCheck(id);
        return Result.success();
    }

    // 调拨
    @RequirePermission("biz:stock:transfer")
    @GetMapping("/transfer/page")
    public Result<PageResult<BizStockTransfer>> transferPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long goodsId) {
        return Result.success(stockService.transferPage(page, size, goodsId));
    }

    @RequirePermission("biz:stock:transfer")
    @Log(module = "库存管理", operation = "库存调拨")
    @PostMapping("/transfer")
    public Result<Void> createTransfer(@RequestBody BizStockTransfer transfer) {
        stockService.createTransfer(transfer);
        return Result.success();
    }

    // 报损报溢
    @RequirePermission("biz:stock:adjust")
    @GetMapping("/adjust/page")
    public Result<PageResult<BizStockAdjust>> adjustPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String adjustType,
            @RequestParam(required = false) Long warehouseId) {
        return Result.success(stockService.adjustPage(page, size, adjustType, warehouseId));
    }

    @RequirePermission("biz:stock:adjust")
    @Log(module = "库存管理", operation = "报损报溢")
    @PostMapping("/adjust")
    public Result<Void> createAdjust(@RequestBody BizStockAdjust adjust) {
        stockService.createAdjust(adjust);
        return Result.success();
    }
}
