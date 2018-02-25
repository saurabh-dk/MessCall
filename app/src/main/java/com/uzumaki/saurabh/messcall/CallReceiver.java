package com.uzumaki.saurabh.messcall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;


public class CallReceiver extends BroadcastReceiver {

    String incomingNumber = "";
    int timeout = 2000;
    AudioManager audioManager;
    TelephonyManager telephonyManager;
    MessCallPreferences messCallPreferences;

    @Override
    public void onReceive(final Context context, Intent intent) {

        messCallPreferences = new MessCallPreferences();

        if (messCallPreferences.getShouldRejectCalls(context)) {
            if ("android.intent.action.PHONE_STATE".equals(intent.getAction())) {
                telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                Log.d(TAG, "onReceive: ");
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {

                    timeout = messCallPreferences.getUsableCallTimeout(context);

                    // Get incoming number
                    incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    //audioManager.adjustStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                    Log.d("Incoming call", incomingNumber);
                    try {
                        // Get the getITelephony() method
                        Class<?> classTelephony = Class.forName(telephonyManager.getClass().getName());
                        Method method = classTelephony.getDeclaredMethod("getITelephony");

                        // Disable access check
                        method.setAccessible(true);

                        // Invoke getITelephony() to get the ITelephony interface
                        final Object telephonyInterface = method.invoke(telephonyManager);

                        // Get the endCall method from ITelephony
                        Class<?> telephonyInterfaceClass = Class.forName(telephonyInterface.getClass().getName());
                        final Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");

                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Invoke endCall()
                                try {
                                    methodEndCall.invoke(telephonyInterface);
                                    //Toast.makeText(context, "Call rejected => " + incomingNumber, Toast.LENGTH_LONG).show();

                                    storeData(incomingNumber);
                                    if (messCallPreferences.getShouldSendSmsPref(context)) {
                                        sendMessage(context, incomingNumber);
                                    }
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, timeout);

                    } catch (Exception e) {

                        e.printStackTrace();

                    }
                }
            }
        }
    }

    private void storeData(String incomingNumber) {
        FileHelper.saveToFile(incomingNumber);
    }

    private void sendMessage(Context context, String incomingNumber) {
        String content = messCallPreferences.getMessageContentPref(context);
        if (content != null && (incomingNumber.startsWith("9",3)
                || incomingNumber.startsWith("8", 3)
                || incomingNumber.startsWith("7", 3)
                || incomingNumber.startsWith("6", 3))) {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(content);
            smsManager.sendMultipartTextMessage(incomingNumber, null, parts, null, null);
        }
    }
}
