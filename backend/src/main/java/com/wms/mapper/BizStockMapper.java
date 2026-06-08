package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.BizStock;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BizStockMapper extends BaseMapper<BizStock> {
    
    IPage<BizStock> selectStockPage(IPage<BizStock> page, @Param("goodsName") String goodsName,
                                     @Param("warehouseId") Long warehouseId);
    
    List<BizStock> selectWarningList();
    
    IPage<BizStock> selectWarningPage(IPage<BizStock> page);
    
    List<BizStock> selectOverflowList();
    
    IPage<BizStock> selectOverflowPage(IPage<BizStock> page);
    
    List<BizStock> selectAllWithGoods();
    
    BizStock selectByGoodsAndWarehouse(@Param("goodsId") Long goodsId, @Param("warehouseId") Long warehouseId);
    
    /**
     * 查询库存及关联商品信息（用于即时预警检查）
     */
    BizStock selectStockWithGoods(@Param("goodsId") Long goodsId);
    
    /**
     * 原子增加库存（入库操作）
     * @return 影响行数
     */
    int atomicIncreaseStock(@Param("goodsId") Long goodsId, @Param("warehouseId") Long warehouseId, 
                            @Param("quantity") int quantity);
    
    /**
     * 原子减少库存（出库操作，带库存充足校验）
     * @return 影响行数，0表示库存不足
     */
    int atomicDecreaseStock(@Param("goodsId") Long goodsId, @Param("warehouseId") Long warehouseId, 
                            @Param("quantity") int quantity);
}
