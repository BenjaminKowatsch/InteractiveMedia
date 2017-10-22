package com.media.interactive.cs3.hdm.interactivemedia;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

    private LinearLayout profSection;
    private Button signOut;
    private SignInButton signIn;
    private TextView name, email;
    private ImageView profPic;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;

    private LoginButton loginButton;
    private TextView fbStatusText;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton =(LoginButton) findViewById(R.id.fb_login_bn);
        fbStatusText = (TextView) findViewById(R.id.fb_status_text);
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                fbStatusText.setText("Login Status success\n"+ loginResult.getAccessToken().getUserId()+ "\n UserToken: "+ loginResult.getAccessToken().getToken());
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

        profSection.setVisibility(View.GONE);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions).build();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bn_login:
                signIn();
                break;
            case R.id.bn_logout:
                signOut();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("MainActivity", connectionResult.getErrorMessage());
    }

    private void signIn(){
        Log.i("MainActivity","signIn called");
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent,REQ_CODE);
    }

    private void signOut(){
        Log.i("MainActivity","signOut called");
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUI(false);
            }
        });
    }

    private void handleResult(GoogleSignInResult result){
        if(result.isSuccess()){
            final GoogleSignInAccount account = result.getSignInAccount();
            final String googleName = account.getDisplayName();
            final String googleEmail = account.getEmail();

            name.setText(googleName);
            email.setText(googleEmail);

            if(account.getPhotoUrl() != null) {
                final String googleImgUrl = account.getPhotoUrl().toString();
                Glide.with(this).load(googleImgUrl).into(profPic);
            }
            updateUI(true);
        } else {
            updateUI(false);
        }
    }

    private void updateUI(boolean isLoggedIn){
        if(isLoggedIn){
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
        Log.i("MainActivity","onActivityResult called");
        if(requestCode == REQ_CODE){
            Log.i("MainActivity","correct requestCode received");
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(googleSignInResult);
        } else {
            callbackManager.onActivityResult(requestCode,resultCode,data);
        }
    }
}
