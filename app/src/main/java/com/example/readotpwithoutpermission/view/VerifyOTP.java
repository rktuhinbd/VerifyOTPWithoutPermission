package com.example.readotpwithoutpermission.view;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.readotpwithoutpermission.R;
import com.example.readotpwithoutpermission.helper.SmsBroadcastReceiver;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.Task;

public class VerifyOTP extends AppCompatActivity {

    // = = = Declare view properties = = = //

    private LinearLayout layout_progress;
    private LinearLayout layout_progress2;
    private TextView tv_message;
    private TextView tv_next;
    private EditText et_otp;
    private ImageButton ib_back;


    // = = = Declare data properties = = = //

    private SmsBroadcastReceiver smsBroadcastReceiver;
    private final int beginIndex = 15;
    private final int endIndex = 21;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_o_t_p);

        initiateViewProperties();
        clickActions();
        smsRetriever();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSmsRetriever();     // making SMS User Consent API enabled
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(smsBroadcastReceiver);   // making SMS User Consent API disabled
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initiateViewProperties() {
        layout_progress = findViewById(R.id.layout_progress);
        layout_progress2 = findViewById(R.id.layout_progress2);
        tv_message = findViewById(R.id.tv_message);
        tv_next = findViewById(R.id.tv_next);
        et_otp = findViewById(R.id.et_otp);
        ib_back = findViewById(R.id.ib_back);

        String phoneNumber = getIntent().getExtras().getString("phone");
        tv_message.setText(getString(R.string.otp_message) + "\n" + phoneNumber);
    }

    private void clickActions() {
        ib_back.setOnClickListener(v -> finish());

        tv_next.setOnClickListener(v -> {
            if (et_otp.getText().toString().trim().length() == 6) {
                layout_progress.setVisibility(View.VISIBLE);
                layout_progress2.setVisibility(View.GONE);
                new Handler().postDelayed(() -> {
                    layout_progress.setVisibility(View.GONE);
                    layout_progress2.setVisibility(View.VISIBLE);
                }, 3000);

            } else {
                Toast.makeText(this, "Please enter the OTP we sent", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registerSmsRetriever() {
        smsBroadcastReceiver = new SmsBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(smsBroadcastReceiver, intentFilter);
    }

    private void smsRetriever() {

        SmsRetrieverClient client = SmsRetriever.getClient(this);
        Task<Void> task = client.startSmsRetriever();

        task.addOnSuccessListener(aVoid -> SmsBroadcastReceiver.initSMSListener(new SmsBroadcastReceiver
                .SMSListener() {
            @Override
            public void onSuccess(String message) {
                if (message != null) {
                    Log.e("Received Message", message + "");

                    /**
                     * Remember SMS format should be like below, otherwise the app will not verify sms
                     *  1. Sms should be started with <#>
                     *  2. Now your sms body containing the OTP. Count the starting index of OTP starting
                     *     and ending index of OTP which will be needed to set the code in OTP editText.
                     *  3. Sms must be end with the 11-digit UID we you generated using AppSignatureHelper class
                     * */

                    et_otp.setText(parseOneTimeCode(message));
                }
            }

            @Override
            public void onError(String message) {
                if (message != null)
                    Log.e("OTP Expired", message);
            }
        }));

        task.addOnFailureListener(e -> e.printStackTrace());

    }


    private String parseOneTimeCode(String otp) {
        return otp.substring(beginIndex, endIndex);
    }

}
