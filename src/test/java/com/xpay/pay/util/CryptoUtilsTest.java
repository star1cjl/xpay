package com.xpay.pay.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.core.util.KeyValuePair;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

public class CryptoUtilsTest {
	@Test
	public void testMd5() {
		String str = "amount=0.01&app_id=148946099217658&busi_code=T2016060516001813420315&dev_id=1908a92d7d33&down_trade_no=58237477024932JrfDWLbLWR&oper_id=105&pay_channel=1&raw_data=1.0.5.105&subject=测试门店1&timestamp=1489463214&undiscountable_amount=0.00&version=v3&APP_SECRET=BTOOuffLDoVTPZsgDZgGapXsoBeKvTMT";
		String md5 = CryptoUtils.md5(str);
		Assert.assertEquals("b0540468252a9fc99cdaa18e0e62dd9c", md5);
	}

	@Test
	public void testMd52() {
		String str = "appid=wxd930ea5d5a258f4f&body=test&device_info=1000&mch_id=10000100&nonce_str=ibuaiVcKdpRxkhJA";
		String stringSignTemp = str + "&key=192006250b4c09247ec02edce69f6a2d";
		String sign = CryptoUtils.md5(stringSignTemp).toUpperCase();
		Assert.assertEquals("9A0A8659F005D6984697E2CA0A9CF3B7", sign);
	}

	@Test
	public void testStream() {
		for (int i = 0; i < 1000; i++) {
			String item = Arrays
					.asList("1", "2")
					.stream()
					.collect(
							Collectors.collectingAndThen(Collectors.toList(),
									collected -> {
										Collections.shuffle(collected);
										return collected.stream();
									})).findFirst().orElse(null);
			System.out.println(item);
		}
	}

	@Test
	public void testSign1() {
		String str = "payKey=ab828874445845469ece59792a7af982&orderPrice=10&outTradeNo=002&productType=40000303&orderTime=20170911172305&productName=测试&payBankAccountNo=123456789&orderIp=127.0.0.1&returnUrl=http://www.baidu.com&notifyUrl=http://106.14.47.193/xpay/notify/kekepay";
		List<KeyValuePair> keyPairs = new ArrayList<KeyValuePair>();	
		String[] keyValues = str.split("&");
		for(String keyValue: keyValues) {
			String[] split = keyValue.split("=");
			String key = split[0];
			String value = split[1];
			KeyValuePair pair = new KeyValuePair(key, value);
			keyPairs.add(pair);
		}
		
		String signature = this.signature(keyPairs, "paySecret", "5da3fbfa777e4189b41638b1f80f1e36");
		System.out.println(signature);
	}
	
	@Test
	public void testSign2() {
		String str = "busi_code=T2016093011252905060661&dev_id=127.0.0.1&amount=0.01&raw_data=a&down_trade_no=X003005120170914142313755789&subject=测试&redirect_url=http://106.14.47.193/xpay/notify/miaofu&app_id=149026948119189&timestamp=1505370193&version=v3";
		List<KeyValuePair> keyPairs = new ArrayList<KeyValuePair>();	
		String[] keyValues = str.split("&");
		for(String keyValue: keyValues) {
			String[] split = keyValue.split("=");
			String key = split[0];
			String value = split[1];
			KeyValuePair pair = new KeyValuePair(key, value);
			keyPairs.add(pair);
		}
		
		String signature = this.signature(keyPairs, "APP_SECRET", "DQaEecPIRgQCjIvNHWjJOarJLeGfIJap");
		System.out.println(signature);
	}

	private String signature(List<KeyValuePair> keyPairs, String signKey,
			String appSecret) {
		keyPairs.sort((x1, x2) -> {
			return x1.getKey().compareTo(x2.getKey());
		});

		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		for (KeyValuePair pair : keyPairs) {
			builder.queryParam(pair.getKey(), pair.getValue());
		}
		builder.queryParam(signKey, appSecret);
		String params = builder.build().toString().substring(1);
		System.out.println("sorted params: " + params);
		String md5 = CryptoUtils.md5(params);
		System.out.println("md5 upper: " + md5.toUpperCase());
		return md5 == null ? null : md5.toUpperCase();
	}
}