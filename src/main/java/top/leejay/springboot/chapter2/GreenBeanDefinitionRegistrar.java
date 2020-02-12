package top.leejay.springboot.chapter2;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import top.leejay.springboot.chapter2.classes.Green;

/**
 * @author xiaokexiang
 * @since 2020/2/12
 * 实现ImportBeanDefinitionRegistrar接口
 */
public class GreenBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 此处green 为组件的name
        registry.registerBeanDefinition("green", new RootBeanDefinition(Green.class));
    }
}
