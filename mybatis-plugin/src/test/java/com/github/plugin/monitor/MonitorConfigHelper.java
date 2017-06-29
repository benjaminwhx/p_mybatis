package com.github.plugin.monitor;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: 吴海旭
 * Date: 2017-06-28
 * Time: 下午6:20
 */
public class MonitorConfigHelper {

	private static SqlSessionFactory sqlSessionFactory;

	static {
		String resource = "mybatis-config/monitorConfig.xml";
		InputStream in = null;
		try {
			in = Resources.getResourceAsStream(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
	}

	public static SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	public static SqlSession getSqlSession() {
		return sqlSessionFactory.openSession();
	}

	public static SqlSession getSqlSession(boolean autoCommit) {
		return sqlSessionFactory.openSession(autoCommit);
	}
}
