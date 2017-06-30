package com.github.plugin;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

/**
 * User: 吴海旭
 * Date: 2017-06-30
 * Time: 下午1:07
 */
public class PluginUtils {
	static class SqlIdHolder {
		final String sqlId;
		SqlIdHolder(String sqlId) {
			this.sqlId = sqlId;
		}
	}

	static ThreadLocal<SqlIdHolder> sqlIdHolderThreadLocal = new ThreadLocal<>();

	public static void setSqlId(String sqlId) {
		sqlIdHolderThreadLocal.set(new SqlIdHolder(sqlId));
	}

	public static String getSqlId() {
		return sqlIdHolderThreadLocal.get().sqlId;
	}

	public static void clearSqlId() {
		sqlIdHolderThreadLocal.remove();
	}

	public static final String MAPPED_STATEMENT_KEY = "delegate.mappedStatement";

	public static MetaObject getRealStatementHandler(StatementHandler handler) {
		MetaObject metaStatementHandler = SystemMetaObject.forObject(handler);
		// 分离代理对象链(由于目标类可能被多个拦截器拦截，从而形成多次代理，通过下面的两次循环
		// 可以分离出最原始的的目标类)
		while (metaStatementHandler.hasGetter("h")) {
			Object object = metaStatementHandler.getValue("h");
			metaStatementHandler = SystemMetaObject.forObject(object);
		}
		// 分离最后一个代理对象的目标类
		while (metaStatementHandler.hasGetter("target")) {
			Object object = metaStatementHandler.getValue("target");
			metaStatementHandler = SystemMetaObject.forObject(object);
		}
		return metaStatementHandler;
	}
}
