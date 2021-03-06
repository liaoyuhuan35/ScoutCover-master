package com.wind.smartcover;

import java.lang.reflect.InvocationTargetException;

import com.wind.smartcover.Util.HallUtil;
import com.wind.smartcover.Util.Wind;
import com.wind.smartcover.phone.ClickView;
import com.wind.smartcover.phone.PhoneUtil;
import com.wind.smartcover.phone.SimSelectActivity;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.app.StatusBarManager;

import android.os.UserHandle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.view.IWindowManager;

import com.android.internal.widget.LockPatternUtils;
import android.content.ServiceConnection;
import com.android.internal.policy.IKeyguardService;

public class WindHallService extends Service implements Handler.Callback {
	private static final String TAG = "WindHallService";
	private static final String LOCK_TAG = "lock/WindHallService";

	// MSG:hall on/off and
	// private static final int WHAT_STATUS_CHANGEED = 0x6688;
	private static final int WHAT_STATUS_CHANGEDON = 0xaa01;
	private static final int WHAT_STATUS_CHANGEDOFF = 0xaa02;
    private final static int MSG_REMOVE_PREVIEW     = 0022;

	// MSG:enable/disable keyguard
	private static final String KEYGUARD_HALL_ENABLED = "android.intent.action.KEYGUARD_HALL_ENABLED";
	private static final String KEYGUARD_HALL_DISABLED = "android.intent.action.KEYGUARD_HALL_DISABLED";

	private boolean isFirstStart = false;

	private Context mContext;
	private Handler mHandler;
	private BroadcastReceiver mOutgoingCallReceiver;
	private ContentObserver mHallContentObserver;

	private PowerManager mPm;

	// Keyguard
	private KeyguardManager mKeyManager;
	private LockPatternUtils mLockPatternUtils;
	private KeyguardLock mKeyLock = null;

	// StatusBar
	private StatusBarManager mStatusBarManager = null;

	private static final int PROJECT_SIM_NUM = android.telephony.TelephonyManager.getDefault()
			.getSimCount();

	/********************************************************************/

	private static final String WOS_PHONE_NUMBER = "wos.phone.number"; // phone
																		// status
	private static final String WOS_HEANDSUP_NUMBER = "wos.headsup.num"; // headup
																			// incomming
	private static final String WOS_HEANDSUP_STATE = "wos.headsup.state"; // headup
																			// status
	private static final String WOS_HEANDSUP_ANWSER = "wos.headsup.anwser"; // headup

	private static final String WOS_UI_ANWSER = "wos.ui.anwser";
	private static final String WOS_UI_REJECT = "wos.ui.reject";
	private static final String WOS_UI_ENDED = "wos.ui.ended";

	private static final int WOS_REMOVE_PHONE	= 0000100;
	private static final int WOS_SCREEN_OFF		= 0000101;
    private static final int WOS_SCREEN_ON     = 0000102;
	// private static final int WOS_UPDATE_LOCK_STATE = 0001000;
	// private static final int WOS_CHECK_LOCK_STATE = 0001002;
	// private static final int WOS_CHECK_UNLOCK_STATE = 0001003;
	// private static final int WOS_UNLOCK = 0xa101;;

	private WindowManager mWindowManager;
	private RelativeLayout mIncallui;
    private RelativeLayout mPreview;
	private LayoutParams mParams;
	private ClickView mClickView;
	private boolean mAdded = false;
	private boolean mHallOn = false;
	
	//在霍尔界面接听
	private boolean mHoldingInHall = false;

    @Override
    public IBinder onBind(Intent intent) {
        IBinder result = null;
        if (null == result)
            result = new ActivityBinder();
        Wind.Log(TAG, "onBind " + result);
        return result;
    }

    public class ActivityBinder extends Binder {
        public WindHallService getService() {
            Wind.Log(TAG, "getService WindHallService");
            return WindHallService.this;
        }
    }

	private void initHallState() {
		int state = HallState.getHallState();
		Wind.Log(TAG, "initHallState Hall State: " + state);
		if (state != 1) {
			state = 0;
		}

		setHallStatus(state);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// initHallState();
		return super.onStartCommand(intent, flags, startId);
	}
	
