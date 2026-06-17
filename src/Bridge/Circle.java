package Bridge;

public class Circle extends Shape {
    public Circle(Color color){
        super(color);
    }

    @Override
    public void draw(){
        System.out.print("绘制");
        color.applyColor();
        System.out.println("的圆形");
    }
}
