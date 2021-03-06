package com.xpay.pay.dao;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.xpay.pay.BaseSpringJunitTest;
import com.xpay.pay.model.Order;
import com.xpay.pay.proxy.IPaymentProxy.PayChannel;
import com.xpay.pay.proxy.PaymentResponse.OrderStatus;
import com.xpay.pay.util.TimeUtils;

public class OrderMapperTest extends BaseSpringJunitTest {
	@Autowired
	protected OrderMapper mapper;
	
	@Test
	public void testInsert() {
		Order order = new Order();
		order.setOrderNo("123");
		order.setAppId(10);
		order.setStoreId(100);
		order.setStoreChannelId(100);
		order.setTotalFee("0.01");
		order.setOrderTime("20170329142345");
		order.setPayChannel(PayChannel.ALIPAY);
		mapper.insert(order);
	}
	
	@Test
	public void testFindById() {
		Order order = mapper.findById(1000);
		System.out.println(order.getOrderNo());
	}
	
	@Test
	public void testFindByOrderNo() {
		List<Order> orders = mapper.findByOrderNo("123");
		System.out.println(orders.size());
	}
	
	@Test
	public void testUpdate() {
		Order order = mapper.findById(1002);
		order.setSellerOrderNo("456");
		order.setExtOrderNo("12a");
		order.setAttach("att=123");
		order.setDeviceId("d");
		order.setIp("1207.0.0.1");
		order.setNotifyUrl("http://localhost:8080/xpay/rest/v1/pay/notify/12a");
		order.setCodeUrl("http://ali.pay.com/asdfv");
		order.setStatus(OrderStatus.NOTPAY);
		mapper.updateById(order);
	}
	
	@Test
	public void testFindByStoreIdAndTime() {
		int storeId=53;
		Date startTime = TimeUtils.parseTime("2017-04-13 00:00:00", TimeUtils.TimePatternTime);
		Date endTime = TimeUtils.parseTime("2017-04-14 00:00:00", TimeUtils.TimePatternTime);
		List<Order> orders = mapper.findByStoreIdAndTime(storeId, startTime, endTime);
		System.out.println(orders.size());
	}
}
