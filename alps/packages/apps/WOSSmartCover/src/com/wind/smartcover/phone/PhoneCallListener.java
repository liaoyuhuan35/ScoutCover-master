package com.wind.smartcover.phone;

import com.wind.smartcover.PubDefs;
import com.wind.smartcover.Util.Wind;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneCallListener extends PhoneStateListener {
	private static final String TAG = "PhoneCallListener";

	private Context mContext = null;

	private static final int INCOMING = 1;
	private static final int OUTGOING = 2;
	private static final int INCALL = 3;
	private static final int NOCALLS = 4;
	private int mUiType = NOCALLS;

	private int mPreStatus = TelephonyManager.CALL_STATE_IDLE;

	public PhoneCallListener(Context context) {
		mContext = context;
	}

	private String mNum = null;

	public void onCallStateChanged(int state, String incomingNumber) {
		Wind.Log(TAG, "onCallStateChanged state=" + state + " incomingNumber="
				+ incomingNumber);
		if (null != incomingNumber)
			mNum = incomingNumber;

		switch (state) {
		// nothing
		case TelephonyManager.CALL_STATE_IDLE:
		case TelephonyManager.CALL_STATE_OFFHOOK:
		case TelephonyManager.CALL_STATE_RINGING:
			updateUi(state);
			break;
		default:
			break;
		}

		mPreStatus = state;
		super.onCallStateChanged(state, incomingNumber);
	}

	private void updateUi(int status) {

	}
}
