package com.xpay.pay.notify;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xpay.pay.controller.PayNotifyServlet.NotifyResponse;
import com.xpay.pay.model.Order;
import com.xpay.pay.proxy.PaymentResponse.OrderStatus;
import com.xpay.pay.service.OrderService;
import com.xpay.pay.service.PaymentService;
import com.xpay.pay.util.CommonUtils;

@Service
public abstract class AbstractNotifyHandler implements INotifyHandler {
	protected final Logger logger = LogManager.getLogger("AccessLog");
	
	@Autowired
	protected OrderService orderService;
	@Autowired
	protected PaymentService paymentService;
	
	@Override
	public boolean validateSignature(String content) {
		return true;
	}
	
	@Override
	public boolean isRedrect() {
		return false;
	}
	
	@Override
	public NotifyResponse handleNotification(String url, String content) {
		Order order = null;
		String respString = getFailedResponse();

		NotifyBody body = this.extractNotifyBody(url, content);
		if(body!=null) {
			order = fetchOrder(body);
			if(order!=null &&  CommonUtils.toInt(body.getTotalFee()) == (int) (order.getTotalFeeAsFloat() * 100)) {
				updateOrderStatus(order, body);
				updateBail(order);
			}
		} else {
			logger.warn("Cannot parse notify content "+content);
		}
		respString = getSuccessResponse();
		
		NotifyResponse response = new NotifyResponse(respString, order);
		response.setRedirect(this.isRedrect());
		return response;
	}
	
	private static final String DEFAULT_CONTENT_TYPE = "text/html;charset=UTF-8";
	@Override
	public String getContentType() {
		return DEFAULT_CONTENT_TYPE;
	}
	
	private static final String DEFAULT_CHARACTER_ENCODING = "utf-8";
	@Override
	public String getCharacterEncoding() {
		return DEFAULT_CHARACTER_ENCODING;
	}
	
	private Order fetchOrder(NotifyBody body) {
		String billNo = body.getBillNo();
		String extOrderNo = body.getExtOrderNo();
		if(StringUtils.isNotBlank(billNo)) {
			return orderService.findActiveByOrderNo(billNo);
		} else {
			return orderService.findActiveByExtOrderNo(extOrderNo);
		}
	}
	
	private void updateOrderStatus(Order order, NotifyBody body) {
		if (order != null && !OrderStatus.SUCCESS.equals(order.getStatus())) {
			order.setStatus(body.getStatus());
			if(StringUtils.isNotBlank(body.getExtOrderNo())) {
				order.setExtOrderNo(body.getExtOrderNo());
			}
			order.setTargetOrderNo(body.getTargetOrderNo());
			orderService.update(order);
		}
	}
	
	private void updateBail(Order order) {
		if(order!=null && OrderStatus.SUCCESS.equals(order.getStatus())) {
			paymentService.updateBail(order, true);
		}
	}
	
	protected abstract NotifyBody extractNotifyBody(String url, String content);
	protected abstract String getSuccessResponse();
	protected abstract String getFailedResponse();

	protected static class NotifyBody {
		private String billNo;
		private OrderStatus status;
		private String extOrderNo;
		private String targetOrderNo;
		private String totalFee;
		
		public NotifyBody(String billNo, String extOrderNo, OrderStatus status, String totalFee, String targetOrderNo) {
			this.billNo = billNo;
			this.extOrderNo = extOrderNo;
			this.status = status;
			this.totalFee = totalFee;
			this.targetOrderNo = targetOrderNo;
		}
		
		public String getBillNo() {
			return billNo;
		}
		public void setBillNo(String billNo) {
			this.billNo = billNo;
		}
		
		public OrderStatus getStatus() {
			return status;
		}
		
		public String getExtOrderNo() {
			return extOrderNo;
		}

		public void setExtOrderNo(String extOrderNo) {
			this.extOrderNo = extOrderNo;
		}

		public void setStatus(OrderStatus status) {
			this.status = status;
		}
		public String getTargetOrderNo() {
			return targetOrderNo;
		}
		public void setTargetOrderNo(String targetOrderNo) {
			this.targetOrderNo = targetOrderNo;
		}
		public String getTotalFee() {
			return totalFee;
		}

		public void setTotalFee(String totalFee) {
			this.totalFee = totalFee;
		}
	}
}
