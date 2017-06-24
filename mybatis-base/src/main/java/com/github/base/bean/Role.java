package com.github.base.bean;

import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * User: 吴海旭
 * Date: 2017-06-24
 * Time: 下午1:56
 */
@Alias("role")
public class Role implements Serializable {
	private static final long serialVersionUID = 4214353179795248450L;
	private Long id;
	private String roleName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	@Override
	public String toString() {
		return "Role{" +
				"id=" + id +
				", roleName='" + roleName + '\'' +
				'}';
	}
}
