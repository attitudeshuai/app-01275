package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.SysRole;
import com.wms.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @RequirePermission("sys:role:list")
    @GetMapping("/page")
    public Result<PageResult<SysRole>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleName) {
        return Result.success(roleService.page(page, size, roleName));
    }

    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.list());
    }

    @RequirePermission("sys:role:list")
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @RequirePermission("sys:role:list")
    @Log(module = "角色管理", operation = "新增角色")
    @PostMapping
    public Result<Void> save(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.success();
    }

    @RequirePermission("sys:role:list")
    @Log(module = "角色管理", operation = "更新角色")
    @PutMapping
    public Result<Void> update(@RequestBody SysRole role) {
        roleService.update(role);
        return Result.success();
    }

    @RequirePermission("sys:role:list")
    @Log(module = "角色管理", operation = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @RequirePermission("sys:role:list")
    @GetMapping("/menu/{roleId}")
    public Result<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        return Result.success(roleService.getMenuIdsByRoleId(roleId));
    }

    @RequirePermission("sys:role:list")
    @Log(module = "角色管理", operation = "分配菜单")
    @PutMapping("/menu")
    public Result<Void> assignMenus(@RequestBody Map<String, Object> params) {
        Long roleId = Long.valueOf(params.get("roleId").toString());
        @SuppressWarnings("unchecked")
        List<Long> menuIds = ((List<Integer>) params.get("menuIds")).stream().map(Long::valueOf).toList();
        roleService.assignMenus(roleId, menuIds);
        return Result.success();
    }
}
