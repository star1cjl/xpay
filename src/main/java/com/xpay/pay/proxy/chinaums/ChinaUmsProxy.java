package com.xpay.pay.proxy.chinaums;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.xpay.pay.exception.GatewayException;
import com.xpay.pay.model.Bill;
import com.xpay.pay.proxy.IPaymentProxy;
import com.xpay.pay.proxy.PaymentRequest;
import com.xpay.pay.proxy.PaymentResponse;
import com.xpay.pay.proxy.PaymentResponse.OrderStatus;
import com.xpay.pay.util.AppConfig;
import com.xpay.pay.util.CryptoUtils;
import com.xpay.pay.util.IDGenerator;

@Component
public class ChinaUmsProxy implements IPaymentProxy {
	protected final Logger logger = LogManager.getLogger("AccessLog");
	private static final AppConfig config = AppConfig.ChinaUmsConfig;
	private static final String baseEndpoint = config.getProperty("provider.endpoint");
	private static final String appId = config.getProperty("provider.app.id");
	private static final String appSecret = config.getProperty("provider.app.secret");
	private static final String appName = config.getProperty("provider.app.name");
	private static final String tId = config.getProperty("provider.tid");
	
	@Autowired
	RestTemplate chinaUmsProxy;
	
	@Override
	public PaymentResponse unifiedOrder(PaymentRequest request) {
		String qrCode = IDGenerator.buildQrCode(appId);
		String url = baseEndpoint.replace("{qrCodeId}", qrCode);
		logger.info("unifiedOrder POST: " + url);
		long l = System.currentTimeMillis();
		PaymentResponse response = null;
		try {
			ChinaUmsRequest chinaUmsRequest = this.toChinaUmsRequest(Method.UnifiedOrder,request);
			chinaUmsRequest.setQrCodeId(qrCode);
			
			List<KeyValuePair> keyPairs = this.getKeyPairs(chinaUmsRequest);
			String sign = this.signature(keyPairs, appSecret);
			chinaUmsRequest.setSign(sign);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
			HttpEntity<?> httpEntity = new HttpEntity<>(chinaUmsRequest, headers);
			ChinaUmsResponse chinaUmsResponse = chinaUmsProxy.exchange(url, HttpMethod.POST, httpEntity, ChinaUmsResponse.class).getBody();
			logger.info("unifiedOrder result: " + chinaUmsResponse.getErrorCode() + " "+chinaUmsResponse.getErrMsg() + ", took "
					+ (System.currentTimeMillis() - l) + "ms");
			response = toPaymentResponse(chinaUmsRequest, chinaUmsResponse);
		} catch (RestClientException e) {
			logger.info("microPay failed, took " + (System.currentTimeMillis() - l) + "ms", e);
			throw e;
		}
		return response;
	}

