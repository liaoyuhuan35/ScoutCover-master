package com.wind.smartcover.sms;

import com.wind.smartcover.Util.Wind;

import android.util.Log;

public class SmsInfo {
	private String mThreadId; // msg thread id
	private String mMsgCount; // msg count
	private String mMsgSnippet; // msg data
	private String mAddress; // msg address
	private Long mDate; // msg date

	private String mName;
	private int mId;

	// private String mRead;

	public SmsInfo() {
	}

	public SmsInfo(String threadId, String msgCount, String msgSnippet) {
		mThreadId = threadId;
		mMsgCount = msgCount;
		mMsgSnippet = msgSnippet;
	}

	public String getName() {
		mName = getPhoneNum(mAddress);
		return mName;
	}

	public void setAddress(String address) {
		mAddress = address;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setDate(long date) {
		mDate = date;
	}

	public long getDate() {
		return mDate;
	}

	public void setThreadId(String threadId) {
		mThreadId = threadId;
	}

	public String getThreadId() {
		return mThreadId;
	}

	public String getMsgCount() {
		return mMsgCount;
	}

	public void setMsgCount(String msgCount) {
		mMsgCount = msgCount;
	}

	public String getMsgSnippet() {
		return mMsgSnippet;
	}

	public void setMsgSnippet(String msgSnippet) {
		mMsgSnippet = msgSnippet;
	}

	public void setId(int id) {
		mId = id;
	}

	public int getId() {
		return mId;
	}

	private String getPhoneNum(String str) {
		Wind.Log("getPhoneNum", str);
		if (str.length() > 11)
			str = str.substring(str.length() - 11);
		Wind.Log("getPhoneNum", str);
		return str;
	}
}
