package top.leejay.springboot.chapter2;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import top.leejay.springboot.chapter2.classes.Blue;

/**
 * @author xiaokexiang
 * @since 2020/2/12
 * 3. 实现ImportSelector接口，需要注意的是： BlueSelector并不会被加载入IOC
 * top.leejay.springboot.chapter2.classes.Blue
 */
public class BlueSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{Blue.class.getName()};
    }
}
