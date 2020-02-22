package top.leejay.springboot.chapter7;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Li Jie
 * @date 2/22/2020
 * 实现ApplicationContextInitializer接口，用于在刷新容器(初始化ConfigurableApplicationContext)之前的回调接口
 */
public class MyApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.out.println("ApplicationContextInitializer before refresh IOC ...");
    }
}
