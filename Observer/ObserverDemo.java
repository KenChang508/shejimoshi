package Observer;

public class ObserverDemo {
    public static void main(String[] args) {
        Subject subject = new Subject();

        new BinaryObserver(subject);
        new OctalObserver(subject);
        new HexObserver(subject);

        System.out.println("设置状态为15：");
        subject.setState(15);

        System.out.println("\n设置状态为10：");
        subject.setState(10);
    }
}
