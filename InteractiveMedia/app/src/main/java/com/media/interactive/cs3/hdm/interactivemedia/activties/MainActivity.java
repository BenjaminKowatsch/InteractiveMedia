package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DummyDataAdder;
import com.media.interactive.cs3.hdm.interactivemedia.data.Hash;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String appTag;
    private String hasRun;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(this);


        //databaseHelper.deleteAllUsers();
        databaseHelper.resetDatabase();

        final Login testUser = Login.getInstance();
        testUser.setUsername("Test User");
        testUser.setEmail("user.test@gmail.com");
        testUser.setHashedPassword(Hash.hashStringSha256("Passwort1234"));
        //databaseHelper.deleteAllUsers();

        DummyDataAdder dummyDataAdder = new DummyDataAdder(this);
        dummyDataAdder.addAllDummyData();


        Login.getInstance().clear();

        appTag = getResources().getString(R.string.app_tag);
        hasRun = getResources().getString(R.string.has_run);
        sharedPreferences = getSharedPreferences(appTag, MODE_PRIVATE);

        Log.e(TAG, "Exceptionally Thread Id: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void launchNextActivity() {
        Login.getInstance().login(MainActivity.this)
            .thenAccept(Void -> {
                if (sharedPreferences.getBoolean(hasRun, false)) {
                    // This is not the first run
                    Log.d(TAG, "Launching Home Activity");
                    final Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    throw new RuntimeException("Not first run");
                }
            })
            .exceptionally(error -> {
                Log.d(TAG, "Login failed due to " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
                // Do first run stuff
                Log.d(TAG, "Launching Login Activity"); // TODO: change intent to Login activity
                final Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                sharedPreferences.edit()
                    .putBoolean(hasRun, true)
                    .commit();
                throw new RuntimeException(error.getMessage());
            }).thenAccept((result) -> {
                finish();
            });
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        launchNextActivity();
    }
}
