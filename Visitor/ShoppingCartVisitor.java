package Visitor;

public class ShoppingCartVisitor implements Visitor {
    @Override
    public void visit(Book book){
        System.out.println("书籍：" + book.getName() + "，价格：" + book.getPrice());
    }

    @Override
    public void visit(Fruit fruit){
        System.out.println("水果：" + fruit.getName() + "，价格：" + fruit.getPrice());
    }
}
