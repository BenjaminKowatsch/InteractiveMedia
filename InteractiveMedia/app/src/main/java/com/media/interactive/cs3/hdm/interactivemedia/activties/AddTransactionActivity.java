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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.adapter.SplitAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.data.DatabaseProviderHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.ConstantDeduction;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.EvenSplit;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.Split;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.SplitFactory;
import com.media.interactive.cs3.hdm.interactivemedia.database.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.util.CallbackListener;
import com.media.interactive.cs3.hdm.interactivemedia.util.DecimalDigitsInputFilter;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;
import com.media.interactive.cs3.hdm.interactivemedia.views.NonScrollRecyclerView;
import com.media.interactive.cs3.hdm.interactivemedia.views.RecyclerItemTouchHelper;
import com.media.interactive.cs3.hdm.interactivemedia.volley.AuthorizedJsonObjectRequest;
import com.media.interactive.cs3.hdm.interactivemedia.volley.RestRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

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


/**
 * The Class AddTransactionActivity.
 */
public class AddTransactionActivity extends ImagePickerActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    /**
     * The Constant CURRENCY_FORMAT.
     */
    public static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    /**
     * The Constant GROUP_TO_ADD_TO.
     */
    public static final String GROUP_TO_ADD_TO = "GroupToAddTo";

    /**
     * The Constant GROUP_CREATED_AT_ADD_TO.
     */
    public static final String GROUP_CREATED_AT_ADD_TO = "GroupCreatedAtToAddTo";

    /**
     * The Constant TAG.
     */
    private static final String TAG = AddTransactionActivity.class.getSimpleName();

    /**
     * The Constant DECIMALS_BEFORE_POINT.
     */
    private static final int DECIMALS_BEFORE_POINT = 8;

    /**
     * The Constant DECIMALS_AFTER_POINT.
     */
    private static final int DECIMALS_AFTER_POINT = 2;

    /**
     * The Constant PLACE_PICKER_REQUEST.
     */
    private static final int PLACE_PICKER_REQUEST = 3;

    /**
     * The date format.
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    /**
     * The time format.
     */
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    /**
     * The date time format.
     */
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(dateFormat.toPattern() + timeFormat.toPattern());

    /**
     * The decimal filter.
     */
    private final InputFilter decimalFilter = new DecimalDigitsInputFilter(DECIMALS_BEFORE_POINT, DECIMALS_AFTER_POINT);

    /**
     * The user selection.
     */
    private Spinner userSelection;

    /**
     * The date edit text.
     */
    private EditText dateEditText;

    /**
     * The time edit text.
     */
    private EditText timeEditText;

    /**
     * The group id.
     */
    private String groupId;

    /**
     * The group created at.
     */
    private String groupCreatedAt;

    /**
     * The location display.
     */
    private TextView locationDisplay;

    /**
     * The helper.
     */
    private DatabaseProviderHelper helper;

    /**
     * The user adapter.
     */
    private SimpleCursorAdapter userAdapter;

    /**
     * The name.
     */
    private EditText name;

    /**
     * The amount.
     */
    private EditText amount;

    /**
     * The selected place.
     */
    private Place selectedPlace = null;

    /**
     * The splits view.
     */
    private NonScrollRecyclerView splitsView;

    /**
     * The splits adapter.
     */
    private SplitAdapter splitsAdapter;

    /**
     * The split list.
     */
    private List<Split> splitList;

    /**
     * The linear layout.
     */
    private LinearLayout linearLayout;

    /**
     * The valid amount.
     */
    private boolean validAmount;

    /**
     * On activity result. Is called when a user picked a location.
     *
     * @param requestCode the request code
     * @param resultCode  the result code
     * @param data        the data
     */
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

    /**
     * Start location selection via intent.
     */
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

    /**
     * On create.
     *
     * @param savedInstanceState the saved instance state
     */
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
        final EditText amountEditText = findViewById(R.id.et_add_transaction_amount);

        final Button cancel = findViewById(R.id.bn_add_transaction_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        splitList = new ArrayList<>();
        final List<User> userList = getAllGroupUsers();
        splitsAdapter = new SplitAdapter(this, splitList, userList);
        splitsView.setAdapter(splitsAdapter);
        final ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(splitsView);

        final TextView amountError = findViewById(R.id.et_add_transaction_amount_error);
        final TextView nameError = findViewById(R.id.et_add_transaction_purpose_error);

        amountEditText.setFilters(new InputFilter[]{decimalFilter});

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

        final UUID randomUuid = UUID.randomUUID();
        final String randomFilename = randomUuid.toString() + ".png";
        initImagePickerActivity(R.id.iv_transaction_image, randomFilename, true);
        setDateTextField(dateEditText);
        setDateTimeTextField(timeEditText);
        setAmountTextField(amountEditText);
        setMinimumDate(Helper.parseDateString(groupCreatedAt));

        splitList.add(new EvenSplit());
    }

    /**
     * Gets the all group users from the database.
     *
     * @return the all group users
     */
    private List<User> getAllGroupUsers() {
        final Cursor userCursor = initializeUserAdapter().getCursor();
        final List<User> out = new ArrayList<>();
        while (userCursor.moveToNext()) {
            out.add(UserTable.extractUserFromCurrentPosition(userCursor));
        }
        return out;
    }

    /**
     * Create and show the add deduction dialog.
     */
    private void showAddDeductionDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        final LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_constant_deduction, null);
        dialogBuilder.setView(dialogView);

        final TextView errorMessage = dialogView.findViewById(R.id.add_split_error);
        final EditText editText = dialogView.findViewById(R.id.add_split_amount);
        final TextView dotErrorMessage = dialogView.findViewById(R.id.add_split_dot_error);
        editText.setFilters(new InputFilter[]{decimalFilter});

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
                    if (editable.toString().length() > 0 && !editable.toString().equals(".")) {
                        dotErrorMessage.setVisibility(View.GONE);
                        final double parse = Double.parseDouble(editable.toString());
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

    /**
     * Creates a transaction data object using the UI data.
     * Tries to upload the optional transaction image. Then uploads the transaction data.
     * If no image is selected the transaction is immediately uploaded.
     *
     * @param view the view
     */
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

    /**
     * Saves the transaction into the SQL database marked as 'unsynchronized'.
     * Tries to upload the transaction to the backend.
     * If it succeeds the transaction is marked as 'synchronized'.
     *
     * @param toSave the to save
     * @throws JSONException the JSON exception
     */
    private void sendToBackend(final Transaction toSave) throws JSONException {
        helper.saveTransaction(toSave);
        Login.getInstance().getSynchronisationHelper()
            .requestTransactionsByGroupId(this, toSave.getGroup().getGroupId(),
                groupCreatedAt, new CallbackListener<JSONObject, Exception>() {
            @Override
            public void onSuccess(JSONObject response) {
                final String url = getResources().getString(R.string.web_service_url).concat(getString(R.string.requestPathAddTransaction1))
                    .concat(toSave.getGroup().getGroupId()).concat(getString(R.string.requestPathAddTransaction2));
                Log.d(TAG, "url: " + url);
                try {
                    final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
                        Request.Method.POST, url, toSave.toJson(), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Update Transaction with Response
                            try {
                                if (response.getBoolean("success")) {
                                    helper.updateTransactionWithResponse(toSave, response.getJSONObject("payload"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            finish();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            makeToast(getString(R.string.requestErrorMessageAddTransaction));
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
                makeToast(getString(R.string.requestErrorMessagePullTransactions));
                finish();
            }
        });

    }

    /**
     * Builds the from current view.
     *
     * @return the transaction
     */
    private Transaction buildFromCurrentView() {
        return buildTransaction(name, splitsView, dateEditText, timeEditText, amount);
    }

    /**
     * Builds the transaction.
     *
     * @param nameText   the name text
     * @param splitsView the splits view
     * @param dateText   the date text
     * @param timeText   the time text
     * @param amountText the amount text
     * @return the transaction
     */
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

    /**
     * Parses the amount.
     *
     * @param amountText the amount text
     * @return the double
     */
    private double parseAmount(EditText amountText) {
        return Double.parseDouble(amountText.getText().toString());
    }

    /**
     * Parses the date time.
     *
     * @param dateText the date text
     * @param timeText the time text
     * @return the date
     */
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

    /**
     * Setup date picker.
     */
    private void setupDatePicker() {
        final Calendar calendar = Calendar.getInstance();

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

    /**
     * Sets the time text.
     *
     * @param hourOfDay    the hour of day
     * @param minute       the minute
     * @param timeEditText the time edit text
     */
    private void setTimeText(int hourOfDay, int minute, EditText timeEditText) {
        final Calendar date = Calendar.getInstance();
        date.set(0, 0, 0, hourOfDay, minute);
        timeEditText.setText(timeFormat.format(date.getTime()));
    }

    /**
     * Sets the date text.
     *
     * @param year         the year
     * @param month        the month
     * @param day          the day
     * @param dateEditText the date edit text
     */
    private void setDateText(int year, int month, int day, EditText dateEditText) {
        final Calendar date = Calendar.getInstance();
        date.set(year, month, day);
        dateEditText.setText(dateFormat.format(date.getTime()));
    }

    /**
     * Initialize the user adapter using the 'groupId' to request data from the database.
     *
     * @return the simple cursor adapter
     */
    private SimpleCursorAdapter initializeUserAdapter() {

        final String[] projection = {UserTable.TABLE_NAME + ".*"};
        final String selection = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_GROUP_ID + " = ?";
        final String[] selectionArgs = {groupId};
        final Cursor query = getContentResolver().query(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI, projection, selection, selectionArgs, null);

        final String[] columns = new String[]{UserTable.COLUMN_USERNAME};
        final int[] to = new int[]{android.R.id.text1};

        final SimpleCursorAdapter userAdapter = new SimpleCursorAdapter(this, R.layout.group_spinner_item, query, columns, to, 0);
        userAdapter.setDropDownViewResource(R.layout.group_spinner_dropdown_item);
        return userAdapter;
    }

    /**
     * On swiped.
     *
     * @param viewHolder the view holder
     * @param direction  the direction
     * @param position   the position
     */
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
