package com.wind.smartcover;

import java.util.ArrayList;

import com.wind.smartcover.R;
import com.wind.smartcover.Util.Wind;
import com.wind.smartcover.clock.HallClock;
import com.wind.smartcover.settings.CircleLayout;
import com.wind.util.BaiduStatWrapper;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainHallActivity extends EActivity {
	private static final String TAG = "MainHallActivity";

	private final static int MSG_OPEN_ENDED = 0xaa20;
	private final static int MSG_START_WHITE_ANIM = 0xaa21;
    private final static int MSG_REMOVE_PREVIEW     = 0022;
	private final static int MSG_SHOW_CLOCK_VIEW = 00010000;
	private final static int MSG_HIDE_CLOCK_VIEW = 00010001;
	private Context mContext = null;

	private ViewPager mMainPager = null;
	private ArrayList<View> mPageList;
	private HallClock mHallClock = null;
	private View mClockView = null;
	private View mSettingsView = null;
	private CircleLayout mSettingsL = null;

	private LightController mLightContr;
	private TextView mTvMissCall;
	private TextView mTvMissMms;

	private BroadcastReceiver mMainHallReceiver = null;

	private SharedPreferenceUtil mSPUtil = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Wind.Log(TAG, "onCreate()");
		// A: zhangxutong@wind-mobi.com 2015 10 27 begin for baidutongji sdk
		BaiduStatWrapper.init(this);
		// A: zhangxutong@wind-mobi.com 2015 10 27 end

		mContext = this;

		WindApp.getInstance().addActivity(MainHallActivity.this);
		topView();
		hideStatusBar();
		setContentView(R.layout.main_hall_activity);
		hideNavigation();
		removeWOSLockScreen();

		mMainPager = (ViewPager) findViewById(R.id.main_pager);
		LayoutInflater inflater = getLayoutInflater();
		mClockView = inflater.inflate(R.layout.wind_hall_clock_view, null);
		mSettingsView = inflater.inflate(R.layout.activity_wind_hall_settings,
				null);
		mSettingsL = (CircleLayout) mSettingsView
				.findViewById(R.id.layout_circlesettings);

		mPageList = new ArrayList<View>();
		mPageList.add(mClockView);
		mPageList.add(mSettingsView);
		mMainPager.setAdapter(mPagerAdapter);
		mMainPager.setCurrentItem(0);
		mMainPager.setOnPageChangeListener(pageChangeListener);

		mSPUtil = SharedPreferenceUtil.getInstance(mContext);

		mHallClock = (HallClock) mClockView.findViewById(R.id.main_hall_clock);
		mHallClock.regeistMsgs();

		mTvMissCall = (TextView) mSettingsView.findViewById(R.id.tv_dialer);
		mTvMissMms = (TextView) mSettingsView.findViewById(R.id.tv_sms);

		initContent();

		mMainHallReceiver = new MainHallReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(PubDefs.WOS_RESET_CLOCK);
		filter.addAction(PubDefs.WOS_NULL_LIGHT);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(mMainHallReceiver, filter);

		if (!checkHallStatus(mContext)) {
			this.exitThisActivity();
			return;
		}
		mConnectionService = new ConnectionService();
		bindService(mConnectionService);

		mKeyguardManager = (KeyguardManager) mContext
				.getSystemService(Context.KEYGUARD_SERVICE);
		// if(PubDefs.isReplaceSystemLock(mContext)){
		if (!isKeyguardLocked()) {
			mHandler.sendEmptyMessageDelayed(MSG_START_WHITE_ANIM, 300);
		} else {
			mHandler.sendEmptyMessageDelayed(MSG_START_WHITE_ANIM, 1500);
		}
		getWindow().getDecorView().post(new Runnable(){
		    @Override
		    public void run() {
		        mHandler.post(mCheckHallStatus);
		    }
		});
	}
	
	private void checkHallStatus(){
        Wind.Log(TAG, "checkHallStatus()");
        if (!checkHallStatus(mContext)) {
            Wind.Log(TAG, "checkHallStatus() exitThisActivity");
            this.exitThisActivity();
        }
	}
	
	private Runnable mCheckHallStatus = new Runnable(){
        @Override
	    public void run() {
	        checkHallStatus();
	    }
	};

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Wind.Log(TAG, "onStart()");
		// if(mSPUtil.getCallogStatus() !=0){
		updateMissedCallView();
		// }
		// if(mSPUtil.getMmsStatus()!=0){
		updateMissedMmsView();
		// }
	}

	@Override
	protected void onResume() {
		super.onResume();
		Wind.Log(TAG, "onResume()");
		setStatusbarTransparent();
		hideNavigation();
		mHandler.sendEmptyMessageDelayed(MSG_SHOW_CLOCK_VIEW, 200);
//		new Thread(new flashControlOpenThread()).start();
		flashControlOpenLM();
		// A: zhangxutong@wind-mobi.com 2015 10 27 begin for baidutongji sdk
		BaiduStatWrapper.onResume(this);
		// A: zhangxutong@wind-mobi.com 2015 10 27 end
		
	} 

	@Override
	protected void onPause() {
		super.onPause();
		Wind.Log(TAG, "onPause()");
		mHandler.removeMessages(MSG_SHOW_CLOCK_VIEW);
		if (mHallClock != null)
			mHallClock.setVisibility(View.INVISIBLE);
//		new Thread(new flashControlCloseThread()).start();
		flashControlCloseLM();
		// A: zhangxutong@wind-mobi.com 2015 10 27 begin for baidutongji sdk
		BaiduStatWrapper.onPause(this);
		// A: zhangxutong@wind-mobi.com 2015 10 27 end

	}

	private KeyguardManager mKeyguardManager;

	private boolean isKeyguardLocked() {
		Wind.Log(TAG, "isKeyguardLocked  mKeyguardManager=" + mKeyguardManager);
		boolean isKeyguardLocked = false;
		mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
		Wind.Log(TAG, "isKeyguardLocked  mKeyguardManager=" + mKeyguardManager);
		if (mKeyguardManager == null) {
			return true;
		}
		
		try {
			isKeyguardLocked = mKeyguardManager.inKeyguardRestrictedInputMode();
		} catch (NullPointerException e) {
			Wind.Log(TAG, "isKeyguardLocked  " + e.toString()
					+ " mKeyguardManager=" + mKeyguardManager + " mContext="
					+ mContext);
			isKeyguardLocked = true;
		}
		return isKeyguardLocked;
	}

	private void removeWOSLockScreen() {
		Wind.Log(TAG, "removeWOSLockScreen  ");
		Intent lockIntent = new Intent("com.wos.action.lockscreen_locked");
		lockIntent.putExtra("unlock", true);
		mContext.sendBroadcast(lockIntent);
	}

	public void resetToClockPage() {
		mMainPager.setCurrentItem(0);
		mHallClock.startAnim();
	}

	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {
		@Override
		public void onPageSelected(int cur) {
			if (cur == 1) {
//				new Thread(new flashControlOpenThread()).start();
				flashControlOpenLM();
			} else {
				if (mLightContr != null) {
					mLightContr.closeLight();
				}
				// new Thread(new flashControlCloseThread()).start();
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
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

		@Override
		public void destroyItem(View container, int position, Object object) {
			Wind.Log(TAG, "destroyItem position" + position);
			mMainPager.removeView(mPageList.get(position));
		}

		@Override
		public Object instantiateItem(View container, int position) {
			mMainPager.addView(mPageList.get(position));
			return mPageList.get(position);
		}
	};

	@Override
	protected void onDestroy() {
		Wind.Log(TAG, "onDestroy()");
		/*
		 * if(mLightContr != null){ mLightContr.closeLight();
		 * mLightContr.releaseCamera(); mLightContr = null; }
		 */
		if (mMainHallReceiver != null) {
			unregisterReceiver(mMainHallReceiver);
		}
		if (mHallClock != null)
			mHallClock.unregeistMsgs();
		unregiestContent();
        if (mConnectionService != null){
            unbindService(mConnectionService);
        }
		WindApp.getInstance().removeActivity(MainHallActivity.this);
		super.onDestroy();
	};

	/*
	 * add for sms
	 */
	// private static final Uri CONTENT_URI_SMS = Uri.parse("content://sms/");
	// private static final Uri CONTENT_URI_MMS =
	// Uri.parse("content://mms/inbox/");
	@SuppressLint("InlinedApi")
	private static final Uri CONTENT_URI_SMS = Sms.CONTENT_URI;
	@SuppressLint("InlinedApi")
	private static final Uri CONTENT_INBOX_URI_SMS = Sms.Inbox.CONTENT_URI;
	@SuppressLint("InlinedApi")
	private static final Uri CONTENT_URI_MMS = Mms.CONTENT_URI;
	private int mMissedMms = 0;
	private int mMissedCall = 0;
	private Handler mHandler;

	private ContentObserver mMmsContentObserver = null;
	private ContentObserver mCallLogContentObserver = null;

	protected void initContent() {
		mHandler = new MainHandler();
		ContentResolver cr = mContext.getContentResolver();
		mMmsContentObserver = new MmsContentObserver(mHandler);
		mCallLogContentObserver = new CallLogContentObserver(mHandler);

		cr.registerContentObserver(CONTENT_URI_SMS, true, mMmsContentObserver);
		cr.registerContentObserver(CONTENT_INBOX_URI_SMS, true,
				mMmsContentObserver);
		cr.registerContentObserver(CONTENT_URI_MMS, true, mMmsContentObserver);
		cr.registerContentObserver(CallLog.Calls.CONTENT_URI, true,
				mCallLogContentObserver);
	}

	private void unregiestContent() {
		ContentResolver cr = mContext.getContentResolver();
		if (null != mMmsContentObserver)
			cr.unregisterContentObserver(mMmsContentObserver);
		if (null != mCallLogContentObserver)
			cr.unregisterContentObserver(mCallLogContentObserver);
	}

	private int getMissedCallCount() {
		int count = 0;

		try {
			Cursor c = mContext.getContentResolver().query(
					CallLog.Calls.CONTENT_URI, new String[] { Calls.TYPE },
					Calls.TYPE + "=? and " + Calls.NEW + "=?",
					new String[] { Calls.MISSED_TYPE + "", "1" }, null);
			if (c != null) {
				count = c.getCount();
				c.close();
			}
		} catch (Exception e) {
		}

		return count;
	}

	private int getMissedMmsCount() {
		int count = 0;

		Cursor c = null;
		try {
			c = mContext.getContentResolver().query(CONTENT_URI_SMS, null,
					"type = 1 and read = 0", null, null);
			if (c != null) {
				count += c.getCount();
				c.close();
			}
			// /*
			c = mContext.getContentResolver().query(CONTENT_URI_MMS, null,
					"read = 0", null, null);
			if (c != null) {
				count += c.getCount();
				c.close();
			}
			// */
		} catch (Exception e) {
		}
		return count;
	}

	private void updateMissedCallView() {
		mMissedCall = getMissedCallCount();
		mSettingsL.updateMissedCallView(mMissedCall);
	}

	private void updateMissedMmsView() {
		mMissedMms = getMissedMmsCount();
		mSettingsL.updateMissedMmsView(mMissedMms);
	}

	private class MmsContentObserver extends ContentObserver {

		public MmsContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Wind.Log(TAG, "MmsContentObserver onChange");
			updateMissedMmsView();
		}
	}

	private class CallLogContentObserver extends ContentObserver {

		public CallLogContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Wind.Log(TAG, "CallLogContentObserver onChange");
			updateMissedCallView();
		}
	}

	class flashControlOpenThread implements Runnable {
		@Override
		public void run() {
			Wind.Log(TAG, "flashControlOpenThread mLightContr=" + mLightContr);
			flashControlOpen();
		}
	}

	protected void flashControlOpenLM(){
		if(Wind.IS_SYS_M){
			if (mLightContr == null) {
				mLightContr = LightController.getInstance(mContext);
				mLightContr.openCamera();
	            mSettingsL.setmLightController(mLightContr);
			}
		}else{
			new Thread(new flashControlOpenThread()).start();
		}
	}

	protected void flashControlCloseLM(){
		new Thread(new flashControlCloseThread()).start();
	}

	protected void flashControlOpen() {
		Wind.Log(TAG, "flashControlOpen mLightContr=" + mLightContr);
		if (mLightContr == null) {
			mLightContr = LightController.getInstance(mContext);
		}
		mLightContr.openCamera();
		mHandler.removeMessages(MSG_OPEN_ENDED);
		mHandler.sendEmptyMessage(MSG_OPEN_ENDED);
	}

	protected void flashControlClose() {
		Wind.Log(TAG, "flashControlClose mLightContr=" + mLightContr);
		if (mLightContr != null) {
			mLightContr.closeLight();
			mLightContr.releaseCamera();
			mLightContr = null;
		}
	}

	class flashControlCloseThread implements Runnable {
		@Override
		public void run() {
			Wind.Log(TAG, "flashControlCloseThread mLightContr=" + mLightContr);
			flashControlClose();
		}
	}

	private class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
            Wind.Log(TAG, "MainHandler handleMessage=" + msg.toString());
            if (msg.what == MSG_OPEN_ENDED) {
                mSettingsL.setmLightController(mLightContr);
            } else if (msg.what == MSG_START_WHITE_ANIM) {
                startClockWhiteAnim();
            } else if (msg.what == MSG_SHOW_CLOCK_VIEW) {
                if (mHallClock != null)
                    mHallClock.setVisibility(View.VISIBLE);
            } else if (msg.what == MSG_REMOVE_PREVIEW) {
                if (mWindHallService != null) {
                    mWindHallService.removePreView();
                }
            }
		}
	}

	private void startClockWhiteAnim() {
		Wind.Log(TAG, "startClockWhiteAnim mHallClock=" + mHallClock);
		if (mHallClock != null) {
			mHallClock.setVisibility(View.VISIBLE);
			mHallClock.startAnim();
		}
	}

	private class MainHallReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Wind.Log(TAG, "MainHallReceiver action=" + action);
			if (PubDefs.WOS_RESET_CLOCK.equals(action)) {
				resetToClockPage();
			} else if (PubDefs.WOS_NULL_LIGHT.equals(action)) {
				flashControlOpenLM();
			} else if (Intent.ACTION_SCREEN_ON.equals(action)) {
				// if (!isKeyguardLocked()) {
				// mHandler.sendEmptyMessageDelayed(MSG_START_WHITE_ANIM, 300);
				// } else {
				mHandler.sendEmptyMessageDelayed(MSG_START_WHITE_ANIM, 1200);
				// }
			}
		}
	}

    private WindHallService mWindHallService;
    private ConnectionService mConnectionService;

    class ConnectionService implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Wind.Log(TAG, "onServiceConnected " + name);
            if (name.getShortClassName().endsWith("WindHallService") ) {
                try {
                    mWindHallService = ((WindHallService.ActivityBinder) binder)
                            .getService();

                    Wind.Log(TAG, "onServiceConnected() mWindHallService="+mWindHallService + " time="+android.os.SystemClock.currentThreadTimeMillis());
                    if (mWindHallService != null) {
                        if (!isKeyguardLocked()) {
                            mWindHallService.removePreView();
                        } else {
                            mHandler.sendEmptyMessageDelayed( MSG_REMOVE_PREVIEW, 500);
                        }
                    }
                } catch (Exception e) {
                    Wind.Log(TAG, "onServiceConnected " + e.toString());
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Wind.Log(TAG, "onServiceDisconnected " + name);

        }
    }

    private void bindService(ServiceConnection connection) {
        Wind.Log(TAG, "bindService " +android.os.SystemClock.currentThreadTimeMillis());
        Intent intent = new Intent();
        intent.setClass(this, WindHallService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
}