    protected boolean mPreViewAdded = false;

    protected void addPreView() {
        Wind.Log(TAG, "addPreView ");
        if (!mAdded && !mPreViewAdded && isHallOn()) {
            Wind.Log(TAG, "addPreView in");
            mWindowManager.addView(mPreview, mParams);
            mPreViewAdded = true;

            mHandler.sendEmptyMessageDelayed(MSG_REMOVE_PREVIEW, 3000);
        }
    }

    protected void removePreView() {
        Wind.Log(TAG, "removePreView");
        mHandler.removeMessages(MSG_REMOVE_PREVIEW);
        if (mPreViewAdded) {
            Wind.Log(TAG, "removePreView in");
            mPreViewAdded = false;
            mWindowManager.removeView(mPreview);
        }
    }

	private void addPhoneView() {
		int status = getHallStatus();
		Wind.Log(TAG, "addPhoneView mAdded=" + mAdded + " status=" + status);
		if (!mAdded && isHallOn() && mPhoneStatus != PhoneState.CALL_STATE_IDLE) {
		    removePreView();
		    
            PhoneUtil.wosTurnOffProximitySensor(mContext);
			mWindowManager.addView(mIncallui, mParams);
			mAdded = true;
			//sendScreenOnMsg();
		}
		sendScreenOffMsg();
	}

	//非来电状态  s灭屏
	protected void sendScreenOffMsg(){
		Wind.Log(TAG, "sendScreenOffMsg mPhoneStatus="+mPhoneStatus);
		mHandler.removeMessages(WOS_SCREEN_OFF);
		if(isHallOn() && mAdded){
			if (PhoneState.needScreenOff(mPhoneStatus)) {
		        Wind.Log(TAG, "sendScreenOffMsg sendEmptyMessageDelayed WOS_SCREEN_OFF");
				mHandler.sendEmptyMessageDelayed(WOS_SCREEN_OFF, 5000);
			}
		}
	}
    protected void sendScreenOnMsg(){
        Wind.Log(TAG, "sendScreenOnMsg");
        mHandler.removeMessages(WOS_SCREEN_OFF);
        mHandler.removeMessages(WOS_SCREEN_ON);
        if(isHallOn() && mAdded){
            if (PhoneState.needScreenOff(mPhoneStatus)) {
                mHandler.sendEmptyMessageDelayed(WOS_SCREEN_ON, 500);
            }
        }
    }
	

	protected void turnScreenOff() {
		Wind.Log(TAG, "Turn screen off");
		mPm.goToSleep(android.os.SystemClock.uptimeMillis(),
				android.os.PowerManager.GO_TO_SLEEP_REASON_DEVICE_ADMIN, 0);
//		PowerManager pm = (PowerManager) mContext
//				.getSystemService(Context.POWER_SERVICE);
//		pm.goToSleep(android.os.SystemClock.currentThreadTimeMillis());
	}

	// Turn screen on
	protected void turnScreenOn() {
		Wind.Log(TAG, "wakeUp Turn screen on");
		PowerManager pm = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		pm.wakeUp(android.os.SystemClock.uptimeMillis());
	}

	private void removePhoneViewDelay() {
		Wind.Log(TAG, "removePhoneViewDelay");
		mHandler.removeMessages(WOS_REMOVE_PHONE);
		Message msg = new Message();
		msg.what = WOS_REMOVE_PHONE;
		mHandler.sendMessageDelayed(msg, 4000);
	}

