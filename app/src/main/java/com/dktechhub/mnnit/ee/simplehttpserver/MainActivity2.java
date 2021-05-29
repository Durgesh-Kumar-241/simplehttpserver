package com.dktechhub.mnnit.ee.simplehttpserver;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.dktechhub.mnnit.ee.simplehttpserver.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.net.URL;

public class MainActivity2 extends AppCompatActivity {
    Button actionButton, openBrowser;
    //HTTPServerService httpServerService;
    SettingsFragment settingsFragment = new SettingsFragment();
    LogsFragment logsFragment = new LogsFragment();

    BroadcastReceiver broadcastReceiver;
    BroadcastReceiver onOfManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        setContentView(R.layout.activity_main2);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        //sectionsPagerAdapter.addFragment(homeFragment,"Home");
        sectionsPagerAdapter.addFragment(logsFragment, "Home");
        sectionsPagerAdapter.addFragment(settingsFragment, "Options");

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);




        IntentFilter filter = new IntentFilter();
        filter.addAction("com.dktechhub.mnnit.ee.simplehttpserver.uiupdater");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                logsFragment.log(intent.getStringExtra("log"));
            }
        };
        registerReceiver(broadcastReceiver, filter);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("com.dktechhub.mnnit.ee.simplehttpserver.onoffmanager");
        onOfManager = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean on = intent.getBooleanExtra("on", false);
                if (on) {
                    logsFragment.onServerStarted(intent.getIntExtra("port", 0));
                }


            }
        };
        registerReceiver(onOfManager, filter2);


        actionButton = findViewById(R.id.start);
        openBrowser = findViewById(R.id.open_browser);
        openBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(new URL(logsFragment.getLocal()))));
                    startActivity(Intent.createChooser(intent, "Open browser"));
                } catch (Exception e) {
                    Toast.makeText(MainActivity2.this, "failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (isMyServiceRunning()) {
            actionButton.setText("Stop");
            // bindService(new Intent(this,RunningService.class),downloadServiceConnection,BIND_IMPORTANT);
        } else {
            actionButton.setText("Start");

        }

        actionButton.setOnClickListener(v -> OnButtonClicked());

    }


    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RunningService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public void applyTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean dark_theme = sharedPreferences.getBoolean("dark_theme", false);
        if (dark_theme) setTheme(R.style.ThemeOverlay_MaterialComponents_Dark);
        else setTheme(R.style.Theme_MaterialComponents_Light);
    }

    public void startService() {

        Intent serviceIntent = new Intent(this, RunningService.class);
        serviceIntent.putExtra("inputExtra", "Running server in background");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, RunningService.class);
        stopService(serviceIntent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(onOfManager);
    }

    public void toggleService()
    {
        if (isMyServiceRunning()) {
            stopService();

            actionButton.setText("Start");
            //unbindService(downloadServiceConnection);
        } else {
            startService();
            // bindService(new Intent(this,RunningService.class),downloadServiceConnection,BIND_IMPORTANT);
            actionButton.setText("stop");
        }
    }

    public void OnButtonClicked()
    {
        if(permissionGranted())
        {
            toggleService();
        }else
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},200);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==200) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toggleService();
            }
            else Toast.makeText(this, "External storage permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean permissionGranted() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }



}