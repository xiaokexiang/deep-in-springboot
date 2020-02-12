package top.leejay.springboot.chapter1;

/**
 * @author xiaokexiang
 * @since 2020/2/12
 * SpringBoot 程序启动入口:
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * 它是组合注解其中包括三个注解:
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.context.annotation.ComponentScan
 *
 * a. @ComponentScan
 *   ``` java
 *   @ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
 *                @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
 *   ```
 *   指定包扫描的路径，让Spring扫描指定的包及包下的组件
 */
public class SpringBootApplication {
}
