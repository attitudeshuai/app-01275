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
}
