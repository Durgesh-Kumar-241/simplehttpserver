package com.dktechhub.mnnit.ee.simplehttpserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class RunningService extends Service {
    private static final String CHANNEL_ID = "apache2";
    private static final String ACTION_STOP_SERVICE = "stop_my_service";
    // private final IBinder localBinder = new ServiceBinder();
    private ServiceInterface serviceInterface;
    HTTPServer httpServer;
    private final int notificationId=12345;

    public RunningService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(this.serviceInterface!=null)
        {
            serviceInterface.onServerStarted();
        }
        httpServer=new HTTPServer(getApplicationContext(), RunningService.this::log);
        httpServer.startServing();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            //Log.d(TAG,"called to cancel service");
            //manager.cancel(NOTIFCATION_ID);
            stopSelf();
        }
        createNotificationChannel();
        Intent intent2 = new Intent(this, MainActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent2, 0);

        Intent stopSelf = new Intent(this, RunningService.class);
        stopSelf.setAction(ACTION_STOP_SERVICE);
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf,0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_public_24)
                .setContentTitle("Apache2 server")
                .setContentText("HTTP Server running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_baseline_stop_circle_24, "Stop", pStopSelf)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent);

// notificationId is a unique int for each notification that you must define

        Notification notification = builder.build();
       // notificationManager.notify(notificationId, notification);

        startForeground(1, notification);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(this.serviceInterface!=null)
        {
            serviceInterface.onServerStopped();
        }httpServer.stopServing();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    public interface ServiceInterface{
        void onServerStarted();
        void onServerStopped();
        //void log(String s);
    }

    public void log(String s)
    {
        Intent local=new Intent();
        local.setAction("com.dktechhub.mnnit.ee.simplehttpserver.uiupdater");
        local.putExtra("log",s);
        this.sendBroadcast(local);
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}