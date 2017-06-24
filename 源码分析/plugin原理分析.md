```
<!-- mybatis configuration配置文件中配置 -->
<plugins>
    <plugin interceptor="com.jd.jr.bt.mybatis.plugin.ExamplePlugin">
        <property name="someProperty" value="100" />
    </plugin>
</plugins>
```

配置完成后，Configuration会进行解析，并加入配置的拦截器。
```
// 初始化拦截链
protected final InterceptorChain interceptorChain = new InterceptorChain();

// 解析xml plugin节点
private void pluginElement(XNode parent) throws Exception {
  if (parent != null) {
    for (XNode child : parent.getChildren()) {
        String interceptor = child.getStringAttribute("interceptor");
        Properties properties = child.getChildrenAsProperties();
        Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
        interceptorInstance.setProperties(properties);
        configuration.addInterceptor(interceptorInstance);
    }
  }
}

// 把拦截器放入拦截链中
public void addInterceptor(Interceptor interceptor) {
    interceptorChain.addInterceptor(interceptor);
}
```

Configuration类中有几个方法通过`interceptorChain.pluginAll()`调用了自定义拦截器的plugin方法来生成代理对象，源码如下：

```
public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
    parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
    return parameterHandler;
}

public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler,ResultHandler resultHandler, BoundSql boundSql) {
    ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
    resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
    return resultSetHandler;
}

public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
    return statementHandler;
}

public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    ...
    executor = (Executor) interceptorChain.pluginAll(executor);
    return executor;
}
```

我们再来看看拦截链的源码，实际上mybatis的四大对象StatementHandler、ParameterHandler、ResultHandler和Executor都调用了pluginAll方法，并且都会进入你配置的拦截器的plugin方法：

```
public class InterceptorChain {

  private final List<Interceptor> interceptors = new ArrayList<Interceptor>();

  /**
   * 对配置的plugin调用plugin方法
   */
  public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
      target = interceptor.plugin(target);
    }
    return target;
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptors.add(interceptor);
  }
  
  public List<Interceptor> getInterceptors() {
    return Collections.unmodifiableList(interceptors);
  }

}
```

我们自己定义的拦截器的plugin方法实际上调用了Plugin的wrap方法，我们看看wrap的源码：

```
public static Object wrap(Object target, Interceptor interceptor) {
    // 获取自定义拦截器中定义的@Intercepts中配置的类和方法的map
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
    // mybatis四大对象访问的类型
    Class<?> type = target.getClass();
    // 签名map中是否包含这4大对象的接口
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
    if (interfaces.length > 0) {
      // 如果存在，返回target的动态代理类
      return Proxy.newProxyInstance(
          type.getClassLoader(),
          interfaces,
          new Plugin(target, interceptor, signatureMap));
    }
    // 返回原对象
    return target;
  }
```

如果plugin返回的是target的代理类，那么后面执行target的方法时，就会进入Plugin的invoke方法中（不懂的请去了解jdk动态代理的相关知识）

```
@Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      // 获取自定义拦截器中指定的方法，只有这些方法才会进行拦截
      Set<Method> methods = signatureMap.get(method.getDeclaringClass());
      if (methods != null && methods.contains(method)) {
        // 走入自定义拦截器的intercept方法
        return interceptor.intercept(new Invocation(target, method, args));
      }
      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
  }
```

既然走到自定义拦截器的intercept方法了，那么我们可以自己定义一些操作来拦截sql的执行，比如实现分页、分库分表的功能。具体的可以看我代码里的例子。