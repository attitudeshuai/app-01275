package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.BizPaymentRecord;
import com.wms.entity.BizSaleItem;
import com.wms.entity.BizSaleOrder;
import com.wms.mapper.BizGoodsMapper;
import com.wms.mapper.BizPaymentRecordMapper;
import com.wms.mapper.BizSaleItemMapper;
import com.wms.mapper.BizSaleOrderMapper;
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
class SaleServiceTest {

    @Mock
    private BizSaleOrderMapper orderMapper;
    @Mock
    private BizSaleItemMapper itemMapper;
    @Mock
    private BizPaymentRecordMapper paymentRecordMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private BizGoodsMapper goodsMapper;
    @Mock
    private StockService stockService;

    @InjectMocks
    private SaleService saleService;

    @Test
    void page_ShouldReturnPageResult() {
        Page<BizSaleOrder> page = new Page<>(1, 10);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);

        when(orderMapper.selectOrderPage(any(IPage.class), isNull(), isNull(), isNull(), isNull())).thenReturn(page);

        PageResult<BizSaleOrder> result = saleService.page(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    void getById_ShouldReturnOrderWithItems() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setOrderNo("XS001");

        BizSaleItem item = new BizSaleItem();
        item.setId(1L);
        item.setOrderId(1L);

        when(orderMapper.selectOrderById(1L)).thenReturn(order);
        when(itemMapper.selectByOrderId(1L)).thenReturn(Arrays.asList(item));

        BizSaleOrder result = saleService.getById(1L);

        assertNotNull(result);
        assertEquals("XS001", result.getOrderNo());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void save_ShouldCreateOrder() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setCustomerId(1L);
            
            BizSaleItem item = new BizSaleItem();
            item.setGoodsId(1L);
            item.setQuantity(10);
            item.setPrice(new BigDecimal("100"));
            order.setItems(Arrays.asList(item));

            when(orderMapper.insert(any(BizSaleOrder.class))).thenReturn(1);
            when(itemMapper.insert(any(BizSaleItem.class))).thenReturn(1);

            assertDoesNotThrow(() -> saleService.save(order));
            verify(orderMapper).insert(any(BizSaleOrder.class));
        }
    }

    @Test
    void approve_WithValidOrder_ShouldApprove() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setId(1L);
            order.setStatus(0);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            assertDoesNotThrow(() -> saleService.approve(1L, true));
            verify(orderMapper).updateById(any(BizSaleOrder.class));
        }
    }

    @Test
    void approve_WithInvalidStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(1);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.approve(1L, true));
    }

    @Test
    void ship_WithApprovedOrder_ShouldShip() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setId(1L);
            order.setOrderNo("XS001");
            order.setStatus(1);

            BizSaleItem item = new BizSaleItem();
            item.setId(1L);
            item.setGoodsId(1L);
            item.setQuantity(10);

            com.wms.entity.BizGoods goods = new com.wms.entity.BizGoods();
            goods.setId(1L);
            goods.setPurchasePrice(new BigDecimal("80"));

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(itemMapper.selectByOrderId(1L)).thenReturn(Arrays.asList(item));
            when(goodsMapper.selectById(1L)).thenReturn(goods);
            when(itemMapper.updateById(any(BizSaleItem.class))).thenReturn(1);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            assertDoesNotThrow(() -> saleService.ship(1L, 1L));
            verify(stockService).updateStock(eq(1L), eq(1L), eq(10), eq("OUT"), eq("SALE"), eq(1L), eq("XS001"), anyString());
        }
    }

    @Test
    void ship_WithInvalidStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(0);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.ship(1L, 1L));
    }

    @Test
    void receive_WithShippedOrder_ShouldReceive() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setId(1L);
            order.setOrderNo("XS001");
            order.setStatus(2);
            order.setTotalAmount(new BigDecimal("1000"));
            order.setReceivedAmount(BigDecimal.ZERO);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            assertDoesNotThrow(() -> saleService.receive(1L, new BigDecimal("500"), "BANK", "部分收款"));
            verify(orderMapper).updateById(any(BizSaleOrder.class));
        }
    }

    @Test
    void receive_FullPayment_ShouldCompleteOrder() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setId(1L);
            order.setOrderNo("XS001");
            order.setStatus(2);
            order.setTotalAmount(new BigDecimal("1000"));
            order.setReceivedAmount(BigDecimal.ZERO);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            assertDoesNotThrow(() -> saleService.receive(1L, new BigDecimal("1000"), "CASH", "全额收款"));
            verify(orderMapper).updateById(argThat(o -> ((BizSaleOrder)o).getStatus() == 3));
        }
    }

    @Test
    void delete_WithPendingOrder_ShouldDelete() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(0);

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.deleteById(1L)).thenReturn(1);
        when(itemMapper.delete(any())).thenReturn(1);

        assertDoesNotThrow(() -> saleService.delete(1L));
        verify(orderMapper).deleteById(1L);
    }

    @Test
    void quote_ShouldCreateQuoteOrder() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setCustomerId(1L);
            
            BizSaleItem item = new BizSaleItem();
            item.setGoodsId(1L);
            item.setQuantity(10);
            item.setPrice(new BigDecimal("100"));
            order.setItems(Arrays.asList(item));

            when(orderMapper.insert(any(BizSaleOrder.class))).thenReturn(1);
            when(itemMapper.insert(any(BizSaleItem.class))).thenReturn(1);

            assertDoesNotThrow(() -> saleService.quote(order));
            verify(orderMapper).insert(argThat(o -> ((BizSaleOrder)o).getStatus() == -1));
        }
    }

    @Test
    void confirmQuote_WithQuoteStatus_ShouldConfirm() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(-1);
        order.setOrderNo("BJ001");

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

        assertDoesNotThrow(() -> saleService.confirmQuote(1L));
        verify(orderMapper).updateById(argThat(o -> 
            ((BizSaleOrder)o).getStatus() == 0 && 
            ((BizSaleOrder)o).getOrderNo().startsWith("XS")
        ));
    }

    @Test
    void confirmQuote_WithPendingStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(0);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.confirmQuote(1L));
    }

    @Test
    void confirmQuote_WithApprovedStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(1);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.confirmQuote(1L));
    }

    @Test
    void approve_Cancelled_ShouldSetStatusTo4() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setId(1L);
            order.setStatus(0);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.approve(1L, false);

            verify(orderMapper).updateById(argThat(o -> ((BizSaleOrder)o).getStatus() == 4));
        }
    }

    @Test
    void approve_WithQuoteStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(-1);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.approve(1L, true));
    }

    @Test
    void approve_WithCancelledStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(4);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.approve(1L, true));
    }

    @Test
    void ship_WithCancelledStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(4);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.ship(1L, 1L));
    }

    @Test
    void ship_WithAlreadyShipped_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(2);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.ship(1L, 1L));
    }

    @Test
    void receive_PartialPayment_ShouldKeepStatusShipped() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setId(1L);
            order.setOrderNo("XS001");
            order.setStatus(2);
            order.setTotalAmount(new BigDecimal("1000"));
            order.setReceivedAmount(new BigDecimal("300"));

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);
            when(paymentRecordMapper.insert(any(BizPaymentRecord.class))).thenReturn(1);

            saleService.receive(1L, new BigDecimal("300"), "BANK", "第二次收款");

            verify(orderMapper).updateById(argThat(o -> 
                ((BizSaleOrder)o).getStatus() == 2 &&
                ((BizSaleOrder)o).getReceivedAmount().compareTo(new BigDecimal("600")) == 0
            ));
        }
    }

    @Test
    void receive_WithReceivedStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(3);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> 
            saleService.receive(1L, new BigDecimal("100"), "BANK", "测试"));
    }

    @Test
    void receive_WithCancelledStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(4);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> 
            saleService.receive(1L, new BigDecimal("100"), "BANK", "测试"));
    }

    @Test
    void update_WithQuoteStatus_ShouldUpdate() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setCustomerId(2L);

        BizSaleItem item = new BizSaleItem();
        item.setGoodsId(1L);
        item.setQuantity(15);
        item.setPrice(new BigDecimal("120"));
        order.setItems(Arrays.asList(item));

        BizSaleOrder existing = new BizSaleOrder();
        existing.setId(1L);
        existing.setStatus(-1);

        when(orderMapper.selectById(1L)).thenReturn(existing);
        when(itemMapper.delete(any())).thenReturn(1);
        when(itemMapper.insert(any(BizSaleItem.class))).thenReturn(1);
        when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

        assertDoesNotThrow(() -> saleService.update(order));
        verify(orderMapper).updateById(argThat(o -> 
            ((BizSaleOrder)o).getCustomerId() == 2L &&
            ((BizSaleOrder)o).getTotalAmount().compareTo(new BigDecimal("1800")) == 0
        ));
    }

    @Test
    void update_WithApprovedStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);

        BizSaleOrder existing = new BizSaleOrder();
        existing.setId(1L);
        existing.setStatus(1);

        when(orderMapper.selectById(1L)).thenReturn(existing);

        assertThrows(BusinessException.class, () -> saleService.update(order));
    }

    @Test
    void delete_WithQuoteStatus_ShouldDelete() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(-1);

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.deleteById(1L)).thenReturn(1);
        when(itemMapper.delete(any())).thenReturn(1);

        assertDoesNotThrow(() -> saleService.delete(1L));
        verify(orderMapper).deleteById(1L);
    }

    @Test
    void delete_WithApprovedStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(1);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.delete(1L));
    }

    @Test
    void delete_WithShippedStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(2);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.delete(1L));
    }

    @Test
    void delete_WithReceivedStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(3);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.delete(1L));
    }

    @Test
    void delete_WithCancelledStatus_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(4);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.delete(1L));
    }

    @Test
    void getPaymentRecords_ShouldReturnRecords() {
        BizPaymentRecord record1 = new BizPaymentRecord();
        record1.setId(1L);
        record1.setOrderId(1L);
        record1.setAmount(new BigDecimal("500"));

        BizPaymentRecord record2 = new BizPaymentRecord();
        record2.setId(2L);
        record2.setOrderId(1L);
        record2.setAmount(new BigDecimal("500"));

        when(paymentRecordMapper.selectByOrderId(1L)).thenReturn(Arrays.asList(record1, record2));

        var result = saleService.getPaymentRecords(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
