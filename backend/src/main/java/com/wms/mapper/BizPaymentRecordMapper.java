package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.BizPaymentRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BizPaymentRecordMapper extends BaseMapper<BizPaymentRecord> {
    
    @Select("SELECT p.*, u.real_name as operator_name " +
            "FROM biz_payment_record p " +
            "LEFT JOIN sys_user u ON p.operator_id = u.id " +
            "WHERE p.order_id = #{orderId} " +
            "ORDER BY p.create_time DESC")
    List<BizPaymentRecord> selectByOrderId(@Param("orderId") Long orderId);
}
