package com.github.plugin.monitor;

import com.alibaba.fastjson.JSONObject;
import com.github.base.util.JSONUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: 吴海旭
 * Date: 2017-06-29
 * Time: 上午11:48
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
public class MonitorInterceptor implements Interceptor {

	private Logger logger = LoggerFactory.getLogger(MonitorInterceptor.class);

	/**
	 * SQL执行异常报警开关。
	 */
	private boolean sqlExceptionEnabled = true;

	/**
	 * 慢SQL报警开关。
	 */
	private boolean slowSqlEnabled = true;

	/**
	 * 最小的慢SQL超时时间，不能小于这个值，以免报警过于频繁。
	 */
	public static final long MIN_SLOW_SQL_TIMEOUT = 20;

	/**
	 * 慢SQL执行超时时间，单位是毫秒。
	 */
	private long slowSqlTimeout = 1000;

	/**
	 * 连接数过多报警开关。
	 */
	private boolean tooManyActiveConnectionEnabled = true;

	/**
	 * 最低允许的活跃连接占比。不能过低，以免导致报警过于频繁。
	 */
	public static final float MIN_MAX_ACTIVE_CONNECTION_RATIO = 0.3f;

	/**
	 * 连接数过多报警。活跃连接占比允许的最大值，超过该值将会报警。
	 */
	private float maxActiveConnectionRatio = 0.7f;

	// 报警
	private Monitor monitor = new DefaultMonitor();

