package com.wind.util;

import com.baidu.mobstat.StatService;
import com.wind.smartcover.PubDefs;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class BaiduStatWrapper {
	private final static String TAG = PubDefs.WIND + "BaiduStatWrapper";
	/**
	 * Macro name of project( Pdu provide)
	 */
	public final static String PROJECT_MACRO = "ro.wind.project_name";

	public final static String D260_CMB_MACRO = "ro.wind_project_cherrymobile";
	public final static String D260_BASE_MACRO = "ro.wind_project_d260";
	public final static String D260_XTH_MACRO = "ro.wind_project_d260_xth";
	public final static String D260_STX_MACRO = "ro.wind.project_d260f_stx";

	public final static String D261_XTH_MACRO = "ro.wind_project_d261_xth";
	public final static String D261_QMB_MACRO = "D261_QMB";

	public final static String M350_QMB_MACRO = "ro.wind.mobile.m350";
	/**
	 * Channel name of project ( zhangxutong@wind-mobi.com define)
	 */
	public final static String D260_CMB_CHANNEL = "D260_Cherry";
	public final static String D260_BASE_CHANNEL = "D260_BASE";
	public final static String D260_XTH_CHANNEL = "D260_XTH";
	public final static String D260_STX_CHANNEL = "D260_STX";

	public final static String D261_XTH_CHANNEL = "D261_XTH";
	public final static String D261_QMB_CHANNEL = "D261_QMB";

	public final static String M350_QMB_CHANNEL = "M350_QMB";

	/**
	 * Use in Activity onCreate(). for BaiduStat initial configuration
	 * 
	 * @param context
	 *            (Activity context ,not Application context)
	 */
	public static void init(Context context) {
		if (android.os.SystemProperties.get(D260_CMB_MACRO).equals("1")) {
			StatService.setAppChannel(context, D260_CMB_CHANNEL, true);
		} else if (android.os.SystemProperties.get(D260_STX_MACRO).equals("1")) {
			StatService.setAppChannel(context, D260_STX_CHANNEL, true);
		} else if (android.os.SystemProperties.get(D260_XTH_MACRO).equals("1")) {
			StatService.setAppChannel(context, D260_XTH_CHANNEL, true);
		} else if (android.os.SystemProperties.get(D260_BASE_MACRO).equals("1")) {
			StatService.setAppChannel(context, D260_BASE_CHANNEL, true);
		}

		else if (android.os.SystemProperties.get(D261_XTH_MACRO).equals("1")) {
			StatService.setAppChannel(context, D261_XTH_CHANNEL, true);
		} else if (isD261QMB()) {
			StatService.setAppChannel(context, D261_QMB_CHANNEL, true);
		}

		else if (isM350QMB()) {
			StatService.setAppChannel(context, M350_QMB_CHANNEL, true);
		}

		else {
			StatService.setAppChannel(context, "BASE_version_" + Build.MODEL,
					true);
		}
		StatService.setSessionTimeOut(30);
		android.util.Log.i(TAG, "project name :"
				+ android.os.SystemProperties.get("ro.wind.project_name"));
	}

	/**
	 * Use in Activity onResume()
	 * 
	 * @param context
	 *            (Activity context ,not Application context)
	 */
	public static void onResume(Context context) {
		StatService.onResume(context);
	}

	/**
	 * Use in Activity onPause()
	 * 
	 * @param context
	 *            (Activity context ,not Application context)
	 */
	public static void onPause(Context context) {
		StatService.onPause(context);
	}

	public static boolean isM350QMB() {
		return android.os.SystemProperties.get(M350_QMB_MACRO).equals("1");
	}

	public static boolean isD261QMB() {
		return android.os.SystemProperties.get(PROJECT_MACRO).equals(
				D261_QMB_MACRO);
	}
}
