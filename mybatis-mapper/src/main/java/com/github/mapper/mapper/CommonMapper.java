package com.github.mapper.mapper;

import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * User: 吴海旭
 * Date: 2017-06-25
 * Time: 下午3:35
 */
public interface CommonMapper<T> extends Mapper<T>, IdsMapper<T>, InsertLogIdMapper<T> {
}
