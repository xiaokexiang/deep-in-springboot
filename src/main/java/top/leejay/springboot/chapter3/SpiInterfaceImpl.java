package top.leejay.springboot.chapter3;

/**
 * @author xiaokexiang
 * @since 2020/2/13
 */
public class SpiInterfaceImpl implements SpiInterface {
    @Override
    public void test() {
        System.out.println("SpiInterfaceImpl test run ...");
    }
}
