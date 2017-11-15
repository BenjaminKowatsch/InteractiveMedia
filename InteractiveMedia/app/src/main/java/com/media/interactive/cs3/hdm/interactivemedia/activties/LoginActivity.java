package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.RestRequestQueue;
import com.media.interactive.cs3.hdm.interactivemedia.data.Hash;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;
import com.media.interactive.cs3.hdm.interactivemedia.data.UserType;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.util.concurrent.CompletableFuture;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;


public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "LoginActivity";

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = (LoginButton) findViewById(R.id.fb_login_bn);
        fbStatusText = (TextView) findViewById(R.id.fb_status_text);
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(LoginResult loginResult) {
                final User user = User.getInstance();
                user.setAccessToken(loginResult.getAccessToken().getToken().toString());
                user.setUserType(UserType.FACEBOOK);
                user.login(LoginActivity.this)
                        .thenAccept(LoginActivity.this::navigateToHome)
                        .exceptionally(LoginActivity.this::loginFailedHandler);
                /*
                navigateToHome();
                final String url = getResources().getString(R.string.web_service_url).concat("/facebook_login");
                Log.d(TAG,"url: "+ url);
                JSONObject data = new JSONObject();
                String accessToken = null;
                accessToken = loginResult.getAccessToken().getToken().toString();
                try {
                    data.put("accessToken", accessToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RestRequestQueue.getInstance(LoginActivity.this)
                        .send(url, Request.Method.POST,data)
                        .thenAccept(User.getInstance()::loginResponseHandler)
                        .exceptionally(error -> {
                            throw new RuntimeException(error.getMessage(),error.getCause());
                        });
                */
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

        final String serverClientId = getString(R.string.server_client_id);
        final GoogleSignInOptions signInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();
    }

    private void navigateToHome(Void aVoid) {
        final Intent toHome = new Intent(LoginActivity.this, HomeActivity.class);
        toHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(toHome);
        finish();
    }

    private Void loginFailedHandler(Throwable error) {
            Log.d(TAG,"Login failed");
            throw new RuntimeException(error.getMessage());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                break;
            case R.id.bn_default_login:
                final User user = User.getInstance();
                user.setUsername(loginUsername.getText().toString());
                user.setHashedPassword(Hash.hashStringSHA256(loginPassword.getText().toString()));
                user.setUserType(UserType.DEFAULT);
                user.login(LoginActivity.this)
                        .thenAccept(LoginActivity.this::navigateToHome)
                        .exceptionally(LoginActivity.this::loginFailedHandler);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void handleResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            final GoogleSignInAccount account = result.getSignInAccount();
            final User user = User.getInstance();
            user.setAccessToken(account.getIdToken());
            user.setUserType(UserType.GOOGLE);
            user.login(LoginActivity.this)
                    .thenAccept(LoginActivity.this::navigateToHome)
                    .exceptionally(LoginActivity.this::loginFailedHandler);
            /*
            if (account.getPhotoUrl() != null) {
                final String googleImgUrl = account.getPhotoUrl().toString();
                Glide.with(this).load(googleImgUrl).into(profPic);
            }*/

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

    @RequiresApi(api = Build.VERSION_CODES.N)
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
