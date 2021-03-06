package com.wind.smartcover.phone;

import com.wind.smartcover.PhoneState;
import com.wind.smartcover.R;
import com.wind.smartcover.Util.Wind;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("ClickableViewAccessibility")
public class ClickView extends RelativeLayout {
	private final static String TAG = "ClickView";

	private Context mContext;

	private ImageView mHeadImg;
	private TextView mName;
	private TextView mStatus;
	private Chronometer mTime;

	private ImageButton mHangOn;
	private ImageButton mHangOff;
	private ImageButton mEndCall;
	private GetPeopleInfo mGPI;

	private int mPhoneStatus;
	private boolean mStarted = false;

	public ClickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mGPI = new GetPeopleInfo();
	}

	@Override
	protected void onFinishInflate() {
		mHeadImg = (ImageView) findViewById(R.id.img_head);

		mHangOn = (ImageButton) findViewById(R.id.btn_hang_on);
		mHangOff = (ImageButton) findViewById(R.id.btn_hang_off);
		mEndCall = (ImageButton) findViewById(R.id.btn_end_call);

		mName = (TextView) findViewById(R.id.tv_name);
		mStatus = (TextView) findViewById(R.id.tv_status);
		mTime = (Chronometer) findViewById(R.id.tv_time);

		mHangOff.setOnClickListener(ClickListener);
		mHangOn.setOnClickListener(ClickListener);
		mEndCall.setOnClickListener(ClickListener);

		showDefault();
	};

	public void updateInfos(String Num) {
		mGPI.QueryPeopleInfo(mContext, Num);
		Wind.Log(TAG, "updateInfos Num=" + Num);
		Uri imgUri = mGPI.getPhotoUri();
		if (imgUri != null)
			mHeadImg.setImageURI(imgUri);
		else
			mHeadImg.setImageResource(R.drawable.head_img);
		// mHeadImg.setBackgroundResource(R.drawable.head_img);

		if (mGPI.getPhoneName() != null) {
			mName.setText(mGPI.getPhoneName());
		} else
			mName.setText("");
	}

	public void startCallTimer() {
		Wind.Log(TAG, "startCallTimer mStarted=" + mStarted);
		if (mStarted)
			return;

		mStarted = true;
		mTime.setBase(SystemClock.elapsedRealtime());
		mTime.start();
	}

	private void cancelCallTimer() {
		mStarted = false;
		Wind.Log(TAG, "cancelCallTimer ");
		mTime.stop();
	}

	public void updatePhoneState(int nstate) {
		Wind.Log(TAG, "updatePhoneState  nstate=" + nstate);

		if (mPhoneStatus == PhoneState.CALL_STATE_PICKUP) {
			if (nstate == PhoneState.CALL_STATE_RINGING)
				return;
		}
		mPhoneStatus = nstate;

		switch (nstate) {
		// nothing
		case PhoneState.CALL_STATE_IDLE:
			showCallEnded();
			cancelCallTimer();
			mTime.setText("");
			break;
		case PhoneState.CALL_STATE_CALLING: {
			showDialing();
		}
			break;
		case PhoneState.CALL_STATE_OFFHOOK:
			startCallTimer();
			showOnHold();
			break;
		case PhoneState.CALL_STATE_RINGING:
			showIncomming();
			break;
		case PhoneState.CALL_STATE_PICKUP:
			showHolding();
			break;
		default:
			showDefault();
			cancelCallTimer();
			break;
		}
		invalidate();
	}

	protected void showIncomming() {
		mHangOn.setVisibility(View.VISIBLE);
		mHangOff.setVisibility(View.VISIBLE);
		mEndCall.setVisibility(View.GONE);
		mStatus.setText(R.string.phone_incomming);
		mTime.setText(R.string.null_string);
	}

	protected void showHolding() {
		mHangOn.setVisibility(View.GONE);
		mHangOff.setVisibility(View.GONE);
		mEndCall.setVisibility(View.VISIBLE);
		mStatus.setText(R.string.phone_holding);
		mTime.setText(R.string.null_string);
	}

	protected void showDialing() {
		mHangOn.setVisibility(View.GONE);
		mHangOff.setVisibility(View.GONE);
		mEndCall.setVisibility(View.VISIBLE);
		mEndCall.setEnabled(true);
		mStatus.setText(R.string.phone_dialing);
		mTime.setText(R.string.null_string);
	}

	protected void showOnHold() {
		mHangOn.setVisibility(View.GONE);
		mHangOff.setVisibility(View.GONE);
		mEndCall.setVisibility(View.VISIBLE);
		mEndCall.setEnabled(true);
		mStatus.setText(R.string.phone_onhold);
	}

	protected void showCallEnded() {
		mHangOn.setVisibility(View.GONE);
		mHangOff.setVisibility(View.GONE);
		mEndCall.setVisibility(View.VISIBLE);
		mEndCall.setEnabled(false);
		mStatus.setText(R.string.phone_callended);
	}

	protected void showDefault() {
		mHangOn.setVisibility(View.GONE);
		mHangOff.setVisibility(View.GONE);
		mEndCall.setVisibility(View.GONE);
		mStatus.setText(R.string.null_string);
		mTime.setText(R.string.null_string);
	}

	// private static final String WOS_HEANDSUP_REJECT = "wos.headsup.reject";
	// private static final String WOS_HEANDSUP_ENDED = "wos.headsup.ended";
	private static final String WOS_HEANDSUP_ANWSER = "wos.headsup.anwser";
	private static final String WOS_UI_ANWSER = "wos.ui.anwser";
	private static final String WOS_UI_ENDED = "wos.ui.ended";

	private void sendBroadcast(String strAction) {
		Wind.Log(TAG, "sendBroadcast strAction=" + strAction);
		Intent hallIntent = new Intent(strAction);
		mContext.sendBroadcast(hallIntent);
	}

	private OnClickListener ClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_hang_on: {
				sendBroadcast(WOS_HEANDSUP_ANWSER);
				sendBroadcast(WOS_UI_ANWSER);
			}
				break;
			case R.id.btn_hang_off:
			case R.id.btn_end_call: {
				sendBroadcast(WOS_UI_ENDED);
			}
				break;
			default:
				break;
			}
		}

	};
}
