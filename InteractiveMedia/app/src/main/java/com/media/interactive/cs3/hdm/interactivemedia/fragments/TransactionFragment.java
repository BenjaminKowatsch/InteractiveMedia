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
import com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity;
import com.media.interactive.cs3.hdm.interactivemedia.activties.TransactionDetailViewActivity;
import com.media.interactive.cs3.hdm.interactivemedia.adapter.TransactionAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.settlement.PaymentAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.database.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.database.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.PaymentTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.CURRENCY_FORMAT;
import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.GROUP_CREATED_AT_ADD_TO;
import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.GROUP_TO_ADD_TO;




/**
 * The Class TransactionFragment.
 */
public class TransactionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, IFragment {

    /**
     * The Constant PAYMENT_FROM_NAME.
     */
    public static final String PAYMENT_FROM_NAME = DatabaseHelper.PAYMENT_USER_JOIN_COLUMN_FROM_USER;
    /**
     * The Constant TAG.
     */
    private static final String TAG = TransactionFragment.class.getSimpleName();
    /**
     * The Constant CURSOR_LOADER_TRANSACTIONS_NAME.
     */
    private static final int CURSOR_LOADER_TRANSACTIONS_NAME = 0;
    /**
     * The Constant TRANSACTION_NAME_FILTER.
     */
    private static final String TRANSACTION_NAME_FILTER = "transactionName";
    /**
     * The main mode.
     */
    private boolean mainMode = true;
    /**
     * The group selection.
     */
    private Spinner groupSelection;
    /**
     * The transaction adapter.
     */
    private TransactionAdapter transactionAdapter;
    /**
     * The transaction list fragment.
     */
    private View transactionListFragment;
    /**
     * The content resolver.
     */
    private ContentResolver contentResolver;
    /**
     * The group adapter.
     */
    private SimpleCursorAdapter groupAdapter;
    /**
     * The payment list adapter.
     */
    private CursorAdapter paymentListAdapter;
    /**
     * The payment list view.
     */
    private ListView paymentListView;
    /**
     * The total payments text.
     */
    private TextView totalPaymentsText;

    /**
     * The should pay name.
     */
    private TextView shouldPayName;

    /**
     * The should pay text.
     */
    private TextView shouldPayText;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TransactionFragment() {
        setHasOptionsMenu(true);
    }


    /* (non-Javadoc)
     * @see android.app.Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
     */
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

    /* (non-Javadoc)
     * @see android.app.Fragment#onCreate(android.os.Bundle)
     */
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

    /**
     * Inits the payment list adapter.
     *
     * @return the cursor adapter
     */
    private CursorAdapter initPaymentListAdapter() {
        final Cursor payments = getPaymentCoursorForCurrentGroup();
        return new PaymentAdapter(this.getContext(), payments);
    }

    /**
     * Gets the payment coursor for current group.
     *
     * @return the payment coursor for current group
     */
    private Cursor getPaymentCoursorForCurrentGroup() {
        return new DatabaseHelper(this.getContext())
            .getNewestPaymentsWithUserNamesForGroup(getCurrentGroupInternalId());
    }

    /**
     * Refresh payment adapter.
     */
    private void refreshPaymentAdapter() {
        if (mainMode) {
            final Cursor cursor = getPaymentCoursorForCurrentGroup();
            paymentListAdapter.swapCursor(cursor);
            updatePaymentTexts();
        }
    }

