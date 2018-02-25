package com.uzumaki.saurabh.messcall;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST = 1; // This could be any Integer

    private MessCallPreferences messCallPreferences;
    private TextView permissionText, messageContent, textView, timeoutSeconds;
    private Switch smsSwitch, callSwitch;
    private boolean shouldSendSms = false, shouldRejectCalls = false;
    private LinearLayout editMessageLayout, timeoutLayout, timeoutAlertLayout;
    private Button editMessage, editTimeout;
    private int currentTimeout;

    private String permissionsMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionText = (TextView) findViewById(R.id.permissionText);

        smsSwitch = (Switch) findViewById(R.id.smsSwitch);
        callSwitch = (Switch) findViewById(R.id.callSwitch);

        messageContent = (TextView) findViewById(R.id.messageContent);
        editMessage = (Button) findViewById(R.id.editMessage);
        editTimeout = (Button) findViewById(R.id.editTimeout);
        textView = (TextView) findViewById(R.id.textView);
        timeoutSeconds = (TextView) findViewById(R.id.timeoutSeconds);

        timeoutLayout = (LinearLayout) findViewById(R.id.timeoutLayout);

        messCallPreferences = new MessCallPreferences();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        timeoutSeconds.setText(String.valueOf(messCallPreferences.getCallTimeout(this)));
        shouldSendSms = messCallPreferences.getShouldSendSmsPref(this);
        shouldRejectCalls = messCallPreferences.getShouldRejectCalls(this);

        if (shouldSendSms) {
            smsSwitch.setChecked(true);
            hideMessageContentView(false);
            String content = messCallPreferences.getMessageContentPref(this);
            if (content != null) {
                messageContent.setText(content);
            } else {
                messageContent.setText(R.string.no_message);
            }
        } else {
            smsSwitch.setChecked(false);
            messageContent.setText(R.string.no_message);
            hideMessageContentView(true);
        }

        if (shouldRejectCalls) {
            timeoutLayout.setVisibility(View.VISIBLE);
            callSwitch.setChecked(true);
            smsSwitch.setEnabled(true);
            hideAndDisableSmsSwitch(false);
            if (!shouldSendSms) {
                hideMessageContentView(true);
            }
        } else {
            timeoutLayout.setVisibility(View.GONE);
            callSwitch.setChecked(false);
            smsSwitch.setEnabled(false);
            hideMessageContentView(true);
            hideAndDisableSmsSwitch(true);
        }

        callSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                messCallPreferences.setShouldRejectCalls(MainActivity.this, b);
                hideAndDisableSmsSwitch(!b);
                if (b) {
                    timeoutLayout.setVisibility(View.VISIBLE);
                    if (messCallPreferences.getShouldSendSmsPref(MainActivity.this)) {
                        hideMessageContentView(false);
                    } else {
                        hideMessageContentView(true);
                    }
                } else {
                    timeoutLayout.setVisibility(View.GONE);
                    hideMessageContentView(true);
                }
            }
        });

        smsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                messCallPreferences.setShouldSendSmsPref(MainActivity.this, b);
                hideMessageContentView(!b);
            }
        });

        editMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditMessageAlert();
            }
        });

        editTimeout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditTimeoutAlert();
            }
        });
    }

    private void showEditTimeoutAlert() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        timeoutAlertLayout = (LinearLayout) this.getLayoutInflater().inflate(R.layout.edit_call_timeout_dialog, null);
        alertDialog.setView(timeoutAlertLayout);

        SeekBar timeoutSeekbar = (SeekBar) timeoutAlertLayout.findViewById(R.id.timeoutSeekbar);

        final TextView secondsTextView = (TextView) timeoutAlertLayout.findViewById(R.id.seconds);

        alertDialog.setCancelable(false);

        currentTimeout = messCallPreferences.getCallTimeout(MainActivity.this);

        timeoutSeekbar.setProgress(currentTimeout);

        secondsTextView.setText(String.valueOf(currentTimeout));

        timeoutSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentTimeout = i;
                secondsTextView.setText(String.valueOf(currentTimeout));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                messCallPreferences.setCallTimeout(MainActivity.this, currentTimeout);
                timeoutSeconds.setText(Integer.toString(messCallPreferences.getCallTimeout(MainActivity.this)));
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.create();
        alertDialog.show();
    }

    private void showEditMessageAlert() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        editMessageLayout = (LinearLayout) this.getLayoutInflater().inflate(R.layout.edit_message_dialog, null);
        alertDialog.setView(editMessageLayout);

        final AppCompatEditText message_content = (AppCompatEditText) editMessageLayout.findViewById(R.id.message_content);

        alertDialog.setCancelable(false);

        String content = messCallPreferences.getMessageContentPref(MainActivity.this);

        if (content != null) {
            message_content.setText(content);
        }

        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if ("".equals(message_content.getText().toString().trim())) {
                    messageContent.setText(R.string.no_message);
                    messCallPreferences.setMessageContentPref(MainActivity.this, null);
                } else {
                    messageContent.setText(message_content.getText().toString().trim());
                    messCallPreferences.setMessageContentPref(MainActivity.this, message_content.getText().toString().trim());
                }

            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.setTitle("Edit the message");

        alertDialog.create();
        alertDialog.show();
    }


    private void hideAndDisableSmsSwitch(boolean state) {
        if (state) {
            smsSwitch.setVisibility(View.GONE);
            smsSwitch.setEnabled(false);
        } else {
            smsSwitch.setVisibility(View.VISIBLE);
            smsSwitch.setEnabled(true);
        }
    }

    private void hideMessageContentView(boolean state) {
        if (state) hideOrShowMessageContentLayout(false);
        else hideOrShowMessageContentLayout(true);
    }

    private void hideOrShowMessageContentLayout(boolean state) {
        if (state) {
            textView.setVisibility(View.VISIBLE);
            messageContent.setVisibility(View.VISIBLE);
            editMessage.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
            messageContent.setVisibility(View.GONE);
            editMessage.setVisibility(View.GONE);
        }
    }

    private void checkAndRequestPermissions() {
        // Check if we already have the permissions.
        if (ContextCompat.checkSelfPermission(this,
                SEND_SMS) +
                ContextCompat.checkSelfPermission(this,
                        CALL_PHONE) +
                ContextCompat.checkSelfPermission(this,
                        READ_PHONE_STATE) +
                ContextCompat.checkSelfPermission(this,
                        WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{SEND_SMS, CALL_PHONE, READ_PHONE_STATE, WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
        } else {
            // We already have all the needed permissions.
            permissionText.setText(R.string.all_permissions_done);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

        permissionsMessage = getString(R.string.all_permissions_done);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    for (int result :
                            grantResults) {
                        if (result == PackageManager.PERMISSION_DENIED) {
                            permissionsMessage = "\nSome permissions were denied. The app will be disabled.";
                            disableApp();
                            break;
                        }
                    }
                } else {
                    permissionsMessage = "\nSome permissions were denied. The app will be disabled.";
                    disableApp();
                }
                break;
        }

        permissionText.setText(permissionsMessage);
    }

    private void disableApp() {
        callSwitch.setChecked(false);
        callSwitch.setEnabled(false);
        smsSwitch.setChecked(false);
        smsSwitch.setEnabled(false);
        hideMessageContentView(true);
    }
}