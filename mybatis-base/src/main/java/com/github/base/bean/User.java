package com.github.base.bean;

import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.List;

/**
 * User: 吴海旭
 * Date: 2017-06-24
 * Time: 下午0:55
 */
@Alias("user")
public class User implements Serializable {
	private static final long serialVersionUID = -6532514335200325000L;
	private Long id;
	private String userName;
	private SEX sex;
	private Long roleId;
	private List<Order> orderList;
	private Role role;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public SEX getSex() {
		return sex;
	}

	public void setSex(SEX sex) {
		this.sex = sex;
	}

	public List<Order> getOrderList() {
		return orderList;
	}

	public void setOrderList(List<Order> orderList) {
		this.orderList = orderList;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", userName='" + userName + '\'' +
				", sex=" + sex +
				", roleId=" + roleId +
				", orderList=" + orderList +
				", role=" + role +
				'}';
	}
}
