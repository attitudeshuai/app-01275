package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.dto.PurchaseOrderDTO;
import com.wms.entity.BizPaymentRecord;
import com.wms.entity.BizPurchaseOrder;
import com.wms.entity.BizSaleItem;
import com.wms.entity.BizSaleOrder;
import com.wms.entity.BizStock;
import com.wms.mapper.BizPaymentRecordMapper;
import com.wms.mapper.BizPurchaseOrderMapper;
import com.wms.mapper.BizSaleOrderMapper;
import com.wms.mapper.BizStockMapper;
import com.wms.util.UserContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SaleServiceIntegrationTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private BizSaleOrderMapper saleOrderMapper;

    @Autowired
    private BizPurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private BizPaymentRecordMapper paymentRecordMapper;

    @Autowired
    private BizStockMapper stockMapper;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(1L);
        ensureStockAvailable();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void fullStatusFlow_QuoteToConfirmToApproveToShipToReceive_ShouldPersistCorrectStatus() {
        BizSaleOrder quoteOrder = createTestSaleOrder();
        saleService.quote(quoteOrder);

        BizSaleOrder order = saleOrderMapper.selectList(null).get(0);
        assertNotNull(order);
        assertEquals(-1, order.getStatus());
        assertTrue(order.getOrderNo().startsWith("BJ"));
        assertNotNull(order.getQuoteTime());

        saleService.confirmQuote(order.getId());
        order = saleOrderMapper.selectById(order.getId());
        assertEquals(0, order.getStatus());
        assertTrue(order.getOrderNo().startsWith("XS"));
        assertNotNull(order.getConfirmTime());

        saleService.approve(order.getId(), true);
        order = saleOrderMapper.selectById(order.getId());
        assertEquals(1, order.getStatus());
        assertNotNull(order.getApproveTime());
        assertEquals(1L, order.getApproverId());

        BizStock stockBefore = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int qtyBefore = stockBefore.getQuantity();

        saleService.ship(order.getId(), 1L);
        order = saleOrderMapper.selectById(order.getId());
        assertEquals(2, order.getStatus());
        assertNotNull(order.getShipTime());
        assertEquals(1L, order.getWarehouseId());

        BizStock stockAfterShip = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertEquals(qtyBefore - 5, stockAfterShip.getQuantity());
        assertTrue(stockAfterShip.getQuantity() >= 0, "库存数量不能为负数");

        saleService.receive(order.getId(), new BigDecimal("100.00"), "BANK", "全额收款");
        order = saleOrderMapper.selectById(order.getId());
        assertEquals(3, order.getStatus());
        assertEquals(new BigDecimal("100.00"), order.getReceivedAmount());
        assertNotNull(order.getReceiveTime());
    }

    @Test
    void statusFlow_QuoteToCancel_ShouldSetStatusTo4() {
        BizSaleOrder quoteOrder = createTestSaleOrder();
        saleService.quote(quoteOrder);

        BizSaleOrder order = saleOrderMapper.selectList(null).get(0);
        Long orderId = order.getId();

        saleService.confirmQuote(orderId);
        saleService.approve(orderId, false);
        order = saleOrderMapper.selectById(orderId);
        assertEquals(4, order.getStatus());

        BizStock stockBefore = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int qtyBefore = stockBefore.getQuantity();

        assertThrows(Exception.class, () -> saleService.ship(orderId, 1L));

        BizStock stockAfter = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertEquals(qtyBefore, stockAfter.getQuantity());
    }

    @Test
    void partialPayment_MultiplePayments_ShouldAccumulateReceivedAmount() {
        BizSaleOrder order = createDirectSaleOrder(10);
        saleService.save(order);
        BizSaleOrder savedOrder = saleOrderMapper.selectList(null).get(0);
        Long orderId = savedOrder.getId();

        saleService.approve(orderId, true);
        saleService.ship(orderId, 1L);

        saleService.receive(orderId, new BigDecimal("30.00"), "CASH", "首付款");
        savedOrder = saleOrderMapper.selectById(orderId);
        assertEquals(2, savedOrder.getStatus());
        assertEquals(new BigDecimal("30.00"), savedOrder.getReceivedAmount());

        saleService.receive(orderId, new BigDecimal("40.00"), "BANK", "中期款");
        savedOrder = saleOrderMapper.selectById(orderId);
        assertEquals(2, savedOrder.getStatus());
        assertEquals(new BigDecimal("70.00"), savedOrder.getReceivedAmount());

        saleService.receive(orderId, new BigDecimal("30.00"), "WECHAT", "尾款");
        savedOrder = saleOrderMapper.selectById(orderId);
        assertEquals(3, savedOrder.getStatus());
        assertEquals(new BigDecimal("100.00"), savedOrder.getReceivedAmount());

        List<BizPaymentRecord> records = paymentRecordMapper.selectByOrderId(orderId);
        assertEquals(3, records.size());
    }

    @Test
    void statusFlow_ShipWithoutSufficientStock_ShouldThrowException() {
        BizSaleOrder order = createDirectSaleOrder(1000);
        saleService.save(order);
        BizSaleOrder savedOrder = saleOrderMapper.selectList(null).get(0);
        Long orderId = savedOrder.getId();

        saleService.approve(orderId, true);

        BizStock stockBefore = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertTrue(stockBefore.getQuantity() < 1000, "现有库存不足以发出1000件");

        assertThrows(Exception.class, () -> saleService.ship(orderId, 1L));

        BizStock stockAfter = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertEquals(stockBefore.getQuantity(), stockAfter.getQuantity());
    }

    @Test
    void statusFlow_InvalidStateTransitions_ShouldThrowException() {
        BizSaleOrder quoteOrder = createTestSaleOrder();
        saleService.quote(quoteOrder);
        BizSaleOrder order = saleOrderMapper.selectList(null).get(0);
        Long orderId = order.getId();

        assertThrows(Exception.class, () -> saleService.approve(orderId, true));
        assertThrows(Exception.class, () -> saleService.ship(orderId, 1L));
        assertThrows(Exception.class, () -> saleService.receive(orderId, new BigDecimal("100"), "CASH", null));

        saleService.confirmQuote(orderId);
        assertThrows(Exception.class, () -> saleService.ship(orderId, 1L));
        assertThrows(Exception.class, () -> saleService.receive(orderId, new BigDecimal("100"), "CASH", null));

        saleService.approve(orderId, true);
        assertThrows(Exception.class, () -> saleService.approve(orderId, true));
        assertThrows(Exception.class, () -> saleService.receive(orderId, new BigDecimal("100"), "CASH", null));

        saleService.ship(orderId, 1L);
        assertThrows(Exception.class, () -> saleService.approve(orderId, true));
        assertThrows(Exception.class, () -> saleService.ship(orderId, 1L));

        saleService.receive(orderId, new BigDecimal("100.00"), "CASH", "全额");
        assertThrows(Exception.class, () -> saleService.ship(orderId, 1L));
        assertThrows(Exception.class, () -> saleService.receive(orderId, new BigDecimal("100"), "CASH", null));
    }

    @Test
    void calculateTotalAmount_ShouldSumAllItemAmountsCorrectly() {
        BizSaleOrder order = new BizSaleOrder();
        order.setCustomerId(1L);

        BizSaleItem item1 = new BizSaleItem();
        item1.setGoodsId(1L);
        item1.setQuantity(3);
        item1.setPrice(new BigDecimal("20.00"));

        BizSaleItem item2 = new BizSaleItem();
        item2.setGoodsId(1L);
        item2.setQuantity(2);
        item2.setPrice(new BigDecimal("30.00"));

        order.setItems(Arrays.asList(item1, item2));
        saleService.save(order);

        BizSaleOrder savedOrder = saleOrderMapper.selectList(null).get(0);
        assertTrue(new BigDecimal("120.00").compareTo(savedOrder.getTotalAmount()) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(savedOrder.getReceivedAmount()) == 0);
    }

    private void ensureStockAvailable() {
        BizStock stock = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        if (stock == null || stock.getQuantity() < 100) {
            PurchaseOrderDTO dto = new PurchaseOrderDTO();
            dto.setSupplierId(1L);
            dto.setWarehouseId(1L);

            PurchaseOrderDTO.PurchaseItemDTO item = new PurchaseOrderDTO.PurchaseItemDTO();
            item.setGoodsId(1L);
            item.setQuantity(200);
            item.setPrice(new BigDecimal("10.00"));
            dto.setItems(Arrays.asList(item));

            purchaseService.save(dto);
            
            LambdaQueryWrapper<BizPurchaseOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(BizPurchaseOrder::getId);
            BizPurchaseOrder purchaseOrder = purchaseOrderMapper.selectList(wrapper).get(0);
            
            purchaseService.approve(purchaseOrder.getId(), true, "测试用采购入库");
            purchaseService.inbound(purchaseOrder.getId());
        }
    }

    private BizSaleOrder createTestSaleOrder() {
        BizSaleOrder order = new BizSaleOrder();
        order.setCustomerId(1L);
        order.setRemark("测试报价单");

        BizSaleItem item = new BizSaleItem();
        item.setGoodsId(1L);
        item.setQuantity(5);
        item.setPrice(new BigDecimal("20.00"));
        order.setItems(Arrays.asList(item));

        return order;
    }

    private BizSaleOrder createDirectSaleOrder(int quantity) {
        BizSaleOrder order = new BizSaleOrder();
        order.setCustomerId(1L);
        order.setRemark("测试销售单");

        BizSaleItem item = new BizSaleItem();
        item.setGoodsId(1L);
        item.setQuantity(quantity);
        item.setPrice(new BigDecimal("10.00"));
        order.setItems(Arrays.asList(item));

        return order;
    }
}
