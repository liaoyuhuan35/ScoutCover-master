package com.wind.smartcover.calllog;

import java.util.ArrayList;
import java.util.List;

import com.wind.smartcover.EActivity;
import com.wind.smartcover.R;
import com.wind.smartcover.WindApp;
import com.wind.smartcover.Util.Wind;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract.PhoneLookup;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

public class CallLogActivity extends EActivity {
	private static final String TAG = "CallLogActivity";
	private Context mContext = null;

	private Button mBtnBack = null;

	private List<QueryDate> mList = null;
	private ListAdapter mAdapter = null;
	private ListView mListView = null;
	private LinearLayout mEmpty = null;

	private String[] mQuery = new String[] { PhoneLookup.NUMBER, "name",
			"date", PhoneLookup.TYPE, };

	private ContentObserver mCalllogContentObserver;

	private final static int MAX_LINES = 20;

	private int getMin(int ncur) {
		if (ncur >= MAX_LINES) {
			return MAX_LINES;
		} else {
			return ncur;
		}
	}

	protected class queryCallLogThread implements Runnable {
		@Override
		public void run() {
			queryCallLog();
		}
	}

	void queryCallLog() {
		Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI,
				mQuery, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
		if (cursor != null) {
			mList.clear();
			for (int i = 0; i < cursor.getCount(); i++) {
				QueryDate item = new QueryDate();
				cursor.moveToPosition(i);
				item.Num = cursor.getString(0);
				item.Name = cursor.getString(1);
				item.Date = cursor.getString(2);
				item.Type = cursor.getString(3);
				mList.add(item);
			}
			cursor.close();

			if (mList.size() > 0)
				sendDelayMsg(MSG_QUEY_ENDED, 10);
			else
				mHandler.sendEmptyMessage(MSG_NO_MISSED_CALLS);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindApp.getInstance().addActivity(CallLogActivity.this);
		setContentView(R.layout.activity_call_log);
		hideNavigation();
		mContext = this;
		mList = new ArrayList<QueryDate>();

		mListView = (ListView) findViewById(R.id.CallList);
		mEmpty = (LinearLayout) findViewById(R.id.linear_callog_empty);
		mEmpty.setVisibility(View.INVISIBLE);
		mAdapter = new ListAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		// mListView.setOnItemClickListener(itemClick);

		mBtnBack = (Button) findViewById(R.id.btn_back);
		mBtnBack.setOnClickListener(mClickListener);

		if (!checkHallStatus(mContext)) {
			this.exitThisActivity();
			return;
		}
		mCalllogContentObserver = new CalllogContentObserver(this, mHandler);
		ContentResolver cr = mContext.getContentResolver();
		cr.registerContentObserver(CallLog.CONTENT_URI, true,
				mCalllogContentObserver);
		sendDelayMsg(MSG_QUEY_CALLLOG, 10);
	}

	private final static int MSG_QUEY_CALLLOG = 0xaa10;
	private final static int MSG_QUEY_ENDED = 0xaa11;
	private final static int MSG_NO_MISSED_CALLS = 0xaa12;

	private void sendDelayMsg(int what, int ndelay) {
		Message mess = new Message();
		mess.what = what;
		mHandler.sendMessageDelayed(mess, ndelay);
	}

	public OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.btn_back: {
				exitThisActivity();
			}
				break;
			default:
				break;
			}
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_QUEY_CALLLOG) {
				new Thread(new queryCallLogThread()).start();
			} else if (msg.what == MSG_QUEY_ENDED) {
				mListView.setVisibility(View.VISIBLE);
				mEmpty.setVisibility(View.INVISIBLE);
				mAdapter.notifyDataSetChanged();
			} else if (msg.what == MSG_NO_MISSED_CALLS) {
				mListView.setVisibility(View.INVISIBLE);
				mEmpty.setVisibility(View.VISIBLE);
			}
		}
	};

	private class CalllogContentObserver extends ContentObserver {
		private Context mContext;

		// private Handler mHandler;

		public CalllogContentObserver(Context context, Handler handler) {
			super(handler);
			mContext = context;
			// mHandler = handler;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Wind.Log(TAG, "HallContentObserver onChange ");
			sendDelayMsg(MSG_QUEY_CALLLOG, 10);

			// updateHallStatus();
		}
	}

	@Override
	protected void onDestroy() {
		mList = null;
		WindApp.getInstance().removeActivity(CallLogActivity.this);
		ContentResolver cr = this.getContentResolver();
		cr.unregisterContentObserver(mCalllogContentObserver);
		super.onDestroy();
	};
}
