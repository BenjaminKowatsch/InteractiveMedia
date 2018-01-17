package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.media.interactive.cs3.hdm.interactivemedia.CallbackListener;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.data.Hash;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.UserType;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends ImagePickerActivity
        implements View.OnClickListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private Button register;
    private EditText registerUsername;
    private TextView registerUsernameError;
    private EditText registerEmail;
    private TextView registerEmailError;
    private EditText registerPassword;
    private TextView registerPasswordError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_register);

        register = (Button) findViewById(R.id.bn_register);
        registerUsername = (EditText) findViewById(R.id.et_register_username);
        registerUsernameError = (TextView) findViewById(R.id.et_register_username_error);
        registerEmail = (EditText) findViewById(R.id.et_register_email);
        registerEmailError = (TextView) findViewById(R.id.et_register_email_error);
        registerPassword = (EditText) findViewById(R.id.et_register_password);
        registerPasswordError = (TextView) findViewById(R.id.et_register_password_error);
        register.setOnClickListener(this);

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
                if (isEmailValid(editable.toString())) {
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

        initImagePickerActivity(R.id.register_profile_picture, "profile_picture.png",false);
    }

    private boolean isRegisterEnabled() {
        boolean isNameValid = isNameValid(registerUsername.getText().toString());
        boolean isPasswordValid = isPasswordValid(registerPassword.getText().toString());
        boolean isEmailValid = isEmailValid(registerEmail.getText().toString());
        return isEmailValid && isNameValid && isPasswordValid;
    }

    private boolean isNameValid(String name) {
        return name != null && name.length() > 4;
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.length() > 3;
    }

    private boolean isEmailValid(String email) {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        }
        return false;
    }

    private void navigateToHome() {
        final Intent toHome = new Intent(RegisterActivity.this, HomeActivity.class);
        toHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(toHome);
        finish();
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
                                //TODO: Update User ImageUrl
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

            default:
                Log.e(TAG, "OnClick error occurred");
                break;
        }
    }
}
