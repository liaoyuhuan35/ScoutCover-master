package com.wind.smartcover;

import java.util.LinkedList;
import java.util.List;

import com.wind.smartcover.Util.Wind;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

public class WindApp extends Application {
	private final static String TAG = "WindApp";

	private List<Activity> activityList = new LinkedList<Activity>();
	private static WindApp instance;

	private WindApp() {
	}

	public static WindApp getInstance() {
		if (null == instance) {
			synchronized (WindApp.class) {
				instance = new WindApp();
			}
		}
		return instance;
	}

	public void addActivity(Activity activity) {
		Wind.Log(TAG, "onDestroy() addActivity" + activity.toString());
		activityList.add(activity);
	}

	public void removeActivity(Activity activity) {
		Wind.Log(TAG, "onDestroy() removeActivity" + activity);
		activityList.remove(activity);
		Wind.Log(TAG, "onDestroy() removeActivity" + activity);
	}

	public void exitActivitys() {
		Wind.Log(TAG, "onDestroy() exitActivitys");
		try {
			for (Activity activity : activityList) {
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
	}
}

// private MainHallActivity mHallAcitvity =null;
// private IncalluiActivity mIncalluiActivity =null;
// private MusicPlayerActivity mMusicPlayerActivity = null;
// private CallLogActivity mCallLogActivity = null;
// private SmsReaderActivity mSmsReaderActivity = null;
// private ClockPreviewActivity mClockPreviewActivity = null;
// public void exitActivitys(){
// if(mHallAcitvity != null){
// mHallAcitvity.exitThisActivity();
// mHallAcitvity=null;
// }
// if(mIncalluiActivity != null){
// mIncalluiActivity.exitThisActivity();
// mIncalluiActivity = null;
// }
// if(mMusicPlayerActivity != null){
// mMusicPlayerActivity.exitThisActivity();
// mMusicPlayerActivity = null;
// }
// if(mCallLogActivity != null){
// mCallLogActivity.exitThisActivity();
// mCallLogActivity=null;
// }
// if(mSmsReaderActivity != null){
// mSmsReaderActivity.exitThisActivity();
// mSmsReaderActivity = null;
// }
// if(mClockPreviewActivity != null){
// mClockPreviewActivity.exitThisActivity();
// mClockPreviewActivity = null;
// }
//
//
// }
