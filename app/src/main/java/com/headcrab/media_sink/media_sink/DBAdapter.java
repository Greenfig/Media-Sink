package com.headcrab.media_sink.media_sink;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Rene on 11/04/2015.
 */
public class DBAdapter {

    static final String _ID = "_id";
    static final String SONG_ID = "songID";
    static final String SONG_NAME = "title";
    static final String SONG_ARTIST = "artist";
    static final String SONG_DUR = "songLength";
    static final String SONG_PATH = "songPath";

    static final String DATABASE_NAME = "MediaSink";
    static final String DATABASE_TABLE = "Music";
    static final int DATABASE_VERSION = 1;

    static final String DATABASE_CREATE =
            "create table Music (_id integer primary key autoincrement, songID text, title text not null, artist text, songLength text, songPath text not null)";

    final Context context;


    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public DBAdapter(Context cont){
        this.context = cont;
        DBHelper = new DatabaseHelper(context);
    }

    public static class DatabaseHelper extends SQLiteOpenHelper{

        DatabaseHelper(Context context){
            super(context, DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS Music");
            onCreate(db);
        }
    }

    //---open db
    public DBAdapter open() throws SQLException{
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //close db
    public void close(){
        DBHelper.close();
    }

    //insert a new song into the db
    public void insertSong(long id,String title,String artist,String songLength,Uri songPath){
        ContentValues initialValues = new ContentValues();
        initialValues.put(SONG_ID, String.valueOf(id));
        initialValues.put(SONG_NAME, title);
        initialValues.put(SONG_ARTIST, artist);
        initialValues.put(SONG_DUR, songLength);
        initialValues.put(SONG_PATH, songPath.toString());
        db.insert(DATABASE_TABLE, null, initialValues);
    }

    public Song getSongData(int id){
        Cursor _song = db.query(DATABASE_TABLE, new String[] { SONG_ID, SONG_NAME, SONG_ARTIST, SONG_DUR, SONG_PATH }, _ID + "=" + id,null,null,null,null,null);
        Song temp = null;
        if(_song!=null && _song.getCount()>0){
            _song.moveToFirst();
            long Sid = _song.getLong(_song.getColumnIndex("songID"));
            String SName = _song.getString(_song.getColumnIndex("title"));
            String SArtist = _song.getString(_song.getColumnIndex("artist"));
            String SDur = _song.getString(_song.getColumnIndex("songLength"));
            Uri SPath = Uri.parse(_song.getString(_song.getColumnIndex("songPath")));
            temp = new Song(Sid,SName,SArtist,SDur,SPath);
            return temp;
        }
        return temp;
        //return null;
    }

    //get All songs
    public ArrayList<Song> getAllSongs(){
        ArrayList<Song> sonL = new ArrayList<>();
        Cursor _song = db.query(DATABASE_TABLE, new String[] { SONG_ID, SONG_NAME, SONG_ARTIST, SONG_DUR, SONG_PATH }, null,null,null,null, SONG_NAME,null);
        if(_song!=null){
            _song.moveToFirst();
            do {
                long Sid = _song.getLong(_song.getColumnIndex("songID"));
                String SName = _song.getString(_song.getColumnIndex("title")).trim();
                String SArtist = _song.getString(_song.getColumnIndex("artist")).trim();
                String SDur = _song.getString(_song.getColumnIndex("songLength")).trim();
                Uri SPath = Uri.parse(_song.getString(_song.getColumnIndex("songPath")));
                Song temp = new Song(Sid, SName, SArtist, SDur, SPath);
                sonL.add(temp);
            }while(_song.moveToNext());
            _song.close();
        }
        return sonL;
    }

    //get song count
    public int songCount(){
        Cursor temp = db.rawQuery("SELECT COUNT(*) FROM Music",null);
        return temp.getCount();
    }

    /*
        Convert bitmap to string
        By: Shyildo
        FROM: http://stackoverflow.com/questions/13562429/how-many-ways-to-convert-bitmap-to-string-and-vice-versa
     */
    public static byte[] BitToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    /*
        Convert String to Bitmap
     */
    public static Bitmap StringToBit(byte[] image){
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    //check
    public boolean hasTable(){
        Cursor temp;
        temp = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table' AND name='Music'", null);
        if(temp.getCount()>0){
            temp.close();
            return true;
        }
        else{
            temp.close();
            return false;
        }
    }

    //Drop table
    public void dropTable(){
        db.execSQL("DROP TABLE IF EXISTS Music");
    }

    //Recreate Table
    public void createTable(){
        db.execSQL(DATABASE_CREATE);
    }

}
