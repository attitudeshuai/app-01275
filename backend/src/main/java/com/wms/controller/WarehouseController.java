package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.BizWarehouse;
import com.wms.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biz/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @RequirePermission("biz:warehouse:list")
    @GetMapping("/page")
    public Result<PageResult<BizWarehouse>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String warehouseName,
            @RequestParam(required = false) Integer status) {
        return Result.success(warehouseService.page(page, size, warehouseName, status));
    }

    /**
     * 获取仓库下拉列表（无需权限，用于其他模块选择仓库）
     */
    @GetMapping("/list")
    public Result<List<BizWarehouse>> list() {
        return Result.success(warehouseService.list());
    }

    @RequirePermission("biz:warehouse:add")
    @Log(module = "仓库管理", operation = "新增仓库")
    @PostMapping
    public Result<Void> save(@RequestBody BizWarehouse warehouse) {
        warehouseService.save(warehouse);
        return Result.success();
    }

    @RequirePermission("biz:warehouse:edit")
    @Log(module = "仓库管理", operation = "更新仓库")
    @PutMapping
    public Result<Void> update(@RequestBody BizWarehouse warehouse) {
        warehouseService.update(warehouse);
        return Result.success();
    }

    @RequirePermission("biz:warehouse:delete")
    @Log(module = "仓库管理", operation = "删除仓库")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return Result.success();
    }
}
