package top.leejay.springboot.chapter10;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Li Jie
 * @date 3/22/2020
 * Bean 生命周期：实例化instantiation -> 属性赋值populate -> 初始化initialization -> Bean销毁Destruction
 */
@Component
public class DogConfiguration {

    @Bean(initMethod = "initMethod", destroyMethod = "destroyMethod")
    public Dog dog() {
        return new Dog();
    }

}
