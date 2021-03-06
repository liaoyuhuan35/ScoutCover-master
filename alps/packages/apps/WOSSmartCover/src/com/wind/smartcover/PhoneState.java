package com.wind.smartcover;

import com.wind.smartcover.Util.Wind;

public class PhoneState {

    public static final String TAG = "PhoneState";
	
    public static final int INVALID = 0;
    public static final int NEW = 1;            /* The call is new. */
    public static final int IDLE = 2;           /* The call is idle.  Nothing active */
    public static final int ACTIVE = 3;         /* There is an active call */
    public static final int INCOMING = 4;       /* A normal incoming phone call */
    public static final int CALL_WAITING = 5;   /* Incoming call while another is active */
    public static final int DIALING = 6;        /* An outgoing call during dial phase */
    public static final int REDIALING = 7;      /* Subsequent dialing attempt after a failure */
    public static final int ONHOLD = 8;         /* An active phone call placed on hold */
    public static final int DISCONNECTING = 9;  /* A call is being ended. */
    public static final int DISCONNECTED = 10;  /* State after a call disconnects */
    public static final int CONFERENCED = 11;   /* Call part of a conference call */
    public static final int PRE_DIAL_WAIT = 12; /* Waiting for user before outgoing call */
    public static final int CONNECTING = 13;    /* Waiting for Telecomm broadcast to finish */


    public static boolean isConnectingOrConnected(int state) {
        switch(state) {
            case ACTIVE:
            case INCOMING:
            case CALL_WAITING:
            case CONNECTING:
            case DIALING:
            case REDIALING:
            case ONHOLD:
            case CONFERENCED:
                return true;
            default:
        }
        return false;
    }
    public static boolean isOnHold(int state) {
        return state == ACTIVE || state == ONHOLD;
    }

    public static boolean isDialing(int state) {
        return state == DIALING || state == REDIALING;
    }

    public static boolean isIncoming(int state) {
        return state == INCOMING || state == CALL_WAITING;
    }
    public static boolean isEndcall(int state) {
        return !(isConnectingOrConnected(state));
    }


    public static String toString(int state) {
        switch (state) {
            case INVALID:
                return "INVALID";
            case NEW:
                return "NEW";
            case IDLE:
                return "IDLE";
            case ACTIVE:
                return "ACTIVE";
            case INCOMING:
                return "INCOMING";
            case CALL_WAITING:
                return "CALL_WAITING";
            case DIALING:
                return "DIALING";
            case REDIALING:
                return "REDIALING";
            case ONHOLD:
                return "ONHOLD";
            case DISCONNECTING:
                return "DISCONNECTING";
            case DISCONNECTED:
                return "DISCONNECTED";
            case CONFERENCED:
                return "CONFERENCED";
            case PRE_DIAL_WAIT:
                return "PRE_DIAL_WAIT";
            case CONNECTING:
                return "CONNECTING";
            default:
                return "UNKNOWN";
        }
    }

	public static boolean needScreenOff(int state) {
		Wind.Log(TAG, "needScreenOff state=" + state);
		switch (state) {
		case CALL_STATE_OUTGOING:
		case CALL_STATE_OFFHOOK:
		case CALL_STATE_CALLING:
		case CALL_STATE_PICKUP:
			return true;
		default:
			break;
		}
		return false;
	}

	public final static int CALL_STATE_IDLE		= 0;			//nothing
	public final static int CALL_STATE_RINGING	= 1;			//before anwser
	public final static int CALL_STATE_OUTGOING	= 2;			//before anwser
	public final static int CALL_STATE_OFFHOOK	= 3;			//holding
	public final static int CALL_STATE_CALLING	= 4;			//calling
	public final static int CALL_STATE_PICKUP	= 5;			//pick up  ing
}