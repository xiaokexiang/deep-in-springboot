package top.leejay.springboot.chapter2;

import org.springframework.context.annotation.Import;
import top.leejay.springboot.chapter2.classes.Blue;
import top.leejay.springboot.chapter2.classes.Red;

import java.lang.annotation.*;

/**
 * @author xiaokexiang
 * @see org.springframework.context.annotation.Import
 * @since 2020/2/12
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({Red.class, YellowConfiguration.class, BlueSelector.class, GreenBeanDefinitionRegistrar.class})
public @interface EnableColor {
}
