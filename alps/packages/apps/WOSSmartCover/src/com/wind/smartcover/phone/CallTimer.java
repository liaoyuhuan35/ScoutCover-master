package com.wind.smartcover.phone;

import com.wind.smartcover.Util.Wind;

import android.os.Handler;

/**
 * Helper class used to keep track of events requiring regular intervals.
 */
public class CallTimer extends Handler {
	private final static String TAG = "CallTimer";

	private Runnable mInternalCallback;
	private Runnable mCallback;
	private long mInterval;
	private boolean mRunning;

	private Handler mTimeHandler = new Handler();

	public CallTimer(Runnable callback) {
		// Preconditions.checkNotNull(callback);

		mInterval = 0;
		mRunning = false;
		mCallback = callback;
		mInternalCallback = new CallTimerCallback();
	}

	public boolean start(long interval) {
		if (interval <= 0) {
			return false;
		}

		// cancel any previous timer
		cancel();

		mInterval = interval;
		mRunning = true;
		mCallback.run();
		periodicUpdateTimer();

		return true;
	}

	public void cancel() {
		removeCallbacks(mInternalCallback);
		mRunning = false;
	}

	private void periodicUpdateTimer() {
		if (!mRunning) {
			return;
		}
		Wind.Log(TAG, "periodicUpdateTimer");
		mTimeHandler.postDelayed(mInternalCallback, mInterval);
	}

	private class CallTimerCallback implements Runnable {
		@Override
		public void run() {
			mCallback.run();
			periodicUpdateTimer();
		}
	}
}
