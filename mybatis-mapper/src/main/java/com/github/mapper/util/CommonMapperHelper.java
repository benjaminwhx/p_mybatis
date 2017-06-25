package com.github.mapper.util;

import com.github.mapper.mapper.InsertLogIdMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;
import tk.mybatis.mapper.common.special.InsertUseGeneratedKeysMapper;
import tk.mybatis.mapper.entity.Config;
import tk.mybatis.mapper.mapperhelper.MapperHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: 吴海旭
 * Date: 2017-06-25
 * Time: 下午1:43
 */
public class CommonMapperHelper {

    private static SqlSessionFactory sqlSessionFactory;

    static {
        String resource = "mybatis-config.xml";
        InputStream in = null;
        try {
            in = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);

        MapperHelper mapperHelper = new MapperHelper();
        Config config = new Config();
        config.setEnableMethodAnnotation(true);
        mapperHelper.setConfig(config);

        mapperHelper.registerMapper(Mapper.class);
//        mapperHelper.registerMapper(MySqlMapper.class);
        mapperHelper.registerMapper(IdsMapper.class);
        mapperHelper.registerMapper(InsertLogIdMapper.class);

        mapperHelper.processConfiguration(getSqlSession().getConfiguration());
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
