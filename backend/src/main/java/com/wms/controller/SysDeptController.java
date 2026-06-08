package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.Result;
import com.wms.entity.SysDept;
import com.wms.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @GetMapping("/tree")
    public Result<List<SysDept>> tree() {
        return Result.success(deptService.tree());
    }

    @RequirePermission("sys:dept:list")
    @GetMapping("/list")
    public Result<List<SysDept>> list() {
        return Result.success(deptService.tree());
    }

    @RequirePermission("sys:dept:add")
    @Log(module = "部门管理", operation = "新增部门")
    @PostMapping
    public Result<Void> save(@RequestBody SysDept dept) {
        deptService.save(dept);
        return Result.success();
    }

    @RequirePermission("sys:dept:edit")
    @Log(module = "部门管理", operation = "更新部门")
    @PutMapping
    public Result<Void> update(@RequestBody SysDept dept) {
        deptService.update(dept);
        return Result.success();
    }

    @RequirePermission("sys:dept:delete")
    @Log(module = "部门管理", operation = "删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.success();
    }
}
