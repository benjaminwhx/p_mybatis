我们在spring中使用mybatis时，仅仅需要一个简单的配置，它就能帮我们把接口Mapper类进行注入，它到底做了什么呢，让我们带着疑问来看看。

# 1、源码分析
```java
<bean id="springMapperScannerConfigurer" class="org.mybatis.spring.mapper.MapperScannerConfigurer">
    <!-- mapper接口存放的包 -->
    <property name="basePackage" value="com.xx.xx"/>
 </bean>
```

上面是我们在集成spring的时候的配置，那么mybatis是如何来扫描它们并放入spring的呢？我们来看看MapperScannerConfigurer这个类的源码。首先因为 `MapperScannerConfigurer` 实现了 `BeanDefinitionRegistryPostProcessor` 这个接口，并实现了 `postProcessBeanDefinitionRegistry` 这个方法，在spring初始化的时候将bean以及bean的一些属性信息保存至 `BeanDefinitionHolder` 中。

```java
public class MapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware{
    
      @Override
      public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        if (this.processPropertyPlaceHolders) {
          processPropertyPlaceHolders();
        }
    
        // 通过bean的注册信息实例化Mapper扫描类
        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        scanner.setAddToConfig(this.addToConfig);
        scanner.setAnnotationClass(this.annotationClass);
        scanner.setMarkerInterface(this.markerInterface);
        scanner.setSqlSessionFactory(this.sqlSessionFactory);
        scanner.setSqlSessionTemplate(this.sqlSessionTemplate);
        scanner.setSqlSessionFactoryBeanName(this.sqlSessionFactoryBeanName);
        scanner.setSqlSessionTemplateBeanName(this.sqlSessionTemplateBeanName);
        scanner.setResourceLoader(this.applicationContext);
        scanner.setBeanNameGenerator(this.nameGenerator);
        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
      }
      
      /**
       * 替换property中的通配符为配置文件中的值 
       * 例如：
       *    basePackage=com.jd.jr.bt.mapper
       *    ${basePackage} 替换为 com.jd.jr.bt.mapper
       */
      private void processPropertyPlaceHolders() {
          // 获取spring配置文件中配置的PropertyResourceConfigurer以及它的子类，我们最常用的PropertyPlaceholderConfigurer也能获取到
          Map<String, PropertyResourceConfigurer> prcs = applicationContext.getBeansOfType(PropertyResourceConfigurer.class);
      
          // 如果存在配置文件就执行替换逻辑
          if (!prcs.isEmpty() && applicationContext instanceof ConfigurableApplicationContext) {
            // 通过beanName拿到配置文件的MapperScannerConfigurer的配置Bean
            BeanDefinition mapperScannerBean = ((ConfigurableApplicationContext) applicationContext)
                .getBeanFactory().getBeanDefinition(beanName);
      
            // PropertyResourceConfigurer does not expose any methods to explicitly perform
            // property placeholder substitution. Instead, create a BeanFactory that just
            // contains this mapper scanner and post process the factory.
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            factory.registerBeanDefinition(beanName, mapperScannerBean);
      
            for (PropertyResourceConfigurer prc : prcs.values()) {
              // 通过手动调用来提前替换${}为配置的值
              prc.postProcessBeanFactory(factory);
            }
      
            // 获取替换后的值
            PropertyValues values = mapperScannerBean.getPropertyValues();
      
            // 用替换后的值更新内存中的值
            this.basePackage = updatePropertyValue("basePackage", values);
            this.sqlSessionFactoryBeanName = updatePropertyValue("sqlSessionFactoryBeanName", values);
            this.sqlSessionTemplateBeanName = updatePropertyValue("sqlSessionTemplateBeanName", values);
          }
        }
}
```

`postProcessBeanDefinitionRegistry` 方法在一开始通过判断 `processPropertyPlaceHolders` 是不是true，如果配置为true，就会执行值替换通配符的逻辑，因为 `postProcessBeanDefinitionRegistry` 方法会在 `postProcessBeanFactory` 方法前执行，所以这里手动调用了 `postProcessBeanFactory` 来提前替换通配符。所以当你用配置文件去管理这些property的值的时候，这个配置才有用。否则不用配置。下面是使用的示例：

