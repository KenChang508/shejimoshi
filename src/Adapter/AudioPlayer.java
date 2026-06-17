package Adapter;

public class AudioPlayer implements MediaPlayer {
    @Override
    public void play(String audioType, String fileName){
        if (audioType.equals("mp3")){
            System.out.println("播放mp3文件：" + fileName);
        } else {
            MediaAdapter adapter = new MediaAdapter(audioType);
            adapter.play(audioType, fileName);
        }
    }
}
