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

/**
 * Created by Rene on 2015-03-19.
 */
public class MusicService extends Service implements
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener{

    //MediaPlayer
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosition;
    private final IBinder musicBind = new MusicBinder();


    public void onCreate(){
        super.onCreate();

        songPosition = 0;
        player = new MediaPlayer();

        //initialize player
        initializePlayer();
    }

    public void playSong(){
        //play a song
        player.reset();

        //get song Uri
        Song playSong = songs.get(songPosition);
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

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }
}
