在mybatis结合spring使用的时候，我们一般都会进行如下配置：

```java
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="mapperLocations" value="classpath:spring/mapping/*.xml"/>
    <property name="typeAliasesPackage" value="com.xx.domain"/>
    <property name="configuration">
      <bean class="org.apache.ibatis.session.Configuration">
        <property name="defaultStatementTimeout" value="3"/>
      </bean>
    </property>
    <property name="plugins">
      <array>
        <ref bean="xxPlugin" />
      </array>
    </property>
  </bean>
```



那么，有多少同学想过这个SqlSessionFactoryBean是什么呢？没错，它相当于一个工厂类的概念，spring把它当做一个Bean管理起来，用到了就给你返回它。因为SqlSessionFactory全局唯一即可，所以它可以被spring管理为单例。我们下面看看它的几个主要方法：



```java
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean, ApplicationListener<ApplicationEvent> {
	@Override
  public SqlSessionFactory getObject() throws Exception {
      if (this.sqlSessionFactory == null) {
        afterPropertiesSet();
      }

      return this.sqlSessionFactory;
  }
  
  @Override
  public Class<? extends SqlSessionFactory> getObjectType() {
      return this.sqlSessionFactory == null ? SqlSessionFactory.class : this.sqlSessionFactory.getClass();
  }
  
  @Override
  public void afterPropertiesSet() throws Exception {
      notNull(dataSource, "Property 'dataSource' is required");
      notNull(sqlSessionFactoryBuilder, "Property 'sqlSessionFactoryBuilder' is required");
      state((configuration == null && configLocation == null) || !(configuration != null && configLocation != null), "Property 'configuration' and 'configLocation' can not specified with together");

      this.sqlSessionFactory = buildSqlSessionFactory();
  }
}
```



上面的 `getObject()` 方法是spring要返回的bean的值，而 `getObjectType` 则是它所代表的类型，可以发现，它在 `afterPropertiesSet` 里调用了 `buildSqlSessionFactory` 来初始化 `sqlSessionFactory`，我们来看看这个方法都做了什么。



