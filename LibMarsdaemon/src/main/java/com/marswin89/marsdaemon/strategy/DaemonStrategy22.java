package com.marswin89.marsdaemon.strategy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.marswin89.marsdaemon.DaemonConfigurations;
import com.marswin89.marsdaemon.IDaemonStrategy;
import com.marswin89.marsdaemon.nativ.NativeDaemonAPI21;
/**
 * the strategy in android API 22.
 * 
 * @author Mars
 *
 */
public class DaemonStrategy22 implements IDaemonStrategy{
	private final static String INDICATOR_DIR_NAME 					= "indicators";
	private final static String INDICATOR_PERSISTENT_FILENAME 		= "indicator_p";
	private final static String INDICATOR_DAEMON_ASSISTANT_FILENAME = "indicator_d";
	private final static String OBSERVER_PERSISTENT_FILENAME		= "observer_p";
	private final static String OBSERVER_DAEMON_ASSISTANT_FILENAME	= "observer_d";
	
	private IBinder 				mRemote;
	private Parcel					mServiceData;
	private DaemonConfigurations 	mConfigs;

	@Override
	public boolean onInitialization(Context context) {
		return initIndicatorFiles(context);
	}

	@Override
	public void onPersistentCreate(final Context context, DaemonConfigurations configs) {
		initAmsBinder();
		initServiceParcel(context, configs.DAEMON_ASSISTANT_CONFIG.SERVICE_NAME);
		startServiceByAmsBinder();
		
		Thread t = new Thread(){
			public void run() {
				File indicatorDir = context.getDir(INDICATOR_DIR_NAME, Context.MODE_PRIVATE);
				new NativeDaemonAPI21(context).doDaemon(
						new File(indicatorDir, INDICATOR_PERSISTENT_FILENAME).getAbsolutePath(), 
						new File(indicatorDir, INDICATOR_DAEMON_ASSISTANT_FILENAME).getAbsolutePath(), 
						new File(indicatorDir, OBSERVER_PERSISTENT_FILENAME).getAbsolutePath(),
						new File(indicatorDir, OBSERVER_DAEMON_ASSISTANT_FILENAME).getAbsolutePath());
			};
		};
		t.start();
		
		if(configs != null && configs.LISTENER != null){
			this.mConfigs = configs;
			configs.LISTENER.onPersistentStart(context);
		}
	}

	@Override
	public void onDaemonAssistantCreate(final Context context, DaemonConfigurations configs) {
		initAmsBinder();
		initServiceParcel(context, configs.PERSISTENT_CONFIG.SERVICE_NAME);
		startServiceByAmsBinder();
		
		Thread t = new Thread(){
			public void run() {
				File indicatorDir = context.getDir(INDICATOR_DIR_NAME, Context.MODE_PRIVATE);
				new NativeDaemonAPI21(context).doDaemon(
						new File(indicatorDir, INDICATOR_DAEMON_ASSISTANT_FILENAME).getAbsolutePath(), 
						new File(indicatorDir, INDICATOR_PERSISTENT_FILENAME).getAbsolutePath(), 
						new File(indicatorDir, OBSERVER_DAEMON_ASSISTANT_FILENAME).getAbsolutePath(),
						new File(indicatorDir, OBSERVER_PERSISTENT_FILENAME).getAbsolutePath());
			};
		};
		t.start();
		
		if(configs != null && configs.LISTENER != null){
			this.mConfigs = configs;
			configs.LISTENER.onDaemonAssistantStart(context);
		}
		
	}
	
	
	@Override
	public void onDaemonDead() {
		if(startServiceByAmsBinder()){
			
			if(mConfigs != null && mConfigs.LISTENER != null){
				mConfigs.LISTENER.onWatchDaemonDaed();
			}
			
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}
	
	
	private void initAmsBinder(){
		Class<?> activityManagerNative;
		try {
			activityManagerNative = Class.forName("android.app.ActivityManagerNative");
			Object amn = activityManagerNative.getMethod("getDefault").invoke(activityManagerNative);  
			Field mRemoteField = amn.getClass().getDeclaredField("mRemote");
			mRemoteField.setAccessible(true);
			mRemote = (IBinder) mRemoteField.get(amn);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}  
	}
	
	
	@SuppressLint("Recycle")// when process dead, we should save time to restart and kill self, don`t take a waste of time to recycle
	private void initServiceParcel(Context context, String serviceName){
		Intent intent = new Intent();
		ComponentName component = new ComponentName(context.getPackageName(), serviceName);
		intent.setComponent(component);
		
		/*  
        //get ContextImpl instance
//          Object contextImpl = ((Application)context.getApplicationContext()).getBaseContext();
          //this context is ContextImpl, get MainThread instance immediately
          Field mainThreadField = context.getClass().getDeclaredField("mMainThread");
          mainThreadField.setAccessible(true);
          Object mainThread = mainThreadField.get(context);
          //get ApplicationThread instance
          Object applicationThread = mainThread.getClass().getMethod("getApplicationThread").invoke(mainThread);  
          //get Binder
          Binder callerBinder = (Binder) (applicationThread.getClass().getMethod("asBinder").invoke(applicationThread));  
          */
          
          //get handle
//          UserHandle userHandle = android.os.Process.myUserHandle();
//          int handle = (Integer) userHandle.getClass().getMethod("getIdentifier").invoke(userHandle);
          
          //write pacel
          mServiceData = Parcel.obtain();
          mServiceData.writeInterfaceToken("android.app.IActivityManager");
          mServiceData.writeStrongBinder(null);
//          mServiceData.writeStrongBinder(callerBinder);
          intent.writeToParcel(mServiceData, 0);
          mServiceData.writeString(null);
//          mServiceData.writeString(intent.resolveTypeIfNeeded(context.getContentResolver()));
          mServiceData.writeInt(0);
//          mServiceData.writeInt(handle);
		
	}
	
	
	private boolean startServiceByAmsBinder(){
		try {
			if(mRemote == null || mServiceData == null){
				Log.e("Daemon", "REMOTE IS NULL or PARCEL IS NULL !!!");
				return false;
			}
			mRemote.transact(34, mServiceData, null, 0);//START_SERVICE_TRANSACTION = 34
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	
	private boolean initIndicatorFiles(Context context){
		File dirFile = context.getDir(INDICATOR_DIR_NAME, Context.MODE_PRIVATE);
		if(!dirFile.exists()){
			dirFile.mkdirs();
		}
		try {
			createNewFile(dirFile, INDICATOR_PERSISTENT_FILENAME);
			createNewFile(dirFile, INDICATOR_DAEMON_ASSISTANT_FILENAME);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void createNewFile(File dirFile, String fileName) throws IOException{
		File file = new File(dirFile, fileName);
		if(!file.exists()){
			file.createNewFile();
		}
	}
}
