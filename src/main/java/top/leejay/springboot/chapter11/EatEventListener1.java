package top.leejay.springboot.chapter11;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author xiaokexiang
 */
@Component
public class EatEventListener1 implements ApplicationListener<EatEvent> {

    @Override
    public void onApplicationEvent(EatEvent event) {
        System.out.println("EatEventListener1 get event from " + event.getName());
    }
}
