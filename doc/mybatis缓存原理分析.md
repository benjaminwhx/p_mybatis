# mybatis缓存原理分析

## 1.mybatis中的缓存原理（不结合spring）

Mybatis中有一级缓存和二级缓存，默认情况下一级缓存是开启的，而且是不能关闭的。一级缓存是指SqlSession级别的缓存，当在同一个SqlSession中进行相同的SQL语句查询时，第二次以后的查询不会从数据库查询，而是直接从缓存中获取，一级缓存最多缓存1024条SQL。二级缓存是指可以跨SqlSession的缓存。  

### 1.1.一级缓存

一级缓存是默认启用的，在BaseExecutor的query()方法中实现，底层默认使用的是PerpetualCache实现，PerpetualCache采用HashMap存储数据。一级缓存会在进行增、删、改操作时进行清除。  

```
// 维护者一个map的缓存
protected PerpetualCache localCache;
protected PerpetualCache localOutputParameterCache; 
  
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    // 上一个查询执行完成 并且 <select>标签增加了flushCache="true" 刷新一级缓存
    if (queryStack == 0 && ms.isFlushCacheRequired()) {
      clearLocalCache();
    }
    List<E> list;
    try {
      queryStack++;
      list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
      if (list != null) {
        handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
      } else {
        list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
      }
    } finally {
      queryStack--;
    }
    if (queryStack == 0) {
      for (DeferredLoad deferredLoad : deferredLoads) {
        deferredLoad.load();
      }
      // issue #601
      deferredLoads.clear();
      if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
        // issue #482
        clearLocalCache();
      }
    }
    return list;
  }
  
@Override
public void clearLocalCache() {
  if (!closed) {
    localCache.clear();
    localOutputParameterCache.clear();
  }
}

/**
 * 如果存在缓存参数值，取出来覆盖当前参数值，只针对Callable
 */
private void handleLocallyCachedOutputParameters(MappedStatement ms, CacheKey key, Object parameter, BoundSql boundSql) {
    if (ms.getStatementType() == StatementType.CALLABLE) {
      final Object cachedParameter = localOutputParameterCache.getObject(key);
      if (cachedParameter != null && parameter != null) {
        final MetaObject metaCachedParameter = configuration.newMetaObject(cachedParameter);
        final MetaObject metaParameter = configuration.newMetaObject(parameter);
        for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
          if (parameterMapping.getMode() != ParameterMode.IN) {
            final String parameterName = parameterMapping.getProperty();
            final Object cachedValue = metaCachedParameter.getValue(parameterName);
            metaParameter.setValue(parameterName, cachedValue);
          }
        }
      }
    }
  }
  
private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    List<E> list;
    // 放入一个初始值
    localCache.putObject(key, EXECUTION_PLACEHOLDER);
    try {
      list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
    } finally {
      localCache.removeObject(key);
    }
    // 放入一级缓存
    localCache.putObject(key, list);
    if (ms.getStatementType() == StatementType.CALLABLE) {
      localOutputParameterCache.putObject(key, parameter);
    }
    return list;
  }
```

可以看到上面的查询方法先是看<select>有没有flushCache="true"，有就刷新一级缓存，没有的话先去从一级缓存取数据，如果有结果，用缓存参数的值覆盖当前传入的值，最后返回结果。
如果一级缓存里没有结果，调用`queryFromDatabase`方法，从数据库查询结果并返回。

一级缓存的范围有SESSION和STATEMENT两种，默认是SESSION，如果我们不需要使用一级缓存，那么我们可以把一级缓存的范围指定为STATEMENT，这样每次执行完一个Mapper语句后都会将一级缓存清除。如果需要更改一级缓存的范围，请在Mybatis的配置文件中，在<settings>下通过localCacheScope指定。  
    
> 结论：一级缓存默认存在，不想使用有两种方法关闭。
> (1)、<select>指定flushCache="true"
> (2)、<setting name="localCacheScope" value="SESSION"/>



### 1.2.二级缓存

## 2.mybatis在spring中的缓存