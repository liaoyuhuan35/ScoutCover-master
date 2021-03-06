package com.wind.smartcover.Util;

import android.content.Context;

import com.wind.smartcover.PubDefs;

public class HallUtil {

	// 霍尔开关状态
	public static final int HALL_SWITCH_ON = 1;
	public static final int HALL_SWITCH_OFF = 0;

	// 皮套状态 1 合上 0 关闭
	public static final int HALL_STATUS_ON = 1;
	public static final int HALL_STATUS_OFF = 0;

	// 霍尔开关是否打开
	public static boolean isHallSwitchOpen(Context context) {
		return android.provider.Settings.System.getInt(
				context.getContentResolver(), PubDefs.SYS_HALL_SWITCH,
				HALL_SWITCH_OFF) == HALL_SWITCH_ON;
	}

	// 打开霍尔开关
	public static void openHallSwitch(Context context) {
		android.provider.Settings.System.putInt(context.getContentResolver(),
				PubDefs.SYS_HALL_SWITCH, HALL_SWITCH_ON);
	}

	// 皮套是否合上
	public static boolean isHallOn(Context context) {
		return (getHallStatus(context) == HALL_STATUS_ON);
	}

	public static int getHallStatus(Context context) {
		return android.provider.Settings.System.getInt(
				context.getContentResolver(), PubDefs.SYS_HALL_STATUS,
				HALL_STATUS_OFF);
	}

	public static void setHallStatus(Context context, int state) {
		android.provider.Settings.System.putInt(context.getContentResolver(),
				PubDefs.SYS_HALL_STATUS, state);
	}
}
