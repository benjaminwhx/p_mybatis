package com.github.plugin.rw;

import com.github.plugin.PluginUtils;
import org.springframework.beans.factory.FactoryBean;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * User: 吴海旭
 * Date: 2017-06-30
 * Time: 上午10:50
 */
public class DataSourceSwitchFactoryBean implements FactoryBean<DataSource> {
	private DataSource dataSourceMajor;
	private DataSource dataSourceMinor;
	private Set<String> minorSqlIds;

	public void setDataSourceMajor(DataSource dataSourceMajor) {
		this.dataSourceMajor = dataSourceMajor;
	}

	public void setDataSourceMinor(DataSource dataSourceMinor) {
		this.dataSourceMinor = dataSourceMinor;
	}

	public void setMinorSqlIds(Set<String> minorSqlIds) {
		this.minorSqlIds = minorSqlIds;
	}

	@Override
	public DataSource getObject() throws Exception {
		return (DataSource) Proxy.newProxyInstance(
				this.getClass().getClassLoader(),
				new Class[]{DataSource.class},
				new DataSourceInvocationHandler());
	}

	@Override
	public Class<?> getObjectType() {
		return DataSource.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	private class DataSourceInvocationHandler implements InvocationHandler {
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String sqlId = PluginUtils.getSqlId();
			boolean isSlaveSqlId = sqlId != null && minorSqlIds != null && minorSqlIds.contains(sqlId);
			DataSource dataSource = isSlaveSqlId ? dataSourceMinor : dataSourceMajor;
			return method.invoke(dataSource, args);
		}
	}
}
