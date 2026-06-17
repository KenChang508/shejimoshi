package Observer;

public class OctalObserver extends Observer {
    public OctalObserver(Subject subject){
        this.subject = subject;
        this.subject.attach(this);
    }

    @Override
    public void update(){
        System.out.println("八进制数：" + Integer.toOctalString(subject.getState()));
    }
}
