package com.github.plugin.rw.plan2;

import com.github.base.util.ReflectUtil;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;

import java.sql.Connection;
import java.util.Properties;

/**
 * User: 吴海旭
 * Date: 2017-07-01
 * Time: 下午5:38
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class})})
public class DynamicPlugin implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //如果是采用了我们代理,则路由    数据源
        Connection conn = (Connection)invocation.getArgs()[0];
        if(conn instanceof ConnectionProxy){
            StatementHandler statementHandler = (StatementHandler) invocation
                    .getTarget();

            MappedStatement mappedStatement = null;
            if (statementHandler instanceof RoutingStatementHandler) {
                StatementHandler delegate = (StatementHandler) ReflectUtil
                        .getFieldValue(statementHandler, "delegate");
                mappedStatement = (MappedStatement) ReflectUtil.getFieldValue(
                        delegate, "mappedStatement");
            } else {
                mappedStatement = (MappedStatement) ReflectUtil.getFieldValue(
                        statementHandler, "mappedStatement");
            }
            String key = AbstractDynamicDataSourceProxy.WRITE;

            if(mappedStatement.getSqlCommandType() == SqlCommandType.SELECT){
                key = AbstractDynamicDataSourceProxy.READ;
            }else{
                key = AbstractDynamicDataSourceProxy.WRITE;
            }

            ConnectionProxy connectionProxy = (ConnectionProxy)conn;
            connectionProxy.getTargetConnection(key);

        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
