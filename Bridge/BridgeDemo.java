package Bridge;

public class BridgeDemo {
    public static void main(String[] args) {
        Shape redCircle = new Circle(new Red());
        redCircle.draw();

        Shape blueSquare = new Square(new Blue());
        blueSquare.draw();
    }
}
