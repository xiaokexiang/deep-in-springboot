## IOC Refresh

`至此IOC容器已经准备就绪且主启动类已经加载到IOC容器中，refresh是IOC容器最核心的方法。`

###  refreshContext

```java
private void refreshContext(ConfigurableApplicationContext context) {
    // 刷新上下文
    refresh(context);
    // 注册关闭程序的钩子函数
    if (this.registerShutdownHook) {
        try {
            context.registerShutdownHook();
        }
        catch (AccessControlException ex) {
            // Not allowed in some environments.
        }
    }
}

protected void refresh(ApplicationContext applicationContext) {
    // 判断传入的applicationContext是否是AbstractApplicationContext的实例
    Assert.isInstanceOf(AbstractApplicationContext.class, applicationContext);
    // 强转成AbstractApplicationContext并调用
    ((AbstractApplicationContext) applicationContext).refresh();
}
```

> `registerShutdownHook`方法向JVM注册了一个钩子，除非JVM当时已关闭，否则再JVM关闭时关闭上下文。

### refresh(核心)

```java
public void refresh() throws BeansException, IllegalStateException {
    synchronized (this.startupShutdownMonitor) {
        // 准备刷新上下文
        prepareRefresh();

        // 调用父类刷新beanfactory
        ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

        // 准备beanfacotry在上下文中的使用
        prepareBeanFactory(beanFactory);

        try {
            // Allows post-processing of the bean factory in context subclasses.
            postProcessBeanFactory(beanFactory);

            // Invoke factory processors registered as beans in the context.
            invokeBeanFactoryPostProcessors(beanFactory);

            // Register bean processors that intercept bean creation.
            registerBeanPostProcessors(beanFactory);

            // Initialize message source for this context.
            initMessageSource();

            // Initialize event multicaster for this context.
            initApplicationEventMulticaster();

            // Initialize other special beans in specific context subclasses.
            onRefresh();

            // Check for listener beans and register them.
            registerListeners();

            // Instantiate all remaining (non-lazy-init) singletons.
            finishBeanFactoryInitialization(beanFactory);

            // Last step: publish corresponding event.
            finishRefresh();
        }

        catch (BeansException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception encountered during context initialization - " +
                            "cancelling refresh attempt: " + ex);
            }

            // Destroy already created singletons to avoid dangling resources.
            destroyBeans();

            // Reset 'active' flag.
            cancelRefresh(ex);

            // Propagate exception to caller.
            throw ex;
        }

        finally {
            // Reset common introspection caches in Spring's core, since we
            // might not ever need metadata for singleton beans anymore...
            resetCommonCaches();
        }
    }
}
```

- <b>prepareRefresh</b>

  ```java
  // 初始化前的预处理
  protected void prepareRefresh() {
      // Switch to active.
      // 记录启动时间
      this.startupDate = System.currentTimeMillis();
      // 设置IOC容器的关闭状态为false
      this.closed.set(false);
      // 设置IOC容器已激活
      this.active.set(true);
  
      if (logger.isDebugEnabled()) {
          if (logger.isTraceEnabled()) {
              logger.trace("Refreshing " + this);
          }
          else {
              logger.debug("Refreshing " + getDisplayName());
          }
      }
  
      // Initialize any placeholder property sources in the context environment.
      // 初始化属性配置
      initPropertySources();
  
      // 校验必要参数是否为空
      getEnvironment().validateRequiredProperties();
  
      // 存储预刷新的监听器
      if (this.earlyApplicationListeners == null) {
          this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
      }
      else {
          // Reset local application listeners to pre-refresh state.
          this.applicationListeners.clear();
          this.applicationListeners.addAll(this.earlyApplicationListeners);
      }
  
     // 存储早期的应用事件 合适的时候进行广播
      this.earlyApplicationEvents = new LinkedHashSet<>();
  }
  ```

  - <b>initPropertySources</b>

    ```java
    // 模板方法 由子类实现
    protected void initPropertySources() {
        // For subclasses: do nothing by default.
    }
    
    // 子类：GenericWebApplicationContext 实现
    @Override
    protected void initPropertySources() {
        ConfigurableEnvironment env = getEnvironment();
        if (env instanceof ConfigurableWebEnvironment) {
            ((ConfigurableWebEnvironment) env).initPropertySources(this.servletContext, null);
        }
    }
    
    // 调用ConfigurableWebEnvironment initPropertySources接口实现
    public interface ConfigurableWebEnvironment extends ConfigurableEnvironment {
      void initPropertySources(@Nullable ServletContext servletContext, 
                               @Nullable ServletConfig servletConfig);
    }
    
    // 默认接口实现
    @Override
    public void initPropertySources(@Nullable ServletContext servletContext, 
                                    @Nullable ServletConfig servletConfig) {
        WebApplicationContextUtils.initServletPropertySources(
            getPropertySources(), servletContext, servletConfig);
    }
    
    
    // 将基于Servlet的存根属性源替换为使用给定 ServletContext 和 ServletConfig 对象填充的实际实例。
    // 可以理解中的数据写入IOC容器中
    public static final String SERVLET_CONTEXT_PROPERTY_SOURCE_NAME = "servletContextInitParams";
    public static final String SERVLET_CONFIG_PROPERTY_SOURCE_NAME = "servletConfigInitParams"
    public static void initServletPropertySources(MutablePropertySources sources,
    			@Nullable ServletContext servletContext, @Nullable ServletConfig servletConfig) {
    
        Assert.notNull(sources, "'propertySources' must not be null");
        String name = StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME;
        if (servletContext != null && sources.contains(name) && sources.get(name) instanceof StubPropertySource) {
            sources.replace(name, new ServletContextPropertySource(name, servletContext));
        }
        name = StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME;
        if (servletConfig != null && sources.contains(name) && sources.get(name) instanceof StubPropertySource) {
            sources.replace(name, new ServletConfigPropertySource(name, servletConfig));
        }
    }
    ```

  - <b>validateRequiredProperties</b>

    ```java
    // 获取当前环境 没有就创建一个StandardEnviroment
    @Override
    public ConfigurableEnvironment getEnvironment() {
        if (this.environment == null) {
            this.environment = createEnvironment();
        }
        return this.environment;
    }
    
    // 校验一些属性是否为空，空就抛出异常
    @Override
    public void validateRequiredProperties() {
        MissingRequiredPropertiesException ex = new MissingRequiredPropertiesException();
        for (String key : this.requiredProperties) {
            if (this.getProperty(key) == null) {
                ex.addMissingRequiredProperty(key);
            }
        }
        if (!ex.getMissingRequiredProperties().isEmpty()) {
            throw ex;
        }
    }
    ```

