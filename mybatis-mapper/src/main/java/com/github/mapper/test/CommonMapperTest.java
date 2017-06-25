package com.github.mapper.test;

import com.github.base.util.PrintUtil;
import com.github.mapper.mapper.UserLoginMapper;
import com.github.mapper.model.UserLogin;
import com.github.mapper.util.CommonMapperHelper;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/**
 * User: 吴海旭
 * Date: 2017-06-25
 * Time: 下午1:43
 */
public class CommonMapperTest {

    private static Logger logger = LoggerFactory.getLogger(CommonMapperTest.class);

    private void testMapper1() {
        SqlSession sqlSession = CommonMapperHelper.getSqlSession();
        UserLoginMapper mapper = sqlSession.getMapper(UserLoginMapper.class);
        UserLogin userLogin = new UserLogin();
        userLogin.setUsername("test2");
        // 设置动态表名
        userLogin.setDynamicTableName("user_login2");
        List<UserLogin> select = mapper.select(userLogin);
        PrintUtil.printList(select, logger);
    }

    private void testMapper2() {
        SqlSession sqlSession = CommonMapperHelper.getSqlSession();
        UserLoginMapper mapper = sqlSession.getMapper(UserLoginMapper.class);
        Example example = new Example(UserLogin.class);
        example.createCriteria().andEqualTo("username", "test1");
        example.setTableName("user_login2");
        List<UserLogin> userLogins = mapper.selectByExample(example);
        PrintUtil.printList(userLogins, logger);
    }

    private void testMapper3() {
        SqlSession sqlSession = CommonMapperHelper.getSqlSession(true);
        UserLoginMapper mapper = sqlSession.getMapper(UserLoginMapper.class);
        UserLogin userLogin = new UserLogin();
        userLogin.setUsername("benjamin");
        userLogin.setLogindate(new Date());
        userLogin.setLoginip("192.168.1.1");
        int i = mapper.insertUseGeneratedKeys(userLogin);
        logger.info("i=" + i + ", id=" + userLogin.getLogid());
    }

    private void testMapper4() {
        SqlSession sqlSession = CommonMapperHelper.getSqlSession(true);
        UserLoginMapper mapper = sqlSession.getMapper(UserLoginMapper.class);
        List<UserLogin> userLogins = mapper.selectByIds("2,3,4,5");
        PrintUtil.printList(userLogins, logger);
    }

    private void testMapper5() {
        SqlSession sqlSession = CommonMapperHelper.getSqlSession(true);
        UserLoginMapper mapper = sqlSession.getMapper(UserLoginMapper.class);
        int i = mapper.deleteByPrimaryKey(1L);
        logger.info("i=" + i);
    }

    public static void main(String[] args) {
        CommonMapperTest test = new CommonMapperTest();
//        test.testMapper1();
//        test.testMapper2();
        test.testMapper3();
//        test.testMapper4();
//        test.testMapper5();
    }
}
