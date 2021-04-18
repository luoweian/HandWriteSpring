package com.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class WApplicationContext {
    private Class configClass;

    private ConcurrentHashMap<String, Object> singletonObject = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();


    public WApplicationContext(Class configClass) {
        this.configClass = configClass;
        //解析配置类
        //获取AppConfig上的ComponentScane注解 -> 扫描路径 ---> 扫描  ---> beanDefinition ---> BeanDefinitionMap
        scan(configClass);

        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName, beanDefinition); //单例bean
                singletonObject.put(beanName, bean);
            }
        }

    }


    public Object createBean(String beanName, BeanDefinition beanDefinition){
        Class clazz = beanDefinition.getClazz();
        try {
            //实例化对象
            Object instance = clazz.getDeclaredConstructor().newInstance();

            //属性填充
            for (Field declaredField : clazz.getDeclaredFields()) {
                if(declaredField.isAnnotationPresent(Autowired.class)){
                    Object bean = getBean(declaredField.getName());
                    if(bean == null) throw new Exception("单例池找不到bean对象");
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }

            //Aware回调
            if(instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            //初始化
            if(instance instanceof InitializingBean){
                ((InitializingBean)instance).afterPropertiesSet();
            }


            //BeanPostProcessor

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class configClass) {

        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
//        Annotation declaredAnnotation = configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();//获取扫描路径
        path = path.replace(".", "/");
//        System.out.println(path);

        //扫描, 只扫描加@Component的类
        // Bootstrap  --->  jre/lib
        // Ext        --->  jre/ext/lib
        // App        --->  classpath   ---->
        ClassLoader classLoader = WApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                String fileName = f.getAbsolutePath();
                if(!fileName.endsWith(".class")) continue;
                String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                className = className.replace("\\", ".");
//                System.out.println(className);
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    if(clazz.isAnnotationPresent(Component.class)){

                        if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                            BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();

                        }

                        //解析类 ---->  BeanDefinition
                        //BeanDefinition
                        Component component = clazz.getDeclaredAnnotation(Component.class);
                        String beanName = component.value();
                        if(beanName.isEmpty()) beanName = clazz.getSimpleName().substring(0,1).toLowerCase() + clazz.getSimpleName().substring(1, clazz.getSimpleName().length());
                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setClazz(clazz);
                        if (clazz.isAnnotationPresent(Scope.class)) {
                            Scope scope = clazz.getDeclaredAnnotation(Scope.class);
                            beanDefinition.setScope(scope.value());
                        }else {
                            beanDefinition.setScope("singleton");
                        }

                        beanDefinitionMap.put(beanName,beanDefinition);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public Object getBean(String beanName){
        if(beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                return singletonObject.get(beanName);
            }else {
                //创建bean
                Object bean = createBean(beanName, beanDefinition);
                return bean;
            }
        }else{
            throw new NullPointerException("不存在Bean, 名字是： " + beanName);
        }
    }
}
