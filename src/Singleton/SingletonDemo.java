package Singleton;

public class SingletonDemo {
    public static void main(String[] args) {
        President p1 = President.getInstance();
        p1.setName("张三");
        p1.showInfo();

        President p2 = President.getInstance();
        p2.showInfo();

        System.out.println("是否为同一对象：" + (p1 == p2));
    }
}
