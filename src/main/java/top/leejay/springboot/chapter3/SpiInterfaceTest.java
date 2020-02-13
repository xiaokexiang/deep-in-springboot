package top.leejay.springboot.chapter3;

import java.util.ServiceLoader;

/**
 * @author xiaokexiang
 * @since 2020/2/13
 * SPI测试类
 */
public class SpiInterfaceTest {
    public static void main(String[] args) {
        ServiceLoader<SpiInterface> spiInterfaces = ServiceLoader.load(SpiInterface.class);
        spiInterfaces.forEach(System.out::println);
    }
}
