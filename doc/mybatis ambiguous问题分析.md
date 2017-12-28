# mybatis ambiguous问题分析

## 1、问题描述
```
Caused by: java.lang.IllegalArgumentException: selectAccountById is ambiguous in Mapped Statements collection (try using the full name including the namespace, or rename one of the entries)
```
这个问题其实不是mybatis的问题，字面意思就可以看出这个key模糊不清，没有指定namespace所导致的

## 2、问题起因以及解决方法
肯定是用户使用手动查询的时候没有指定namespace所导致的，应该是getSqlSession().selectList("com.xx.yy.findByIds")而不是etSqlSession().selectList("findByIds")

## 3、源码分析
在Configuration中有一个sql映射Map `mappedStatements`，它的类型是 `StrictMap`，这个map是mybatis自己定义的map，重写了put和get方法。下面是这两个方法的源码：

```
public V put(String key, V value) {
  if (containsKey(key)) {
    //如果已经存在此key了，直接报错
    throw new IllegalArgumentException(name + " already contains value for " + key);
  }
  if (key.contains(".")) {
    //如果有.符号，取得短名称，大致用意就是包名不同，类名相同，提供模糊查询的功能
    final String shortKey = getShortName(key);
    if (super.get(shortKey) == null) {
      //如果没有这个缩略，则放一个缩略
      super.put(shortKey, value);
    } else {
      //如果已经有此缩略，表示模糊，放一个Ambiguity型的
      super.put(shortKey, (V) new Ambiguity(shortKey));
    }
  }
  //再放一个全名
  return super.put(key, value);
  //可以看到，如果有包名，会放2个key到这个map，一个缩略，一个全名
}

public V get(Object key) {
  V value = super.get(key);
  //如果找不到相应的key，直接报错
  if (value == null) {
    throw new IllegalArgumentException(name + " does not contain value for " + key);
  }
  //如果是模糊型的，也报错，提示用户
  if (value instanceof Ambiguity) {
    throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
        + " (try using the full name including the namespace, or rename one of the entries)");
  }
  return value;
}
```

可以看到mybatis在初始化的时候，先把所有mapper文件的key（namespace+id）和对应的映射语句放到内存里，如果拿的时候根据namespace+id的规则来拿就不会有问题，但是如果只根据key来拿的话，就会让mybatis模糊，抛异常告诉用户。

## 4、结论
不同namespace可以指定相同id，手动查询的时候要使用namespace+id去查询。