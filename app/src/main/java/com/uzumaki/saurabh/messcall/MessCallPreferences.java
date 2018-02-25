package com.uzumaki.saurabh.messcall;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Saurabh on 25-Feb-18.
 */

public class MessCallPreferences {

    String SHOULD_SEND_SMS_PREFERENCE = "sendSms";
    String SHOULD_REJECT_CALLS_PREFERENCE = "rejectCalls";
    String MESSAGE_CONTENT = "messageContent";
    String CALL_TIMEOUT = "callTimeout";

    int defaultTimeout = 2000;

    private SharedPreferences sharedPref;

    public void setShouldSendSmsPref(Context context, boolean shouldSendSms) {
        sharedPref = getDefaultSharedPrefs(context);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(SHOULD_SEND_SMS_PREFERENCE, shouldSendSms).apply();
    }

    public boolean getShouldSendSmsPref(Context context) {
        sharedPref = getDefaultSharedPrefs(context);
        return sharedPref.getBoolean(SHOULD_SEND_SMS_PREFERENCE, false);
    }

    public void setMessageContentPref(Context context, String messageContent) {
        sharedPref = getDefaultSharedPrefs(context);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(MESSAGE_CONTENT, messageContent).apply();
    }

    public String getMessageContentPref(Context context) {
        sharedPref = getDefaultSharedPrefs(context);
        return sharedPref.getString(MESSAGE_CONTENT, null);
    }

    public void setShouldRejectCalls(Context context, boolean shouldRejectCalls) {
        sharedPref = getDefaultSharedPrefs(context);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(SHOULD_REJECT_CALLS_PREFERENCE, shouldRejectCalls).apply();
    }

    public boolean getShouldRejectCalls(Context context) {
        sharedPref = getDefaultSharedPrefs(context);
        return sharedPref.getBoolean(SHOULD_REJECT_CALLS_PREFERENCE, false);
    }

    public void setCallTimeout(Context context, int time) {
        sharedPref = getDefaultSharedPrefs(context);
        SharedPreferences.Editor editor = sharedPref.edit();

        time = time * 1000;
        editor.putInt(CALL_TIMEOUT, time).apply();
    }

    public int getCallTimeout(Context context) {
        sharedPref = getDefaultSharedPrefs(context);
        return (sharedPref.getInt(CALL_TIMEOUT, defaultTimeout))/1000;
    }

    public int getUsableCallTimeout(Context context) {
        sharedPref = getDefaultSharedPrefs(context);
        return sharedPref.getInt(CALL_TIMEOUT, defaultTimeout);
    }

    private SharedPreferences getDefaultSharedPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
