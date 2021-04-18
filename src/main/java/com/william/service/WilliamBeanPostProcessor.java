package com.william.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class WilliamBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        System.out.println(bean + " 初始化前");
        if(beanName.equals("userService")){
            ((UserServiceImpl) bean).setBeanName("williamBean");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        System.out.println(bean + " 初始化后");
        if("userService".equals(beanName)){
            Object aopInstance = Proxy.newProxyInstance(WilliamBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("Aop---------代理逻辑前------");
                    Object invoke = method.invoke(bean, args);
                    System.out.println("Aop---------代理逻辑后-------");
                    return invoke;
                }
            });
            return aopInstance;
        }
        return bean;
    }
}
