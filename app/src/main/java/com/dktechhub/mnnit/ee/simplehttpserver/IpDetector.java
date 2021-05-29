package com.dktechhub.mnnit.ee.simplehttpserver;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class IpDetector {

    WifiManager wifiManager;
    OnIpGet onIpGet;
    // WifiObserverInterface wifiObserverInterface;
    Context context;

    public IpDetector(@NotNull Context context,OnIpGet onIpGet) {
        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.onIpGet=onIpGet;
        getLocalIP();
        getIPAddress(true);
    }
    public String getIpfromInt(int ipA)
    {

        String ip = String.format("%d.%d.%d.%d", (ipA & 0xff), (ipA >> 8 & 0xff), (ipA >> 16 & 0xff), (ipA >> 24 & 0xff));
        return ip;
    }

    public void getLocalIP() {
        String localIP = "";
        if (wifiManager.isWifiEnabled()) {
            if (wifiManager.getConnectionInfo().getSupplicantState() == SupplicantState.COMPLETED) {
                onIpGet.localIp(getIpfromInt(wifiManager.getDhcpInfo().ipAddress),true);
            } else {
                onIpGet.localIp("Not connected",false);
            }
        } else if (isApOn()) {
            try {
                Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                        .getNetworkInterfaces();
                while (enumNetworkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = enumNetworkInterfaces
                            .nextElement();
                    Enumeration<InetAddress> enumInetAddress = networkInterface
                            .getInetAddresses();
                    while (enumInetAddress.hasMoreElements()) {
                        InetAddress inetAddress = enumInetAddress.nextElement();

                        if (inetAddress.isSiteLocalAddress()) {
                            localIP += inetAddress.getHostAddress();
                            Log.d("MWebHotspot", localIP);
                            if (inetAddress.getHostAddress().contains("192.168")) {
                                onIpGet.localIp(inetAddress.getHostAddress(),true);
                            }
                        }
                    }
                }

            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                onIpGet.localIp("Not connected",false);
                //onIpGet.globalIp("Not connected");
            }


        } else {
            //wifiObserverInterface.onNoConnectionDetected();
            onIpGet.localIp("Not connected",false);
        }
    }

    public void getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        if(sAddr.contains("192.168"))
                            continue;
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                            {onIpGet.globalIp(sAddr,true);
                                return;}
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                onIpGet.globalIp( delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase(),true);
                                return;

                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        } // for now eat exceptions
        onIpGet.globalIp("not connected",false);
    }

    public boolean isWifiOn() {
        return wifiManager.isWifiEnabled();
    }

    public boolean isApOn() {

        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Throwable ignored) {
        }
        return false;

    }
    public interface OnIpGet{
        void localIp(String s,boolean isConnected);
        void globalIp(String s,boolean isConnected);
    }
    }
