package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.media.interactive.cs3.hdm.interactivemedia.CallbackListener;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.RestRequestQueue;
import com.media.interactive.cs3.hdm.interactivemedia.authorizedrequests.AuthorizedJsonObjectRequest;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.DatabaseProviderHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.SynchronisationHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.util.MoneyTextWatcher;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import static com.google.android.gms.location.places.ui.PlacePicker.getPlace;

public class AddTransactionActivity extends ImagePickerActivity {
  public static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY);
  public static final String GROUP_TO_ADD_TO = "GroupToAddTo";
  public static final String GROUP_CREATED_AT_ADD_TO = "GroupCreatedAtToAddTo";
  private static final String TAG = AddTransactionActivity.class.getSimpleName();
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
  private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
  private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(dateFormat.toPattern() + timeFormat.toPattern());
  private Spinner userSelection;
  private EditText dateEditText;
  private EditText timeEditText;
  private String groupId;
  private String groupCreatedAt;
  private TextView locationDisplay;
  private DatabaseProviderHelper helper;
  private SimpleCursorAdapter userAdapter;
  private Place selectedPlace = null;
  private final static int PLACE_PICKER_REQUEST = 3;

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PLACE_PICKER_REQUEST) {
      if (resultCode == RESULT_OK) {
        selectedPlace = getPlace(this, data);
        final String toastMsg = String.format("Place: %s %s", selectedPlace.getAddress(), selectedPlace.getLatLng().toString());
        if (selectedPlace != null) {
          final LatLng latLng = selectedPlace.getLatLng();
          locationDisplay.setText("Location: \n" + selectedPlace.getAddress() + "\n"
              + "Latitude: " + latLng.latitude + "\n"
              + "Longitude: " + latLng.longitude);

        } else {
          locationDisplay.setText(null);
        }
        Log.d(TAG, toastMsg);
        Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
      }
    }
  }

  private void startLocationSelection() {
    final PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
    try {
      startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
    } catch (GooglePlayServicesRepairableException e) {
      e.printStackTrace();
    } catch (GooglePlayServicesNotAvailableException e) {
      e.printStackTrace();
    }

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_transaction);

    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle("");

    dateEditText = findViewById(R.id.et_add_transaction_date);
    timeEditText = findViewById(R.id.et_add_transaction_time);
    userSelection = findViewById(R.id.s_add_transaction_user);
    EditText amountEditText = findViewById(R.id.et_add_transaction_amount);
    amountEditText.addTextChangedListener(new MoneyTextWatcher(amountEditText, CURRENCY_FORMAT));

    helper = new DatabaseProviderHelper(getContentResolver());

    groupId = getIntent().getStringExtra(GROUP_TO_ADD_TO);
    groupCreatedAt = getIntent().getStringExtra(GROUP_CREATED_AT_ADD_TO);


    locationDisplay = findViewById(R.id.transaction_location_display);

    final Button addTransactionButton = findViewById(R.id.bn_add_transaction);
    addTransactionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        createAndSaveTransaction(view);
      }
    });
    final Button cancel = findViewById(R.id.bn_add_transaction_cancel);
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
      }
    });

    final Button locationButton = findViewById(R.id.transaction_location);
    locationButton.setText(locationButton.getHint());

    locationButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        //Toast.makeText(AddTransactionActivity.this, "Soon implemented", Toast.LENGTH_SHORT).show();
        startLocationSelection();
      }
    });

    setupDatePicker();

    userAdapter = initializeUserAdapter();
    userSelection.setAdapter(userAdapter);

    final UUID randomUUID = UUID.randomUUID();
    final String randomFilename = randomUUID.toString() + ".png";
    initImagePickerActivity(R.id.iv_transaction_image, randomFilename, true);
    setDateTextField(dateEditText);
    setDateTimeTextField(timeEditText);
    setAmountTextField(amountEditText);
  }

  private Group loadGroup() {
    if (groupId == null) {
      Log.e(this.getClass().getSimpleName(), "Intent is missing id of group");
      return null;
    } else {
      return new DatabaseHelper(this).getGroupWithUsers(groupId);
    }
  }

  private void createAndSaveTransaction(View view) {
    final Transaction toSave = buildFromCurrentView();
    toSave.setSynched(false);
    // Upload group image if sending the group data was successfull
    if (getCurrentPhotoPath() != null) {
      toSave.setImageUrl(getCurrentPhotoPath());
    }
    try {
      Log.d(TAG, toSave.toJson().toString());
    } catch (JSONException e) {
      e.printStackTrace();
    }
    uploadImage(new CallbackListener<JSONObject, Exception>() {
      @Override
      public void onSuccess(JSONObject response) {
        helper.setTransactionImageUrlByResponse(AddTransactionActivity.this, toSave, response);
        try {
          sendToBackend(toSave);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onFailure(Exception error) {
        makeToast(error.getMessage());
        try {
          sendToBackend(toSave);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    });

  }

  private void sendToBackend(final Transaction toSave) throws JSONException {
    helper.saveTransaction(toSave);
    Login.getInstance().getSynchronisationHelper().requestTransactionsByGroupId(this, toSave.getGroup().getGroupId(), groupCreatedAt, new CallbackListener<JSONObject, Exception>() {
      @Override
      public void onSuccess(JSONObject response) {
        final String url = getResources().getString(R.string.web_service_url).concat("/v1/groups/").concat(toSave.getGroup().getGroupId()).concat("/transactions");
        Log.d(TAG, "url: " + url);
        try {
          final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
              Request.Method.POST, url, toSave.toJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
              // Update Transaction with Response
              Log.d(TAG, " ******************BeforeUpdate: "+toSave.toString());
              try {
                if(response.getBoolean("success")) {
                  helper.updateTransactionWithResponse(toSave, response.getJSONObject("payload"));
                }
              } catch (JSONException e) {
                e.printStackTrace();
              }
              Log.d(TAG, " ******************AfterUpdate: "+toSave.toString());
              Log.d(TAG, " ****************** "+response.toString());
              finish();
            }
          }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
              makeToast("Error while sending the group to backend.");
              finish();
            }
          });
          RestRequestQueue.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onFailure(Exception error) {
        makeToast("Could not pull transactions before pushing.");
        finish();
      }
    });

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
    final String split = "even";
    final double amount = parseAmount(amountText);
    final Date dateTime = parseDateTime(dateText, timeText);
    //FIXME: replace this with real location
    Location location = null;
    if (selectedPlace != null) {
      location = new Location("");
      final LatLng latLng = selectedPlace.getLatLng();
      location.setLongitude(latLng.longitude);
      location.setLatitude(latLng.latitude);
    }
    final String paidBy = userAdapter.getCursor().getString(userAdapter.getCursor().getColumnIndex(UserTable.COLUMN_USER_ID));
    Log.d(TAG, "paidBy: " + paidBy);
    return new Transaction(purpose, paidBy, split, dateTime, location, amount, groupId);
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

    final String[] projection = {UserTable.TABLE_NAME + ".*"};
    final String selection = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_GROUP_ID + " = ?";
    final String[] selectionArgs = {groupId};
    Cursor query = getContentResolver().query(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI, projection, selection, selectionArgs, null);

    String[] columns = new String[] {UserTable.COLUMN_USERNAME};
    int[] to = new int[] {android.R.id.text1};

    SimpleCursorAdapter userAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, query, columns, to, 0);
    userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    return userAdapter;
  }

}
