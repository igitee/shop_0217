/*
 * Copyright 2005-2017 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 */
package com.igomall.controller.admin;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.Setting;
import com.igomall.entity.Member;
import com.igomall.entity.Order;
import com.igomall.entity.OrderPayment;
import com.igomall.entity.OrderRefunds;
import com.igomall.entity.OrderShipping;
import com.igomall.service.DeliveryCorpService;
import com.igomall.service.MemberService;
import com.igomall.service.OrderService;
import com.igomall.service.OrderShippingService;
import com.igomall.service.PaymentMethodService;
import com.igomall.service.ShippingMethodService;
import com.igomall.util.SystemUtils;

/**
 * Controller - 订单
 * 
 * @author IGOMALL  Team
 * @version 1.0
 */
@Controller("adminOrderController")
@RequestMapping("/admin/order")
public class OrderController extends BaseController {

	@Inject
	private OrderService orderService;
	@Inject
	private ShippingMethodService shippingMethodService;
	@Inject
	private PaymentMethodService paymentMethodService;
	@Inject
	private DeliveryCorpService deliveryCorpService;
	@Inject
	private OrderShippingService orderShippingService;
	@Inject
	private MemberService memberService;

	/**
	 * 物流动态
	 */
	@GetMapping("/transit_step")
	public @ResponseBody Map<String, Object> transitStep(Long shippingId) {
		Map<String, Object> data = new HashMap<>();
		OrderShipping orderShipping = orderShippingService.find(shippingId);
		if (orderShipping == null) {
			data.put("message", ERROR_MESSAGE);
			return data;
		}
		Setting setting = SystemUtils.getSetting();
		if (StringUtils.isEmpty(setting.getKuaidi100Key()) || StringUtils.isEmpty(orderShipping.getDeliveryCorpCode()) || StringUtils.isEmpty(orderShipping.getTrackingNo())) {
			data.put("message", ERROR_MESSAGE);
			return data;
		}
		data.put("message", SUCCESS_MESSAGE);
		data.put("transitSteps", orderShippingService.getTransitSteps(orderShipping));
		return data;
	}

	/**
	 * 查看
	 */
	@GetMapping("/view")
	public String view(Long id, ModelMap model) {
		Setting setting = SystemUtils.getSetting();
		model.addAttribute("methods", OrderPayment.Method.values());
		model.addAttribute("refundsMethods", OrderRefunds.Method.values());
		model.addAttribute("paymentMethods", paymentMethodService.findAll());
		model.addAttribute("shippingMethods", shippingMethodService.findAll());
		model.addAttribute("deliveryCorps", deliveryCorpService.findAll());
		model.addAttribute("isKuaidi100Enabled", StringUtils.isNotEmpty(setting.getKuaidi100Key()));
		model.addAttribute("order", orderService.find(id));
		return "admin/order/view";
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	public String list(Order.Type type, Order.Status status, String memberUsername, Boolean isPendingReceive, Boolean isPendingRefunds, Boolean isAllocatedStock, Boolean hasExpired, Pageable pageable, ModelMap model) {
		model.addAttribute("types", Order.Type.values());
		model.addAttribute("statuses", Order.Status.values());
		model.addAttribute("type", type);
		model.addAttribute("status", status);
		model.addAttribute("memberUsername", memberUsername);
		model.addAttribute("isPendingReceive", isPendingReceive);
		model.addAttribute("isPendingRefunds", isPendingRefunds);
		model.addAttribute("isAllocatedStock", isAllocatedStock);
		model.addAttribute("hasExpired", hasExpired);

		Member member = memberService.findByUsername(memberUsername);
		if (StringUtils.isNotEmpty(memberUsername) && member == null) {
			model.addAttribute("page", Page.emptyPage(pageable));
		} else {
			model.addAttribute("page", orderService.findPage(type, status, null, member, null, isPendingReceive, isPendingRefunds, null, null, isAllocatedStock, hasExpired, pageable));
		}
		return "admin/order/list";
	}

}