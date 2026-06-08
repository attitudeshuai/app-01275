package com.wms.service;

import com.wms.dto.PurchaseOrderDTO;
import com.wms.entity.BizPurchaseItem;
import com.wms.entity.BizPurchaseOrder;
import com.wms.entity.BizStock;
import com.wms.mapper.BizPurchaseOrderMapper;
import com.wms.mapper.BizStockMapper;
import com.wms.util.UserContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PurchaseServiceIntegrationTest {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private BizPurchaseOrderMapper purchaseOrderMapper;

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

    @Test
    void fullStatusFlow_PendingToApprovedToInbound_ShouldPersistCorrectStatus() {
        PurchaseOrderDTO dto = createTestPurchaseOrderDTO();

        purchaseService.save(dto);

        BizPurchaseOrder order = purchaseOrderMapper.selectList(null).get(0);
        assertNotNull(order);
        assertEquals(0, order.getStatus(), "待审批状态应为0");

        purchaseService.approve(order.getId(), true, "审批通过");
        order = purchaseOrderMapper.selectById(order.getId());
        assertEquals(1, order.getStatus());
        assertNotNull(order.getApproveTime());
        assertEquals(1L, order.getApproverId());

        BizStock stockBefore = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int qtyBefore = stockBefore != null ? stockBefore.getQuantity() : 0;

        purchaseService.inbound(order.getId());
        order = purchaseOrderMapper.selectById(order.getId());
        assertEquals(3, order.getStatus());
        assertNotNull(order.getInboundTime());

        BizStock stockAfter = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertNotNull(stockAfter);
        assertEquals(qtyBefore + 10, stockAfter.getQuantity());
    }

    @Test
    void statusFlow_RejectPurchaseOrder_ShouldSetStatusTo2AndNotAffectStock() {
        PurchaseOrderDTO dto = createTestPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder order = purchaseOrderMapper.selectList(null).get(0);
        Long orderId = order.getId();

        purchaseService.approve(orderId, false, "采购价格过高，拒绝");
        order = purchaseOrderMapper.selectById(orderId);
        assertEquals(2, order.getStatus());
        assertEquals("采购价格过高，拒绝", order.getApproveRemark());

        BizStock stockBefore = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int qtyBefore = stockBefore != null ? stockBefore.getQuantity() : 0;

        assertThrows(Exception.class, () -> purchaseService.inbound(orderId));

        BizStock stockAfter = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertEquals(qtyBefore, stockAfter.getQuantity());
    }

    @Test
    void statusFlow_Inbound_ShouldIncreaseStockCorrectlyForMultipleItems() {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setSupplierId(1L);
        dto.setWarehouseId(1L);

        PurchaseOrderDTO.PurchaseItemDTO item1 = new PurchaseOrderDTO.PurchaseItemDTO();
        item1.setGoodsId(1L);
        item1.setQuantity(20);
        item1.setPrice(new BigDecimal("15.00"));

        dto.setItems(Arrays.asList(item1));

        purchaseService.save(dto);
        BizPurchaseOrder order = purchaseOrderMapper.selectList(null).get(0);

        BizStock stockBefore = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int qtyBefore = stockBefore.getQuantity();

        purchaseService.approve(order.getId(), true, null);
        purchaseService.inbound(order.getId());

        BizStock stockAfter = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertEquals(qtyBefore + 20, stockAfter.getQuantity());
        assertTrue(stockAfter.getQuantity() > 0, "库存数量不能为负数");
    }

    @Test
    void statusFlow_InvalidStateTransition_ShouldThrowException() {
        PurchaseOrderDTO dto = createTestPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder order = purchaseOrderMapper.selectList(null).get(0);
        Long orderId = order.getId();

        assertThrows(Exception.class, () -> purchaseService.inbound(orderId));

        purchaseService.approve(orderId, true, null);
        assertThrows(Exception.class, () -> purchaseService.approve(orderId, true, "重复审批"));

        purchaseService.inbound(orderId);
        assertThrows(Exception.class, () -> purchaseService.approve(orderId, true, null));
        assertThrows(Exception.class, () -> purchaseService.inbound(orderId));
    }

    @Test
    void calculateTotalAmount_ShouldSumAllItemAmountsCorrectly() {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setSupplierId(1L);
        dto.setWarehouseId(1L);

        PurchaseOrderDTO.PurchaseItemDTO item1 = new PurchaseOrderDTO.PurchaseItemDTO();
        item1.setGoodsId(1L);
        item1.setQuantity(5);
        item1.setPrice(new BigDecimal("10.00"));

        PurchaseOrderDTO.PurchaseItemDTO item2 = new PurchaseOrderDTO.PurchaseItemDTO();
        item2.setGoodsId(1L);
        item2.setQuantity(3);
        item2.setPrice(new BigDecimal("20.00"));

        dto.setItems(Arrays.asList(item1, item2));

        purchaseService.save(dto);

        BizPurchaseOrder order = purchaseOrderMapper.selectList(null).get(0);
        assertTrue(new BigDecimal("110.00").compareTo(order.getTotalAmount()) == 0);
    }

    @Test
    void inbound_WithoutWarehouseId_ShouldThrowException() {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setSupplierId(1L);
        dto.setWarehouseId(null);

        PurchaseOrderDTO.PurchaseItemDTO item = new PurchaseOrderDTO.PurchaseItemDTO();
        item.setGoodsId(1L);
        item.setQuantity(10);
        item.setPrice(new BigDecimal("10.00"));
        dto.setItems(Arrays.asList(item));

        purchaseService.save(dto);
        BizPurchaseOrder order = purchaseOrderMapper.selectList(null).get(0);
        purchaseService.approve(order.getId(), true, null);

        assertThrows(Exception.class, () -> purchaseService.inbound(order.getId()));
    }

    private PurchaseOrderDTO createTestPurchaseOrderDTO() {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setSupplierId(1L);
        dto.setWarehouseId(1L);
        dto.setRemark("测试采购单");

        PurchaseOrderDTO.PurchaseItemDTO item = new PurchaseOrderDTO.PurchaseItemDTO();
        item.setGoodsId(1L);
        item.setQuantity(10);
        item.setPrice(new BigDecimal("12.50"));
        dto.setItems(Arrays.asList(item));

        return dto;
    }
}
