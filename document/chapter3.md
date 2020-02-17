## Spring & SpringBoot IOC

### ApplicationContext
`Spring的IOC容器最顶级的接口是BeanFactory，那为什么Spring推荐使用ApplicationContext作为IOC容器呢？因为ApplicationContext包含了BeanFactory的全部功能，同时扩展了容器的其他功能(比如事件、国际化等等)`

```java
    public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
            MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

    }
```
> ListableBeanFactory是BeanFactory的扩展，可以实现枚举所有Bean实例
> HierarchicalBeanFactory中的getParentBeanFactory()，是实现多层嵌套容器的支撑(Spring和SpringMvc就是父子容器)

- ConfigurableApplicationContext
  `ApplicationContext的子接口，是一个非常核心的接口，其中的refresh()方法会在容器启动刷新时起作用。`

- AbstractApplicationContext
  `ConfigurableApplicationContext的抽象继承，采用了模板设计方法，由子类来实现具体的抽象方法。`

- AnnotationConfigApplicationContext
  `是ApplicationContext的实现类，是使用注解配置来加载和初始化IOC容器的`
  
  <img alt="ApplicationContext结构图" src="http://image.leejay.top/image/20200217/C9Gd6gbKJOR4.png"/>

### Springboot对IOC容器的扩展

- WebServerApplicationContext
 `创建和管理嵌入式WebServer生命周期的应用程序上下文实现的接口。`

- ConfigurableWebServerApplicationContext
 `WebServerApplicationContext类的子接口`


### 拓展：BeanFactory和FactoryBean

- BeanFactory
  `BeanFactory是Spring的顶级IOC容器接口，ApplicationContext就是继承了BeanFactory接口且进行了功能扩展。`

- FactoryBean
  `FactoryBean<T>是泛型接口，这表明它是一个Bean，采用了工厂设计模式的Bean。若IOC容器中的一个Bean实现了FactoryBean<T>接口，那么通过IOC容器getBean()的时候返回的不是该Bean，而是该Bean中的getObject()方法返回的对象。`

  ```java
  @Configuration
  public class DemoFactoryBean implements FactoryBean<Demo> {
  
      String getName() {
          return DemoFactoryBean.class.getName();
      }
  
      private static List<Demo> demos = new ArrayList<>();
  
      static {
          demos.add(new Demo("zhangsan"));
          demos.add(new Demo("lisi"));
      }
  
      @Override
      public Demo getObject() {
          return demos.get(new Random().nextInt(2));
      }
  
      @Override
      public Class<?> getObjectType() {
          return Demo.class;
      }
  }
  
  class Demo {
      private String name;
  
      public Demo(String name) {
          this.name = name;
      }
  
      String getName() {
          return name;
      }
  }
  
  class DemoTest {
      public static void main(String[] args) {
          // 注册DemoFactoryBean到IOC容器中
          AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DemoFactoryBean.class);
          // 获取的是DemoFactoryBean.getObject()方法的返回值
          Demo demo = (Demo) context.getBean("demoFactoryBean");
          System.out.println(demo.getName());
  
          // 加上&， 则获取的是DemoFactoryBean对象本身
          DemoFactoryBean bean = (DemoFactoryBean) context.getBean("&demoFactoryBean");
          System.out.println(bean.getName());
      }
  }
  ```

