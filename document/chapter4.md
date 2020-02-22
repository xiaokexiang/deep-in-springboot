## SpringBoot准备IOC容器

### 准备流程

#### main方法入口 

```java
@SpringBootApplication
public class SpringbootApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringbootApplication.class, args);
	}
}
```

#### SpringApplication.run方法

```java
public class SpringApplication {
   public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
   	  return run(new Class<?>[] { primarySource }, args);
   }
   // 这里返回的是ConfigurableApplicationContext(继承了ApplicationContext)
   public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
      // 调用SpringApplication构造函数创建SpringApplication对象并调用run()方法
      return new SpringApplication(primarySources).run(args);
   }
}
```

#### 创建SpringApplication

```java
public class SpringApplication {

    public SpringApplication(Class<?>... primarySources) {
   		this(null, primarySources);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        // 此时resourceLoader 为null
        this.resourceLoader = resourceLoader;
        // 断言primarySources是否为null
        Assert.notNull(primarySources, "PrimarySources must not be null");
        // 将primarySources转成LinkedHashSet<Class<?>>集合
        this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
        // 设置WebApplicationType 
        this.webApplicationType = WebApplicationType.deduceFromClasspath();
        // 设置初始化器
        setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
        // 设置监听器
        setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
        // 设置主配置类
        this.mainApplicationClass = deduceMainApplicationClass();
    }
}
```
> 上述代码是创建新的SpringApplication对象，我们可以在调用run()方法之前自定义实例，就是手动配置SpringApplication的属性

#### 自定义SpringApplication

```java
@SpringBootApplication
public class SpringbootApplication {
	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(SpringbootApplication.class);
		// 关闭banner
		springApplication.setBannerMode(Banner.Mode.OFF);
		// 设置webApplicationType
		springApplication.setWebApplicationType(WebApplicationType.SERVLET);
		// 等同于 SpringApplication.run(SpringbootApplication.class, args);
		springApplication.run(args);
	}
}
```

---

### 创建SpringApplication流程分析

#### WebApplicationType.deduceFromClasspath()

```java
public enum WebApplicationType {

	NONE,
	SERVLET,
	REACTIVE;

	private static final String[] SERVLET_INDICATOR_CLASSES = { "javax.servlet.Servlet",
			"org.springframework.web.context.ConfigurableWebApplicationContext" };

	private static final String WEBMVC_INDICATOR_CLASS = "org.springframework.web.servlet.DispatcherServlet";

	private static final String WEBFLUX_INDICATOR_CLASS = "org.springframework.web.reactive.DispatcherHandler";

	private static final String JERSEY_INDICATOR_CLASS = "org.glassfish.jersey.servlet.ServletContainer";

	private static final String SERVLET_APPLICATION_CONTEXT_CLASS = "org.springframework.web.context.WebApplicationContext";

	private static final String REACTIVE_APPLICATION_CONTEXT_CLASS = "org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext";

	static WebApplicationType deduceFromClasspath() {
        // 当前classpath 存在指定的类来判断当前Springboot用哪种环境启动
        // ClassUtils.isPresent()就是通过是是否能创建传入的类来判断当前classpath是否存在该类
        // 符合该条件说明是REACTIVE模式
		if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) && !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
				&& !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
			return WebApplicationType.REACTIVE;
		}
        // 如果SERVLET_INDICATOR_CLASSES有一个不存在就设置成NONE
		for (String className : SERVLET_INDICATOR_CLASSES) {
			if (!ClassUtils.isPresent(className, null)) {
				return WebApplicationType.NONE;
			}
		}
        // 前面条件都不符合设置成SERVLET
		return WebApplicationType.SERVLET;
	}
}
```

#### setInitializers()

`设置初始化器`

- ApplicationContextInitializer

  `用于在刷新容器之前初始化Spring ConfigurableApplicationContext 的回调接口。`

    - 使用方法一

	```java
	public class MyApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			System.out.println("ApplicationContextInitializer before refresh IOC ...");
		}
	}

	@SpringBootApplication
	public class SpringbootApplication {
		public static void main(String[] args) {
			SpringApplication springApplication = new SpringApplication(SpringbootApplication.class);
			// 添加ApplicationContextInitializer实现类到SpringApplication
			springApplication.addInitializers(new MyApplicationContextInitializer());
			springApplication.run(args);
		}
	}
	```

	- 使用方法二

	`src/main/resources/META-INF目录下创建spring.factories文件。`

	``` java
	// MyApplicationContextInitializer就是ApplicationContextInitializer接口的实现类
	org.springframework.context.ApplicationContextInitializer=top.leejay.springboot.chapter7.MyApplicationContextInitializer
	```

