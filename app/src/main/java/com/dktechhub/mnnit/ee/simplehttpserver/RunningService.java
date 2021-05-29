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

public class RunningService extends Service {
   // private final IBinder localBinder = new ServiceBinder();
    private ServiceInterface serviceInterface;
    HTTPServer httpServer;
    public RunningService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(this.serviceInterface!=null)
        {
            serviceInterface.onServerStarted();
        }
        httpServer=new HTTPServer(getApplicationContext(), new HTTPServer.HTTPInterface() {
            @Override
            public void log(String s) {
                RunningService.this.log(s);
            }

            @Override
            public void notifyOnff(int port, boolean on) {
                RunningService.this.notifyOnOf(port,on);
            }


        });
        httpServer.startServing();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        Intent notificationIntent = new Intent(this, MainActivity2.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                .setContentTitle("Http server running")
                .setContentText(input)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager  mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel( MyApplication.CHANNEL_ID, MyApplication.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID);
        }
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

    public void setServiceInterface(ServiceInterface serviceInterface)
    {
        this.serviceInterface=serviceInterface;
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

    public void notifyOnOf(int port,boolean on)
    {
        Intent local=new Intent();
        local.setAction("com.dktechhub.mnnit.ee.simplehttpserver.onoffmanager");
        local.putExtra("on",true);
        local.putExtra("port",port);
        this.sendBroadcast(local);
    }
}