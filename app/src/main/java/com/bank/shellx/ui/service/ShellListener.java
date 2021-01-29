package com.bank.shellx.ui.service;


import android.app.Service;

import com.bank.shellx.adb.adblib.AdbCrypto;
import com.bank.shellx.adb.adblib.AdbUtils;
import com.bank.shellx.adb.console.ConsoleBuffer;
import com.bank.shellx.adb.devconn.DeviceConnection;
import com.bank.shellx.adb.devconn.DeviceConnectionListener;

import java.util.HashMap;
import java.util.LinkedList;

public class ShellListener implements DeviceConnectionListener {
	private static final int TERM_LENGTH = 25000;

	private HashMap<DeviceConnection, LinkedList<DeviceConnectionListener>> listenerMap =
			new HashMap<DeviceConnection, LinkedList<DeviceConnectionListener>>();
	private HashMap<DeviceConnection, ConsoleBuffer> consoleMap =
			new HashMap<DeviceConnection, ConsoleBuffer>();
	private Service service;

	public ShellListener(Service service) {
		this.service = service;
	}

	public synchronized void addListener(DeviceConnection conn, DeviceConnectionListener listener) {
		LinkedList<DeviceConnectionListener> listeners = listenerMap.get(conn);
		if (listeners != null) {
			listeners.add(listener);
		}
		else {
			listeners = new LinkedList<DeviceConnectionListener>();
			listeners.add(listener);
			listenerMap.put(conn, listeners);
		}

		/* If the listener supports console input, we'll tell them about the console buffer
		 * by firing them an initial console updated callback */
		ConsoleBuffer console = consoleMap.get(conn);
		if (console != null && listener.isConsole()) {
			listener.consoleUpdated(conn, console);
		}
	}

	public synchronized void removeListener(DeviceConnection conn, DeviceConnectionListener listener) {
		LinkedList<DeviceConnectionListener> listeners = listenerMap.get(conn);
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	@Override
	public void notifyConnectionEstablished(DeviceConnection devConn) {
		consoleMap.put(devConn, new ConsoleBuffer(TERM_LENGTH));

		LinkedList<DeviceConnectionListener> listeners = listenerMap.get(devConn);
		if (listeners != null) {
			for (DeviceConnectionListener listener : listeners) {
				listener.notifyConnectionEstablished(devConn);
			}
		}
	}

	@Override
	public void notifyConnectionFailed(DeviceConnection devConn, Exception e) {
		LinkedList<DeviceConnectionListener> listeners = listenerMap.get(devConn);
		if (listeners != null) {
			for (DeviceConnectionListener listener : listeners) {
				listener.notifyConnectionFailed(devConn, e);
			}
		}
	}

	@Override
	public void notifyStreamFailed(DeviceConnection devConn, Exception e) {
		/* Return if this connection has already "failed" */
		if (consoleMap.remove(devConn) == null) {
			return;
		}

		LinkedList<DeviceConnectionListener> listeners = listenerMap.get(devConn);
		if (listeners != null) {
			for (DeviceConnectionListener listener : listeners) {
				listener.notifyStreamFailed(devConn, e);
			}
		}
	}

	@Override
	public void notifyStreamClosed(DeviceConnection devConn) {
		/* Return if this connection has already "failed" */
		if (consoleMap.remove(devConn) == null) {
			return;
		}

		LinkedList<DeviceConnectionListener> listeners = listenerMap.get(devConn);
		if (listeners != null) {
			for (DeviceConnectionListener listener : listeners) {
				listener.notifyStreamClosed(devConn);
			}
		}
	}

	@Override
	public AdbCrypto loadAdbCrypto(DeviceConnection devConn) {
		return AdbUtils.readCryptoConfig(service.getFilesDir());
	}

	@Override
	public void receivedData(DeviceConnection devConn, byte[] data,
							 int offset, int length) {
		/* Add data to the console for this connection */
		ConsoleBuffer console = consoleMap.get(devConn);
		if (console != null) {
			/* Hack to remove the bell from the end of the prompt */
			if (data[offset+length-1] == 0x07) {
				length--;
			}

			console.append(data, offset, length);

			/* Attempt to deliver a console update notification */
			LinkedList<DeviceConnectionListener> listeners = listenerMap.get(devConn);
			if (listeners != null) {
				for (DeviceConnectionListener listener : listeners) {
					if (listener.isConsole()) {
						listener.consoleUpdated(devConn, console);
					}
				}
			}
		}
	}

	@Override
	public boolean canReceiveData() {
		/* We can always receive data */
		return true;
	}

	@Override
	public boolean isConsole() {
		return false;
	}

	@Override
	public void consoleUpdated(DeviceConnection devConsole,
							   ConsoleBuffer console) {
	}
}

