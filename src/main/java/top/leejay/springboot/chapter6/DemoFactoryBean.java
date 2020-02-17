package top.leejay.springboot.chapter6;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * @author Li Jie
 * @date 2/17/2020
 */
@Configuration
public class DemoFactoryBean implements FactoryBean<Demo> {

    public String getName() {
        return "demoFactoryBean";
    }

    private static List<Demo> demos = new ArrayList<>();

    static {
        demos.add(new Demo("zhangsan"));
        demos.add(new Demo("lisi"));
    }

    @Override
    public Demo getObject() {
        return demos.get(new Random().nextInt(2));
    }

    @Override
    public Class<?> getObjectType() {
        return Demo.class;
    }
}

class Demo {
    private String name;

    public Demo(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }
}

class DemoTest {
    public static void main(String[] args) {
        // 注册DemoFactoryBean到IOC容器中
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DemoFactoryBean.class);
        // 获取的是DemoFactoryBean.getObject()方法的返回值
        Demo demo = (Demo) context.getBean("demoFactoryBean");
        System.out.println(demo.getName());

        // 加上&， 则获取的是DemoFactoryBean对象本身
        DemoFactoryBean bean = (DemoFactoryBean) context.getBean("&demoFactoryBean");
        System.out.println(bean.getName());
    }
}