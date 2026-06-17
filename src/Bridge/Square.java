package Bridge;

public class Square extends Shape {
    public Square(Color color){
        super(color);
    }

    @Override
    public void draw(){
        System.out.print("绘制");
        color.applyColor();
        System.out.println("的正方形");
    }
}
