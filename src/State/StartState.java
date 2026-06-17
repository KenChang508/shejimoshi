package State;

import javax.naming.Context;

public class StartState implements State {
    @Override
    public void doAction(Context context){
        System.out.println("玩家开始玩游戏");
        context.setState(this);
    }

    public String toString(){
        return "开始状态";
    }
}
