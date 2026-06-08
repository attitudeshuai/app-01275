package com.wms.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.dto.PurchaseOrderDTO;
import com.wms.entity.BizPurchaseItem;
import com.wms.entity.BizPurchaseOrder;
import com.wms.entity.SysUser;
import com.wms.mapper.BizPurchaseItemMapper;
import com.wms.mapper.BizPurchaseOrderMapper;
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
public class PurchaseService {

    private final BizPurchaseOrderMapper orderMapper;
    private final BizPurchaseItemMapper itemMapper;
    private final SysUserMapper userMapper;
    private final StockService stockService;

    public PageResult<BizPurchaseOrder> page(int page, int size, String orderNo, Long supplierId, Integer status) {
        IPage<BizPurchaseOrder> result = orderMapper.selectOrderPage(new Page<>(page, size), orderNo, supplierId, status, null);
        return PageResult.of(result);
    }

    public BizPurchaseOrder getById(Long id) {
        BizPurchaseOrder order = orderMapper.selectOrderById(id);
        if (order != null) {
            order.setItems(itemMapper.selectByOrderId(id));
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

    @Transactional
    public void save(PurchaseOrderDTO dto) {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setOrderNo("CG" + IdUtil.getSnowflakeNextIdStr());
        order.setSupplierId(dto.getSupplierId());
        order.setWarehouseId(dto.getWarehouseId());
        order.setRemark(dto.getRemark());
        order.setApplicantId(UserContext.getUserId());
        order.setDeptId(getCurrentUserDeptId());
        order.setStatus(0);
        order.setApplyTime(LocalDateTime.now());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderDTO.PurchaseItemDTO itemDto : dto.getItems()) {
            BigDecimal amount = itemDto.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(amount);
        }
        order.setTotalAmount(totalAmount);
        orderMapper.insert(order);
        
        for (PurchaseOrderDTO.PurchaseItemDTO itemDto : dto.getItems()) {
            BizPurchaseItem item = new BizPurchaseItem();
            item.setOrderId(order.getId());
            item.setGoodsId(itemDto.getGoodsId());
            item.setQuantity(itemDto.getQuantity());
            item.setPrice(itemDto.getPrice());
            item.setAmount(itemDto.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            itemMapper.insert(item);
        }
        log.info("创建采购单: {}", order.getOrderNo());
    }

    @Transactional
    public void update(PurchaseOrderDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("采购单ID不能为空");
        }
        BizPurchaseOrder existing = orderMapper.selectById(dto.getId());
        if (existing == null || existing.getStatus() != 0) {
            throw new BusinessException("采购单状态不允许修改");
        }
        
        itemMapper.delete(new LambdaQueryWrapper<BizPurchaseItem>().eq(BizPurchaseItem::getOrderId, dto.getId()));
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderDTO.PurchaseItemDTO itemDto : dto.getItems()) {
            BizPurchaseItem item = new BizPurchaseItem();
            item.setOrderId(dto.getId());
            item.setGoodsId(itemDto.getGoodsId());
            item.setQuantity(itemDto.getQuantity());
            item.setPrice(itemDto.getPrice());
            item.setAmount(itemDto.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            totalAmount = totalAmount.add(item.getAmount());
            itemMapper.insert(item);
        }
        
        existing.setSupplierId(dto.getSupplierId());
        existing.setWarehouseId(dto.getWarehouseId());
        existing.setRemark(dto.getRemark());
        existing.setTotalAmount(totalAmount);
        orderMapper.updateById(existing);
        log.info("更新采购单: {}", existing.getOrderNo());
    }

    @Transactional
    public void delete(Long id) {
        BizPurchaseOrder order = orderMapper.selectById(id);
        if (order == null || order.getStatus() != 0) {
            throw new BusinessException("采购单状态不允许删除");
        }
        orderMapper.deleteById(id);
        itemMapper.delete(new LambdaQueryWrapper<BizPurchaseItem>().eq(BizPurchaseItem::getOrderId, id));
        log.info("删除采购单: {}", id);
    }

    @Transactional
    public void approve(Long id, boolean approved, String remark) {
        BizPurchaseOrder order = orderMapper.selectById(id);
        if (order == null || order.getStatus() != 0) {
            throw new BusinessException("采购单状态异常");
        }
        order.setStatus(approved ? 1 : 2);
        order.setApproverId(UserContext.getUserId());
        order.setApproveTime(LocalDateTime.now());
        order.setApproveRemark(remark);
        orderMapper.updateById(order);
        log.info("审批采购单: {} - {}", order.getOrderNo(), approved ? "通过" : "拒绝");
    }

    @Transactional
    public void inbound(Long id) {
        BizPurchaseOrder order = orderMapper.selectById(id);
        if (order == null || order.getStatus() != 1) {
            throw new BusinessException("采购单状态异常，无法入库");
        }
        
        Long warehouseId = order.getWarehouseId();
        if (warehouseId == null) {
            throw new BusinessException("采购单未指定入库仓库");
        }
        
        List<BizPurchaseItem> items = itemMapper.selectByOrderId(id);
        for (BizPurchaseItem item : items) {
            stockService.updateStock(item.getGoodsId(), warehouseId, item.getQuantity(), 
                                    "IN", "PURCHASE", order.getId(), order.getOrderNo(), "采购入库");
        }
        
        order.setStatus(3);
        order.setInboundTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("采购入库: {}", order.getOrderNo());
    }
}
