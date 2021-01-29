package com.bank.shellx.web.webSocket;

public interface WebShellListener {

    void onConnectionSuccess();

    void onConnectionFailed();

    void onFailed();

    void onReceive(String s);
}
