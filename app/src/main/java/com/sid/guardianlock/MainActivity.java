package com.sid.guardianlock;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.app.admin.DeviceAdminReceiver;


public class MainActivity extends AppCompatActivity {

    private final int REQUEST_ENABLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String passcode = (int)(Math.random()*899999+100000)+"";
        final DevicePolicyManager devicePolicyManager =(DevicePolicyManager)getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        final ComponentName demoDeviceAdmin =new ComponentName(this, MyAdmin.class);
        Button button = (Button)findViewById(R.id.button);
        final EditText editText = (EditText)findViewById(R.id.editText);
        final CheckBox checkBox = (CheckBox)findViewById(R.id.checkBox), checkBox2 = (CheckBox)findViewById(R.id.checkBox2);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });
        if (!devicePolicyManager.isAdminActive(demoDeviceAdmin)) {
            // try to become active – must happen here in this activity, to get result
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    demoDeviceAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    R.string.app_desc);
            startActivityForResult(intent, REQUEST_ENABLE);
        } else {
            // Already is a device administrator, can do security operations now.
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean guardianMode = checkBox.isChecked(), prankMode = checkBox2.isChecked();
                    if(guardianMode) {
                        devicePolicyManager.resetPassword(passcode, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                        //devicePolicyManager.lockNow();
                        if(prankMode)
                            for(int x=0; x<9999; x++) sendSMS(editText.getText().toString(), "Nuclear code: " + passcode + ". Reply \"STOP\" to cancel.");
                        else sendSMS(editText.getText().toString(), passcode);
                    }else if(prankMode) sendSMS(editText.getText().toString(), "Nuclear code: " + (int)(Math.random()*899999+100000) + ". Reply \"STOP\" to cancel.");
                }
            });

        }

        //IF EVER LOCKED OUT OF PHONE, COMMENT EVERYTHING AFTER VARIABLE DECLARATION/INSTANTIATION ABOVE AND UNCOMMENT SECTION BELOW
        /*devicePolicyManager.resetPassword(passcode, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        sendSMS("14082197895", passcode);*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_ENABLE == requestCode)
        {
            if (resultCode == Activity.RESULT_OK) {
                // Has become the device administrator
            } else {
                //Canceled or failed
            }
        }
    }

    public static class MyAdmin extends DeviceAdminReceiver {
        // implement onEnabled(), onDisabled(), …

    }

    private void sendSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }
}
