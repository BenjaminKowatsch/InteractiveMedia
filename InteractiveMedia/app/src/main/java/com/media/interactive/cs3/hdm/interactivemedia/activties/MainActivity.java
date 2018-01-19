package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.media.interactive.cs3.hdm.interactivemedia.CallbackListener;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String appTag;
    private String hasRun;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;
    private Boolean transactionReload = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(this);

        databaseHelper.resetDatabase();

        Login.getInstance().clear();

        appTag = getResources().getString(R.string.app_tag);
        hasRun = getResources().getString(R.string.has_run);
        sharedPreferences = getSharedPreferences(appTag, MODE_PRIVATE);

        transactionReload = getIntent().getExtras().getBoolean("transactionReload");

        Log.e(TAG, "Exceptionally Thread Id: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
    }



    private void launchNextActivity() {
        Login.getInstance().login(MainActivity.this, new CallbackListener<JSONObject, Exception>() {
            @Override
            public void onSuccess(JSONObject param) {
                if (sharedPreferences.getBoolean(hasRun, false)) {
                    // This is not the first run
                    Log.d(TAG, "Launching Home Activity");
                    final Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    if(transactionReload != null && transactionReload){
                        Log.d(TAG, "transactionReload set");
                        intent.putExtra("transactionReload",true);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Not first run");
                }
                finish();
            }

            @Override
            public void onFailure(Exception error) {
                Log.d(TAG, "Login failed due to " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
                // Do first run stuff
                Log.d(TAG, "Launching Login Activity");
                final Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                sharedPreferences.edit()
                    .putBoolean(hasRun, true)
                    .commit();
                finish();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        launchNextActivity();
    }
}
