package com.wind.smartcover;

import com.wind.smartcover.Util.HallUtil;
import com.wind.smartcover.Util.Wind;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.WindowManager.LayoutParams;

public class EActivity extends Activity {
	private static final String TAG = "EActivity";
	protected AudioManager mAudio;

	protected boolean checkHallStatus(Context context) {
		if (!HallUtil.isHallOn(context)) {
			Wind.Log(TAG, "checkHallStatus false");
			return false;
		}
		return true;
	}

	protected void topView() {
		getWindow().setType(LayoutParams.LAST_SYSTEM_WINDOW + 1);
		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
		// getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	protected void setStatusbarTransparent() {
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Wind.Log(TAG, "setStatusbarTransparent LOLLIPOP");
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
			window.setNavigationBarColor(Color.TRANSPARENT);
		}
	}

	/*
	 * hide status bar must before setContentView
	 */
	protected void hideStatusBar() {
		Wind.Log(TAG, "hideStatusBar");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setStatusbarTransparent();
	}

	/*
	 * hide navigation bar
	 */

    protected void setNavigationInvisible() {
        Wind.Log(TAG, "setNavigationInvisible");
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.INVISIBLE
//                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                );
    }
	protected void hideNavigation() {
		Wind.Log(TAG, "hideNavigation");
		setNavigationInvisible();
		getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
				SystemUiListener);

		getAudioService();
	}

	protected OnSystemUiVisibilityChangeListener SystemUiListener = new OnSystemUiVisibilityChangeListener() {

		@Override
		public void onSystemUiVisibilityChange(int arg0) {
	        setNavigationInvisible();
		}
	};

	/*
	 * exit this activity
	 */
	public void exitThisActivity() {
		this.finish();
	}

	protected void getAudioService() {
		mAudio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			mAudio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			mAudio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
