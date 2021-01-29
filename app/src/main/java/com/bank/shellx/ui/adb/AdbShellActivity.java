package com.bank.shellx.ui.adb;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bank.shellx.R;
import com.bank.shellx.adb.adblib.AdbCrypto;
import com.bank.shellx.adb.adblib.AdbUtils;
import com.bank.shellx.adb.console.CommandHistory;
import com.bank.shellx.adb.console.ConsoleBuffer;
import com.bank.shellx.adb.devconn.AddCommandListener;
import com.bank.shellx.adb.devconn.DeviceConnection;
import com.bank.shellx.adb.devconn.DeviceConnectionListener;
import com.bank.shellx.ui.dialog.Dialog;
import com.bank.shellx.ui.dialog.SpinnerDialog;
import com.bank.shellx.ui.service.ShellService;

import java.util.concurrent.atomic.AtomicBoolean;

public class AdbShellActivity extends AppCompatActivity implements DeviceConnectionListener, AddCommandListener {

    private TextView shellView;
    private EditText commandBox;
    private ScrollView shellScroller;
    private Button submitButton;

    private String hostName;
    private int port;
    private String addedCommand;

    private DeviceConnection connection;

    private Intent service;
    private ShellService.ShellServiceBinder binder;

    private SpinnerDialog connectWaiting;

    private final static String PREFS_FILE = "AdbCmdHistoryPrefs";
    private static final int MAX_COMMAND_HISTORY = 15;
    private CommandHistory commandHistory;

    private boolean updateGui;
    private AtomicBoolean updateQueued = new AtomicBoolean();
    private AtomicBoolean updateRequired = new AtomicBoolean();

    private boolean autoScrollEnabled = true;
    private boolean userScrolling = false;
    private boolean scrollViewAtBottom = true;

    private ConsoleBuffer lastConsoleBuffer;

    private StringBuilder commandBuffer = new StringBuilder();

