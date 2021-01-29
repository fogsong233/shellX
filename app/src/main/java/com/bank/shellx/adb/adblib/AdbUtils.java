package com.bank.shellx.adb.adblib;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

//Adb的工具类
public class AdbUtils {
	
	public static final String PUBLIC_KEY_NAME = "public.key";
	public static final String PRIVATE_KEY_NAME = "private.key";
	
	public static AdbCrypto readCryptoConfig(File dataDir) {
		File pubKey = new File(dataDir, PUBLIC_KEY_NAME);
		File privKey = new File(dataDir, PRIVATE_KEY_NAME);
		
		AdbCrypto crypto = null;
		if (pubKey.exists() && privKey.exists())
		{
			try {
				crypto = AdbCrypto.loadAdbKeyPair(new AndroidBase64(), privKey, pubKey);
			} catch (Exception e) {
				crypto = null;
			}
		}
		if (crypto == null){
			return writeNewCryptoConfig(dataDir);
		}
		
		return crypto;
	}
	
	public static AdbCrypto writeNewCryptoConfig(File dataDir) {
		File pubKey = new File(dataDir, PUBLIC_KEY_NAME);
		File privKey = new File(dataDir, PRIVATE_KEY_NAME);
		
		AdbCrypto crypto = null;
		
		try {
			crypto = AdbCrypto.generateAdbKeyPair(new AndroidBase64());
			crypto.saveAdbKeyPair(privKey, pubKey);
		} catch (Exception e) {
			crypto = null;
		}
		
		return crypto;
	}
	
	public static boolean safeClose(Closeable c) {
		if (c == null)
			return false;
		
		try {
			c.close();
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
}
