package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.android.volley.error.AuthFailureError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.media.interactive.cs3.hdm.interactivemedia.CallbackListener;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.RestRequestQueue;
import com.media.interactive.cs3.hdm.interactivemedia.data.Hash;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.UserType;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity
    implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private static final String PROFILE_PICTURE_FILE_NAME = "profile_picture.jpg";
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int RESULT_LOAD_IMAGE = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private boolean readExternalStoragePermissionGranted = false;
    private String currentPhotoPath = null;
    private ImageView profilePicture;

    private Button register;
    private EditText registerUsername;
    private EditText registerEmail;
    private EditText registerPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_register);
        profilePicture = (ImageView) findViewById(R.id.register_profile_picture);
        register = (Button) findViewById(R.id.bn_register);
        registerUsername = (EditText) findViewById(R.id.et_register_username);
        registerEmail = (EditText) findViewById(R.id.et_register_email);
        registerPassword = (EditText) findViewById(R.id.et_register_password);
        register.setOnClickListener(this);
        profilePicture.setOnClickListener(this);
        readExternalStoragePermissionGranted = isStoragePermissionGranted();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_register:
                final Login login = Login.getInstance();
                login.getUser().setUsername(registerUsername.getText().toString());
                login.setUserType(UserType.DEFAULT);
                login.getUser().setEmail(registerEmail.getText().toString());
                login.setHashedPassword(Hash.hashStringSha256(registerPassword.getText().toString()));
                login.register(RegisterActivity.this, new CallbackListener<JSONObject, Exception>() {
                    @Override
                    public void onSuccess(JSONObject param) {
                        Toast.makeText(getApplicationContext(),
                            "Success fully logged in",
                            Toast.LENGTH_SHORT).show();
                        uploadImage(currentPhotoPath);
                        /*
                        final Intent toHome = new Intent(RegisterActivity.this, HomeActivity.class);
                        toHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(toHome);
                        finish();*/
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e(TAG, "error: " + error.getMessage());
                    }
                });

                break;
            case R.id.register_profile_picture:
                final Dialog dialog = createDialog();
                dialog.show();
                break;
            default:
                Log.e(TAG, "OnClick error occurred");
                break;
        }
    }
}
