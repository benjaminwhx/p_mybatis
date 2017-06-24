# MyBatis Generator详解

中文文档地址：[mybatis-generator](http://mbg.cndocs.tk/index.html)

## Generator xml配置文件详解

### 1、配置文件头

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
```
使用最新版的MBG需要使用上面的xml头，配置文件必须包含上面的`DOCTYPE`。

### 2、根节点`<generatorConfiguration>`
`<generatorConfiguration>`节点没有任何属性，直接写节点即可，如下：
```
<generatorConfiguration>
    <!-- 具体配置内容 -->
</generatorConfiguration>  
```

### 3、`<generatorConfiguration>`子元素
从这段开始，就是配置的主要内容，这些配置都是`generatorConfiguration`元素的子元素。  
包含以下子元素（有严格的顺序）：  

1、`<properties>` (0个或1个)  
2、`<classPathEntry>` (0个或多个)  
3、`<context>` (1个或多个)  

#### 3.1、`<properties>` 元素
这个元素用来指定外部的属性元素，不是必须的元素。  

元素用于指定一个需要在配置中解析使用的外部属性文件，引入属性文件后，可以在配置中使用 ${property}这种形式的引用，通过这种方式引用属性文件中的属性值。 对于后面需要配置的**jdbc信息**和targetProject属性会很有用。  

这个属性可以通过resource或者url来指定属性文件的位置，这两个属性只能使用其中一个来指定，同时出现会报错。  

`resource`：指定**classpath**下的属性文件，使用类似`com/myproject/generatorConfig.properties`这样的属性值。
`url`：可以指定文件系统上的特定位置，例如`file:///C:/myfolder/generatorConfig.properties`

#### 3.2、`<classPathEntry>` 元素
这个元素可以0或多个，不受限制。  

最常见的用法是通过这个属性指定驱动的路径，例如：

    <classPathEntry location="E:\mysql\mysql-connector-java-5.1.29.jar"/>
    
**注意，classPathEntry只在下面这两种情况下才有效：**
1、当加载 JDBC 驱动内省数据库时
2、当加载根类中的 JavaModelGenerator 检查重写的方法时

因此，如果你需要加载其他用途的jar包，classPathEntry起不到作用，不能这么写，解决的办法就是将你用的jar包添加到类路径中，在Eclipse等IDE中运行的时候，添加jar包比较容易。当从命令行执行的时候，需要用java -cp xx.jar,xx2.jar xxxMainClass这种方式在-cp后面指定来使用(注意-jar会导致-cp无效)。

#### 3.3 `<context>` 元素
在MBG的配置中，至少需要有一个`<context>`元素。

`<context>`元素用于指定生成一组对象的环境。例如指定要连接的数据库，要生成对象的类型和要处理的数据库中的表。运行MBG的时候还可以指定要运行的`<context>`。

该元素只有一个**必选属性**`id`，用来唯一确定一个`<context>`元素，该`id`属性可以在运行MBG的使用。

此外还有几个**可选属性**：

`defaultModelType`:**这个属性很重要**，这个属性定义了MBG如何生成**实体类**。
这个属性有以下可选值：

`conditional`:*这是默认值*,这个模型和下面的`hierarchical`类似，除了如果那个单独的类将只包含一个字段，将不会生成一个单独的类。 因此,如果一个表的主键只有一个字段,那么不会为该字段生成单独的实体类,会将该字段合并到基本实体类中。
`flat`:该模型为每一张表只生成一个实体类。这个实体类包含表中的所有字段。**这种模型最简单，推荐使用。**
`hierarchical`:如果表有主键,那么该模型会产生一个单独的主键实体类,如果表还有BLOB字段， 则会为表生成一个包含所有BLOB字段的单独的实体类,然后为所有其他的字段生成一个单独的实体类。 MBG会在所有生成的实体类之间维护一个继承关系。  

`targetRuntime`:此属性用于指定生成的代码的运行时环境。该属性支持以下可选值：

`MyBatis3`:*这是默认值*
`MyBatis3Simple`
`Ibatis2Java2`
`Ibatis2Java5` 一般情况下使用默认值即可，有关这些值的具体作用以及区别请查看中文文档的详细内容。
`introspectedColumnImpl`:该参数可以指定扩展`org.mybatis.generator.api.IntrospectedColumn`该类的实现类。该属性的作用可以查看[扩展MyBatis Generator](http://mbg.cndocs.tk/reference/extending.html)。
一般情况下，我们使用如下的配置即可：

```
<context id="Mysql" defaultModelType="flat">
```

如果你希望不生成和`Example`查询有关的内容，那么可以按照如下进行配置:

```
<context id="Mysql" targetRuntime="MyBatis3Simple" defaultModelType="flat">
```

