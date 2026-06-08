package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.BizSupplier;
import com.wms.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biz/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @RequirePermission("biz:supplier:list")
    @GetMapping("/page")
    public Result<PageResult<BizSupplier>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) Integer status) {
        return Result.success(supplierService.page(page, size, supplierName, status));
    }

    /**
     * 获取供应商下拉列表（无需权限，用于其他模块选择供应商）
     */
    @GetMapping("/list")
    public Result<List<BizSupplier>> list() {
        return Result.success(supplierService.list());
    }

    @RequirePermission("biz:supplier:add")
    @Log(module = "供应商管理", operation = "新增供应商")
    @PostMapping
    public Result<Void> save(@RequestBody BizSupplier supplier) {
        supplierService.save(supplier);
        return Result.success();
    }

    @RequirePermission("biz:supplier:edit")
    @Log(module = "供应商管理", operation = "更新供应商")
    @PutMapping
    public Result<Void> update(@RequestBody BizSupplier supplier) {
        supplierService.update(supplier);
        return Result.success();
    }

    @RequirePermission("biz:supplier:delete")
    @Log(module = "供应商管理", operation = "删除供应商")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return Result.success();
    }
}
