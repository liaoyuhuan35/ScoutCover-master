package com.wind.smartcover.calllog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.wind.smartcover.PubDefs;
import com.wind.smartcover.R;
import com.wind.smartcover.Util.Wind;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract.PhoneLookup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter {
	private static final String TAG = "ListAdapter";

	private List<QueryDate> mList = null;
	private LayoutInflater inflater = null;
	private Context mContext = null;

	public ListAdapter(Context context, List<QueryDate> callInfo) {
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = callInfo;
		mContext = context;
	}

	@Override
	public int getCount() {
		if (mList != null) {
			return mList.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		// TODO Auto-generated method stub
		View itemView = null;
		ViewHolder viewHolder = null;

		if (convertView == null) {
			itemView = newView(viewGroup, position);
		} else {
			itemView = convertView;
		}
		QueryDate itemInfo = (QueryDate) getItem(position);
		viewHolder = (ViewHolder) itemView.getTag();
		viewHolder.setViews(itemInfo);

		return itemView;
	}

	public View newView(ViewGroup parent, int pos) {
		View v = inflater.inflate(R.layout.call_log_item, parent, false);
		new ViewHolder(v);
		return v;
	}

	private Uri getImageUriByNum(String strNum) {
		Wind.Log(TAG, "getImageUriByNum " + strNum);

		Uri imgUri = null;
		Uri contentUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(strNum));
		Cursor cursor = mContext.getContentResolver().query(contentUri,
				new String[] { PhoneLookup.PHOTO_URI }, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				if (cursor.getString(0) != null) {
					imgUri = Uri.parse(cursor.getString(0));
					return imgUri;
				}
			}
			cursor.close();
		}
		return imgUri;
	}

	class ViewHolder {
		private ImageView imgHead = null;
		private ImageView imgType = null;
		private TextView tvName = null;
		private TextView tvNum = null;
		private TextView tvDate = null;
		private ImageView imgCall = null;
		private View view = null;

		public ViewHolder(View v) {
			view = v;
			imgHead = (ImageView) view.findViewById(R.id.image_head);
			imgType = (ImageView) view.findViewById(R.id.img_type);
			tvName = (TextView) view.findViewById(R.id.text_name);
			tvNum = (TextView) view.findViewById(R.id.text_num);
			tvDate = (TextView) view.findViewById(R.id.text_duration);
			imgCall = (ImageView) view.findViewById(R.id.img_call);
			view.setTag(this);
		}

		final int DAY = 1440; // the count of day's second

		public void setViews(QueryDate itemInfo) {

			if (itemInfo.Num != null) {
				tvName.setText(itemInfo.Num);
				tvNum.setText(itemInfo.Num);
			}

			if (itemInfo.Name != null)
				tvName.setText(itemInfo.Name);

			if (null != itemInfo.Num) {
				itemInfo.PhotoUri = getImageUriByNum(itemInfo.Num);
			}
			if (itemInfo.PhotoUri != null)
				imgHead.setImageURI(itemInfo.PhotoUri);
			else
				imgHead.setImageResource(R.drawable.callog_default);
			Wind.Log(TAG, "getImageUriByNum " + itemInfo.PhotoUri);
			// if(itemInfo.PhotoUri!= null)
			// imgHead.setImageURI(itemInfo.PhotoUri);

			if (itemInfo.Date != null) {
				long callTime = Long.parseLong(itemInfo.Date);
				long newTime = new Date().getTime();
				long duration = (newTime - callTime) / (1000 * 60);
				String strData;
				if (duration < 60) {
					strData = duration
							+ mContext.getResources().getString(
									R.string.minuters_ago);
				} else if (duration >= 60 && duration < DAY) {
					strData = (duration / 60)
							+ mContext.getResources().getString(
									R.string.hours_ago);
				} else if (duration >= DAY && duration < DAY * 2) {
					strData = mContext.getResources().getString(
							R.string.yesterday);
				} else if (duration >= DAY * 2 && duration < DAY * 3) {
					strData = mContext.getResources().getString(
							R.string.three_day_ago);
				} else if (duration >= DAY * 7) {
					SimpleDateFormat sdf = new SimpleDateFormat(mContext
							.getResources().getString(
									R.string.formate_month_day));
					strData = sdf.format(new Date(callTime));
				} else {
					strData = (duration / DAY)
							+ mContext.getResources().getString(
									R.string.days_ago);
				}
				tvDate.setText(strData);
			}
			if (null != itemInfo.Type) {
				int n = Integer.parseInt(itemInfo.Type);
				Wind.Log(TAG, "type n=" + n + " type=" + itemInfo.Type);
				if (CallLog.Calls.INCOMING_TYPE == n) {
					imgType.setImageResource(R.drawable.callog_received);
				} else if (CallLog.Calls.OUTGOING_TYPE == n) {
					imgType.setImageResource(R.drawable.callog_dialed);
				} else if (CallLog.Calls.MISSED_TYPE == n) {
					imgType.setImageResource(R.drawable.callog_missed);
				}
			}
			imgCall.setOnClickListener(new clickListener());
		}

		private class clickListener implements View.OnClickListener {

			@Override
			public void onClick(View view) {
				Wind.Log(TAG, "imgCall click " + tvNum.getText());

				Intent cItent = new Intent(PubDefs.WOS_UI_DIALER);
				cItent.putExtra(PubDefs.WOS_CALL_NUM, tvNum.getText());
				mContext.sendBroadcast(cItent);
				// Intent myIntent = new Intent("android.intent.action.CALL",
				// Uri.parse("tel:" + tvNum.getText()));
				// startActivity(myIntent);
				// Intent intent = new Intent(mContext,myIntent);
				// mContext.startActivity(intent);
			}

		}
	}
}
