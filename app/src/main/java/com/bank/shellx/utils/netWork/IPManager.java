package com.bank.shellx.utils.netWork;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.bank.shellx.application.MyApplication;
import com.bank.shellx.utils.logUtils.LogUtil;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPManager { public static String getIPAddress(Context context) {
    NetworkInfo info = ((ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    if (info != null && info.isConnected()) {
        MyApplication.hasNetWork = true;
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
            try {
                //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }


        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
            return ipAddress;
        }else if(info.getType() == ConnectivityManager.TYPE_ETHERNET  ){

            //有线网络
            try {
                // 获取本地设备的所有网络接口
                Enumeration<NetworkInterface> enumerationNi = NetworkInterface
                        .getNetworkInterfaces();
                while (enumerationNi.hasMoreElements()) {
                    NetworkInterface networkInterface = enumerationNi.nextElement();
                    String interfaceName = networkInterface.getDisplayName();
                    LogUtil.i("tag", "网络名字" + interfaceName);

                    // 如果是有线网卡
                    if (interfaceName.equals("eth0")) {
                        Enumeration<InetAddress> enumIpAddr = networkInterface
                                .getInetAddresses();

                        while (enumIpAddr.hasMoreElements()) {
                            // 返回枚举集合中的下一个IP地址信息
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            // 不是回环地址，并且是ipv4的地址
                            if (!inetAddress.isLoopbackAddress()
                                    && inetAddress instanceof Inet4Address) {
                                LogUtil.i("tag", inetAddress.getHostAddress() + "   ");

                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    } else {
        //当前无网络连接,请在设置中打开网络
        MyApplication.hasNetWork = false;
    }
    return "暂无网络";
}


    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

}
