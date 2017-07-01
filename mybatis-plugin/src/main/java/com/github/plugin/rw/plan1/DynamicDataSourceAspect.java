package com.github.plugin.rw.plan1;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * User: 吴海旭
 * Date: 2017-07-01
 * Time: 下午4:39
 */
@Aspect
public class DynamicDataSourceAspect {

    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceAspect.class);

    @Pointcut("execution(* com.github.*.*(..))")
    public void pointCut() {

    }

    @Before(value = "pointCut()")
    public void before(JoinPoint point) {
        Object target = point.getTarget();
        String methodName = point.getSignature().getName();
        Class<?>[] clazz = target.getClass().getInterfaces();
        Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
        try {
            Method method = clazz[0].getMethod(methodName, parameterTypes);
            if (method != null && method.isAnnotationPresent(DataSource.class)) {
                DataSource data = method.getAnnotation(DataSource.class);
                DynamicDataSourceHolder.putDataSource(data.value());
            }
        } catch (Exception e) {
            logger.error(String.format("Choose DataSource error, method:%s, msg:%s", methodName, e.getMessage()));
        }
    }

    @After(value = "pointCut()")
    public void after(JoinPoint point) {
        DynamicDataSourceHolder.clear();
    }
}
