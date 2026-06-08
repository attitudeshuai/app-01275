package com.wms.service;

import com.wms.common.BusinessException;
import com.wms.dto.PurchaseOrderDTO;
import com.wms.entity.BizPurchaseItem;
import com.wms.entity.BizPurchaseOrder;
import com.wms.entity.SysUser;
import com.wms.mapper.BizPurchaseItemMapper;
import com.wms.mapper.BizPurchaseOrderMapper;
import com.wms.mapper.SysUserMapper;
import com.wms.util.UserContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderFlowTest {

    @Mock
    private BizPurchaseOrderMapper orderMapper;
    @Mock
    private BizPurchaseItemMapper itemMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private StockService stockService;

    @InjectMocks
    private PurchaseService purchaseService;

    private BizPurchaseOrder buildOrder(Integer status) {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setOrderNo("CG001");
        order.setSupplierId(1L);
        order.setWarehouseId(1L);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("1000.00"));
        return order;
    }

    private BizPurchaseItem buildItem(Long goodsId, int quantity, BigDecimal price) {
        BizPurchaseItem item = new BizPurchaseItem();
        item.setId(1L);
        item.setOrderId(1L);
        item.setGoodsId(goodsId);
        item.setQuantity(quantity);
        item.setPrice(price);
        item.setAmount(price.multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    private PurchaseOrderDTO buildDTO() {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setSupplierId(1L);
        dto.setWarehouseId(1L);
        dto.setRemark("测试");

        PurchaseOrderDTO.PurchaseItemDTO item = new PurchaseOrderDTO.PurchaseItemDTO();
        item.setGoodsId(1L);
        item.setQuantity(10);
        item.setPrice(new BigDecimal("100.00"));
        dto.setItems(Arrays.asList(item));

        return dto;
    }

    @Test
    void approve_ShouldSetStatusToApproved_WhenOrderIsPending() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);

            BizPurchaseOrder order = buildOrder(0);
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizPurchaseOrder.class))).thenReturn(1);

            purchaseService.approve(1L, true, "同意");

            verify(orderMapper).updateById(argThat(o -> {
                BizPurchaseOrder po = (BizPurchaseOrder) o;
                return po.getStatus() == 1
                        && po.getApproverId().equals(1L)
                        && po.getApproveRemark().equals("同意")
                        && po.getApproveTime() != null;
            }));
        }
    }

    @Test
    void approve_ShouldSetStatusToRejected_WhenDisapproved() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);

            BizPurchaseOrder order = buildOrder(0);
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizPurchaseOrder.class))).thenReturn(1);

            purchaseService.approve(1L, false, "预算不足");

            verify(orderMapper).updateById(argThat(o -> {
                BizPurchaseOrder po = (BizPurchaseOrder) o;
                return po.getStatus() == 2
                        && po.getApproveRemark().equals("预算不足");
            }));
        }
    }

    @Test
    void approve_ShouldReject_WhenStatusIsApproved() {
        BizPurchaseOrder order = buildOrder(1);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.approve(1L, true, "再次审批"));
        verify(orderMapper, never()).updateById(any());
    }

    @Test
    void approve_ShouldReject_WhenStatusIsRejected() {
        BizPurchaseOrder order = buildOrder(2);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.approve(1L, true, "重新审批"));
        verify(orderMapper, never()).updateById(any());
    }

    @Test
    void approve_ShouldReject_WhenStatusIsInbounded() {
        BizPurchaseOrder order = buildOrder(3);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.approve(1L, true, "审批"));
        verify(orderMapper, never()).updateById(any());
    }

    @Test
    void approve_ShouldReject_WhenOrderNotFound() {
        when(orderMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> purchaseService.approve(1L, true, "审批"));
    }

    @Test
    void inbound_ShouldSetStatusToInbounded_WhenOrderIsApproved() {
        BizPurchaseOrder order = buildOrder(1);
        BizPurchaseItem item = buildItem(1L, 10, new BigDecimal("100.00"));

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(itemMapper.selectByOrderId(1L)).thenReturn(Arrays.asList(item));
        when(orderMapper.updateById(any(BizPurchaseOrder.class))).thenReturn(1);

        purchaseService.inbound(1L);

        verify(stockService).updateStock(eq(1L), eq(1L), eq(10), eq("IN"), eq("PURCHASE"), eq(1L), eq("CG001"), anyString());
        verify(orderMapper).updateById(argThat(o -> {
            BizPurchaseOrder po = (BizPurchaseOrder) o;
            return po.getStatus() == 3 && po.getInboundTime() != null;
        }));
    }

    @Test
    void inbound_ShouldReject_WhenStatusIsPending() {
        BizPurchaseOrder order = buildOrder(0);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.inbound(1L));
        verify(stockService, never()).updateStock(anyLong(), anyLong(), anyInt(), anyString(), anyString(), anyLong(), anyString(), anyString());
    }

    @Test
    void inbound_ShouldReject_WhenStatusIsRejected() {
        BizPurchaseOrder order = buildOrder(2);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.inbound(1L));
    }

    @Test
    void inbound_ShouldReject_WhenStatusIsAlreadyInbounded() {
        BizPurchaseOrder order = buildOrder(3);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.inbound(1L));
    }

    @Test
    void inbound_ShouldReject_WhenWarehouseIsNull() {
        BizPurchaseOrder order = buildOrder(1);
        order.setWarehouseId(null);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.inbound(1L));
    }

    @Test
    void inbound_ShouldCallStockServiceForEachItem() {
        BizPurchaseOrder order = buildOrder(1);
        BizPurchaseItem item1 = buildItem(1L, 10, new BigDecimal("100.00"));
        BizPurchaseItem item2 = buildItem(2L, 5, new BigDecimal("200.00"));

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(itemMapper.selectByOrderId(1L)).thenReturn(Arrays.asList(item1, item2));
        when(orderMapper.updateById(any(BizPurchaseOrder.class))).thenReturn(1);

        purchaseService.inbound(1L);

        verify(stockService).updateStock(eq(1L), eq(1L), eq(10), eq("IN"), eq("PURCHASE"), eq(1L), eq("CG001"), anyString());
        verify(stockService).updateStock(eq(2L), eq(1L), eq(5), eq("IN"), eq("PURCHASE"), eq(1L), eq("CG001"), anyString());
    }

    @Test
    void save_ShouldCalculateTotalAmountCorrectly() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(new SysUser());
            when(orderMapper.insert(any(BizPurchaseOrder.class))).thenReturn(1);
            when(itemMapper.insert(any(BizPurchaseItem.class))).thenReturn(1);

            PurchaseOrderDTO dto = new PurchaseOrderDTO();
            dto.setSupplierId(1L);
            dto.setWarehouseId(1L);

            PurchaseOrderDTO.PurchaseItemDTO i1 = new PurchaseOrderDTO.PurchaseItemDTO();
            i1.setGoodsId(1L);
            i1.setQuantity(5);
            i1.setPrice(new BigDecimal("100.00"));

            PurchaseOrderDTO.PurchaseItemDTO i2 = new PurchaseOrderDTO.PurchaseItemDTO();
            i2.setGoodsId(2L);
            i2.setQuantity(3);
            i2.setPrice(new BigDecimal("200.00"));

            dto.setItems(Arrays.asList(i1, i2));

            purchaseService.save(dto);

            verify(orderMapper).insert(argThat(o -> {
                BizPurchaseOrder order = (BizPurchaseOrder) o;
                return order.getTotalAmount().compareTo(new BigDecimal("1100.00")) == 0
                        && order.getStatus() == 0
                        && order.getOrderNo() != null
                        && order.getOrderNo().startsWith("CG");
            }));
        }
    }

    @Test
    void save_ShouldSetInitialStatusToPending() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(new SysUser());
            when(orderMapper.insert(any(BizPurchaseOrder.class))).thenReturn(1);
            when(itemMapper.insert(any(BizPurchaseItem.class))).thenReturn(1);

            purchaseService.save(buildDTO());

            verify(orderMapper).insert(argThat(o -> {
                BizPurchaseOrder order = (BizPurchaseOrder) o;
                return order.getStatus() == 0
                        && order.getApplicantId().equals(1L)
                        && order.getApplyTime() != null;
            }));
        }
    }

    @Test
    void save_ShouldCalculateItemAmountAsPriceTimesQuantity() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(new SysUser());
            when(orderMapper.insert(any(BizPurchaseOrder.class))).thenReturn(1);
            when(itemMapper.insert(any(BizPurchaseItem.class))).thenReturn(1);

            purchaseService.save(buildDTO());

            verify(itemMapper).insert(argThat(item -> {
                BizPurchaseItem pi = (BizPurchaseItem) item;
                return pi.getAmount().compareTo(new BigDecimal("1000.00")) == 0;
            }));
        }
    }

    @Test
    void update_ShouldReject_WhenStatusIsApproved() {
        BizPurchaseOrder order = buildOrder(1);
        when(orderMapper.selectById(1L)).thenReturn(order);

        PurchaseOrderDTO dto = buildDTO();
        dto.setId(1L);

        assertThrows(BusinessException.class, () -> purchaseService.update(dto));
        verify(orderMapper, never()).updateById(any());
    }

    @Test
    void update_ShouldReject_WhenStatusIsRejected() {
        BizPurchaseOrder order = buildOrder(2);
        when(orderMapper.selectById(1L)).thenReturn(order);

        PurchaseOrderDTO dto = buildDTO();
        dto.setId(1L);

        assertThrows(BusinessException.class, () -> purchaseService.update(dto));
    }

    @Test
    void update_ShouldReject_WhenStatusIsInbounded() {
        BizPurchaseOrder order = buildOrder(3);
        when(orderMapper.selectById(1L)).thenReturn(order);

        PurchaseOrderDTO dto = buildDTO();
        dto.setId(1L);

        assertThrows(BusinessException.class, () -> purchaseService.update(dto));
    }

    @Test
    void update_ShouldReject_WhenIdIsNull() {
        PurchaseOrderDTO dto = buildDTO();
        dto.setId(null);

        assertThrows(BusinessException.class, () -> purchaseService.update(dto));
    }

    @Test
    void update_ShouldRecalculateTotalAmount() {
        BizPurchaseOrder order = buildOrder(0);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(itemMapper.delete(any())).thenReturn(1);
        when(itemMapper.insert(any(BizPurchaseItem.class))).thenReturn(1);
        when(orderMapper.updateById(any(BizPurchaseOrder.class))).thenReturn(1);

        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setId(1L);
        dto.setSupplierId(1L);
        dto.setWarehouseId(1L);

        PurchaseOrderDTO.PurchaseItemDTO i1 = new PurchaseOrderDTO.PurchaseItemDTO();
        i1.setGoodsId(1L);
        i1.setQuantity(2);
        i1.setPrice(new BigDecimal("150.00"));

        PurchaseOrderDTO.PurchaseItemDTO i2 = new PurchaseOrderDTO.PurchaseItemDTO();
        i2.setGoodsId(2L);
        i2.setQuantity(4);
        i2.setPrice(new BigDecimal("50.00"));

        dto.setItems(Arrays.asList(i1, i2));

        purchaseService.update(dto);

        verify(orderMapper).updateById(argThat(o -> {
            BizPurchaseOrder updated = (BizPurchaseOrder) o;
            return updated.getTotalAmount().compareTo(new BigDecimal("500.00")) == 0;
        }));
    }

    @Test
    void delete_ShouldReject_WhenStatusIsNotPending() {
        BizPurchaseOrder order = buildOrder(1);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.delete(1L));
        verify(orderMapper, never()).deleteById(anyLong());
    }

    @Test
    void delete_ShouldReject_WhenStatusIsRejected() {
        BizPurchaseOrder order = buildOrder(2);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.delete(1L));
    }

    @Test
    void delete_ShouldReject_WhenStatusIsInbounded() {
        BizPurchaseOrder order = buildOrder(3);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.delete(1L));
    }

    @Test
    void delete_ShouldRemoveOrderAndItems_WhenStatusIsPending() {
        BizPurchaseOrder order = buildOrder(0);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.deleteById(1L)).thenReturn(1);
        when(itemMapper.delete(any())).thenReturn(1);

        assertDoesNotThrow(() -> purchaseService.delete(1L));
        verify(orderMapper).deleteById(1L);
        verify(itemMapper).delete(any());
    }
}
