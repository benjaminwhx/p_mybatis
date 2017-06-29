package com.github.plugin.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: 吴海旭
 * Date: 2017-06-29
 * Time: 下午1:24
 */
public class DefaultMonitor implements Monitor {

	private Logger logger = LoggerFactory.getLogger(DefaultMonitor.class);

	@Override
	public void alarm(String msg) {
		logger.info(msg);
	}
}
