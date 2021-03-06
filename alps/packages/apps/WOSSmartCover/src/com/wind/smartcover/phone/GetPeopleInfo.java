package com.wind.smartcover.phone;

import com.wind.smartcover.R;
import com.wind.smartcover.Util.Wind;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;

public class GetPeopleInfo {
	private final static String TAG = "GetPeopleInfo";

	private String mQuryNum = null;

	private Uri mPhotoUri = null;
	private String mName = null;
	private String mNum = null;
	private int mIndex = -1;

	public boolean QueryPeopleInfo(Context context, String num) {
		mQuryNum = num;
		mName = mQuryNum;
		mNum = mQuryNum;
		mPhotoUri = null;
		return getInfoByNum(context);
	}

	public Uri getPhotoUri() {
		return mPhotoUri;
	}

	public String getPhoneName() {
		return mName;
	}

	public String getPhoneNum() {
		return mNum;
	}

	public int getSimIndex() {
		return mIndex;
	}

	static private String INDEX_IN_SIM = "index_in_sim";

	private void InitValues(String strQueryNum) {
		mName = strQueryNum;
		mPhotoUri = null;
		mIndex = -1;
	}

	private boolean getInfoByNum(Context context) {
		Wind.Log(TAG, "getInfoByNum " + mQuryNum);
		boolean bResult = false;
		ContentResolver contentResolve = null;
		contentResolve = context.getContentResolver();
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(mQuryNum));
		Cursor cursor = contentResolve.query(lookupUri, new String[] {
				PhoneLookup._ID, PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME,
				PhoneLookup.PHOTO_URI, INDEX_IN_SIM, }, null, null, null);

		Wind.Log(TAG, "cursor " + cursor);

		InitValues(mQuryNum);
		if (cursor != null && cursor.getCount() > 0) {
			Wind.Log(TAG, "cursor != null && cursor.getCount() > 0");
			cursor.moveToFirst();

			if (null != cursor.getString(1)) {
				mNum = cursor.getString(1);
			}
			if (null != cursor.getString(2)) {
				mName = cursor.getString(2);
			}
			if (null != cursor.getString(3)) {
				mPhotoUri = Uri.parse(cursor.getString(3));
			}
			String strIndex = cursor.getString(4);
			if (null != strIndex) {
				mIndex = Integer.parseInt(strIndex);
			}

			Wind.Log(TAG, "id " + cursor.getString(0));
			Wind.Log(TAG, "num " + cursor.getString(1));
			Wind.Log(TAG, "name " + cursor.getString(2));
			Wind.Log(TAG, "uri " + cursor.getString(3));
			Wind.Log(TAG, "simIndex " + cursor.getString(4));
			bResult = true;
		}
		cursor.close();

		if (PhoneNumberUtils.isEmergencyNumber(mNum)) {
			mName = context.getString(R.string.emergency_number);
			Wind.Log(TAG, "name " + mName);
		}
		return bResult;
	}

}
