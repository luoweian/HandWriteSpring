package com.william.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

@Component
public class WilliamBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        System.out.println(bean + " 初始化前");
        if(beanName.equals("userService")){
            ((UserService) bean).setBeanName("williamBean");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        System.out.println(bean + " 初始化后 ------- ");
        return bean;
    }
}
