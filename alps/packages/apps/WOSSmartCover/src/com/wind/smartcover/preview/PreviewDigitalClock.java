package com.wind.smartcover.preview;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wind.smartcover.FontUtil;
import com.wind.smartcover.PubDefs;
import com.wind.smartcover.R;
import com.wind.smartcover.Util.Wind;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PreviewDigitalClock extends RelativeLayout {
	private static final String TAG = "PreviewDigitalClock";
	/*
	 * mDate1:AM/PM mDate2:18:05 mDate3:Monday mDate4:17,Jun
	 */
	private TextView mDate1;
	private TextView mDate2;
	private TextView mDate3;
	private TextView mDate4;

	private Context mContext;

	public PreviewDigitalClock(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public PreviewDigitalClock(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PreviewDigitalClock(Context context) {
		this(context, null, 0);
	}

	private void setFontColor(int color) {
		Wind.Log(TAG, "changFontColor color" + color);
		mDate1.setTextColor(color);
		mDate2.setTextColor(color);
		mDate3.setTextColor(color);
		mDate4.setTextColor(color);
	}

	private void findViewsId() {
		mDate1 = (TextView) findViewById(R.id.textView1);
		mDate2 = (TextView) findViewById(R.id.textView2);
		mDate3 = (TextView) findViewById(R.id.textView3);
		mDate4 = (TextView) findViewById(R.id.textView4);
	}

	private void setTextTypeface(Typeface type) {
		if (null == type)
			return;
		mDate1.setTypeface(type);
		mDate2.setTypeface(type);
		mDate3.setTypeface(type);
		mDate4.setTypeface(type);
	}

	private void setTextTypeface() {
		Typeface type = null;
		try {
			type = FontUtil.getTypeface(mContext, "fonts/Roboto-Thin.ttf");
		} catch (RuntimeException id) {
			type = null;
		}
		setTextTypeface(type);
	}

	private int mStyle = -1;

	public void setStyle(int nStyle) {
		mStyle = nStyle;
		initClockSrc();
		updateTime();
	}

	private void initClockSrc() {
		findViewsId();
		setTextTypeface();
		if (mStyle < 0)
			return;
		Wind.Log(TAG, "initClockSrc nStyle" + mStyle);
		switch (mStyle) {
		case 4: {
			setFontColor(getResources().getColor(R.color.digital_clock_color1));
		}
			break;
		case 5: {
			setFontColor(getResources().getColor(R.color.digital_clock_color2));
		}
			break;
		case 6: {
			setFontColor(getResources().getColor(R.color.digital_clock_color3));
		}
			break;
		case 7: {
			setFontColor(getResources().getColor(R.color.digital_clock_color4));
		}
			break;
		default: {
		}
			break;
		}
	}

	public void updateTime() {
		Wind.Log(TAG, "updateTime");
		Date curDate = new Date(System.currentTimeMillis());
		if (curDate.getHours() >= 12 && curDate.getHours() < 24)
			mDate1.setText(R.string.pm);
		else
			mDate1.setText(R.string.am);

		SimpleDateFormat date2 = new SimpleDateFormat();
		String timeFormat = android.provider.Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		if (timeFormat != null && timeFormat.equals("24")) {
			date2.applyPattern("HH:mm");
		} else {
			date2.applyPattern("hh:mm");
		}
		SimpleDateFormat date3 = new SimpleDateFormat("EEE");
		SimpleDateFormat date4 = new SimpleDateFormat("d,MMM");
		String StrDate2 = date2.format(curDate);
		String StrDate3 = date3.format(curDate);
		String StrDate4 = date4.format(curDate);

		mDate2.setText(StrDate2);
		mDate3.setText(StrDate3);
		mDate4.setText(StrDate4);
	}

	@Override
	protected void onFinishInflate() {
		Wind.Log(TAG, "onFinishInflate");
		super.onFinishInflate();
		initClockSrc();
		updateTime();
	}

}
