package com.github.base.bean;

import java.io.Serializable;

/**
 * User: 吴海旭
 * Date: 2017-06-28
 * Time: 下午6:11
 */
public class Student implements Serializable {
	private static final long serialVersionUID = 3246689245235341481L;
	private Long id;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Student{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}
