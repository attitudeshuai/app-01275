package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.BizCategory;
import com.wms.entity.BizGoods;
import com.wms.mapper.BizCategoryMapper;
import com.wms.mapper.BizGoodsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsService {

    private final BizGoodsMapper goodsMapper;
    private final BizCategoryMapper categoryMapper;

    public PageResult<BizGoods> page(int page, int size, String goodsCode, String goodsName, Long categoryId) {
        IPage<BizGoods> result = goodsMapper.selectGoodsPage(new Page<>(page, size), goodsCode, goodsName, categoryId);
        return PageResult.of(result);
    }

    public List<BizGoods> list() {
        return goodsMapper.selectList(new LambdaQueryWrapper<BizGoods>()
                .eq(BizGoods::getStatus, 1)
                .orderByDesc(BizGoods::getId)
                .last("LIMIT 500")); // 限制返回数量，防止数据量过大
    }

    /**
     * 根据ID列表获取商品
     */
    public List<BizGoods> getByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return goodsMapper.selectList(new LambdaQueryWrapper<BizGoods>()
                .in(BizGoods::getId, ids)
                .eq(BizGoods::getStatus, 1));
    }

    public BizGoods getById(Long id) {
        return goodsMapper.selectById(id);
    }

    public void save(BizGoods goods) {
        if (goodsMapper.selectCount(new LambdaQueryWrapper<BizGoods>()
                .eq(BizGoods::getGoodsCode, goods.getGoodsCode())) > 0) {
            throw new BusinessException("商品编码已存在");
        }
        goodsMapper.insert(goods);
        log.info("新增商品: {}", goods.getGoodsName());
    }

    public void update(BizGoods goods) {
        goodsMapper.updateById(goods);
        log.info("更新商品: {}", goods.getGoodsName());
    }

    public void delete(Long id) {
        goodsMapper.deleteById(id);
        log.info("删除商品: {}", id);
    }

    public List<BizCategory> categoryTree() {
        List<BizCategory> categories = categoryMapper.selectList(
            new LambdaQueryWrapper<BizCategory>().orderByAsc(BizCategory::getSort));
        return buildCategoryTree(categories);
    }

    public void saveCategory(BizCategory category) {
        categoryMapper.insert(category);
    }

    public void updateCategory(BizCategory category) {
        categoryMapper.updateById(category);
    }

    public void deleteCategory(Long id) {
        long count = categoryMapper.selectCount(new LambdaQueryWrapper<BizCategory>().eq(BizCategory::getParentId, id));
        if (count > 0) {
            throw new BusinessException("存在子分类，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    private List<BizCategory> buildCategoryTree(List<BizCategory> categories) {
        Map<Long, BizCategory> map = categories.stream().collect(Collectors.toMap(BizCategory::getId, c -> c));
        List<BizCategory> tree = new ArrayList<>();
        for (BizCategory cat : categories) {
            if (cat.getParentId() == 0) {
                tree.add(cat);
            } else {
                BizCategory parent = map.get(cat.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(cat);
                }
            }
        }
        return tree;
    }
}
