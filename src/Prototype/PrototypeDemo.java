package Prototype;

public class PrototypeDemo {
    public static void main(String[] args) {
        Circle circle = new Circle();
        circle.draw();

        Circle circle2 = circle.clone();
        circle2.draw();

        Rectangle rect = new Rectangle();
        rect.draw();

        Rectangle rect2 = rect.clone();
        rect2.draw();
    }
}
