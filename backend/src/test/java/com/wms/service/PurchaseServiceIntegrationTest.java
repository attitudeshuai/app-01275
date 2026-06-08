package com.wms.service;

import com.wms.common.BusinessException;
import com.wms.dto.PurchaseOrderDTO;
import com.wms.entity.BizPurchaseOrder;
import com.wms.entity.BizStock;
import com.wms.mapper.BizPurchaseOrderMapper;
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
class PurchaseServiceIntegrationTest {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private BizPurchaseOrderMapper orderMapper;

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

    private PurchaseOrderDTO createPurchaseOrderDTO() {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setSupplierId(1L);
        dto.setWarehouseId(1L);
        dto.setRemark("集成测试采购单");

        List<PurchaseOrderDTO.PurchaseItemDTO> items = new ArrayList<>();
        PurchaseOrderDTO.PurchaseItemDTO item = new PurchaseOrderDTO.PurchaseItemDTO();
        item.setGoodsId(1L);
        item.setQuantity(50);
        item.setPrice(new BigDecimal("15.00"));
        items.add(item);
        dto.setItems(items);

        return dto;
    }

    @Test
    void fullStatusFlow_PendingToApprovedToInbound_ShouldSuccess() {
        PurchaseOrderDTO dto = createPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        assertNotNull(savedOrder.getId());
        assertEquals(0, savedOrder.getStatus());
        assertNotNull(savedOrder.getOrderNo());
        assertTrue(savedOrder.getOrderNo().startsWith("CG"));
        assertEquals(0, savedOrder.getTotalAmount().compareTo(new BigDecimal("750.00")));

        purchaseService.approve(savedOrder.getId(), true, "审批通过");

        BizPurchaseOrder approvedOrder = orderMapper.selectById(savedOrder.getId());
        assertEquals(1, approvedOrder.getStatus());
        assertNotNull(approvedOrder.getApproverId());
        assertNotNull(approvedOrder.getApproveTime());
        assertEquals("审批通过", approvedOrder.getApproveRemark());

        BizStock beforeStock = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int beforeQty = beforeStock != null ? beforeStock.getQuantity() : 0;

        purchaseService.inbound(savedOrder.getId());

        BizPurchaseOrder inboundOrder = orderMapper.selectById(savedOrder.getId());
        assertEquals(3, inboundOrder.getStatus());
        assertNotNull(inboundOrder.getInboundTime());

        BizStock afterStock = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertNotNull(afterStock);
        assertEquals(beforeQty + 50, afterStock.getQuantity());
    }

    @Test
    void approve_Rejected_ShouldSetStatusTo2() {
        PurchaseOrderDTO dto = createPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        purchaseService.approve(savedOrder.getId(), false, "价格过高，拒绝");

        BizPurchaseOrder rejectedOrder = orderMapper.selectById(savedOrder.getId());
        assertEquals(2, rejectedOrder.getStatus());
        assertEquals("价格过高，拒绝", rejectedOrder.getApproveRemark());
    }

    @Test
    void approve_FromRejectedStatus_ShouldThrowException() {
        PurchaseOrderDTO dto = createPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        purchaseService.approve(savedOrder.getId(), false, "拒绝");

        assertThrows(BusinessException.class, () -> 
            purchaseService.approve(savedOrder.getId(), true, "再次审批"));
    }

    @Test
    void inbound_FromPendingStatus_ShouldThrowException() {
        PurchaseOrderDTO dto = createPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        assertThrows(BusinessException.class, () -> 
            purchaseService.inbound(savedOrder.getId()));
    }

    @Test
    void inbound_FromRejectedStatus_ShouldThrowException() {
        PurchaseOrderDTO dto = createPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        purchaseService.approve(savedOrder.getId(), false, "拒绝");

        assertThrows(BusinessException.class, () -> 
            purchaseService.inbound(savedOrder.getId()));
    }

    @Test
    void inbound_AlreadyInbound_ShouldThrowException() {
        PurchaseOrderDTO dto = createPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        purchaseService.approve(savedOrder.getId(), true, "通过");
        purchaseService.inbound(savedOrder.getId());

        assertThrows(BusinessException.class, () -> 
            purchaseService.inbound(savedOrder.getId()));
    }

