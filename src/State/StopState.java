package State;

import javax.naming.Context;

public class StopState implements State {
    @Override
    public void doAction(Context context){
        System.out.println("玩家停止游戏");
        context.setState(this);
    }

    public String toString(){
        return "停止状态";
    }
}
