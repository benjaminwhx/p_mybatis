package com.github.core.builder;

import com.github.base.bean.MyPage;
import com.github.base.bean.SEX;
import com.github.base.bean.User;
import com.github.base.mapper.UserMapper;
import com.github.base.resulthandler.UserResultHandler;
import com.github.base.util.PrintUtil;
import com.github.core.util.MyBatisConfigHelper;
import org.apache.ibatis.session.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * User: 吴海旭
 * Date: 2017-06-24
 * Time: 下午1:23
 */
public class MyBatisCoreBuilder {

	private Logger logger = LoggerFactory.getLogger(MyBatisCoreBuilder.class);

	private void testUserMapperInsert() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession(true);
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		User user = new User();
		user.setUserName("张三");
		user.setSex(SEX.MALE);
		user.setRoleId(1L);
		int insert = mapper.insert(user);
		if (insert == 1) {
			logger.info("插入成功, id=" + user.getId());
		} else {
			logger.info("插入失败, insert=" + insert);
		}
	}

	private void testUserMapperInsertMulti() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession();
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		for (int i = 0; i < 30; ++i) {
			User user = new User();
			user.setUserName("张三" + i);
			if (i % 2 == 0) {
				user.setSex(SEX.MALE);
			} else {
				user.setSex(SEX.FEMALE);
			}
			user.setRoleId(1L);
			mapper.insert(user);
		}
		sqlSession.commit();
	}

	private void testUserMapperSelect1() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession();
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		User user = mapper.getUserByUserId(2L);
		logger.info(user.toString());
	}

	private void testUserMapperSelect2() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession();
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		List<User> userList = mapper.getUsersByName("Benjamin");
		PrintUtil.printList(userList, logger);
	}

	private void testUserMapperSelect3() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession();
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		List<User> userList = mapper.getAllUsers();
		PrintUtil.printList(userList, logger);
	}

	private void testUserMapperSelect4() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession();
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		List<User> userList = mapper.getUserByRowBounds(new RowBounds(0, 5));
		PrintUtil.printList(userList, logger);
	}

	private void testUserMapperSelect5() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession();
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		MyPage<User> myPage = new MyPage<>();
		myPage.setPageNo(2);
		myPage.setPageSize(5);
		List<User> userList = mapper.getUserByPage(myPage);
		PrintUtil.printList(userList, logger);
	}

	private void testUserMapperSelect6() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession();
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		mapper.getUserByUserIdAndResultHandler(new UserResultHandler());
	}

	private void testUserMapperUpdate() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession(true);
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		User user = new User();
		user.setId(6L);
		user.setUserName("Benjamin2");
		mapper.update(user);
	}

	private void testUserMapperDelete() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession(true);
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		mapper.delete(1L);
	}

	// batch方式花费：10869ms
	private void testUserMapperBatch() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession(ExecutorType.BATCH);
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		long begin = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 1000; j++) {
				User user = new User();
				user.setUserName("new名字" + j);
				user.setSex(SEX.MALE);
				user.setRoleId(1L);
				mapper.insert(user);
			}
			sqlSession.commit();
		}
		long end = System.currentTimeMillis();
		System.out.println("batch方式花费：" + (end - begin) + "ms");
	}

	// batch方式花费：10388ms
	private void testUserMapperBatch2() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession(ExecutorType.BATCH);
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		long begin = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 1000; j++) {
				User user = new User();
				user.setUserName("new名字" + j);
				user.setSex(SEX.MALE);
				user.setRoleId(1L);
				mapper.insert(user);
			}
			mapper.flush();
		}
		sqlSession.commit();
		long end = System.currentTimeMillis();
		System.out.println("batch方式花费：" + (end - begin) + "ms");
	}

	// mapper文件for循环方式花费：3110ms
	private void testInsertBatch() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession(true);
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		long begin = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			List<User> users = new ArrayList<>();
			for (int j = 0; j < 1000; j++) {
				User user = new User();
				user.setUserName("new2名字" + j);
				user.setSex(SEX.FEMALE);
				user.setRoleId(1L);
				users.add(user);
			}
			mapper.insertBatch(users);
		}
		long end = System.currentTimeMillis();
		System.out.println("mapper文件for循环方式花费：" + (end - begin) + "ms");
	}

	// mapper文件for循环方式花费：33705ms
	private void testInsert() {
		SqlSession sqlSession = MyBatisConfigHelper.getSqlSession(true);
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		long begin = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			User user = new User();
			user.setUserName("new2名字" + i);
			user.setSex(SEX.FEMALE);
			user.setRoleId(1L);
			mapper.insert(user);
		}
		long end = System.currentTimeMillis();
		System.out.println("mapper文件for循环方式花费：" + (end - begin) + "ms");
	}

	public static void main(String[] args) {
		MyBatisCoreBuilder builder = new MyBatisCoreBuilder();
//		builder.testUserMapperInsert();
//		builder.testUserMapperInsertMulti();
//		builder.testUserMapperSelect1();
//		builder.testUserMapperSelect2();
//		builder.testUserMapperSelect3();
//		builder.testUserMapperSelect4();
//		builder.testUserMapperSelect5();
//		builder.testUserMapperUpdate();
//		builder.testUserMapperDelete();
//		builder.testUserMapperSelect6();
//		builder.testUserMapperBatch();
		builder.testUserMapperBatch2();
//		builder.testInsertBatch();
//		builder.testInsert();
	}
}
