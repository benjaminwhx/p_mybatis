package com.github.base.bean;

import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * User: 吴海旭
 * Date: 2017-06-24
 * Time: 下午0:59
 */
@Alias("order")
public class Order implements Serializable {
	private static final long serialVersionUID = 7799983304195819315L;
	private Long id;
	private String orderId;
	private String orderName;
	private User user;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderName() {
		return orderName;
	}

	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Order{" +
				"id=" + id +
				", orderId=" + orderId +
				", orderName='" + orderName + '\'' +
				", user=" + user +
				'}';
	}
}
