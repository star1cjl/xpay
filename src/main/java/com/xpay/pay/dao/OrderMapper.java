package com.xpay.pay.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.xpay.pay.model.Order;

public interface OrderMapper extends BaseMapper<Order> {
	List<Order> findByOrderNo(String orderNo);
	
	List<Order> findByExtOrderNo(String extOrderNo);
	
	List<Order> findByStoreIdAndTime(@Param("storeId")long storeId, @Param("startTime")Date startTime, @Param("endTime")Date endTime);
}
