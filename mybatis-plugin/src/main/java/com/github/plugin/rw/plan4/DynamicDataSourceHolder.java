package com.github.plugin.rw.plan4;

import com.github.plugin.rw.plan4.DynamicDataSourceGlobal;

/**
 * User: 吴海旭
 * Date: 2017-07-01
 * Time: 下午4:03
 */
public class DynamicDataSourceHolder {

    private static ThreadLocal<DynamicDataSourceGlobal> holder = new ThreadLocal<>();

    public static void putDataSource(DynamicDataSourceGlobal dataSourceGlobal) {
        holder.set(dataSourceGlobal);
    }

    public static DynamicDataSourceGlobal getDataSource() {
        return holder.get();
    }

    public static void clear() {
        holder.remove();
    }
}
