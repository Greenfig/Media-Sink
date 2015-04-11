package com.headcrab.media_sink.media_sink;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;



/**
 * Created by Rene on 2015-03-12.
 */
public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs = theSongs;
        songInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        RelativeLayout songLayout = (RelativeLayout) songInf.inflate(R.layout.song, parent, false);

        ImageView imageView = (ImageView) songLayout.findViewById(R.id.song_image);
        TextView songView = (TextView) songLayout.findViewById(R.id.song_title);
        TextView artistView = (TextView) songLayout.findViewById(R.id.song_artist);
        TextView songL = (TextView) songLayout.findViewById(R.id.song_length);

        Song currentSong = songs.get(position);

        imageView.setImageBitmap(currentSong.getAlbumArt());
        songView.setText(currentSong.getTitle());
        artistView.setText(currentSong.getArtist());
        songL.setText(String.valueOf(currentSong.getSongLength()));

        songLayout.setTag(position);
        return songLayout;
    }
}