	@Override
	public PaymentResponse query(PaymentRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PaymentResponse refund(PaymentRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private ChinaUmsRequest toChinaUmsRequest(Method method, PaymentRequest request) {
		ChinaUmsRequest chinaUmsRequest = new ChinaUmsRequest();
		chinaUmsRequest.setMsgSrc(appName);
		chinaUmsRequest.setRequestTimeStamp(IDGenerator.formatTime());
		chinaUmsRequest.setMid(request.getExtStoreId());
		chinaUmsRequest.setTid(tId);
		chinaUmsRequest.setBillNo(request.getOrderNo());
		chinaUmsRequest.setBillDate(IDGenerator.formatDate());
		chinaUmsRequest.setBillDesc(request.getSubject());
		chinaUmsRequest.setTotalAmount(String.valueOf((int)(request.getTotalFeeAsFloat()*100)));
		chinaUmsRequest.setNotifyUrl(request.getNotifyUrl());
		chinaUmsRequest.setMsgType(method.getMsgType());
		return chinaUmsRequest;
	}
	
	private List<KeyValuePair> getKeyPairs(ChinaUmsRequest paymentRequest) {
		if (paymentRequest == null) {
			return null;
		}
		List<KeyValuePair> keyPairs = new ArrayList<KeyValuePair>();

		if (StringUtils.isNotBlank(paymentRequest.getMsgSrc())) {
			keyPairs.add(new KeyValuePair("msgSrc", paymentRequest.getMsgSrc()));
		}
		if (StringUtils.isNotBlank(paymentRequest.getRequestTimeStamp())) {
			keyPairs.add(new KeyValuePair("requestTimeStamp", paymentRequest
					.getRequestTimeStamp()));
		}
		if (StringUtils.isNotBlank(paymentRequest.getMid())) {
			keyPairs.add(new KeyValuePair("mid", paymentRequest
					.getMid()));
		}
		if (StringUtils.isNotBlank(paymentRequest.getTid())) {
			keyPairs.add(new KeyValuePair("tid", paymentRequest.getTid()));
		}
		if (StringUtils.isNotBlank(paymentRequest.getBillNo())) {
			keyPairs.add(new KeyValuePair("billNo", paymentRequest.getBillNo()));
		}
		if (StringUtils.isNotBlank(paymentRequest.getBillDate())) {
			keyPairs.add(new KeyValuePair("billDate", paymentRequest.getBillDate()));
		}
		if (StringUtils.isNotBlank(paymentRequest.getBillDesc())) {
			keyPairs.add(new KeyValuePair("billDesc", paymentRequest.getBillDesc()));
		}
		if (StringUtils.isNotBlank(paymentRequest.getTotalAmount())) {
			keyPairs.add(new KeyValuePair("totalAmount", paymentRequest.getTotalAmount()));
		}
		if (StringUtils.isNotBlank(paymentRequest.getQrCodeId())) {
			keyPairs.add(new KeyValuePair("qrCodeId", paymentRequest.getQrCodeId()));
		}
		if (StringUtils.isNotBlank(paymentRequest.getSystemId())) {
			keyPairs.add(new KeyValuePair("systemId", paymentRequest.getSystemId()));
		}
		if (StringUtils.isNotBlank(paymentRequest.getSign())) {
			keyPairs.add(new KeyValuePair("sign", paymentRequest.getSign()));
		}
		if (StringUtils.isNotBlank(paymentRequest.getNotifyUrl())) {
			keyPairs.add(new KeyValuePair("notifyUrl", paymentRequest.getNotifyUrl()));
		}
		if (StringUtils.isNotBlank(paymentRequest.getMsgType())) {
			keyPairs.add(new KeyValuePair("msgType", paymentRequest.getMsgType()));
		}
		keyPairs.sort((x1, x2) -> {
			return x1.getKey().compareTo(x2.getKey());
		});
		return keyPairs;
	}
	
	private String signature(List<KeyValuePair> keyPairs, String appSecret) {
		keyPairs.sort((x1, x2) -> {
			return x1.getKey().compareTo(x2.getKey());
		});
		
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		for(KeyValuePair pair : keyPairs) {
			builder.queryParam(pair.getKey(), pair.getValue());
		}
		builder.queryParam("key", appSecret);
		String params = builder.build().toString().substring(1);
		logger.debug("sorted params: "+params);
		String md5 = CryptoUtils.md5(params);
		logger.debug("md5 upper: "+md5.toUpperCase());
		return md5 == null? null:md5.toUpperCase();
	}

	private PaymentResponse toPaymentResponse(ChinaUmsRequest chinaUmsRequest, ChinaUmsResponse chinaUmsResponse) {
		if (chinaUmsResponse == null || !ChinaUmsResponse.SUCCESS.equals(chinaUmsResponse.getErrorCode())
				|| StringUtils.isBlank(chinaUmsResponse.getBillQRCode())) {
			String code = chinaUmsResponse == null ? NO_RESPONSE : chinaUmsResponse.getErrorCode();
			String msg = chinaUmsResponse == null ? "No response" : chinaUmsResponse.getErrMsg();
			throw new GatewayException(code, msg);
		}
		PaymentResponse response = new PaymentResponse();
		response.setCode(PaymentResponse.SUCCESS);
		Bill bill = new Bill();
		bill.setCodeUrl(chinaUmsResponse.getBillQRCode());
		bill.setOrderNo(chinaUmsRequest.getBillNo());
		bill.setGatewayOrderNo(chinaUmsRequest.getQrCodeId());
		bill.setOrderStatus(toOrderStatus(chinaUmsResponse.getBillStatus()));
		response.setBill(bill);
		return response;
	}

	private OrderStatus toOrderStatus(String billStatus) {
		if("PAID".equals(billStatus)) {
			return OrderStatus.SUCCESS;
		} else if("UNPAID".equals(billStatus)) {
			return OrderStatus.NOTPAY;
		}else if("REFUND".equals(billStatus)) {
			return OrderStatus.REFUND;
		}else if("CLOSED".equals(billStatus)) {
			return OrderStatus.CLOSED;
		} else {
			return OrderStatus.NOTPAY;
		}
		
	}


}