package com.wind.smartcover.preview;

import com.wind.smartcover.PubDefs;
import com.wind.smartcover.R;
import com.wind.smartcover.Util.Wind;
import com.wind.smartcover.util.ZXTUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PreviewAnalogClock extends RelativeLayout {
	private static final String TAG = "AnalogClockView";

	private Time mTime;
	private Context mContext;

	private Drawable mdDial;
	private Drawable mdHour;
	private Drawable mdMinute;
	private Drawable mdSecond;
	private ImageView mAnimalDial = null;
	private ImageView mAnimalSecond = null;
	private ImageView mAnimalHour = null;
	private ImageView mAnimalMinute = null;

	private TextView mDate = null;

	public PreviewAnalogClock(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		// mAttrs= attrs;
	}

	public PreviewAnalogClock(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PreviewAnalogClock(Context context) {
		this(context, null, 0);
	}

	public void resetImages(Drawable dDial, Drawable dHour, Drawable dMinute,
			Drawable dSecond) {
		Wind.Log(TAG, "resetImages");
		mdDial = dDial;
		mdHour = dHour;
		mdMinute = dMinute;
		mdSecond = dSecond;

		mAnimalDial.setImageDrawable(mdDial);
		mAnimalHour.setImageDrawable(mdHour);
		mAnimalMinute.setImageDrawable(mdMinute);
		mAnimalSecond.setImageDrawable(mdSecond);

		updateTime();
	}

	public void updateTime() {
		mTime = new Time(Time.getCurrentTimezone());
		startHourAnimation();
		drawDate();
		startSecondAnimation();
		startMinuterAnimation();
	}

	private int mStyle = -1;

	public void setStyle(int nStyle) {
		mStyle = nStyle;
		initClockSrc();
		updateTime();
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

	private void initClockSrc() {
		Wind.Log(TAG, "initClockSrc nStyle" + mStyle);
		switch (mStyle) {
		case 0: {
			moveView(mDate, 139);
			checkViewVisible(mDate, mStyle);
		}
			break;
		case 1: {
			moveView(mDate, 155);
			checkViewVisible(mDate, mStyle);
		}
			break;
		case 2: {
			moveView(mDate, 130);
			checkViewVisible(mDate, mStyle);
		}
			break;
		case 3: {
			checkViewVisible(mDate, mStyle);
		}
			break;
		default: {
		}
			break;
		}
	}

	// A: zhangxutong@wind-mobi.com 2015 08 18 end

	@Override
	protected void onFinishInflate() {
		Wind.Log(TAG, "onFinishInflate");
		super.onFinishInflate();
		mAnimalHour = (ImageView) findViewById(R.id.clock_hour);
		mAnimalMinute = (ImageView) findViewById(R.id.clock_minute);
		mAnimalSecond = (ImageView) findViewById(R.id.clock_second);
		mAnimalDial = (ImageView) findViewById(R.id.clock_dial);

		mDate = (TextView) findViewById(R.id.clock_day);

	}

	private final static int HOUR_DURATION = 12 * 60 * 60 * 1000;
	private final static int MINUTE_DURATION = 60 * 60 * 1000;
	private final static int SECOND_DURATION = 60 * 1000;
	private final static int HOUR_PERCENT = 360 / 12;

	private void startHourAnimation() {
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

	private void startMinuterAnimation() {
		Wind.Log(TAG, "setMinuterAnimation");
		mTime.setToNow();
		int minute = mTime.minute;
		int second = mTime.second;
		float mMinute = minute + second / 60.0f;
		mMinute = mMinute * 6;
		RotateAnimation rotateAnim = newAnim(mMinute, MINUTE_DURATION);
		mAnimalMinute.setAnimation(rotateAnim);
		rotateAnim.start();
		invalidate();
	}

	private void startSecondAnimation() {
		Wind.Log(TAG, "startSecondAnimation");
		mTime.setToNow();
		int nSecond = mTime.second;
		nSecond = nSecond * 6;
		RotateAnimation rotateAnim = newAnim(nSecond, SECOND_DURATION);
		mAnimalSecond.setAnimation(rotateAnim);
		rotateAnim.start();
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
