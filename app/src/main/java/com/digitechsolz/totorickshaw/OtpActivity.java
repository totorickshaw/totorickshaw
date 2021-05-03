package com.digitechsolz.totorickshaw;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class OtpActivity extends AppCompatActivity implements WebInterface {

    //Session Manager
    LoginSessionManager loginSessionManager;
    //Edit text field
    EditText otp_value;
    //Button
    Button submit_btn;
    //Coordinatelayout for Snackbar
    ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }

        setContentView(R.layout.activity_otp);
        init();
    }

    private void init() {
        loginSessionManager = new LoginSessionManager(getApplicationContext());

        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);
        otp_value = findViewById(R.id.otp_value);
        submit_btn = findViewById(R.id.submit_btn);

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otp_value.getText().toString().length() == 0) {
                    otp_value.requestFocus();
                    otp_value.setError("OTP cannot be empty");
                    return;
                } else if (otp_value.getText().toString().length() < 4 || otp_value.getText().toString().length() > 4) {
                    otp_value.requestFocus();
                    otp_value.setError("OTP must be 4 character length");
                    return;
                } else if (!otp_value.getText().toString().equalsIgnoreCase(getIntent().getStringExtra("otp"))) {
                    otp_value.requestFocus();
                    otp_value.setError("Please enter correct OTP");
                    return;
                } else {
                    CheckInternet checkInternet = new CheckInternet(OtpActivity.this);
                    if (checkInternet.isNetworkConnected()) {
                        postOtp(otp_value.getText().toString());
                    } else {
                        Snackbar snackbar = Snackbar
                                .make(constraintLayout, "No Internet, Try to connect!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent=new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                        startActivity(intent);
                                    }
                                });
                        snackbar.setActionTextColor(Color.RED);
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setTextColor(Color.YELLOW);
                        snackbar.show();
                    }
                }
            }
        });
    }

    private void postOtp(String otp_val) {
        WebServiceController wc = new WebServiceController(OtpActivity.this,OtpActivity.this);
        wc.getRequest(new RestApiUrl().COMPLREG_URL + "?api=2b011e87-a1ea-4d8c-be5e-ec9fafac9283&user_id="+ getIntent().getStringExtra("user_id"), 0);
    }

    @Override
    public void getResponse(String response, int flag) {
        if (flag == 0) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject != null) {
                    HashMap<String, String> loginIndata = new HashMap<>();
                    loginIndata.put("user_id", jsonObject.getString("user_id").trim());

                    loginSessionManager.createLoginSession(loginIndata);

                    Intent intent = new Intent(getApplicationContext(), LocationSetupActivity.class);
                    startActivity(intent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailure(int flag) {

    }
}