package com.wms.controller;

import com.wms.common.Result;
import com.wms.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys/preference")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;

    /**
     * 获取商品选择历史
     * @param type PURCHASE 或 SALE
     */
    @GetMapping("/goods-history/{type}")
    public Result<List<Long>> getGoodsHistory(@PathVariable String type) {
        return Result.success(preferenceService.getGoodsHistory(type.toUpperCase()));
    }

    /**
     * 保存商品选择历史
     */
    @PostMapping("/goods-history/{type}")
    public Result<Void> saveGoodsHistory(@PathVariable String type, @RequestBody Map<String, List<Long>> body) {
        List<Long> goodsIds = body.get("goodsIds");
        if (goodsIds != null && !goodsIds.isEmpty()) {
            preferenceService.saveGoodsHistory(type.toUpperCase(), goodsIds);
        }
        return Result.success();
    }

    /**
     * 清除商品选择历史
     */
    @DeleteMapping("/goods-history/{type}")
    public Result<Void> clearGoodsHistory(@PathVariable String type) {
        preferenceService.clearGoodsHistory(type.toUpperCase());
        return Result.success();
    }
}
