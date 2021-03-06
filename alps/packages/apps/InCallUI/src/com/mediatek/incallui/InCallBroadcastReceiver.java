
package com.mediatek.incallui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//A: WOS WOS_SmartCover 2.0 liaoyuhuan@wind-mobi.com 150727 begin
import android.os.SystemProperties;
//A: WOS WOS_SmartCover 2.0 liaoyuhuan@wind-mobi.com 150727 end

import com.android.incallui.Log;
import com.android.incallui.StatusBarNotifier;

/**
 * M: This BroadcastReceiver is registered in the AndroidManifest.xml so as to receive
 * broadcast even if the whole process has dead.
 * This gain InCallUI a chance to clear the Notification after the bind with Telecom
 * break for any unexpected reason.
 * Find more info in the InCallController.java in the Telecom package.
 */
public class InCallBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_UPDATE_UI_FORCED =
            "com.android.incallui.ACTION_UPDATE_UI_FORCED";

    //A: WOS WOS_SmartCover 2.0 liaoyuhuan@wind-mobi.com 150727 begin
    public static final boolean WIND_OS_APP_SMARTCOVER = SystemProperties.get("ro.wind_os_app_smartcover").equals("1");
    public static final String WOS_ANSWER_VOICE_INCOMING_CALL = "com.wos.voice.incoming";
    //A: WOS WOS_SmartCover 2.0 liaoyuhuan@wind-mobi.com 150727 end
    
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.i(this, "Broadcast from Telecom: " + action);

        //A: WOS WOS_SmartCover 2.0 liaoyuhuan@wind-mobi.com 150727 begin
        if(action.equals(WOS_ANSWER_VOICE_INCOMING_CALL) && WIND_OS_APP_SMARTCOVER){
        	StatusBarNotifier.wosSmartCoverAnswer(context);
        	return;
        }
        //A: WOS WOS_SmartCover 2.0 liaoyuhuan@wind-mobi.com 150727 end

        if (action.equals(ACTION_UPDATE_UI_FORCED)) {
            StatusBarNotifier.clearAllCallNotifications(context);
        } else {
            Log.d(this, "Unkown type action. ");
        }
    }
}
