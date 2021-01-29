package com.bank.shellx.ui.service;

        import android.annotation.SuppressLint;
        import android.app.Notification;
        import android.app.NotificationChannel;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
        import android.graphics.BitmapFactory;
        import android.net.wifi.WifiManager;
        import android.os.Binder;
        import android.os.Build;
        import android.os.IBinder;
        import android.os.PowerManager;

        import androidx.core.app.NotificationCompat;

        import com.bank.shellx.R;
        import com.bank.shellx.adb.adblib.AdbCrypto;
        import com.bank.shellx.adb.console.ConsoleBuffer;
        import com.bank.shellx.adb.devconn.DeviceConnection;
        import com.bank.shellx.adb.devconn.DeviceConnectionListener;
        import com.bank.shellx.ui.adb.AdbShellActivity;

        import java.util.HashMap;


public class ShellService extends Service implements DeviceConnectionListener {

    private ShellServiceBinder binder = new ShellServiceBinder();
    private ShellListener listener = new ShellListener(this);

    private HashMap<String, DeviceConnection> currentConnectionMap =
            new HashMap<>();

    private WifiManager.WifiLock wlanLock;
    private PowerManager.WakeLock wakeLock;

    private final static int CONN_BASE = 12131;
    private final static int FAILED_BASE = 12111;

    private int foregroundId;

    public class ShellServiceBinder extends Binder {
        public DeviceConnection createConnection(String host, int port) {
            DeviceConnection conn = new DeviceConnection(listener, host, port);
            listener.addListener(conn, ShellService.this);
            return conn;
        }

        public DeviceConnection findConnection(String host, int port) {
            String connStr = host+":"+port;
            return currentConnectionMap.get(connStr);
        }

        public void notifyPausingActivity(DeviceConnection devConn) {
        }

        public void notifyResumingActivity(DeviceConnection devConn) {
        }

        public void notifyDestroyingActivity(DeviceConnection devConn) {
            /* If we're pausing before destruction after the connection is closed, remove the failure
             * notification */
            if (devConn.isClosed()) {
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(getFailedNotificationId(devConn));
            }

            /* Stop the the service if no connections remain */
            if (currentConnectionMap.isEmpty()) {
                stopSelf();
            }
        }

        public void addListener(DeviceConnection conn, DeviceConnectionListener listener) {
            ShellService.this.listener.addListener(conn, listener);
        }

        public void removeListener(DeviceConnection conn, DeviceConnectionListener listener) {
            ShellService.this.listener.removeListener(conn, listener);
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    private synchronized void acquireWakeLocks() {
        if (wlanLock == null) {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wlanLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "Shell X");
        }
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Shell X");
        }
        wakeLock.acquire();
        wlanLock.acquire();
    }

    private synchronized void releaseWakeLocks() {
        wlanLock.release();
        wakeLock.release();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    private int getFailedNotificationId(DeviceConnection devConn) {
        return FAILED_BASE + getConnectionString(devConn).hashCode();
    }

    private int getConnectedNotificationId(DeviceConnection devConn) {
        return CONN_BASE + getConnectionString(devConn).hashCode();
    }

    private PendingIntent createPendingIntentForConnection(DeviceConnection devConn) {
        Context appContext = getApplicationContext();

        Intent i = new Intent(appContext, AdbShellActivity.class);
        i.putExtra("IP", devConn.getHost());
        i.putExtra("Port", devConn.getPort());
        i.setAction(getConnectionString(devConn));

        return PendingIntent.getActivity(appContext, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification createNotification(DeviceConnection devConn, boolean connected) {
        String ticker;
        String message;

        if (connected) {
            ticker = "连接成功";
            message = "连接到"+getConnectionString(devConn);
        }
        else {
            ticker = "连接中断";
            message = "连接"+getConnectionString(devConn)+" 失败";
        }

        String ID = "com.bank.shellx";	//这里的id里面输入自己的项目的包的路径
        String NAME = "Shell X";
        Intent intent = new Intent(ShellService.this, AdbShellActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder notification; //创建服务对象
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(ID, NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            assert manager != null;
            manager.createNotificationChannel(channel);
            notification = new NotificationCompat.Builder(ShellService.this).setChannelId(ID);
        } else {
            notification = new NotificationCompat.Builder(ShellService.this);
        }
        notification.setContentTitle(ticker)
                .setContentText(message)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .build();
        Notification notification1 = notification.build();
        return notification1;
    }

    private void updateNotification(DeviceConnection devConn, boolean connected) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        removeNotification(devConn);

        if (connected) {
            if (foregroundId != 0) {
                /* There's already a foreground notification, so use the normal notification framework */
                nm.notify(getConnectedNotificationId(devConn), createNotification(devConn, connected));
            }
            else {
                /* This is the first notification so make it the foreground one */
                foregroundId = getConnectedNotificationId(devConn);
                startForeground(foregroundId, createNotification(devConn, connected));
            }
        }
        else {
            nm.notify(getFailedNotificationId(devConn), createNotification(devConn, connected));
        }
    }

    private void removeNotification(DeviceConnection devConn) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /* Removing failure notifications is easy */
        nm.cancel(getFailedNotificationId(devConn));

        /* Connected notifications is a bit more complex */
        if (getConnectedNotificationId(devConn) == foregroundId) {
            /* We're the foreground notification, so we need to switch in another
             * notification to take our place */

            /* Search for a new device connection to promote */
            DeviceConnection newConn = null;
            for (DeviceConnection conn : currentConnectionMap.values()) {
                if (devConn == conn) {
                    continue;
                }
                else {
                    newConn = conn;
                    break;
                }
            }

            if (newConn == null) {
                /* None found, so we're done in foreground */
                stopForeground(true);
                foregroundId = 0;
            }
            else {
                /* Found one, so cancel this guy's original notification
                 * and start it as foreground */
                foregroundId = getConnectedNotificationId(newConn);
                nm.cancel(foregroundId);
                startForeground(foregroundId, createNotification(newConn, true));
            }
        }
        else {
            /* This just a normal connected notification */
            nm.cancel(getConnectedNotificationId(devConn));
        }
    }

    private String getConnectionString(DeviceConnection devConn) {
        return devConn.getHost()+":"+devConn.getPort();
    }

    private void addNewConnection(DeviceConnection devConn) {
        currentConnectionMap.put(getConnectionString(devConn), devConn);
        acquireWakeLocks();
    }

    private void removeConnection(DeviceConnection devConn) {
        currentConnectionMap.remove(getConnectionString(devConn));
        releaseWakeLocks();

        /* Stop the the service if no connections remain */
        if (currentConnectionMap.isEmpty()) {
            stopSelf();
        }
    }

    @Override
    public void notifyConnectionEstablished(DeviceConnection devConn) {
        addNewConnection(devConn);
        updateNotification(devConn, true);
    }

    @Override
    public void notifyConnectionFailed(DeviceConnection devConn, Exception e) {
        /* No notification is displaying here */
    }

    @Override
    public void notifyStreamFailed(DeviceConnection devConn, Exception e) {
        updateNotification(devConn, false);
        removeConnection(devConn);
    }

    @Override
    public void notifyStreamClosed(DeviceConnection devConn) {
        removeNotification(devConn);
        removeConnection(devConn);
    }

    @Override
    public AdbCrypto loadAdbCrypto(DeviceConnection devConn) {
        return null;
    }

    @Override
    public void receivedData(DeviceConnection devConn, byte[] data, int offset,
                             int length) {
    }

    @Override
    public boolean canReceiveData() {
        return false;
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public void consoleUpdated(DeviceConnection devConn,
                               ConsoleBuffer console) {
    }
}
