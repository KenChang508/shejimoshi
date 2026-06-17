package ChainOfResponsibility;

public class DebugLogger extends Logger {
    public DebugLogger(int level){
        this.level = level;
    }

    @Override
    protected void write(String message){
        System.out.println("DEBUG: " + message);
    }
}
