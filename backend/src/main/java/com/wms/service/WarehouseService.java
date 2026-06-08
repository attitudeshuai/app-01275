package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.BizWarehouse;
import com.wms.mapper.BizWarehouseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final BizWarehouseMapper warehouseMapper;

    public PageResult<BizWarehouse> page(int page, int size, String warehouseName, Integer status) {
        LambdaQueryWrapper<BizWarehouse> wrapper = new LambdaQueryWrapper<>();
        if (warehouseName != null && !warehouseName.isEmpty()) {
            wrapper.like(BizWarehouse::getWarehouseName, warehouseName);
        }
        if (status != null) {
            wrapper.eq(BizWarehouse::getStatus, status);
        }
        wrapper.orderByDesc(BizWarehouse::getCreateTime);
        IPage<BizWarehouse> result = warehouseMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }

    public List<BizWarehouse> list() {
        return warehouseMapper.selectList(new LambdaQueryWrapper<BizWarehouse>().eq(BizWarehouse::getStatus, 1));
    }

    public void save(BizWarehouse warehouse) {
        if (warehouseMapper.selectCount(new LambdaQueryWrapper<BizWarehouse>()
                .eq(BizWarehouse::getWarehouseCode, warehouse.getWarehouseCode())) > 0) {
            throw new BusinessException("仓库编码已存在");
        }
        warehouseMapper.insert(warehouse);
        log.info("新增仓库: {}", warehouse.getWarehouseName());
    }

    public void update(BizWarehouse warehouse) {
        warehouseMapper.updateById(warehouse);
        log.info("更新仓库: {}", warehouse.getWarehouseName());
    }

    public void delete(Long id) {
        warehouseMapper.deleteById(id);
        log.info("删除仓库: {}", id);
    }
}