- <b>obtainFreshBeanFactory</b>

  ```java
  protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
      refreshBeanFactory();
      return getBeanFactory();
  }
  
  // 抽象方法 子类实现
  protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;
  
  // GenericApplicationContext 实现
  @Override
  protected final void refreshBeanFactory() throws IllegalStateException {
      // CAS判断refresh标识是否设置过，设置过抛异常
      if (!this.refreshed.compareAndSet(false, true)) {
          throw new IllegalStateException(
              "GenericApplicationContext does not support multiple refresh attempts: just call 'refresh' once");
      }
      // 设置beanfactory的序列化id
      this.beanFactory.setSerializationId(getId());
  }
  
  ```

  

- <b>prepareBeanFactory</b>

  ```java
  // beanfactory预处理
  protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
      // 设置classLoader
      beanFactory.setBeanClassLoader(getClassLoader());
      // 设置表达式解析器
      beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(
          beanFactory.getBeanClassLoader()));
      // 设置参数编辑注册器
      beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));
  
      // Configure the bean factory with context callbacks.
      // 配置beanfactory的后置回调处理器
      beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
      // 忽略指定的interface 或 自动注入类
      beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
      beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
      beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
      beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
      beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
      beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
      
      // BeanFactory接口没在普通工厂中注册为可解析类型。
      // 处理autowire bean自动注入
      beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
      beanFactory.registerResolvableDependency(ResourceLoader.class, this);
      beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
      beanFactory.registerResolvableDependency(ApplicationContext.class, this);
  
      // 配置一个可加载所有监听器的组件
      beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));
  
      // Detect a LoadTimeWeaver and prepare for weaving, if found.
      if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
          beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
          // Set a temporary ClassLoader for type matching.
          beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
      }
  
      // 注册了默认的运行时环境、系统配置属性、系统环境的信息
      if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
          beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
      }
      if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
          beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
      }
      if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
          beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
      }
  }
  ```

  - <b>BeanPostProcessor</b>

    `Bean的后置处理器，可以在对象实例化后初始化之前以及初始化之后进行一些后置处理`

    ```java
    // 定义cat类
    public class Cat {
        private String name;
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    
        public Cat(String name) {
            this.name = name;
        }
    }
    
    // 将cat注入IOC容器中
    @Configuration
    @ComponentScan("top.leejay.springboot.chapter9")
    public class CatConfiguration {
    
        @Bean
        public Cat cat() {
            // 创建一个名为cat的bean注入到IOC容器中，其name为a little cat
            return new Cat("a little cat");
        }
    }
    
    // 编写Cat的BeanPostProcessor
    @Component
    public class CatBeanPostProcessor implements BeanPostProcessor {
    
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) 
            throws BeansException {
            // 在每个bean初始化前调用
            return bean;
        }
    
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) 
            throws BeansException {
            // 在每个bean初始化后调用
            if (bean instanceof Cat) {
                Cat cat = (Cat)bean;
                cat.setName("dog");
            }
            return bean;
        }
    }
    ```

  - <b>BeanPostProcessor的执行时机</b>

    `包含@Bean(initMethoy)、Servlet规范：@PostConstruct以及InitializingBean接口的执行顺序。`

    ```java
    
    ```

    

- 

​     