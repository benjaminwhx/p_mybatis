package com.github.plugin.rw;

import com.github.plugin.PluginUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * 在执行sql前将sqlId放入到ThreadLocal中， 后面DataSourceSwitchFactoryBean会根据此sqlId来判断使用那个数据源
 *
 * User: 吴海旭
 * Date: 2017-06-30
 * Time: 下午1:12
 */
@Intercepts({
		@Signature(
				type = Executor.class,
				method = "query",
				args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
		@Signature(
				type = Executor.class,
				method = "query",
				args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
		@Signature(
				type = Executor.class,
				method = "update",
				args = {MappedStatement.class, Object.class})
})
public class DataSourceSwitchInterceptor implements Interceptor {

	private Logger logger = LoggerFactory.getLogger(DataSourceSwitchInterceptor.class);

	public Object intercept(Invocation invocation) throws Throwable {
		MappedStatement mappedStatement = (MappedStatement)invocation.getArgs()[0];
		String sqlId = mappedStatement.getId();
		PluginUtils.setSqlId(sqlId);
		logger.debug("DataSourceSwitchInterceptor intercept {}", sqlId);
		try {
			return invocation.proceed();
		} finally {
			PluginUtils.clearSqlId();
		}
	}

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties propertiesArg) {
	}
}