```java
protected SqlSessionFactory buildSqlSessionFactory() throws IOException {
        Configuration configuration;
        XMLConfigBuilder xmlConfigBuilder = null;
  	    /**
  	     * 可以自定义configuration来配置statementTimeout
  	     * <property name="defaultStatementTimeout" value="3" />
  	     */
        if (this.configuration != null) {
            configuration = this.configuration;
            if (configuration.getVariables() == null) {
                configuration.setVariables(this.configurationProperties);
            } else if (this.configurationProperties != null) {
                configuration.getVariables().putAll(this.configurationProperties);
            }
        } else if (this.configLocation != null) {
            xmlConfigBuilder = new XMLConfigBuilder(this.configLocation.getInputStream(), null, this.configurationProperties);
            configuration = xmlConfigBuilder.getConfiguration();
        } else {
            configuration = new Configuration();
            if (this.configurationProperties != null) {
                configuration.setVariables(this.configurationProperties);
            }
        }

        if (this.objectFactory != null) {
            configuration.setObjectFactory(this.objectFactory);
        }
        if (this.objectWrapperFactory != null) {
            configuration.setObjectWrapperFactory(this.objectWrapperFactory);
        }
        if (this.vfs != null) {
            configuration.setVfsImpl(this.vfs);
        }

  	    // 根据 ",; \t\n" 来拆分多个类型别名包
        if (hasLength(this.typeAliasesPackage)) {
            String[] typeAliasPackageArray = tokenizeToStringArray(this.typeAliasesPackage,
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (String packageToScan : typeAliasPackageArray) {
                // 注册类型别名
                configuration.getTypeAliasRegistry().registerAliases(packageToScan,
                        typeAliasesSuperType == null ? java.lang.Object.class : typeAliasesSuperType);
            }
        }

        if (!isEmpty(this.typeAliases)) {
            // 注册类型别名数组
            for (Class<?> typeAlias : this.typeAliases) {
                configuration.getTypeAliasRegistry().registerAlias(typeAlias);
            }
        }

        if (!isEmpty(this.plugins)) {
            // 注册插件
            for (Interceptor plugin : this.plugins) {
                configuration.addInterceptor(plugin);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Registered plugin: '" + plugin + "'");
                }
            }
        }

        if (hasLength(this.typeHandlersPackage)) {
            // 根据 ",; \t\n" 来拆分多个类型处理器包
            String[] typeHandlersPackageArray = tokenizeToStringArray(this.typeHandlersPackage,
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            // 注册类型处理器
            for (String packageToScan : typeHandlersPackageArray) {
                configuration.getTypeHandlerRegistry().register(packageToScan);
            }
        }

        if (!isEmpty(this.typeHandlers)) {
            for (TypeHandler<?> typeHandler : this.typeHandlers) {
                // 注册类型处理器数组
                configuration.getTypeHandlerRegistry().register(typeHandler);
            }
        }

        if (this.databaseIdProvider != null) {//fix #64 set databaseId before parse mapper xmls
            try {
configuration.setDatabaseId(this.databaseIdProvider.getDatabaseId(this.dataSource));
            } catch (SQLException e) {
                throw new NestedIOException("Failed getting a databaseId", e);
            }
        }

   		// 放入自定义缓存类
        if (this.cache != null) {
            configuration.addCache(this.cache);
        }

        if (xmlConfigBuilder != null) {
            try {
                // 如果配置了mybatis-config.xml进行解析
                xmlConfigBuilder.parse();
            } catch (Exception ex) {
                throw new NestedIOException("Failed to parse config resource: " + this.configLocation, ex);
            } finally {
                ErrorContext.instance().reset();
            }
        }
		
  		// 新建mybatis自带的transactionFactory -> SpringManagedTransactionFactory
        if (this.transactionFactory == null) {
            this.transactionFactory = new SpringManagedTransactionFactory();
        }

  		// 通过dataSource、transactionFactory来设置环境
        configuration.setEnvironment(new Environment(this.environment, this.transactionFactory, this.dataSource));

  		// 解析mapper.xml文件
        if (!isEmpty(this.mapperLocations)) {
            for (Resource mapperLocation : this.mapperLocations) {
                if (mapperLocation == null) {
                    continue;
                }

                try {
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperLocation.getInputStream(),
                            configuration, mapperLocation.toString(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                } catch (Exception e) {
                    throw new NestedIOException("Failed to parse mapping resource: '" + mapperLocation + "'", e);
                } finally {
                    ErrorContext.instance().reset();
                }
            }
        }

  		// 返回new DefaultSqlSessionFactory(configuration)
        return this.sqlSessionFactoryBuilder.build(configuration);
    }
```


这里是通过配置来初始化 `configuration` ，这里的 `transactionFactory` 如果用户不自定义默认为 `SpringManagedTransactionFactory` 。

还记得上一篇博客里最后提到的 `MapperFactoryBean` 吗？不记得看可以看看这篇博客：[mybatis在spring中注入接口原理分析上篇](./mybatis在spring中注入接口原理分析上篇.md)

spring在注入mybatis接口的时候其实拿到的是 `MapperFactoryBean` 的 `getObject` 返回的值：`getSqlSession().getMapper(this.mapperInterface);` 这里获取的 `SqlSession` 其实是 `SqlSessionTemplate`  那它是如何获取Mapper的呢？



```java
public <T> T getMapper(Class<T> type) {
    return getConfiguration().getMapper(type, this);
}
```

接着来看Configuration中的getMapper方法：

```java
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
}
```

那这个mapperRegistry是个什么鬼？是一个mapper注册中心吗？bingo，答对啦。咦，它是什么时候注册mapper的呢？还记不记得 `MapperFactoryBean` 的 `checkDaoConfig` 方法里的一句: `configuration.addMapper(this.mapperInterface);` ？没错，就是这，我们来看看这个 `MapperRegistry` 的真面目吧。



