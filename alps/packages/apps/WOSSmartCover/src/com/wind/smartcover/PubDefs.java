package com.wind.smartcover;

import com.wind.smartcover.Util.Wind;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class PubDefs {
	public final static String WIND	= Wind.NAME;
	
	private final static String TAG = "PubDefs";
	/* Project Macro Defines begin */
	public final static String PROJECT_MACRO = "ro.wind.project_name";

	public final static String D260_CMB_MACRO = "ro.wind_project_cherrymobile";
	public final static String D260_BASE_MACRO = "ro.wind_project_d260";
	public final static String D260_XTH_MACRO = "ro.wind_project_d260_xth";

	public final static String D261_XTH_MACRO = "ro.wind_project_d261_xth";
	public final static String D261_QMB_MACRO = "D261_QMB";

	public final static String D260_CMB_CHANNEL = "D260_Cherry";
	public final static String D260_BASE_CHANNEL = "D260_BASE";
	public final static String D260_XTH_CHANNEL = "D260_XTH";

	public final static String D261_XTH_CHANNEL = "D261_XTH";
	public final static String D261_QMB_CHANNEL = "D261_QMB";

	//放入皮套，立即灭屏上锁
	public final static boolean HALL_ON_SCREEN_OFF_LOCK = true;
	/* Project Macro Defines end */

	private static boolean isD260XTH() {
		android.util.Log.i(TAG,
				"D260_XTH?:"
						+ android.os.SystemProperties.get(PROJECT_MACRO)
								.equals(D260_XTH_CHANNEL));
		return android.os.SystemProperties.get(PROJECT_MACRO).equals(
				D260_XTH_CHANNEL);
	}

	// private static boolean isHallSwitchOpen() {
	// if (isD260XTH())
	// return true;
	// else
	// return false;
	// }
	//
	// /*
	// * false:normal true:use switch
	// */
	// public static final boolean mbEnableHallSwitch = false;/*
	// * isHallSwitchOpen()
	// * ;
	// */

	/*
	 * 
	 * others hang off
	 */
	public static final String MSG_PHONE_EXIT = "msg.phone.exit";
	public static final String MSG_PHONE_HANGOFF = "phone.incallui.hangoff";
	public static final String MSG_EXIT_ACTIVITY = "phone.exit.activity";

	/*
	 * smart cover settings clock switch
	 */
	public static final String SMARTCOVER_SETTINGS = "smartcover_settings";
	public static final String CLOCK_STYLE = "clock_style";

	public static final String WOS_UI_DIALER = "wos.ui.dialer";
	public static final String WOS_CALL_NUM = "call_num";

	public static final String SYS_HALL_SWITCH = "system_hall_switch";
	public static final String SYS_HALL_STATUS = "system_hall_status";
	public static final String SYS_HALL_SIMCARD = "system_hall_simcard";

	public static final String WOS_RESET_CLOCK = "wos.reset.clock";
	public static final String WOS_NULL_LIGHT = "wos.null.light";

	public static void sendMsg(Context ctx, String action) {
		Intent intent = new Intent(action);
		ctx.sendBroadcast(intent);
	}

	private static int getSystemLockStyle(Context ctx) {
		return Settings.System.getInt(ctx.getContentResolver(),
				"WOS_LOCK_STYLE", 0);
	}

	public static boolean isReplaceSystemLock(Context ctx) {
		Log.d(TAG, "isReplaceSystemLock getSystemLockStyle=" + getSystemLockStyle(ctx));
		if (getSystemLockStyle(ctx) >= 3)
			return false;
		else
			return true;
	}

}
