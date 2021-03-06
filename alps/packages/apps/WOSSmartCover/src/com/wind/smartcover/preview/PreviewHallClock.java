package com.wind.smartcover.preview;

import com.wind.smartcover.R;
import com.wind.smartcover.SharedPreferenceUtil;
import com.wind.smartcover.Util.Wind;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PreviewHallClock extends RelativeLayout {
	private static final String TAG = "PreviewHallClock";

	private PreviewDigitalClock mDigital;
	private PreviewAnalogClock mAnalog;
	private Context mContext;

	private ImageView mBg;
	private int mResetStyle;
	private boolean mbSeleted = true;
	private SharedPreferenceUtil mSpUtil = null;

	private Drawable mdDial;
	private Drawable mdHour;
	private Drawable mdMinute;
	private Drawable mdSecond;

	public PreviewHallClock(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		getClockStyle(context, attrs);
		mContext = context;
		mSpUtil = SharedPreferenceUtil.getInstance(mContext
				.getApplicationContext());
	}

	public PreviewHallClock(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PreviewHallClock(Context context) {
		this(context, null, 0);
	}

	private void getClockStyle(Context context, AttributeSet attrs) {
		Wind.Log(TAG, "getClockStyle");
		TypedArray arrClock = context.obtainStyledAttributes(attrs,
				R.styleable.style_clock);

		mdDial = arrClock.getDrawable(R.styleable.style_clock_clock_dial);
		mdHour = arrClock.getDrawable(R.styleable.style_clock_clock_hour);
		mdMinute = arrClock.getDrawable(R.styleable.style_clock_clock_minute);
		mdSecond = arrClock.getDrawable(R.styleable.style_clock_clock_second);
		mResetStyle = arrClock.getInteger(
				R.styleable.style_clock_preview_style, 0);
	}

	private boolean isAnalogClock(int nStyle) {
		if (nStyle >= 4)
			return false;
		else
			return true;
	}

	private void initClockSrc() {
		Wind.Log(TAG, "initClockSrc nStyle=" + mResetStyle);
		mDigital = (PreviewDigitalClock) findViewById(R.id.digital_clock);
		mAnalog = (PreviewAnalogClock) findViewById(R.id.analog_clock);
		mBg = (ImageView) findViewById(R.id.clock_bg);
		updateViews();
		this.setOnClickListener(listener);
		if (isAnalogClock(mResetStyle)) {
			mAnalog.resetImages(mdDial, mdHour, mdMinute, mdSecond);
			mAnalog.setStyle(mResetStyle);
			mDigital.setVisibility(View.GONE);
			mAnalog.setVisibility(View.VISIBLE);
		} else {
			mDigital.setStyle(mResetStyle);
			mDigital.setVisibility(View.VISIBLE);
			mAnalog.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onFinishInflate() {
		Wind.Log(TAG, "onFinishInflate");
		super.onFinishInflate();

		initClockSrc();
	}

	public void clearSelected() {
		mbSeleted = false;
		updateViews();
	}

	public void setSelected() {
		mbSeleted = true;
		updateViews();
	}

	private void updateViews() {
		if (mbSeleted) {
			mSpUtil.setResetClockStyle(mResetStyle);
			mBg.setVisibility(View.VISIBLE);
		} else {
			mSpUtil.setResetClockStyle(-1);
			mBg.setVisibility(View.INVISIBLE);
		}
	}

	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mbSeleted = !mbSeleted;
			updateViews();
		}
	};

	public void updateDigitalTime() {
		if (null != mDigital)
			mDigital.updateTime();
	}

	public void updateAnalogViews() {
		if (null != mAnalog)
			mAnalog.updateTime();
	}
}
