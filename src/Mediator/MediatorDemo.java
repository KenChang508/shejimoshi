package Mediator;

public class MediatorDemo {
    public static void main(String[] args) {
        ChatMediator mediator = new ChatMediatorImpl();

        User user1 = new ConcreteUser(mediator, "张三");
        User user2 = new ConcreteUser(mediator, "李四");
        User user3 = new ConcreteUser(mediator, "王五");

        mediator.addUser(user1);
        mediator.addUser(user2);
        mediator.addUser(user3);

        user1.send("大家好！");
    }
}
