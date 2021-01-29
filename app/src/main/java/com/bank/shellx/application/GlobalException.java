package com.bank.shellx.application;


//错误全局获取
public class GlobalException implements Thread.UncaughtExceptionHandler {

    private final static GlobalException myCrashHandler = new GlobalException();

    private GlobalException() {
    }

    public static synchronized GlobalException getInstance() {
        return myCrashHandler;
    }

    public void uncaughtException(Thread arg0, Throwable arg1) {
        //暂时不处理了
    }
}