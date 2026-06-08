package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
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
    void quote_ShouldCreateQuoteOrderWithStatusMinus1() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setCustomerId(1L);
            BizSaleItem item = new BizSaleItem();
            item.setGoodsId(1L);
            item.setQuantity(10);
            item.setPrice(new BigDecimal("100"));
            order.setItems(Arrays.asList(item));

            when(orderMapper.insert(any(BizSaleOrder.class))).thenAnswer(invocation -> {
                BizSaleOrder o = invocation.getArgument(0);
                o.setId(1L);
                return 1;
            });
            when(itemMapper.insert(any(BizSaleItem.class))).thenReturn(1);

            saleService.quote(order);

            verify(orderMapper).insert(argThat(o -> o.getStatus() == -1 && o.getOrderNo().startsWith("BJ")));
        }
    }

    @Test
    void statusFlow_QuoteToPendingReview_ShouldSetStatusTo0() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setId(1L);
            order.setStatus(-1);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.confirmQuote(1L);

            verify(orderMapper).updateById(argThat(o -> o.getStatus() == 0 && o.getOrderNo().startsWith("XS")));
        }
    }

    @Test
    void statusFlow_PendingReviewToApproved_ShouldSetStatusTo1() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setId(1L);
            order.setStatus(0);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.approve(1L, true);

            verify(orderMapper).updateById(argThat(o -> o.getStatus() == 1));
        }
    }

    @Test
    void statusFlow_PendingReviewToCancelled_ShouldSetStatusTo4() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setId(1L);
            order.setStatus(0);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.approve(1L, false);

            verify(orderMapper).updateById(argThat(o -> o.getStatus() == 4));
        }
    }

    @Test
    void statusFlow_ApprovedToShipped_ShouldSetStatusTo2() {
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

            saleService.ship(1L, 1L);

            verify(orderMapper).updateById(argThat(o -> o.getStatus() == 2 && o.getWarehouseId() == 1L && o.getShipTime() != null));
            verify(stockService).updateStock(eq(1L), eq(1L), eq(10), eq("OUT"), eq("SALE"), eq(1L), eq("XS001"), anyString());
        }
    }

    @Test
    void statusFlow_ShippedToReceived_ShouldSetStatusTo3() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setId(1L);
            order.setOrderNo("XS001");
            order.setStatus(2);
            order.setTotalAmount(new BigDecimal("1000"));
            order.setReceivedAmount(BigDecimal.ZERO);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(paymentRecordMapper.insert(any(com.wms.entity.BizPaymentRecord.class))).thenReturn(1);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.receive(1L, new BigDecimal("1000"), "CASH", "全额收款");

            verify(paymentRecordMapper).insert(any());
            verify(orderMapper).updateById(argThat(o -> o.getStatus() == 3 && o.getReceivedAmount().compareTo(new BigDecimal("1000")) == 0 && o.getReceiveTime() != null));
        }
    }

    @Test
    void statusFlow_PartialPayment_ShouldKeepStatusAs2() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setId(1L);
            order.setOrderNo("XS001");
            order.setStatus(2);
            order.setTotalAmount(new BigDecimal("1000"));
            order.setReceivedAmount(BigDecimal.ZERO);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(paymentRecordMapper.insert(any(com.wms.entity.BizPaymentRecord.class))).thenReturn(1);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.receive(1L, new BigDecimal("500"), "BANK", "部分收款");

            verify(orderMapper).updateById(argThat(o -> o.getStatus() == 2 && o.getReceivedAmount().compareTo(new BigDecimal("500")) == 0));
        }
    }

    @Test
    void statusFlow_CancelledOrderCannotShip_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(4);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.ship(1L, 1L));
    }

    @Test
    void statusFlow_ShippedOrderCannotApprove_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(2);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.approve(1L, true));
    }

    @Test
    void statusFlow_CompletedOrderCannotReceive_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(3);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.receive(1L, new BigDecimal("100"), "CASH", "再次收款"));
    }

    @Test
    void ship_ShouldDecreaseStockQuantity() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = new BizSaleOrder();
            order.setId(1L);
            order.setOrderNo("XS001");
            order.setStatus(1);

            BizSaleItem item1 = new BizSaleItem();
            item1.setId(1L);
            item1.setGoodsId(1L);
            item1.setQuantity(3);

            BizSaleItem item2 = new BizSaleItem();
            item2.setId(2L);
            item2.setGoodsId(2L);
            item2.setQuantity(5);

            com.wms.entity.BizGoods goods1 = new com.wms.entity.BizGoods();
            goods1.setId(1L);
            goods1.setPurchasePrice(new BigDecimal("80"));

            com.wms.entity.BizGoods goods2 = new com.wms.entity.BizGoods();
            goods2.setId(2L);
            goods2.setPurchasePrice(new BigDecimal("50"));

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(itemMapper.selectByOrderId(1L)).thenReturn(Arrays.asList(item1, item2));
            when(goodsMapper.selectById(1L)).thenReturn(goods1);
            when(goodsMapper.selectById(2L)).thenReturn(goods2);
            when(itemMapper.updateById(any(BizSaleItem.class))).thenReturn(1);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.ship(1L, 1L);

            verify(stockService, times(2)).updateStock(anyLong(), eq(1L), anyInt(), eq("OUT"), eq("SALE"), anyLong(), eq("XS001"), anyString());
            verify(stockService).updateStock(eq(1L), eq(1L), eq(3), eq("OUT"), eq("SALE"), eq(1L), eq("XS001"), anyString());
            verify(stockService).updateStock(eq(2L), eq(1L), eq(5), eq("OUT"), eq("SALE"), eq(1L), eq("XS001"), anyString());
        }
    }

    @Test
    void confirmQuote_NonQuoteStatusOrder_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setStatus(0);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.confirmQuote(1L));
    }
}
