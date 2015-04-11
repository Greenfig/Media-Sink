package com.headcrab.media_sink.media_sink;


import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Xml;
import android.widget.ImageView;

import javax.xml.datatype.Duration;


/**
 * Created by Rene on 2015-03-12.
 */
public class Song {
    private long id;
    private String title;
    private String artist;
    private String songLength;
    private Bitmap albumArt;

    public Song(long songId, String songTitle, String songArtist, String songL, Bitmap albumA){
        id = songId;
        title = songTitle;
        artist = songArtist;
        songLength = songL;
        albumArt = albumA;
    }

    public long getId(){return id;}
    public String getTitle() {return title;}
    public String getArtist() {return artist;}
    public String getSongLength() {return songLength;}
    public Bitmap getAlbumArt() {return albumArt;}
}