```java
public class MapperRegistry {
    private final Configuration config;
    // 已知的mapper，在初始化的时候解析<mappers>标签放入
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<Class<?>, MapperProxyFactory<?>>();

    public MapperRegistry(Configuration config) {
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            // 用缓存的mapperProxyFactory构造一个mapperProxy
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    public <T> void addMapper(Class<T> type) {
        if (type.isInterface()) {
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            boolean loadCompleted = false;
            try {
                // 放进去mapper接口对应的MapperProxyFactory
                knownMappers.put(type, new MapperProxyFactory<T>(type));
                // It's important that the type is added before the parser is run
                // otherwise the binding may automatically be attempted by the
                // mapper parser. If the type is already known, it won't try.
                MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
                parser.parse();
                loadCompleted = true;
            } finally {
                if (!loadCompleted) {
                    knownMappers.remove(type);
                }
            }
        }
    }
}
```



它的内部结构很简单，一个缓存map：key为mapper接口类的Class类型，value为 `MapperProxyFactory` ，`getMapper` 里调用它的 `newInstance` 方法生成mapper接口的proxy实例，我们来看看这个方法。



```java
protected T newInstance(MapperProxy<T> mapperProxy) {
    // 返回一个动态代理的对象
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
}

public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
}
```

这样，我们可以知道sqlSession.getMapper()拿到的是一个代理对象MapperProxy，mapper接口里面所有方法的调用都会进入MapperProxy的invoke方法。

```java
public java.lang.Object invoke(java.lang.Object proxy, Method method, java.lang.Object[] args) throws Throwable {
    try {
        if (java.lang.Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        } else if (isDefaultMethod(method)) {
            return invokeDefaultMethod(proxy, method, args);
        }
    } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
    }
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
}

private MapperMethod cachedMapperMethod(Method method) {
    MapperMethod mapperMethod = methodCache.get(method);
    if (mapperMethod == null) {
        // 缓存方法的接口类、方法、配置信息
        mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
        methodCache.put(method, mapperMethod);
    }
    return mapperMethod;
}
```

invoke方法先缓存method，让同一个方法只初始化一次。接着再调用 `MapperMethod` 的 `execute` 方法：

```java
public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
    // 初始化SqlCommand
    this.command = new SqlCommand(config, mapperInterface, method);
    // 初始化MethodSignature
    this.method = new MethodSignature(config, mapperInterface, method);
}

public java.lang.Object execute(SqlSession sqlSession, java.lang.Object[] args) {
    java.lang.Object result;
    // 根据SqlCommand的类型走不同逻辑
    switch (command.getType()) {
        case INSERT: {
            java.lang.Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.insert(command.getName(), param));
            break;
        }
        case UPDATE: {
            java.lang.Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.update(command.getName(), param));
            break;
        }
        case DELETE: {
            java.lang.Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.delete(command.getName(), param));
            break;
        }
        case SELECT:
            if (method.returnsVoid() && method.hasResultHandler()) {
                executeWithResultHandler(sqlSession, args);
                result = null;
            } else if (method.returnsMany()) {
                result = executeForMany(sqlSession, args);
            } else if (method.returnsMap()) {
                result = executeForMap(sqlSession, args);
            } else if (method.returnsCursor()) {
                result = executeForCursor(sqlSession, args);
            } else {
                java.lang.Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.selectOne(command.getName(), param);
            }
            break;
        case FLUSH:
            result = sqlSession.flushStatements();
            break;
        default:
            throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
        throw new BindingException("Mapper method '" + command.getName()
                + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
}
```

这个方法当中用到了两个重要的类：`SqlCommand` 和 `MethodSignature` 

