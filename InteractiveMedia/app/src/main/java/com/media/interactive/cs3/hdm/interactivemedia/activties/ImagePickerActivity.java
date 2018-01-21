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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.media.interactive.cs3.hdm.interactivemedia.ArrayAdapterWithIcon;
import com.media.interactive.cs3.hdm.interactivemedia.CallbackListener;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.RestRequestQueue;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by benny on 03.01.18.
 */

public class ImagePickerActivity extends AppCompatActivity {

    private static final String TAG = ImagePickerActivity.class.getSimpleName();

    private static final int REQUEST_TAKE_IMAGE = 1;
    private static final int RESULT_LOAD_IMAGE = 2;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    protected ImageView imageView;
    private boolean readExternalStoragePermissionGranted = false;
    private TextRecognizer detector;
    private EditText dateTextField = null;
    private EditText dateTimeTextField = null;
    private EditText amountTextField = null;
    private Date minimumDate;
    private Date recognizedDate = null;
    private Double recognizedAmount = null;
    private boolean ocrEnable = false;

    private String imageFilename;

    private String currentPhotoPath = null;

    /**
     * Initializes the activity parameters.
     *
     * @param viewId        Reference to the ImageView where the picked image shall be displayed
     * @param imageFilename if null a default imageFilename 'image.png' will be used
     */
    protected void initImagePickerActivity(final int viewId, String imageFilename, boolean ocrEnabled) {
        this.ocrEnable = ocrEnabled;
        this.imageFilename = imageFilename != null ? imageFilename : "image.png";
        imageView = (ImageView) findViewById(viewId);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == viewId) {
                    final Dialog dialog = createOptionsDialog();
                    dialog.show();
                } else {
                    Log.e(TAG, "OnClick error occurred");
                }

            }
        });
        detector = new TextRecognizer.Builder(getApplicationContext()).build();
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
                startActivityForResult(takePictureIntent, REQUEST_TAKE_IMAGE);
            }
        }
    }

    private File createImageFile() throws IOException {
        final File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        final File image = new File(storageDir, imageFilename);

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        Log.d(TAG, "Created Image with file path: " + currentPhotoPath);
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        Log.d(TAG, "imageView-size:" + imageView.getWidth() + " x " + imageView.getHeight());

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
        doOCR(bitmap);
        imageView.setImageBitmap(bitmap);
    }

    private void doOCR(Bitmap bitmap) {
        if(ocrEnable) {
            recognizedAmount = null;
            recognizedDate = null;
            final SimpleDateFormat sdfDate = new SimpleDateFormat("dd.MM.yyyy");
            final SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm");
            if (detector.isOperational() && bitmap != null) {
                final Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                final SparseArray<TextBlock> textBlocks = detector.detect(frame);

                final List<Double> parsedNumbers = new ArrayList<>();
                Date date = null;
                Date time = null;
                double meanValue = 0;
                for (int i = 0; i < textBlocks.size(); i++) {
                    //extract scanned text blocks here
                    final TextBlock tBlock = textBlocks.valueAt(i);
                    for (Text line : tBlock.getComponents()) {
                        //extract scanned text lines here
                        final String lineElement = line.getValue().replace(" ", "");
                        try {
                            double parsed = Double.parseDouble(lineElement.replace(",", "."));
                            parsedNumbers.add(parsed);
                            meanValue += parsed;
                        } catch (NumberFormatException e) {
                        }
                        for (Text element : line.getComponents()) {
                            //extract scanned text words here
                            final String elem = element.getValue().replace(" ", "");
                            try {
                                date = sdfDate.parse(elem);
                            } catch (ParseException e) {
                            }
                            try {
                                time = sdfTime.parse(elem);
                            } catch (ParseException e) {
                            }
                        }
                    }
                }
                if (textBlocks.size() > 0) {
                    if (date != null) {
                        recognizedDate = new Date(date.getTime());
                    }
                    if (time != null && recognizedDate != null) {
                        recognizedDate = new Date(recognizedDate.getTime() + time.getTime());
                    }
                    // Sort ascending
                    Collections.sort(parsedNumbers, new Comparator<Double>() {
                        public int compare(Double o1, Double o2) {
                            return o1.compareTo(o2);
                        }
                    });
                    meanValue /= parsedNumbers.size();
                    double variance = 0;
                    for (double possiblePrice : parsedNumbers) {
                        variance += ((possiblePrice - meanValue) * (possiblePrice - meanValue)) / (parsedNumbers.size() - 1);
                    }
                    double varianceCoefficient = Math.sqrt(variance) / meanValue;
                    // Remove autliers based on variance coefficient
                    while (varianceCoefficient > 1.1) {
                        // Remove highest entry and recalculate
                        parsedNumbers.remove(parsedNumbers.size() - 1);
                        // recalculate mean value
                        meanValue = 0;
                        for (double number : parsedNumbers) {
                            meanValue += number / parsedNumbers.size();
                        }
                        variance = 0;
                        for (double number : parsedNumbers) {
                            variance += ((number - meanValue) * (number - meanValue)) / (parsedNumbers.size() - 1);
                        }
                        varianceCoefficient = Math.sqrt(variance) / meanValue;
                    }
                    // Set second highest amount to recognizedAmount
                    if (parsedNumbers.size() >= 2) {
                        recognizedAmount = parsedNumbers.get(parsedNumbers.size() - 2);
                    }
                }
            } else {
                Log.d(TAG, "Could not set up the detector!");
            }
            Log.d(TAG, "Possible price: " + recognizedAmount);
            Log.d(TAG, "Possible date: " + recognizedDate);
            if(recognizedAmount == null && recognizedDate == null){
                makeToast("Could not recognize any price or date.\nPlease try again.");
            }
            if(recognizedDate != null && dateTextField != null && dateTimeTextField != null) {
                if(recognizedDate.after(minimumDate)) {
                    dateTextField.setText(sdfDate.format(recognizedDate));
                    dateTimeTextField.setText(sdfTime.format(recognizedDate));
                } else {
                    makeToast("Recognized date is before group creation date.");
                }
            }
            if(recognizedAmount != null && amountTextField != null){
                amountTextField.setText(String.valueOf(recognizedAmount));
            }

        }
    }

    protected void setMinimumDate(Date minimumDate){
        this.minimumDate = minimumDate;
    }

    protected void uploadImage(final CallbackListener<JSONObject, Exception> callbackListener) {
        uploadImage(currentPhotoPath, callbackListener);
    }

    private void uploadImage(String imageFilePath, final CallbackListener<JSONObject, Exception> callbackListener) {
        if (imageFilePath != null && imageFilePath.length() > 0) {
            final String url = getResources().getString(R.string.web_service_url) + "/v1/object-store/upload?filename=image.png";
            final SimpleMultiPartRequest simpleMultiPartRequest = new SimpleMultiPartRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        final JSONObject object = new JSONObject(response);
                        if (object.getBoolean("success") == false) {
                            callbackListener.onFailure(new Exception("Image upload failed"));
                        } else {
                            callbackListener.onSuccess(object);
                        }
                        Log.d(TAG, response);
                    } catch (JSONException e) {
                        callbackListener.onFailure(new Exception("Image upload failed"));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callbackListener.onFailure(new Exception("Image upload failed"));
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    final Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", Login.getInstance().getUserType().getValue() + " " + Login.getInstance().getAccessToken());

                    return params;
                }
            };

            simpleMultiPartRequest.addFile("uploadField", imageFilePath);

            RestRequestQueue.getInstance(this).addToRequestQueue(simpleMultiPartRequest);
        } else {
            callbackListener.onFailure(new Exception("No image to upload selected"));
        }
    }

    protected void makeToast(String message) {
        Toast.makeText(ImagePickerActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_IMAGE && resultCode == RESULT_OK) {
            setPic();
        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            final Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            final Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            final File original = new File(cursor.getString(columnIndex));

            try {
                final File copy = createImageFile();
                Helper.copyFile(original, copy);
            } catch (IOException e) {
                e.printStackTrace();
            }

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
                case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                    readExternalStoragePermissionGranted = true;
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isStoragePermissionGranted() {
        if (ContextCompat.checkSelfPermission(ImagePickerActivity.this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ImagePickerActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Log.v(TAG, "Permission is granted");

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                return true;
            } else {

                // No explanation needed, we can request the permission.

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(ImagePickerActivity.this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
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

    private Dialog createOptionsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Integer[] icons = new Integer[] {R.drawable.ic_menu_camera, R.drawable.ic_menu_share, R.drawable.ic_menu_gallery};
        final ListAdapter adapter = new ArrayAdapterWithIcon(this, getResources().getStringArray(R.array.image_sources), icons);

        builder.setTitle(R.string.pick_image_source)
            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // The 'which' argument contains the index position
                    // of the selected item
                    switch (which) {
                        case 0: // Camera
                            dispatchTakePictureIntent();
                            break;
                        case 1: // URL
                            startUrlDialog();
                            break;
                        case 2: // Gallery
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

    private void startUrlDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.select_image_url, null);
        dialogBuilder.setView(dialogView);

        final TextView errorMessage = (TextView) dialogView.findViewById(R.id.select_image_url_error);

        final EditText editText = (EditText) dialogView.findViewById(R.id.select_image_url);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        dialogBuilder.setTitle("Enter image URL");
        dialogBuilder.setMessage("Enter url below");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                final String url = editText.getText().toString();
                Glide.with(ImagePickerActivity.this)
                    .load(url)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .placeholder(ContextCompat.getDrawable(ImagePickerActivity.this, R.drawable.anonymoususer))
                    .into(new SimpleTarget<Bitmap>() {

                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            doOCR(resource);
                            imageView.setImageBitmap(resource);
                            try {
                                final File created = createImageFile();
                                final OutputStream outputStream = new FileOutputStream(created);
                                resource.compress(Bitmap.CompressFormat.PNG, 85, outputStream);
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {

                            currentPhotoPath = null;
                            makeToast("Image could not be loaded.");
                        }
                    });
            }

        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);
        positiveButton.setTextColor(Color.WHITE);
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (Helper.isUrlValid(editable.toString())) {
                    errorMessage.setVisibility(View.GONE);
                    positiveButton.setEnabled(true);
                } else {
                    errorMessage.setVisibility(View.VISIBLE);
                    positiveButton.setEnabled(false);
                }
            }
        });

    }

    protected String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    protected void setDateTextField(EditText dateTextField) {
        this.dateTextField = dateTextField;
    }

    protected void setAmountTextField(EditText amountTextField) {
        this.amountTextField = amountTextField;
    }

    protected void setDateTimeTextField(EditText dateTimeTextField) {
        this.dateTimeTextField = dateTimeTextField;
    }
}
