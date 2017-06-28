package com.github.plugin.id;

/**
 *
 * ID生成器接口
 *
 * User: 吴海旭
 * Date: 2017-06-28
 * Time: 下午5:30
 */
public interface IDGen {

	/**
	 * 根据传入的表名获得自增id
	 * @param tableName 表名
	 * @return long类型的自增id
	 */
	long newId(String tableName);
}
