package Prototype;

public class Shape implements Cloneable {
    private String type;

    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return this.type;
    }

    public void draw(){
        System.out.println("绘制一个" + type);
    }

    @Override
    public Shape clone(){
        try {
            return (Shape) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
