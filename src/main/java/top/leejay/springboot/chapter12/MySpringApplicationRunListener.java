package top.leejay.springboot.chapter12;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author xiaokexiang
 * 简单模拟SpringApplicationRunListener监听器的功能
 */
public class MySpringApplicationRunListener implements SpringApplicationRunListener {

    private final SpringApplication application;

    private final String[] args;

    public MySpringApplicationRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }


    @Override
    public void started(ConfigurableApplicationContext context) {
        // IOC启动完毕，但没执行CommandLineRunner/ApplicationRunner前
        context.publishEvent(new ApplicationStartedEvent(this.application, this.args, context));
    }
}
