package com.headcrab.media_sink.media_sink;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Rene on 2015-03-11.
 */
public class MediaPlayer extends Service {

    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return 0;
    }

}
