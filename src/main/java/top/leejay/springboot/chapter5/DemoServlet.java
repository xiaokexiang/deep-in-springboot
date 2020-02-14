package top.leejay.springboot.chapter5;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Li Jie
 * @date 2/14/2020
 */
public class DemoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("Demo Servlet ...");
    }
}

class DemoServletRegistryBean extends ServletRegistrationBean<DemoServlet> {
    public DemoServletRegistryBean(DemoServlet servlet, String... urlMappings) {
        super(servlet, urlMappings);
    }
}

@Configuration
class ServletConfiguration {
    @Bean
    public DemoServletRegistryBean demoServletRegistryBean() {
        return new DemoServletRegistryBean(new DemoServlet(), "/servlet");
    }
}