# mybatis

## 文档地址

1、mybatis-page中文文档地址：[https://pagehelper.github.io/](https://pagehelper.github.io/)  
2、mybatis-generator中文文档地址：[mybatis-generator](http://mbg.cndocs.tk/index.html)  
3、mybatis相关工具地址：[http://www.mybatis.tk/](http://www.mybatis.tk/)  
4、mybatis官方指南：[http://www.mybatis.org/mybatis-3/zh/index.html](http://www.mybatis.org/mybatis-3/zh/index.html)  
5、mybatis-spring官方指南：[http://www.mybatis.org/spring/zh/index.html](http://www.mybatis.org/spring/zh/index.html)  

## github地址

1、Mapper: [Mapper](https://github.com/abel533/Mapper)  
2、PageHelper：[PageHelper](https://github.com/pagehelper/Mybatis-PageHelper)  

https://my.oschina.net/lixin91/blog?catalog=3489293&temp=1497112350271

中文注释：https://github.com/tuguangquan/mybatis

mybatis设计模式：http://www.crazyant.net/2022.html

## mybatis study instruction（mybatis学习指南）

### 1、Mapper中的resultType的别名设置
```
<!-- 在config.xml中指定typeAlias的实体类包名，默认为类名的小写值，如Blog.class，取值blog，当然resultType中你可以制定大写也可以指定小写，最后都会被toLowerCase一下。-->
<typeAliases>
    <package name="com.jd.jr.bt.mybatis.bean"/>
</typeAliases>

@Alias("custom_blogName")
这个注解用在实体类上，注解会覆盖默认值
```

上面的源码解释如下，方法都在TypeAliasRegistry.class这个类中：
```
/**
 * 注册包下面所有复合superType类型的别名
 */
public void registerAliases(String packageName, Class<?> superType){
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<Class<?>>();
    // 对packageName这个包中的所有类匹配superType类型的类
    resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
    // 返回匹配的所有类
    Set<Class<? extends Class<?>>> typeSet = resolverUtil.getClasses();
    for(Class<?> type : typeSet){
      // Ignore inner classes and interfaces (including package-info.java)
      // Skip also inner classes. See issue #6
      if (!type.isAnonymousClass() && !type.isInterface() && !type.isMemberClass()) {
        // 不是匿名类、内部类、接口，注册这个别名
        registerAlias(type);
      }
    }
  }

/**
 * 对实体类进行别名设置，提取类名/@Alias
 */
public void registerAlias(Class<?> type) {
    // 得到类名
    String alias = type.getSimpleName();
    // 获取到类上的注解 @Alias
    Alias aliasAnnotation = type.getAnnotation(Alias.class);
    if (aliasAnnotation != null) {
      // 如果有注解，取注解的值
      alias = aliasAnnotation.value();
    }
    // 注册别名
    registerAlias(alias, type);
  }

/**
 * 注册别名，可以直接拿来使用
 */
public TypeAliasRegistry() {
    registerAlias("string", String.class);

    registerAlias("byte", Byte.class);
    registerAlias("long", Long.class);
    registerAlias("short", Short.class);
    registerAlias("int", Integer.class);
    registerAlias("integer", Integer.class);
    registerAlias("double", Double.class);
    registerAlias("float", Float.class);
    registerAlias("boolean", Boolean.class);

    registerAlias("byte[]", Byte[].class);
    registerAlias("long[]", Long[].class);
    registerAlias("short[]", Short[].class);
    registerAlias("int[]", Integer[].class);
    registerAlias("integer[]", Integer[].class);
    registerAlias("double[]", Double[].class);
    registerAlias("float[]", Float[].class);
    registerAlias("boolean[]", Boolean[].class);

    registerAlias("_byte", byte.class);
    registerAlias("_long", long.class);
    registerAlias("_short", short.class);
    registerAlias("_int", int.class);
    registerAlias("_integer", int.class);
    registerAlias("_double", double.class);
    registerAlias("_float", float.class);
    registerAlias("_boolean", boolean.class);

    registerAlias("_byte[]", byte[].class);
    registerAlias("_long[]", long[].class);
    registerAlias("_short[]", short[].class);
    registerAlias("_int[]", int[].class);
    registerAlias("_integer[]", int[].class);
    registerAlias("_double[]", double[].class);
    registerAlias("_float[]", float[].class);
    registerAlias("_boolean[]", boolean[].class);

    registerAlias("date", Date.class);
    registerAlias("decimal", BigDecimal.class);
    registerAlias("bigdecimal", BigDecimal.class);
    registerAlias("biginteger", BigInteger.class);
    registerAlias("object", Object.class);

    registerAlias("date[]", Date[].class);
    registerAlias("decimal[]", BigDecimal[].class);
    registerAlias("bigdecimal[]", BigDecimal[].class);
    registerAlias("biginteger[]", BigInteger[].class);
    registerAlias("object[]", Object[].class);

    registerAlias("map", Map.class);
    registerAlias("hashmap", HashMap.class);
    registerAlias("list", List.class);
    registerAlias("arraylist", ArrayList.class);
    registerAlias("collection", Collection.class);
    registerAlias("iterator", Iterator.class);

    registerAlias("ResultSet", ResultSet.class);
  }
```

### 2、insert插入后对象的id为null？
如果使用的mysql，并且对象的id为自增主键，则可以给mapper.xml中insert中增加两个属性  
```
useGeneratedKeys="true" keyProperty="id"
```
这样最后得到的对象中id就有值了。最重要的源码在PreparedStatementHandler中  
```
@Override
  public int update(Statement statement) throws SQLException {
    PreparedStatement ps = (PreparedStatement) statement;
    ps.execute();
    int rows = ps.getUpdateCount();
    Object parameterObject = boundSql.getParameterObject();
    // 如果指定了useGeneratedKeys="true"，keyGenerator则是Jdbc3KeyGenerator这个类，并且setId的操作在processAfter中执行
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);
    return rows;
  }

// Jdbc3KeyGenerator.class
private void populateKeys(ResultSet rs, MetaObject metaParam, String[] keyProperties, TypeHandler<?>[] typeHandlers) throws SQLException {
    for (int i = 0; i < keyProperties.length; i++) {
      String property = keyProperties[i];
      if (!metaParam.hasSetter(property)) {
        throw new ExecutorException("No setter found for the keyProperty '" + property + "' in " + metaParam.getOriginalObject().getClass().getName() + ".");
      }
      TypeHandler<?> th = typeHandlers[i];
      if (th != null) {
        Object value = th.getResult(rs, i + 1);
        // 设置id的值
        metaParam.setValue(property, value);
      }
    }
  }
```

### 3、如何使用自定义typeHandler？
typeHandler就是类型转换的意思，在Hibernate中要进行POJO和表字段的映射，@Column就是用来干这个的，当然在mybatis中，我们可以指定javaType和jdbcType以及typeHandler来进行类型转换。  
详见 `MySexTypeHandler.class` 这个类的使用。  

### 4、mybatis如何使用级联操作？
详见UserMapper.xml中的resultMap中的collection以及OrderMapper.xml中的resultMap中的association，例子执行 `MyBatisBuilder.class`  

### 5、动态SQL
详见mapper xml文件中定义的<if> <choose> <foreach> <sql> 等元素。  

### 6、mybatis的缓存
缓存的使用：`CacheBuilder.class`   
缓存的说明在`UserMapper.xml`中，自定义缓存 `MyCache.class`  

### 7、plugin interceptor
分页插件的使用：`PagePlugin.class`  
源码分析：[plugin原理分析](../源码分析/plugin原理分析.md)  
按理参考：`PageBuilder.class`

### 8、order by interceptor
排序插件的使用：`OrderByBuilder.class`
