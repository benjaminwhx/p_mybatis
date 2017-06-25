package com.github.mapper.mapper;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import tk.mybatis.mapper.provider.SpecialProvider;

/**
 * User: 吴海旭
 * Date: 2017-06-25
 * Time: 下午3:47
 * 主键不是id，自定义mapper
 */
public interface InsertLogIdMapper<T> {

    @Options(useGeneratedKeys = true, keyProperty = "logid")
    @InsertProvider(type = SpecialProvider.class, method = "dynamicSQL")
    int insertUseGeneratedKeys(T record);
}
