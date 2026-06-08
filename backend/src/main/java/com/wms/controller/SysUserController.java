package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.SysUser;
import com.wms.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @RequirePermission("sys:user:list")
    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Long deptId) {
        return Result.success(userService.page(page, size, username, realName, deptId));
    }

    @RequirePermission("sys:user:list")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @RequirePermission("sys:user:add")
    @Log(module = "用户管理", operation = "新增用户")
    @PostMapping
    public Result<Void> save(@RequestBody SysUser user) {
        userService.save(user);
        return Result.success();
    }

    @RequirePermission("sys:user:edit")
    @Log(module = "用户管理", operation = "更新用户")
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        userService.update(user);
        return Result.success();
    }

    @RequirePermission("sys:user:delete")
    @Log(module = "用户管理", operation = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @RequirePermission("sys:user:edit")
    @Log(module = "用户管理", operation = "修改状态")
    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @RequirePermission("sys:user:resetPwd")
    @Log(module = "用户管理", operation = "重置密码")
    @PutMapping("/reset-password/{id}")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    @Log(module = "用户管理", operation = "更新个人信息")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody SysUser user) {
        userService.updateUserInfo(user);
        return Result.success();
    }
}
