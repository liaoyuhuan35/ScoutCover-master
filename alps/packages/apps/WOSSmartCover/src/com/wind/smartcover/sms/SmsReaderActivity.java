package com.wind.smartcover.sms;

import java.util.List;

import com.wind.smartcover.EActivity;
import com.wind.smartcover.PubDefs;
import com.wind.smartcover.R;
import com.wind.smartcover.WindApp;
import com.wind.smartcover.phone.GetPeopleInfo;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SmsReaderActivity extends EActivity {
	private static final String TAG = "SmsReaderActivity";

	private Button mBtnBack;
	private Button mBtnPreMsg;
	private Button mBtnNextMsg;

	private SmsUtil mSmsUtil = null;
	private List<SmsInfo> mListInfo;

	private TextView mMsgUser;
	private TextView mMsgData;
	private TextView mTvCur;

	private LinearLayout mEmptySms;

	private Context mContext;
	private int mCur = 0;
	private int mMaxCount = 0;

	private static final int DO_NOTHING = 0;
	private static final int DO_NEXT = 1;
	private static final int DO_PRE = 2;

	private GetPeopleInfo mGPI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		WindApp.getInstance().addActivity(SmsReaderActivity.this);
		setContentView(R.layout.activity_wind_sms_reader);
		hideNavigation();

		if (!checkHallStatus(mContext)) {
			this.exitThisActivity();
			return;
		}
		mGPI = new GetPeopleInfo();

		mBtnBack = (Button) findViewById(R.id.btn_back);
		mBtnBack.setOnClickListener(mClickListener);
		mBtnPreMsg = (Button) findViewById(R.id.btn_msg_pre);
		mBtnPreMsg.setOnClickListener(mClickListener);
		mBtnNextMsg = (Button) findViewById(R.id.btn_msg_next);
		mBtnNextMsg.setOnClickListener(mClickListener);
		mBtnBack.setBackgroundResource(R.drawable.btn_back_bg);
		mBtnPreMsg.setBackgroundResource(R.drawable.btn_pre);
		mBtnNextMsg.setBackgroundResource(R.drawable.btn_next);

		mMsgData = (TextView) findViewById(R.id.tv_msg_info);
		mMsgUser = (TextView) findViewById(R.id.tv_msg_name);
		mTvCur = (TextView) findViewById(R.id.tv_cur);
		mMsgData.setMovementMethod(ScrollingMovementMethod.getInstance());

		mEmptySms = (LinearLayout) findViewById(R.id.linear_sms_empty);

		mSmsUtil = SmsUtil.getInstance(mContext.getApplicationContext());
		mListInfo = mSmsUtil.getSmsData();
		mCur = 0;
		updateViews(DO_NOTHING);
		updateBgs();
	}

	private void updateViews(int nOpreate) {
		mMsgData.scrollTo(0, 0);
		mMaxCount = mListInfo.size();
		switch (mMaxCount) {
		case 0: {
			mBtnPreMsg.setVisibility(View.INVISIBLE);
			mBtnNextMsg.setVisibility(View.INVISIBLE);
			mBtnPreMsg.setClickable(false);
			mBtnNextMsg.setClickable(false);
			mEmptySms.setVisibility(View.VISIBLE);
			mMsgData.setVisibility(View.INVISIBLE);
			mMsgUser.setText(R.string.unread_msg);
			mTvCur.setText(R.string.null_string);
		}
			break;
		case 1: {
			mBtnPreMsg.setVisibility(View.VISIBLE);
			mBtnNextMsg.setVisibility(View.VISIBLE);
			mBtnPreMsg.setClickable(false);
			mBtnNextMsg.setClickable(false);
			mEmptySms.setVisibility(View.INVISIBLE);
			mMsgData.setVisibility(View.VISIBLE);

			String strNum = mListInfo.get(mCur).getName();
			mGPI.QueryPeopleInfo(mContext, strNum);
			mMsgUser.setText(mGPI.getPhoneName());
			mMsgData.setText(mListInfo.get(mCur).getMsgSnippet());
			mTvCur.setText((mCur + 1) + "/" + mListInfo.size());
		}
			break;
		default: {
			mBtnPreMsg.setVisibility(View.VISIBLE);
			mBtnNextMsg.setVisibility(View.VISIBLE);
			if (mCur < mMaxCount - 1 && (DO_NEXT == nOpreate)) {
				mCur++;
			} else if (mCur > 0 && (DO_PRE == nOpreate)) {
				mCur--;
			}
			updateBgs();
		}
			break;
		}
	}

	private void updateBgs(int nCur, int nCount) {
		// butn pre
		if (nCur == 0) {
			mBtnPreMsg.setEnabled(false);
		} else {
			mBtnPreMsg.setEnabled(true);
		}
		// butn next
		if (nCount - 1 == nCur) {
			mBtnNextMsg.setEnabled(false);
		} else {
			mBtnNextMsg.setEnabled(true);
		}
		// user & data
		if (nCount > 0) {
			mMsgData.setText(mListInfo.get(mCur).getMsgSnippet());
			String strNum = mListInfo.get(mCur).getName();
			mGPI.QueryPeopleInfo(mContext, strNum);
			mMsgUser.setText(mGPI.getPhoneName());
			mTvCur.setText((mCur + 1) + "/" + mListInfo.size());
			mEmptySms.setVisibility(View.INVISIBLE);
			mMsgData.setVisibility(View.VISIBLE);
		} else {
			mMsgData.setText(R.string.null_string);
			mEmptySms.setVisibility(View.VISIBLE);
			mMsgData.setVisibility(View.INVISIBLE);
			mMsgUser.setText(R.string.unread_msg);
			mTvCur.setText(R.string.null_string);
		}
	}

	private void updateBgs() {
		mMaxCount = mListInfo.size();
		updateBgs(mCur, mMaxCount);
	}

	public OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.btn_back: {
				// mSmsUtil.resetSmsReaded();
				exitThisActivity();
			}
				break;
			case R.id.btn_msg_next: {
				updateViews(DO_NEXT);
			}
				break;
			case R.id.btn_msg_pre: {
				updateViews(DO_PRE);
			}
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		mListInfo = null;
		mSmsUtil = null;
		WindApp.getInstance().removeActivity(SmsReaderActivity.this);
		super.onDestroy();
	};
}
