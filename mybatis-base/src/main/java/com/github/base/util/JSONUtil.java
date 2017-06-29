package com.github.base.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

/**
 * User: 吴海旭
 * Date: 2017-06-29
 * Time: 下午1:21
 */
public class JSONUtil {

	public static String bean2Json(Object bean) throws RuntimeException{
		return JSON.toJSONString(bean, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
	}

	public static <T> T json2Bean(String json, Class<T> clazz) throws RuntimeException{
		return JSON.parseObject(json, clazz);
	}

	public static <T> List<T> json2List(String json, Class<T> clazz) throws RuntimeException{
		return JSON.parseArray(json, clazz);
	}
}
