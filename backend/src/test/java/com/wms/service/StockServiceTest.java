package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.*;
import com.wms.mapper.*;
import com.wms.util.UserContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private BizStockMapper stockMapper;
    @Mock
    private BizStockRecordMapper recordMapper;
    @Mock
    private BizStockCheckMapper checkMapper;
    @Mock
    private BizStockCheckItemMapper checkItemMapper;
    @Mock
    private BizStockTransferMapper transferMapper;
    @Mock
    private BizStockAdjustMapper adjustMapper;

    @InjectMocks
    private StockService stockService;

    @Test
    void page_ShouldReturnPageResult() {
        Page<BizStock> page = new Page<>(1, 10);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);

        when(stockMapper.selectStockPage(any(IPage.class), isNull(), isNull())).thenReturn(page);

        PageResult<BizStock> result = stockService.page(1, 10, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    void warningList_ShouldReturnWarningStocks() {
        BizStock stock = new BizStock();
        stock.setId(1L);
        stock.setQuantity(5);

        when(stockMapper.selectWarningList()).thenReturn(Arrays.asList(stock));

        List<BizStock> result = stockService.warningList();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateStock_Inbound_ShouldIncreaseStock() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizStock stock = new BizStock();
            stock.setId(1L);
            stock.setGoodsId(1L);
            stock.setWarehouseId(1L);
            stock.setQuantity(100);

            when(stockMapper.selectByGoodsAndWarehouse(1L, 1L)).thenReturn(stock);
            when(stockMapper.updateById(any(BizStock.class))).thenReturn(1);
            when(recordMapper.insert(any(BizStockRecord.class))).thenReturn(1);

            assertDoesNotThrow(() -> stockService.updateStock(1L, 1L, 50, "IN", "PURCHASE", 1L, "CG001", "采购入库"));
            verify(stockMapper).updateById(argThat(s -> ((BizStock)s).getQuantity() == 150));
        }
    }

    @Test
    void updateStock_Outbound_ShouldDecreaseStock() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizStock stock = new BizStock();
            stock.setId(1L);
            stock.setGoodsId(1L);
            stock.setWarehouseId(1L);
            stock.setQuantity(100);

            when(stockMapper.selectByGoodsAndWarehouse(1L, 1L)).thenReturn(stock);
            when(stockMapper.updateById(any(BizStock.class))).thenReturn(1);
            when(recordMapper.insert(any(BizStockRecord.class))).thenReturn(1);

            assertDoesNotThrow(() -> stockService.updateStock(1L, 1L, 50, "OUT", "SALE", 1L, "XS001", "销售出库"));
            verify(stockMapper).updateById(argThat(s -> ((BizStock)s).getQuantity() == 50));
        }
    }

    @Test
    void updateStock_InsufficientStock_ShouldThrowException() {
        BizStock stock = new BizStock();
        stock.setId(1L);
        stock.setQuantity(10);

        when(stockMapper.selectByGoodsAndWarehouse(1L, 1L)).thenReturn(stock);

        assertThrows(BusinessException.class, () -> 
            stockService.updateStock(1L, 1L, 50, "OUT", "SALE", 1L, "XS001", "销售出库"));
    }

    @Test
    void updateStock_NewStock_ShouldCreateAndUpdate() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            when(stockMapper.selectByGoodsAndWarehouse(1L, 1L)).thenReturn(null);
            when(stockMapper.insert(any(BizStock.class))).thenReturn(1);
            when(stockMapper.updateById(any(BizStock.class))).thenReturn(1);
            when(recordMapper.insert(any(BizStockRecord.class))).thenReturn(1);

            assertDoesNotThrow(() -> stockService.updateStock(1L, 1L, 50, "IN", "PURCHASE", 1L, "CG001", "采购入库"));
            verify(stockMapper).insert(any(BizStock.class));
        }
    }

    @Test
    void checkPage_ShouldReturnPageResult() {
        Page<BizStockCheck> page = new Page<>(1, 10);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);

        when(checkMapper.selectCheckPage(any(IPage.class), isNull(), isNull(), isNull())).thenReturn(page);

        PageResult<BizStockCheck> result = stockService.checkPage(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    void createCheck_ShouldCreateCheckOrder() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizStockCheck check = new BizStockCheck();
            check.setWarehouseId(1L);
            
            BizStockCheckItem item = new BizStockCheckItem();
            item.setGoodsId(1L);
            item.setSystemQty(100);
            item.setActualQty(95);
            check.setItems(Arrays.asList(item));

            when(checkMapper.insert(any(BizStockCheck.class))).thenReturn(1);
            when(checkItemMapper.insert(any(BizStockCheckItem.class))).thenReturn(1);

            assertDoesNotThrow(() -> stockService.createCheck(check));
            verify(checkMapper).insert(any(BizStockCheck.class));
        }
    }

    @Test
    void confirmCheck_ShouldAdjustStock() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizStockCheck check = new BizStockCheck();
            check.setId(1L);
            check.setCheckNo("PD001");
            check.setWarehouseId(1L);
            check.setStatus(0);

            BizStockCheckItem item = new BizStockCheckItem();
            item.setGoodsId(1L);
            item.setDiffQty(-5);

            BizStock stock = new BizStock();
            stock.setId(1L);
            stock.setQuantity(100);

            when(checkMapper.selectById(1L)).thenReturn(check);
            when(checkItemMapper.selectByCheckId(1L)).thenReturn(Arrays.asList(item));
            when(stockMapper.selectByGoodsAndWarehouse(anyLong(), anyLong())).thenReturn(stock);
            when(stockMapper.updateById(any(BizStock.class))).thenReturn(1);
            when(recordMapper.insert(any(BizStockRecord.class))).thenReturn(1);
            when(checkMapper.updateById(any(BizStockCheck.class))).thenReturn(1);

            assertDoesNotThrow(() -> stockService.confirmCheck(1L));
            verify(checkMapper).updateById(any(BizStockCheck.class));
        }
    }

    @Test
    void createTransfer_ShouldTransferStock() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizStockTransfer transfer = new BizStockTransfer();
            transfer.setGoodsId(1L);
            transfer.setFromWarehouseId(1L);
            transfer.setToWarehouseId(2L);
            transfer.setQuantity(50);

            BizStock fromStock = new BizStock();
            fromStock.setId(1L);
            fromStock.setQuantity(100);

            when(stockMapper.selectByGoodsAndWarehouse(1L, 1L)).thenReturn(fromStock);
            when(stockMapper.selectByGoodsAndWarehouse(1L, 2L)).thenReturn(null);
            when(stockMapper.insert(any(BizStock.class))).thenReturn(1);
            when(stockMapper.updateById(any(BizStock.class))).thenReturn(1);
            when(recordMapper.insert(any(BizStockRecord.class))).thenReturn(1);
            when(transferMapper.insert(any(BizStockTransfer.class))).thenReturn(1);

            assertDoesNotThrow(() -> stockService.createTransfer(transfer));
            verify(transferMapper).insert(any(BizStockTransfer.class));
        }
    }

    @Test
    void createAdjust_Overflow_ShouldIncreaseStock() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizStockAdjust adjust = new BizStockAdjust();
            adjust.setGoodsId(1L);
            adjust.setWarehouseId(1L);
            adjust.setQuantity(10);
            adjust.setAdjustType("OVERFLOW");

            BizStock stock = new BizStock();
            stock.setId(1L);
            stock.setQuantity(100);

            when(stockMapper.selectByGoodsAndWarehouse(1L, 1L)).thenReturn(stock);
            when(stockMapper.updateById(any(BizStock.class))).thenReturn(1);
            when(recordMapper.insert(any(BizStockRecord.class))).thenReturn(1);
            when(adjustMapper.insert(any(BizStockAdjust.class))).thenReturn(1);

            assertDoesNotThrow(() -> stockService.createAdjust(adjust));
            verify(adjustMapper).insert(any(BizStockAdjust.class));
        }
    }

    @Test
    void createAdjust_Loss_ShouldDecreaseStock() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            BizStockAdjust adjust = new BizStockAdjust();
            adjust.setGoodsId(1L);
            adjust.setWarehouseId(1L);
            adjust.setQuantity(10);
            adjust.setAdjustType("LOSS");

            BizStock stock = new BizStock();
            stock.setId(1L);
            stock.setQuantity(100);

            when(stockMapper.selectByGoodsAndWarehouse(1L, 1L)).thenReturn(stock);
            when(stockMapper.updateById(any(BizStock.class))).thenReturn(1);
            when(recordMapper.insert(any(BizStockRecord.class))).thenReturn(1);
            when(adjustMapper.insert(any(BizStockAdjust.class))).thenReturn(1);

            assertDoesNotThrow(() -> stockService.createAdjust(adjust));
            verify(adjustMapper).insert(any(BizStockAdjust.class));
        }
    }
}
