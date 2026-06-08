package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.BizCategory;
import com.wms.entity.BizGoods;
import com.wms.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biz/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    /**
     * 获取商品下拉列表（无需权限，用于其他模块选择商品）
     * 限制返回500条，防止数据量过大
     */
    @GetMapping("/list")
    public Result<List<BizGoods>> list() {
        return Result.success(goodsService.list());
    }

    /**
     * 根据ID列表批量获取商品（无需权限，用于商品选择弹窗回显）
     */
    @PostMapping("/byIds")
    public Result<List<BizGoods>> getByIds(@RequestBody List<Long> ids) {
        return Result.success(goodsService.getByIds(ids));
    }

    /**
     * 商品分页查询（无需权限，用于商品选择弹窗）
     */
    @GetMapping("/page")
    public Result<PageResult<BizGoods>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String goodsCode,
            @RequestParam(required = false) String goodsName,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(goodsService.page(page, size, goodsCode, goodsName, categoryId));
    }

    /**
     * 获取商品详情（无需权限，用于其他模块查看商品信息）
     */
    @GetMapping("/{id}")
    public Result<BizGoods> getById(@PathVariable Long id) {
        return Result.success(goodsService.getById(id));
    }

    @RequirePermission("biz:goods:add")
    @Log(module = "商品管理", operation = "新增商品")
    @PostMapping
    public Result<Void> save(@RequestBody BizGoods goods) {
        goodsService.save(goods);
        return Result.success();
    }

    @RequirePermission("biz:goods:edit")
    @Log(module = "商品管理", operation = "更新商品")
    @PutMapping
    public Result<Void> update(@RequestBody BizGoods goods) {
        goodsService.update(goods);
        return Result.success();
    }

    @RequirePermission("biz:goods:delete")
    @Log(module = "商品管理", operation = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        goodsService.delete(id);
        return Result.success();
    }

    /**
     * 获取商品分类树（无需权限，用于商品选择时筛选分类）
     */
    @GetMapping("/category/tree")
    public Result<List<BizCategory>> categoryTree() {
        return Result.success(goodsService.categoryTree());
    }

    @RequirePermission("biz:goods:add")
    @PostMapping("/category")
    public Result<Void> saveCategory(@RequestBody BizCategory category) {
        goodsService.saveCategory(category);
        return Result.success();
    }

    @RequirePermission("biz:goods:edit")
    @PutMapping("/category")
    public Result<Void> updateCategory(@RequestBody BizCategory category) {
        goodsService.updateCategory(category);
        return Result.success();
    }

    @RequirePermission("biz:goods:delete")
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        goodsService.deleteCategory(id);
        return Result.success();
    }
}
