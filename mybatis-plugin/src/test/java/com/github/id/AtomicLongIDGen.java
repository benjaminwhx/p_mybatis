package com.github.id;

import com.github.plugin.id.IDGen;

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * 仅限测试使用
 *
 * User: 吴海旭
 * Date: 2017-06-28
 * Time: 下午6:09
 */
public class AtomicLongIDGen implements IDGen {

	public static AtomicLong identity = new AtomicLong(10);

	public long newId(String tableName) {
		return identity.incrementAndGet();
	}
}
