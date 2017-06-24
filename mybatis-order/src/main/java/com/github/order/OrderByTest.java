package com.github.order;

import com.github.base.bean.User;
import com.github.base.mapper.UserMapper;
import com.github.base.util.PrintUtil;
import com.github.order.util.OrderConfigHelper;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.mybatis.orderbyhelper.OrderByHelper;

import java.util.List;

/**
 * User: 吴海旭
 * Date: 2017-06-24
 * Time: 下午5:22
 */
public class OrderByTest {

	private Logger logger = LoggerFactory.getLogger(OrderByTest.class);

	private void selectUserOrderByInterceptor() {
		SqlSession sqlSession = OrderConfigHelper.getSqlSession(true);
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);

		// 按id desc排序输出
		OrderByHelper.orderBy("id desc");
		List<User> allUsers = mapper.getAllUsers();
		PrintUtil.printList(allUsers, logger);
	}

	public static void main(String[] args) {
		OrderByTest test = new OrderByTest();
		test.selectUserOrderByInterceptor();
	}
}
