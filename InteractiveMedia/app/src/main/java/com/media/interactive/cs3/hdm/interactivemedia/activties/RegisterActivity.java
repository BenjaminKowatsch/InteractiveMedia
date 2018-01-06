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

    private void dispatchTakePictureIntent() {
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "IO Exception while creating image file");
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                final Uri photoURI = FileProvider.getUriForFile(this,
                    "com.media.interactive.cs3.hdm.interactivemedia.fileprovider",
                    photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        final File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        final File image = new File(storageDir, PROFILE_PICTURE_FILE_NAME);

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "Created Image with file path: " + currentPhotoPath);
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = profilePicture.getWidth();
        int targetH = profilePicture.getHeight();
        Log.d(TAG, "profilePicture-size:" + profilePicture.getWidth() + " x " + profilePicture.getHeight());

        // Get the dimensions of the bitmap
        final BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        Log.d(TAG, "photoW:" + photoW + " x photoH " + photoH);

        // Determine how much to scale down the image
        int scaleFactor = Math.min(Math.round((float) photoW / (float) targetW), Math.round((float) photoH / (float) targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        Log.d(TAG, "Bitmap-size:" + bitmap.getWidth() + " x " + bitmap.getHeight());
        profilePicture.setImageBitmap(bitmap);
    }

    private void uploadImage(String imageFilePath) {
        if(imageFilePath != null && imageFilePath.length() > 0) {
            final String url = getResources().getString(R.string.web_service_url) + "/v1/object-store/upload?filename=image.png";
            SimpleMultiPartRequest simpleMultiPartRequest = new SimpleMultiPartRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        final JSONObject object = new JSONObject(response);
                        if (object.getBoolean("success") == false) {
                            makeToast("Image upload failed");
                        } else {
                            final JSONObject payload = object.getJSONObject("payload");
                            Log.d(TAG,"Path returned: "+ payload.getString("path"));
                            Login.getInstance().setProfilePicture(payload.getString("path"));

                            final Intent toHome = new Intent(RegisterActivity.this, HomeActivity.class);
                            toHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(toHome);
                            finish();
                        }
                        Log.d(TAG, response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        makeToast("Image upload failed");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    makeToast("Image upload failed");
                    Log.e(TAG, error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    final Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", Login.getInstance().getUserType().getValue() + " " + Login.getInstance().getAccessToken());

                    return params;
                }
            };
            ;

            simpleMultiPartRequest.addFile("uploadField", imageFilePath);
            RestRequestQueue.getInstance(this).addToRequestQueue(simpleMultiPartRequest);
        } else {
            Log.d(TAG, "No image to upload selected");
            final Intent toHome = new Intent(RegisterActivity.this, HomeActivity.class);
            toHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(toHome);
            finish();
        }
    }

    private void makeToast(String message){
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic();
        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            final Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            final Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            currentPhotoPath = cursor.getString(columnIndex);
            cursor.close();

            setPic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                    readExternalStoragePermissionGranted = true;
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isStoragePermissionGranted() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Log.v(TAG, "Permission is granted");

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                return true;
            } else {

                // No explanation needed, we can request the permission.

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(RegisterActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return false;
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        return true;
    }

    private void dispatchPickPictureIntent() {
        final Intent i = new Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    private Dialog createDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pick_image_source)
            .setItems(R.array.image_sources, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // The 'which' argument contains the index position
                    // of the selected item
                    switch (which) {
                        case 0:
                            dispatchTakePictureIntent();
                            break;
                        case 1:
                            if (true == readExternalStoragePermissionGranted) {
                                dispatchPickPictureIntent();
                            } else {
                                makeToast("Permission denied, cannot pick image.");
                            }
                            break;
                        default:
                            Log.e(TAG, "onCreateDialog: Error while selecting item at dialog");
                            break;
                    }
                }
            });
        return builder.create();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_register:
                final Login login = Login.getInstance();
                login.setUsername(registerUsername.getText().toString());
                login.setUserType(UserType.DEFAULT);
                login.setEmail(registerEmail.getText().toString());
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
