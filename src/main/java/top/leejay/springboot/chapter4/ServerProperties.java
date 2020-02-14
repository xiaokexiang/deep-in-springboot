package top.leejay.springboot.chapter4;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

/**
 * @author Li Jie
 * @date 2/14/2020
 * 测试@EnableConfigurationProperties 与 @ConfigurationProperties注解配合使用
 * 如果不使用@EnableConfigurationProperties可以采用@Component
 */
@ConfigurationProperties(prefix = "server")
public class ServerProperties {

    private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

/**
 * EnableConfigurationProperties用于注册ServerProperties配置类到容器中
 * Configuration则注册ServerPropertiesRegister到容器中
 * 此方法的优点是: 可以将配置类进行整合，统一管理
 */
@Configuration
@EnableConfigurationProperties(ServerProperties.class)
class ServerPropertiesRegister {

}

@RestController
class ServerPropertiesController {

    @Resource
    private ServerProperties properties;

    @GetMapping("/pro")
    public int getPort() {
        return properties.getPort();
    }
}