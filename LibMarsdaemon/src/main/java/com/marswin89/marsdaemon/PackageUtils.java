package com.marswin89.marsdaemon;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Utils to prevent component from third-party app forbidding
 *
 * @author Mars
 *
 */
public class PackageUtils {
	/**
	 * set the component in our package default
	 * @param context
	 * @param componentClassName
	 */
	public static void setComponentDefault(Context context, String componentClassName){
		PackageManager pm = context.getPackageManager();
		ComponentName componentName = new ComponentName(context.getPackageName(), componentClassName);
		pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
	}
	
	/**
	 * get the component in our package default
	 * @param context
	 * @param componentClassName
	 */
	public static boolean isComponentDefault(Context context, String componentClassName){
		PackageManager pm = context.getPackageManager();
		ComponentName componentName = new ComponentName(context.getPackageName(), componentClassName);
		return pm.getComponentEnabledSetting(componentName) == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
	}
}
