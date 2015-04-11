package com.headcrab.media_sink.media_sink;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.headcrab.media_sink.media_sink.MusicService.MusicBinder;
import android.widget.MediaController.MediaPlayerControl;

//new
import java.util.Comparator;
import java.util.Collections;
import java.util.ArrayList;

import android.view.View;
import android.widget.ListView;
import android.database.Cursor;


public class MusicList extends Activity implements MediaPlayerControl {

    //array store of songs
    private ArrayList<Song> songList;
    //song View
    private ListView songView;

    private MusicService musicServ;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;

    private  boolean paused = false, playbackPaused = false;

    //-------------------------service
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicServ = binder.getService();
            //pass list
            musicServ.populateSongList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name){
            musicBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        //--------
        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<>();

        getSongList();


        //--------Sort songs alphabetically
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdapter = new SongAdapter(this, songList);
        songView.setAdapter(songAdapter);

        setController();
    }

    @Override
    public void onStart(){
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //--------
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_music_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicServ.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicServ = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop(){
        controller.hide();
        super.onStop();
    }

    @Override
    public void onDestroy(){
        stopService(playIntent);
        musicServ=null;
        super.onDestroy();
    }

    public void songPicked(View view){
        musicServ.setSong(Integer.parseInt(view.getTag().toString()));
        musicServ.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    //-----helper
    //---Get media and iterate through them
    public void getSongList(){
        //get song info
        ContentResolver musicResolver = getContentResolver();
        //get album art
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        byte[] rawArt;
        BitmapFactory.Options bfo = new BitmapFactory.Options();

        String[] musicUri = new String[]{"%Media-Sink/Music%"};
        Cursor musicCursor = musicResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + " like ? ",
                musicUri, null);


        //Iterate
        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songLColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            long albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            do{
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisLength = setDuration(musicCursor.getString(songLColumn));
                String thisAlbum = musicCursor.getString(albumColumn);
                Long thisAlbumId = albumIdColumn;
                String path = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                Uri songPath = Uri.parse(path);
                Bitmap thisAlbumArt = null;

                metadataRetriever.setDataSource(getApplicationContext(), songPath);
                rawArt = metadataRetriever.getEmbeddedPicture();
                try{
                    thisAlbumArt = BitmapFactory.decodeByteArray(rawArt, 0 , rawArt.length,bfo);
                }catch( NullPointerException ex){
                    thisAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.play);
                }

                songList.add(new Song(thisId,thisTitle,thisArtist,thisLength, thisAlbumArt));
            }while(musicCursor.moveToNext());
        }
        musicCursor.close();
    }

    //----set duration to minutes:seconds
    public String setDuration(String duration){

        int seconds = Integer.valueOf(duration) / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        if(seconds<10){
            return String.valueOf(minutes) + ":0" + String.valueOf(seconds);
        }
        else{
            return String.valueOf(minutes) + ":" + String.valueOf(seconds);
        }

    }

    /*
     *MEDIA PLAYER CONTROLLER METHODS
     */

    @Override
    public void start() {
        musicServ.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicServ.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicServ != null && musicBound && musicServ.isPng()){
            return musicServ.getDur();
        }
        else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if(musicServ != null && musicBound && musicServ.isPng()){
            return musicServ.getPosn();
        }
        else{
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicServ.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicServ != null && musicBound){
            return musicServ.isPng();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void setController(){
        //setup controller
        //initialize controller
        controller = new MusicController(this);

        //set user control options
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener(){
            @Override
            public void onClick(View v){
                playPrevious();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    private void playNext(){
        musicServ.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    private void playPrevious(){
        musicServ.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }
}
