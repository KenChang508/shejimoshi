package Decorator;

public class DecoratorDemo {
    public static void main(String[] args) {
        Coffee coffee = new SimpleCoffee();
        System.out.println(coffee.description() + "，价格：" + coffee.cost());

        coffee = new MilkDecorator(coffee);
        System.out.println(coffee.description() + "，价格：" + coffee.cost());

        coffee = new SugarDecorator(coffee);
        System.out.println(coffee.description() + "，价格：" + coffee.cost());
    }
}
