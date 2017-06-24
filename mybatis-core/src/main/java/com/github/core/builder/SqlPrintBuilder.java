package com.github.core.builder;

import com.github.base.util.SqlHelper;
import com.github.core.util.MyBatisConfigHelper;
import org.apache.ibatis.session.SqlSession;

/**
 * User: 吴海旭
 * Date: 2017-06-24
 * Time: 下午3:07
 */
public class SqlPrintBuilder {

	public static void main(String[] args) {
		SqlSession sqlSession = null;
		try {
			sqlSession = MyBatisConfigHelper.getSqlSession(true);

//			String mapperSql = SqlHelper.getMapperSql(sqlSession, UserMapper.class, "getUsersByName", "abc");
//			System.out.println(mapperSql);

//			String mapperSql2 = SqlHelper.getMapperSql(sqlSession, "com.github.base.mapper.UserMapper.getUsersByName", "abc");
//			System.out.println(mapperSql2);

//			String mapperSql3 = SqlHelper.getMapperSql(sqlSession.getMapper(UserMapper.class), "getUsersByName", "abc");
//			System.out.println(mapperSql3);

			String namespaceSql4 = SqlHelper.getNamespaceSql(sqlSession, "com.github.base.mapper.UserMapper.getUsersByName", "abc");
			System.out.println(namespaceSql4);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sqlSession != null) {
				sqlSession.close();
			}
		}
	}
}
