package com.wind.smartcover.settings;

import com.wind.smartcover.LightController;
import com.wind.smartcover.PubDefs;
import com.wind.smartcover.R;
import com.wind.smartcover.SharedPreferenceUtil;
import com.wind.smartcover.Util.Wind;
import com.wind.smartcover.calllog.CallLogActivity;
import com.wind.smartcover.music.MusicPlayerActivity;
import com.wind.smartcover.preview.ClockPreviewActivity;
import com.wind.smartcover.sms.SmsReaderActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class CircleLayout extends ViewGroup {
	private static final String TAG = "CircleLayout";

	private float radius;
	private int mDegreeDelta;
	private int offset;

	private Context mContext;

	private TextView mTvMissCall;
	private TextView mTvMissMms;

	private int mMissedMms = 0;
	private int mMissedCall = 0;

	private ImageButton mBtnLight;
	private ImageButton mBtnCallog;
	private ImageButton mBtnMms;
	private SharedPreferenceUtil mSPUtil = null;

	public CircleLayout(Context context) {
		super(context);
	}

	public CircleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.CircleLayout);
		radius = a.getDimension(R.styleable.CircleLayout_radius, 20);
		offset = a.getInteger(R.styleable.CircleLayout_offset, 0);

		Wind.Log(TAG, "radius:" + radius);
		a.recycle();
		mContext = context;
		mSPUtil = SharedPreferenceUtil.getInstance(mContext);

		// initFlashLight();
	}

	public CircleLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private LightController mLightController = null;

	public void setmLightController(LightController controll) {
		Wind.Log(TAG, "setmLightController mLightController=" + mLightController
				+ " controll=" + controll);
		mLightController = controll;

		if (mLightController == null || !mLightController.isOpenCamera()) {
			mBtnLight.setBackgroundResource(R.drawable.ic_light_unable);
			mBtnLight.setClickable(false);
			// mBtnLight.setVisibility(View.GONE);
			return;
		} else {
			mBtnLight.setBackgroundResource(R.drawable.ic_light_off);
			mBtnLight.setClickable(true);
			mBtnLight.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onFinishInflate() {
		Wind.Log(TAG, "onFinishInflate ");

		mBtnLight = (ImageButton) findViewById(R.id.bt_light);
		mBtnCallog = (ImageButton) findViewById(R.id.bt_dialer);
		mBtnMms = (ImageButton) findViewById(R.id.bt_sms);

		mTvMissCall = (TextView) findViewById(R.id.tv_dialer);
		mTvMissMms = (TextView) findViewById(R.id.tv_sms);

		mBtnMms.setOnClickListener(ClickListener);
		mBtnCallog.setOnClickListener(ClickListener);

		mBtnCallog.setOnTouchListener(TouchListener);
		mBtnMms.setOnTouchListener(TouchListener);
	};

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		final int count = getChildCount();

		mDegreeDelta = 360 / count;

		final int parentLeft = getPaddingLeft();
		final int parentRight = right - left - getPaddingRight();

		final int parentTop = getPaddingTop();
		final int parentBottom = bottom - top - getPaddingBottom();

		Wind.Log(TAG, "parentLeft=" + parentLeft + "parentRight=" + parentRight
				+ "parentTop=" + parentTop + "parentBottom=" + parentBottom);

		if (count < 1) {
			return;
		}
		Wind.Log(TAG, "" + Math.cos(0 * Math.PI / 180));

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {

				final int width = child.getMeasuredWidth();
				final int height = child.getMeasuredHeight();

				int childLeft;
				int childTop;
				if (count == 1) {
					childLeft = parentLeft + (parentRight - parentLeft - width)
							/ 2;
					childTop = parentTop + (parentBottom - parentTop - height)
							/ 2;
					child.layout(childLeft, childTop, childLeft + width,
							childTop + height);
				} else {
					childLeft = (int) (parentLeft
							+ (parentRight - parentLeft - width) / 2 - (radius * Math
							.sin((i * mDegreeDelta + offset) * Math.PI / 180)));
					childTop = (int) (parentTop
							+ (parentBottom - parentTop - height) / 2 - (radius * Math
							.cos((i * mDegreeDelta + offset) * Math.PI / 180)));
					child.layout(childLeft, childTop, childLeft + width,
							childTop + height);
					child.setOnClickListener(ClickListener);
				}
			}
		}
	}

	@Override
	public boolean canResolveLayoutDirection() {
		return super.canResolveLayoutDirection();
	}

	private void startActivity(Class<?> cls) {
		Intent intent = new Intent(mContext, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		mContext.startActivity(intent);
	}

	private void resetDialerSmsView(boolean bSet) {
		if (bSet) {
			mBtnMms.setBackgroundResource(R.drawable.btn_p_sms_press);
			mBtnCallog.setBackgroundResource(R.drawable.btn_p_dialer_press);
		} else {
			mBtnMms.setBackgroundResource(R.drawable.btn_p_sms_default);
			mBtnCallog.setBackgroundResource(R.drawable.btn_p_dialer_default);
		}
	}

	private OnTouchListener TouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			Wind.Log(TAG, "TouchListener action=" + event.getAction());
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				switch (view.getId()) {
				case R.id.rl_dialer:
				case R.id.bt_dialer: {
					mBtnCallog
							.setBackgroundResource(R.drawable.btn_p_dialer_press);
					break;
				}

				case R.id.bt_light: {
					break;
				}
				case R.id.rl_sms:
				case R.id.bt_sms: {
					mBtnMms.setBackgroundResource(R.drawable.btn_p_sms_press);
					break;
				}
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP
					| event.getAction() == MotionEvent.ACTION_CANCEL) {
				resetDialerSmsView(false);
			}

			return false;
		}

	};

	private OnClickListener ClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			Wind.Log(TAG, "onClick view.getId" + view.getId());
			resetDialerSmsView(false);
			switch (view.getId()) {
			case R.id.rl_dialer:
			case R.id.bt_dialer: {
				startActivity(CallLogActivity.class);
				updateMissedCallView(true);
				Wind.Log(TAG, "onClick bt_dialer");
			}
				break;
			case R.id.bt_light: {
				Wind.Log(TAG, "onClick bt_light mLightController="
						+ mLightController);
				if (mLightController == null) {
					PubDefs.sendMsg(mContext, PubDefs.WOS_NULL_LIGHT);
					return;
				}

				mLightController.changeStatus();
				if (mLightController.isLightOpen()) {
					mBtnLight.setBackgroundResource(R.drawable.ic_light_on);
				} else {
					mBtnLight.setBackgroundResource(R.drawable.ic_light_off);
				}
			}
				break;
			case R.id.bt_music: {
				startActivity(MusicPlayerActivity.class);
				Wind.Log(TAG, "onClick bt_music");
			}
				break;
			case R.id.bt_settings: {
				startActivity(ClockPreviewActivity.class);
				Wind.Log(TAG, "onClick bt_settings");
			}
				break;
			case R.id.rl_sms:
			case R.id.bt_sms: {
				startActivity(SmsReaderActivity.class);
				updateMissedMmsView(true);
				Wind.Log(TAG, "onClick bt_sms");
			}
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

		measureChildren(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(sizeWidth, sizeHeight);
	}

	void resetViewTextSize(TextView view, int nCount) {
		if (nCount <= 0) {
			view.setVisibility(View.INVISIBLE);
		} else if (nCount > 0 && nCount < 10) {
			view.setText("" + nCount);
			view.setVisibility(View.VISIBLE);
			view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
		} else if (nCount >= 10) {
			view.setText("" + nCount);
			view.setVisibility(View.VISIBLE);
			view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		} else if (nCount >= 100) {
			view.setText("" + nCount);
			view.setVisibility(View.VISIBLE);
			view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		}
	}

	private void updateMissedCallView(boolean mbUpdate) {
		Wind.Log(TAG, "updateMissedView Call mbUpdate=" + mbUpdate
				+ " mMissedCall =" + mMissedCall);
		if (mbUpdate && mMissedCall > 0) {
			mSPUtil.setCallogStatus(1);
			resetViewTextSize(mTvMissCall, mMissedCall);
			mTvMissCall.setVisibility(View.VISIBLE);
		} else {
			mSPUtil.setCallogStatus(0);
			mTvMissCall.setVisibility(View.INVISIBLE);
		}
	}

	private void updateMissedCallView() {
		if (mSPUtil.getMmsStatus() == 0) {
			updateMissedCallView(true);
		} else {
			updateMissedCallView(true);
		}
	}

	private void updateMissedMmsView(boolean mbUpdate) {
		Wind.Log(TAG, "updateMissedView Mms mbUpdate=" + mbUpdate
				+ " mMissedMms =" + mMissedMms);
		if (mbUpdate && mMissedMms > 0) {
			mSPUtil.setMmsStatus(1);
			resetViewTextSize(mTvMissMms, mMissedMms);
			mTvMissMms.setVisibility(View.VISIBLE);
		} else {
			mSPUtil.setMmsStatus(0);
			mTvMissMms.setVisibility(View.INVISIBLE);
		}
	}

	private void updateMissedMmsView() {
		if (mSPUtil.getMmsStatus() == 0) {
			updateMissedMmsView(true);
		} else {
			updateMissedMmsView(true);
		}
	}

	public void updateMissedCallView(int nMissedCall) {
		mMissedCall = nMissedCall;
		updateMissedCallView(true);
	}

	public void updateMissedMmsView(int nMissedMms) {
		mMissedMms = nMissedMms;
		updateMissedMmsView(true);
	}

}
