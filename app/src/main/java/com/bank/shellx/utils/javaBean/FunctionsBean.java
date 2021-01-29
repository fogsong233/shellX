package com.bank.shellx.utils.javaBean;

import android.graphics.drawable.Drawable;

public class FunctionsBean {

    private String name;
    private int icon;
    private FunctionOnclickCallBack callBack;

    public FunctionsBean(String name, int icon, FunctionOnclickCallBack cb){
        this.name = name;
        this.icon = icon;
        callBack = cb;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public FunctionOnclickCallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(FunctionOnclickCallBack callBack) {
        this.callBack = callBack;
    }

    //CallBack接口
    @FunctionalInterface
    public interface FunctionOnclickCallBack{
        void onClick();
    }

}
