package com.media.interactive.cs3.hdm.interactivemedia;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    // Google SignIn variables
    private LinearLayout profSection;
    private Button signOut;
    private SignInButton signIn;
    private TextView name;
    private TextView email;
    private ImageView profPic;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;

    // Facebook SignIn variables
    private LoginButton loginButton;
    private TextView fbStatusText;
    private CallbackManager callbackManager;

    // Default Login variables
    private Button defaultLoginButton;
    private EditText loginUsername;
    private EditText loginPassword;

    // Register variables
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton = (LoginButton) findViewById(R.id.fb_login_bn);
        fbStatusText = (TextView) findViewById(R.id.fb_status_text);
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                fbStatusText.setText("Login Status success\n"
                        + loginResult.getAccessToken().getUserId()
                        + "\n UserToken: "
                        + loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                fbStatusText.setText("Login Status cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                fbStatusText.setText("Login Status error");

            }
        });

        profSection = (LinearLayout) findViewById(R.id.prof_section);
        signOut = (Button) findViewById(R.id.bn_logout);
        signIn = (SignInButton) findViewById(R.id.bn_login);
        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        profPic = (ImageView) findViewById(R.id.prof_pic);
        signIn.setOnClickListener(this);
        signOut.setOnClickListener(this);

        registerButton = (Button) findViewById(R.id.bn_create_account);
        registerButton.setOnClickListener(this);

        defaultLoginButton = (Button) findViewById(R.id.bn_default_login);
        defaultLoginButton.setOnClickListener(this);
        loginUsername = (EditText) findViewById(R.id.et_login_username);
        loginPassword = (EditText) findViewById(R.id.et_login_password);

        profSection.setVisibility(View.GONE);
        final GoogleSignInOptions signInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_login:
                signIn();
                break;
            case R.id.bn_logout:
                signOut();
                break;
            case R.id.bn_create_account:
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                break;
            case R.id.bn_default_login:
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorMessage());
    }

    private void signIn() {
        Log.i(TAG, "signIn called");
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQ_CODE);
    }

    private void signOut() {
        Log.i(TAG, "signOut called");
        Auth.GoogleSignInApi
                .signOut(googleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUi(false);
                    }
                });
    }

    private void handleResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            final GoogleSignInAccount account = result.getSignInAccount();
            final String googleName = account.getDisplayName();
            final String googleEmail = account.getEmail();

            name.setText(googleName);
            email.setText(googleEmail);

            if (account.getPhotoUrl() != null) {
                final String googleImgUrl = account.getPhotoUrl().toString();
                Glide.with(this).load(googleImgUrl).into(profPic);
            }
            updateUi(true);
        } else {
            updateUi(false);
        }
    }

    private void updateUi(boolean isLoggedIn) {
        if (isLoggedIn) {
            profSection.setVisibility(View.VISIBLE);
            signIn.setVisibility(View.GONE);
        } else {
            profSection.setVisibility(View.GONE);
            signIn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult called");
        if (requestCode == REQ_CODE) {
            Log.i(TAG, "correct requestCode received");
            final GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi
                    .getSignInResultFromIntent(data);
            handleResult(googleSignInResult);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
}