```java
<bean id="commonMapperScannerConfigurer" class="org.mybatis.spring.mapper.MapperScannerConfigurer">
    <property name="sqlSessionFactoryBeanName" value="#{sqlSessionFactoryBeanName}"/>
    <property name="basePackage" value="${basePackage}"/>
    <property name="processPropertyPlaceHolders" value="true" />
</bean>

xxx.properties
basePackage=com.jd.jr.bt.mapper
sqlSessionFactoryBeanName=sqlSessionFactory
```

接下来调用 `ClassPathMapperScanner` 的 `registerFilters` 方法来注册过滤器。

```java
public void registerFilters() {
    boolean acceptAllInterfaces = true;

    // 对于annotationClass属性的处理
    // 如果annotationClass不为空，表示用户设置了此属性，那么就要根据此属性生成过滤器以保证达到用户
    // 想要的效果，而封装此属性的过滤器就是AnnotationTypeFilter.AnnotationTypeFilter保证在扫描对应
    // java文件时只接受标记有注解为annotationClass接口 
    if (this.annotationClass != null) {
      // 把annotationClass加入includeFilter中
      addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
      acceptAllInterfaces = false;
    }

    // 对于markerInterface属性的处理
    if (this.markerInterface != null) {
      addIncludeFilter(new AssignableTypeFilter(this.markerInterface) {
        @Override
        protected boolean matchClassName(String className) {
          // 对于markerInterface返回false，其他实现它接口的类都通过
          return false;
        }
      });
      acceptAllInterfaces = false;
    }

    // 全局默认处理
    // 在上面两个属性中如果存在其中任何属性，acceptAllInterfaces的值将会被改变，但是如果用户没有设定以上的属性
    // 那么，Spring会为我们增加一个默认的过滤器实现TypeFilter接口的局部类，旨在接受所有接口文件。
    if (acceptAllInterfaces) {
      // default include filter that accepts all classes
      addIncludeFilter(new TypeFilter() {
        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
          // mybatis让spring扫描mapper接口的时候匹配规则的时候都通过
          return true;
        }
      });
    }

    // 不扫描package-info.java文件
    addExcludeFilter(new TypeFilter() {
      @Override
      public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        String className = metadataReader.getClassMetadata().getClassName();
        return className.endsWith("package-info");
      }
    });
  }
```

如果配置的时候指定了 `annotationClass` 或者 `markerInterface` ，那么spring就会去扫描指定的注解类或自定义接口实现类。否则扫描默认mapper接口包中的所有接口。这个 `addIncludeFilter` 和 `addExcludeFilter` 用到了spring-context中的类：`ClassPathScanningCandidateComponentProvider`。

```java
private final List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();
private final List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();

public void addIncludeFilter(TypeFilter includeFilter){
       this.includeFilters.add(includeFilter);
}

public void addExcludeFilter(TypeFilter excludeFilter){
       this.excludeFilters.add(0,excludeFilter);
}
```

设置完过滤器后，真正要指定扫描方法 `scan` 方法扫描 `basePackages` 中的类了。该方法在spring-context的 `ClassPathBeanDefinitionScanner` 类里。

```java
public int scan(String... basePackages) {
    int beanCountAtScanStart = this.registry.getBeanDefinitionCount();

    doScan(basePackages);

    // Register annotation config processors, if necessary.
    if (this.includeAnnotationConfig) {
        AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
    }

    return (this.registry.getBeanDefinitionCount() - beanCountAtScanStart);
}
```

scan是个全局方法，扫描工作通过 `doScan(basePackages)` 委托给了mybatis自己实现的 `doScan` 方法，同时，还包括了 `includeAnnotationConfig` 属性的处理，`AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);` 代码主要是完成对于注解处理器的简单注册，我们下面主要分析下扫描功能的实现。这个方法在mybatis的 `ClassPathMapperScanner` 这个类里。

```java
public Set<BeanDefinitionHolder> doScan(String... basePackages) {
    // 调用spring的doScan
    Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

    if (!beanDefinitions.isEmpty()) {
        processBeanDefinitions(beanDefinitions);
    }
    return beanDefinitions;
}
```
  
我们可以看到mybatis自己实现的doScan方法先调用了spring的doScan方法（spring扫描通用接口，@Controller、@Service、@Component都是走这里扫描），然后拿到了过滤后的beanDefinition集合，然后进行处理。
  
下面是我们看看spring中的 `doScan` 方法。我们可以看到最重要的就是`findCandidateComponents`这个方法，它主要过滤了一些不通过的bean，最后把通过的全部返回。我们来看看是怎么处理这些组件的。

