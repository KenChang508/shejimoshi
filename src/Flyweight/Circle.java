package Flyweight;

public class Circle implements Shape {
    private String color;

    public Circle(String color){
        this.color = color;
        System.out.println("创建一个" + color + "的圆形");
    }

    @Override
    public void draw(int x, int y){
        System.out.println("在(" + x + "," + y + ")绘制一个" + color + "的圆形");
    }
}
