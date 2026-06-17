package Visitor;

public class VisitorDemo {
    public static void main(String[] args) {
        ItemElement[] items = {
                new Book(50.0, "Java编程"),
                new Book(30.0, "设计模式"),
                new Fruit(10.0, "苹果"),
                new Fruit(15.0, "香蕉")
        };

        Visitor visitor = new ShoppingCartVisitor();
        for (ItemElement item : items){
            item.accept(visitor);
        }
    }
}
