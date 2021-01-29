package com.bank.shellx.web.jsonUtils;

import java.util.List;

public class ListToJson {

    public static String toText(List<String> list) {
        StringBuilder lastResult = new StringBuilder();
        StringBuilder sb;
        for (String content : list) {
            int i = list.indexOf(content);
            sb = new StringBuilder();
            sb.append(i).append(".").append(content).append("\n");
            lastResult.append(sb);
        }
        lastResult.append("(请回复数字序号！)");
        return lastResult.toString();
    }
}