```java
/**
 * 存放sql的id和crud类型
 */
public static class SqlCommand {
    private final String name;
    private final SqlCommandType type;

    public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
        final String methodName = method.getName();
        final Class<?> declaringClass = method.getDeclaringClass();
        // 解析获取MappedStatement
        MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass,
                configuration);
        // 如果没有解析到Statement，则查找@Flush注解，没有注解则报错。
        if (ms == null) {
            if (method.getAnnotation(Flush.class) != null) {
                name = null;
                type = SqlCommandType.FLUSH;
            } else {
                throw new BindingException("Invalid bound statement (not found): "
                        + mapperInterface.getName() + "." + methodName);
            }
        } else {
            name = ms.getId();
            type = ms.getSqlCommandType();
            if (type == SqlCommandType.UNKNOWN) {
                throw new BindingException("Unknown execution method for: " + name);
            }
        }
    }

    public String getName() {
        return name;
    }

    public SqlCommandType getType() {
        return type;
    }

    private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
                                                   Class<?> declaringClass, Configuration configuration) {
        String statementId = mapperInterface.getName() + "." + methodName;
        if (configuration.hasStatement(statementId)) {
            return configuration.getMappedStatement(statementId);
        } else if (mapperInterface.equals(declaringClass)) {
            return null;
        }
        for (Class<?> superInterface : mapperInterface.getInterfaces()) {
            if (declaringClass.isAssignableFrom(superInterface)) {
                MappedStatement ms = resolveMappedStatement(superInterface, methodName,
                        declaringClass, configuration);
                if (ms != null) {
                    return ms;
                }
            }
        }
        return null;
    }
}

public static class MethodSignature {
    // 返回值是否是数组或集合
    private final boolean returnsMany;
    // 返回值是否是map
    private final boolean returnsMap;
    // 返回值是否返回void
    private final boolean returnsVoid;
    // 返回值是否是cursor类型
    private final boolean returnsCursor;
    // 返回值类型
    private final Class<?> returnType;
    // 返回值为map的key
    private final String mapKey;
    // 参数里ResultHandler在第几个位置
    private final Integer resultHandlerIndex;
    // 参数里RowBounds在第几个位置
    private final Integer rowBoundsIndex;
    private final ParamNameResolver paramNameResolver;

    public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
        Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
        if (resolvedReturnType instanceof Class<?>) {
            this.returnType = (Class<?>) resolvedReturnType;
        } else if (resolvedReturnType instanceof ParameterizedType) {
            this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
        } else {
            this.returnType = method.getReturnType();
        }
        this.returnsVoid = void.class.equals(this.returnType);
        this.returnsMany = (configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray());
        this.returnsCursor = Cursor.class.equals(this.returnType);
        this.mapKey = getMapKey(method);
        this.returnsMap = (this.mapKey != null);
        this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
        this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
        this.paramNameResolver = new ParamNameResolver(configuration, method);
    }

    public java.lang.Object convertArgsToSqlCommandParam(java.lang.Object[] args) {
        return paramNameResolver.getNamedParams(args);
    }

    private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
        Integer index = null;
        final Class<?>[] argTypes = method.getParameterTypes();
        for (int i = 0; i < argTypes.length; i++) {
            if (paramType.isAssignableFrom(argTypes[i])) {
                if (index == null) {
                    index = i;
                } else {
                    throw new BindingException(method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
                }
            }
        }
        return index;
    }

    /**
     * 从方法上获取@MapKey，如果存在，返回定义的value()值
     */
    private String getMapKey(Method method) {
        String mapKey = null;
        if (Map.class.isAssignableFrom(method.getReturnType())) {
            final MapKey mapKeyAnnotation = method.getAnnotation(MapKey.class);
            if (mapKeyAnnotation != null) {
                mapKey = mapKeyAnnotation.value();
            }
        }
        return mapKey;
    }
}
```

从execute方法可以看出是真正调用了sqlSession的crud方法。

回顾一下之前的内容：

1. 构建 `SqlSessionFactory` 注入到 `MapperFactoryBean` 中，构造出一个代理类 `SqlSessionTemplate` 代替`SqlSession`
2. 当spring要注入到mapper接口类时，获取 `MapperProxy` 的代理方法，执行方法的时候会经过它做一次缓存操作



那么下面我们就来说说这个 `SqlSessionTemplate` 是如何代替 `SqlSession` 的，先来看看它的构造方法

```java
public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
    this(sqlSessionFactory, sqlSessionFactory.getConfiguration().getDefaultExecutorType());
}

public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType) {
    this(sqlSessionFactory, executorType,
            new MyBatisExceptionTranslator(
                    sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true));
}

public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
                          PersistenceExceptionTranslator exceptionTranslator) {

    notNull(sqlSessionFactory, "Property 'sqlSessionFactory' is required");
    notNull(executorType, "Property 'executorType' is required");

    this.sqlSessionFactory = sqlSessionFactory;
    this.executorType = executorType;
    this.exceptionTranslator = exceptionTranslator;
    // 代理出一个sqlSession
    this.sqlSessionProxy = (SqlSession) newProxyInstance(
            SqlSessionFactory.class.getClassLoader(),
            new Class[] { SqlSession.class },
            new SqlSessionInterceptor());
}
```

