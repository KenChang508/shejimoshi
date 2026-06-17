package Template;

public class Football extends Game {
    @Override
    void initialize(){
        System.out.println("足球游戏初始化");
    }

    @Override
    void startPlay(){
        System.out.println("开始足球比赛");
    }

    @Override
    void endPlay(){
        System.out.println("足球比赛结束");
    }
}
