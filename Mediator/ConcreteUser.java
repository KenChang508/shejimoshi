package Mediator;

public class ConcreteUser extends User {
    public ConcreteUser(ChatMediator mediator, String name){
        super(mediator, name);
    }

    @Override
    public void send(String msg){
        System.out.println(name + "发送消息：" + msg);
        mediator.sendMessage(msg, this);
    }

    @Override
    public void receive(String msg){
        System.out.println(name + "收到消息：" + msg);
    }
}
