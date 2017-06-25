package com.github.core.builder;

import com.github.base.bean.MyPage;
import com.github.base.bean.SEX;
import com.github.base.bean.User;
import com.github.base.mapper.UserMapper;
import com.github.base.util.PrintUtil;
import com.github.core.util.MyBatisConfigHelper;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

	public static void main(String[] args) {
		MyBatisCoreBuilder builder = new MyBatisCoreBuilder();
//		builder.testUserMapperInsert();
//		builder.testUserMapperInsertMulti();
		builder.testUserMapperSelect1();
//		builder.testUserMapperSelect2();
//		builder.testUserMapperSelect3();
//		builder.testUserMapperSelect4();
//		builder.testUserMapperSelect5();
//		builder.testUserMapperUpdate();
//		builder.testUserMapperDelete();
	}
}
