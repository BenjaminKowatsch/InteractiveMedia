package com.media.interactive.cs3.hdm.interactivemedia;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_register);
        register = (Button) findViewById(R.id.bn_register);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_register:
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
