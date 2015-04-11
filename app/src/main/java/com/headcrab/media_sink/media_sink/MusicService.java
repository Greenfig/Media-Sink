package com.headcrab.media_sink.media_sink;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.media.MediaPlayer;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;

/**
 * Created by Rene on 2015-03-19.
 */
public class MusicService extends Service implements
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener{

    //MediaPlayer
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosition;
    private final IBinder musicBind = new MusicBinder();

    private String songTitle = "";
    private static final int NOTIFY_ID = 1;

    private boolean shuffle = false;
    private Random rand;


    public void onCreate(){
        super.onCreate();

        AudioManager requestAudioFocus;

        songPosition = 0;
        player = new MediaPlayer();

        //initialize player
        initializePlayer();
        rand = new Random();
    }

    public void playSong(){
        //play a song
        player.reset();

        //get song Uri
        Song playSong = songs.get(songPosition);
        //get title for notification
        songTitle = playSong.getTitle();
        //get id
        long currSong = playSong.getId();
        //set Uri
        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source",e);
        }

        player.prepareAsync();
    }

    public void setSong(int songIndex){
        songPosition = songIndex;
    }

    public void initializePlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void populateSongList(ArrayList<Song> songlist){
        songs = songlist;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    public class MusicBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public IBinder onBind(Intent intent){
        return musicBind;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition() == 0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        //Start playback
        Intent notIntent = new Intent(this, MusicList.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //pending intent takes the user back to the main activity class when the user selects the notification
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    @Override
    public void onDestroy(){
        stopForeground(true);
    }

    //Controller related methods
    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void playPrev(){
        songPosition--;
        if(songPosition == 0){
            songPosition = songs.size()-1;
        }
        playSong();
    }

    public void playNext(){
        //shuffle
        if(shuffle){
            int newSong = songPosition;
            while (newSong == songPosition){
                newSong = rand.nextInt(songs.size());
            }
            songPosition = newSong;
        }
        else {
            songPosition++;
            if (songPosition == songs.size()) {
                songPosition = 0;
            }
        }
        playSong();
    }

    public void setShuffle(){
        if(shuffle){
            shuffle = false;
        }
        else{
            shuffle = true;
        }
    }
}
