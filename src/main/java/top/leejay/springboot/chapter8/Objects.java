package top.leejay.springboot.chapter8;

/**
 * @author Li Jie
 * @date 3/13/2020
 */
public class Objects extends SubObjects {

    static {
        System.out.println("i'm objects ... ");
    }

    private int i;

    public Objects(int i) {
        System.out.println("Objects init ... ");
        this.i = i;
    }

    public static void main(String[] args) {
        Objects objects = null;
        objects = new Objects(1);
        System.out.println(objects.i);
    }
}

class SubObjects {

    static String name = "hello";

    static {
        System.out.println(name);
        System.out.println("i'm subObjects ... ");
    }

    SubObjects() {
        System.out.println("SubObjects init ...");
    }
}
