package com.wind.smartcover.Util;

public class Wind {
	public final static String NAME = "wind/";

	public static void Log(String tag, String msg) {
		android.util.Log.i(NAME + tag, msg);
	}
	
	public static final boolean IS_SYS_M = (android.os.Build.VERSION.SDK_INT>=23);

	// 绿色接听来电按钮放右边(D260_KEP)
	public static final boolean MACRO_LEFT_HANGON = android.os.SystemProperties.get("ro.wind.project_name").equals("D260_KEP");

	// 合上皮套立即上锁
	public static final boolean MACRO_HALL_IMMEDIATELY_LOCK = false;

	public static void getAppInfo(android.content.Context context) {
		android.util.Log.i(NAME, "	getAppInfo ");
		android.content.pm.PackageManager manager;
		android.content.pm.PackageInfo info = null;
		manager = context.getPackageManager();
		try {
			info = manager.getPackageInfo(context.getPackageName(), 0);
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		if (info != null) {
			android.util.Log.i(NAME, "	versionCode = " + info.versionCode
					+ "	versionName = " + info.versionName + " 	packageName = "
					+ info.packageName + "	signatures = " + info.signatures);
		}
	}
}
