package com.wind.smartcover.phone;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.ITelephony;
import com.wind.smartcover.PubDefs;
import com.wind.smartcover.Util.Wind;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

public class PhoneUtil {

	public static final String TAG = "PhoneUtil";

	/**
	 * hang off
	 * 
	 * @param context
	 */
	
	public static void endCall(Context context) {
		Wind.Log(TAG, "endCall ");
		rejectRingCall(context);
		try {
			Object telephonyObject = getTelephonyObject(context);
			if (null != telephonyObject) {
				Class telephonyClass = telephonyObject.getClass();
				Wind.Log(TAG, "endCall ");
				Method endCallMethod = telephonyClass.getMethod("endCall");
				endCallMethod.setAccessible(true);

				endCallMethod.invoke(telephonyObject);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	private static Object getTelephonyObject(Context context) {
		Wind.Log(TAG, "getTelephonyObject ");
		Object telephonyObject = null;
		try {
			// init iTelephony
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			// Will be used to invoke hidden methods with reflection
			// Get the current object implementing ITelephony interface
			Class telManager = telephonyManager.getClass();
			Method getITelephony = telManager
					.getDeclaredMethod("getITelephony");
			getITelephony.setAccessible(true);
			telephonyObject = getITelephony.invoke(telephonyManager);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return telephonyObject;
	}

	/**
	 * across aidl,hang on,only used in before android 2.3
	 * 
	 * @param context
	 */
	private static void answerRingingCallWithReflect(Context context) {
		Wind.Log(TAG, "answerRingingCallWithReflect ");
		try {
			Object telephonyObject = getTelephonyObject(context);
			if (null != telephonyObject) {
				Wind.Log(TAG, "answerRingingCall ");
				Class telephonyClass = telephonyObject.getClass();
				Method endCallMethod = telephonyClass
						.getMethod("answerRingingCall");
				endCallMethod.setAccessible(true);

				endCallMethod.invoke(telephonyObject);

				// endCallMethod.silenceRinger();
				// endCallMethod.answerRingingCall();
				// ITelephony iTelephony = (ITelephony) telephonyObject;
				// iTelephony.answerRingingCall();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

	}

	public void answerRingingCall() throws RemoteException {
		try {
			mITelephony.answerRingingCall();
		} catch (RemoteException e) {
			throw e;
		}
	}

	private ITelephony mITelephony = null;

	/**
	 * 
	 * like insert audio,send broadcast abou hang on,let's system hang on.
	 * 
	 * @param context
	 */
	private static void answerRingingCallWithBroadcast(Context context) {
		Wind.Log(TAG, "answerRingingCallWithBroadcast ");
		AudioManager localAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		// is insert audio
		boolean isWiredHeadsetOn = localAudioManager.isWiredHeadsetOn();
		if (!isWiredHeadsetOn) {
			Wind.Log(TAG, "!isWiredHeadsetOn");
			Intent headsetPluggedIntent = new Intent(Intent.ACTION_HEADSET_PLUG);
			headsetPluggedIntent.putExtra("state", 1);
			headsetPluggedIntent.putExtra("microphone", 0);
			headsetPluggedIntent.putExtra("name", "");
			context.sendBroadcast(headsetPluggedIntent);

			Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP,
					KeyEvent.KEYCODE_HEADSETHOOK);
			meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
			context.sendOrderedBroadcast(meidaButtonIntent, null);

			Intent headsetUnpluggedIntent = new Intent(
					Intent.ACTION_HEADSET_PLUG);
			headsetUnpluggedIntent.putExtra("state", 0);
			headsetUnpluggedIntent.putExtra("microphone", 0);
			headsetUnpluggedIntent.putExtra("name", "");
			context.sendBroadcast(headsetUnpluggedIntent);

		} else {
			Wind.Log(TAG, "isWiredHeadsetOn");
			Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP,
					KeyEvent.KEYCODE_HEADSETHOOK);
			meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
			context.sendOrderedBroadcast(meidaButtonIntent, null);
		}
	}

	// public static final String ACTION_ANSWER_VOICE_INCOMING_CALL =
	// "com.android.incallui.ACTION_ANSWER_VOICE_INCOMING_CALL";
	public static final String WOS_ANSWER_VOICE_REJECT_CALL = "com.wos.voice.reject";  
	public static final String WOS_ANSWER_VOICE_INCOMING_CALL = "com.wos.voice.incoming";    
	public static final String ACTION_DECLINE_INCOMING_CALL = "com.android.incallui.ACTION_DECLINE_INCOMING_CALL";

	protected static void answerRingCall(Context context) {
		// Intent intent = new Intent(ACTION_ANSWER_VOICE_INCOMING_CALL);
		// context.sendBroadcast(intent);
		Intent wosintent = new Intent(WOS_ANSWER_VOICE_INCOMING_CALL);
		context.sendBroadcast(wosintent);
	}
	protected static void rejectRingCall(Context context) {
		Wind.Log(TAG, "rejectRingCall ");
		Intent wosintent = new Intent(WOS_ANSWER_VOICE_REJECT_CALL);
		context.sendBroadcast(wosintent);
	}

	/**
	 * hang on
	 * 
	 * @param context
	 */
	public static void answerRingingCall(Context context) {
		Wind.Log(TAG, "answerRingingCall ");
		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
		// //2.3or2.3+
		// answerRingingCallWithBroadcast(context);
		// } else {
		// answerRingingCallWithReflect(context);
		// }
		answerRingCall(context);
	}

	/**
	 * dial
	 * 
	 * @param context
	 * @param phoneNumber
	 */
	public static void callPhone(Context context, String phoneNumber) {
		Wind.Log(TAG, "callPhone phoneNumber" + phoneNumber);
		if (!TextUtils.isEmpty(phoneNumber)) {
			try {
				Intent callIntent = new Intent(Intent.ACTION_CALL,
						Uri.parse("tel:" + phoneNumber));
				context.startActivity(callIntent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * dialer
	 * 
	 * @param context
	 * @param phoneNumber
	 */
	public static void dialPhone(Context context, String phoneNumber) {
		Wind.Log(TAG, "dialPhone phoneNumber" + phoneNumber);
		if (!TextUtils.isEmpty(phoneNumber)) {
			try {
				Intent callIntent = new Intent(Intent.ACTION_DIAL,
						Uri.parse("tel:" + phoneNumber));
				context.startActivity(callIntent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    public static final String ACTION_UPDATE_UI_FORCED = "com.android.incallui.ACTION_UPDATE_UI_FORCED";
    

    public static void hideHeadup(Context ctx) {
        Wind.Log(TAG, "hideHeadup ");
        Intent wosintent = new Intent(ACTION_UPDATE_UI_FORCED);
        ctx.sendBroadcast(wosintent);
    }

    public static final String WOS_TURN_OFF_PROXIMITY_SENSOR = "com.wos.turnoff.proximitysensor";
    public static final String WOS_RESUME_PROXIMITY_SENSOR = "com.wos.resume.proximitysensor";

    public static void wosTurnOffProximitySensor(Context ctx) {
        Wind.Log(TAG, "wosTurnOffProximitySensor ");
        Intent intent = new Intent(WOS_TURN_OFF_PROXIMITY_SENSOR);
        ctx.sendBroadcast(intent);
    }
    
    public static void wosResumeProximitySensor(Context ctx) {
        Wind.Log(TAG, "wosResumeProximitySensor ");
        Intent intent = new Intent(WOS_RESUME_PROXIMITY_SENSOR);
        ctx.sendBroadcast(intent);
    }
}