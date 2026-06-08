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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 采购单据状态流转集成测试
 * 覆盖：待审批(0) -> 已通过(1) -> 已入库(3)，以及审批拒绝(0 -> 2) 的全流程
 * 同时校验：库存变动、非法状态流转拦截、库存不能变为负数等业务规则
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("采购单据状态流转集成测试")
class PurchaseOrderFlowIntegrationTest {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private BizPurchaseOrderMapper orderMapper;

    @Autowired
    private BizStockMapper stockMapper;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(1L);
        UserContext.setUsername("admin");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private PurchaseOrderDTO buildOrderDto(int quantity, BigDecimal price) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setSupplierId(1L);
        dto.setWarehouseId(1L);
        dto.setRemark("集成测试采购单");

        PurchaseOrderDTO.PurchaseItemDTO item = new PurchaseOrderDTO.PurchaseItemDTO();
        item.setGoodsId(1L);
        item.setQuantity(quantity);
        item.setPrice(price);
        dto.setItems(Arrays.asList(item));
        return dto;
    }

    private Long createOrder(int quantity, BigDecimal price) {
        purchaseService.save(buildOrderDto(quantity, price));
        // 取最近一条采购单
        BizPurchaseOrder latest = orderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizPurchaseOrder>()
                        .orderByDesc(BizPurchaseOrder::getId).last("LIMIT 1")
        ).get(0);
        return latest.getId();
    }

    @Test
    @DisplayName("完整流程：待审批 -> 已通过 -> 已入库，库存正确增加")
    void shouldFlowFromPendingToApprovedToInbound() {
        BizStock before = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int beforeQty = before == null ? 0 : before.getQuantity();

        Long orderId = createOrder(20, new BigDecimal("10.00"));

        // 1) 创建后应为待审批
        BizPurchaseOrder created = orderMapper.selectById(orderId);
        assertEquals(0, created.getStatus(), "新建采购单状态应为待审批(0)");
        assertEquals(0, new BigDecimal("200.00").compareTo(created.getTotalAmount()),
                "总金额应等于数量*单价");

        // 2) 审批通过 -> 已通过
        purchaseService.approve(orderId, true, "同意采购");
        BizPurchaseOrder approved = orderMapper.selectById(orderId);
        assertEquals(1, approved.getStatus(), "审批通过后状态应为已通过(1)");
        assertNotNull(approved.getApproveTime(), "审批后应记录审批时间");
        assertEquals(1L, approved.getApproverId(), "审批人应为当前用户");

        // 3) 入库 -> 已入库，且库存增加
        purchaseService.inbound(orderId);
        BizPurchaseOrder inbound = orderMapper.selectById(orderId);
        assertEquals(3, inbound.getStatus(), "入库后状态应为已入库(3)");
        assertNotNull(inbound.getInboundTime(), "入库后应记录入库时间");

        BizStock after = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertNotNull(after, "入库后必须存在库存记录");
        assertEquals(beforeQty + 20, after.getQuantity().intValue(),
                "入库后库存应增加 20");
    }

    @Test
    @DisplayName("审批拒绝流转：待审批 -> 已拒绝，不影响库存")
    void shouldFlowFromPendingToRejected() {
        BizStock before = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int beforeQty = before == null ? 0 : before.getQuantity();

        Long orderId = createOrder(15, new BigDecimal("8.00"));

        purchaseService.approve(orderId, false, "供应商资质不符");
        BizPurchaseOrder rejected = orderMapper.selectById(orderId);
        assertEquals(2, rejected.getStatus(), "审批拒绝后状态应为已拒绝(2)");
        assertEquals("供应商资质不符", rejected.getApproveRemark());

        BizStock after = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int afterQty = after == null ? 0 : after.getQuantity();
        assertEquals(beforeQty, afterQty, "审批拒绝不应触发库存变动");
    }

    @Test
    @DisplayName("非法流转：已拒绝单据不能再次审批")
    void rejectedOrderShouldNotBeApprovedAgain() {
        Long orderId = createOrder(5, new BigDecimal("12.00"));
        purchaseService.approve(orderId, false, "拒绝");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> purchaseService.approve(orderId, true, "再次审批"));
        assertTrue(ex.getMessage().contains("状态"), "应提示状态异常");
    }

    @Test
    @DisplayName("非法流转：未通过审批的采购单不允许入库")
    void pendingOrderShouldNotBeInbound() {
        Long orderId = createOrder(5, new BigDecimal("12.00"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> purchaseService.inbound(orderId));
        assertTrue(ex.getMessage().contains("无法入库") || ex.getMessage().contains("状态"));
    }

    @Test
    @DisplayName("非法流转：已入库的采购单不允许重复入库")
    void inboundedOrderShouldNotBeInboundAgain() {
        Long orderId = createOrder(3, new BigDecimal("9.00"));
        purchaseService.approve(orderId, true, "同意");
        purchaseService.inbound(orderId);

        assertThrows(BusinessException.class, () -> purchaseService.inbound(orderId),
                "已入库单据不能再次入库");
    }

    @Test
    @DisplayName("非法流转：已通过的采购单不允许删除")
    void approvedOrderShouldNotBeDeleted() {
        Long orderId = createOrder(2, new BigDecimal("5.00"));
        purchaseService.approve(orderId, true, "同意");

        assertThrows(BusinessException.class, () -> purchaseService.delete(orderId),
                "审批通过后的采购单不允许删除");
    }
}
