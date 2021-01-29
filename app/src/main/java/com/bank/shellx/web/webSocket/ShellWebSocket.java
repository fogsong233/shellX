package com.bank.shellx.web.webSocket;

import com.bank.shellx.adb.adblib.AdbCrypto;
import com.bank.shellx.adb.adblib.AdbUtils;
import com.bank.shellx.adb.console.ConsoleBuffer;
import com.bank.shellx.adb.devconn.DeviceConnection;
import com.bank.shellx.adb.devconn.DeviceConnectionListener;
import com.bank.shellx.application.MyApplication;
import com.bank.shellx.utils.logUtils.LogUtil;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class ShellWebSocket extends WebSocketServer implements DeviceConnectionListener {

    DeviceConnection deviceConnection;
    boolean isConnecting = false;
    //返回结果的列表
    LinkedBlockingQueue<String> result = new LinkedBlockingQueue<>();

    public ShellWebSocket(InetSocketAddress address) {
        super(address);
        deviceConnection = new DeviceConnection(this,"localhost",5555);
        deviceConnection.startConnect();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Shell命令行已连接");
        if (!isConnecting) {
            conn.send("连接失败");
        }
        new Thread(){
            @Override
            public void run() {
                while (true) {
                    try {
                        String s = result.take();
                        LogUtil.e("socket",s);
                        conn.send(s);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (isConnecting) {
            deviceConnection.queueCommand(message);
        }
        LogUtil.e("socket",message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
        LogUtil.e("socket","ok");
    }

    @Override
    public void notifyConnectionEstablished(DeviceConnection devConn) {
        isConnecting = true;
    }

    @Override
    public void notifyConnectionFailed(DeviceConnection devConn, Exception e) {

    }

    @Override
    public void notifyStreamFailed(DeviceConnection devConn, Exception e) {

    }

    @Override
    public void notifyStreamClosed(DeviceConnection devConn) {

    }

    @Override
    public AdbCrypto loadAdbCrypto(DeviceConnection devConn) {
        return AdbUtils.readCryptoConfig(MyApplication.getContext().getFilesDir());
    }

    @Override
    public boolean canReceiveData() {
        return true;
    }

    @Override
    public void receivedData(DeviceConnection devConn, byte[] data, int offset, int length) {
        result.add(Arrays.toString(data));
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public void consoleUpdated(DeviceConnection devConn, ConsoleBuffer console) {

    }
}
