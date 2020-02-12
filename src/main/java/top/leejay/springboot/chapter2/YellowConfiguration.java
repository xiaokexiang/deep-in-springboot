package top.leejay.springboot.chapter2;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.leejay.springboot.chapter2.classes.Yellow;

/**
 * @author xiaokexiang
 * @since 2020/2/12
 * color 配置类 用于测试 @Configuration & @Bean 实现注解装配
 */
@Configuration
public class YellowConfiguration {
    @Bean
    Yellow yellow() {
        return new Yellow();
    }
}