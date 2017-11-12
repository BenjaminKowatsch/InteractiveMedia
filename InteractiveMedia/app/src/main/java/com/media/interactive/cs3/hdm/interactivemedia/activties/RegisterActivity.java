package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.data.Hash;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_register:
                final User user = User.getInstance();
                user.setUsername(registerUsername.getText().toString());
                user.setEmail(registerEmail.getText().toString());
                user.setHashedPassword(Hash.hashStringSHA256(registerPassword.getText().toString()));
                user.register(RegisterActivity.this);
                Toast.makeText(getApplicationContext(),
                        "TODO: create REST request to backend",
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, "TODO: create REST request to backend");
                break;
            default:
                Log.e(TAG, "OnClick error occurred");
                break;
        }
    }
}
