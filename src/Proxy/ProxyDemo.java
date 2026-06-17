package Proxy;

public class ProxyDemo {
    public static void main(String[] args) {
        Image image = new ProxyImage("test.jpg");

        System.out.println("第一次显示：");
        image.display();

        System.out.println("\n第二次显示：");
        image.display();
    }
}
