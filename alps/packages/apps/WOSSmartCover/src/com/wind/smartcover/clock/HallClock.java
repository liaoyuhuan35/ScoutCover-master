package com.wind.smartcover.clock;

import com.wind.smartcover.R;
import com.wind.smartcover.PubDefs;
import com.wind.smartcover.Util.Wind;
//import com.wind.smartcover.clock.DigitalClock.TimeReceiver;
//import com.wind.smartcover.clock.DigitalClock.StyleContentObserver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class HallClock extends RelativeLayout {
	private static final String TAG = "HallClock";

	private DigitalClock mDigital;
	private AnalogClock mAnalog;
	private Context mContext;

	private ImageView mWhiteView = null;
	private AnimationDrawable mWhiteAnim = null;

	public HallClock(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		// regeist();
	}

	public HallClock(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HallClock(Context context) {
		this(context, null, 0);
	}

	private int getClockStyle(Context context) {
		Wind.Log(TAG, "getClockStyle");
		return Settings.System.getInt(mContext.getContentResolver(),
				PubDefs.CLOCK_STYLE, 0);
	}

	public void startAnim() {
		Wind.Log(TAG, "startAnim");
		mWhiteAnim = (AnimationDrawable) mWhiteView.getDrawable();
		if (mWhiteAnim != null) {
			if (mWhiteAnim.isRunning()) {
				mWhiteAnim.stop();
			}

			mWhiteAnim.start();

		}
		if (mAnalog != null) {
			mAnalog.refreshTime();
		}
	}

	private boolean isAnalogClock(int nStyle) {
		if (nStyle >= 4)
			return false;
		else
			return true;
	}

	private void initClockSrc() {
		Wind.Log(TAG, "initClockSrc");
		mWhiteView = (ImageView) findViewById(R.id.white_anim);
		mWhiteView.setImageResource(R.drawable.hall_phone_white_anim);
		mWhiteAnim = (AnimationDrawable) mWhiteView.getDrawable();
		mDigital = (DigitalClock) findViewById(R.id.digital_clock);
		mAnalog = (AnalogClock) findViewById(R.id.analog_clock);
		int nStyle = getClockStyle(mContext);
		Wind.Log(TAG, "initClockSrc nStyle=" + nStyle);
		mDigital.resetStyle(nStyle);
		mAnalog.resetStyle(nStyle);

		if (isAnalogClock(nStyle)) {
			mDigital.setVisibility(View.GONE);
			mAnalog.setVisibility(View.VISIBLE);
		} else {
			mDigital.setVisibility(View.VISIBLE);
			mAnalog.setVisibility(View.GONE);
		}

		// startAnim();
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 100:
				break;
			}
		}
	};

	private ContentObserver mObserver;
	ContentResolver mCr;

	public class StyleContentObserver extends ContentObserver {

		public StyleContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			initClockSrc();
		}
	}

	public void regeistMsgs() {
		mCr = mContext.getContentResolver();
		mObserver = new StyleContentObserver(handler);
		mCr.registerContentObserver(
				Settings.System.getUriFor(PubDefs.CLOCK_STYLE), true, mObserver);
		regeistTimeReceiver();
	}

	public void unregeistMsgs() {
		mCr = mContext.getContentResolver();
		mCr.unregisterContentObserver(mObserver);
		mContext.unregisterReceiver(mTimeReceiver);
	}

	private void regeistTimeReceiver() {
		mTimeReceiver = new TimeReceiver();
		mTimeFilter = new IntentFilter();
		mTimeFilter.addAction(Intent.ACTION_TIME_TICK);
		mContext.registerReceiver(mTimeReceiver, mTimeFilter);
	}

	private TimeReceiver mTimeReceiver = null;
	private IntentFilter mTimeFilter = null;

	class TimeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Wind.Log(TAG, "TimeReceiver onReceive" + intent.getAction());
			if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
				mDigital.updateTime();
			}
		}
	}

	private static final int DOUBLE_CLICK_TIME = 500;
	private long mFirstClick = 0;
	private long mLastClick = 0;
	private long mClickCount = 0;

	private void clear() {
		mFirstClick = 0;
		mLastClick = 0;
		mClickCount = 0;
	}

	private boolean DOUBLE_CLICK_MACRO = true;

	private void screenOff() {
		if (!DOUBLE_CLICK_MACRO)
			return;

		PowerManager pm = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		pm.goToSleep(SystemClock.uptimeMillis());
		PowerManager.WakeLock wakeLock = pm.newWakeLock(
				PowerManager.SCREEN_DIM_WAKE_LOCK, "WOSSmartCover");
		wakeLock.acquire();
		wakeLock.release();
	}

	@Override
	public boolean onTouchEvent(android.view.MotionEvent event) {
		Wind.Log(TAG, "onTouchEvent event" + event.getAction());
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (mFirstClick != 0
					&& System.currentTimeMillis() - mFirstClick > DOUBLE_CLICK_TIME) {
				clear();
			}

			mClickCount++;
			if (1 == mClickCount)
				mFirstClick = System.currentTimeMillis();
			else if (2 == mClickCount) {
				mLastClick = System.currentTimeMillis();
				if (mLastClick - mFirstClick < DOUBLE_CLICK_TIME) {
					screenOff();
				}
				clear();
			}
			return true;
		}
		return super.onTouchEvent(event);
	};

	@Override
	protected void onFinishInflate() {
		Wind.Log(TAG, "onFinishInflate");
		super.onFinishInflate();

		initClockSrc();
	}
}
