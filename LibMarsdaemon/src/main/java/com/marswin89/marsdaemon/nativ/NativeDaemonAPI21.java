package com.marswin89.marsdaemon.nativ;

import com.marswin89.marsdaemon.NativeDaemonBase;

import android.content.Context;

/**
 * native code to watch each other when api over 21 (contains 21)
 * @author Mars
 *
 */
public class NativeDaemonAPI21 extends NativeDaemonBase{

	public NativeDaemonAPI21(Context context) {
		super(context);
	}

	static{
		try {
			System.loadLibrary("daemon_api21");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public native void doDaemon(String indicatorSelfPath, String indicatorDaemonPath, String observerSelfPath, String observerDaemonPath);
}
