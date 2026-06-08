package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.BizCustomer;
import com.wms.mapper.BizCustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final BizCustomerMapper customerMapper;

    public PageResult<BizCustomer> page(int page, int size, String customerName, Integer status) {
        LambdaQueryWrapper<BizCustomer> wrapper = new LambdaQueryWrapper<>();
        if (customerName != null && !customerName.isEmpty()) {
            wrapper.like(BizCustomer::getCustomerName, customerName);
        }
        if (status != null) {
            wrapper.eq(BizCustomer::getStatus, status);
        }
        wrapper.orderByDesc(BizCustomer::getCreateTime);
        IPage<BizCustomer> result = customerMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }

    public List<BizCustomer> list() {
        return customerMapper.selectList(new LambdaQueryWrapper<BizCustomer>().eq(BizCustomer::getStatus, 1));
    }

    public void save(BizCustomer customer) {
        if (customerMapper.selectCount(new LambdaQueryWrapper<BizCustomer>()
                .eq(BizCustomer::getCustomerCode, customer.getCustomerCode())) > 0) {
            throw new BusinessException("客户编码已存在");
        }
        customerMapper.insert(customer);
        log.info("新增客户: {}", customer.getCustomerName());
    }

    public void update(BizCustomer customer) {
        customerMapper.updateById(customer);
        log.info("更新客户: {}", customer.getCustomerName());
    }

    public void delete(Long id) {
        customerMapper.deleteById(id);
        log.info("删除客户: {}", id);
    }
}