很明显可以看出，它使用了默认的 `ExecutorType` 并且代理出一个 `SqlSession` ，所有的SqlSession的方法都交由 `SqlSessionProxy` 来执行，并且走 `SqlSessionInterceptor` 进行拦截。下面看看这个拦截器都做了哪些事。

```java
 /**
 * SqlSession执行方法会经过此拦截器进行代理执行。
 */
private class SqlSessionInterceptor implements InvocationHandler {
    @Override
    public java.lang.Object invoke(java.lang.Object proxy, Method method, java.lang.Object[] args) throws Throwable {
        // 从spring中获取session 或 使用sqlSessionFactory.openSession获取session
        SqlSession sqlSession = getSqlSession(
                SqlSessionTemplate.this.sqlSessionFactory,
                SqlSessionTemplate.this.executorType,
                SqlSessionTemplate.this.exceptionTranslator);
        try {
            // 执行sqlSession的原始方法
            java.lang.Object result = method.invoke(sqlSession, args);
            if (!isSqlSessionTransactional(sqlSession, SqlSessionTemplate.this.sqlSessionFactory)) {
                // force commit even on non-dirty sessions because some databases require
                // a commit/rollback before calling close()
                sqlSession.commit(true);
            }
            return result;
        } catch (Throwable t) {
            Throwable unwrapped = unwrapThrowable(t);
            if (SqlSessionTemplate.this.exceptionTranslator != null && unwrapped instanceof PersistenceException) {
                // 释放连接来避免死锁 See issue #22
                closeSqlSession(sqlSession, SqlSessionTemplate.this.sqlSessionFactory);
                sqlSession = null;
                Throwable translated = SqlSessionTemplate.this.exceptionTranslator.translateExceptionIfPossible((PersistenceException) unwrapped);
                if (translated != null) {
                    unwrapped = translated;
                }
            }
            throw unwrapped;
        } finally {
            // 释放连接
            if (sqlSession != null) {
                closeSqlSession(sqlSession, SqlSessionTemplate.this.sqlSessionFactory);
            }
        }
    }
}
```

可以很清楚的看到代理拦截器做了哪些事，分为有事务场景的和没有事务场景的。

1. 存在spring事务

   1.1. 从spring的threadLocal中获取sqlSession，没有则新增

   1.2. 执行sqlSession的方法

   1.3. 释放连接

2. 不存在spring事务

   2.1. 调用sessionFactory.openSession()方法创建一个sqlSession

   2.2. 执行sqlSession的方法

   2.3. 执行sqlSession.commit(true)

   2.4. 释放连接



下面我们可以看看 `getSqlSession` 具体是如何实现和spring关联的。

