package top.leejay.springboot.chapter1;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Date;
import java.util.stream.Stream;

/**
 * @author xiaokexiang
 * @since 2020/2/12
 * @see org.springframework.boot.SpringBootConfiguration 中包含
 * @see org.springframework.context.annotation.Configuration 注解, Configuration修饰的类为配置类，可以理解成一个application.yml
 */
@Configuration
public class SpringConfiguration {
    // 作用等同于<bean id="getDate" class="java.util.Date"/>
    @Bean
    public Date getDate() {
        return new Date();
    }
}
class MainApp {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        // 获取所有的DefinitionBeans并打印
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        // 包括SpringConfiguration; getDate;
        Stream.of(beanDefinitionNames).forEach(System.out::println);
    }
}
