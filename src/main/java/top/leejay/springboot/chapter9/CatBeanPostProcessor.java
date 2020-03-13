package top.leejay.springboot.chapter9;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author Li Jie
 * @date 3/13/2020
 * 自定义Bean后置处理器
 */
@Component
public class CatBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 在每个bean初始化前调用
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 在每个bean初始化后调用
        if (bean instanceof Cat) {
            Cat cat = (Cat)bean;
            cat.setName("dog");
        }
        return bean;
    }
}