```java

public class SpringApplication {
    
    // setInitializers 传入的参数是ApplicationContextInitializer子类的集合,用于初始化IOC容器
    public void setInitializers(Collection<? extends ApplicationContextInitializer<?>> initializers) {
		this.initializers = new ArrayList<>(initializers);
	}
    
    private <T> Collection<T> getSpringFactoriesInstances(Class<T> type) {
   		return getSpringFactoriesInstances(type, new Class<?>[] {});
    }
   
    private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
   		ClassLoader classLoader = getClassLoader();
        //这里的SpringFactoriesLoader就是之前第一篇提到的Spring的SPI，作用是加载指定目录下的spring.factories文件里面的ApplicationContextInitializer配置
   		Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
		// 反射创建这些组件
   		List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
   		AnnotationAwareOrderComparator.sort(instances);
   		return instances;
    }
}

```

- SpringFactoriesLoader.loadFactoryNames()

  `SpringFactoriesLoader之前有解析过，就是会去加载Springboot和Springboot-autuconfigure jar下的spring.factories的文件。`

  ```java
  # springboot Application Context Initializers
  org.springframework.context.ApplicationContextInitializer=\

  # 报告IOC容器的一些常见错误配置
  org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer,\

  # 设置Spring应用上下文ID
  org.springframework.boot.context.ContextIdApplicationContextInitializer,\

  # 加载 application.properties 中 context.initializer.classes 配置的类
  org.springframework.boot.context.config.DelegatingApplicationContextInitializer,\
  org.springframework.boot.rsocket.context.RSocketPortInfoApplicationContextInitializer,\

  # 将内置servlet容器实际使用的监听端口写入到 Environment 环境属性中
  org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer

  # springboot auto-configure Initializers
  org.springframework.context.ApplicationContextInitializer=\

  # 创建一个 SpringBoot 和 ConfigurationClassPostProcessor 共用的 CachingMetadataReaderFactory 对象
  org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer,\

  # 将 ConditionEvaluationReport 写入日志
  org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener
  ```
  > 默认一共设置6个ApplicationContextInitializer的配置


#### setListeners()

`设置监听器`

- ApplicationListener
`由应用程序事件监听器实现的接口。基于观察者模式的标准 java.util.EventListener 接口。`

``` java
// 加载所有类型为ApplicationListener的已配置的组件的全限定类名
setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
```

- 加载的Listener

```java
# Application Listeners
org.springframework.context.ApplicationListener=\
# 应用上下文加载完成后对缓存做清除工作
org.springframework.boot.ClearCachesApplicationListener,\

# 监听双亲应用上下文的关闭事件并往自己的子应用上下文中传播
org.springframework.boot.builder.ParentContextCloserApplicationListener,\

# 检测系统文件编码与应用环境编码是否一致，如果系统文件编码和应用环境的编码不同则终止应用启动
org.springframework.boot.context.FileEncodingApplicationListener,\

# 根据 spring.output.ansi.enabled 参数配置 AnsiOutput
org.springframework.boot.context.config.AnsiOutputApplicationListener,\

# 从常见的那些约定的位置读取配置文件
org.springframework.boot.context.config.ConfigFileApplicationListener,\

# 监听到事件后转发给 application.properties 中配置的 context.listener.classes 的监听器
org.springframework.boot.context.config.DelegatingApplicationListener,\

# 对环境就绪事件 ApplicationEnvironmentPreparedEvent 和应用失败事件 ApplicationFailedEvent 做出响应
org.springframework.boot.context.logging.ClasspathLoggingApplicationListener,\

# 配置 LoggingSystem。使用 logging.config 环境变量指定的配置或者缺省配置
org.springframework.boot.context.logging.LoggingApplicationListener,\

# 使用一个可以和 SpringBoot 可执行jar包配合工作的版本替换 LiquibaseServiceLocator
org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener

# Application Listeners
org.springframework.context.ApplicationListener=\

# 使用一个后台线程尽早触发一些耗时的初始化任务
org.springframework.boot.autoconfigure.BackgroundPreinitializer
```

#### deduceMainApplicationClass()

`用于确定主配置类。`

``` java
public class SpringApplication {
	private Class<?> deduceMainApplicationClass() {
		try {
			StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
			for (StackTraceElement stackTraceElement : stackTrace) {
				// 从 deduceMainApplicationClass 方法开始往上爬，哪一层调用栈上有main方法，方法对应的类就是主配置类，就返回这个类
				if ("main".equals(stackTraceElement.getMethodName())) {
					return Class.forName(stackTraceElement.getClassName());
				}
			}
		}
		catch (ClassNotFoundException ex) {
			// Swallow and continue
		}
		return null;
	}
}
```
> 至此new SpringApplication(SpringBootApplication.class)初始化操作完成，下一篇是执行run()方法的代码解读。



