package com.wms.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.BizPaymentRecord;
import com.wms.entity.BizSaleItem;
import com.wms.entity.BizSaleOrder;
import com.wms.entity.SysUser;
import com.wms.mapper.BizPaymentRecordMapper;
import com.wms.mapper.BizSaleItemMapper;
import com.wms.mapper.BizSaleOrderMapper;
import com.wms.mapper.BizGoodsMapper;
import com.wms.mapper.SysUserMapper;
import com.wms.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleService {

    private final BizSaleOrderMapper orderMapper;
    private final BizSaleItemMapper itemMapper;
    private final BizPaymentRecordMapper paymentRecordMapper;
    private final SysUserMapper userMapper;
    private final BizGoodsMapper goodsMapper;
    private final StockService stockService;

    public PageResult<BizSaleOrder> page(int page, int size, String orderNo, Long customerId, Integer status) {
        IPage<BizSaleOrder> result = orderMapper.selectOrderPage(new Page<>(page, size), orderNo, customerId, status, null);
        return PageResult.of(result);
    }

    public BizSaleOrder getById(Long id) {
        BizSaleOrder order = orderMapper.selectOrderById(id);
        if (order != null) {
            order.setItems(itemMapper.selectByOrderId(id));
            order.setPaymentRecords(paymentRecordMapper.selectByOrderId(id));
        }
        return order;
    }

    /**
     * 获取当前用户的部门ID
     */
    private Long getCurrentUserDeptId() {
        Long userId = UserContext.getUserId();
        SysUser user = userMapper.selectById(userId);
        return user != null ? user.getDeptId() : null;
    }

    /**
     * 创建销售报价单（状态为-1报价中）
     */
    @Transactional
    public void quote(BizSaleOrder order) {
        order.setOrderNo("BJ" + IdUtil.getSnowflakeNextIdStr());
        order.setSalesmanId(UserContext.getUserId());
        order.setDeptId(getCurrentUserDeptId());
        order.setStatus(-1); // 报价中状态
        order.setReceivedAmount(BigDecimal.ZERO);
        order.setQuoteTime(LocalDateTime.now());
        order.setCreateTime(LocalDateTime.now());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getItems() != null) {
            for (BizSaleItem item : order.getItems()) {
                item.setAmount(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                totalAmount = totalAmount.add(item.getAmount());
            }
        }
        order.setTotalAmount(totalAmount);
        orderMapper.insert(order);
        
        if (order.getItems() != null) {
            for (BizSaleItem item : order.getItems()) {
                item.setOrderId(order.getId());
                itemMapper.insert(item);
            }
        }
        log.info("创建销售报价单: {}", order.getOrderNo());
    }

    /**
     * 确认报价，转为销售单（状态从-1变为0待审核）
     */
    @Transactional
    public void confirmQuote(Long id) {
        BizSaleOrder order = orderMapper.selectById(id);
        if (order == null || order.getStatus() != -1) {
            throw new BusinessException("报价单状态异常，无法确认");
        }
        // 生成新的销售单号
        order.setOrderNo("XS" + IdUtil.getSnowflakeNextIdStr());
        order.setStatus(0); // 待审核
        order.setConfirmTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("确认报价转销售单: {}", order.getOrderNo());
    }

    /**
     * 直接创建销售单（跳过报价环节）
     */
    @Transactional
    public void save(BizSaleOrder order) {
        order.setOrderNo("XS" + IdUtil.getSnowflakeNextIdStr());
        order.setSalesmanId(UserContext.getUserId());
        order.setDeptId(getCurrentUserDeptId());
        order.setStatus(0);
        order.setReceivedAmount(BigDecimal.ZERO);
        order.setCreateTime(LocalDateTime.now());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getItems() != null) {
            for (BizSaleItem item : order.getItems()) {
                item.setAmount(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                totalAmount = totalAmount.add(item.getAmount());
            }
        }
        order.setTotalAmount(totalAmount);
        orderMapper.insert(order);
        
        if (order.getItems() != null) {
            for (BizSaleItem item : order.getItems()) {
                item.setOrderId(order.getId());
                itemMapper.insert(item);
            }
        }
        log.info("创建销售单: {}", order.getOrderNo());
    }

    @Transactional
    public void update(BizSaleOrder order) {
        BizSaleOrder existing = orderMapper.selectById(order.getId());
        if (existing == null || (existing.getStatus() != 0 && existing.getStatus() != -1)) {
            throw new BusinessException("销售单状态不允许修改");
        }
        
        itemMapper.delete(new LambdaQueryWrapper<BizSaleItem>().eq(BizSaleItem::getOrderId, order.getId()));
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getItems() != null) {
            for (BizSaleItem item : order.getItems()) {
                item.setAmount(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                totalAmount = totalAmount.add(item.getAmount());
                item.setOrderId(order.getId());
                itemMapper.insert(item);
            }
        }
        order.setTotalAmount(totalAmount);
        orderMapper.updateById(order);
        log.info("更新销售单: {}", order.getOrderNo());
    }

    @Transactional
    public void delete(Long id) {
        BizSaleOrder order = orderMapper.selectById(id);
        if (order == null || (order.getStatus() != 0 && order.getStatus() != -1)) {
            throw new BusinessException("销售单状态不允许删除");
        }
        orderMapper.deleteById(id);
        itemMapper.delete(new LambdaQueryWrapper<BizSaleItem>().eq(BizSaleItem::getOrderId, id));
        log.info("删除销售单: {}", id);
    }

    @Transactional
    public void approve(Long id, boolean approved) {
        BizSaleOrder order = orderMapper.selectById(id);
        if (order == null || order.getStatus() != 0) {
            throw new BusinessException("销售单状态异常");
        }
        order.setStatus(approved ? 1 : 4);
        order.setApproverId(UserContext.getUserId());
        order.setApproveTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("审核销售单: {} - {}", order.getOrderNo(), approved ? "通过" : "取消");
    }

    @Transactional
    public void ship(Long id, Long warehouseId) {
        BizSaleOrder order = orderMapper.selectById(id);
        if (order == null || order.getStatus() != 1) {
            throw new BusinessException("销售单状态异常，无法发货");
        }
        
        List<BizSaleItem> items = itemMapper.selectByOrderId(id);
        for (BizSaleItem item : items) {
            // 记录发货时的成本价（从商品表获取当前采购价）
            com.wms.entity.BizGoods goods = goodsMapper.selectById(item.getGoodsId());
            if (goods != null) {
                item.setCostPrice(goods.getPurchasePrice());
                itemMapper.updateById(item);
            }
            
            stockService.updateStock(item.getGoodsId(), warehouseId, item.getQuantity(), 
                                    "OUT", "SALE", order.getId(), order.getOrderNo(), "销售出库");
        }
        
        order.setStatus(2);
        order.setWarehouseId(warehouseId);
        order.setShipTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("销售发货: {}", order.getOrderNo());
    }

    /**
     * 收款（支持分多次收款，每次收款生成收款记录）
     */
    @Transactional
    public void receive(Long id, BigDecimal amount, String paymentMethod, String remark) {
        BizSaleOrder order = orderMapper.selectById(id);
        if (order == null || order.getStatus() != 2) {
            throw new BusinessException("销售单状态异常，无法收款");
        }
        
        // 创建收款记录
        BizPaymentRecord record = new BizPaymentRecord();
        record.setPaymentNo("SK" + IdUtil.getSnowflakeNextIdStr());
        record.setOrderId(id);
        record.setOrderNo(order.getOrderNo());
        record.setAmount(amount);
        record.setPaymentMethod(paymentMethod != null ? paymentMethod : "BANK");
        record.setRemark(remark);
        record.setOperatorId(UserContext.getUserId());
        record.setCreateTime(LocalDateTime.now());
        paymentRecordMapper.insert(record);
        
        // 更新销售单已收金额
        BigDecimal newReceived = order.getReceivedAmount().add(amount);
        order.setReceivedAmount(newReceived);
        
        if (newReceived.compareTo(order.getTotalAmount()) >= 0) {
            order.setStatus(3);
            order.setReceiveTime(LocalDateTime.now());
        }
        orderMapper.updateById(order);
        log.info("销售收款: {} - {} (收款单号: {})", order.getOrderNo(), amount, record.getPaymentNo());
    }

    /**
     * 获取销售单的收款记录
     */
    public List<BizPaymentRecord> getPaymentRecords(Long orderId) {
        return paymentRecordMapper.selectByOrderId(orderId);
    }
}
