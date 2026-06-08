package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.BizPurchaseItem;
import com.wms.entity.BizPurchaseOrder;
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
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

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

    @Test
    void page_ShouldReturnPageResult() {
        Page<BizPurchaseOrder> page = new Page<>(1, 10);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);

        when(orderMapper.selectOrderPage(any(IPage.class), isNull(), isNull(), isNull(), isNull())).thenReturn(page);

        PageResult<BizPurchaseOrder> result = purchaseService.page(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    void getById_ShouldReturnOrderWithItems() {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setOrderNo("CG001");

        BizPurchaseItem item = new BizPurchaseItem();
        item.setId(1L);
        item.setOrderId(1L);

        when(orderMapper.selectOrderById(1L)).thenReturn(order);
        when(itemMapper.selectByOrderId(1L)).thenReturn(Arrays.asList(item));

        BizPurchaseOrder result = purchaseService.getById(1L);

        assertNotNull(result);
        assertEquals("CG001", result.getOrderNo());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void save_ShouldCreateOrder() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            com.wms.dto.PurchaseOrderDTO dto = new com.wms.dto.PurchaseOrderDTO();
            dto.setSupplierId(1L);
            dto.setWarehouseId(1L);
            
            com.wms.dto.PurchaseOrderDTO.PurchaseItemDTO item = new com.wms.dto.PurchaseOrderDTO.PurchaseItemDTO();
            item.setGoodsId(1L);
            item.setQuantity(10);
            item.setPrice(new BigDecimal("100"));
            dto.setItems(Arrays.asList(item));

            when(orderMapper.insert(any(BizPurchaseOrder.class))).thenReturn(1);
            when(itemMapper.insert(any(BizPurchaseItem.class))).thenReturn(1);

            assertDoesNotThrow(() -> purchaseService.save(dto));
            verify(orderMapper).insert(any(BizPurchaseOrder.class));
        }
    }

    @Test
    void approve_WithValidOrder_ShouldApprove() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizPurchaseOrder order = new BizPurchaseOrder();
            order.setId(1L);
            order.setStatus(0);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizPurchaseOrder.class))).thenReturn(1);

            assertDoesNotThrow(() -> purchaseService.approve(1L, true, "同意"));
            verify(orderMapper).updateById(any(BizPurchaseOrder.class));
        }
    }

    @Test
    void approve_WithInvalidStatus_ShouldThrowException() {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setStatus(1); // Already approved

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.approve(1L, true, "同意"));
    }

    @Test
    void inbound_WithApprovedOrder_ShouldInbound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizPurchaseOrder order = new BizPurchaseOrder();
            order.setId(1L);
            order.setOrderNo("CG001");
            order.setStatus(1); // Approved
            order.setWarehouseId(1L); // Set warehouse ID

            BizPurchaseItem item = new BizPurchaseItem();
            item.setGoodsId(1L);
            item.setQuantity(10);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(itemMapper.selectByOrderId(1L)).thenReturn(Arrays.asList(item));
            when(orderMapper.updateById(any(BizPurchaseOrder.class))).thenReturn(1);

            assertDoesNotThrow(() -> purchaseService.inbound(1L));
            verify(stockService).updateStock(eq(1L), eq(1L), eq(10), eq("IN"), eq("PURCHASE"), eq(1L), eq("CG001"), anyString());
        }
    }

    @Test
    void inbound_WithInvalidStatus_ShouldThrowException() {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setStatus(0); // Not approved

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.inbound(1L));
    }

    @Test
    void inbound_WithNoWarehouse_ShouldThrowException() {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setStatus(1); // Approved
        order.setWarehouseId(null); // No warehouse

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.inbound(1L));
    }

    @Test
    void delete_WithPendingOrder_ShouldDelete() {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setStatus(0);

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.deleteById(1L)).thenReturn(1);
        when(itemMapper.delete(any())).thenReturn(1);

        assertDoesNotThrow(() -> purchaseService.delete(1L));
        verify(orderMapper).deleteById(1L);
    }

    @Test
    void delete_WithApprovedOrder_ShouldThrowException() {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setStatus(1);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.delete(1L));
    }

    @Test
    void approve_WithRejectedStatus_ShouldThrowException() {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setStatus(2);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.approve(1L, true, "同意"));
    }

    @Test
    void approve_WithInboundStatus_ShouldThrowException() {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setStatus(3);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.approve(1L, true, "同意"));
    }

    @Test
    void approve_Rejected_ShouldSetStatusTo2() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizPurchaseOrder order = new BizPurchaseOrder();
            order.setId(1L);
            order.setStatus(0);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizPurchaseOrder.class))).thenReturn(1);

            purchaseService.approve(1L, false, "价格过高");

            verify(orderMapper).updateById(argThat(o -> 
                ((BizPurchaseOrder)o).getStatus() == 2 && 
                "价格过高".equals(((BizPurchaseOrder)o).getApproveRemark())
            ));
        }
    }

    @Test
    void inbound_WithRejectedStatus_ShouldThrowException() {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setStatus(2);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.inbound(1L));
    }

    @Test
    void inbound_WithAlreadyInbound_ShouldThrowException() {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setStatus(3);
        order.setWarehouseId(1L);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.inbound(1L));
    }

    @Test
    void update_WithPendingOrder_ShouldUpdate() {
        com.wms.dto.PurchaseOrderDTO dto = new com.wms.dto.PurchaseOrderDTO();
        dto.setId(1L);
        dto.setSupplierId(2L);
        dto.setWarehouseId(2L);
        dto.setRemark("更新备注");

        com.wms.dto.PurchaseOrderDTO.PurchaseItemDTO item = new com.wms.dto.PurchaseOrderDTO.PurchaseItemDTO();
        item.setGoodsId(1L);
        item.setQuantity(20);
        item.setPrice(new BigDecimal("150"));
        dto.setItems(Arrays.asList(item));

        BizPurchaseOrder existing = new BizPurchaseOrder();
        existing.setId(1L);
        existing.setStatus(0);

        when(orderMapper.selectById(1L)).thenReturn(existing);
        when(itemMapper.delete(any())).thenReturn(1);
        when(itemMapper.insert(any(BizPurchaseItem.class))).thenReturn(1);
        when(orderMapper.updateById(any(BizPurchaseOrder.class))).thenReturn(1);

        assertDoesNotThrow(() -> purchaseService.update(dto));
        verify(orderMapper).updateById(argThat(o -> 
            ((BizPurchaseOrder)o).getSupplierId() == 2L &&
            ((BizPurchaseOrder)o).getTotalAmount().compareTo(new BigDecimal("3000")) == 0
        ));
    }

    @Test
    void update_WithApprovedOrder_ShouldThrowException() {
        com.wms.dto.PurchaseOrderDTO dto = new com.wms.dto.PurchaseOrderDTO();
        dto.setId(1L);

        BizPurchaseOrder existing = new BizPurchaseOrder();
        existing.setId(1L);
        existing.setStatus(1);

        when(orderMapper.selectById(1L)).thenReturn(existing);

        assertThrows(BusinessException.class, () -> purchaseService.update(dto));
    }

    @Test
    void update_WithNullId_ShouldThrowException() {
        com.wms.dto.PurchaseOrderDTO dto = new com.wms.dto.PurchaseOrderDTO();
        dto.setId(null);

        assertThrows(BusinessException.class, () -> purchaseService.update(dto));
    }

    @Test
    void delete_WithRejectedOrder_ShouldThrowException() {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setStatus(2);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.delete(1L));
    }

    @Test
    void delete_WithInboundOrder_ShouldThrowException() {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setId(1L);
        order.setStatus(3);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> purchaseService.delete(1L));
    }
}
