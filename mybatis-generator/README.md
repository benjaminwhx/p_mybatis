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

1、<properties> (0个或1个)  
2、<classPathEntry> (0个或多个)  
3、<context> (1个或多个)  

#### 3.1、`<properties>` 元素
这个元素用来指定外部的属性元素，不是必须的元素。  

元素用于指定一个需要在配置中解析使用的外部属性文件，引入属性文件后，可以在配置中使用 ${property}这种形式的引用，通过这种方式引用属性文件中的属性值。 对于后面需要配置的**jdbc信息**和targetProject属性会很有用。  

这个属性可以通过resource或者url来指定属性文件的位置，这两个属性只能使用其中一个来指定，同时出现会报错。  

`resource`：指定**classpath**下的属性文件，使用类似`com/myproject/generatorConfig.properties`这样的属性值。
`url`：可以指定文件系统上的特定位置，例如`file:///C:/myfolder/generatorConfig.properties`

#### 3.2、`<properties>` 元素