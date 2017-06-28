package com.github.base.util;

import java.lang.reflect.Field;

/**
 * User: 吴海旭
 * Date: 2017-06-17
 * Time: 上午11:27
 */
public class ReflectUtil {

	/**
	 * 利用反射获取指定对象的指定属性
	 * @param obj 目标对象
	 * @param fieldName 目标属性
	 * @return 目标属性的值
	 */
	public static Object getFieldValue(Object obj, String fieldName) {
		Object result = null;
		Field field = getField(obj, fieldName);
		if (field != null) {
			field.setAccessible(true);
			try {
				result = field.get(obj);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 利用反射获取指定对象里面的指定属性
	 * @param obj 目标对象
	 * @param fieldName 目标属性
	 * @return 目标字段
	 */
	private static Field getField(Object obj, String fieldName) {
		Field field = null;
		for (Class<?> clazz=obj.getClass(); clazz != Object.class; clazz=clazz.getSuperclass()) {
			try {
				field = clazz.getDeclaredField(fieldName);
				break;
			} catch (NoSuchFieldException e) {
				//这里不用做处理，子类没有该字段可能对应的父类有，都没有就返回null。
			}
		}
		return field;
	}

	/**
	 * 利用反射设置指定对象的指定属性为指定的值
	 * @param obj 目标对象
	 * @param fieldName 目标属性
	 * @param fieldValue 目标值
	 */
	public static void setFieldValue(Object obj, String fieldName,
									 Object fieldValue) {
		Field field = ReflectUtil.getField(obj, fieldName);
		if (field != null) {
			try {
				field.setAccessible(true);
				field.set(obj, fieldValue);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
