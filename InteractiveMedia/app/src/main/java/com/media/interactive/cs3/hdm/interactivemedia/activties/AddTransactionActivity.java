package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;

import java.util.Date;

public class AddTransactionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

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
    }

    private void createAndSaveTransaction(View view) {
        Transaction toSave = buildFromCurrentView();
        saveToLocalDatabase(toSave);
        finish();
    }

    private Transaction buildFromCurrentView() {
        final EditText name = findViewById(R.id.et_add_transaction_purpose);
        final TextView split = findViewById(R.id.tv_add_transaction_split);
        final DatePicker date = findViewById(R.id.dp_add_transaction_date);
        final TimePicker time = findViewById(R.id.tp_add_transaction_time);
        final EditText amount = findViewById(R.id.et_add_transaction_amount);
        return buildTransaction(name, split, date, time, amount);
    }

    private void saveToLocalDatabase(Transaction transaction) {
        ContentValues transactionContent = transaction.toContentValues();
        Uri id = getContentResolver().insert(DatabaseProvider.CONTENT_TRANSACTION_URI, transactionContent);
    }

    private Transaction buildTransaction(EditText nameText, TextView splitText,
                                         DatePicker datePicker, TimePicker timePicker, EditText amountText) {
        final String purpose = nameText.getText().toString();
        final String split = splitText.getText().toString();
        final Date dateTime = toLocalDateTime(datePicker, timePicker);
        final double amount = Double.parseDouble(amountText.getText().toString());
        return new Transaction(purpose, split, dateTime, amount);
    }

    private Date toLocalDateTime(DatePicker datePicker, TimePicker timePicker) {
        return new Date(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                timePicker.getHour(), timePicker.getMinute());
    }

}
