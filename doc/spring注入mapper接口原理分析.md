# spring注入mapper接口原理分析

mybatis结合spring入口在`MapperScannerConfigurer`，我们看看它到底做了什么，让spring轻易的把mybatis无缝衔接。

### 1.processPropertyPlaceHolders属性的处理

我们在集成mybatis-spring的时候，大多数都会使用这么一个配置来扫描mapper class，那么mybatis是如何来扫描它们并放入spring的呢？我们来看看MapperScannerConfigurer这个类的源码，首先因为MapperScannerConfigurer实现了BeanDefinitionRegistryPostProcessor这个接口，并实现了postProcessBeanDefinitionRegistry这个方法，在spring初始化的时候将bean以及bean的一些属性信息保存至BeanDefinitionHolder中。

```
public class MapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware{
      ...
    
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

postProcessBeanDefinitionRegistry方法在一开始通过判断processPropertyPlaceHolders是不是true，如果配置为true，就会执行值替换通配符的逻辑，因为postProcessBeanDefinitionRegistry方法会在postProcessBeanFactory方法前执行，所以这里手动调用了postProcessBeanFactory来提前替换通配符。所以当你用配置文件去管理这些property的值的时候，这个配置才有用。否则不用配置。下面是使用的示例：

```
<bean id="commonMapperScannerConfigurer" class="org.mybatis.spring.mapper.MapperScannerConfigurer">
    <property name="sqlSessionFactoryBeanName" value="#{sqlSessionFactoryBeanName}"/>
    <property name="basePackage" value="${basePackage}"/>
    <property name="processPropertyPlaceHolders" value="true" />
</bean>

xxx.properties
basePackage=com.jd.jr.bt.mapper
sqlSessionFactoryBeanName=sqlSessionFactory

```

接下来注册ClassPathMapperScanner，并且设置配置的值，并且注册过滤器scan.registerFilters()和扫描scan.scan(...)，下面将详细解释这两个方法。

### 2.根据配置属性生成过滤器

```
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

从上面的函数我们可以看出，控制扫描文件Spring通过不同的过滤器完成，这些定义的过滤器记录在了includeFilters和excludeFilters属性中。如果没有配置annotationClass和markerInterface，默认全部扫描所有的mapper接口。

```
public void addIncludeFilter(TypeFilter includeFilter){
       this.includeFilters.add(includeFilter);
}

public void addExcludeFilter(TypeFilter excludeFilter){
       this.excludeFilters.add(0,excludeFilter);
}
```

### 3.扫描java文件

设置了相关属性以及生成了对应的过滤器后就可以进行文件的扫描了，扫描工作是有ClassPathMapperScanner类的父类ClassPathBeanDefinitionScanner（spring的类）的scan方法完成的。

```
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

scan是个全局方法，扫描工作通过`doScan(basePackages)`委托给了doScan方法，同时，还包括了includeAnnotationConfig属性的处理，AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);代码主要是完成对于注解处理器的简单注册，我们下面主要分析下扫描功能的实现。这个方法在mybatis的`ClassPathMapperScanner`这个类里。

```
@Override
  public Set<BeanDefinitionHolder> doScan(String... basePackages) {
    Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

    if (beanDefinitions.isEmpty()) {
      // 没有扫描到文件发出警告
      logger.warn("No MyBatis mapper was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
    } else {
      // 处理bean
      processBeanDefinitions(beanDefinitions);
    }

    return beanDefinitions;
  }
  ```
  
我们可以看到mybatis自己实现的doScan方法先调用了spring的doScan方法（spring扫描通用接口，@Controller、@Service、@Component都是走这里扫描），然后拿到了过滤后的beanDefinition集合，然后进行处理。
  
```
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
```
我们可以看到最重要的就是`findCandidateComponents`这个方法，它主要过滤了一些不通过的bean，最后把通过的全部返回。我们来看看是怎么处理这些组件的。

```
public Set<BeanDefinition> findCandidateComponents(String basePackage) {
    Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
    try {
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(basePackage) + '/' + this.resourcePattern;
        Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
        boolean traceEnabled = logger.isTraceEnabled();
        boolean debugEnabled = logger.isDebugEnabled();
        for (Resource resource : resources) {
            if (traceEnabled) {
                logger.trace("Scanning " + resource);
            }
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
                            if (debugEnabled) {
                                logger.debug("Identified candidate component class: " + resource);
                            }
                            candidates.add(sbd);
                        }
                        else {
                            if (debugEnabled) {
                                logger.debug("Ignored because not a concrete top-level class: " + resource);
                            }
                        }
                    }
                    else {
                        if (traceEnabled) {
                            logger.trace("Ignored because not matching any filter: " + resource);
                        }
                    }
                }
                catch (Throwable ex) {
                    throw new BeanDefinitionStoreException(
                            "Failed to read candidate component class: " + resource, ex);
                }
            }
            else {
                if (traceEnabled) {
                    logger.trace("Ignored because not readable: " + resource);
                }
            }
        }
    }
    catch (IOException ex) {
        throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
    }
    return candidates;
}
```

findCandidateComponents方法根据传入的包路径信息并结合类文件路径拼接成文件的绝对路径，同时完成了文件的扫描过程并且根据对应的文件生成了对应的bean,使用ScannedGenericBeanDefinition类型的bean承载信息，bean中值记录了resource和source信息。这里，我们更感兴趣的是isCandidateCompanent(metadataReader)，此句代码用于判断当前扫描的文件是否符合要求，而我们之前注册的过滤器也是在此派上用场的。我们来看看对应的过滤方法1和2.

```
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
 * spring默认的过滤方法2，mybatis要实现对接口的扫描，这个方法不能用，mybatis对它进行了重写
 * 重写的方法在下面。
 */
protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		AnnotationMetadata metadata = beanDefinition.getMetadata();
		return (metadata.isIndependent() && (metadata.isConcrete() ||
				(metadata.isAbstract() && metadata.hasAnnotatedMethods(Lookup.class.getName()))));
}

@Override
  protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
    // 是接口就通过
    return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
  }
```

我们看到mybatis重写了isCandidateComponent方法来让spring通过过滤。

现在扫描完毕，开始注册mapper到configuration中去。

```
private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
    GenericBeanDefinition definition;
    for (BeanDefinitionHolder holder : beanDefinitions) {
      definition = (GenericBeanDefinition) holder.getBeanDefinition();

      if (logger.isDebugEnabled()) {
        logger.debug("Creating MapperFactoryBean with name '" + holder.getBeanName() 
          + "' and '" + definition.getBeanClassName() + "' mapperInterface");
      }

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
        if (explicitFactoryUsed) {
          logger.warn("Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
        }
        definition.getPropertyValues().add("sqlSessionTemplate", new RuntimeBeanReference(this.sqlSessionTemplateBeanName));
        explicitFactoryUsed = true;
      } else if (this.sqlSessionTemplate != null) {
        if (explicitFactoryUsed) {
          logger.warn("Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
        }
        definition.getPropertyValues().add("sqlSessionTemplate", this.sqlSessionTemplate);
        explicitFactoryUsed = true;
      }

      // 如果没有设置SqlSessionFactory或者SqlSessionTemplate，按类型注入
      if (!explicitFactoryUsed) {
        if (logger.isDebugEnabled()) {
          logger.debug("Enabling autowire by type for MapperFactoryBean with name '" + holder.getBeanName() + "'.");
        }
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
      }
    }
  }
```

可以发现上面利用beanDefinition构造MapperFactoryBean，传入一系列的参数，如果是一个SqlSessionFactory的情况，可以不用设置SqlSessionFactory和SqlSessionTemplate，spring会自动注入类型相同的类，那么MapperFactoryBean是如何运作的？spring是如何把MapperFactoryBean注入到各个接口的？

### 4.注入原理分析

```
public class MapperFactoryBean<T> extends SqlSessionDaoSupport implements FactoryBean<T> {
}

public abstract class SqlSessionDaoSupport extends DaoSupport {
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

上面的代码可以发现，MapperFactoryBean继承自SqlSessionDaoSupport，而SqlSessionDaoSupport继承自DaoSupport，DaoSupport实现了InitializingBean，在afterPropertiesSet方法中调用了checkDaoConfig方法。下面是MapperFactoryBean的checkDaoConfig方法的实现。

```
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
```

mybatis最后把mapper接口和xml文件关联起来`configuration.addMapper(this.mapperInterface)`，我们关注到，MapperFactoryBean实现了FactoryBean接口，我们看看对应的方法。

```
@Override
  public T getObject() throws Exception {
    return getSqlSession().getMapper(this.mapperInterface);
  }

  @Override
  public Class<T> getObjectType() {
    return this.mapperInterface;
  }
```

可以看到，mybatis利用FactoryBean让spring对每个接口类返回不同的类型，并且注入的都是mybatis动态代理得到的MapperProxy。

至此，MapperScannerConfigurer原理分析结束。可以看出，mybatis通过一个包名先是得到下面所有的类，注册到spring容器中。然后再把对应的mapper接口放入configuration中，最后根据FactoryBean去获取真实的类型和值去注入，这样我们就可以直接在代码中引入Mapper接口来使用了。