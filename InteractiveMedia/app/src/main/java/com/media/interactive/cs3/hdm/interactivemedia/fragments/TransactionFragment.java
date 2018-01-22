package com.media.interactive.cs3.hdm.interactivemedia.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.TransactionAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity;
import com.media.interactive.cs3.hdm.interactivemedia.activties.TransactionDetailViewActivity;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.PaymentTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.settlement.PaymentAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.CURRENCY_FORMAT;
import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.GROUP_CREATED_AT_ADD_TO;
import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.GROUP_TO_ADD_TO;


public class TransactionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, IMyFragment {
    public static final String PAYMENT_FROM_NAME = DatabaseHelper.PAYMENT_USER_JOIN_COLUMN_FROM_USER;
    private boolean mainMode = true;
    private static final String TAG = TransactionFragment.class.getSimpleName();
    private Spinner groupSelection;

    private TransactionAdapter transactionAdapter;
    private View transactionListFragment;
    private ContentResolver contentResolver;
    private SimpleCursorAdapter groupAdapter;
    private CursorAdapter paymentListAdapter;
    private ListView paymentListView;
    private static final int CURSOR_LOADER_TRANSACTIONS_NAME = 0;
    private static final String TRANSACTION_NAME_FILTER = "transactionName";
    private TextView totalPaymentsText;
    private TextView shouldPayName;
    private TextView shouldPayText;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TransactionFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_transaction, menu);

        final MenuItem item = menu.findItem(R.id.menu_group_selection);
        item.setActionView(R.layout.group_spinner);

        groupSelection = item.getActionView().findViewById(R.id.menu_group_spinner);

        groupSelection.setAdapter(groupAdapter);

        groupSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                initOrRestartLoaderWithGroupId();
                refreshPaymentAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                initOrRestartLoaderWithGroupId();
                refreshPaymentAdapter();
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        contentResolver = getActivity().getContentResolver();

        groupAdapter = initializeGroupAdapter();
        if (groupAdapter.getCursor().getCount() < 1) {
            mainMode = false;
        } else {
            groupAdapter.getCursor().moveToFirst();

            paymentListAdapter = initPaymentListAdapter();
            initOrRestartLoaderWithGroupId();
        }
    }

    private CursorAdapter initPaymentListAdapter() {
        final Cursor payments = getPaymentCoursorForCurrentGroup();
        return new PaymentAdapter(this.getContext(), payments);
    }

    private Cursor getPaymentCoursorForCurrentGroup() {
        return new DatabaseHelper(this.getContext())
                .getNewestPaymentsWithUserNamesForGroup(getCurrentGroupInternalId());
    }

    private void refreshPaymentAdapter() {
        if (mainMode) {
            final Cursor cursor = getPaymentCoursorForCurrentGroup();
            paymentListAdapter.swapCursor(cursor);
            updatePaymentTexts();
        }
    }

    private void updatePaymentTexts() {
        Cursor payments = getPaymentCoursorForCurrentGroup();
        double paymentSum = 0.0;
        double highestPayment = -1.0;
        String highestPaymentName = null;
        while (payments.moveToNext()) {
            final double paymentAmount = payments.getDouble(payments.getColumnIndex(PaymentTable.COLUMN_AMOUNT));
            paymentSum += paymentAmount;
            if (paymentAmount > highestPayment) {
                highestPayment = paymentAmount;
                highestPaymentName = payments.getString(payments.getColumnIndex(PAYMENT_FROM_NAME));
            }
        }
        if (highestPayment > 0) {
            if(highestPaymentName.contains(" ")){
                shouldPayName.setText(highestPaymentName.split(" ")[0]);
            }else {
                shouldPayName.setText(highestPaymentName);
            }
            shouldPayText.setVisibility(View.VISIBLE);
        } else {
            shouldPayName.setText("");
            shouldPayText.setVisibility(View.INVISIBLE);
        }
        totalPaymentsText.setText(CURRENCY_FORMAT.format(paymentSum));
    }

    private void initOrRestartLoaderWithGroupId() {
        if (getLoaderManager().getLoader(CURSOR_LOADER_TRANSACTIONS_NAME) == null || getLoaderManager().getLoader(CURSOR_LOADER_TRANSACTIONS_NAME).isStarted() == false) {
            getLoaderManager().initLoader(CURSOR_LOADER_TRANSACTIONS_NAME, null, TransactionFragment.this);
        } else {
            getLoaderManager().restartLoader(CURSOR_LOADER_TRANSACTIONS_NAME, null, TransactionFragment.this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initOrRestartLoaderWithGroupId();
        refreshPaymentAdapter();
    }


    private String getCurrentGroupId() {
        return groupAdapter.getCursor().getString(groupAdapter.getCursor().getColumnIndex(GroupTable.COLUMN_GROUP_ID));
    }

    private long getCurrentGroupInternalId() {
        return groupAdapter.getCursor().getLong(groupAdapter.getCursor().getColumnIndex(GroupTable.COLUMN_ID));
    }

    private String getCurrentGroupCreatedAt() {
        return groupAdapter.getCursor().getString(groupAdapter.getCursor().getColumnIndex(GroupTable.COLUMN_CREATED_AT));
    }

    private SimpleCursorAdapter initializeGroupAdapter() {

        final String[] projection = {GroupTable.TABLE_NAME + ".*"};
        final String sortOrder = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_CREATED_AT + " DESC";
        final String selection = UserTable.TABLE_NAME + "." + UserTable.COLUMN_USER_ID + " = ? AND "
                + GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_SYNCHRONIZED + " = 1 ";
        final String[] selectionArgs = {Login.getInstance().getUser().getUserId()};
        final Cursor query = contentResolver.query(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI, projection, selection, selectionArgs, sortOrder);

        final String[] columns = new String[]{GroupTable.COLUMN_NAME};
        final int[] to = new int[]{android.R.id.text1};

        final SimpleCursorAdapter groupAdapter = new SimpleCursorAdapter(this.getContext(), R.layout.group_spinner_item, query, columns, to, 0);
        groupAdapter.setDropDownViewResource(R.layout.group_spinner_dropdown_item);
        return groupAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        transactionListFragment = inflater.inflate(R.layout.fragment_transaction_list, container, false);

        totalPaymentsText = transactionListFragment.findViewById(R.id.total_payments);
        shouldPayName = transactionListFragment.findViewById(R.id.should_pay_name);
        shouldPayText = transactionListFragment.findViewById(R.id.should_pay_text);

        final SearchView searchView = transactionListFragment.findViewById(R.id.transaction_search);
        int searchTextViewId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        final TextView searchTextView = (TextView) searchView.findViewById(searchTextViewId);
        searchTextView.setTextColor(Color.WHITE);
        searchTextView.setHintTextColor(Color.WHITE);

        int searchImageViewId = searchView.getContext().getResources().getIdentifier("android:id/search_button", null, null);
        ImageView icon = searchView.findViewById(searchImageViewId);
        icon.setColorFilter(Color.WHITE);

        int searchCloseImageViewId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView icon2 = searchView.findViewById(searchCloseImageViewId);
        icon2.setColorFilter(Color.WHITE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                final Bundle bundle = new Bundle();
                bundle.putString(TRANSACTION_NAME_FILTER, s);
                getLoaderManager().restartLoader(CURSOR_LOADER_TRANSACTIONS_NAME, bundle, TransactionFragment.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                final Bundle bundle = new Bundle();
                bundle.putString(TRANSACTION_NAME_FILTER, s);
                getLoaderManager().restartLoader(CURSOR_LOADER_TRANSACTIONS_NAME, bundle, TransactionFragment.this);
                return true;
            }
        });

        initOrRestartLoaderWithGroupId();
        paymentListView = transactionListFragment.findViewById(R.id.payment_list);
        if (paymentListView == null) {
            Log.e(TAG, "PaymentList is null");
        } else {
            paymentListView.setAdapter(paymentListAdapter);
        }
        return transactionListFragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final TransactionAdapter.ViewHolder viewHolder = ((TransactionAdapter.ViewHolder) v.getTag());
        final Transaction transaction = viewHolder.getTransaction();
        final String paidByUsername = viewHolder.getPaidByUsername();
        startTransactionDetailActivity(transaction, paidByUsername);
    }

    private void startTransactionDetailActivity(Transaction transaction, String paidByUsername) {
        final Intent intent = new Intent(this.getActivity(), TransactionDetailViewActivity.class);
        final Bundle b = new Bundle();
        b.putLong("id", transaction.getId());
        b.putString("imageUrl", transaction.getImageUrl());
        b.putString("name", transaction.getInfoName());
        b.putString("createdAt", Helper.formatDate(transaction.getDateTime()));
        b.putDouble("amount", transaction.getAmount());
        b.putBoolean("sync", transaction.isSynched());
        b.putString("paidByUsername", paidByUsername);

        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String search = "%%";
        switch (id) {
            case CURSOR_LOADER_TRANSACTIONS_NAME:
                if (args != null) {
                    search = "%" + args.getString(TRANSACTION_NAME_FILTER) + "%";
                }
                break;
            default:
                break;
        }
        final String[] projection = {TransactionTable.TABLE_NAME + ".*", UserTable.TABLE_NAME + "." + UserTable.COLUMN_USERNAME};
        final String sortOrder = TransactionTable.TABLE_NAME + "." + TransactionTable.COLUMN_INFO_CREATED_AT + " DESC";
        final String selection = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_GROUP_ID + " = ? AND ("
                + TransactionTable.TABLE_NAME + "." + TransactionTable.COLUMN_INFO_NAME + " like ? OR "
                + TransactionTable.TABLE_NAME + "." + TransactionTable.COLUMN_INFO_CREATED_AT + " like ? )";
        String[] selectionArgs;
        if (mainMode) {
            selectionArgs = new String[]{getCurrentGroupId(), search, search};
        } else {
            selectionArgs = new String[]{"", search, search};
        }
        return new CursorLoader(getActivity(), DatabaseProvider.CONTENT_GROUP_USER_TRANSACTION_JOIN_URI, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "Data: " + data.getCount());
        if (transactionAdapter == null) {
            transactionAdapter = new TransactionAdapter(getContext(), R.layout.fragment_transaction, data);
        } else {
            transactionAdapter.swapCursor(data);
        }

        setListAdapter(transactionAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        transactionAdapter.swapCursor(null);
    }


    @Override
    public View.OnClickListener getOnFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Hello from " + TAG);
                if (isMainMode()) {
                    final Intent intent = new Intent(view.getContext(), AddTransactionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(GROUP_TO_ADD_TO, getCurrentGroupId());
                    intent.putExtra(GROUP_CREATED_AT_ADD_TO, getCurrentGroupCreatedAt());
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Create a group before adding transactions", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    public boolean isMainMode() {
        return mainMode;
    }
}
