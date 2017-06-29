# 一些实用的插件

## 1、AutoIncrementKeyPlugin插件使用

改变带有自增id的insert语句的执行行为， 由数据库执行后返回id的方式修改为在执行前设置id的值， 添加insert id列值的方式。

使用方法：  
在mybatis配置文件中添加插件配置， 如下实例：

```
<plugins>
    <plugin interceptor="com.github.plugin.id.AutoIncrementKeyPlugin">
        <!--属性idGen.type为外部自增长id生成服务类class-->
        <property name="idGen.type" value="com.jd.coo.sa.mybatis.plugins.test.AtomicLongIDGen"/>
        <!--要拦截insert操作的表配置， 以英文逗号或分号分隔， 若不指定拦截器将不工作。
        student=id:id表示表student有主键的属性为id，数据库列名也为id
        如果sql语句中的表名带有库名， 会去掉库名， 如： dms.send_d将使用send_d作为表名判断是否要拦截-->
        <property name="interceptTableSettings" value="student=id:id,teacher=id:id,test=id:id"/>
        <!-- 指定是否要替换sql语句中的xxx.NEXTVAL, 如果是oracle语法的sql，
        要对此类语句做替换， 默认会做替换-->
        <property name="replaceNextValue" value="true"/>
        <!--表示如果已经设置了keyProperty的值， 是否还要新生成id， true表示是， false为否, 默认为false-->
        <property name="replaceExistsNewId" value="true"/>
    </plugin>
</plugins>
```

## 2、监控sql异常， 慢sql， 连接数报警插件使用

通过mybatis插件拦截Executor接口的query和update方法， 在执行时做异常捕获， 慢sql统计， 以及连接数阀值监控。

配置如下：

```
<plugins>
    <plugin interceptor="com.github.plugin.monitor.MonitorInterceptor">
        <property name="sqlExceptionEnabled" value="true"/>
        <property name="slowSqlEnabled" value="true"/>
        <property name="slowSqlTimeout" value="21"/>
        <property name="tooManyActiveConnectionEnabled" value="true"/>
        <property name="maxActiveConnectionRatio" value="0.8"/>
    </plugin>
</plugins>
```
