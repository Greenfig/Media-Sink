package com.headcrab.media_sink.media_sink;

import android.content.ContentResolver;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

//new
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Collections;
import java.util.ArrayList;
import android.widget.ListView;
import android.net.Uri;
import android.database.Cursor;

import javax.xml.datatype.Duration;


public class MainActivity extends ActionBarActivity {

    //array store of songs
    private ArrayList<Song> songList;
    //song View
    private ListView songView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    //-----helper
    //---Get media and iterate through them
    public void getSongList(){
        //get song info
        ContentResolver musicResolver = getContentResolver();
        //Uri musicUri = Uri.parse(Environment.getExternalStorageDirectory().toString() + "/Media-Sink/Music/Sheepy&Proximity_Music");
        String[] musicUri = new String[]{"%Media-Sink/Music/Sheepy&Proximity_Music%"};
        Cursor musicCursor = musicResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + " like ? ",
                musicUri, null);

        //Iterate
        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songLColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do{
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisLength = setDuration(musicCursor.getString(songLColumn));

                songList.add(new Song(thisId,thisTitle,thisArtist,thisLength));
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
}
