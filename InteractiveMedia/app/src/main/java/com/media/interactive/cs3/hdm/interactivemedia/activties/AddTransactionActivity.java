package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.media.interactive.cs3.hdm.interactivemedia.CallbackListener;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.RestRequestQueue;
import com.media.interactive.cs3.hdm.interactivemedia.authorizedrequests.AuthorizedJsonObjectRequest;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.DatabaseProviderHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.MoneyTextWatcher;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;

import org.json.JSONException;
import org.json.JSONObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class AddTransactionActivity extends ImagePickerActivity {
    private static final String TAG = AddTransactionActivity.class.getSimpleName();
    public static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    public static final String GROUP_TO_ADD_TO = "GroupToAddTo";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(dateFormat.toPattern() + timeFormat.toPattern());
    private Spinner userSelection;
    private EditText dateEditText;
    private EditText timeEditText;
    private String groupId;
    private DatabaseProviderHelper helper;
    private SimpleCursorAdapter userAdapter;
    private AtomicInteger placePickerId = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
        dateEditText = findViewById(R.id.et_add_transaction_date);
        timeEditText = findViewById(R.id.et_add_transaction_time);
        userSelection = findViewById(R.id.s_add_transaction_user);
        EditText amountEditText = findViewById(R.id.et_add_transaction_amount);
        amountEditText.addTextChangedListener(new MoneyTextWatcher(amountEditText, CURRENCY_FORMAT));

        helper = new DatabaseProviderHelper(getContentResolver());

        groupId = getIntent().getStringExtra(GROUP_TO_ADD_TO);

        Button addTransactionButton = findViewById(R.id.bn_add_transaction);
        addTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAndSaveTransaction(view);
            }
        });
        Button cancel = findViewById(R.id.bn_add_transaction_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button locationButton = findViewById(R.id.transaction_location);

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AddTransactionActivity.this, "Soon implemented", Toast.LENGTH_SHORT).show();
            }
        });

        setupDatePicker();

        userAdapter = initializeUserAdapter();
        userSelection.setAdapter(userAdapter);

        initImagePickerActivity(R.id.iv_transaction_image, null);
    }

    private void createAndSaveTransaction(View view) {
        final Transaction toSave = buildFromCurrentView();
        helper.saveTransaction(toSave);
        finish();
    }

    private void sendToBackend(Transaction toSave) throws JSONException {
        final String url = getResources().getString(R.string.web_service_url).concat("/v1/groups/").concat(toSave.getGroupId()).concat("/transactions");
        Log.d(TAG, "url: " + url);
        final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
            Request.Method.POST, url, toSave.toJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                makeToast("Error while sending the group to backend.");
            }
        });
        RestRequestQueue.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }


    private Transaction buildFromCurrentView() {
        final EditText name = findViewById(R.id.et_add_transaction_purpose);
        final TextView split = findViewById(R.id.tv_add_transaction_split);
        final EditText amount = findViewById(R.id.et_add_transaction_amount);
        return buildTransaction(name, split, dateEditText, timeEditText, amount);
    }



    private Transaction buildTransaction(EditText nameText, TextView splitText,
                                         EditText dateText, EditText timeText, EditText amountText) {
        final String purpose = nameText.getText().toString();
        final String split = splitText.getText().toString();
        final double amount = parseAmount(amountText);
        final Date dateTime = parseDateTime(dateText, timeText);
        final ImageUploadCallbackListener imageUploadCallbackListener = new ImageUploadCallbackListener();
        uploadImage(imageUploadCallbackListener);
        //FIXME: replace this with real location
        final Location location = new Location("");
        return new Transaction(purpose, userAdapter.getCursor().getString(1), split, dateTime,
                imageUploadCallbackListener.imageUrl, location, amount, groupId);
    }

    private double parseAmount(EditText amountText) {
        try {
            final Number parsed = CURRENCY_FORMAT.parse(amountText.getText().toString());
            return parsed.doubleValue();
        } catch (ParseException e) {
            Log.e(this.getClass().getName(), e.getMessage());
        }
        return -1d;
    }

    private Date parseDateTime(EditText dateText, EditText timeText) {
        final String dateTimeText = dateText.getText().toString() + timeText.getText().toString();
        try {
            return dateTimeFormat.parse(dateTimeText);
        } catch (ParseException e) {
            Log.e(this.getClass().getName(), "Could not parse dateTime from text " + dateTimeText
                    + " using default of now instead.");
            Log.d(this.getClass().getName(), e.getMessage());
            return new Date(System.currentTimeMillis());
        }
    }

    private void setupDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // implement the date picker dialog
        final DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                setDateText(year, month, day, dateEditText);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar
                .DAY_OF_MONTH));

        final TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                setTimeText(hourOfDay, minute, timeEditText);
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

        // prevent showing keyboard
        dateEditText.setInputType(InputType.TYPE_NULL);
        timeEditText.setInputType(InputType.TYPE_NULL);

        dateEditText.setText(dateFormat.format(new Date(System.currentTimeMillis())));
        timeEditText.setText(timeFormat.format(new Date(System.currentTimeMillis())));

        // register from edit text listener
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePicker.show();
            }
        });

        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePicker.show();
            }
        });
    }

    private void setTimeText(int hourOfDay, int minute, EditText timeEditText) {
        Calendar date = Calendar.getInstance();
        date.set(0, 0, 0, hourOfDay, minute);
        timeEditText.setText(timeFormat.format(date.getTime()));
    }

    private void setDateText(int year, int month, int day, EditText dateEditText) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day);
        dateEditText.setText(dateFormat.format(date.getTime()));
    }

    private SimpleCursorAdapter initializeUserAdapter() {

        final String[] projection = { UserTable.TABLE_NAME+".*"};
        final String selection = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_GROUP_ID + " = ?";
        final String[] selectionArgs = {groupId};
        Cursor query = getContentResolver().query(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI, projection,selection,selectionArgs,null);

        String[] columns = new String[] { UserTable.COLUMN_USERNAME };
        int[] to = new int[] { android.R.id.text1 };

        SimpleCursorAdapter userAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, query, columns, to, 0);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return userAdapter;
    }

    private class ImageUploadCallbackListener extends CallbackListener<JSONObject, Exception> {
        private String imageUrl;

        @Override
        public void onSuccess(JSONObject response) {
            JSONObject payload;
            String imageName = null;
            try {
                payload = response.getJSONObject("payload");
                imageName = payload.getString("path");
                Log.d(this.getClass().getName(), "Path returned: " + payload.getString("path"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final String newImageUrl = getResources().getString(R.string.web_service_url)
                    .concat("/v1/object-store/download?filename=").concat(imageName);
            imageUrl = newImageUrl;
        }

        @Override
        public void onFailure(Exception error) {
            imageUrl = null;
            makeToast(error.getMessage());
        }
    }
}
