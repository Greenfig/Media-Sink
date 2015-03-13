package com.headcrab.media_sink.media_sink;

/**
 * Created by Rene on 2015-03-12.
 */
public class Song {
    private long id;
    private String title;
    private String artist;
    //private String songLength;

    public Song(long songId, String songTitle, String songArtist){
        id = songId;
        title = songTitle;
        artist = songArtist;
    }

    public long getId(){return id;}
    public String getTitle() {return title;}
    public String getArtist() {return artist;}

    //public String getSongLength() {return songLength;}
}
