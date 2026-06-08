package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.BizGoods;
import org.apache.ibatis.annotations.Param;

public interface BizGoodsMapper extends BaseMapper<BizGoods> {
    
    IPage<BizGoods> selectGoodsPage(IPage<BizGoods> page, @Param("goodsCode") String goodsCode,
                                     @Param("goodsName") String goodsName, @Param("categoryId") Long categoryId);
}
