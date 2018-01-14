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

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.MoneyTextWatcher;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.util.ImageUploadCallbackListener;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class AddTransactionActivity extends ImagePickerActivity {
    public static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    public static final String GROUP_TO_ADD_TO = "GroupToAddTo";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(dateFormat.toPattern() + timeFormat.toPattern());
    private Spinner userSelection;
    private EditText dateEditText;
    private EditText timeEditText;
    private long groupId;
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

        groupId = getIntent().getLongExtra(GROUP_TO_ADD_TO, -1);

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
        saveToLocalDatabase(toSave);
        finish();
    }

    private Transaction buildFromCurrentView() {
        final EditText name = findViewById(R.id.et_add_transaction_purpose);
        final TextView split = findViewById(R.id.tv_add_transaction_split);
        final EditText amount = findViewById(R.id.et_add_transaction_amount);
        return buildTransaction(name, split, dateEditText, timeEditText, amount);
    }

    private void saveToLocalDatabase(Transaction transaction) {
        ContentValues transactionContent = transaction.toContentValues();
        Uri id = getContentResolver().insert(DatabaseProvider.CONTENT_TRANSACTION_URI, transactionContent);
        if (id != null) {
            ContentValues transactionGroupContent = new ContentValues();
            final int transactionId = Integer.parseInt(id.getLastPathSegment());
            transactionGroupContent.put(GroupTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
            transactionGroupContent.put(GroupTransactionTable.COLUMN_GROUP_ID, transaction.getGroupId());
            getContentResolver().insert(DatabaseProvider.CONTENT_GROUP_TRANSACTION_URI, transactionGroupContent);
        }
    }

    private Transaction buildTransaction(EditText nameText, TextView splitText,
                                         EditText dateText, EditText timeText, EditText amountText) {
        final String purpose = nameText.getText().toString();
        final String split = splitText.getText().toString();
        final double amount = parseAmount(amountText);
        final Date dateTime = parseDateTime(dateText, timeText);
        final ImageUploadCallbackListener imageUploadCallbackListener =
                new ImageUploadCallbackListener(getResources().getString(R.string.web_service_url));
        uploadImage(imageUploadCallbackListener);
        //FIXME: replace this with real location
        final Location location = new Location("");
        return new Transaction(purpose, userAdapter.getCursor().getString(1), split, dateTime,
                imageUploadCallbackListener.getImageUrl(), location, amount, groupId);
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
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        Cursor query = databaseHelper.getUsersForGroup(groupId);

        String[] columns = new String[] { UserTable.COLUMN_USERNAME };
        int[] to = new int[] { android.R.id.text1 };

        SimpleCursorAdapter userAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, query, columns, to, 0);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return userAdapter;
    }

}
