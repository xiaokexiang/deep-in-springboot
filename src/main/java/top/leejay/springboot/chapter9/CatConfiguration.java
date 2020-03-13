package top.leejay.springboot.chapter9;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Li Jie
 * @date 3/13/2020
 * 将cat 注入到IOC容器中
 */
@Configuration
@ComponentScan("top.leejay.springboot.chapter9")
public class CatConfiguration {

    @Bean
    public Cat cat() {
        // 创建一个名为cat的bean注入到IOC容器中，其name为a little cat
        return new Cat("a little cat");
    }
}
