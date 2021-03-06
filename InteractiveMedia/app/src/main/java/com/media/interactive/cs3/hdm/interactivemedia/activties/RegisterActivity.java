package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.data.Hash;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.UserType;
import com.media.interactive.cs3.hdm.interactivemedia.util.CallbackListener;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * The type Register activity.
 */
public class RegisterActivity extends ImagePickerActivity
    implements View.OnClickListener {

    /**
     * The Constant TAG.
     */
    private static final String TAG = RegisterActivity.class.getSimpleName();

    /**
     * The register.
     */
    private Button register;

    /**
     * The register username.
     */
    private EditText registerUsername;

    /**
     * The register username error.
     */
    private TextView registerUsernameError;

    /**
     * The register email.
     */
    private EditText registerEmail;

    /**
     * The register email error.
     */
    private TextView registerEmailError;

    /**
     * The register password.
     */
    private EditText registerPassword;

    /**
     * The register password error.
     */
    private TextView registerPasswordError;

    /**
     * On create.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_register);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        register = (Button) findViewById(R.id.bn_register);
        registerUsername = (EditText) findViewById(R.id.et_register_username);
        registerUsernameError = (TextView) findViewById(R.id.et_register_username_error);
        registerEmail = (EditText) findViewById(R.id.et_register_email);
        registerEmailError = (TextView) findViewById(R.id.et_register_email_error);
        registerPassword = (EditText) findViewById(R.id.et_register_password);
        registerPasswordError = (TextView) findViewById(R.id.et_register_password_error);
        register.setOnClickListener(this);

        final Button cancel = findViewById(R.id.bn_register_cancel);
        cancel.setOnClickListener(this);

        register.setEnabled(false);

        registerEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        registerUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isNameValid(editable.toString())) {
                    registerUsernameError.setVisibility(View.GONE);
                    register.setEnabled(isRegisterEnabled());
                } else {
                    registerUsernameError.setVisibility(View.VISIBLE);
                    register.setEnabled(false);
                }
            }
        });

        registerEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (Helper.isEmailValid(editable.toString())) {
                    registerEmailError.setVisibility(View.GONE);
                    register.setEnabled(isRegisterEnabled());
                } else {
                    registerEmailError.setVisibility(View.VISIBLE);
                    register.setEnabled(false);
                }
            }
        });

        registerPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isPasswordValid(editable.toString())) {
                    registerPasswordError.setVisibility(View.GONE);
                    register.setEnabled(isRegisterEnabled());
                } else {
                    registerPasswordError.setVisibility(View.VISIBLE);
                    register.setEnabled(false);
                }
            }
        });

        initImagePickerActivity(R.id.register_profile_picture, "profile_picture.png", false);
    }

    /**
     * Checks if is register enabled.
     *
     * @return true, if is register enabled
     */
    private boolean isRegisterEnabled() {
        final boolean isNameValid = isNameValid(registerUsername.getText().toString());
        final boolean isPasswordValid = isPasswordValid(registerPassword.getText().toString());
        final boolean isEmailValid = Helper.isEmailValid(registerEmail.getText().toString());
        return isEmailValid && isNameValid && isPasswordValid;
    }

    /**
     * Checks if is name valid.
     *
     * @param name the name
     * @return true, if is name valid
     */
    private boolean isNameValid(String name) {
        return name != null && name.length() > 4;
    }

    /**
     * Checks if is password valid.
     *
     * @param password the password
     * @return true, if is password valid
     */
    private boolean isPasswordValid(String password) {
        return password != null && password.length() > 3;
    }

    /**
     * Navigate to home activity.
     */
    private void navigateToHome() {
        final Intent toHome = new Intent(RegisterActivity.this, HomeActivity.class);
        toHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(toHome);
        finish();
    }

    /**
     * On click.
     *
     * @param v the v
     */
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

                        uploadImage(new CallbackListener<JSONObject, Exception>() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                makeToast("Uploading image succeeded. ");
                                JSONObject payload = null;
                                String imageName = null;
                                try {
                                    payload = response.getJSONObject("payload");
                                    imageName = payload.getString("path");
                                    Log.d(TAG, "Path returned: " + payload.getString("path"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                final String newImageUrl = getResources().getString(R.string.web_service_url)
                                    .concat("/v1/object-store/download?filename=").concat(imageName);
                                Login.getInstance().getUser().setImageUrl(newImageUrl);
                                Login.getInstance().updateImageUrl(RegisterActivity.this);

                                navigateToHome();
                            }

                            @Override
                            public void onFailure(Exception error) {
                                makeToast("Uploading image failed. ");
                                navigateToHome();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e(TAG, "error: " + error.getMessage());
                        makeToast("Registration failed, please try again.");
                    }
                });

                break;
            case R.id.bn_register_cancel:
                finish();
                break;
            default:
                Log.e(TAG, "OnClick error occurred");
                break;
        }
    }

}
