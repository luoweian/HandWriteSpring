package com.william;

import com.spring.WApplicationContext;
import com.william.service.UserService;
import com.william.service.UserServiceImpl;

public class Test {

    public static void main(String[] args) {
        WApplicationContext applicationContext =
                new WApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
            userService.test();
    }
}