    private static final int MENU_ID_CTRL_C = 1;
    private static final int MENU_ID_AUTOSCROLL = 2;
    private static final int MENU_ID_EXIT = 3;

    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            binder = (ShellService.ShellServiceBinder)arg1;
            if (connection != null) {
                binder.removeListener(connection, AdbShellActivity.this);
            }
            connection = AdbShellActivity.this.connectOrLookupConnection(hostName, port);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            binder = null;
        }
    };

    public static void start(Context ctx,String IP, int port) {
        Intent i = new Intent(ctx,AdbShellActivity.class);
        i.putExtra("IP",IP);
        i.putExtra("port",port);
        ctx.startActivity(i);
    }

    public static void start(Context ctx,String IP, int port,String text) {
        Intent i = new Intent(ctx,AdbShellActivity.class);
        i.putExtra("IP",IP);
        i.putExtra("port",port);
        i.putExtra("text",text);
        ctx.startActivity(i);
    }

    @Override
    public void onNewIntent(Intent shellIntent) {
        super.onNewIntent(shellIntent);
        hostName = shellIntent.getStringExtra("IP");
        port = shellIntent.getIntExtra("port", -1);
        addedCommand = shellIntent.getStringExtra("text");
        if (hostName == null || port == -1) {
            finish();
            return;
        }


        if (binder == null) {
            /* Bind the service if we're not bound already. After binding, the callback will
             * perform the initial connection. */
            getApplicationContext().bindService(service, serviceConn, Service.BIND_AUTO_CREATE);
        } else {
            /* We're already bound, so do the connect or lookup immediately */
            if (connection != null) {
                binder.removeListener(connection, this);
            }
            connection = connectOrLookupConnection(hostName, port);
        }
    }

    private DeviceConnection startConnection(String host, int port) {
        /* Display the connection progress spinner */
        connectWaiting = SpinnerDialog.displayDialog(this, "连接到 "+hostName+":"+port,
                "请确保目标设备已启用网络ADB," +
                        "\n" +
                        "如果正在连接且第一次使用该设备，则可能需要接受目标设备上的提示。" +
                        "\n" , true);

        /* Create the connection object */
        DeviceConnection conn = binder.createConnection(host, port);

        /* Add this activity as a connection listener */
        binder.addListener(conn, this);

        /* Begin the async connection process */
        conn.startConnect();

        return conn;
    }

    private DeviceConnection connectOrLookupConnection(String host, int port) {
        DeviceConnection conn = binder.findConnection(host, port);
        if (conn == null) {
            /* No existing connection, so start the connection process */
            conn = startConnection(host, port);
        }
        else {
            /* Add ourselves as a new listener of this connection */
            binder.addListener(conn, this);
        }
        return conn;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adb_shell);

        /* Setup our controls */
        shellView = findViewById(R.id.shellView);
        commandBox = findViewById(R.id.command);
        shellScroller = findViewById(R.id.shellScroller);
        submitButton = findViewById(R.id.adb_shell_submit);

        submitButton.setOnClickListener(
                v -> {
                    String text = commandBox.getText().toString();

                    /* Append the command to our command buffer (which is empty) */
                    commandBuffer.append(text);

                    /* Add the command to the previous command list */
                    commandHistory.add(text);

                    /* Append a newline since it's not included in the command itself */
                    commandBuffer.append('\n');

                    /* Send it to the device */
                    connection.queueCommand(commandBuffer.toString());

                    /* Clear the textbox and command buffer */
                    commandBuffer.setLength(0);
                    commandBox.setText("");

                    /* Force scroll to the bottom */
                    scrollViewAtBottom = true;
                    doAsyncGuiUpdate();
                }
        );

        View.OnLongClickListener showMenu = view -> {
            openContextMenu(commandBox);
            return true;
        };

        final ViewTreeObserver.OnScrollChangedListener onScrollChangedListener = () -> {
            View view = shellScroller.getChildAt(0);
            int diff = view.getBottom() - (shellScroller.getHeight() + shellScroller.getScrollY());
            if (diff <= 0) {
                doAsyncGuiUpdate();
                scrollViewAtBottom = true;
            }
            else {
                scrollViewAtBottom = false;
            }
        };

        shellScroller.setOnTouchListener((view, event) -> {
            ViewTreeObserver observer = AdbShellActivity.this.shellScroller.getViewTreeObserver();
            switch (event.getActionMasked())
            {
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_DOWN:
                    observer.addOnScrollChangedListener(onScrollChangedListener);
                    userScrolling = true;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (scrollViewAtBottom) {
                        doAsyncGuiUpdate();
                    }
                    userScrolling = false;
                    break;
            }

            /* Don't consume the event */
            return false;
        });

        commandBox.setImeActionLabel("Run", EditorInfo.IME_ACTION_DONE);
        commandBox.setOnLongClickListener(showMenu);

        registerForContextMenu(commandBox);
        registerForContextMenu(shellView);

        /* Pull previous command history (if any) */
        commandHistory = CommandHistory.loadCommandHistoryFromPrefs(MAX_COMMAND_HISTORY, this, PREFS_FILE);

        service = new Intent(this, ShellService.class);
        getApplicationContext().startService(service);

        onNewIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        /* Save the command history first */
        commandHistory.save();

        if (binder != null && connection != null) {
            /* Tell the service about our impending doom */
            binder.notifyDestroyingActivity(connection);

            /* Dissociate our activity's listener */
            binder.removeListener(connection, this);
        }

        /* If the connection hasn't actually finished yet,
         * close it before terminating */
        if (connectWaiting != null) {
            AdbUtils.safeClose(connection);
        }

        /* Unbind from the service since we're going away */
        if (service != null) {
            getApplicationContext().unbindService(serviceConn);
        }

        Dialog.closeDialogs();
        SpinnerDialog.closeDialogs();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        /* Tell the service about our UI state change */
        if (binder != null) {
            binder.notifyResumingActivity(connection);
        }

        /* There might be changes we need to display */
        updateTerminalView();

        /* Start updating the GUI again */
        updateGui = true;
        super.onResume();
    }

    @Override
    public void onPause() {
        /* Tell the service about our UI state change */
        if (binder != null) {
            binder.notifyPausingActivity(connection);
        }

        /* Stop updating the GUI for now */
        updateGui = false;
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v == commandBox) {
            commandHistory.populateMenu(menu);
        }
        else {
            menu.add(Menu.NONE, MENU_ID_CTRL_C, Menu.NONE, "Send Ctrl+C");

            MenuItem autoscroll = menu.add(Menu.NONE, MENU_ID_AUTOSCROLL, Menu.NONE, "Auto-scroll terminal");
            autoscroll.setCheckable(true);
            autoscroll.setChecked(autoScrollEnabled);

            menu.add(Menu.NONE, MENU_ID_EXIT, Menu.NONE, "Exit Terminal");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            commandBox.setText(item.getTitle());
        }
        else {
            switch (item.getItemId())
            {
                case MENU_ID_CTRL_C:
                    if (connection != null) {
                        connection.queueBytes(new byte[]{0x03});

                        /* Force scroll to the bottom */
                        scrollViewAtBottom = true;
                        doAsyncGuiUpdate();
                    }
                    break;

                case MENU_ID_AUTOSCROLL:
                    item.setChecked(!item.isChecked());
                    autoScrollEnabled = item.isChecked();
                    break;

                case MENU_ID_EXIT:
                    AdbUtils.safeClose(connection);
                    finish();
                    break;
            }
        }
        return true;
    }

    private void updateTerminalView() {
        if (lastConsoleBuffer != null) {
            lastConsoleBuffer.updateTextView(shellView);
        }
        if (autoScrollEnabled) {
            shellView.post(() -> {
                if (scrollViewAtBottom) {
                    shellScroller.smoothScrollTo(0, shellView.getBottom());
                }
            });
        }
    }

    @Override
    public void notifyConnectionEstablished(DeviceConnection devConn) {
        connectWaiting.dismiss();
        connectWaiting = null;
        if ( (addedCommand != null) && (!addedCommand.equals("")) ){
            addCommand(addedCommand);
        }
    }

    @Override
    public void notifyConnectionFailed(DeviceConnection devConn, Exception e) {
        connectWaiting.dismiss();
        connectWaiting = null;

        Dialog.displayDialog(this, "Connection Failed", e.getMessage(), true);
    }

    @Override
    public void notifyStreamFailed(DeviceConnection devConn, Exception e) {
        Dialog.displayDialog(this, "Connection Terminated", e.getMessage(), true);
    }

    @Override
    public void notifyStreamClosed(DeviceConnection devConn) {
        Dialog.displayDialog(this, "Connection Closed", "The connection was gracefully closed.", true);
    }

    @Override
    public AdbCrypto loadAdbCrypto(DeviceConnection devConn) {
        return AdbUtils.readCryptoConfig(getFilesDir());
    }

    @Override
    public boolean canReceiveData() {
        /* We just handle console updates */
        return false;
    }

    @Override
    public void receivedData(DeviceConnection devConn, byte[] data, int offset,
                             int length) {
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    private void setGuiDirty() {
        /* Remember that a GUI update is needed */
        updateRequired.set(true);
    }

    private void doAsyncGuiUpdate() {
        /* If no update is required, do nothing */
        if (!updateRequired.get()) {
            return;
        }

        /* If an update isn't already queued, fire one off */
        if (updateQueued.compareAndSet(false, true)) {
            new Thread(() -> {
                /* Wait for a few milliseconds to avoid spamming GUI updates */

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    return;
                }

                runOnUiThread(() -> {
                    /* We won't need an update again after this */
                    updateRequired.set(false);

                    /* Redraw the terminal */
                    updateTerminalView();

                    /* This update is finished */
                    updateQueued.set(false);

                    /* If someone updated the console between the time that we
                     * started redrawing and when we finished, we need to update
                     * the GUI again, otherwise the GUI update could be missed. */
                    if (updateRequired.get()) {
                        doAsyncGuiUpdate();
                    }
                });
            }).start();
        }
    }

    @Override
    public void consoleUpdated(DeviceConnection devConn, ConsoleBuffer console) {
        lastConsoleBuffer = console;

        setGuiDirty();
        if (updateGui && !userScrolling && scrollViewAtBottom) {
            doAsyncGuiUpdate();
        }
    }

    @Override
    public void addCommand(String text) {
        /* Append the command to our command buffer (which is empty) */
        commandBuffer.append(text);

        /* Add the command to the previous command list */
        commandHistory.add(text);

        /* Append a newline since it's not included in the command itself */
        commandBuffer.append('\n');

        /* Send it to the device */
        connection.queueCommand(commandBuffer.toString());

        /* Clear the textbox and command buffer */
        commandBuffer.setLength(0);
        commandBox.setText("");

        /* Force scroll to the bottom */
        scrollViewAtBottom = true;
        doAsyncGuiUpdate();
    }
}