    @Test
    void delete_PendingStatus_ShouldDelete() {
        PurchaseOrderDTO dto = createPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        purchaseService.delete(savedOrder.getId());

        BizPurchaseOrder deletedOrder = orderMapper.selectById(savedOrder.getId());
        assertNull(deletedOrder);
    }

    @Test
    void delete_ApprovedStatus_ShouldThrowException() {
        PurchaseOrderDTO dto = createPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        purchaseService.approve(savedOrder.getId(), true, "通过");

        assertThrows(BusinessException.class, () -> 
            purchaseService.delete(savedOrder.getId()));
    }

    @Test
    void update_PendingStatus_ShouldUpdate() {
        PurchaseOrderDTO dto = createPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        PurchaseOrderDTO updateDTO = new PurchaseOrderDTO();
        updateDTO.setId(savedOrder.getId());
        updateDTO.setSupplierId(1L);
        updateDTO.setWarehouseId(1L);
        updateDTO.setRemark("更新后的采购单");

        List<PurchaseOrderDTO.PurchaseItemDTO> items = new ArrayList<>();
        PurchaseOrderDTO.PurchaseItemDTO item = new PurchaseOrderDTO.PurchaseItemDTO();
        item.setGoodsId(1L);
        item.setQuantity(100);
        item.setPrice(new BigDecimal("20.00"));
        items.add(item);
        updateDTO.setItems(items);

        purchaseService.update(updateDTO);

        BizPurchaseOrder updatedOrder = orderMapper.selectById(savedOrder.getId());
        assertEquals("更新后的采购单", updatedOrder.getRemark());
        assertEquals(0, updatedOrder.getTotalAmount().compareTo(new BigDecimal("2000.00")));
    }

    @Test
    void update_ApprovedStatus_ShouldThrowException() {
        PurchaseOrderDTO dto = createPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        purchaseService.approve(savedOrder.getId(), true, "通过");

        PurchaseOrderDTO updateDTO = new PurchaseOrderDTO();
        updateDTO.setId(savedOrder.getId());

        assertThrows(BusinessException.class, () -> 
            purchaseService.update(updateDTO));
    }

    @Test
    void getById_ShouldReturnOrderWithItems() {
        PurchaseOrderDTO dto = createPurchaseOrderDTO();
        purchaseService.save(dto);

        BizPurchaseOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("集成测试"))
                .findFirst()
                .orElseThrow();

        BizPurchaseOrder result = purchaseService.getById(savedOrder.getId());

        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals(50, result.getItems().get(0).getQuantity());
    }

    @Test
    void inbound_MultipleItems_ShouldIncreaseStockForAll() {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setSupplierId(1L);
        dto.setWarehouseId(1L);
        dto.setRemark("多商品采购单");

        List<PurchaseOrderDTO.PurchaseItemDTO> items = new ArrayList<>();

        PurchaseOrderDTO.PurchaseItemDTO item1 = new PurchaseOrderDTO.PurchaseItemDTO();
        item1.setGoodsId(1L);
        item1.setQuantity(30);
        item1.setPrice(new BigDecimal("10.00"));
        items.add(item1);

        PurchaseOrderDTO.PurchaseItemDTO item2 = new PurchaseOrderDTO.PurchaseItemDTO();
        item2.setGoodsId(1L);
        item2.setQuantity(20);
        item2.setPrice(new BigDecimal("12.00"));
        items.add(item2);

        dto.setItems(items);
        purchaseService.save(dto);

        BizPurchaseOrder savedOrder = orderMapper.selectList(null).stream()
                .filter(o -> o.getRemark() != null && o.getRemark().contains("多商品"))
                .findFirst()
                .orElseThrow();

        purchaseService.approve(savedOrder.getId(), true, "通过");

        BizStock beforeStock = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int beforeQty = beforeStock != null ? beforeStock.getQuantity() : 0;

        purchaseService.inbound(savedOrder.getId());

        BizStock afterStock = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertEquals(beforeQty + 50, afterStock.getQuantity());
    }
}
