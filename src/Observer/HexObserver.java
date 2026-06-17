package Observer;

public class HexObserver extends Observer {
    public HexObserver(Subject subject){
        this.subject = subject;
        this.subject.attach(this);
    }

    @Override
    public void update(){
        System.out.println("十六进制数：" + Integer.toHexString(subject.getState()).toUpperCase());
    }
}
