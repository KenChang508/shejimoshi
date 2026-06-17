package State;

public class StateDemo {
    public static void main(String[] args) {
        Context context = new Context();

        StartState startState = new StartState();
        startState.doAction(context);
        System.out.println("当前状态：" + context.getState().toString());

        StopState stopState = new StopState();
        stopState.doAction(context);
        System.out.println("当前状态：" + context.getState().toString());
    }
}
