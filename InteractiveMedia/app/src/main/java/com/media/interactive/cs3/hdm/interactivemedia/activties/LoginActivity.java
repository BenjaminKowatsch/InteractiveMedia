package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.data.Hash;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.UserType;
import com.media.interactive.cs3.hdm.interactivemedia.util.CallbackListener;

import org.json.JSONObject;




/**
 * The type Login activity.
 */
public class LoginActivity extends AppCompatActivity
    implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    /**
     * The Constant TAG.
     */
    private static final String TAG = LoginActivity.class.getSimpleName();

    /**
     * The Constant REQ_CODE.
     */
    private static final int REQ_CODE = 9001;

    /**
     * The sign in.
     */
    private SignInButton signIn;

    /**
     * The google api client.
     */
    private GoogleApiClient googleApiClient;

    /**
     * The login button.
     */
    private LoginButton loginButton;

    /**
     * The callback manager.
     */
    private CallbackManager callbackManager;

    /**
     * The default login button.
     */
    private Button defaultLoginButton;

    /**
     * The login username.
     */
    private EditText loginUsername;

    /**
     * The login password.
     */
    private EditText loginPassword;

    /**
     * The register button.
     */
    private Button registerButton;

    /**
     * On create.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        loginButton = (LoginButton) findViewById(R.id.fb_login_bn);
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final Login login = Login.getInstance();
                login.setAccessToken(loginResult.getAccessToken().getToken().toString());
                login.setUserType(UserType.FACEBOOK);
                login.login(LoginActivity.this, new CallbackListener<JSONObject, Exception>() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        navigateToHome();
                    }

                    @Override
                    public void onFailure(Exception error) {
                        loginFailedHandler(error);
                    }
                });
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Facebook Login Status cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "Facebook Login Status error");
            }
        });

        signIn = (SignInButton) findViewById(R.id.bn_login);
        signIn.setOnClickListener(this);

        registerButton = (Button) findViewById(R.id.bn_create_account);
        registerButton.setOnClickListener(this);

        defaultLoginButton = (Button) findViewById(R.id.bn_default_login);
        defaultLoginButton.setOnClickListener(this);
        loginUsername = (EditText) findViewById(R.id.et_login_username);
        loginPassword = (EditText) findViewById(R.id.et_login_password);


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

    /**
     * Navigate to home activity.
     */
    private void navigateToHome() {
        final Intent toHome = new Intent(LoginActivity.this, HomeActivity.class);
        toHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(toHome);
        finish();
    }

    /**
     * Login failed handler.
     *
     * @param error the error
     * @return the void
     */
    private void loginFailedHandler(Throwable error) {
        error.printStackTrace();
        Toast.makeText(this, "Login failed please try again.", Toast.LENGTH_SHORT).show();
    }

    /**
     * On click.
     *
     * @param v the v
     */
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
                final Login login = Login.getInstance();
                login.getUser().setUsername(loginUsername.getText().toString());
                login.setHashedPassword(Hash.hashStringSha256(loginPassword.getText().toString()));
                login.setUserType(UserType.DEFAULT);
                login.login(LoginActivity.this, new CallbackListener<JSONObject, Exception>() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        navigateToHome();
                    }

                    @Override
                    public void onFailure(Exception error) {
                        loginFailedHandler(error);
                    }
                });
                break;
            default:
                Log.e(TAG, "OnClick error occurred");
                break;
        }
    }

    /**
     * On connection failed.
     *
     * @param connectionResult the connection result
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorMessage());
    }

    /**
     * Sign in.
     */
    private void signIn() {
        Log.i(TAG, "signIn called");
        final Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQ_CODE);
    }

    /**
     * Sign out.
     */
    private void signOut() {
    }

    /**
     * Handle result.
     *
     * @param result the result
     */
    private void handleResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            final GoogleSignInAccount account = result.getSignInAccount();
            final Login login = Login.getInstance();
            login.setAccessToken(account.getIdToken());
            login.getUser().setUsername(account.getDisplayName());
            login.setUserType(UserType.GOOGLE);
            login.login(LoginActivity.this, new CallbackListener<JSONObject, Exception>() {
                @Override
                public void onSuccess(JSONObject response) {
                    navigateToHome();
                }

                @Override
                public void onFailure(Exception error) {
                    loginFailedHandler(error);
                }
            });
        } else {
            Log.e(TAG, "Error during Google login");
        }
    }

    /**
     * On activity result.
     *
     * @param requestCode the request code
     * @param resultCode  the result code
     * @param data        the data
     */
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
