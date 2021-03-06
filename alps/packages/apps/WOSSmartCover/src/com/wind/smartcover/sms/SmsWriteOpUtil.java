package com.wind.smartcover.sms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.wind.smartcover.PubDefs;
import com.wind.smartcover.Util.Wind;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

public final class SmsWriteOpUtil {

	private static final String TAG = "SmsWriteOpUtil";
	private static final int OP_WRITE_SMS = 15;

	public static boolean isWriteEnabled(Context context) {
		Wind.Log(TAG, "isWriteEnabled ");
		int uid = getUid(context);
		Object opRes = checkOp(context, OP_WRITE_SMS, uid);

		if (opRes instanceof Integer) {
			return (Integer) opRes == AppOpsManager.MODE_ALLOWED;
		}
		return false;
	}

	public static boolean setWriteEnable(Context context, boolean enable) {
		Wind.Log(TAG, "setWriteEnable enable=" + enable);
		int uid = getUid(context);

		int mode = enable ? AppOpsManager.MODE_ALLOWED
				: AppOpsManager.MODE_IGNORED;
		return setMode(context, OP_WRITE_SMS, uid, mode);
	}

	private static Object checkOp(Context context, int code, int uid) {
		Wind.Log(TAG, "checkOp code=" + code + " uid=" + uid);
		AppOpsManager appOpsmanager = (AppOpsManager) context
				.getSystemService(Context.APP_OPS_SERVICE);
		Class appOpsManagerClass = appOpsmanager.getClass();

		try {
			// get invoke function
			Class[] types = new Class[3];
			types[0] = Integer.TYPE;
			types[1] = Integer.TYPE;
			types[2] = String.class;

			Method checkOpMethod = appOpsManagerClass.getMethod("checkOp",
					types);

			// use invoke function
			Object[] args = new Object[3];
			args[0] = Integer.valueOf(code);
			args[1] = Integer.valueOf(code);
			args[2] = context.getPackageName();

			Object result = checkOpMethod.invoke(appOpsmanager, args);
			return result;
		} catch (NoSuchMethodException e) {
			Wind.Log(TAG, "" + e.toString());
		} catch (InvocationTargetException e) {
			Wind.Log(TAG, "" + e.toString());
		} catch (IllegalAccessException e) {
			Wind.Log(TAG, "" + e.toString());
		}
		return null;
	}

	private static boolean setMode(Context context, int code, int uid, int mode) {
		Wind.Log(TAG, "setMode code=" + code + " uid=" + uid + " mode=" + mode);
		AppOpsManager appOpsmanager = (AppOpsManager) context
				.getSystemService(Context.APP_OPS_SERVICE);
		Class appOpsManagerClass = appOpsmanager.getClass();

		try {
			// get invoke function
			Class[] types = new Class[4];
			types[0] = Integer.TYPE;
			types[1] = Integer.TYPE;
			types[2] = String.class;
			types[3] = Integer.TYPE;

			Method setModeMethod = appOpsManagerClass.getMethod("setMode",
					types);

			// use invoke function
			Object[] args = new Object[4];
			args[0] = Integer.valueOf(code);
			args[1] = Integer.valueOf(code);
			args[2] = context.getPackageName();
			args[3] = Integer.valueOf(code);

			setModeMethod.invoke(appOpsmanager, args);
			return true;
		} catch (NoSuchMethodException e) {
			Wind.Log(TAG, "" + e.toString());
		} catch (InvocationTargetException e) {
			Wind.Log(TAG, "" + e.toString());
		} catch (IllegalAccessException e) {
			Wind.Log(TAG, "" + e.toString());
		}
		return false;
	}

	private static int getUid(Context context) {
		Wind.Log(TAG, "getUid ");
		try {
			int uid = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_SERVICES).uid;
			return uid;
		} catch (PackageManager.NameNotFoundException e) {
			Wind.Log(TAG, "" + e.toString());
			return 0;
		}
	}
}