    /**
     * Update payment texts.
     */
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
            if (highestPaymentName.contains(" ")) {
                shouldPayName.setText(highestPaymentName.split(" ")[0]);
            } else {
                shouldPayName.setText(highestPaymentName);
            }
            shouldPayText.setVisibility(View.VISIBLE);
        } else {
            shouldPayName.setText("");
            shouldPayText.setVisibility(View.INVISIBLE);
        }
        totalPaymentsText.setText(CURRENCY_FORMAT.format(paymentSum));
    }

    /**
     * Inits the or restart loader with group id.
     */
    private void initOrRestartLoaderWithGroupId() {
        if (getLoaderManager().getLoader(CURSOR_LOADER_TRANSACTIONS_NAME) == null
            || getLoaderManager().getLoader(CURSOR_LOADER_TRANSACTIONS_NAME).isStarted() == false) {
            getLoaderManager().initLoader(CURSOR_LOADER_TRANSACTIONS_NAME, null, TransactionFragment.this);
        } else {
            getLoaderManager().restartLoader(CURSOR_LOADER_TRANSACTIONS_NAME, null, TransactionFragment.this);
        }
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        initOrRestartLoaderWithGroupId();
        refreshPaymentAdapter();
    }


    /**
     * Gets the current group id.
     *
     * @return the current group id
     */
    private String getCurrentGroupId() {
        return groupAdapter.getCursor().getString(groupAdapter.getCursor().getColumnIndex(GroupTable.COLUMN_GROUP_ID));
    }

    /**
     * Gets the current group internal id.
     *
     * @return the current group internal id
     */
    private long getCurrentGroupInternalId() {
        return groupAdapter.getCursor().getLong(groupAdapter.getCursor().getColumnIndex(GroupTable.COLUMN_ID));
    }

    /**
     * Gets the current group created at.
     *
     * @return the current group created at
     */
    private String getCurrentGroupCreatedAt() {
        return groupAdapter.getCursor().getString(groupAdapter.getCursor().getColumnIndex(GroupTable.COLUMN_CREATED_AT));
    }

    /**
     * Initialize group adapter.
     *
     * @return the simple cursor adapter
     */
    private SimpleCursorAdapter initializeGroupAdapter() {

        final String[] projection = {GroupTable.TABLE_NAME + ".*"};
        final String sortOrder = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_CREATED_AT + " DESC";
        final String selection = UserTable.TABLE_NAME + "." + UserTable.COLUMN_USER_ID + " = ? AND "
            + GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_SYNCHRONIZED + " = 1 ";
        final String[] selectionArgs = {Login.getInstance().getUser().getUserId()};
        final Cursor query = contentResolver.query(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI,
            projection, selection, selectionArgs, sortOrder);

        final String[] columns = new String[] {GroupTable.COLUMN_NAME};
        final int[] to = new int[] {android.R.id.text1};

        final SimpleCursorAdapter groupAdapter = new SimpleCursorAdapter(this.getContext(),
            R.layout.group_spinner_item, query, columns, to, 0);
        groupAdapter.setDropDownViewResource(R.layout.group_spinner_dropdown_item);
        return groupAdapter;
    }

    /* (non-Javadoc)
     * @see android.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        transactionListFragment = inflater.inflate(R.layout.fragment_transaction, container, false);

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


    /* (non-Javadoc)
     * @see android.app.Fragment#onAttach(android.content.Context)
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);

    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onDetach()
     */
    @Override
    public void onDetach() {
        super.onDetach();
    }

    /* (non-Javadoc)
     * @see android.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final TransactionAdapter.ViewHolder viewHolder = ((TransactionAdapter.ViewHolder) v.getTag());
        final Transaction transaction = viewHolder.getTransaction();
        final String paidByUsername = viewHolder.getPaidByUsername();
        startTransactionDetailActivity(transaction, paidByUsername);
    }

    /**
     * Start transaction detail activity.
     *
     * @param transaction    the transaction
     * @param paidByUsername the paid by username
     */
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

    /* (non-Javadoc)
     * @see android.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
     */
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
            selectionArgs = new String[] {getCurrentGroupId(), search, search};
        } else {
            selectionArgs = new String[] {"", search, search};
        }
        return new CursorLoader(getActivity(),
            DatabaseProvider.CONTENT_GROUP_USER_TRANSACTION_JOIN_URI, projection,
            selection, selectionArgs, sortOrder);
    }

    /* (non-Javadoc)
     * @see android.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "Data: " + data.getCount());
        if (transactionAdapter == null) {
            transactionAdapter = new TransactionAdapter(getContext(), R.layout.fragment_transaction_list_item, data);
        } else {
            transactionAdapter.swapCursor(data);
        }

        setListAdapter(transactionAdapter);
    }

    /* (non-Javadoc)
     * @see android.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        transactionAdapter.swapCursor(null);
    }


    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.fragments.IFragment#getOnFabClickListener()
     */
    @Override
    public View.OnClickListener getOnFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    /**
     * Checks if is main mode.
     *
     * @return true, if is main mode
     */
    public boolean isMainMode() {
        return mainMode;
    }
}
