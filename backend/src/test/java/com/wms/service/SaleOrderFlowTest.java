package com.wms.service;

import com.wms.common.BusinessException;
import com.wms.entity.BizGoods;
import com.wms.entity.BizPaymentRecord;
import com.wms.entity.BizSaleItem;
import com.wms.entity.BizSaleOrder;
import com.wms.entity.SysUser;
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
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleOrderFlowTest {

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

    private BizSaleOrder buildOrder(Integer status) {
        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setOrderNo("XS001");
        order.setCustomerId(1L);
        order.setWarehouseId(1L);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setReceivedAmount(BigDecimal.ZERO);
        return order;
    }

    private BizSaleItem buildItem(Long goodsId, int quantity, BigDecimal price) {
        BizSaleItem item = new BizSaleItem();
        item.setId(1L);
        item.setOrderId(1L);
        item.setGoodsId(goodsId);
        item.setQuantity(quantity);
        item.setPrice(price);
        item.setAmount(price.multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    private BizGoods buildGoods(Long id, BigDecimal purchasePrice) {
        BizGoods goods = new BizGoods();
        goods.setId(id);
        goods.setPurchasePrice(purchasePrice);
        return goods;
    }

    @Test
    void quote_ShouldCreateOrderWithQuoteStatus() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(new SysUser());
            when(orderMapper.insert(any(BizSaleOrder.class))).thenReturn(1);
            when(itemMapper.insert(any(BizSaleItem.class))).thenReturn(1);

            BizSaleOrder order = new BizSaleOrder();
            order.setCustomerId(1L);

            BizSaleItem item = new BizSaleItem();
            item.setGoodsId(1L);
            item.setQuantity(5);
            item.setPrice(new BigDecimal("100.00"));
            order.setItems(Arrays.asList(item));

            saleService.quote(order);

            verify(orderMapper).insert(argThat(o -> {
                BizSaleOrder saved = (BizSaleOrder) o;
                return saved.getStatus() == -1
                        && saved.getOrderNo() != null
                        && saved.getOrderNo().startsWith("BJ")
                        && saved.getQuoteTime() != null
                        && saved.getReceivedAmount().compareTo(BigDecimal.ZERO) == 0;
            }));
        }
    }

    @Test
    void quote_ShouldCalculateTotalAmountFromItems() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(new SysUser());
            when(orderMapper.insert(any(BizSaleOrder.class))).thenReturn(1);
            when(itemMapper.insert(any(BizSaleItem.class))).thenReturn(1);

            BizSaleOrder order = new BizSaleOrder();
            order.setCustomerId(1L);

            BizSaleItem i1 = new BizSaleItem();
            i1.setGoodsId(1L);
            i1.setQuantity(3);
            i1.setPrice(new BigDecimal("100.00"));

            BizSaleItem i2 = new BizSaleItem();
            i2.setGoodsId(2L);
            i2.setQuantity(2);
            i2.setPrice(new BigDecimal("200.00"));

            order.setItems(Arrays.asList(i1, i2));

            saleService.quote(order);

            verify(orderMapper).insert(argThat(o -> {
                BizSaleOrder saved = (BizSaleOrder) o;
                return saved.getTotalAmount().compareTo(new BigDecimal("700.00")) == 0;
            }));
        }
    }

    @Test
    void confirmQuote_ShouldChangeStatusToPendingAndRegenerateOrderNo() {
        BizSaleOrder order = buildOrder(-1);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

        saleService.confirmQuote(1L);

        verify(orderMapper).updateById(argThat(o -> {
            BizSaleOrder updated = (BizSaleOrder) o;
            return updated.getStatus() == 0
                    && updated.getOrderNo() != null
                    && updated.getOrderNo().startsWith("XS")
                    && updated.getConfirmTime() != null;
        }));
    }

    @Test
    void confirmQuote_ShouldReject_WhenStatusIsNotQuote() {
        BizSaleOrder order = buildOrder(0);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.confirmQuote(1L));
        verify(orderMapper, never()).updateById(any());
    }

    @Test
    void confirmQuote_ShouldReject_WhenOrderNotFound() {
        when(orderMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> saleService.confirmQuote(1L));
    }

    @Test
    void approve_ShouldSetStatusToApproved_WhenOrderIsPending() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = buildOrder(0);
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.approve(1L, true);

            verify(orderMapper).updateById(argThat(o -> {
                BizSaleOrder updated = (BizSaleOrder) o;
                return updated.getStatus() == 1
                        && updated.getApproverId().equals(1L)
                        && updated.getApproveTime() != null;
            }));
        }
    }

    @Test
    void approve_ShouldSetStatusToCancelled_WhenDisapproved() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = buildOrder(0);
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.approve(1L, false);

            verify(orderMapper).updateById(argThat(o -> {
                BizSaleOrder updated = (BizSaleOrder) o;
                return updated.getStatus() == 4;
            }));
        }
    }

    @Test
    void approve_ShouldReject_WhenStatusIsApproved() {
        BizSaleOrder order = buildOrder(1);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.approve(1L, true));
        verify(orderMapper, never()).updateById(any());
    }

    @Test
    void approve_ShouldReject_WhenStatusIsShipped() {
        BizSaleOrder order = buildOrder(2);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.approve(1L, true));
    }

    @Test
    void approve_ShouldReject_WhenStatusIsReceived() {
        BizSaleOrder order = buildOrder(3);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.approve(1L, true));
    }

    @Test
    void approve_ShouldReject_WhenStatusIsCancelled() {
        BizSaleOrder order = buildOrder(4);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.approve(1L, true));
    }

    @Test
    void approve_ShouldReject_WhenStatusIsQuote() {
        BizSaleOrder order = buildOrder(-1);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.approve(1L, true));
    }

    @Test
    void ship_ShouldSetStatusToShipped_WhenOrderIsApproved() {
        BizSaleOrder order = buildOrder(1);
        BizSaleItem item = buildItem(1L, 5, new BigDecimal("100.00"));
        BizGoods goods = buildGoods(1L, new BigDecimal("80.00"));

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(itemMapper.selectByOrderId(1L)).thenReturn(Arrays.asList(item));
        when(goodsMapper.selectById(1L)).thenReturn(goods);
        when(itemMapper.updateById(any(BizSaleItem.class))).thenReturn(1);
        when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

        saleService.ship(1L, 1L);

        verify(stockService).updateStock(eq(1L), eq(1L), eq(5), eq("OUT"), eq("SALE"), eq(1L), eq("XS001"), anyString());
        verify(orderMapper).updateById(argThat(o -> {
            BizSaleOrder updated = (BizSaleOrder) o;
            return updated.getStatus() == 2
                    && updated.getWarehouseId().equals(1L)
                    && updated.getShipTime() != null;
        }));
    }

    @Test
    void ship_ShouldRecordCostPriceFromGoods() {
        BizSaleOrder order = buildOrder(1);
        BizSaleItem item = buildItem(1L, 5, new BigDecimal("100.00"));
        BizGoods goods = buildGoods(1L, new BigDecimal("80.00"));

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(itemMapper.selectByOrderId(1L)).thenReturn(Arrays.asList(item));
        when(goodsMapper.selectById(1L)).thenReturn(goods);
        when(itemMapper.updateById(any(BizSaleItem.class))).thenReturn(1);
        when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

        saleService.ship(1L, 1L);

        verify(itemMapper).updateById(argThat(i -> {
            BizSaleItem si = (BizSaleItem) i;
            return si.getCostPrice().compareTo(new BigDecimal("80.00")) == 0;
        }));
    }

    @Test
    void ship_ShouldReject_WhenStatusIsPending() {
        BizSaleOrder order = buildOrder(0);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.ship(1L, 1L));
        verify(stockService, never()).updateStock(anyLong(), anyLong(), anyInt(), anyString(), anyString(), anyLong(), anyString(), anyString());
    }

    @Test
    void ship_ShouldReject_WhenStatusIsShipped() {
        BizSaleOrder order = buildOrder(2);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.ship(1L, 1L));
    }

    @Test
    void ship_ShouldReject_WhenStatusIsCancelled() {
        BizSaleOrder order = buildOrder(4);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.ship(1L, 1L));
    }

    @Test
    void ship_ShouldCallStockServiceForEachItem() {
        BizSaleOrder order = buildOrder(1);
        BizSaleItem item1 = buildItem(1L, 5, new BigDecimal("100.00"));
        BizSaleItem item2 = buildItem(2L, 3, new BigDecimal("200.00"));
        BizGoods goods1 = buildGoods(1L, new BigDecimal("80.00"));
        BizGoods goods2 = buildGoods(2L, new BigDecimal("150.00"));

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(itemMapper.selectByOrderId(1L)).thenReturn(Arrays.asList(item1, item2));
        when(goodsMapper.selectById(1L)).thenReturn(goods1);
        when(goodsMapper.selectById(2L)).thenReturn(goods2);
        when(itemMapper.updateById(any(BizSaleItem.class))).thenReturn(1);
        when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

        saleService.ship(1L, 1L);

        verify(stockService).updateStock(eq(1L), eq(1L), eq(5), eq("OUT"), eq("SALE"), eq(1L), eq("XS001"), anyString());
        verify(stockService).updateStock(eq(2L), eq(1L), eq(3), eq("OUT"), eq("SALE"), eq(1L), eq("XS001"), anyString());
    }

    @Test
    void receive_ShouldKeepShippedStatus_WhenPartialPayment() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = buildOrder(2);
            order.setTotalAmount(new BigDecimal("1000.00"));
            order.setReceivedAmount(BigDecimal.ZERO);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(paymentRecordMapper.insert(any(BizPaymentRecord.class))).thenReturn(1);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.receive(1L, new BigDecimal("400.00"), "BANK", "部分收款");

            verify(orderMapper).updateById(argThat(o -> {
                BizSaleOrder updated = (BizSaleOrder) o;
                return updated.getStatus() == 2
                        && updated.getReceivedAmount().compareTo(new BigDecimal("400.00")) == 0;
            }));
            verify(paymentRecordMapper).insert(argThat(r -> {
                BizPaymentRecord record = (BizPaymentRecord) r;
                return record.getAmount().compareTo(new BigDecimal("400.00")) == 0
                        && record.getPaymentMethod().equals("BANK");
            }));
        }
    }

    @Test
    void receive_ShouldSetStatusToReceived_WhenFullPayment() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = buildOrder(2);
            order.setTotalAmount(new BigDecimal("1000.00"));
            order.setReceivedAmount(BigDecimal.ZERO);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(paymentRecordMapper.insert(any(BizPaymentRecord.class))).thenReturn(1);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.receive(1L, new BigDecimal("1000.00"), "CASH", "全额收款");

            verify(orderMapper).updateById(argThat(o -> {
                BizSaleOrder updated = (BizSaleOrder) o;
                return updated.getStatus() == 3
                        && updated.getReceivedAmount().compareTo(new BigDecimal("1000.00")) == 0
                        && updated.getReceiveTime() != null;
            }));
        }
    }

    @Test
    void receive_ShouldSetStatusToReceived_WhenOverPayment() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = buildOrder(2);
            order.setTotalAmount(new BigDecimal("1000.00"));
            order.setReceivedAmount(new BigDecimal("600.00"));

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(paymentRecordMapper.insert(any(BizPaymentRecord.class))).thenReturn(1);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.receive(1L, new BigDecimal("500.00"), "BANK", "超额收款");

            verify(orderMapper).updateById(argThat(o -> {
                BizSaleOrder updated = (BizSaleOrder) o;
                return updated.getStatus() == 3
                        && updated.getReceivedAmount().compareTo(new BigDecimal("1100.00")) == 0;
            }));
        }
    }

    @Test
    void receive_ShouldAccumulateAcrossMultiplePayments() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = buildOrder(2);
            order.setTotalAmount(new BigDecimal("1000.00"));
            order.setReceivedAmount(new BigDecimal("300.00"));

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(paymentRecordMapper.insert(any(BizPaymentRecord.class))).thenReturn(1);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.receive(1L, new BigDecimal("700.00"), "WECHAT", "尾款");

            verify(orderMapper).updateById(argThat(o -> {
                BizSaleOrder updated = (BizSaleOrder) o;
                return updated.getStatus() == 3
                        && updated.getReceivedAmount().compareTo(new BigDecimal("1000.00")) == 0;
            }));
        }
    }

    @Test
    void receive_ShouldReject_WhenStatusIsPending() {
        BizSaleOrder order = buildOrder(0);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class,
                () -> saleService.receive(1L, new BigDecimal("100.00"), "BANK", "收款"));
        verify(paymentRecordMapper, never()).insert(any());
    }

    @Test
    void receive_ShouldReject_WhenStatusIsApproved() {
        BizSaleOrder order = buildOrder(1);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class,
                () -> saleService.receive(1L, new BigDecimal("100.00"), "BANK", "收款"));
    }

    @Test
    void receive_ShouldReject_WhenStatusIsReceived() {
        BizSaleOrder order = buildOrder(3);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class,
                () -> saleService.receive(1L, new BigDecimal("100.00"), "BANK", "收款"));
    }

    @Test
    void receive_ShouldReject_WhenStatusIsCancelled() {
        BizSaleOrder order = buildOrder(4);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class,
                () -> saleService.receive(1L, new BigDecimal("100.00"), "BANK", "收款"));
    }

    @Test
    void receive_ShouldUseDefaultPaymentMethod_WhenNullProvided() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);

            BizSaleOrder order = buildOrder(2);
            order.setTotalAmount(new BigDecimal("1000.00"));
            order.setReceivedAmount(BigDecimal.ZERO);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(paymentRecordMapper.insert(any(BizPaymentRecord.class))).thenReturn(1);
            when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

            saleService.receive(1L, new BigDecimal("1000.00"), null, null);

            verify(paymentRecordMapper).insert(argThat(r -> {
                BizPaymentRecord record = (BizPaymentRecord) r;
                return record.getPaymentMethod().equals("BANK");
            }));
        }
    }

    @Test
    void save_ShouldCalculateTotalAmountFromItems() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(new SysUser());
            when(orderMapper.insert(any(BizSaleOrder.class))).thenReturn(1);
            when(itemMapper.insert(any(BizSaleItem.class))).thenReturn(1);

            BizSaleOrder order = new BizSaleOrder();
            order.setCustomerId(1L);

            BizSaleItem i1 = new BizSaleItem();
            i1.setGoodsId(1L);
            i1.setQuantity(3);
            i1.setPrice(new BigDecimal("100.00"));

            BizSaleItem i2 = new BizSaleItem();
            i2.setGoodsId(2L);
            i2.setQuantity(2);
            i2.setPrice(new BigDecimal("200.00"));

            order.setItems(Arrays.asList(i1, i2));

            saleService.save(order);

            verify(orderMapper).insert(argThat(o -> {
                BizSaleOrder saved = (BizSaleOrder) o;
                return saved.getTotalAmount().compareTo(new BigDecimal("700.00")) == 0
                        && saved.getStatus() == 0
                        && saved.getOrderNo().startsWith("XS")
                        && saved.getReceivedAmount().compareTo(BigDecimal.ZERO) == 0;
            }));
        }
    }

    @Test
    void save_ShouldCalculateItemAmountAsPriceTimesQuantity() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserId).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(new SysUser());
            when(orderMapper.insert(any(BizSaleOrder.class))).thenReturn(1);
            when(itemMapper.insert(any(BizSaleItem.class))).thenReturn(1);

            BizSaleOrder order = new BizSaleOrder();
            order.setCustomerId(1L);

            BizSaleItem item = new BizSaleItem();
            item.setGoodsId(1L);
            item.setQuantity(5);
            item.setPrice(new BigDecimal("100.00"));
            order.setItems(Arrays.asList(item));

            saleService.save(order);

            verify(itemMapper).insert(argThat(i -> {
                BizSaleItem si = (BizSaleItem) i;
                return si.getAmount().compareTo(new BigDecimal("500.00")) == 0;
            }));
        }
    }

    @Test
    void update_ShouldReject_WhenStatusIsApproved() {
        BizSaleOrder existing = buildOrder(1);
        when(orderMapper.selectById(1L)).thenReturn(existing);

        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);

        assertThrows(BusinessException.class, () -> saleService.update(order));
        verify(orderMapper, never()).updateById(any());
    }

    @Test
    void update_ShouldReject_WhenStatusIsShipped() {
        BizSaleOrder existing = buildOrder(2);
        when(orderMapper.selectById(1L)).thenReturn(existing);

        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);

        assertThrows(BusinessException.class, () -> saleService.update(order));
    }

    @Test
    void update_ShouldReject_WhenStatusIsReceived() {
        BizSaleOrder existing = buildOrder(3);
        when(orderMapper.selectById(1L)).thenReturn(existing);

        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);

        assertThrows(BusinessException.class, () -> saleService.update(order));
    }

    @Test
    void update_ShouldReject_WhenStatusIsCancelled() {
        BizSaleOrder existing = buildOrder(4);
        when(orderMapper.selectById(1L)).thenReturn(existing);

        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);

        assertThrows(BusinessException.class, () -> saleService.update(order));
    }

    @Test
    void update_ShouldAllow_WhenStatusIsPending() {
        BizSaleOrder existing = buildOrder(0);
        when(orderMapper.selectById(1L)).thenReturn(existing);
        when(itemMapper.delete(any())).thenReturn(1);
        when(itemMapper.insert(any(BizSaleItem.class))).thenReturn(1);
        when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setCustomerId(1L);

        BizSaleItem item = new BizSaleItem();
        item.setGoodsId(1L);
        item.setQuantity(10);
        item.setPrice(new BigDecimal("50.00"));
        order.setItems(Arrays.asList(item));

        saleService.update(order);

        verify(orderMapper).updateById(argThat(o -> {
            BizSaleOrder updated = (BizSaleOrder) o;
            return updated.getTotalAmount().compareTo(new BigDecimal("500.00")) == 0;
        }));
    }

    @Test
    void update_ShouldAllow_WhenStatusIsQuote() {
        BizSaleOrder existing = buildOrder(-1);
        when(orderMapper.selectById(1L)).thenReturn(existing);
        when(itemMapper.delete(any())).thenReturn(1);
        when(itemMapper.insert(any(BizSaleItem.class))).thenReturn(1);
        when(orderMapper.updateById(any(BizSaleOrder.class))).thenReturn(1);

        BizSaleOrder order = new BizSaleOrder();
        order.setId(1L);
        order.setCustomerId(1L);

        BizSaleItem item = new BizSaleItem();
        item.setGoodsId(1L);
        item.setQuantity(1);
        item.setPrice(new BigDecimal("100.00"));
        order.setItems(Arrays.asList(item));

        assertDoesNotThrow(() -> saleService.update(order));
    }

    @Test
    void delete_ShouldReject_WhenStatusIsApproved() {
        BizSaleOrder order = buildOrder(1);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.delete(1L));
        verify(orderMapper, never()).deleteById(anyLong());
    }

    @Test
    void delete_ShouldReject_WhenStatusIsShipped() {
        BizSaleOrder order = buildOrder(2);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> saleService.delete(1L));
    }

    @Test
    void delete_ShouldAllow_WhenStatusIsPending() {
        BizSaleOrder order = buildOrder(0);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.deleteById(1L)).thenReturn(1);
        when(itemMapper.delete(any())).thenReturn(1);

        assertDoesNotThrow(() -> saleService.delete(1L));
        verify(orderMapper).deleteById(1L);
    }

    @Test
    void delete_ShouldAllow_WhenStatusIsQuote() {
        BizSaleOrder order = buildOrder(-1);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.deleteById(1L)).thenReturn(1);
        when(itemMapper.delete(any())).thenReturn(1);

        assertDoesNotThrow(() -> saleService.delete(1L));
    }
}
