package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.MoneyTextWatcher;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {
    public static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    public static final String GROUP_TO_ADD_TO = "GroupToAddTo";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(dateFormat.toPattern() + timeFormat.toPattern());
    private EditText dateEditText;
    private EditText timeEditText;
    private long groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
        dateEditText = findViewById(R.id.et_add_transaction_date);
        timeEditText = findViewById(R.id.et_add_transaction_time);
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
        setupDatePicker();
    }

    private void createAndSaveTransaction(View view) {
        Transaction toSave = buildFromCurrentView();
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
        return new Transaction(purpose, split, dateTime, amount, groupId);
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

}
