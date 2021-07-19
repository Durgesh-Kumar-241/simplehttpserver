package com.dktechhub.mnnit.ee.simplehttpserver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogsFragment extends Fragment implements View.OnClickListener {

    private TextView logger;
    private TextView localIp, globalIp;
    private DateFormat simpleDateFormat;

    IpDetector ipDetector;
    int port=2004;

    @SuppressLint("SimpleDateFormat")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_logs, container, false);
        logger = root.findViewById(R.id.logger);
        localIp = root.findViewById(R.id.localip);
        globalIp = root.findViewById(R.id.globalip);
        ipDetector=new IpDetector(getContext(), new IpDetector.OnIpGet() {
            @Override
            public void localIp(String s, boolean isConnected) {
                if(isConnected)
                    localIp.setText("http://" + s + ":" + port);
                else localIp.setText("Not connected");
            }

            @Override
            public void globalIp(String s, boolean isConnected) {
                if(isConnected)
                    globalIp.setText("http://" + s + ":" + port);
                else globalIp.setText("Not connected");
            }
        });
        localIp.setOnClickListener(this);
        globalIp.setOnClickListener(this);
        this.simpleDateFormat = new SimpleDateFormat("HH:mm:ss ");
        setupViewsWithurl();
        return root;
    }





    public void log(String text)
            {   try {
               // this.logger.append('\n'+this.simpleDateFormat.format(new Date())+'\n' +"------\n"+ text + "\n------\n");
                this.logger.append(String.format("\n%s\n------\n%s\n------\n",this.simpleDateFormat.format(new Date()),text));
            }catch (Exception ignored)
            {

            }
    }


    public void setupViewsWithurl()
    {
        SharedPreferences sharedPreference= PreferenceManager.getDefaultSharedPreferences(this.getContext());
        int port=Integer.parseInt(sharedPreference.getString("port", String.valueOf(2004)));
        this.port=port;
        ipDetector.update();
    }

    @Override
    public void onClick(View v) {
        try{
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("http://localhost:"+this.port));
            startActivity(Intent.createChooser(intent,"Open browser"));
        }catch (Exception e)
        {
            Toast.makeText(LogsFragment.this.getContext(), "failed", Toast.LENGTH_SHORT).show();
        }
    }
}