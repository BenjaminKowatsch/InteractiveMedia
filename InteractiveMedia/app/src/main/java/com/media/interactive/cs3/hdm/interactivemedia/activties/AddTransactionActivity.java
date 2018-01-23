package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.DatabaseProviderHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.ConstantDeduction;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.EvenSplit;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.Split;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.SplitFactory;
import com.media.interactive.cs3.hdm.interactivemedia.recyclerview.NonScrollRecyclerView;
import com.media.interactive.cs3.hdm.interactivemedia.recyclerview.RecyclerItemTouchHelper;
import com.media.interactive.cs3.hdm.interactivemedia.recyclerview.SplitAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.util.DecimalDigitsInputFilter;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.google.android.gms.location.places.ui.PlacePicker.getPlace;

public class AddTransactionActivity extends ImagePickerActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    public static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    public static final String GROUP_TO_ADD_TO = "GroupToAddTo";
    public static final String GROUP_CREATED_AT_ADD_TO = "GroupCreatedAtToAddTo";
    private static final String TAG = AddTransactionActivity.class.getSimpleName();
    private static final int DECIMALS_BEFORE_POINT = 8;
    private static final int DECIMALS_AFTER_POINT = 2;
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
    private EditText name;
    private EditText amount;
    private Place selectedPlace = null;
    private final static int PLACE_PICKER_REQUEST = 3;
    private NonScrollRecyclerView splitsView;
    private SplitAdapter splitsAdapter;
    private List<Split> splitList;
    private LinearLayout linearLayout;
    private boolean validAmount;
    private final InputFilter decimalFilter = new DecimalDigitsInputFilter(DECIMALS_BEFORE_POINT, DECIMALS_AFTER_POINT);

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                selectedPlace = getPlace(this, data);
                final String toastMsg = String.format("Place: %s %s", selectedPlace.getAddress(), selectedPlace.getLatLng().toString());
                if (selectedPlace != null) {
                    final LatLng latLng = selectedPlace.getLatLng();
                    locationDisplay.setText("Location: \n" + selectedPlace.getAddress() + "\n"
                            + "Latitude:  " + String.format("%.2f", latLng.latitude) + "\n"
                            + "Longitude:  " + String.format("%.2f", latLng.longitude));

                } else {
                    locationDisplay.setText(null);
                }
                Log.d(TAG, toastMsg);
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
        groupId = getIntent().getStringExtra(GROUP_TO_ADD_TO);
        groupCreatedAt = getIntent().getStringExtra(GROUP_CREATED_AT_ADD_TO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        linearLayout = findViewById(R.id.add_transaction_linear_layout);
        dateEditText = findViewById(R.id.et_add_transaction_date);
        timeEditText = findViewById(R.id.et_add_transaction_time);
        userSelection = findViewById(R.id.s_add_transaction_user);

        splitsView = findViewById(R.id.add_transaction_splits);

        splitsView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        splitsView.setItemAnimator(new DefaultItemAnimator());
        splitsView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        EditText amountEditText = findViewById(R.id.et_add_transaction_amount);

        final Button cancel = findViewById(R.id.bn_add_transaction_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        splitList = new ArrayList<>();
        List<User> userList = getAllGroupUsers();
        splitsAdapter = new SplitAdapter(this, splitList, userList);
        splitsView.setAdapter(splitsAdapter);
        final ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(splitsView);

        final TextView amountError = findViewById(R.id.et_add_transaction_amount_error);
        final TextView nameError = findViewById(R.id.et_add_transaction_purpose_error);

        amountEditText.setFilters(new InputFilter[] {decimalFilter});

        helper = new DatabaseProviderHelper(getContentResolver());

        name = findViewById(R.id.et_add_transaction_purpose);
        amount = amountEditText;
        locationDisplay = findViewById(R.id.transaction_location_display);

        final Button addTransactionButton = findViewById(R.id.bn_add_transaction);
        addTransactionButton.setEnabled(false);

        final Button addSplitButton = findViewById(R.id.bn_add_transaction_add_split);
        addSplitButton.setEnabled(false);
        addSplitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDeductionDialog();
            }
        });

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                validAmount = amount.getText().toString().length() > 0 && !amount.getText().toString().equals(".");
                boolean validName = (name.getText().toString().length() > 0);

                addSplitButton.setEnabled(validAmount);
                amountError.setVisibility(validAmount ? View.GONE : View.VISIBLE);
                nameError.setVisibility(validName ? View.GONE : View.VISIBLE);
                addTransactionButton.setEnabled(validName && validName);
            }
        };

        name.addTextChangedListener(textWatcher);
        amount.addTextChangedListener(textWatcher);


        addTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAndSaveTransaction(view);
            }
        });

        final Button locationButton = findViewById(R.id.transaction_location);
        locationButton.setText(locationButton.getHint());
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        setMinimumDate(Helper.parseDateString(groupCreatedAt));

        splitList.add(new EvenSplit());
    }

    private List<User> getAllGroupUsers() {
        final Cursor userCursor = initializeUserAdapter().getCursor();
        List<User> out = new ArrayList<>();
        while (userCursor.moveToNext()) {
            out.add(UserTable.extractUserFromCurrentPosition(userCursor));
        }
        return out;
    }

    private void showAddDeductionDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        final LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_constant_deduction, null);
        dialogBuilder.setView(dialogView);

        final TextView errorMessage = dialogView.findViewById(R.id.add_split_error);
        final EditText editText = dialogView.findViewById(R.id.add_split_amount);
        final TextView dotErrorMessage = dialogView.findViewById(R.id.add_split_dot_error);
        editText.setFilters(new InputFilter[] {decimalFilter});

        final Spinner userSelection = dialogView.findViewById(R.id.add_split_user);
        final SimpleCursorAdapter dialogUserAdapter = initializeUserAdapter();
        userSelection.setAdapter(dialogUserAdapter);

        dialogBuilder.setTitle("Add deduction for split");
        dialogBuilder.setMessage("Choose user that should pay and the amount");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String userId = dialogUserAdapter.getCursor().getString(dialogUserAdapter.getCursor().getColumnIndex(UserTable.COLUMN_USER_ID));
                splitList.add(0, new ConstantDeduction(parseAmount(editText), userId));
                splitsAdapter.update(splitList);
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

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if(editable.toString().length() > 0 && !editable.toString().equals(".")) {
                        dotErrorMessage.setVisibility(View.GONE);
                        double parse = Double.parseDouble(editable.toString());
                        if (parse < parseAmount(amount)) {
                            errorMessage.setVisibility(View.GONE);
                            positiveButton.setEnabled(true);
                        } else {
                            errorMessage.setVisibility(View.VISIBLE);
                            positiveButton.setEnabled(false);
                        }
                    } else {
                        dotErrorMessage.setVisibility(View.VISIBLE);
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Text in " + editable + " could not be parsed", e);
                }
            }
        });

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
                Log.d(TAG, error.getMessage());
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
                            Log.d(TAG, " ******************BeforeUpdate: " + toSave.toString());
                            try {
                                if (response.getBoolean("success")) {
                                    helper.updateTransactionWithResponse(toSave, response.getJSONObject("payload"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG, " ******************AfterUpdate: " + toSave.toString());
                            Log.d(TAG, " ****************** " + response.toString());
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
        return buildTransaction(name, splitsView, dateEditText, timeEditText, amount);
    }

    private Transaction buildTransaction(EditText nameText, NonScrollRecyclerView splitsView,
                                         EditText dateText, EditText timeText, EditText amountText) {
        final String purpose = nameText.getText().toString();
        final Split split = SplitFactory.buildSplitFromList(splitList);
        final double amount = parseAmount(amountText);
        final Date dateTime = parseDateTime(dateText, timeText);
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
        return Double.parseDouble(amountText.getText().toString());
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

        String[] columns = new String[]{UserTable.COLUMN_USERNAME};
        int[] to = new int[]{android.R.id.text1};

        SimpleCursorAdapter userAdapter = new SimpleCursorAdapter(this, R.layout.group_spinner_item, query, columns, to, 0);
        userAdapter.setDropDownViewResource(R.layout.group_spinner_dropdown_item);
        return userAdapter;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof SplitAdapter.SimpleViewHolder) {
            // get the removed item name to display it in snack bar
            final Split split = splitList.get(viewHolder.getAdapterPosition());

            // backup of removed item for undo purpose
            final Split deletedSplit = splitList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            splitsAdapter.removeSplit(viewHolder.getAdapterPosition());

            // showing snack bar with Undo option
            final Snackbar snackbar = Snackbar
                    .make(linearLayout, split + " removed from split list", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    splitsAdapter.restoreSplit(deletedSplit, deletedIndex);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }

    }
}
