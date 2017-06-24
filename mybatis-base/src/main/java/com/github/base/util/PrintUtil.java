package com.github.base.util;

import org.slf4j.Logger;

import java.util.List;

/**
 * User: 吴海旭
 * Date: 2017-06-24
 * Time: 下午1:21
 */
public class PrintUtil {

	public static void printList(List<? extends Object> list, Logger logger) {
		for (Object object : list) {
			logger.info(object.toString());
		}
	}
}
