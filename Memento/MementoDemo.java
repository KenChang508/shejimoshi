package Memento;

public class MementoDemo {
    public static void main(String[] args) {
        Originator originator = new Originator();
        CareTaker careTaker = new CareTaker();

        originator.setState("状态1");
        careTaker.add(originator.saveStateToMemento());

        originator.setState("状态2");
        careTaker.add(originator.saveStateToMemento());

        originator.setState("状态3");
        System.out.println("当前状态：" + originator.getState());

        originator.getStateFromMemento(careTaker.get(0));
        System.out.println("恢复到第一个状态：" + originator.getState());

        originator.getStateFromMemento(careTaker.get(1));
        System.out.println("恢复到第二个状态：" + originator.getState());
    }
}
