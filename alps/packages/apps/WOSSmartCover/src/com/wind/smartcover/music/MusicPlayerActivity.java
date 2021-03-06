package com.wind.smartcover.music;

/**
 * com.android.music.metachanged
 * com.android.music.queuechanged
 * com.android.music.playbackcomplete
 * com.android.music.playstatechanged
 */

import java.util.List;

import com.wind.smartcover.EActivity;
import com.wind.smartcover.PubDefs;
import com.wind.smartcover.R;
import com.wind.smartcover.WindApp;
import com.wind.smartcover.Util.Wind;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MusicPlayerActivity extends EActivity {
	private static final String TAG = "MusicPlayerActivity";

	private Context mContext;

	private Button mButton1;
	private Button mButton2;
	private Button mButton3;
	private Button mBtnBack;

	private static final int MSG_INVISIBLE = 0xbb01;
	private static final int MSG_LOCK = 0xbb02;
	private static final int MSG_PLAY_MSUCI = 0xbb03;
	private SeekBar mSeekBar;
	// private AudioManager mAudio;

	private TextView mMusicName;
	private TextView mMusicSinger;

	private RelativeLayout mMain = null;
	private RelativeLayout mEmpty = null;

	private static final String SERVICECMD = "com.android.music.musicservicecommand";
	private static final String CMDNAME = "command";
	/*
	 * private static final String CMDTOGGLEPAUSE = "togglepause"; private
	 * static final String CMDSTOP = "stop";
	 */
	private static final String CMDPAUSE = "pause";
	private static final String CMDPLAY = "play";
	private static final String CMDPREVIOUS = "previous";
	private static final String CMDNEXT = "next";
	private boolean mPlaying = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindApp.getInstance().addActivity(MusicPlayerActivity.this);
		setContentView(R.layout.activity_wind_music);
		hideNavigation();
		mContext = this;
		connectMediaService();

		mMain = (RelativeLayout) findViewById(R.id.layout_music_main);
		mEmpty = (RelativeLayout) findViewById(R.id.layout_music_empty);
		mEmpty.setVisibility(View.VISIBLE);
		mMain.setVisibility(View.INVISIBLE);
		mButton1 = (Button) findViewById(R.id.btn_music_pre);
		mButton2 = (Button) findViewById(R.id.btn_music_play);
		mButton3 = (Button) findViewById(R.id.btn_music_next);
		mButton1.setOnClickListener(mClickListener);
		mButton2.setOnClickListener(mClickListener);
		mButton3.setOnClickListener(mClickListener);

		mSeekBar = (SeekBar) findViewById(R.id.wos_seek_Bar);
		initAudio();

		mBtnBack = (Button) findViewById(R.id.btn_back);
		mBtnBack.setOnClickListener(mClickListener);

		mMusicName = (TextView) findViewById(R.id.tv_music_name);
		mMusicSinger = (TextView) findViewById(R.id.tv_music_singer);

		if (!checkHallStatus(mContext)) {
			this.exitThisActivity();
			return;
		}
		initUI();
		startMusic();
	}

	protected void connectMediaService() {
		Wind.Log(TAG, "connectMediaService");
		if (!isServiceRunning("com.android.music.MediaPlaybackService")) {
			Wind.Log(TAG, "connectMediaService !isServiceRunning");
			Intent intent = new Intent();
			intent.setClassName("com.android.music",
					"com.android.music.MediaPlaybackService");
			mContext.startService(intent);

			mPlaying = false;
			mHandler.sendEmptyMessageDelayed(MSG_PLAY_MSUCI, 300);
		}
	}

	protected void stopMediaService() {
		Intent intent = new Intent();
		intent.setClassName("com.android.music",
				"com.android.music.MediaPlaybackService");
		mContext.stopService(intent);
	}

	protected void startMusic() {
		Wind.Log(TAG, "startMusic " + mPlaying);
		Intent intent = new Intent(SERVICECMD);
		intent.putExtra(CMDNAME, CMDPLAY);
		sendBroadcast(intent);
	}

	private void initUI() {
		mButton1.setBackgroundResource(R.drawable.music_pre);
		mButton2.setBackgroundResource(R.drawable.music_play);
		mButton3.setBackgroundResource(R.drawable.music_next);
		mBtnBack.setBackgroundResource(R.drawable.btn_back_bg);
	}

	protected void initAudio() {
		mAudio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

		int maxVolume = mAudio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mSeekBar.setMax(maxVolume);
		int currenvolume = mAudio.getStreamVolume(AudioManager.STREAM_MUSIC);
		mSeekBar.setProgress(currenvolume);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				sendHideSeekbarMsg();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				setSeekbarVisible();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				int tmpInt = seekBar.getProgress();
				if (tmpInt < 1) {
					tmpInt = 0;
				}
				mAudio.setStreamVolume(AudioManager.STREAM_MUSIC, tmpInt, 0);
			}
		});
	}

	private void setSeekbarVisible() {
		mHandler.removeMessages(MSG_INVISIBLE);
		mSeekBar.setVisibility(View.VISIBLE);
	}

	private void sendHideSeekbarMsg() {
		mHandler.removeMessages(MSG_INVISIBLE);
		Message msg = new Message();
		msg.what = MSG_INVISIBLE;
		mHandler.sendMessageDelayed(msg, 2000);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Wind.Log(TAG, "handleMessage msg=" + msg.what);
			if (msg.what == MSG_INVISIBLE) {
				mSeekBar.setVisibility(View.INVISIBLE);
			} else if (msg.what == MSG_LOCK) {
				mbStateChange = true;
			} else if (msg.what == MSG_PLAY_MSUCI) {
				startMusic();
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			mAudio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);

			int currenvolume = mAudio
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			mSeekBar.setProgress(currenvolume);
			mSeekBar.setVisibility(View.VISIBLE);
			sendHideSeekbarMsg();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			mAudio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
			int currenvolume = mAudio
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			mSeekBar.setProgress(currenvolume);
			mSeekBar.setVisibility(View.VISIBLE);
			sendHideSeekbarMsg();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean isServiceRunning(String serviceClassName) {
		final ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		for (RunningServiceInfo runningServiceInfo : services) {
			if (runningServiceInfo.service.getClassName().equals(
					serviceClassName)) {
				return true;
			}
		}
		return false;
	}

	public OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Wind.Log(TAG, "onClick " + view.getId() + " mbStateChange="
					+ mbStateChange + " mPlaying=" + mPlaying);
			Intent intent = new Intent(SERVICECMD);
			switch (view.getId()) {
			case R.id.btn_music_pre: {
				connectMediaService();
				mbStateChange = true;
				intent.putExtra(CMDNAME, CMDPREVIOUS);
				sendBroadcast(intent);
			}
				break;
			case R.id.btn_music_play: {
				connectMediaService();
				mbStateChange = true;
				if (mPlaying) {
					intent.putExtra(CMDNAME, CMDPAUSE);
				} else {
					intent.putExtra(CMDNAME, CMDPLAY);
				}
				sendBroadcast(intent);
			}
				break;
			case R.id.btn_music_next: {
				connectMediaService();
				mbStateChange = true;
				intent.putExtra(CMDNAME, CMDNEXT);
				sendBroadcast(intent);
			}
				break;
			case R.id.btn_back: {
				exitThisActivity();
			}
				break;
			default:
				break;
			}
		}
	};

	private boolean mbStateChange = false;
	private MusicBroadcastReceiver mbr = null;

	class MusicBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Wind.Log(TAG, "action " + action);

			String artistName = intent.getStringExtra("artist");
			String album = intent.getStringExtra("album");
			String track = intent.getStringExtra("track");
			mPlaying = intent.getBooleanExtra("playing", false);
			long duration = intent.getLongExtra("duration", 3000);
			long position = intent.getLongExtra("position", 1000);
			Wind.Log(TAG, "artistName " + artistName + " album " + album
					+ " track " + track + " playing " + mPlaying + " duration "
					+ duration + " position " + position);

			parseMusicMsg2(action, artistName, album, track);
		}
	};

	// only support MUSIC_METACHANGED;
	private void updatePlayBtn() {
		if (mPlaying) {
			mButton2.setBackground(mContext.getResources().getDrawable(
					R.drawable.music_pause));
		} else {
			mButton2.setBackground(mContext.getResources().getDrawable(
					R.drawable.music_play));
		}
	}

	private void parseMusicMsg2(String action, String artistName, String album,
			String track) {
		updatePlayBtn();

		if (MUSIC_QUITPLAYBACK.equals(action)) {
			stopMediaService();
			connectMediaService();
			return;
		}

		if (!MUSIC_METACHANGED.equals(action)) {
			return;
		}

		if (artistName == null && album == null && track == null) {
			mEmpty.setVisibility(View.VISIBLE);
			mMain.setVisibility(View.INVISIBLE);
			return;
		} else {
			mEmpty.setVisibility(View.INVISIBLE);
			mMain.setVisibility(View.VISIBLE);
		}

		mHandler.removeMessages(MSG_LOCK);
		mHandler.sendEmptyMessageDelayed(MSG_INVISIBLE, 300);
		mMusicName.setText(track);
		mMusicSinger.setText(artistName);
	}

	private void parseMusicMsg(String action, String artistName, String album,
			String track) {

		if (action.equals(MUSIC_METACHANGED)) {
			mbStateChange = false;
			mHandler.removeMessages(MSG_LOCK);
			mHandler.sendEmptyMessageDelayed(MSG_INVISIBLE, 300);
			mMusicName.setText(track);
			mMusicSinger.setText(artistName);
		} else if (mbStateChange) {
			mMusicName.setText(track);
			mMusicSinger.setText(artistName);
		}

		if (!action.equals(MUSIC_QUEUECHANGED)) {
			if (artistName == null && album == null && track == null) {
				mEmpty.setVisibility(View.VISIBLE);
				mMain.setVisibility(View.INVISIBLE);
				return;
			} else {
				mEmpty.setVisibility(View.INVISIBLE);
				mMain.setVisibility(View.VISIBLE);
			}
		}

		if (mPlaying) {
			mButton2.setBackground(mContext.getResources().getDrawable(
					R.drawable.music_pause));
		} else {
			mButton2.setBackground(mContext.getResources().getDrawable(
					R.drawable.music_play));
		}
	}

	private static final String MUSIC_METACHANGED = "com.android.music.metachanged";
	private static final String MUSIC_QUEUECHANGED = "com.android.music.queuechanged";
	private static final String MUSIC_PLAYBACKCOMPLETE = "com.android.music.playbackcomplete";
	private static final String MUSIC_PLAYSTATECHANGED = "com.android.music.playstatechanged";

	private static final String MUSIC_QUITPLAYBACK = "com.android.music.quitplayback";

	@Override
	protected void onResume() {
		mbr = new MusicBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MUSIC_METACHANGED);
		intentFilter.addAction(MUSIC_QUEUECHANGED);
		intentFilter.addAction(MUSIC_PLAYBACKCOMPLETE);
		intentFilter.addAction(MUSIC_PLAYSTATECHANGED);
		intentFilter.addAction(MUSIC_QUITPLAYBACK);
		// intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		this.registerReceiver(mbr, intentFilter);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mbr != null)
			this.unregisterReceiver(mbr);
		WindApp.getInstance().removeActivity(MusicPlayerActivity.this);
		super.onDestroy();
	};
}
