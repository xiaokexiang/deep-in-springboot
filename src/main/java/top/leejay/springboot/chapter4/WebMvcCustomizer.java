package top.leejay.springboot.chapter4;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * @author Li Jie
 * @date 2/14/2020
 * 在WebMvc模块可以通过实现WebServerFactoryCustomizer接口来定制修改配置，作用与application.yml类似
 */

@Component
public class WebMvcCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        System.out.println("change properties by WebServerFactoryCustomizer");
        factory.setPort(8888);
        factory.setContextPath("/springboot");
    }
}
