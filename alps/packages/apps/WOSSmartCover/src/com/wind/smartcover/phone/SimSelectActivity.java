package com.wind.smartcover.phone;

import com.wind.smartcover.EActivity;
import com.wind.smartcover.PubDefs;
import com.wind.smartcover.R;
import com.wind.smartcover.WindApp;
import com.wind.smartcover.Util.Wind;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class SimSelectActivity extends EActivity {
	private static final String TAG = "SimSelectActivity";

	private ImageButton mSimCard0; // 0
	private ImageButton mSimCard1; // 1
	private Button mExit;

	private Context mContext;
	private String mDialNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		WindApp.getInstance().addActivity(SimSelectActivity.this);
		setContentView(R.layout.activity_select_simcard);
		hideNavigation();

		if (!checkHallStatus(mContext)) {
			this.exitThisActivity();
			return;
		}
		mDialNum = this.getIntent().getStringExtra(PubDefs.WOS_CALL_NUM);
		initButtons();
	}

	private void initButtons() {
		mSimCard0 = (ImageButton) findViewById(R.id.btn_select_sim1);
		mSimCard1 = (ImageButton) findViewById(R.id.btn_select_sim2);
		mExit = (Button) findViewById(R.id.btn_back);
		mSimCard0.setOnClickListener(mClickListener);
		mSimCard1.setOnClickListener(mClickListener);
		mExit.setOnClickListener(mClickListener);
	}

	private void setSimCard(int which) {
		Settings.System.putInt(mContext.getContentResolver(),
				PubDefs.SYS_HALL_SIMCARD, which);
	}

	public OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.btn_select_sim1: {
				setSimCard(0);
				Dialer(mDialNum);
			}
				break;
			case R.id.btn_select_sim2: {
				setSimCard(1);
				Dialer(mDialNum);
			}
				break;
			case R.id.btn_back: {
			}
				break;
			default:
				break;
			}
			exitThisActivity();
		}
	};

	private void Dialer(String mCallNum) {
		Wind.Log(TAG, "Dialer mCallNum = " + mCallNum);
		if (null == mCallNum) {
			return;
		}

		if (PhoneNumberUtils.isEmergencyNumber(mCallNum)) {
			Intent cIntent = new Intent(
					"android.intent.action.CALL_PRIVILEGED", Uri.parse("tel:"
							+ mCallNum));
			cIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			mContext.startActivity(cIntent);
		} else {
			Intent cIntent = new Intent("android.intent.action.CALL",
					Uri.parse("tel:" + mCallNum));
			cIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			mContext.startActivity(cIntent);
		}
	}

	@Override
	protected void onDestroy() {
		WindApp.getInstance().removeActivity(SimSelectActivity.this);
		super.onDestroy();
	};
}
