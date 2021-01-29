package com.bank.shellx.web;

import android.content.Context;
import android.content.res.AssetManager;


import com.bank.shellx.web.handler.InitDataHandler;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;
import com.yanzhenjie.andserver.website.AssetsWebsite;
import com.yanzhenjie.andserver.website.WebSite;


import java.util.concurrent.TimeUnit;

public class WebsiteServer {

    Context context;
    Server server;

    public WebsiteServer(Context ctx) {
        context = ctx;
    }

    public boolean start() {
        try {
            AssetManager assetsManager = context.getAssets();
            WebSite website = new AssetsWebsite(assetsManager, "/website");

            AndServer andserver = new AndServer.Build()
                    .port(8080)     //设置端口号
                    .timeout(10*1000)      //设置连接超时时间
                    .website(website)
                    .registerHandler("/init",new InitDataHandler())
                    .build();
            server = andserver.createServer();
            server.start();
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isRunning() {
        return server.isRunning();
    }

    public void stop() {
        if (server.isRunning()) {
            server.stop();
        }
    }
}