package com.wind.smartcover.sms;

import java.util.ArrayList;
import java.util.List;

import com.wind.smartcover.PubDefs;
import com.wind.smartcover.Util.Wind;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class SmsUtil {
	private static final String TAG = "SmsUtil";
	/*
	 * CONTENT_URI_SMS: CONTENT_URI_SMS_INBOX: CONTENT_URI_SMS_SENT:
	 */
	public static final String CONTENT_URI_SMS = "content://sms";
	public static final String CONTENT_URI_SMS_INBOX = "content://sms/inbox";
	public static final String CONTENT_URI_SMS_SENT = "content://sms/sent";
	public static final String CONTENT_URI_SMS_CONVERSATIONS = "content://sms/conversations";

	private Context mContext;
	private static SmsUtil mSmsUtil = null;

	private SmsUtil(Context context) {
		mContext = context;
	}

	public static SmsUtil getInstance(Context context) {
		if (null == mSmsUtil) {
			synchronized (SmsUtil.class) {
				mSmsUtil = new SmsUtil(context);
			}
		}
		return mSmsUtil;
	}

	public static String[] UNREAD_SMS = new String[] { "_id", "read" };

	public void resetSmsReaded() {
		Wind.Log(TAG, "resetSmsReaded");
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = resolver.query(Sms.Inbox.CONTENT_URI, UNREAD_SMS,
					"read=0", null, "date desc");
			cursor.moveToFirst();
			if (cursor != null) {
				Wind.Log(TAG, "resetSmsReaded cursor.moveToFirst");
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); ++i) {
					Wind.Log(TAG, "resetSmsReaded id " + cursor.getInt(0));
					ContentValues values = new ContentValues();
					values.put("read", "1");
					resolver.update(Uri.parse(CONTENT_URI_SMS_INBOX), values,
							" _id=?", new String[] { "" + cursor.getInt(0) });
					cursor.moveToNext();
				}
				cursor.close();
			}
		} catch (Exception e) {
			Wind.Log(TAG, "resetSmsReaded " + e.toString());
			cursor = null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/*
	 * 0:sms time 1:sms date 2:phone num 3:sms type
	 */
	public static String[] SMS_QUERY = new String[] { "_id",
			Telephony.Sms.DATE, Telephony.Sms.BODY, Telephony.Sms.ADDRESS,
			Telephony.Sms.TYPE, };

	public List<SmsInfo> getSmsData() {
		List<SmsInfo> list = new ArrayList<SmsInfo>();
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = resolver.query(Sms.CONTENT_URI, SMS_QUERY, "read=0", null,
					"date desc");
			if (cursor != null) {
				int count = cursor.getCount();
				cursor.moveToFirst();
				synchronized (this) {
					for (int i = 0; i < count; i++) {
						cursor.moveToPosition(i);

						SmsInfo mmt = new SmsInfo();
						mmt.setId(cursor.getInt(0));
						mmt.setDate(cursor.getLong(1));
						mmt.setMsgSnippet(cursor.getString(2));
						mmt.setAddress(cursor.getString(3));
						list.add(mmt);
					}
				}
				cursor.close();
			}
		} catch (Exception e) {
			Wind.Log(TAG, e.toString());
			cursor = null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public static String[] SMS_READ = new String[] { Telephony.Sms.DATE,
			Telephony.Sms.BODY, Telephony.Sms.ADDRESS, Telephony.Sms.TYPE };

	public void setSmsReaded(long date) {
		Wind.Log(TAG, "setSmsReaded");
		ContentResolver resolver = mContext.getContentResolver();
		try {
			ContentValues values = new ContentValues();
			values.put("read", "1");
			resolver.update(Sms.Inbox.CONTENT_URI, values, "read=0 AND date="
					+ date, null);
		} catch (Exception e) {
			Wind.Log(TAG, e.toString());
		} finally {
		}
	}

}

