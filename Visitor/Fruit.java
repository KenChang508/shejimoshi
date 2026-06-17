package Visitor;

public class Fruit implements ItemElement {
    private double price;
    private String name;

    public Fruit(double price, String name){
        this.price = price;
        this.name = name;
    }

    public double getPrice(){
        return price;
    }

    public String getName(){
        return name;
    }

    @Override
    public int accept(Visitor visitor){
        visitor.visit(this);
        return (int) price;
    }
}
