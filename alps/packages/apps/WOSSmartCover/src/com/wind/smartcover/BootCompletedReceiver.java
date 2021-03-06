package com.wind.smartcover;

import com.wind.smartcover.Util.HallUtil;
import com.wind.smartcover.Util.Wind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCompletedReceiver";

	private boolean isHallSwitchOpen(Context ctx) {
		return HallUtil.isHallSwitchOpen(ctx);
	}

	private void setHallStatus(Context context, int state) {
		Wind.Log(TAG, "setHallStatus state=" + state);
		HallUtil.setHallStatus(context, state);
	}

	protected void connectMediaService(Context context) {
		Wind.Log(TAG, "connectMediaService");
		Intent intent = new Intent();
		intent.setClassName("com.android.music",
				"com.android.music.MediaPlaybackService");
		context.startService(intent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Wind.Log(TAG, "boot complete, start hall service " + intent.getAction());

		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Wind.Log(TAG, "startHallService");
			setHallStatus(context, HallUtil.HALL_STATUS_OFF);
			
			if (isHallSwitchOpen(context)) {
				Intent hallServiceIntent = new Intent(context,
						WindHallService.class);
				hallServiceIntent.putExtra("bootcomplete", 1);
				context.startService(hallServiceIntent);
				connectMediaService(context);
			}
		} else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)
				|| intent.getAction().equals(Intent.ACTION_REBOOT)) {
			Wind.Log(TAG, "setHallStatus(context,HallUtil.HALL_STATUS_OFF)");
			setHallStatus(context, HallUtil.HALL_STATUS_OFF);
		}
	}

}
