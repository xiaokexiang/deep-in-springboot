package top.leejay.springboot.chapter11;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author xiaokexiang
 */
public class Test {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext("top.leejay.springboot.chapter11");
        applicationContext.publishEvent(new EatEvent("hello"));
        applicationContext.close();

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("top.leejay.springboot.chapter11");
        EatEventPublisher publisher = context.getBean(EatEventPublisher.class);
        publisher.publishEvent(new EatEvent("second hello"));
        context.close();
    }
}