```java
protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
    Assert.notEmpty(basePackages, "At least one base package must be specified");
    Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
    for (String basePackage : basePackages) {
        // 扫描basePackage路径下的java文件，找出候选组件
        Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
        for (BeanDefinition candidate : candidates) {
            //解析scope属性
            ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
            candidate.setScope(scopeMetadata.getScopeName());
            String beanName = this.beanNameGenerator.generateBeanName(candidate, this.registry);
            if (candidate instanceof AbstractBeanDefinition) {
                postProcessBeanDefinition((AbstractBeanDefinition) candidate, beanName);
            }
            if (candidate instanceof AnnotatedBeanDefinition) {
                //如果是AnnotationBeanDefinition类型的bean需要检测下常用注解如：Primary,Lazy等。
                AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
            }
            //检测当前bean是否已经注册
            if (checkCandidate(beanName, candidate)) {
                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
                //如果当前bean是用于生成代理的bean那么需要进一步处理
                definitionHolder =
                        AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
                beanDefinitions.add(definitionHolder);
                registerBeanDefinition(definitionHolder, this.registry);
            }
        }
    }
    return beanDefinitions;
}

public Set<BeanDefinition> findCandidateComponents(String basePackage) {
    Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
    try {
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(basePackage) + '/' + this.resourcePattern;
        Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
        boolean traceEnabled = logger.isTraceEnabled();
        boolean debugEnabled = logger.isDebugEnabled();
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                try {
                    MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                    // 过滤方法1
                    if (isCandidateComponent(metadataReader)) {
                        ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                        sbd.setResource(resource);
                        sbd.setSource(resource);
                        // 过滤方法2
                        if (isCandidateComponent(sbd)) {
                            candidates.add(sbd);
                        }
                    }
                } catch (Throwable ex) {
                    throw new BeanDefinitionStoreException("Failed to read candidate component class: " + resource, ex);
                }
            }
        }
    } catch (IOException ex) {
        throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
    }
    return candidates;
}

/**
 * 根据之前加入的filter过滤出符合条件的。
 */
protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
    for (TypeFilter tf : this.excludeFilters) {
        if (tf.match(metadataReader, this.metadataReaderFactory)) {
            return false;
        }
    }
    for (TypeFilter tf : this.includeFilters) {
        if (tf.match(metadataReader, this.metadataReaderFactory)) {
            return isConditionMatch(metadataReader);
        }
    }
    return false;
}

/**
 * spring默认的过滤方法，mybatis要实现对接口的扫描，这个方法不能用，mybatis对它进行了重写
 * 重写的方法在下面。
 */
protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		AnnotationMetadata metadata = beanDefinition.getMetadata();
		return (metadata.isIndependent() && (metadata.isConcrete() ||
				(metadata.isAbstract() && metadata.hasAnnotatedMethods(Lookup.class.getName()))));
}

protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
    // 是接口就通过
    return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
}
```

mybatis重写了 `isCandidateComponent` 方法来让spring通过过滤来扫描接口类。现在扫描完毕，开始注册mapper到configuration中去。下面我们来看看扫描里面最重要的一个方法 `processBeanDefinitions` ，它

```java
private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
    GenericBeanDefinition definition;
    for (BeanDefinitionHolder holder : beanDefinitions) {
        definition = (GenericBeanDefinition) holder.getBeanDefinition();

        // the mapper interface is the original class of the bean
        // but, the actual class of the bean is MapperFactoryBean
        // 开始构造MapperFactoryBean类型的bean.设置bean的构造函数传入mapper接口作为参数
        definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName()); // issue #59
        // bean的真实类型由mapper接口统一换为MapperFactoryBean
        definition.setBeanClass(this.mapperFactoryBean.getClass());

        definition.getPropertyValues().add("addToConfig", this.addToConfig);

        boolean explicitFactoryUsed = false;
        if (StringUtils.hasText(this.sqlSessionFactoryBeanName)) {
            definition.getPropertyValues().add("sqlSessionFactory", new RuntimeBeanReference(this.sqlSessionFactoryBeanName));
            explicitFactoryUsed = true;
        } else if (this.sqlSessionFactory != null) {
            definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
            explicitFactoryUsed = true;
        }

        if (StringUtils.hasText(this.sqlSessionTemplateBeanName)) {
            definition.getPropertyValues().add("sqlSessionTemplate", new RuntimeBeanReference(this.sqlSessionTemplateBeanName));
            explicitFactoryUsed = true;
        } else if (this.sqlSessionTemplate != null) {
            definition.getPropertyValues().add("sqlSessionTemplate", this.sqlSessionTemplate);
            explicitFactoryUsed = true;
        }

        // 如果没有设置SqlSessionFactory或者SqlSessionTemplate，按类型注入
        if (!explicitFactoryUsed) {
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }
    }
}
```

