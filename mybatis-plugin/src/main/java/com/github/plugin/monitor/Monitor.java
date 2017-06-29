package com.github.plugin.monitor;

/**
 * User: 吴海旭
 * Date: 2017-06-29
 * Time: 下午1:24
 */
public interface Monitor {

	/**
	 * 报警
	 * @param msg 报警内容
	 */
	void alarm(String msg);
}
