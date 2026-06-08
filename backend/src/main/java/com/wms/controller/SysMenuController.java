package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.Result;
import com.wms.entity.SysMenu;
import com.wms.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @RequirePermission("sys:menu:list")
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        return Result.success(menuService.tree());
    }

    @RequirePermission("sys:menu:add")
    @Log(module = "菜单管理", operation = "新增菜单")
    @PostMapping
    public Result<Void> save(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return Result.success();
    }

    @RequirePermission("sys:menu:edit")
    @Log(module = "菜单管理", operation = "更新菜单")
    @PutMapping
    public Result<Void> update(@RequestBody SysMenu menu) {
        menuService.update(menu);
        return Result.success();
    }

    @RequirePermission("sys:menu:delete")
    @Log(module = "菜单管理", operation = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
