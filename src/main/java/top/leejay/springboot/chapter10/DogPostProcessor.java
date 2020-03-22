package top.leejay.springboot.chapter10;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author Li Jie
 * @date 3/22/2020
 */
@Component
public class DogPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Dog) {
            System.out.println("Dog postProcessBeforeInitialization ... ");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Dog) {
            System.out.println("Dog postProcessAfterInitialization ... ");
        }
        return bean;
    }
}
