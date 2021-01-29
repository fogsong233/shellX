package com.bank.shellx.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bank.shellx.R;
import com.bank.shellx.application.MyApplication;
import com.bank.shellx.ui.customUtilsUi.PickPackageActivity;
import com.bank.shellx.utils.logUtils.LogUtil;
import com.bank.shellx.utils.regex.StringParse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//自定义命令的类
public class CustomAdbActivity extends BaseActivity {

    //命令的大概格式：
    //wm density {dpi:key}
    private String command;
    private Map<String,String> parsedString;
    private LinearLayout ll;
    private List<View> view = new ArrayList<>();
    MaterialButton submitButton;

    public static void start(String name,String command){
        Context c = MyApplication.getContext();
        Intent i = new Intent(c,CustomAdbActivity.class);
        i.putExtra("command",command);
        i.putExtra("name",name);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置初始view，并获取layout布局
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_adb);
        //获取命令
        Intent i = getIntent();
        command = i.getStringExtra("command");
        //获取这个自定义adb的名字
        String name = i.getStringExtra("name");
        //设置这个名字
        TextView tName = findViewById(R.id.adb_custom_name);
        tName.setText(name);
        //解析命令
        parsedString = StringParse.metaCommand(command);
        //如果没有参数直接执行
        if (parsedString.size() == 0){
            //准备直接执行
        }
        //获取layout
        ll = findViewById(R.id.custom_adb_layout);
        //获取提交button
        submitButton = findViewById(R.id.custom_adb_submit);
        //设置button提交方法
        submitButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StringBuilder resultCommand = new StringBuilder();
                        for (String s : parsedString.values()){
                            if (s.equals("key") || s.equals("file") || s.equals("pkg")){
                                continue;
                            }
                            //转义
                            switch (s){
                                case "!key":
                                    s = "key";
                                    break;
                                case "!file":
                                    s = "file";
                                    break;
                                case "!pkg":
                                    s = "pkg";
                                    break;
                                default:
                                    break;
                            }
                            resultCommand.append(s);
                        }
                        LogUtil.e("Az",resultCommand.toString());
                    }
                }
        );
        //构造控件
        buildParamsWidgets();
    }

    //根据值生成控件
    private void buildParamsWidgets(){
        for (Map.Entry<String,String> entry : parsedString.entrySet()){
            switch (entry.getValue()){
                case "key":
                    key(entry.getKey());
                case "file":
                    break;
                case "pkg":
                    pkg(entry.getKey());
                    break;
                default:
                    break;
            }
        }
    }

    //具体生成控件的方法

    //普通的key类型生成
    private void key(final String name){
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 10;
        EditText editText = new EditText(MyApplication.getContext());
        editText.setHint(name);
        editText.setLayoutParams(lp);
        editText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                            parsedString.put(name,s.toString());
                        }
                    }
            );
        ll.addView(editText,lp);
    }

    //生成点击选择包名的按钮生成
    private void pkg(final String name){
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 10;
        final EditText editText = new EditText(MyApplication.getContext());
        editText.setHint(name);
        editText.setLayoutParams(lp);
        editText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        LogUtil.e("Az",s.toString());
                        parsedString.put(name,s.toString());
                    }
                }
        );
        //点击后去选择包名
        editText.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MyApplication.setEditText(editText);
                        Intent i = new Intent(CustomAdbActivity.this, PickPackageActivity.class);
                        startActivity(i);
                    }
                }
        );
        ll.addView(editText,lp);
    }

}