	private static volatile AtomicLong tooManyActiveConnectionAlarmTimes = new AtomicLong(0);
	private static volatile Date tooManyActiveConnectionLatestAlarmTime = null;

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object[] args = invocation.getArgs();
		MappedStatement statement = (MappedStatement) args[0];
		Object parameterObject = null;
		String sqlId = null;
		BoundSql sql = null;
		try {
			parameterObject = args[1];
			sqlId = statement.getId();
			sql = statement.getBoundSql(parameterObject);

			//执行sql。
			long startMs = System.currentTimeMillis();
			Object result = invocation.proceed();
			long endMs = System.currentTimeMillis();
			long usedTimeInMs = endMs - startMs;

			try {

				//慢sql监控。
				if (slowSqlEnabled) {
					if (usedTimeInMs > this.slowSqlTimeout) {
						if (logger.isWarnEnabled()) {
							String params = toString(parameterObject);
							logger.warn("sqlId={}执行耗时{}毫秒，超过阀值{}，执行的sql语句是[{}], 参数值：[{}]", sqlId, usedTimeInMs, slowSqlTimeout, sql.getSql(), params);
						}

						// 模拟报警
						monitor.alarm(sqlId + "耗时" + usedTimeInMs + "毫秒，超过阀值" + this.slowSqlTimeout);
					}
				}

				//连接过多监控。
				if (tooManyActiveConnectionEnabled) {
					BasicDataSource basicDataSource = getBasicDataSource(statement);
					if(basicDataSource != null) {
						int connectionCount = basicDataSource.getNumActive();
						float ratio = (connectionCount * 1.0f) / basicDataSource.getMaxActive();
						if (ratio >= this.maxActiveConnectionRatio) {
							logger.warn("数据库连接数过多，使用率已经超过了{}%, 当前活跃连接数{},允许最大活跃连接数{}, 检测连接池使用率超比例次数 {}", (this.maxActiveConnectionRatio * 100), basicDataSource.getNumActive(), basicDataSource.getMaxActive(),
									tooManyActiveConnectionAlarmTimes.incrementAndGet());

							if (shouldAlarm(tooManyActiveConnectionLatestAlarmTime)) {
								tooManyActiveConnectionLatestAlarmTime = new Date();

								// 模拟报警
								monitor.alarm("数据库连接数过多，使用率已经超过了" + (this.maxActiveConnectionRatio * 100)
										+ "%, 当前活跃连接数" + basicDataSource.getNumActive()
										+ ",允许最大活跃连接数" + basicDataSource.getMaxActive()
										+ ", 检测到连接池使用超比例次数" + tooManyActiveConnectionAlarmTimes.get());
							}
						}
					}
				}
			} catch (Throwable t) {
				logger.error("数据库监控插件出现异常{}", t);
			}

			return result;
		} catch (Throwable e) {
			//sql执行异常报警。
			if (sqlExceptionEnabled) {
				Throwable targetException = e;
				if (e instanceof InvocationTargetException) {
					InvocationTargetException exception = (InvocationTargetException) e;
					targetException = exception.getTargetException();
				}
				if (targetException != null) {
					String paramVal = toString(parameterObject);
					BasicDataSource basicDataSource = getBasicDataSource(statement);
					int maxActive, active;
					if (basicDataSource == null) {
						maxActive = active = -1;
					} else {
						maxActive = basicDataSource.getMaxActive();
						active = basicDataSource.getNumActive();
					}
					logger.error("执行SQL异常，sqlId={}, sql={}, parameter={}, maxActive={}, current={}",
							sqlId,
							(sql == null ? "UNKNOWN SQL" : sql.getSql()),
							paramVal,
							maxActive,
							active);
					logger.error("SQL异常", targetException);

					// 模拟报警
					monitor.alarm(sqlId + " sqlException，错误详细信息请查看日志");
				}
			}
			throw e;
		}
	}

	private boolean shouldAlarm(Date latestAlarmTime) {
		if (latestAlarmTime == null) {
			return true;
		}
		Date now = new Date();
		return now.getTime() - latestAlarmTime.getTime() > 5 * 60 * 1000;
	}

	private String toString(Object parameterObject) {
		String params;
		if (parameterObject == null) {
			params = "null";
		} else {
			params = JSONUtil.bean2Json(parameterObject);
		}
		return params;
	}

	private BasicDataSource getBasicDataSource(MappedStatement statement) {
		DataSource dataSource = statement.getConfiguration().getEnvironment().getDataSource();
		if (dataSource instanceof BasicDataSource) {
			return (BasicDataSource) dataSource;
		}
		return null;
	}

	public void setSqlExceptionEnabled(boolean sqlExceptionEnabled) {
		this.sqlExceptionEnabled = sqlExceptionEnabled;
	}

	public void setSlowSqlEnabled(boolean slowSqlEnabled) {
		this.slowSqlEnabled = slowSqlEnabled;
	}

	public void setSlowSqlTimeout(long slowSqlTimeout) {
		if (slowSqlTimeout >= MIN_SLOW_SQL_TIMEOUT) {
			this.slowSqlTimeout = slowSqlTimeout;
		}
	}

	public void setTooManyActiveConnectionEnabled(boolean tooManyActiveConnectionEnabled) {
		this.tooManyActiveConnectionEnabled = tooManyActiveConnectionEnabled;
	}

	public void setMaxActiveConnectionRatio(float maxActiveConnectionRatio) {
		if (maxActiveConnectionRatio >= MIN_MAX_ACTIVE_CONNECTION_RATIO) {
			if (maxActiveConnectionRatio > 1.0) {
				throw new RuntimeException("maxActiveConnectionRatio must between 0.3 to 1.0 and greater than 0.3");
			} else {
				this.maxActiveConnectionRatio = maxActiveConnectionRatio;
			}
		}
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
		logger.debug("set properties for {} mybatis plugin", this.getClass().getName());
		//解析属性配置值，设置到对应的拦截器中。
		Set<String> keys = properties.stringPropertyNames();
		for (String key : keys) {
			String value = properties.getProperty(key);
			if (value != null && value.length() > 0) {
				try {
					BeanUtils.setProperty(this, key, value);
				} catch (Throwable e) {
					logger.error("属性值设置出错，请检查属性" + key + "的配置是否支持，或者属性的值类型不正确。");
					throw new RuntimeException("configure property " + key + " error", e);
				}
			}
		}

		logger.debug("set properties end", this.getClass().getName());
	}
}
