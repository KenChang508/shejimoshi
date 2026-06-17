package Flyweight;

public class FlyweightDemo {
    public static void main(String[] args) {
        Shape circle1 = ShapeFactory.getCircle("红色");
        circle1.draw(10, 20);

        Shape circle2 = ShapeFactory.getCircle("红色");
        circle2.draw(30, 40);

        Shape circle3 = ShapeFactory.getCircle("蓝色");
        circle3.draw(50, 60);

        System.out.println("circle1和circle2是否相同：" + (circle1 == circle2));
    }
}
