package Adapter;

public class AdapterDemo {
    public static void main(String[] args) {
        AudioPlayer player = new AudioPlayer();
        player.play("mp3", "歌曲.mp3");
        player.play("vlc", "电影.vlc");
        player.play("mp4", "视频.mp4");
    }
}