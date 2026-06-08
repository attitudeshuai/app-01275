package com.wms.service;

import com.wms.common.BusinessException;
import com.wms.entity.BizSaleItem;
import com.wms.entity.BizSaleOrder;
import com.wms.entity.BizStock;
import com.wms.mapper.BizPaymentRecordMapper;
import com.wms.mapper.BizSaleOrderMapper;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 销售单据状态流转集成测试
 * 覆盖：报价中(-1) -> 待审核(0) -> 已审核(1) -> 已发货(2) -> 已收款(3)
 * 以及取消订单(0 -> 4) 的全流程
 * 同时校验：发货扣减库存、库存不能为负数、分多次收款、非法状态流转拦截等业务规则
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("销售单据状态流转集成测试")
class SaleOrderFlowIntegrationTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private BizSaleOrderMapper orderMapper;

    @Autowired
    private BizStockMapper stockMapper;

    @Autowired
    private BizPaymentRecordMapper paymentRecordMapper;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(1L);
        UserContext.setUsername("admin");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private BizSaleOrder buildOrder(int quantity, BigDecimal price) {
        BizSaleOrder order = new BizSaleOrder();
        order.setCustomerId(1L);
        order.setRemark("集成测试销售单");

        BizSaleItem item = new BizSaleItem();
        item.setGoodsId(1L);
        item.setQuantity(quantity);
        item.setPrice(price);

        List<BizSaleItem> items = new ArrayList<>();
        items.add(item);
        order.setItems(items);
        return order;
    }

    private Long latestOrderId() {
        return orderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizSaleOrder>()
                        .orderByDesc(BizSaleOrder::getId).last("LIMIT 1")
        ).get(0).getId();
    }

    @Test
    @DisplayName("完整流程：报价中 -> 待审核 -> 已审核 -> 已发货 -> 已收款")
    void shouldFlowFromQuoteToReceived() {
        BizStock before = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int beforeQty = before.getQuantity();

        // 1) 报价 (-1)
        saleService.quote(buildOrder(5, new BigDecimal("100.00")));
        Long orderId = latestOrderId();
        BizSaleOrder quoted = orderMapper.selectById(orderId);
        assertEquals(-1, quoted.getStatus(), "报价单状态应为报价中(-1)");
        assertTrue(quoted.getOrderNo().startsWith("BJ"), "报价单号应以BJ开头");
        assertEquals(0, new BigDecimal("500.00").compareTo(quoted.getTotalAmount()));

        // 2) 确认报价 -> 待审核 (0)
        saleService.confirmQuote(orderId);
        BizSaleOrder confirmed = orderMapper.selectById(orderId);
        assertEquals(0, confirmed.getStatus(), "确认报价后应为待审核(0)");
        assertTrue(confirmed.getOrderNo().startsWith("XS"), "确认后应转为销售单号XS");

        // 3) 审核通过 -> 已审核 (1)
        saleService.approve(orderId, true);
        BizSaleOrder approved = orderMapper.selectById(orderId);
        assertEquals(1, approved.getStatus(), "审核通过后应为已审核(1)");
        assertNotNull(approved.getApproveTime());

        // 4) 发货 -> 已发货 (2)，库存扣减
        saleService.ship(orderId, 1L);
        BizSaleOrder shipped = orderMapper.selectById(orderId);
        assertEquals(2, shipped.getStatus(), "发货后应为已发货(2)");
        assertEquals(1L, shipped.getWarehouseId().longValue());
        assertNotNull(shipped.getShipTime());

        BizStock afterShip = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertEquals(beforeQty - 5, afterShip.getQuantity().intValue(), "发货后库存应扣减5");

        // 5) 收款（一次付清） -> 已收款 (3)
        saleService.receive(orderId, new BigDecimal("500.00"), "BANK", "全额收款");
        BizSaleOrder received = orderMapper.selectById(orderId);
        assertEquals(3, received.getStatus(), "全额收款后应为已收款(3)");
        assertEquals(0, new BigDecimal("500.00").compareTo(received.getReceivedAmount()));
        assertNotNull(received.getReceiveTime());

        assertEquals(1, paymentRecordMapper.selectByOrderId(orderId).size(), "应生成1条收款记录");
    }

    @Test
    @DisplayName("分多次收款：部分收款不改变状态，全部收齐才进入已收款")
    void partialReceiveShouldKeepStatusUntilFullyPaid() {
        saleService.save(buildOrder(2, new BigDecimal("300.00"))); // total=600
        Long orderId = latestOrderId();
        saleService.approve(orderId, true);
        saleService.ship(orderId, 1L);

        // 第一次部分收款
        saleService.receive(orderId, new BigDecimal("200.00"), "CASH", "首付");
        BizSaleOrder afterFirst = orderMapper.selectById(orderId);
        assertEquals(2, afterFirst.getStatus(), "未收齐金额时仍应保持已发货(2)状态");
        assertEquals(0, new BigDecimal("200.00").compareTo(afterFirst.getReceivedAmount()));

        // 第二次收齐
        saleService.receive(orderId, new BigDecimal("400.00"), "BANK", "尾款");
        BizSaleOrder afterSecond = orderMapper.selectById(orderId);
        assertEquals(3, afterSecond.getStatus(), "收齐后应进入已收款(3)");
        assertEquals(0, new BigDecimal("600.00").compareTo(afterSecond.getReceivedAmount()));

        assertEquals(2, paymentRecordMapper.selectByOrderId(orderId).size(), "应生成2条收款记录");
    }

    @Test
    @DisplayName("取消订单流转：待审核 -> 已取消(4)")
    void shouldCancelPendingOrder() {
        saleService.save(buildOrder(1, new BigDecimal("100.00")));
        Long orderId = latestOrderId();

        saleService.approve(orderId, false);
        BizSaleOrder cancelled = orderMapper.selectById(orderId);
        assertEquals(4, cancelled.getStatus(), "审核拒绝/取消后状态应为已取消(4)");
    }

    @Test
    @DisplayName("库存不能为负数：发货数量超过库存应抛业务异常且库存不变")
    void shipShouldFailWhenStockInsufficient() {
        BizStock before = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        int beforeQty = before.getQuantity();

        // 构造数量大于现有库存的销售单
        saleService.save(buildOrder(beforeQty + 1, new BigDecimal("50.00")));
        Long orderId = latestOrderId();
        saleService.approve(orderId, true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> saleService.ship(orderId, 1L));
        assertTrue(ex.getMessage().contains("库存不足"), "应提示库存不足");

        // 库存不能变为负数
        BizStock after = stockMapper.selectByGoodsAndWarehouse(1L, 1L);
        assertTrue(after.getQuantity() >= 0, "库存不允许出现负数");
    }

    @Test
    @DisplayName("非法流转：未审核的销售单不允许发货")
    void pendingOrderShouldNotBeShipped() {
        saleService.save(buildOrder(1, new BigDecimal("10.00")));
        Long orderId = latestOrderId();

        assertThrows(BusinessException.class, () -> saleService.ship(orderId, 1L),
                "未审核的销售单不允许发货");
    }

    @Test
    @DisplayName("非法流转：未发货的销售单不允许收款")
    void approvedOrderShouldNotBeReceived() {
        saleService.save(buildOrder(1, new BigDecimal("10.00")));
        Long orderId = latestOrderId();
        saleService.approve(orderId, true);

        assertThrows(BusinessException.class,
                () -> saleService.receive(orderId, new BigDecimal("10.00"), "CASH", "提前收款"),
                "未发货的销售单不允许收款");
    }

    @Test
    @DisplayName("非法流转：已审核的销售单不允许重复审核")
    void approvedOrderShouldNotBeApprovedAgain() {
        saleService.save(buildOrder(1, new BigDecimal("10.00")));
        Long orderId = latestOrderId();
        saleService.approve(orderId, true);

        assertThrows(BusinessException.class, () -> saleService.approve(orderId, true),
                "已审核的销售单不允许重复审核");
    }

    @Test
    @DisplayName("非法流转：报价单状态错误时不能确认")
    void shouldNotConfirmNonQuoteOrder() {
        saleService.save(buildOrder(1, new BigDecimal("10.00"))); // 直接创建为待审核(0)
        Long orderId = latestOrderId();

        assertThrows(BusinessException.class, () -> saleService.confirmQuote(orderId),
                "非报价中的销售单不允许确认报价");
    }

    @Test
    @DisplayName("非法流转：已发货的销售单不允许删除")
    void shippedOrderShouldNotBeDeleted() {
        saleService.save(buildOrder(1, new BigDecimal("10.00")));
        Long orderId = latestOrderId();
        saleService.approve(orderId, true);
        saleService.ship(orderId, 1L);

        assertThrows(BusinessException.class, () -> saleService.delete(orderId),
                "已发货的销售单不允许删除");
    }
}
