## SpringBoot 源码学习

### SprintBootApplication
 `被@SprintBootApplication修饰的类是SpringBoot应用的启动引导类`

 它是一个组合注解，其中包括三个注解，分别是:
  
   `@SpringBootConfiguration`
   
   `@EnableAutoConfiguration`
   
   `@ComponentScan`
   
---
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

---
### SpringBootConfiguration
 `本质上@SpringBootConfiguration是被@Configuration修饰的注解`
 
 - @Configuration
 `被@Configuration修饰的类会被IOC认为是配置类，可以理解成一个@Configuration修饰的类相当于一个application.yml配置文件`

---
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

#### 自动装配

 `@EnableAutoConfiguration是一个组合注解，其中包括@AutoConfigurationPackage和@@Import(AutoConfigurationImportSelector.class)`
 
 - @AutoConfigurationPackage
   
   `关键在于@Import(AutoConfigurationPackages.Registrar.class)，注解所在的类的包应在AutoConfigurationPackages中注册,
   或者理解成将主配置所在的根包进行保存用于以后扫描使用`
   
   ```java
   
   static class Registrar implements ImportBeanDefinitionRegistrar, DeterminableImports {
      @Override
      public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        // 向IOC容器手动注册组件，并将著启动类的包名设置到basePackage中
        register(registry, new PackageImport(metadata).getPackageName());
      }

      @Override
      public Set<Object> determineImports(AnnotationMetadata metadata) {
        return Collections.singleton(new PackageImport(metadata));
      }
   }
   
   private static final class PackageImport {
   
      private final String packageName;

      PackageImport(AnnotationMetadata metadata) {
        // 通过metadata 获取注解所在的包名，也就是主启动类的包名
        this.packageName = ClassUtils.getPackageName(metadata.getClassName());
      }

      String getPackageName() {
        return this.packageName;
      }
   }
   ```

 - AutoConfigurationImportSelector
   与手动装配`@Import()`传入`ImportSelector`的实现类类似，`AutoConfigurationImportSelector`实现了`DeferredImportSelector`,
   而`DeferredImportSelector`继承了`ImportSelector`。
   
   > DeferredImportSelector 处理自动配置。如果需要自定义扩展 @EnableAutoConfiguration，则也可以编写该类的子类。它的执行时机晚于`ImportSelector`
   
   ```java
   public class AutoConfigurationImportSelector implements DeferredImportSelector {
              
     @Override
     public String[] selectImports(AnnotationMetadata annotationMetadata) {
        if (!isEnabled(annotationMetadata)) {
          return NO_IMPORTS;
        }
        // 获取自动配置类的元数据信息
        AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader
            .loadMetadata(this.beanClassLoader);
        // 关键在于将自动配置的元数据转成指定的Entry
        AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(autoConfigurationMetadata,
            annotationMetadata);
        // 返回自动配置类集合用于后面加载到容器中
        return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
     }
   
     protected AutoConfigurationEntry getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata,
                    AnnotationMetadata annotationMetadata) {
          if (!isEnabled(annotationMetadata)) {
            return EMPTY_ENTRY;
          }
          AnnotationAttributes attributes = getAttributes(annotationMetadata);
          // 将自动配置元数据转成List<String>
          List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
          configurations = removeDuplicates(configurations);
          // 获取需要排除的configurations
          Set<String> exclusions = getExclusions(annotationMetadata, attributes);
          checkExcludedClasses(configurations, exclusions);
          configurations.removeAll(exclusions);
          // 对自动配置类和自动配置元数据进行比较过滤，取相同的key的集合，用于后面加载到IOC容器中
          configurations = filter(configurations, autoConfigurationMetadata);
          fireAutoConfigurationImportEvents(configurations, exclusions);
          return new AutoConfigurationEntry(configurations, exclusions);
     }
     
     protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
        // 通过EnableAutoConfiguration.class获取META-INF下的spring.factories的资源(自动配置类的全限定名)
        // 这里运用了类似JDK的SPI技术
        List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),
                getBeanClassLoader());
        Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/top.leejay.springboot.chapter3.SpiInterface. If you "
                + "are using a custom packaging, make sure that file is correct.");
        return configurations;
     }   
   }
   ```
 ---
 
 ### Java SPI
 `SPI全称为 Service Provider Interface，是jdk内置的一种服务提供发现机制。SPI规定，所有要预先声明的类都应该放在 META-INF/services 中。
 配置的文件名是接口/抽象类的全限定名，文件内容是抽象类的子类或接口的实现类的全限定类名，如果有多个，借助换行符，一行一个`
 
 - 在`src/main/resources`下新建文件夹`META-INF/services`，并新建文件名为: `top.leejay.springboot.chapter3.SpiInterface`，该文件的
 值为该接口实现类的全路径: top.leejay.springboot.chapter3.SpiInterfaceImpl
 
 - 新建接口实现类`top.leejay.springboot.chapter3.SpiInterfaceImpl`实现`SpiInterface`接口并实现方法
 
 - 新建测试类
 ```java
 public class SpiInterfaceTest {
     public static void main(String[] args) {
         ServiceLoader<SpiInterface> spiInterfaces = ServiceLoader.load(SpiInterface.class);
         spiInterfaces.forEach(System.out::println);
     }
 }
 ```
  > Spring中的SPI使用的是`SpringFactoriesLoader.loadFactories()`方法，但是更灵活不需要将文件名固定死