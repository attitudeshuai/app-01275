package com.wms.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.*;
import com.wms.mapper.*;
import com.wms.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final BizStockMapper stockMapper;
    private final BizStockRecordMapper recordMapper;
    private final BizStockCheckMapper checkMapper;
    private final BizStockCheckItemMapper checkItemMapper;
    private final BizStockTransferMapper transferMapper;
    private final BizStockAdjustMapper adjustMapper;
    private final NotificationService notificationService;

    public PageResult<BizStock> page(int page, int size, String goodsName, Long warehouseId) {
        IPage<BizStock> result = stockMapper.selectStockPage(new Page<>(page, size), goodsName, warehouseId);
        return PageResult.of(result);
    }

    public List<BizStock> warningList() {
        return stockMapper.selectWarningList();
    }

    public PageResult<BizStock> warningPage(int page, int size, String type) {
        IPage<BizStock> result;
        if ("high".equals(type)) {
            result = stockMapper.selectOverflowPage(new Page<>(page, size));
        } else {
            result = stockMapper.selectWarningPage(new Page<>(page, size));
        }
        return PageResult.of(result);
    }

    public PageResult<BizStockRecord> recordPage(int page, int size, Long goodsId, Long warehouseId, String recordType) {
        IPage<BizStockRecord> result = recordMapper.selectRecordPage(new Page<>(page, size), goodsId, warehouseId, recordType);
        return PageResult.of(result);
    }

    @Transactional
    public void updateStock(Long goodsId, Long warehouseId, int quantity, String recordType, 
                           String refType, Long refId, String refNo, String remark) {
        BizStock stock = stockMapper.selectByGoodsAndWarehouse(goodsId, warehouseId);
        int beforeQty = 0;
        int afterQty;
        
        // 如果库存记录不存在，先创建
        if (stock == null) {
            stock = new BizStock();
            stock.setGoodsId(goodsId);
            stock.setWarehouseId(warehouseId);
            stock.setQuantity(0);
            stockMapper.insert(stock);
        }
        beforeQty = stock.getQuantity();
        
        // 使用原子更新防止并发问题
        int affected;
        if ("IN".equals(recordType)) {
            // 入库：原子增加库存
            affected = stockMapper.atomicIncreaseStock(goodsId, warehouseId, quantity);
            afterQty = beforeQty + quantity;
        } else {
            // 出库：原子减少库存（带库存充足校验）
            affected = stockMapper.atomicDecreaseStock(goodsId, warehouseId, quantity);
            if (affected == 0) {
                // 原子更新失败，说明库存不足
                throw new BusinessException("库存不足，当前库存: " + beforeQty + "，需要: " + quantity);
            }
            afterQty = beforeQty - quantity;
        }
        
        // 记录库存变动流水
        BizStockRecord record = new BizStockRecord();
        record.setGoodsId(goodsId);
        record.setWarehouseId(warehouseId);
        record.setRecordType(recordType);
        record.setQuantity(quantity);
        record.setBeforeQty(beforeQty);
        record.setAfterQty(afterQty);
        record.setRefType(refType);
        record.setRefId(refId);
        record.setRefNo(refNo);
        record.setOperatorId(UserContext.getUserId());
        record.setRemark(remark);
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
        
        log.info("库存变动: goodsId={}, warehouseId={}, type={}, qty={}, before={}, after={}", 
                 goodsId, warehouseId, recordType, quantity, beforeQty, afterQty);
        
        // 即时库存预警检查
        checkStockWarningImmediate(goodsId, afterQty);
    }
    
    /**
     * 即时库存预警检查
     * 在库存变动后立即检查是否触发预警阈值
     */
    private void checkStockWarningImmediate(Long goodsId, int currentQty) {
        try {
            BizStock stockInfo = stockMapper.selectStockWithGoods(goodsId);
            if (stockInfo == null) return;
            
            Integer safetyStock = stockInfo.getSafetyStock();
            Integer maxStock = stockInfo.getMaxStock();
            String goodsName = stockInfo.getGoodsName();
            
            // 检查低库存预警
            if (safetyStock != null && currentQty < safetyStock && currentQty > 0) {
                notificationService.createLowStockWarningNotification(
                    goodsName, currentQty, safetyStock, goodsId
                );
                log.warn("即时低库存预警: {} 当前库存={}, 安全库存={}", goodsName, currentQty, safetyStock);
            }
            
            // 检查高库存预警
            if (maxStock != null && currentQty > maxStock) {
                notificationService.createHighStockWarningNotification(
                    goodsName, currentQty, maxStock, goodsId
                );
                log.warn("即时高库存预警: {} 当前库存={}, 最大库存={}", goodsName, currentQty, maxStock);
            }
        } catch (Exception e) {
            // 预警检查失败不影响主流程
            log.error("即时库存预警检查失败: goodsId={}", goodsId, e);
        }
    }

    // 盘点相关
    public PageResult<BizStockCheck> checkPage(int page, int size, String checkNo, Long warehouseId, Integer status) {
        IPage<BizStockCheck> result = checkMapper.selectCheckPage(new Page<>(page, size), checkNo, warehouseId, status);
        return PageResult.of(result);
    }

    public BizStockCheck getCheckById(Long id) {
        BizStockCheck check = checkMapper.selectCheckById(id);
        if (check != null) {
            check.setItems(checkItemMapper.selectByCheckId(id));
        }
        return check;
    }

    @Transactional
    public void createCheck(BizStockCheck check) {
        check.setCheckNo("PD" + IdUtil.getSnowflakeNextIdStr());
        check.setStatus(0);
        check.setOperatorId(UserContext.getUserId());
        check.setCreateTime(LocalDateTime.now());
        checkMapper.insert(check);
        
        if (check.getItems() != null) {
            for (BizStockCheckItem item : check.getItems()) {
                item.setCheckId(check.getId());
                item.setDiffQty(item.getActualQty() - item.getSystemQty());
                checkItemMapper.insert(item);
            }
        }
        log.info("创建盘点单: {}", check.getCheckNo());
    }

    @Transactional
    public void confirmCheck(Long checkId) {
        BizStockCheck check = checkMapper.selectById(checkId);
        if (check == null || check.getStatus() != 0) {
            throw new BusinessException("盘点单状态异常");
        }
        
        List<BizStockCheckItem> items = checkItemMapper.selectByCheckId(checkId);
        for (BizStockCheckItem item : items) {
            if (item.getDiffQty() != 0) {
                String type = item.getDiffQty() > 0 ? "IN" : "OUT";
                int qty = Math.abs(item.getDiffQty());
                updateStock(item.getGoodsId(), check.getWarehouseId(), qty, type, 
                           "CHECK", checkId, check.getCheckNo(), "盘点调整");
            }
        }
        
        check.setStatus(1);
        check.setFinishTime(LocalDateTime.now());
        checkMapper.updateById(check);
        log.info("确认盘点单: {}", check.getCheckNo());
    }

    // 调拨相关
    public PageResult<BizStockTransfer> transferPage(int page, int size, Long goodsId) {
        IPage<BizStockTransfer> result = transferMapper.selectTransferPage(new Page<>(page, size), goodsId);
        return PageResult.of(result);
    }

    @Transactional
    public void createTransfer(BizStockTransfer transfer) {
        transfer.setTransferNo("DB" + IdUtil.getSnowflakeNextIdStr());
        transfer.setStatus(1);
        transfer.setOperatorId(UserContext.getUserId());
        transfer.setCreateTime(LocalDateTime.now());
        
        updateStock(transfer.getGoodsId(), transfer.getFromWarehouseId(), transfer.getQuantity(), 
                   "OUT", "TRANSFER", null, transfer.getTransferNo(), "调拨出库");
        updateStock(transfer.getGoodsId(), transfer.getToWarehouseId(), transfer.getQuantity(), 
                   "IN", "TRANSFER", null, transfer.getTransferNo(), "调拨入库");
        
        transferMapper.insert(transfer);
        log.info("库存调拨: {}", transfer.getTransferNo());
    }

    // 报损报溢相关
    public PageResult<BizStockAdjust> adjustPage(int page, int size, String adjustType, Long warehouseId) {
        IPage<BizStockAdjust> result = adjustMapper.selectAdjustPage(new Page<>(page, size), adjustType, warehouseId);
        return PageResult.of(result);
    }

    @Transactional
    public void createAdjust(BizStockAdjust adjust) {
        adjust.setAdjustNo("TZ" + IdUtil.getSnowflakeNextIdStr());
        adjust.setOperatorId(UserContext.getUserId());
        adjust.setCreateTime(LocalDateTime.now());
        
        String recordType = "OVERFLOW".equals(adjust.getAdjustType()) ? "IN" : "OUT";
        String remark = "OVERFLOW".equals(adjust.getAdjustType()) ? "报溢入库" : "报损出库";
        
        updateStock(adjust.getGoodsId(), adjust.getWarehouseId(), adjust.getQuantity(), 
                   recordType, "ADJUST", null, adjust.getAdjustNo(), remark);
        
        adjustMapper.insert(adjust);
        log.info("库存调整: {} - {}", adjust.getAdjustNo(), adjust.getAdjustType());
    }
}
