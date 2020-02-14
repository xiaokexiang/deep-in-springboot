### SpringMvc的自动装配

- WebMvcAutoConfiguration

  `WebMvc的自动装配入口是WebMvcAutoConfiguration。大部分的自动装配都是XXXAutoConfiguration结尾`
  
  ```java
    // 表明当前类是配置类
    @Configuration(proxyBeanMethods = false)
    // 判断当前是否是Servlet环境
    @ConditionalOnWebApplication(type = Type.SERVLET)
    // 当前环境需要有Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class
    @ConditionalOnClass({ Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class })
    // 如果没有自定义WebMvc的配置类，则使用本自动配置
    @ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
    // 执行顺序低于最高级，数字越小优先级越高
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
    // 自动装配执行顺序在DispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class,ValidationAutoConfiguration.class之后
    @AutoConfigureAfter({ DispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class,
        ValidationAutoConfiguration.class })
    public class WebMvcAutoConfiguration {
  
        @Configuration(proxyBeanMethods = false)
        // 用于注册处理器适配器 和 处理器映射器
        @Import(EnableWebMvcConfiguration.class)
        @EnableConfigurationProperties({ WebMvcProperties.class, ResourceProperties.class })
        @Order(0)
        public static class WebMvcAutoConfigurationAdapter implements WebMvcConfigurer {
        
        }
  
        @Configuration(proxyBeanMethods = false)
        public static class EnableWebMvcConfiguration extends DelegatingWebMvcConfiguration implements ResourceLoaderAware {
          // 注册处理器适配器，处理器映射器，Hibernate validator和全局异常处理器等
        }
    }
  ```
  > 在SpringBoot2.x中，自定义的WebMvc配置需要实现 WebMvcConfigurer 接口，并重写接口中需要配置的方法即可。
  > WebMvcAutoConfigurationAdapter提供了默认的WebMvc配置，其中包括视图解析器、静态资源映射、主页映射等。
  > EnableWebMvcConfiguration中注册了处理器适配器，处理器映射器，Hibernate validator和全局异常处理器等组件。

- DispatcherServletAutoConfiguration
  
  ```java
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = Type.SERVLET)
    // 存在DispatcherServlet才会使用本配置类
    @ConditionalOnClass(DispatcherServlet.class)
    // 执行顺序在ServletWebServerFactoryAutoConfiguration之后，可以先查看
    @AutoConfigureAfter(ServletWebServerFactoryAutoConfiguration.class)
    public class DispatcherServletAutoConfiguration {
  
    @Configuration(proxyBeanMethods = false)
  	@Conditional(DefaultDispatcherServletCondition.class)
  	@ConditionalOnClass(ServletRegistration.class)
    // 作用是将配置类加载到IOC容器中，与@Component类似，案例可见chapter4-ServerProperties.java
  	@EnableConfigurationProperties({ HttpProperties.class, WebMvcProperties.class })
  	protected static class DispatcherServletConfiguration {
  
        // 构造DispatcherServlet
  		@Bean(name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
  		public DispatcherServlet dispatcherServlet(HttpProperties httpProperties, WebMvcProperties webMvcProperties) {
  			DispatcherServlet dispatcherServlet = new DispatcherServlet();
  			dispatcherServlet.setDispatchOptionsRequest(webMvcProperties.isDispatchOptionsRequest());
  			dispatcherServlet.setDispatchTraceRequest(webMvcProperties.isDispatchTraceRequest());
  			dispatcherServlet.setThrowExceptionIfNoHandlerFound(webMvcProperties.isThrowExceptionIfNoHandlerFound());
  			dispatcherServlet.setPublishEvents(webMvcProperties.isPublishRequestHandledEvents());
  			dispatcherServlet.setEnableLoggingRequestDetails(httpProperties.isLogRequestDetails());
  			return dispatcherServlet;
  		}
  
        // 注册文件上传组件
  		@Bean
  		@ConditionalOnBean(MultipartResolver.class)
  		@ConditionalOnMissingBean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
  		public MultipartResolver multipartResolver(MultipartResolver resolver) {
  			// Detect if the user has created a MultipartResolver but named it incorrectly
  			return resolver;
  		}
  	  }
  
    @Configuration(proxyBeanMethods = false)
    @Conditional(DispatcherServletRegistrationCondition.class)
    @ConditionalOnClass(ServletRegistration.class)
    @EnableConfigurationProperties(WebMvcProperties.class)
    @Import(DispatcherServletConfiguration.class)
    protected static class DispatcherServletRegistrationConfiguration {

        @Bean(name = DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
        @ConditionalOnBean(value = DispatcherServlet.class, name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
        public DispatcherServletRegistrationBean dispatcherServletRegistration(DispatcherServlet dispatcherServlet,
                WebMvcProperties webMvcProperties, ObjectProvider<MultipartConfigElement> multipartConfig) {
            DispatcherServletRegistrationBean registration = new DispatcherServletRegistrationBean(dispatcherServlet,
                    webMvcProperties.getServlet().getPath());
            registration.setName(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
            registration.setLoadOnStartup(webMvcProperties.getServlet().getLoadOnStartup());
            multipartConfig.ifAvailable(registration::setMultipartConfig);
            return registration;
        }

      }
    }
  ```
  > 1. `DispatcherServletAutoConfiguration`执行顺序在`ServletWebServerFactoryAutoConfiguration`之后，核心是`DispatcherServletConfiguration`注册配置类
  > 2. @EnableConfigurationProperties的作用与@Component类似，就是把@ConfigurationProperties修饰的注解类加载到IOC容器中。
  > 3. 注册`DispatcherServlet`&`DispatcherServletRegistrationBean`到容器中。
  
  - SpringBoot注册传统Servlet三大组件
  
    a. web.xml
      在spring中注册servlet相关的组件采用的是web.xml
    
    b. @ServletComponentScan
      在SpringBoot启动类上标注@ServletComponentScan，并指定package。然后注册Filter、Servlet和Listener组件的时候，在对应的类上添加`@WebServlet`即可。
    
    c. RegistrationBean
    ```java
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
    ```
  

