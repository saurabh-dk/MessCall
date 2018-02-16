package com.uzumaki.saurabh.messcall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.content.ContentValues.TAG;


public class CallReceiver extends BroadcastReceiver {

    String incomingNumber = "";
    AudioManager audioManager;
    TelephonyManager telephonyManager;
    private final String filename= Environment.getExternalStorageDirectory()+"/MessCall.txt";

    @Override
    public void onReceive(final Context context, Intent intent) {

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Log.d(TAG, "onReceive: ");
        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {

            // Get incoming number
            incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

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
                            Toast.makeText(context, "Call rejected => " + incomingNumber, Toast.LENGTH_LONG).show();
                            writeFile(incomingNumber);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }


                    }
                }, 1000);


            } catch (Exception e) {

                e.printStackTrace();

            }
        }
    }

    public void writeFile(String phoneNo){
        FileWriter fw=null;
        BufferedWriter bw=null;
        try {

            fw=new FileWriter(filename,true);
            bw=new BufferedWriter(fw);
            bw.write(phoneNo);

        } catch (IOException e) {
            e.printStackTrace();
        }

        finally{
            try {

                if(bw!=null)
                    bw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
