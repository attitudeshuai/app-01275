package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.BizSupplier;
import com.wms.mapper.BizSupplierMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final BizSupplierMapper supplierMapper;

    public PageResult<BizSupplier> page(int page, int size, String supplierName, Integer status) {
        LambdaQueryWrapper<BizSupplier> wrapper = new LambdaQueryWrapper<>();
        if (supplierName != null && !supplierName.isEmpty()) {
            wrapper.like(BizSupplier::getSupplierName, supplierName);
        }
        if (status != null) {
            wrapper.eq(BizSupplier::getStatus, status);
        }
        wrapper.orderByDesc(BizSupplier::getCreateTime);
        IPage<BizSupplier> result = supplierMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }

    public List<BizSupplier> list() {
        return supplierMapper.selectList(new LambdaQueryWrapper<BizSupplier>().eq(BizSupplier::getStatus, 1));
    }

    public void save(BizSupplier supplier) {
        if (supplierMapper.selectCount(new LambdaQueryWrapper<BizSupplier>()
                .eq(BizSupplier::getSupplierCode, supplier.getSupplierCode())) > 0) {
            throw new BusinessException("供应商编码已存在");
        }
        supplierMapper.insert(supplier);
        log.info("新增供应商: {}", supplier.getSupplierName());
    }

    public void update(BizSupplier supplier) {
        supplierMapper.updateById(supplier);
        log.info("更新供应商: {}", supplier.getSupplierName());
    }

    public void delete(Long id) {
        supplierMapper.deleteById(id);
        log.info("删除供应商: {}", id);
    }
}
