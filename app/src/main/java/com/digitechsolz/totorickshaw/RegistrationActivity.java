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

public class RegistrationActivity extends AppCompatActivity implements WebInterface {

    //Session Manager
    LoginSessionManager loginSessionManager;
    //Edit text field
    EditText name, phone, password;
    //Button
    Button reg_btn;
    //Image View
    ImageView view_pass;
    //Textview field
    TextView login_now;
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

        setContentView(R.layout.activity_registration);
        init();
    }

    private void init() {
        loginSessionManager = new LoginSessionManager(getApplicationContext());

        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        password = findViewById(R.id.password);
        reg_btn = findViewById(R.id.reg_btn);
        view_pass = findViewById(R.id.view_pass);
        login_now = findViewById(R.id.login_now);

        view_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                    view_pass.setImageResource(R.drawable.ic_visibility_off);
                    //Show Password
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    view_pass.setImageResource(R.drawable.ic_visibility);
                    //Hide Password
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().length() == 0) {
                    name.requestFocus();
                    name.setError("Name cannot be empty");
                    return;
                } else if (phone.getText().toString().length() == 0) {
                    phone.requestFocus();
                    phone.setError("Phone no. cannot be empty");
                    return;
                } else if (password.getText().toString().length() == 0) {
                    password.requestFocus();
                    password.setError("Password cannot be empty");
                    return;
                } else {
                    CheckInternet checkInternet = new CheckInternet(RegistrationActivity.this);
                    if (checkInternet.isNetworkConnected()) {
                        postLogin(name.getText().toString(),phone.getText().toString(),password.getText().toString());
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

        login_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void postLogin(String name, String phone, String pass) {
        WebServiceController wc = new WebServiceController(RegistrationActivity.this,RegistrationActivity.this);
        wc.getRequest(new RestApiUrl().REGISTRATION_URL + "?api=2b011e87-a1ea-4d8c-be5e-ec9fafac9283&name="+ name +"&phone="+ phone +"&password="+ pass, 0);
    }

    @Override
    public void getResponse(String response, int flag) {
        if (flag == 0) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject != null) {
                    if (jsonObject.getString("success").trim().equalsIgnoreCase("0")) {
                        Snackbar snackbar = Snackbar
                                .make(constraintLayout, "Phone no. already registered!", Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(Color.RED);
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setTextColor(Color.YELLOW);
                        snackbar.show();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), OtpActivity.class);
                        intent.putExtra("user_id", jsonObject.getString("user_id").trim());
                        intent.putExtra("otp", jsonObject.getString("otp").trim());
                        startActivity(intent);
                        finish();
                    }
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