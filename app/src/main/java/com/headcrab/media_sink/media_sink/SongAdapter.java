package com.headcrab.media_sink.media_sink;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;



/**
 * Created by Rene on 2015-03-12.
 */
public class SongAdapter extends BaseAdapter{

    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    private Context context;
    private DBAdapter db;

    public SongAdapter(Context c, ArrayList<Song> theSongs, DBAdapter d){
        songs = theSongs;
        context = c;
        songInf = LayoutInflater.from(c);
        db = d;
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

       /* Bitmap thisAlbumArt = null;

        //get album art
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        byte[] rawArt;
        BitmapFactory.Options bfo = new BitmapFactory.Options();

        metadataRetriever.setDataSource(songInf.getContext(), currentSong.getSongPath());
        rawArt = metadataRetriever.getEmbeddedPicture();
        try {
            thisAlbumArt = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
        } catch (NullPointerException ex) {
            thisAlbumArt = null;
        }*/
        new getImage(songInf.getContext(),currentSong.getSongPath(),imageView).execute();
        //imageView.setImageBitmap(thisAlbumArt);
        songView.setText(currentSong.getTitle());
        artistView.setText(currentSong.getArtist());
        songL.setText(String.valueOf(currentSong.getSongLength()));

        songLayout.setTag(position);
        return songLayout;
    }

    public class getImage extends AsyncTask<Object,Void,Bitmap>{
        private Context context;
        private ImageView imv;
        private Uri path;

        public getImage(Context c, Uri p, ImageView i){
            context = c;
            path = p;
            imv = i;
        }

        @Override
        protected Bitmap doInBackground(Object... params){
            Bitmap thisAlbumArt = null;

            //get album art
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            byte[] rawArt;
            BitmapFactory.Options bfo = new BitmapFactory.Options();
            bfo.inPreferredConfig = Bitmap.Config.RGB_565;
            bfo.inSampleSize = 4;

            metadataRetriever.setDataSource(context, path);
            rawArt = metadataRetriever.getEmbeddedPicture();
            try {
                thisAlbumArt = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
                thisAlbumArt.createScaledBitmap(thisAlbumArt,30,30,true);
            } catch (NullPointerException ex) {
                thisAlbumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder);
                thisAlbumArt.createScaledBitmap(thisAlbumArt,30,30,true);
            }


            return thisAlbumArt;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            super.onPostExecute(result);

            if(imv.getTag()!=null && !imv.getTag().toString().equals(path)){
                Log.i("Completed","Image loaded");
                return;
            }

            if(result != null && imv != null){
                imv.setVisibility(View.VISIBLE);
                imv.setImageBitmap(result);
            }
            else{
                imv.setVisibility(View.GONE);
            }
            Log.i("Completed","Image loaded");
        }

    }
}
