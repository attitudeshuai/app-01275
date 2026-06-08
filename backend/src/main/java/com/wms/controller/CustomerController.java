package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.BizCustomer;
import com.wms.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biz/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @RequirePermission("biz:customer:list")
    @GetMapping("/page")
    public Result<PageResult<BizCustomer>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Integer status) {
        return Result.success(customerService.page(page, size, customerName, status));
    }

    /**
     * 获取客户下拉列表（无需权限，用于其他模块选择客户）
     */
    @GetMapping("/list")
    public Result<List<BizCustomer>> list() {
        return Result.success(customerService.list());
    }

    @RequirePermission("biz:customer:add")
    @Log(module = "客户管理", operation = "新增客户")
    @PostMapping
    public Result<Void> save(@RequestBody BizCustomer customer) {
        customerService.save(customer);
        return Result.success();
    }

    @RequirePermission("biz:customer:edit")
    @Log(module = "客户管理", operation = "更新客户")
    @PutMapping
    public Result<Void> update(@RequestBody BizCustomer customer) {
        customerService.update(customer);
        return Result.success();
    }

    @RequirePermission("biz:customer:delete")
    @Log(module = "客户管理", operation = "删除客户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.success();
    }
}
