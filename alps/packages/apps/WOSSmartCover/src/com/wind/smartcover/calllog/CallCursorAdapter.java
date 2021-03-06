package com.wind.smartcover.calllog;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wind.smartcover.R;
import com.wind.smartcover.Util.Wind;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract.PhoneLookup;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class CallCursorAdapter extends ResourceCursorAdapter {
	private static final String TAG = "CallCursorAdapter";
	final int DAY = 1440; // the count of day's second
	private Context mContext;

	@SuppressWarnings("deprecation")
	public CallCursorAdapter(Context context, int layout, Cursor cursor) {
		super(context, layout, cursor);
		mContext = context;
	}

	private Uri getImageUriByNum(String strNum) {
		Wind.Log(TAG, "getImageUriByNum " + strNum);

		Uri contentUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(strNum));
		Cursor cursor = mContext.getContentResolver().query(contentUri,
				new String[] { PhoneLookup.PHOTO_URI }, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			if (cursor.getString(0) != null) {
				return Uri.parse(cursor.getString(0));
			}
		}
		return null;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String num = cursor.getString(cursor.getColumnIndex("number"));
		String name = cursor.getString(cursor.getColumnIndex("name"));
		String time = cursor.getString(cursor.getColumnIndex("date"));
		String type = cursor.getString(cursor.getColumnIndex("type"));

		TextView tvName = (TextView) view.findViewById(R.id.text_name);
		TextView tvNum = (TextView) view.findViewById(R.id.text_num);
		TextView tvTime = (TextView) view.findViewById(R.id.text_duration);
		ImageView imgHead = (ImageView) view.findViewById(R.id.image_head);
		ImageView imgType = (ImageView) view.findViewById(R.id.img_type);
		if (name != null)
			tvName.setText(name);
		else
			tvName.setText(num);
		tvNum.setText(num);

		long callTime = Long.parseLong(time);
		long newTime = new Date().getTime();
		long duration = (newTime - callTime) / (1000 * 60);
		if (duration < 60) {
			time = duration
					+ mContext.getResources().getString(R.string.minuters_ago);
		} else if (duration >= 60 && duration < DAY) {
			time = (duration / 60)
					+ mContext.getResources().getString(R.string.hours_ago);
		} else if (duration >= DAY && duration < DAY * 2) {
			time = mContext.getResources().getString(R.string.yesterday);
		} else if (duration >= DAY * 2 && duration < DAY * 3) {
			time = mContext.getResources().getString(R.string.three_day_ago);
		} else if (duration >= DAY * 7) {
			SimpleDateFormat sdf = new SimpleDateFormat(mContext.getResources()
					.getString(R.string.formate_month_day));
			time = sdf.format(new Date(callTime));
		} else {
			time = (duration / DAY)
					+ mContext.getResources().getString(R.string.days_ago);
		}
		tvTime.setText(time);

		if (null != type) {
			int n = Integer.parseInt(type);
			Wind.Log(TAG, "type n=" + n + " type=" + type);
			View img = view.findViewById(R.id.img_type);
			if (CallLog.Calls.INCOMING_TYPE == n) {
				imgType.setImageResource(R.drawable.callog_received);
			} else if (CallLog.Calls.OUTGOING_TYPE == n) {
				imgType.setImageResource(R.drawable.callog_dialed);
			} else if (CallLog.Calls.MISSED_TYPE == n) {
				imgType.setImageResource(R.drawable.callog_missed);
			}
		}

		Uri image = null;
		if (null != num) {
			image = getImageUriByNum(num);
		}
		if (image != null)
			imgHead.setImageURI(image);
		else
			imgHead.setImageResource(R.drawable.callog_default);

	}
}
