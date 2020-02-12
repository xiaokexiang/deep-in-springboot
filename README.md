## SpringBoot 源码学习

### SprintBootApplication
 `被@SprintBootApplication修饰的类是SpringBoot应用的启动引导类`

 它是一个组合注解，其中包括三个注解，分别是:
  
   `@SpringBootConfiguration`
   
   `@EnableAutoConfiguration`
   
   `@ComponentScan`

### ComponentScan
 `指定包扫描的根路径，用于扫描包及包下的组件`
 
 ```java
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
    @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
 ```
 其中`TypeExcludeFilter`和`AutoConfigurationExcludeFilter`都实现了`TypeFilter`接口。
 
 - TypeFilter
 
 ```java
@FunctionalInterface
public interface TypeFilter {
 boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
  			throws IOException;
}
 ```

核心方法是match，返回`true`表示过滤该组件，返回`false`表示不过滤该组件

 - TypeExcludeFilter
 `向IOC容器中注册一些自定义的组件过滤器，以在包扫描的过程中过滤它们，用于屏蔽主启动类`
 
 - AutoConfigurationExcludeFilter
 `在match方法中判断是否是一个配置类，是否是一个自动配置类，这里用于屏蔽自动配置类`

### SpringBootConfiguration
 `本质上@SpringBootConfiguration是被@Configuration修饰的注解`
 
 - @Configuration
 `被@Configuration修饰的类会被IOC认为是配置类，可以理解成一个@Configuration修饰的类相当于一个application.yml配置文件`

### EnableAutoConfiguration

#### SpringFramework手动装配

- @Component(模式注解: Spring2.5+)

  `只需要在类上添加 @Component 注解即可将类作为组件引入，缺点是无法装配jar包中的组件`
  
- Configuration&Bean(配置类: Spring3.0+)

  `使用 Configuration & Bean 可以装配jar中的组件，缺点是注册过多会导致维护不易`

- EnableXXX&Import(模块装配: Spring3.1+)
  
  `通过给配置类标注 @EnableXXX 注解，再在注解上标注 @Import 注解，即可完成组件装配的效果`
  
  ```java
   @Documented
   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.TYPE)
   @Import()
   public @interface EnableColor {
   }
  ```
    > 通过@Import注解，可以导入配置类、ImportSelector 的实现类、ImportBeanDefinitionRegistrar 的实现类或普通类。
  