package com.bank.shellx.web.handler;


import com.bank.shellx.web.jsonUtils.ListToJson;
import com.yanzhenjie.andserver.RequestHandler;



import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InitDataHandler implements RequestHandler {

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws IOException {
        List<String> data = new ArrayList<>();
        data.add("新增自定义命令");
        data.add("删除自定义命令");
        data.add("在线shell命令行");
        response.setStatusCode(200);
        response.setEntity(new StringEntity(ListToJson.toText(data),"utf-8"));
    }
}
