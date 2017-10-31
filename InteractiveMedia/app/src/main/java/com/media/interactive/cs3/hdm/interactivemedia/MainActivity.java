package com.media.interactive.cs3.hdm.interactivemedia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.media.interactive.cs3.hdm.interactivemedia.data.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Hash;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String appTag;
    private String hasRun;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(this);

        /*
        databaseHelper.deleteAllUsers();
        databaseHelper.resetDatabase();

        final User testUser = User.getInstance();
        testUser.setUsername("Test User");
        testUser.setEmail("user.test@gmail.com");
        testUser.setHashedPassword(Hash.hashStringSHA256("Passwort1234"));
        databaseHelper.insertUser(testUser);
        databaseHelper.deleteAllUsers();
        */

        User.getInstance().clear();

        User.getInstance().setDatabaseHelper(databaseHelper);

        appTag = getResources().getString(R.string.app_tag);
        hasRun = getResources().getString(R.string.has_run);
        sharedPreferences = getSharedPreferences(appTag, MODE_PRIVATE);
        launchNextActivity();
    }

    private void launchNextActivity() {
        Intent intent;
        if (sharedPreferences.getBoolean(hasRun, false) && User.getInstance().login()) {
            /* This is not the first run */
            Log.d(TAG, "Launching Home Activity");
            intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            /* Do first run stuff */
            Log.d(TAG, "Launching Login Activity");
            intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            sharedPreferences.edit()
                    .putBoolean(hasRun, true)
                    .commit();
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        launchNextActivity();
    }
}
