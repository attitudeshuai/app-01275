package com.wms.service;

import com.wms.common.BusinessException;
import com.wms.entity.BizPaymentRecord;
import com.wms.entity.BizSaleItem;
import com.wms.entity.BizSaleOrder;
import com.wms.entity.BizStock;
import com.wms.mapper.BizPaymentRecordMapper;
import com.wms.mapper.BizSaleOrderMapper;
import com.wms.mapper.BizStockMapper;
import com.wms.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SaleServiceIntegrationTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private BizSaleOrderMapper orderMapper;

    @Autowired
    private BizPaymentRecordMapper paymentRecordMapper;

    @Autowired
    private BizStockMapper stockMapper;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(1L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private BizSaleOrder createSaleOrder() {
        BizSaleOrder order = new BizSaleOrder();
        order.setCustomerId(1L);
        order.setRemark("集成测试销售单");

        List<BizSaleItem> items = new ArrayList<>();
        BizSaleItem item = new BizSaleItem();
        item.setGoodsId(1L);
        item.setQuantity(20);
        item.setPrice(new BigDecimal("25.00"));
        items.add(item);
        order.setItems(items);

        return order;
    }

    @Test
    void fullStatusFlow_QuoteToConfirmToApproveToShipToReceive_ShouldSuccess() {
        BizSaleOrder quoteOrder = createSaleOrder();
        quoteOrder.setRemark("报价单测试");
        saleService.quote(quoteOrder);

        BizSaleOrder savedQuote = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("报价单测试"))
                .findFirst()
                .orElseThrow();

        assertNotNull(savedQuote.getId());
        assertEquals(-1, savedQuote.getStatus());
        assertTrue(savedQuote.getOrderNo().startsWith("BJ"));
        assertNotNull(savedQuote.getQuoteTime());

        saleService.confirmQuote(savedQuote.getId());

        BizSaleOrder confirmedOrder = orderMapper.selectById(savedQuote.getId());
        assertEquals(0, confirmedOrder.getStatus());
        assertTrue(confirmedOrder.getOrderNo().startsWith("XS"));
        assertNotNull(confirmedOrder.getConfirmTime());

        saleService.approve(confirmedOrder.getId(), true);

        BizSaleOrder approvedOrder = orderMapper.selectById(savedQuote.getId());
        assertEquals(1, approvedOrder.getStatus());
        assertNotNull(approvedOrder.getApproverId());
        assertNotNull(approvedOrder.getApproveTime());

        BizStock beforeStock = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int beforeQty = beforeStock != null ? beforeStock.getQuantity() : 0;

        saleService.ship(savedQuote.getId(), 1L);

        BizSaleOrder shippedOrder = orderMapper.selectById(savedQuote.getId());
        assertEquals(2, shippedOrder.getStatus());
        assertNotNull(shippedOrder.getShipTime());
        assertEquals(1L, shippedOrder.getWarehouseId());

        BizStock afterStock = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertNotNull(afterStock);
        assertEquals(beforeQty - 20, afterStock.getQuantity());

        saleService.receive(savedQuote.getId(), new BigDecimal("500.00"), "BANK", "全额收款");

        BizSaleOrder receivedOrder = orderMapper.selectById(savedQuote.getId());
        assertEquals(3, receivedOrder.getStatus());
        assertNotNull(receivedOrder.getReceiveTime());
        assertEquals(0, receivedOrder.getReceivedAmount().compareTo(new BigDecimal("500.00")));
    }

    @Test
    void approve_Cancelled_ShouldSetStatusTo4() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), false);

        BizSaleOrder cancelledOrder = orderMapper.selectById(savedOrder.getId());
        assertEquals(4, cancelledOrder.getStatus());
    }

    @Test
    void confirmQuote_FromPendingStatus_ShouldThrowException() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        assertThrows(BusinessException.class, () -> 
            saleService.confirmQuote(savedOrder.getId()));
    }

    @Test
    void approve_FromQuoteStatus_ShouldThrowException() {
        BizSaleOrder order = createSaleOrder();
        saleService.quote(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        assertThrows(BusinessException.class, () -> 
            saleService.approve(savedOrder.getId(), true));
    }

    @Test
    void approve_FromCancelledStatus_ShouldThrowException() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), false);

        assertThrows(BusinessException.class, () -> 
            saleService.approve(savedOrder.getId(), true));
    }

    @Test
    void ship_FromPendingStatus_ShouldThrowException() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        assertThrows(BusinessException.class, () -> 
            saleService.ship(savedOrder.getId(), 1L));
    }

    @Test
    void ship_FromCancelledStatus_ShouldThrowException() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), false);

        assertThrows(BusinessException.class, () -> 
            saleService.ship(savedOrder.getId(), 1L));
    }

    @Test
    void ship_AlreadyShipped_ShouldThrowException() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), true);
        saleService.ship(savedOrder.getId(), 1L);

        assertThrows(BusinessException.class, () -> 
            saleService.ship(savedOrder.getId(), 1L));
    }

    @Test
    void ship_InsufficientStock_ShouldThrowException() {
        BizSaleOrder order = new BizSaleOrder();
        order.setCustomerId(1L);
        order.setRemark("超库存销售单");

        List<BizSaleItem> items = new ArrayList<>();
        BizSaleItem item = new BizSaleItem();
        item.setGoodsId(1L);
        item.setQuantity(10000);
        item.setPrice(new BigDecimal("25.00"));
        items.add(item);
        order.setItems(items);

        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("超库存"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), true);

        assertThrows(BusinessException.class, () -> 
            saleService.ship(savedOrder.getId(), 1L));
    }

    @Test
    void receive_PartialPayment_ShouldKeepStatusShipped() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), true);
        saleService.ship(savedOrder.getId(), 1L);

        saleService.receive(savedOrder.getId(), new BigDecimal("200.00"), "CASH", "首付200");

        BizSaleOrder partialOrder = orderMapper.selectById(savedOrder.getId());
        assertEquals(2, partialOrder.getStatus());
        assertEquals(0, partialOrder.getReceivedAmount().compareTo(new BigDecimal("200.00")));

        List<BizPaymentRecord> records = paymentRecordMapper.selectByOrderId(savedOrder.getId());
        assertEquals(1, records.size());
        assertEquals(0, records.get(0).getAmount().compareTo(new BigDecimal("200.00")));
        assertEquals("CASH", records.get(0).getPaymentMethod());
    }

    @Test
    void receive_MultiplePayments_ShouldAccumulate() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), true);
        saleService.ship(savedOrder.getId(), 1L);

        saleService.receive(savedOrder.getId(), new BigDecimal("200.00"), "BANK", "第一笔");
        saleService.receive(savedOrder.getId(), new BigDecimal("300.00"), "ALIPAY", "第二笔");

        BizSaleOrder fullOrder = orderMapper.selectById(savedOrder.getId());
        assertEquals(3, fullOrder.getStatus());
        assertEquals(0, fullOrder.getReceivedAmount().compareTo(new BigDecimal("500.00")));

        List<BizPaymentRecord> records = paymentRecordMapper.selectByOrderId(savedOrder.getId());
        assertEquals(2, records.size());
    }

    @Test
    void receive_FromReceivedStatus_ShouldThrowException() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), true);
        saleService.ship(savedOrder.getId(), 1L);
        saleService.receive(savedOrder.getId(), new BigDecimal("500.00"), "BANK", "全额收款");

        assertThrows(BusinessException.class, () -> 
            saleService.receive(savedOrder.getId(), new BigDecimal("100.00"), "BANK", "额外收款"));
    }

    @Test
    void receive_FromCancelledStatus_ShouldThrowException() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), false);

        assertThrows(BusinessException.class, () -> 
            saleService.receive(savedOrder.getId(), new BigDecimal("100.00"), "BANK", "测试"));
    }

    @Test
    void delete_PendingStatus_ShouldDelete() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.delete(savedOrder.getId());

        BizSaleOrder deletedOrder = orderMapper.selectById(savedOrder.getId());
        assertNull(deletedOrder);
    }

    @Test
    void delete_QuoteStatus_ShouldDelete() {
        BizSaleOrder order = createSaleOrder();
        saleService.quote(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.delete(savedOrder.getId());

        BizSaleOrder deletedOrder = orderMapper.selectById(savedOrder.getId());
        assertNull(deletedOrder);
    }

    @Test
    void delete_ApprovedStatus_ShouldThrowException() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), true);

        assertThrows(BusinessException.class, () -> 
            saleService.delete(savedOrder.getId()));
    }

    @Test
    void update_PendingStatus_ShouldUpdate() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        BizSaleOrder updateOrder = new BizSaleOrder();
        updateOrder.setId(savedOrder.getId());
        updateOrder.setCustomerId(1L);
        updateOrder.setRemark("更新后的销售单");

        List<BizSaleItem> items = new ArrayList<>();
        BizSaleItem item = new BizSaleItem();
        item.setGoodsId(1L);
        item.setQuantity(30);
        item.setPrice(new BigDecimal("30.00"));
        items.add(item);
        updateOrder.setItems(items);

        saleService.update(updateOrder);

        BizSaleOrder updatedOrder = orderMapper.selectById(savedOrder.getId());
        assertEquals("更新后的销售单", updatedOrder.getRemark());
        assertEquals(0, updatedOrder.getTotalAmount().compareTo(new BigDecimal("900.00")));
    }

    @Test
    void update_QuoteStatus_ShouldUpdate() {
        BizSaleOrder order = createSaleOrder();
        saleService.quote(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        BizSaleOrder updateOrder = new BizSaleOrder();
        updateOrder.setId(savedOrder.getId());
        updateOrder.setCustomerId(1L);
        updateOrder.setRemark("更新后的报价单");

        List<BizSaleItem> items = new ArrayList<>();
        BizSaleItem item = new BizSaleItem();
        item.setGoodsId(1L);
        item.setQuantity(40);
        item.setPrice(new BigDecimal("35.00"));
        items.add(item);
        updateOrder.setItems(items);

        saleService.update(updateOrder);

        BizSaleOrder updatedOrder = orderMapper.selectById(savedOrder.getId());
        assertEquals("更新后的报价单", updatedOrder.getRemark());
        assertEquals(0, updatedOrder.getTotalAmount().compareTo(new BigDecimal("1400.00")));
        assertEquals(-1, updatedOrder.getStatus());
    }

    @Test
    void getById_ShouldReturnOrderWithItemsAndPayments() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), true);
        saleService.ship(savedOrder.getId(), 1L);
        saleService.receive(savedOrder.getId(), new BigDecimal("500.00"), "BANK", "全额收款");

        BizSaleOrder result = saleService.getById(savedOrder.getId());

        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertNotNull(result.getPaymentRecords());
        assertEquals(1, result.getPaymentRecords().size());
    }

    @Test
    void getPaymentRecords_ShouldReturnAllRecords() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), true);
        saleService.ship(savedOrder.getId(), 1L);
        saleService.receive(savedOrder.getId(), new BigDecimal("200.00"), "BANK", "第一笔");
        saleService.receive(savedOrder.getId(), new BigDecimal("300.00"), "CASH", "第二笔");

        List<BizPaymentRecord> records = saleService.getPaymentRecords(savedOrder.getId());

        assertNotNull(records);
        assertEquals(2, records.size());
    }

    @Test
    void save_DirectSaleOrder_ShouldHaveStatusPending() {
        BizSaleOrder order = createSaleOrder();
        order.setRemark("直接创建销售单");
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("直接创建"))
                .findFirst()
                .orElseThrow();

        assertEquals(0, savedOrder.getStatus());
        assertTrue(savedOrder.getOrderNo().startsWith("XS"));
    }

    @Test
    void cancelledOrder_CannotShipOrReceive() {
        BizSaleOrder order = createSaleOrder();
        saleService.save(order);

        BizSaleOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        saleService.approve(savedOrder.getId(), false);

        assertThrows(BusinessException.class, () -> 
            saleService.ship(savedOrder.getId(), 1L));
        assertThrows(BusinessException.class, () -> 
            saleService.receive(savedOrder.getId(), new BigDecimal("100"), "BANK", "测试"));
    }
}
