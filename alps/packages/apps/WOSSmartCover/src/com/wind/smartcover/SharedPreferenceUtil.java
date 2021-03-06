package com.wind.smartcover;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SharedPreferenceUtil {
	private Context mContext;
	private static final String MMS_STATE = "mms_state";
	private static final String CALLOG_STATE = "callog_state";
	private static final String RESET_STYLE = "reset_style";
	private static final String PACKAGE_NAME = "com.wind.smartcover";
	private static SharedPreferenceUtil mSharedPreferenceUtil = null;

	private SharedPreferenceUtil(Context context) {
		mContext = context;
	}

	public static SharedPreferenceUtil getInstance(Context context) {
		if (null == mSharedPreferenceUtil) {
			mSharedPreferenceUtil = new SharedPreferenceUtil(context);
		}
		return mSharedPreferenceUtil;
	}

	private void setSpInt(String action, int date) {
		SharedPreferences sp = mContext.getSharedPreferences(PACKAGE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt(action, date);
		editor.commit();
	}

	private int getSpInt(String action) {
		SharedPreferences sp = mContext.getSharedPreferences(PACKAGE_NAME,
				Context.MODE_PRIVATE);

		return sp.getInt(action, -1);
	}

	public void setResetClockStyle(int nStyle) {
		setSpInt(RESET_STYLE, nStyle);
	}

	public int getResetClockStyle() {
		return getSpInt(RESET_STYLE);
	}

	public void setMmsStatus(int status) {
		setSpInt(MMS_STATE, status);
	}

	public int getMmsStatus() {
		return getSpInt(MMS_STATE);
	}

	/*
	 * 0 invisible 1/-1 visible
	 */
	public void setCallogStatus(int status) {
		setSpInt(CALLOG_STATE, status);
	}

	public int getCallogStatus() {
		return getSpInt(CALLOG_STATE);
	}

}
