package com.wind.smartcover.preview;

import java.util.ArrayList;

import com.wind.smartcover.EActivity;
import com.wind.smartcover.R;
import com.wind.smartcover.PubDefs;
import com.wind.smartcover.SharedPreferenceUtil;
import com.wind.smartcover.WindApp;
import com.wind.smartcover.Util.Wind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ClockPreviewActivity extends EActivity {
	private static final String TAG = "ClockPreviewActivity";
	private ViewPager mViewPager = null;
	private ArrayList<View> mPageList = null;
	private ArrayList<PreviewHallClock> mClockList = null;

	private Button mBtnBack = null;
	private Button mBtnCancel = null;
	private Button mBtnPre = null;
	private Button mBtnNext = null;
	private Context mContext = null;

	private SharedPreferenceUtil mSpUtil = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		WindApp.getInstance().addActivity(ClockPreviewActivity.this);
		setContentView(R.layout.activity_clock_preview);
		hideNavigation();

		mSpUtil = SharedPreferenceUtil.getInstance(mContext
				.getApplicationContext());

		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		LayoutInflater inflater = getLayoutInflater();
		View view1 = inflater.inflate(R.layout.preview_clock_0, null);
		View view2 = inflater.inflate(R.layout.preview_clock_1, null);
		View view3 = inflater.inflate(R.layout.preview_clock_2, null);
		View view4 = inflater.inflate(R.layout.preview_clock_3, null);
		View view5 = inflater.inflate(R.layout.preview_clock_4, null);
		View view6 = inflater.inflate(R.layout.preview_clock_5, null);
		View view7 = inflater.inflate(R.layout.preview_clock_6, null);
		View view8 = inflater.inflate(R.layout.preview_clock_7, null);

		mPageList = new ArrayList<View>();
		mClockList = new ArrayList<PreviewHallClock>();
		mPageList.add(view1);
		mPageList.add(view2);
		mPageList.add(view3);
		mPageList.add(view4);
		mPageList.add(view5);
		mPageList.add(view6);
		mPageList.add(view7);
		mPageList.add(view8);

		mClockList.add((PreviewHallClock) view1
				.findViewById(R.id.preview_hall_clock));
		mClockList.add((PreviewHallClock) view2
				.findViewById(R.id.preview_hall_clock));
		mClockList.add((PreviewHallClock) view3
				.findViewById(R.id.preview_hall_clock));
		mClockList.add((PreviewHallClock) view4
				.findViewById(R.id.preview_hall_clock));
		mClockList.add((PreviewHallClock) view5
				.findViewById(R.id.preview_hall_clock));
		mClockList.add((PreviewHallClock) view6
				.findViewById(R.id.preview_hall_clock));
		mClockList.add((PreviewHallClock) view7
				.findViewById(R.id.preview_hall_clock));
		mClockList.add((PreviewHallClock) view8
				.findViewById(R.id.preview_hall_clock));

		mViewPager.setAdapter(mPagerAdapter);

		mBtnBack = (Button) findViewById(R.id.btn_back);
		mBtnCancel = (Button) findViewById(R.id.btn_cancel);
		mBtnPre = (Button) findViewById(R.id.btn_clock_pre);
		mBtnNext = (Button) findViewById(R.id.btn_clock_next);

		mBtnBack.setOnClickListener(mClickListener);
		mBtnCancel.setOnClickListener(mClickListener);
		mBtnPre.setOnClickListener(mClickListener);
		mBtnNext.setOnClickListener(mClickListener);

		updateBtnsView();
		mViewPager.setOnPageChangeListener(pageListener);
		initTimeReceiver();

		if (!checkHallStatus(mContext)) {
			this.exitThisActivity();
			return;
		}

		initClockViews();
	}

	private void updateBtnsView() {
		int nMax = mPageList.size();
		int nCur = mViewPager.getCurrentItem();
		if (nCur == 0) {
			mBtnPre.setEnabled(false);
		} else {
			mBtnPre.setEnabled(true);
		}
		if (nMax - 1 == nCur) {
			mBtnNext.setEnabled(false);
		} else {
			mBtnNext.setEnabled(true);
		}
	}

	private int getClockStyle(Context context) {
		Wind.Log(TAG, "getClockStyle");
		return Settings.System.getInt(context.getContentResolver(),
				PubDefs.CLOCK_STYLE, 0);
	}

	private void initClockViews() {
		int nClockStyle = getClockStyle(mContext);
		if (nClockStyle >= 0 || nClockStyle < 8) {
			mViewPager.setCurrentItem(nClockStyle);
			if (null != mPageList) {
				PreviewHallClock clock = (PreviewHallClock) mPageList.get(
						nClockStyle).findViewById(R.id.preview_hall_clock);
				clock.setSelected();
				updateBtnsView();
			}
		}
	}

	public OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.btn_back: {
				if (mViewPager.getCurrentItem() == mSpUtil.getResetClockStyle())
					resetClockStyle(mContext, mViewPager.getCurrentItem());
				else
					sendMsg(mContext, PubDefs.WOS_RESET_CLOCK);
				exitThisActivity();
			}
				break;
			case R.id.btn_cancel: {
				exitThisActivity();
			}
				break;
			case R.id.btn_clock_pre: {
				mViewPager.arrowScroll(1);
				updateBtnsView();
			}
				break;
			case R.id.btn_clock_next: {
				mViewPager.arrowScroll(2);
				updateBtnsView();
			}
				break;
			default:
				break;
			}
		}
	};

	private View mPreView = null;
	private OnPageChangeListener pageListener = new OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int cur) {
			if (mPreView != null) {
				PreviewHallClock clock = (PreviewHallClock) mPreView
						.findViewById(R.id.preview_hall_clock);
				clock.setSelected();
			}
			updateBtnsView();
		}

		@Override
		public void onPageScrolled(int cur, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int cur) {
			mPreView = mPageList.get(cur);
		}
	};

	private PagerAdapter mPagerAdapter = new PagerAdapter() {

		@Override
		public int getCount() {
			return mPageList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		public void destroyItem(View view, int position, Object arg2) {
			((ViewPager) view).removeView(mPageList.get(position));
		}

		public Object instantiateItem(View view, int position) {
			View addView = mPageList.get(position);
			((ViewPager) view).addView(addView);
			PreviewHallClock clock = (PreviewHallClock) addView
					.findViewById(R.id.preview_hall_clock);
			if (null != clock) {
				clock.updateAnalogViews();
			}
			return addView;
		}
	};

	public static void sendMsg(Context ctx, String action) {
		Intent intent = new Intent(action);
		ctx.sendBroadcast(intent);
	}

	/*
	 * clock style
	 */
	private void resetClockStyle(Context context, int nStyle) {
		Wind.Log(TAG, "resetClockStyle nStyle" + nStyle);
		Settings.System.putInt(getContentResolver(), PubDefs.CLOCK_STYLE,
				nStyle);
		sendMsg(context, PubDefs.WOS_RESET_CLOCK);
	}

	@Override
	protected void onDestroy() {
		Wind.Log(TAG, "onDestroy()");
		if (mTimeReceiver != null)
			mContext.unregisterReceiver(mTimeReceiver);
		mPageList = null;
		mClockList = null;
		WindApp.getInstance().removeActivity(ClockPreviewActivity.this);
		super.onDestroy();
	};

	private TimeReceiver mTimeReceiver = null;
	private IntentFilter mTimeFilter = null;

	private void initTimeReceiver() {
		mTimeReceiver = new TimeReceiver();
		mTimeFilter = new IntentFilter();
		mTimeFilter.addAction(Intent.ACTION_TIME_TICK);
		mContext.registerReceiver(mTimeReceiver, mTimeFilter);
	}

	class TimeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Wind.Log(TAG, "TimeReceiver onReceive" + intent.getAction());
			if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
				for (PreviewHallClock clock : mClockList) {
					if (null != clock) {
						clock.updateDigitalTime();
					}
				}
			}
		}
	}
}