	private void removePhoneView() {
		mHandler.removeMessages(WOS_REMOVE_PHONE);
		Wind.Log(TAG, "removePhoneView mAdded=" + mAdded);
		if (mAdded) {
			mWindowManager.removeView(mIncallui);
			mAdded = false;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Wind.Log(TAG, "onCreate " );
		Wind.getAppInfo(this.getApplicationContext());
		isFirstStart = true;

		mHandler = new Handler(this);
		// bindService(this);

		// regeist screen of/off boradcast
		mOutgoingCallReceiver = new OutgoingCallReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(WOS_PHONE_NUMBER);
		filter.addAction(WOS_HEANDSUP_NUMBER);
		filter.addAction(WOS_HEANDSUP_STATE);
		filter.addAction(WOS_HEANDSUP_ANWSER);
		filter.addAction(WOS_UI_REJECT);
		filter.addAction(WOS_UI_ANWSER);
		filter.addAction(WOS_UI_ENDED);
		filter.addAction(PubDefs.WOS_UI_DIALER);

		registerReceiver(mOutgoingCallReceiver, filter);

		// regeist system_hall_status
		mContext = this.getApplicationContext();
		ContentResolver cr = mContext.getContentResolver();
		mHallContentObserver = new HallContentObserver(this, mHandler);
		Uri hallConfigUri = Settings.System.getUriFor(PubDefs.SYS_HALL_STATUS);
		Uri hallSwitchUri = Settings.System.getUriFor(PubDefs.SYS_HALL_SWITCH);
		cr.registerContentObserver(hallConfigUri, true, mHallContentObserver);

		mPm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		// Keyguard
		mKeyManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		mLockPatternUtils = new LockPatternUtils(mContext);
		bindService(this);

		// StatusBar
		mStatusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);

		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		
		mPreview = (RelativeLayout) LayoutInflater.from(mContext).inflate(
                R.layout.activity_preview, null);
		if(Wind.MACRO_LEFT_HANGON){
			mIncallui = (RelativeLayout) LayoutInflater.from(mContext).inflate(
					R.layout.activity_incallui2, null);
		}else{
			mIncallui = (RelativeLayout) LayoutInflater.from(mContext).inflate(
					R.layout.activity_incallui, null);
		}
		mClickView = (ClickView) mIncallui.findViewById(R.id.root);

		mParams = new WindowManager.LayoutParams();
		mParams.type = WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;
		mParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
				| LayoutParams.FLAG_NOT_FOCUSABLE
				| LayoutParams.FLAG_TRANSLUCENT_STATUS
				| LayoutParams.FLAG_FULLSCREEN
				| LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		mParams.width = LayoutParams.MATCH_PARENT;
		mParams.screenOrientation = Configuration.ORIENTATION_PORTRAIT;

		updateHallStatus();
	}

	private boolean mIsViewChanging = false;
	private boolean mPreHallStatus = false;

	private void wakeupScreen() {
		Wind.Log(TAG, "wakeUp wakeupScreen ");
		if (!mPm.isScreenOn()) {
			Wind.Log(TAG, "wakeUp wakeupScreen mPm.wakeUp");
			mPm.wakeUp(android.os.SystemClock.uptimeMillis());
		}
	}
	
	
	private void updateHallViewStatus() {
		if (mIsViewChanging)
			return;
		mIsViewChanging = true;
		mPreHallStatus = isHallOn();
		// sendUpdateLockStatusMSG();

		if (mPreHallStatus) {
			replaceKeyguard();
			addPhoneView();
			addSmartCoverView();
			disablePullStatusBar();
			if(!mAdded)
			    wakeupScreen();
		} else {
			if (isSecureKeyguard())
				enableKeyguard();
			removeWOSLockScreen();
			removeSmartCoverView();
			removePhoneView();
			enablePullStatusBar();
		}

		if (mPreHallStatus != isHallOn()) {
			sendHallMsgDelay(WHAT_STATUS_CHANGEDON, 0);
		}
		mIsViewChanging = false;
	}

