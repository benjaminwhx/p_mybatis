package com.github.page;

import com.github.base.bean.User;
import com.github.base.mapper.UserMapper;
import com.github.base.util.PrintUtil;
import com.github.page.util.PageConfigHelper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * User: 吴海旭
 * Date: 2017-06-24
 * Time: 下午5:04
 */
public class PageInterceptorTest {

	private Logger logger = LoggerFactory.getLogger(PageInterceptorTest.class);

	/**
	 * 第一种：RowBounds方式调用
	 */
	private void selectUsePageInterceptor1() {
		SqlSession sqlSession = PageConfigHelper.getSqlSession(true);
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		// 默认不查询count值
		// 需要查询配置page-config.xml rowBoundsWithCount 为true
		PageRowBounds pageRowBounds = new PageRowBounds(0, 6);
		List<User> userListByRowBounds = mapper.getUserByRowBounds(pageRowBounds);
		logger.info("pageRowBounds count: " + pageRowBounds.getTotal());
		PrintUtil.printList(userListByRowBounds, logger);
	}

	/**
	 * 第二种：Mapper接口方式调用，推荐使用。
	 */
	private void selectUsePageInterceptor2() {
		SqlSession sqlSession = PageConfigHelper.getSqlSession(true);
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		// 默认开启查询总数
//		com.github.pagehelper.Page<Object> page = PageHelper.startPage(1, 6);
		// 不开启查询总数
//		com.github.pagehelper.Page<Object> page = PageHelper.startPage(1, 6, false);
		// offset开始查询
		com.github.pagehelper.Page<Object> page = PageHelper.offsetPage(0, 6, false);
		mapper.getAllUsers();

		logger.info("总数：" + page.getTotal());
		PrintUtil.printList(page.getResult(), logger);
	}

	/**
	 * 第三种：参数方式调用，不推荐使用，源码见 PageObjectUtil 这个类
	 * 参数有以下几种：
	 * 	pageNum 第几页
	 * 	pageSize 每页页数
	 * 	countSql 是否执行count sql
	 * 	reasonable 分页合理化，当你传入的页数不对时会自动合理计算，默认false
	 * 	pageSizeZero 当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
	 */
	private void selectUsePageInterceptor3() {
		SqlSession sqlSession = PageConfigHelper.getSqlSession(true);
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		// 不开启查询总数
		List<User> userByPage = mapper.getUserByPageParam(0, 6, false);
		PrintUtil.printList(userByPage, logger);
	}

	/**
	 * 第四种：使用ISelect接口方式
	 */
	private void selectUsePageInterceptor4() {
		SqlSession sqlSession = PageConfigHelper.getSqlSession(true);
		UserMapper mapper = sqlSession.getMapper(UserMapper.class);
		// 不开启查询总数
		com.github.pagehelper.Page<Object> page = PageHelper.startPage(0, 6).doSelectPage(() -> {
			mapper.getAllUsers();
		});
		logger.info("总数：" + page.getTotal());
		PrintUtil.printList(page.getResult(), logger);

		// 计算总数
		long count = PageHelper.count(() -> {
			mapper.getAllUsers();
		});
		System.out.println("总数2：" + count);
	}

	public static void main(String[] args) {
		PageInterceptorTest test = new PageInterceptorTest();
		test.selectUsePageInterceptor1();
//		test.selectUsePageInterceptor2();
//		test.selectUsePageInterceptor3();
//		test.selectUsePageInterceptor4();
	}
}
