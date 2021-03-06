package com.wind.smartcover.clock;

import com.wind.smartcover.R;
import com.wind.smartcover.PubDefs;

import android.content.Context;
import android.provider.Settings;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wind.smartcover.Util.Wind;
import com.wind.smartcover.util.ZXTUtils;

public class AnalogClock extends RelativeLayout {
	private static final String TAG = "AnalogClockView";

	private Time mTime;

	private ImageView mAnimalDial = null;
	private ImageView mAnimalSecond = null;
	private ImageView mAnimalHour = null;
	private ImageView mAnimalMinute = null;
	private TextView mDate = null;
	private Context mContext;
	private int mStyle = 0;

	public void resetStyle(int nStyle) {
		mStyle = nStyle;
		refreshViews();
	}

	public AnalogClock(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public AnalogClock(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AnalogClock(Context context) {
		this(context, null, 0);
	}

	private int getClockStyle(Context context) {
		Wind.Log(TAG, "getClockStyle");
		return Settings.System.getInt(mContext.getContentResolver(),
				PubDefs.CLOCK_STYLE, 0);
	}

	// A: zhangxutong@wind-mobi.com 2015 08 18 begin
	private void moveView(View view, int ndp) {
		RelativeLayout.LayoutParams lp1 = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp1.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		lp1.setMargins(0, ZXTUtils.dip2px(mContext, ndp), 0, 0);
		view.setLayoutParams(lp1);
	}

	private void checkViewVisible(View view, int nStyle) {
		view.setVisibility(nStyle % 4 == 3 ? View.GONE : View.VISIBLE);
	}

	// A: zhangxutong@wind-mobi.com 2015 08 18 end
	private void initClockSrc() {
		int style = getClockStyle(mContext);
		Wind.Log(TAG, "initClockSrc nStyle" + style);
		mAnimalDial = (ImageView) findViewById(R.id.clock_dial);
		mAnimalHour = (ImageView) findViewById(R.id.clock_hour);
		mAnimalMinute = (ImageView) findViewById(R.id.clock_minute);
		mAnimalSecond = (ImageView) findViewById(R.id.clock_second);
		mDate = (TextView) findViewById(R.id.clock_day);

		switch (mStyle) {
		case 0: {
			mAnimalDial.setImageResource(R.drawable.normal_clock_dial_0);
			mAnimalHour.setImageResource(R.drawable.normal_clock_hour_0);
			mAnimalMinute.setImageResource(R.drawable.normal_clock_minute_0);
			mAnimalSecond.setImageResource(R.drawable.normal_clock_second_0);
			// A: zhangxutong@wind-mobi.com 2015 08 18 begin
			moveView(mDate, 195);
			mDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			checkViewVisible(mDate, style);
			// A: zhangxutong@wind-mobi.com 2015 08 18 end
		}
			break;
		case 1: {
			mAnimalDial.setImageResource(R.drawable.normal_clock_dial_1);
			mAnimalHour.setImageResource(R.drawable.normal_clock_hour_1);
			mAnimalMinute.setImageResource(R.drawable.normal_clock_minute_1);
			mAnimalSecond.setImageResource(R.drawable.normal_clock_second_1);
			// A: zhangxutong@wind-mobi.com 2015 08 18 begin
			moveView(mDate, 218);
			mDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
			checkViewVisible(mDate, style);
			// A: zhangxutong@wind-mobi.com 2015 08 18 end
		}
			break;
		case 2: {
			mAnimalDial.setImageResource(R.drawable.normal_clock_dial_2);
			mAnimalHour.setImageResource(R.drawable.normal_clock_hour_2);
			mAnimalMinute.setImageResource(R.drawable.normal_clock_minute_2);
			mAnimalSecond.setImageResource(R.drawable.normal_clock_second_2);
			// A: zhangxutong@wind-mobi.com 2015 08 18 begin
			moveView(mDate, 185);
			mDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			checkViewVisible(mDate, style);
			// A: zhangxutong@wind-mobi.com 2015 08 18 end
		}
			break;
		case 3: {
			mAnimalDial.setImageResource(R.drawable.normal_clock_dial_3);
			mAnimalHour.setImageResource(R.drawable.normal_clock_hour_3);
			mAnimalMinute.setImageResource(R.drawable.normal_clock_minute_3);
			mAnimalSecond.setImageResource(R.drawable.normal_clock_second_3);
			// A: zhangxutong@wind-mobi.com 2015 08 18 begin
			checkViewVisible(mDate, style);
			// A: zhangxutong@wind-mobi.com 2015 08 18 end
		}
			break;
		default: {
		}
			break;
		}
	}

	@Override
	protected void onFinishInflate() {
		Wind.Log(TAG, "onFinishInflate");
		super.onFinishInflate();
		mTime = new Time(Time.getCurrentTimezone());

		refreshViews();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	};

	public void refreshTime() {
		initClockSrc();
		setHourAnimation();
		drawDate();
		setMinuterAnimation();
	}

	public void refreshViews() {
		initClockSrc();
		setHourAnimation();
		drawDate();
		setMinuterAnimation();
		startSecondAnimation();
	}

	private final static int HOUR_DURATION = 12 * 60 * 60 * 1000;
	private final static int MINUTE_DURATION = 60 * 60 * 1000;
	private final static int SECOND_DURATION = 60 * 1000;
	private final static int HOUR_PERCENT = 360 / 12;
	private final static int SEC_MINU_PERCENT = 360 / 60;

	private void setHourAnimation() {
		Wind.Log(TAG, "setHourAnimation");
		mTime.setToNow();
		int hour = mTime.hour % 12;
		int minute = mTime.minute;
		int second = mTime.second;
		float mHour = hour + minute / 60.0f + second / 3600.0f;
		mHour = mHour * HOUR_PERCENT;
		RotateAnimation rotateAnim = newAnim(mHour, HOUR_DURATION);
		mAnimalHour.setAnimation(rotateAnim);
		rotateAnim.start();
		invalidate();
	}

	private void drawDate() {
		Wind.Log(TAG, "drawDate");
		mTime.setToNow();
		mDate.setText("" + mTime.monthDay);
	}

	private void setMinuterAnimation() {
		Wind.Log(TAG, "setMinuterAnimation");
		mTime.setToNow();
		int minute = mTime.minute;
		int second = mTime.second;
		float mMinute = minute + second / 60.0f;
		mMinute = mMinute * SEC_MINU_PERCENT;
		RotateAnimation rotateAnim = newAnim(mMinute, MINUTE_DURATION);
		mAnimalMinute.setAnimation(rotateAnim);
		rotateAnim.start();
		invalidate();
	}

	private void startSecondAnimation() {
		Wind.Log(TAG, "startSecondAnimation");
		mTime.setToNow();
		int nSecond = mTime.second;
		nSecond = nSecond * SEC_MINU_PERCENT;
		RotateAnimation rotateAnim = newAnim(nSecond, SECOND_DURATION);
		mAnimalSecond.setAnimation(rotateAnim);
		rotateAnim.start();
		invalidate();
	}

	private RotateAnimation newAnim(float nOffset, int duration) {
		RotateAnimation anim = new RotateAnimation(nOffset, 359 + nOffset,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim.setStartOffset((int) nOffset);
		anim.setDuration(duration);
		anim.setRepeatCount(-1);
		anim.setInterpolator(new LinearInterpolator());
		return anim;
	}
}