```java
public static SqlSession getSqlSession(SqlSessionFactory sessionFactory, ExecutorType executorType, PersistenceExceptionTranslator exceptionTranslator) {
  		// 1、从spring的threadLocal中获取管理sessionFactory的map，如果有对应的sessionFactory，返回SqlSessionHolder
        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

  		// 2、进行事务判断，取出session返回
        SqlSession session = sessionHolder(executorType, holder);
        if (session != null) {
            return session;
        }

  		// 3、如果无session，新创建一个session
        session = sessionFactory.openSession(executorType);

  		// 4、注册sessionHolder到spring的事务中
  		// 注意：这里要是不在spring事务管理中或者没有使用SpringManagedTransactionFactory将不会把sessionHolder注册到spring中
        registerSessionHolder(sessionFactory, executorType, exceptionTranslator, session);

        return session;
    }

private static SqlSession sessionHolder(ExecutorType executorType, SqlSessionHolder holder) {
        SqlSession session = null;
  		// 如果该holder是事务管理，返回holder中的session，否则返回null
        if (holder != null && holder.isSynchronizedWithTransaction()) {
            if (holder.getExecutorType() != executorType) {
                throw new TransientDataAccessResourceException("Cannot change the ExecutorType when there is an existing transaction");
            }
            holder.requested();
            session = holder.getSqlSession();
        }
        return session;
    }

private static void registerSessionHolder(SqlSessionFactory sessionFactory, ExecutorType executorType, PersistenceExceptionTranslator exceptionTranslator, SqlSession session) {
        SqlSessionHolder holder;
  		// 1、当前是处于spring事务管理
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            Environment environment = sessionFactory.getConfiguration().getEnvironment();

          	// 2、TransactionFactory一定要是SpringManagedTransactionFactory
            if (environment.getTransactionFactory() instanceof SpringManagedTransactionFactory) {
                // 3、新建SqlSessionHolder，绑定到spring的上下文中，并注册一个事务状态同步器，当发生状态变化时，调用SqlSessionSynchronization
                holder = new SqlSessionHolder(session, executorType, exceptionTranslator);
                TransactionSynchronizationManager.bindResource(sessionFactory, holder);
                TransactionSynchronizationManager.registerSynchronization(new SqlSessionSynchronization(holder, sessionFactory));
                holder.setSynchronizedWithTransaction(true);
                holder.requested();
            } else {
                if (TransactionSynchronizationManager.getResource(
                  environment.getDataSource()) ！= null) {
                    throw new TransientDataAccessResourceException(
                            "SqlSessionFactory must be using a SpringManagedTransactionFactory in order to use Spring transaction synchronization");
                }
            }
        }
    }
```

下面是事务回调类，在事务的各个阶段进行通知来清理资源。

```java
/**
 * 回调来清理资源，包括TransactionSynchronizationManager和已经提交和关闭的SqlSession
 * 它认为Connection的生命周期会被DataSourceTransactionManager或者JtaTransactionManager
 * 来管理
 */
private static final class SqlSessionSynchronization extends TransactionSynchronizationAdapter {

    private final SqlSessionHolder holder;

    private final SqlSessionFactory sessionFactory;

    private boolean holderActive = true;

    public SqlSessionSynchronization(SqlSessionHolder holder, SqlSessionFactory sessionFactory) {
        notNull(holder, "Parameter 'holder' must be not null");
        notNull(sessionFactory, "Parameter 'sessionFactory' must be not null");

        this.holder = holder;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public int getOrder() {
        // order right before any Connection synchronization
        return DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 1;
    }

    @Override
    public void suspend() {
        if (this.holderActive) {
            TransactionSynchronizationManager.unbindResource(this.sessionFactory);
        }
    }

    @Override
    public void resume() {
        if (this.holderActive) {
            TransactionSynchronizationManager.bindResource(this.sessionFactory, this.holder);
        }
    }

    @Override
    public void beforeCommit(boolean readOnly) {
        // Connection的commit或者rollback将会交由ConnectionSynchronization或者
        // DataSourceTransactionManager来管理
        // 但是SqlSession / Executor的清理还需要执行(包括刷新BATCH表达式)
        // SpringManagedTransaction will no-op the commit over the jdbc connection
        // TODO This updates 2nd level caches but the tx may be rolledback later on! 
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            try {
                this.holder.getSqlSession().commit();
            } catch (PersistenceException p) {
                if (this.holder.getPersistenceExceptionTranslator() != null) {
                    DataAccessException translated = this.holder
                            .getPersistenceExceptionTranslator()
                            .translateExceptionIfPossible(p);
                    if (translated != null) {
                        throw translated;
                    }
                }
                throw p;
            }
        }
    }

    @Override
    public void beforeCompletion() {
        // Issue #18 Close SqlSession and deregister it now
        // because afterCompletion may be called from a different thread
        if (!this.holder.isOpen()) {
            TransactionSynchronizationManager.unbindResource(sessionFactory);
            this.holderActive = false;
            this.holder.getSqlSession().close();
        }
    }

    @Override
    public void afterCompletion(int status) {
        if (this.holderActive) {
            // afterCompletion可能会有其他线程调用
            // so没有任何值存在的话忽略错误
            TransactionSynchronizationManager.unbindResourceIfPossible(sessionFactory);
            this.holderActive = false;
            this.holder.getSqlSession().close();
        }
        this.holder.reset();
    }
}
```

