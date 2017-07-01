我们在使用mybatis的时候，SqlSession是一个重要的类，我们用它的getMapper()方法得到一个Mapper的代理对象，然后执行crud方法，那么它是如何得到这个mapper的呢？我们来看看源码。

首先进入SqlSession的默认实现类`DefaultSqlSession`

```
@Override
  public <T> T getMapper(Class<T> type) {
    return configuration.<T>getMapper(type, this);
  }
```

接下来是`Configuration`：

```
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
  }
```

接下来看`mapperRegistry`，顾名思义：mapper注册表：

```
// 已知的mapper，在初始化的时候解析<mappers>标签放入
private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<Class<?>, MapperProxyFactory<?>>();

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
```

可以看到我们取得时候，是去内存中已经存在的名叫`knownMappers`的map对象中去取`MapperProxyFactory`这么一个工厂类，并用它去实例化一个mapper的代理独享。我们先看看knownMappers中的值是在什么时候放入的。

回到Configuration中：

```
public void addMappers(String packageName, Class<?> superType) {
    mapperRegistry.addMappers(packageName, superType);
  }

  public void addMappers(String packageName) {
    mapperRegistry.addMappers(packageName);
  }

  public <T> void addMapper(Class<T> type) {
    mapperRegistry.addMapper(type);
  }
```

它调用了MapperRegistry的addMapper方法放入`MapperProxyFactory`：

```
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
```

好了回到刚才的getMapper中去，这里使用了mapperProxyFactory去实例化一个mapperProxy，我们看看它是怎么做的：

```
protected T newInstance(MapperProxy<T> mapperProxy) {
    // 返回一个动态代理的对象
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }

  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }
```

这样，我们可以知道sqlSession.getMapper()拿到的是一个代理对象MapperProxy，mapper接口里面所有方法的调用都会进入MapperProxy的invoke方法，我们来看看：

```
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      } else if (isDefaultMethod(method)) {
        return invokeDefaultMethod(proxy, method, args);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
    // 缓存mappermethod
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
  }
```

当我们调用mapper接口的方法时，mybatis先把我们的原方法进行了缓存，最后是去调用的MapperMethod的execute()方法。我们看看MapperMethod的构造方法和execute方法。

```
public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
    this.command = new SqlCommand(config, mapperInterface, method);
    this.method = new MethodSignature(config, mapperInterface, method);
  }

/**
 * SqlCommand缓存了statementId和操作类型
 */
public static class SqlCommand {

    // statementId: 格式为mapperClassName.methodName
    private final String name;
    // sql的操作类型
    private final SqlCommandType type;

    public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
      final String methodName = method.getName();
      final Class<?> declaringClass = method.getDeclaringClass();
      // 根据statementId从configuration上下文获取MappedStatement
      MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass,
          configuration);
      if (ms == null) {
        // 没有做映射的方法并且有@Flush注解
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
  }
  
public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
      // 获取返回类型
      Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
      if (resolvedReturnType instanceof Class<?>) {
        this.returnType = (Class<?>) resolvedReturnType;
      } else if (resolvedReturnType instanceof ParameterizedType) {
        this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
      } else {
        this.returnType = method.getReturnType();
      }
      // 返回void = true
      this.returnsVoid = void.class.equals(this.returnType);
      // 返回类型是集合或者数组 = true
      this.returnsMany = (configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray());
      this.returnsCursor = Cursor.class.equals(this.returnType);
      this.mapKey = getMapKey(method);
      this.returnsMap = (this.mapKey != null);
      // RowBounds类型的参数的index值
      this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
      // ResultHandler类型的参数的index值
      this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
      // 见下面的源码
      this.paramNameResolver = new ParamNameResolver(configuration, method);
}

public ParamNameResolver(Configuration config, Method method) {
    final Class<?>[] paramTypes = method.getParameterTypes();
    final Annotation[][] paramAnnotations = method.getParameterAnnotations();
    final SortedMap<Integer, String> map = new TreeMap<Integer, String>();
    int paramCount = paramAnnotations.length;
    // get names from @Param annotations
    // 遍历所有参数
    for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
      // 如果是特殊参数不做处理（RowBounds、ResultHandler）
      if (isSpecialParameter(paramTypes[paramIndex])) {
        // skip special parameters
        continue;
      }
      String name = null;
      for (Annotation annotation : paramAnnotations[paramIndex]) {
        if (annotation instanceof Param) {
          hasParamAnnotation = true;
          // 获取@Param的value值作为参数名
          name = ((Param) annotation).value();
          break;
        }
      }
      if (name == null) {
        // @Param was not specified.
        if (config.isUseActualParamName()) {
          // 获取真实的参数名jdk1.8之前不会走
          name = getActualParamName(method, paramIndex);
        }
        if (name == null) {
          // use the parameter index as the name ("0", "1", ...)
          // gcode issue #71
          name = String.valueOf(map.size());
        }
      }
      // 参数索引，参数名
      map.put(paramIndex, name);
    }
    names = Collections.unmodifiableSortedMap(map);
  }

public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
      case INSERT: {
    	Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.insert(command.getName(), param));
        break;
      }
      case UPDATE: {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.update(command.getName(), param));
        break;
      }
      case DELETE: {
        Object param = method.convertArgsToSqlCommandParam(args);
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
          Object param = method.convertArgsToSqlCommandParam(args);
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

我们可以看到execute方法实际上是调用的SqlSession的方法(insert、update、delete、select)，我们再看看convertArgsToSqlCommandParam这个方法是怎么转换参数的。

```
public Object convertArgsToSqlCommandParam(Object[] args) {
  return paramNameResolver.getNamedParams(args);
}

public Object getNamedParams(Object[] args) {
    // names是一个参数map类似这种：{{0, "M"}, {1, "N"}}
    final int paramCount = names.size();
    if (args == null || paramCount == 0) {
      return null;
    } else if (!hasParamAnnotation && paramCount == 1) {
      // 没有@Param注解而且只有一个参数，直接返回值（jdk1.8之前是下标，1.8返回的真实参数名）
      return args[names.firstKey()];
    } else {
      final Map<String, Object> param = new ParamMap<Object>();
      int i = 0;
      for (Map.Entry<Integer, String> entry : names.entrySet()) {
        // {参数名, 参数值} 参数名格式：1真实参数名(jdk1.8才是) 2参数下标
        param.put(entry.getValue(), args[entry.getKey()]);
        // add generic param names (param1, param2, ...)
        final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i + 1);
        // ensure not to overwrite parameter named with @Param
        if (!names.containsValue(genericParamName)) {\
          // {参数名，参数值} 参数名格式：param1 param2 param3...
          param.put(genericParamName, args[entry.getKey()]);
        }
        i++;
      }
      return param;
    }
  }
```

参数转换后拿到的是{参数名，参数值}的一个map。好了，差不多都讲完了。

对了，最后如果方法没有对应的statement，并且有@Flush注解，返回一个空集合`return Collections.emptyList();`，批处理返回已经执行完的结果。