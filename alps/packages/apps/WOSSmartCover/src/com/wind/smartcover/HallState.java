package com.wind.smartcover;

/**
 * create by wuguohu@wind-mobi.com
 * 
 */
public class HallState {
	static {
		System.loadLibrary("windhallstate"); // libwindhallstate.so
	}

	public synchronized static int getHallState() {
		return JniGetState();
	}

	public synchronized static int setHallCfg(int cfg) {
		return JniSetHallCfg(cfg);
	}

	public static native int JniGetState();

	public static native int JniSetHallCfg(int cfg);

}
