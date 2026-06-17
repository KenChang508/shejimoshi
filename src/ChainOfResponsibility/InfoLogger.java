package ChainOfResponsibility;

public class InfoLogger extends Logger {
    public InfoLogger(int level){
        this.level = level;
    }

    @Override
    protected void write(String message){
        System.out.println("INFO: " + message);
    }
}
