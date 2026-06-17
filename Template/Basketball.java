package Template;

public class Basketball extends Game {
    @Override
    void initialize(){
        System.out.println("篮球游戏初始化");
    }

    @Override
    void startPlay(){
        System.out.println("开始篮球比赛");
    }

    @Override
    void endPlay(){
        System.out.println("篮球比赛结束");
    }
}
