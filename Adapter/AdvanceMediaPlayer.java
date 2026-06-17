package Adapter;

public class AdvancedMediaPlayer {
    public void playVlc(String fileName){
        System.out.println("播放vlc文件：" + fileName);
    }

    public void playMp4(String fileName){
        System.out.println("播放mp4文件：" + fileName);
    }
}
