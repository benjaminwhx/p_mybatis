# 使用通用mapper的注意点：

### 一、配置了BATCH提交导致增删改无效。

1、如果我配置了<setting name="defaultExecutorType" value="BATCH"/>默认batch提交的话，sqlSession自动提交会导致插入、更新、删除通用mapper失效。，如果关闭自动提交，得手动控制sqlSession进行commit，但是如果我针对一条id进行删除，类似于int i = mapper.deleteByPrimaryKey(7L);数据库这条数据执行成功了，但是返回值不是1，是-2147482646，这是不是有问题？
2、不配置<setting name="defaultExecutorType" value="BATCH"/>一切正常。

### 二、如果主键不是id，自定义插入Mapper

参考`InsertLogIdMapper.class`