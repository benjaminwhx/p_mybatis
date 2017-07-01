package com.github.plugin.rw.plan2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 编译器将把注释记录在类文件中，在运行时 VM 将保留注释，因此可以反射性地读取。
 * User: 吴海旭
 * Date: 2017-07-01
 * Time: 下午4:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataSource {

    DynamicDataSourceGlobal value() default DynamicDataSourceGlobal.READ;
}