上面代码的意思就是构造一个 `MapperFactoryBean` ，并把 `sqlSessionFactory` 或 `sqlSessionTemplate` 注入到 `MapperFactoryBean` 中，并把扫描到的mapper接口类注入到 `mapperInterface` 字段。

下面我们看看 `MapperFactoryBean` 是如何被Spring注入到每个接口里面的。

```
public class MapperFactoryBean<T> extends SqlSessionDaoSupport implements FactoryBean<T> {
    @Override
    protected void checkDaoConfig() {
        super.checkDaoConfig();

        notNull(this.mapperInterface, "Property 'mapperInterface' is required");

        Configuration configuration = getSqlSession().getConfiguration();
        if (this.addToConfig && !configuration.hasMapper(this.mapperInterface)) {
            try {
                // 把mapperInterface加入configuration中
                configuration.addMapper(this.mapperInterface);
            } catch (Exception e) {
                logger.error("Error while adding the mapper '" + this.mapperInterface + "' to configuration.", e);
                throw new IllegalArgumentException(e);
            } finally {
                ErrorContext.instance().reset();
            }
        }
    }
    
    @Override
    public T getObject() throws Exception {
        return getSqlSession().getMapper(this.mapperInterface);
    }

    @Override
    public Class<T> getObjectType() {
        return this.mapperInterface;
    }
}

public abstract class SqlSessionDaoSupport extends DaoSupport {
    private SqlSession sqlSession;
    private boolean externalSqlSession;

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        if (!this.externalSqlSession) {
            this.sqlSession = new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSession = sqlSessionTemplate;
        this.externalSqlSession = true;
    }
}

public abstract class DaoSupport implements InitializingBean {
    protected final Log logger = LogFactory.getLog(this.getClass());

    public DaoSupport() {
    }

    public final void afterPropertiesSet() throws IllegalArgumentException, BeanInitializationException {
        this.checkDaoConfig();

        try {
            this.initDao();
        } catch (Exception var2) {
            throw new BeanInitializationException("Initialization of DAO failed", var2);
        }
    }

    protected abstract void checkDaoConfig() throws IllegalArgumentException;

    protected void initDao() throws Exception {
    }
}
```

上面的代码可以发现，MapperFactoryBean继承自SqlSessionDaoSupport，而SqlSessionDaoSupport继承自DaoSupport，DaoSupport实现了InitializingBean，在afterPropertiesSet方法中调用了checkDaoConfig方法。而MapperFactoryBean重写了它。

大家还记得吗？刚刚扫描的时候有一个重要的方法 `processBeanDefinitions`，它把 `sqlSessionFactory` 或 `sqlSessionTemplate` 注入到 `MapperFactoryBean` 中，并把扫描到的mapper接口类注入到 `mapperInterface` 字段。最后，mybatis利用FactoryBean让spring对每个接口类返回不同的类型，并且注入的都是mybatis动态代理得到的MapperProxy。关于 `getSqlSession().getMapper(this.mapperInterface);` 如何拿到代理类的，我在另一篇博客中做了讲解：[]()

# 2、总结
* 1）拿到spring配置文件中的MapperScannerConfigurer，如果配置了 `processPropertyPlaceHolders = true` 使用 `${}` 来替换 `PropertyPlaceholderConfigurer` 加载的配置文件中的值。
* 2）过滤扫描项：如果配置了 `annotationClass` ，把标注了该注解得类加入扫描列表中。如果配置了 `markerInterface` ，则把该接口过滤出扫描列表，实现它的子类全部加入。并且不扫描 `package-info.java` 文件。
* 3）开始扫描文件，重写了spring的scan方法来实现扫描接口。
* 4）拿到spring缓存的所有扫描完的接口bean，实例化好 `MapperFactoryBean`，并注入 `sqlSessionFactory` 和 对应的mapper接口类。
* 5、注入bean的时候，调用 `MapperFactoryBean` 的 `getObject` 方法得到一个动态代理生成的Mapper接口类。
* 6、我们可以直接在代码中引入Mapper接口来调用方法了。