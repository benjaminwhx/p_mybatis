package com.github.plugin.id;

import com.github.base.bean.Student;
import com.github.base.mapper.StudentMapper;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: 吴海旭
 * Date: 2017-06-28
 * Time: 下午6:09
 */
public class AutoIncrementKeyPluginTest {

	private Logger logger = LoggerFactory.getLogger(AutoIncrementKeyPluginTest.class);
	private SqlSession sqlSession = null;

	@Before
	public void setUp() {
		sqlSession = MyBatisConfigHelper.getSqlSession();
		deleteAll();
	}

	@After
	public void destroy() {
		if (sqlSession != null) {
			sqlSession.close();
		}
	}

	public void deleteAll() {
		StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);
		mapper.deleteAll();
	}

	@Test
	public void testInsert() {
		StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);
		Student student = new Student();
		student.setId(10L);
		student.setName("jack");
		mapper.insert(student);

		Student student2 = new Student();
		student2.setName("lucy");
		mapper.insert(student2);

		sqlSession.commit();
	}
}
