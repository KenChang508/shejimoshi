package Adapter;

public class MediaAdapter implements MediaPlayer {
    AdvancedMediaPlayer advancedPlayer;

    public MediaAdapter(String audioType){
        advancedPlayer = new AdvancedMediaPlayer();
    }

    @Override
    public void play(String audioType, String fileName){
        if (audioType.equals("vlc")){
            advancedPlayer.playVlc(fileName);
        } else if (audioType.equals("mp4")){
            advancedPlayer.playMp4(fileName);
        }
    }
}