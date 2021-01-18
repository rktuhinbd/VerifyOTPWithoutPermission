package com.example.readotpwithoutpermission.view;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.readotpwithoutpermission.helper.AppSignatureHelper;
import com.example.readotpwithoutpermission.R;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.auth.api.credentials.HintRequest;

public class MainActivity extends AppCompatActivity {

    // = = = Declare view properties = = = //

    private EditText et_phone;
    private TextView tv_next;


    // = = = Declare data properties = = = //

    private static final String TAG = "OTP UID";
    private final int RESOLVE_HINT = 1001;
    private String phoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generateUID();
        requestHint();

        et_phone = findViewById(R.id.et_phone);
        tv_next = findViewById(R.id.tv_next);

        tv_next.setOnClickListener(v -> {

            phoneNumber = et_phone.getText().toString().trim();

            if (phoneNumber != null) {
                if (!phoneNumber.isEmpty()) {
                    // sendPhoneNumberToServer(phoneNumber);
                    Intent intent = new Intent(this, VerifyOTP.class);
                    intent.putExtra("phone", phoneNumber);
                    startActivity(intent);  // Go to next activity
                } else {
                    Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_LONG).show();
            }
        });

    }


    // = = = Generate App Signature (UID). Sms will auto verify using only this UID at the end.
    // TODO Don't forget to remove the this block and AppSignatureHelper class before production release //
    private void generateUID() {
        AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
        for (String signature : appSignatureHelper.getAppSignatures()) {
            Log.e(TAG, signature);  // See the log and get app's hash string.
        }
    }


    // = = = Construct a request for phone numbers and show the picker
    private void requestHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        CredentialsOptions options = new CredentialsOptions.Builder()
                .forceEnableSaveDialog()
                .build();

        PendingIntent intent = Credentials.getClient(this, options).getHintPickerIntent(hintRequest);

        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // This block for request hint --> getting user phone number.
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                if (credential != null) {
                    et_phone.setText(credential.getId());
                }
            }
        }
    }

}
