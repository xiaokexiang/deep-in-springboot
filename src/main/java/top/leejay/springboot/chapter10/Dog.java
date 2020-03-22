package top.leejay.springboot.chapter10;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Li Jie
 * @date 3/22/2020
 * 测试bean实例化与初始化的执行顺序
 */
public class Dog implements InitializingBean, DisposableBean {

    public Dog() {
        System.out.println("Dog construct run ...");
    }

    public void initMethod() {
        System.out.println("Dog init method ...");
    }

    public void destroyMethod() {
        System.out.println("Dog destroy method ...");
    }

    /** 需要注意的是该注解是Servlet标准*/
    @PostConstruct
    public void afterInit() {
        System.out.println("Dog postConstruct run ...");
    }

    @PreDestroy
    public void beforeDestroy() {
        System.out.println("Dog preDestroy ...");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Dog afterPropertiesSet ...");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("Dog disposable ...");
    }
}
