package com.github.core.builder;

import com.github.base.bean.User;
import com.github.base.mapper.UserMapper;
import com.github.base.util.JSONUtil;
import com.github.core.util.MyBatisConfigHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * User: 吴海旭
 * Date: 2017-06-24
 * Time: 下午2:50
 */
public class CacheBuilder {

	private Logger logger = LoggerFactory.getLogger(CacheBuilder.class);

	/**
	 * 一级缓存：sqlSession级别的缓存
	 * 		同一个sqlSession执行相同的mapper的语句，传入参数相同，使用缓存的结果。
	 * 		不同的sqlSession会再次发出sql语句。
	 *
	 * 二级缓存：sqlSessionFactory级别的缓存，默认不开启
	 * 		开启二级缓存需要POJO必须是可序列化的（implements Serializable），而且在mapper文件中必须执行<cache />
	 * 		二级缓存只要是同一个sqlSessionFactory执行相同的mapper语句，传入的参数相同，都会使用缓存的结果
	 */
	private void testOneAndTwoLevelCache() {
		SqlSessionFactory sqlSessionFactory = MyBatisConfigHelper.getSqlSessionFactory();
		SqlSession sqlSession1 = sqlSessionFactory.openSession();
		UserMapper mapper = sqlSession1.getMapper(UserMapper.class);
		User user = mapper.getUserByUserId(1L);
		logger.info("使用同一个sqlSession再执行一次");
		User user2 = mapper.getUserByUserId(1L);
		sqlSession1.commit();

		logger.info("现在创建一个新的sqlSession再执行一次");
		SqlSession sqlSession2 = sqlSessionFactory.openSession();
		UserMapper mapper2 = sqlSession2.getMapper(UserMapper.class);
		User user3 = mapper2.getUserByUserId(1L);
		sqlSession2.commit();

		sqlSession1.close();
		sqlSession2.close();
	}

    /**
     * 不同的SqlSession会产生脏数据问题
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
	private void testMultiSqlSessionIsolation() throws InvocationTargetException, IllegalAccessException {
		SqlSessionFactory sqlSessionFactory = MyBatisConfigHelper.getSqlSessionFactory();
		SqlSession sqlSession1 = sqlSessionFactory.openSession();
		SqlSession sqlSession2 = sqlSessionFactory.openSession();
        UserMapper mapper1 = sqlSession1.getMapper(UserMapper.class);
        UserMapper mapper2 = sqlSession2.getMapper(UserMapper.class);

        User user = mapper1.getUserByUserId(2L);

        logger.info("user1: " + JSONUtil.bean2Json(user));
        User user2 = new User();
        BeanUtils.copyProperties(user2, user);
        user2.setUserName("Lucy_update");

        logger.info("user2: " + JSONUtil.bean2Json(user2));
        mapper2.update(user2);
        sqlSession2.commit();

        User user3 = mapper1.getUserByUserId(2L);
        logger.info("user3: " + JSONUtil.bean2Json(user3));
        sqlSession1.commit();
    }

	public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
//		new CacheBuilder().testOneAndTwoLevelCache();
		new CacheBuilder().testMultiSqlSessionIsolation();
	}
}