	@Override
	public boolean handleMessage(Message msg) {
		Wind.Log(TAG, "handleMessage " + msg.what);
		switch (msg.what) {
		case WHAT_STATUS_CHANGEDON: {
			updateHallViewStatus();
		}
			break;
		case WHAT_STATUS_CHANGEDOFF: {
			updateHallViewStatus();
		}
			break;
		case WOS_REMOVE_PHONE: {
			removePhoneView();
			updateHallViewStatus();
		}
			break;
		case WOS_SCREEN_OFF: {
			turnScreenOff();
		}
			break;
        case WOS_SCREEN_ON: {
            turnScreenOn();
        }
            break;
        case MSG_REMOVE_PREVIEW: {
            removePreView();
        }
            break;
		// case WOS_UPDATE_LOCK_STATE:{
		// updateLockScreenStatus();
		// }break;
		// case WOS_CHECK_LOCK_STATE:{
		// checkLockStatusLocked();
		// }break;
		// case WOS_CHECK_UNLOCK_STATE:{
		// checkLockStatusUnlocked();
		// }break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		unbindService(mKeyguardConnection);
		enablePullStatusBar();

		Wind.Log(TAG, "onDestroy  ");
		// unregeist screen of/off boradcast
		ContentResolver cr = this.getContentResolver();
		cr.unregisterContentObserver(mHallContentObserver);

		// unregeist system_hall_status
		if (mOutgoingCallReceiver != null) {
			unregisterReceiver(mOutgoingCallReceiver);
		}
		// unbindService(mKeyguardConnection);
		restartService();
	}

	private boolean isHallSwitchOpen() {
		return Settings.System.getInt(mContext.getContentResolver(),
				PubDefs.SYS_HALL_SWITCH, 0) == 1;
	}

	private void restartService() {
		if (isHallSwitchOpen()) {
			Intent hallServiceIntent = new Intent(mContext,
					WindHallService.class);
			mContext.startService(hallServiceIntent);
		}
	}

	/*
	 * add/remove hall
	 */
	private static final String TOP_ACTIVITY = "com.wind.smartcover.";

	private boolean isTopActivity() {
		boolean isTop = false;
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		Wind.Log(TAG, "isTopActivity = " + cn.getClassName());
		if (cn.getClassName().contains(TOP_ACTIVITY)) {
			isTop = true;
		}
		Wind.Log(TAG, "isTop = " + isTop);
		return isTop;
	}

	private void addSmartCoverView() {
		Wind.Log(TAG, "addSmartCoverView  ");
		int status = getHallStatus();
		if (!isTopActivity()) {
			removeSmartCoverView();
		}
		if (!mHallOn && (status == 1)) {
			Intent intent = new Intent(getBaseContext(), MainHallActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mHallOn = true;
			Wind.Log(TAG, "addSmartCoverView  startMainHallActivity");
			this.getApplication().startActivity(intent);
		}

		mKeyManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		// Wind.Log(TAG, "addSmartCoverView  mbIsLocked = "+mbIsLocked);
		// if (mbIsLocked){
		// updateLockScreenStatus();
		// sendUpdateLockStatusMSG(400);
		// }
	}

	private void restartSmartCoverView() {
		Wind.Log(TAG, "addSmartCoverView  restart");
		int status = getHallStatus();
		if (status == 1) {
			Intent intent = new Intent(getBaseContext(), MainHallActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mHallOn = true;
			this.getApplication().startActivity(intent);
			// sendUpdateLockStatusMSG();
			// sendUpdateLockStatusMSG(400);
		}
	}

	private void removeSmartCoverView() {
		Wind.Log(TAG, "addSmartCoverView remove  ");
		if (mHallOn) {
			mHallOn = false;
			WindApp.getInstance().exitActivitys();
		}
		// if(!PubDefs.isReplaceSystemLock(mContext)){
		// sendUpdateLockStatusMSG(0);
		// sendUpdateLockStatusMSG(400);
		// }
	}

	/*
	 * StatusBar
	 */
	private void disablePullStatusBar() {
		if (null != mStatusBarManager)
			mStatusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
	}

	private void enablePullStatusBar() {
		if (null != mStatusBarManager)
			mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
	}

	/*
	 * enable keyguard
	 */
	private void removeWOSLockScreen() {
		Wind.Log(TAG, "removeWOSLockScreen  ");
		Intent lockIntent = new Intent("com.wos.action.lockscreen_locked");
		lockIntent.putExtra("unlock", true);
		mContext.sendBroadcast(lockIntent);
	}

	private boolean mbNewLock = false;

	@SuppressWarnings("deprecation")
	private void replaceKeyguard() {
		Wind.Log(TAG,
				"mKeyLock replaceKeyguard  " + mKeyManager.isKeyguardLocked());

		if (!mKeyManager.isKeyguardLocked())
			return;
		// removeWOSLockScreen();
		if (null == mKeyLock)
			mKeyLock = mKeyManager.newKeyguardLock("");
		mKeyLock.disableKeyguard();
		mbNewLock = true;
	}

	@SuppressWarnings("deprecation")
	private void enableKeyguard() {
		Wind.Log(TAG, "mKeyLock enableKeyguard  mbNewLock=" + mbNewLock
				+ " mKeyLock=" + mKeyLock);
		// mKeyLock = mKeyManager.newKeyguardLock("");
		if (null != mKeyLock && mbNewLock && isPhoneHangOff()) {
			mKeyLock.reenableKeyguard();
			mbNewLock = false;
		}
		startIdentifyCredentialReq();
	}
	
	private boolean isPhoneHangOff() {
		Wind.Log(TAG, "isPhoneHangOff  mPhoneStatus=" + mPhoneStatus);
		return mPhoneStatus == PhoneState.CALL_STATE_IDLE;
	}

	private boolean isSecureKeyguard() {
		Wind.Log(TAG, "mLockPatternUtils  isSecure=" + mLockPatternUtils.isSecure(UserHandle.myUserId()));
		return mLockPatternUtils.isSecure(UserHandle.myUserId());
	}

	private String mCallNum = "";
	private int mPhoneStatus = PhoneState.CALL_STATE_IDLE;
	private int mPrePhoneStatus = PhoneState.CALL_STATE_IDLE;

	private boolean resetCallNum(String num) {
		Wind.Log(TAG, "isUpdateUI  num=" + num + " mCallNum=" + mCallNum);
		if (num != null && num.length() > 0) {
			if (mCallNum == null || mCallNum.equals("") || mCallNum != num) {
				mCallNum = num;
				mPrePhoneStatus = PhoneState.CALL_STATE_IDLE;
				return true;
			}
		}
		return false;
	}

	private boolean isUpdateUI(Intent intent) {
		String num = intent.getStringExtra("num");
		Wind.Log(TAG, "isUpdateUI  mPhoneStatus=" + mPhoneStatus
				+ " mPrePhoneStatus=" + mPrePhoneStatus + " num=" + num
				+ " mCallNum=" + mCallNum);
		if (mPrePhoneStatus != mPhoneStatus) {
			if (num != null && num.length() > 0) {
				if (resetCallNum(num))
					return true;
			}
			if (mPrePhoneStatus == PhoneState.CALL_STATE_PICKUP
					&& mPhoneStatus == PhoneState.CALL_STATE_RINGING)
				return false;
			mPrePhoneStatus = mPhoneStatus;
		} else {
			return false;
		}
		return true;
	}

	private void updateUI(Intent intent) {

		boolean bRes = isUpdateUI(intent);
		Wind.Log(TAG, "isUpdateUI " + bRes + "  mPhoneStatus=" + mPhoneStatus
				+ " mPrePhoneStatus=" + mPrePhoneStatus);
		if (!bRes)
			return;

		switch (mPhoneStatus) {
		case PhoneState.CALL_STATE_PICKUP:
		case PhoneState.CALL_STATE_RINGING:
		case PhoneState.CALL_STATE_CALLING:
		case PhoneState.CALL_STATE_OFFHOOK: {
			// removeSmartCoverView();
			addPhoneView();
			Wind.Log(TAG, "isUpdateUI mClickView.updateInfos(mCallNum)" + mCallNum);
			mClickView.updatePhoneState(mPhoneStatus);
			mClickView.updateInfos(mCallNum);
		}
			break;
		case PhoneState.CALL_STATE_IDLE: {
			mClickView.updatePhoneState(mPhoneStatus);
			removePhoneViewDelay();
			restartSmartCoverView();
			if (!isHallOn()) {
				enableKeyguard();
			}
		}
			break;
		default:
			break;
		}
	}

	public void parseInfo(Intent intent) {
		String currentState = intent.getStringExtra("incall_state");
		InCallState state = InCallState.NO_CALLS;
		if (currentState != null) {
			state = InCallState.valueOf(currentState);
		}
		Wind.Log(TAG, "parseInfo currentState=" + currentState + " ,state="
				+ state);

		switch (state) {
		case INCOMING:
			// PhoneUtil.hideHeadup(mContext);
			mPhoneStatus = PhoneState.CALL_STATE_RINGING;
			resetCallNum(intent.getStringExtra("num"));
			break;
		case WAITING_FOR_ACCOUNT:
		case PENDING_OUTGOING:
		case OUTGOING:
			mPhoneStatus = PhoneState.CALL_STATE_CALLING;
			resetCallNum(intent.getStringExtra("num"));
			break;
		case INCALL:
			int phoneStatus = intent.getIntExtra("state", PhoneState.INVALID);
			boolean mbConnect = PhoneState.isConnectingOrConnected(phoneStatus);
			Wind.Log(TAG, "handsNum INCALL mbConnect" + mbConnect);
			if (mbConnect) {
				resetCallNum(intent.getStringExtra("num"));
				mPhoneStatus = PhoneState.CALL_STATE_OFFHOOK;
			} else {
				mPhoneStatus = PhoneState.CALL_STATE_IDLE;
			}
			break;
		case NO_CALLS:
			mCallNum = "";
			mPhoneStatus = PhoneState.CALL_STATE_IDLE;
			mPrePhoneStatus = mPhoneStatus;
			break;
		default:
			mCallNum = "";
			mPhoneStatus = PhoneState.CALL_STATE_IDLE;
			mPrePhoneStatus = mPhoneStatus;
			break;
		}
	}

	public void parseStateInfo(Intent intent) {
		String currentState = intent.getStringExtra("incall_state");
		InCallState state = InCallState.NO_CALLS;
		if (currentState != null) {
			state = InCallState.valueOf(currentState);
		}
		Wind.Log(TAG, "State parseInfo currentState=" + currentState + " ,state="
				+ state);

		switch (state) {
		case INCOMING:
			// PhoneUtil.hideHeadup(mContext);
			mPhoneStatus = PhoneState.CALL_STATE_RINGING;
			break;
		case WAITING_FOR_ACCOUNT:
		case PENDING_OUTGOING:
		case OUTGOING:
			mPhoneStatus = PhoneState.CALL_STATE_CALLING;
			break;
		case INCALL:
			mPhoneStatus = PhoneState.CALL_STATE_OFFHOOK;
			break;
		case NO_CALLS:
			mPhoneStatus = PhoneState.CALL_STATE_IDLE;
			break;
		default:
			mPhoneStatus = PhoneState.CALL_STATE_IDLE;
			break;
		}
	}

	private int getSimCount() {
		int nCount = 0;

		for (int i = 0; i < PROJECT_SIM_NUM; i++) {
			int[] subId = android.telephony.SubscriptionManager.getSubId(i);
			if (subId == null || subId.length == 0 || subId[0] <= 0) {
				continue;
			} else {
				nCount++;
			}
		}

		return nCount;
	}

	private void setSimCard(int which) {
		Settings.System.putInt(mContext.getContentResolver(),
				PubDefs.SYS_HALL_SIMCARD, which);
	}

	private class OutgoingCallReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Wind.Log(TAG, "OutgoingCallReceiver action=" + action);

			// receive status info
			if (WOS_PHONE_NUMBER.equals(action)) {
				Wind.Log(TAG, "OutgoingCallReceiver incall_state=" + intent.getStringExtra("incall_state"));
				parseInfo(intent);
				updateUI(intent);
			} else if (WOS_HEANDSUP_NUMBER.equals(action)) {
				// PhoneUtil.hideHeadup(mContext);
				Wind.Log(TAG, "OutgoingCallReceiver incall_state=" + intent.getStringExtra("incall_state"));
				parseInfo(intent);
				mPhoneStatus = PhoneState.CALL_STATE_RINGING;
				updateUI(intent);
			} else if (WOS_HEANDSUP_STATE.equals(action)) {
				Wind.Log(TAG, "OutgoingCallReceiver incall_state=" + intent.getStringExtra("incall_state"));
				parseStateInfo(intent);
				updateUI(intent);
			}
			// do operate
			else if (WOS_UI_ANWSER.equals(action)) {
				PhoneUtil.answerRingingCall(mContext);
				mPhoneStatus = PhoneState.CALL_STATE_PICKUP;
				updateUI(intent);
			} else if (WOS_UI_ENDED.equals(action)) {
				PhoneUtil.endCall(mContext);
				// mPhoneStatus = PhoneState.CALL_STATE_IDLE;
				updateUI(intent);
			} else if (PubDefs.WOS_UI_DIALER.equals(action)) {
				mCallNum = intent.getStringExtra(PubDefs.WOS_CALL_NUM);
				Wind.Log(TAG, "WOS_UI_DIALER Num=" + mCallNum + " getSimCount="
						+ getSimCount());
				if (getSimCount() <= 1) {
					if (android.telephony.PhoneNumberUtils.isEmergencyNumber(mCallNum)) {
						Wind.Log(TAG, "WOS_UI_DIALER Num privileged");
						Intent cIntent = new Intent(
								"android.intent.action.CALL_PRIVILEGED",
								Uri.parse("tel:" + mCallNum));
						cIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
						mContext.startActivity(cIntent);
					} else {
						Intent cIntent = new Intent(
								"android.intent.action.CALL", Uri.parse("tel:"
										+ mCallNum));
						cIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
						mContext.startActivity(cIntent);
					}

					mPhoneStatus = PhoneState.CALL_STATE_PICKUP;
					updateUI(intent);
				} else {
					// setSimCard(1);
					Intent simIntent = new Intent(mContext,
							SimSelectActivity.class);
					simIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					simIntent.putExtra(PubDefs.WOS_CALL_NUM, mCallNum);
					mContext.startActivity(simIntent);
				}
			} else if (Intent.ACTION_SCREEN_ON.equals(action)) {
				if (!isHallOn())
					enableKeyguard();
				else {
					replaceKeyguard();
				}
				
				sendScreenOffMsg();
				// if(isHallOn()){
				// replaceKeyguard();
				// sendUpdateLockStatusMSG();
				// sendUpdateLockStatusMSG(400);
				// }else{
				// enableKeyguard();
				// }
			}
		}
	}

	private static final int HALL_CHANG_DELAY	= 300;
	private void sendHallMsgDelay(int msg, int second) {
		mHandler.removeMessages(WHAT_STATUS_CHANGEDON);
		mHandler.removeMessages(WHAT_STATUS_CHANGEDOFF);
		if (second <= 0)
			mHandler.sendEmptyMessage(msg);
		else
			mHandler.sendEmptyMessageDelayed(msg, HALL_CHANG_DELAY);
	}

	private void updateHallStatus() {
		int status = getHallStatus();
		Wind.Log(TAG, "updateHallStatus status=" + status + " isFirstStart="
				+ isFirstStart);
		
		if (isFirstStart) {
			if (status == 1) {
                addPreView();
				sendHallMsgDelay(WHAT_STATUS_CHANGEDON, HALL_CHANG_DELAY);
			}
			isFirstStart = false;
		} else {
			if (status == 1) {
                addPreView();
				sendHallMsgDelay(WHAT_STATUS_CHANGEDON, HALL_CHANG_DELAY);
			} else {
                removePreView();
				sendHallMsgDelay(WHAT_STATUS_CHANGEDOFF, HALL_CHANG_DELAY);
			}
		}
	}

	private class HallContentObserver extends ContentObserver {
		public HallContentObserver(Context context, Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Wind.Log(TAG, "HallContentObserver onChange ");
			if (isHallOn() && Wind.MACRO_HALL_IMMEDIATELY_LOCK) {
				if(isSecureKeyguard())
					ScreenOffLock();
			}
			updateHallStatus();
		}
	}

	private int getHallStatus() {
		return HallUtil.getHallStatus(mContext);
	}

	private void setHallStatus(int state) {
		HallUtil.setHallStatus(mContext,state);
	}

	private boolean isHallOn() {
		return HallUtil.isHallOn(mContext);
	}

	private static final String FP_IDENTIFY_CREDENTIAL_REQ_ACTION = "com.identify.credential.req.service.ACTION";

	private void startIdentifyCredentialReq() {
		mContext.sendBroadcast(new Intent(FP_IDENTIFY_CREDENTIAL_REQ_ACTION));
	}

	public static final String KEYGUARD_PACKAGE = "com.android.systemui";
	public static final String KEYGUARD_CLASS = "com.android.systemui.keyguard.KeyguardService";
	private IKeyguardService mKeyguardService;
	// final PowerManager mPowerManager;
	IWindowManager mIWindowManager;

	private IWindowManager getWindowManager() {
		if (mIWindowManager == null) {
			IBinder b = ServiceManager.getService(Context.WINDOW_SERVICE);
			mIWindowManager = IWindowManager.Stub.asInterface(b);
		}
		return mIWindowManager;
	}

	// private boolean mIsLockDoing = false;
	public boolean ScreenOffLock() {
		Wind.Log(TAG, " ScreenOffLock "+PubDefs.HALL_ON_SCREEN_OFF_LOCK);
		if(!PubDefs.HALL_ON_SCREEN_OFF_LOCK)
			return false;
		
		try {
			mPm.goToSleep(android.os.SystemClock.currentThreadTimeMillis(),
					android.os.PowerManager.GO_TO_SLEEP_REASON_DEVICE_ADMIN, 0);
			new LockPatternUtils(mContext)
					.requireCredentialEntry(UserHandle.USER_ALL);
			getWindowManager().lockNow(null);

			Wind.Log(TAG, " lock before true");
			return true;
		} catch (RemoteException e) {
			Wind.Log(TAG, " lock before false");
			return false;
		}

	}

	public boolean lock() {
		boolean mbIsLocked = mKeyManager.inKeyguardRestrictedInputMode();
		Wind.Log(LOCK_TAG, " lock before keyguardDone mbIsLocked" + mbIsLocked);
		// if(mbIsLocked){
		// sendUpdateLockStatusMSG(300);
		// return true;
		// }
		try {
			new LockPatternUtils(mContext).requireCredentialEntry(UserHandle.USER_ALL);
			getWindowManager().lockNow(null);

			Wind.Log(LOCK_TAG, " lock before keyguardDone mbIsLocked true");
			return true;
		} catch (RemoteException e) {
			Wind.Log(LOCK_TAG, " lock before keyguardDone mbIsLocked false");
			return false;
		}

	}

	public boolean unLock() {
		boolean mbIsLocked = mKeyManager.inKeyguardRestrictedInputMode();
		Wind.Log(LOCK_TAG, " unLock before keyguardDone mbIsLocked=" + mbIsLocked);

		try {
			mKeyguardService.keyguardDone(false, true);
		} catch (RemoteException e) {
			Wind.Log(LOCK_TAG, " unLock occurs RemoteException when keyguardDone"+ e);
		}
		return true;
	}

	private final ServiceConnection mKeyguardConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Wind.Log(LOCK_TAG, "*** Keyguard connected (yay!)");
			mKeyguardService = IKeyguardService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Wind.Log(LOCK_TAG, "*** Keyguard disconnected (boo!)");
			mKeyguardService = null;
		}
	};

	public void bindService(Context context) {
		Intent intent = new Intent();
		intent.setClassName(KEYGUARD_PACKAGE, KEYGUARD_CLASS);
		if (!context.bindService(intent, mKeyguardConnection,
				Context.BIND_AUTO_CREATE)) {
			Wind.Log(LOCK_TAG, "*** bindService: can't bind to " + KEYGUARD_CLASS);
		} else {
			Wind.Log(LOCK_TAG, "*** bindService started");
		}
	}
}
