package com.github.core.plugin;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * MyBatis 性能拦截器，用于输出每条 SQL 语句及其执行时间
 *
 * User: 吴海旭
 * Date: 2017-06-18
 * Time: 下午7:41
 */
@Intercepts({
		@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
		@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class PerformanceInterceptor implements Interceptor {

	private Logger logger = LoggerFactory.getLogger(PerformanceInterceptor.class);
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
		String statementId = mappedStatement.getId();
		Configuration configuration = mappedStatement.getConfiguration();
		Object parameterObject = invocation.getArgs()[1];
		BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
		String sql = getSql(boundSql, parameterObject, configuration);

		long start = System.currentTimeMillis();

		Object result = invocation.proceed();

		long end = System.currentTimeMillis();
		long timing = end - start;

		logger.info("耗时：" + timing + " ms" + " - id:" + statementId + " - Sql:" + sql);
		return result;
	}

	@Override
	public Object plugin(Object target) {
		if (target instanceof Executor) {
			return Plugin.wrap(target, this);
		}
		return target;
	}

	@Override
	public void setProperties(Properties properties) {

	}

	/**
	 * 获取待执行的sql语句
	 * @param boundSql
	 * @param parameterObject 参数对象，如果是多参数就是一个Map，对应着propertyName value
	 * @param configuration
	 * @return
	 */
	private String getSql(BoundSql boundSql, Object parameterObject, Configuration configuration) {
		String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
		if (parameterMappings != null) {
			for (int i = 0; i < parameterMappings.size(); i++) {
				ParameterMapping parameterMapping = parameterMappings.get(i);
				if (parameterMapping.getMode() != ParameterMode.OUT) {
					Object value;
					String propertyName = parameterMapping.getProperty();
					if (boundSql.hasAdditionalParameter(propertyName)) {
						value = boundSql.getAdditionalParameter(propertyName);
					} else if (parameterObject == null) {
						value = null;
					} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
						// 单个参数，并且类型是typeHandlerRegistry已经存在的
						value = parameterObject;
					} else {
						// 多参数
						MetaObject metaObject = configuration.newMetaObject(parameterObject);
						value = metaObject.getValue(propertyName);
					}
					// 把？替换成值
					sql = replacePlaceholder(sql, value);
				}
			}
		}
		return sql;
	}

	private String replacePlaceholder(String sql, Object propertyValue) {
		String result;
		if (propertyValue != null) {
			if (propertyValue instanceof String) {
				result = "'" + propertyValue + "'";
			} else if (propertyValue instanceof Date) {
				result = "'" + DATE_FORMAT.format(propertyValue) + "'";
			} else {
				result = propertyValue.toString();
			}
		} else {
			result = "null";
		}
		return sql.replaceFirst("\\?", result);
	}
}
