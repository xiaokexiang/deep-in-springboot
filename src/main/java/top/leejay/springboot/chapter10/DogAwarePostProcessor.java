package top.leejay.springboot.chapter10;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author Li Jie
 * @date 3/22/2020
 * Bean的实例化后置处理器
 */
@Component
public class DogAwarePostProcessor implements InstantiationAwareBeanPostProcessor {

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        if (beanClass.equals(Dog.class)) {
            System.out.println("Dog before Instantiation ...");
        }
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        if (bean instanceof Dog) {
            System.out.println("Dog after Instantiation ...");
        }
        return true;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        if (bean instanceof Dog) {
            System.out.println("Dog postProcessProperties Instantiation ...");
        }
        return null;
    }
}