- ServletWebServerFactoryAutoConfiguration
  
  `该自动配置类的主要作用就是根据classpath加载web容器，且自动注入application.yml中或者自定义的配置。`
  ```java
    @Configuration(proxyBeanMethods = false)
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnClass(ServletRequest.class)
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @EnableConfigurationProperties(ServerProperties.class)
    @Import({ ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar.class,
            ServletWebServerFactoryConfiguration.EmbeddedTomcat.class,
            ServletWebServerFactoryConfiguration.EmbeddedJetty.class,
            ServletWebServerFactoryConfiguration.EmbeddedUndertow.class })
    public class ServletWebServerFactoryAutoConfiguration {
    }
  ```
  > 关键在于Import的组件：BeanPostProcessorsRegistrar; EmbeddedTomcat; EmbeddedJetty和EmbeddedUndertow
  
  - EmbeddedTomcat
  
  ```java
    @Configuration(proxyBeanMethods = false)
    class ServletWebServerFactoryConfiguration {
    
        @Configuration(proxyBeanMethods = false)
        // 只有在当前Classpath下存在Tomcat类，EmbeddedTomcat才会生效
        @ConditionalOnClass({ Servlet.class, Tomcat.class, UpgradeProtocol.class })
        @ConditionalOnMissingBean(value = ServletWebServerFactory.class, search = SearchStrategy.CURRENT)
        public static class EmbeddedTomcat {
    
            @Bean
            public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
                return new TomcatServletWebServerFactory();
            }
        }
    }
  ```
  > 上述代码是抽象后的代码，只是为了体现这里的`EmbeddedTomcat`只是为了提供`TomcatServletWebServerFactory`实例。
  
  - BeanPostProcessorsRegistrar
  
  ```java
    public static class BeanPostProcessorsRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware {
        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                BeanDefinitionRegistry registry) {
            if (this.beanFactory == null) {
                return;
            }
            registerSyntheticBeanIfMissing(registry, "webServerFactoryCustomizerBeanPostProcessor",
                    WebServerFactoryCustomizerBeanPostProcessor.class);
            registerSyntheticBeanIfMissing(registry, "errorPageRegistrarBeanPostProcessor",
                    ErrorPageRegistrarBeanPostProcessor.class);
        }
    }
  ```
  > 1. 通过实现`ImportBeanDefinitionRegistrar`来将`webServerFactoryCustomizerBeanPostProcessor`和`errorPageRegistrarBeanPostProcessor`注册到IOC容器中
  > 2. 其作用分别是：Bean工厂中的所有 WebServerFactoryCustomizer 类型的Bean应用于 WebServerFactory 类型的Bean(或者理解成将自定义的配置应用到WebServerFactory)、将Bean工厂中的所有 ErrorPageRegistrars 应用于 ErrorPageRegistry 类型的Bean。
  
  - SpringBoot中的Customizer(定制器)
  
  `一般我们修改SpringBoot的配置都是用过修改配置文件application.yml来实现，但是也可以通过实现WebServerFactoryCustomizer接口，传入ServletWebServerFactory类型来实现`
  
  ```java
  @Component
  public class WebMvcCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
  
      @Override
      public void customize(TomcatServletWebServerFactory factory) {
          System.out.println("change properties by WebServerFactoryCustomizer");
          factory.setPort(8888);
          factory.setContextPath("/springboot");
      }
  }
  ``` 
  > 该实现类是由`webServerFactoryCustomizerBeanPostProcessor`的`postProcessBeforeInitialization`方法调用
  
  - ServletWebServerFactoryAutoConfiguration中注册的其他组件
  
  ```java
    public class ServletWebServerFactoryAutoConfiguration {
    
        @Bean
        public ServletWebServerFactoryCustomizer servletWebServerFactoryCustomizer(ServerProperties serverProperties) {
            return new ServletWebServerFactoryCustomizer(serverProperties);
        }
    
        @Bean
        @ConditionalOnClass(name = "org.apache.catalina.startup.Tomcat")
        public TomcatServletWebServerFactoryCustomizer tomcatServletWebServerFactoryCustomizer(
                ServerProperties serverProperties) {
            return new TomcatServletWebServerFactoryCustomizer(serverProperties);
        }
    
        @Bean
        @ConditionalOnMissingFilterBean(ForwardedHeaderFilter.class)
        @ConditionalOnProperty(value = "server.forward-headers-strategy", havingValue = "framework")
        public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
            ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
            FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>(filter);
            registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return registration;
        }
    }
  ```
  > servletWebServerFactoryCustomizer & tomcatServletWebServerFactoryCustomizer方法作用与上面的定制器相同，传入`ServerProperties`做自动化配置
  
  - ServerProperties
  `其作用就是一个配置类，在application.yml文件中server开头的配置都会被@ConfigurationProperties注入`
  
  ```java
    @ConfigurationProperties(prefix = "server", ignoreUnknownFields = true)
    public class ServerProperties {
    
        /**
         * Server HTTP port.
         */
        private Integer port;
        ...
    }
  ```
  > 如果定制器与application.yml都修改了指定的参数(比如server.port)，那么port是以定制器为准。
  
---