package com.bank.shellx.utils.regex;

import android.util.ArrayMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//解析字符串的方法集合类
public class StringParse {

    //解析自定义adb命令的带元参数的命令
    public static Map metaCommand(String command){
        Map<String,String> parsedString = new LinkedHashMap<>();
        String[] firstParsedString = command.split("[{}]+?");
        if (firstParsedString.length == 0){
            return parsedString;
        }
        //在每项里匹配，看看是不是元参数
        Pattern regex = Pattern.compile("^.+?:(key|pkg|file)$");
        Matcher matcher;
        for (String item:firstParsedString){
            matcher = regex.matcher(item);
            //判断是否为元数据，是，放入
            if (matcher.find()){
                String[] metaArray = item.split(":");
                parsedString.put(metaArray[0],metaArray[1]);
                //不是，放入
            }else{
                parsedString.put(String.valueOf(item.hashCode()),item);
            }
        }
        return parsedString;
    }
}
