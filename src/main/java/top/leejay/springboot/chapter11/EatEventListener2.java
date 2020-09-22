package top.leejay.springboot.chapter11;

import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author xiaokexiang
 */
@Component
public class EatEventListener2 {

    @EventListener
    // 监听器的注解优先于实现ApplicationListener，通过order注解降注解的优先级
    @Order(value = Ordered.LOWEST_PRECEDENCE)
    public void getEvent(EatEvent eatEvent) {
        System.out.println("EatEventListener2 get event from " + eatEvent.getName());
    }
}
