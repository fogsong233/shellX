package com.bank.shellx.application;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.EditText;

import com.bank.shellx.web.WebsiteServer;
import com.bank.shellx.web.webSocket.ShellWebSocket;

import java.net.InetSocketAddress;


public class MyApplication extends Application {

    private static Context ctx;
    private static EditText editText;
    public static boolean hasNetWork = false;

    @Override
    public void onCreate() {
        super.onCreate();
        GlobalException handler = GlobalException.getInstance();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        ctx = getApplicationContext();
        ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("commandResult","."));
        new WebsiteServer(MyApplication.getContext()).start();
        new Thread() {
            @Override
            public void run() {
                new ShellWebSocket(new InetSocketAddress("localhost",3000)).run();
            }
        }.start();


    }

    public static Context getContext() {
        return ctx;
    }

    public static EditText getEditText(){
        return editText;
    }

    public static void setEditText(EditText e){
        editText = e;
    }

}
