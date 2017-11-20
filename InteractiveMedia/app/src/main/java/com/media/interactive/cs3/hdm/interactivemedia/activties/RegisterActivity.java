package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.data.Hash;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.UserType;

public class RegisterActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private Button register;
    private EditText registerUsername;
    private EditText registerEmail;
    private EditText registerPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_register);
        register = (Button) findViewById(R.id.bn_register);
        registerUsername = (EditText) findViewById(R.id.et_register_username);
        registerEmail = (EditText) findViewById(R.id.et_register_email);
        registerPassword = (EditText) findViewById(R.id.et_register_password);
        register.setOnClickListener(this);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_register:
                final Login login = Login.getInstance();
                login.setUsername(registerUsername.getText().toString());
                login.setUserType(UserType.DEFAULT);
                login.setEmail(registerEmail.getText().toString());
                login.setHashedPassword(Hash.hashStringSHA256(registerPassword.getText().toString()));
                login.register(RegisterActivity.this)
                        .thenAccept((Void) -> {
                            Toast.makeText(getApplicationContext(),
                                    "Success fully logged in",
                                    Toast.LENGTH_SHORT).show();
                            final Intent toHome = new Intent(RegisterActivity.this, HomeActivity.class);
                            toHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(toHome);
                            finish();
                        })
                        .exceptionally((error) -> {
                            Log.e(TAG, "error: "+ error.getMessage());
                            throw new RuntimeException(error.getMessage());
                        });

                break;
            default:
                Log.e(TAG, "OnClick error occurred");
                break;
        }
    }
}
