package top.leejay.springboot.chapter2;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.stream.Stream;

/**
 * @author xiaokexiang
 * @since 2020/2/12
 * color 测试类 用于查看class 是否被装配
 */
public class ColorApp {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ColorConfiguration.class);
        Stream.of(applicationContext.getBeanDefinitionNames()).forEach(System.out::println);
    }
}