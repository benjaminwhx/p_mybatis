package com.github.plugin.rw.plan3;

import java.sql.Connection;

/**
 * User: 吴海旭
 * Date: 2017-07-01
 * Time: 下午5:27
 */
public interface ConnectionProxy extends Connection {

    /**
     * 根据key路由到正确的connection
     * @param key
     * @return
     */
    Connection getTargetConnection(String key);
}
