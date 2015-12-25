package com.marswin89.marsdaemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 
 * @author Mars
 *
 */
public class DaemonClient implements IDaemonClient{
	private DaemonConfigurations mConfigurations;
	public DaemonClient(DaemonConfigurations configurations) {
		this.mConfigurations = configurations;
	}
	@Override
	public void onAttachBaseContext(Context base) {
		initDaemon(base);
	}
	
	
	private final String DAEMON_PERMITTING_SP_FILENAME 	= "d_permit";
	private final String DAEMON_PERMITTING_SP_KEY 		= "permitted";
	
	
	private BufferedReader mBufferedReader;//release later to save time
	
	
	/**
	 * do some thing about daemon
	 * @param base
	 */
	private void initDaemon(Context base) {
		if(!isDaemonPermitting(base) || mConfigurations == null){
			return ;
		}
		String processName = getProcessName();
		String packageName = base.getPackageName();
		
		if(processName.startsWith(mConfigurations.PERSISTENT_CONFIG.PROCESS_NAME)){
			IDaemonStrategy.Fetcher.fetchStrategy().onPersistentCreate(base, mConfigurations);
		}else if(processName.startsWith(mConfigurations.DAEMON_ASSISTANT_CONFIG.PROCESS_NAME)){
			IDaemonStrategy.Fetcher.fetchStrategy().onDaemonAssistantCreate(base, mConfigurations);
		}else if(processName.startsWith(packageName)){
			IDaemonStrategy.Fetcher.fetchStrategy().onInitialization(base);
		}
		
		releaseIO();
	}
	
	
	/* spend too much time !! 60+ms
	private String getProcessName(){
		ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		int pid = android.os.Process.myPid();
		List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
		for (int i = 0; i < infos.size(); i++) {
			RunningAppProcessInfo info = infos.get(i);
			if(pid == info.pid){
				return info.processName;
			}
		}
		return null;
	}
	*/
	
	private String getProcessName() {
		try {
			File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
			mBufferedReader = new BufferedReader(new FileReader(file));
			return mBufferedReader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * release reader IO
	 */
	private void releaseIO(){
		if(mBufferedReader != null){
			try {
				mBufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mBufferedReader = null;
		}
	}
	
	private boolean isDaemonPermitting(Context context){
		SharedPreferences sp = context.getSharedPreferences(DAEMON_PERMITTING_SP_FILENAME, Context.MODE_PRIVATE);
		return sp.getBoolean(DAEMON_PERMITTING_SP_KEY, true);
	}
	
	protected boolean setDaemonPermiiting(Context context, boolean isPermitting) {
		SharedPreferences sp = context.getSharedPreferences(DAEMON_PERMITTING_SP_FILENAME, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putBoolean(DAEMON_PERMITTING_SP_KEY, isPermitting);
		return editor.commit();
	}

}
