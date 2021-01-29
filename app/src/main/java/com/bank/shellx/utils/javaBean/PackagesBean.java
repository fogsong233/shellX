package com.bank.shellx.utils.javaBean;

import android.graphics.drawable.Drawable;

public class PackagesBean {

    private String label;
    private String name;
    private Drawable icon;
    private boolean isSystemApp;

    public PackagesBean() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isSystemApp(){
        return this.isSystemApp;
    }

    public void setSystemApp(boolean isSystemApp){
        this.isSystemApp = isSystemApp;
    }
}
