package com.github.base.bean;

import org.apache.ibatis.type.Alias;

/**
 * User: 吴海旭
 * Date: 2017-06-13
 * Time: 下午6:38
 */
@Alias("sex")
public enum SEX {

	MALE(1, "男"),
	FEMALE(2, "女");

	private int code;
	private String des;

	SEX(int code, String des) {
		this.code = code;
		this.des = des;
	}

	public int getCode() {
		return code;
	}

	public String getDes() {
		return des;
	}

	public static SEX fromCode(int code) {
		for (SEX sex : SEX.values()) {
			if (sex.getCode() == code) {
				return sex;
			}
		}
		return null;
	}
}
