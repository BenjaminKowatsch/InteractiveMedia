package com.media.interactive.cs3.hdm.interactivemedia.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
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
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.TransactionAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.settlement.PaymentAdapter;

import static android.database.DatabaseUtils.dumpCursorToString;
import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.GROUP_CREATED_AT_ADD_TO;
import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.GROUP_TO_ADD_TO;


public class TransactionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, IMyFragment {

    private static final String TAG = TransactionFragment.class.getSimpleName();
    private Spinner groupSelection;

    private AdapterView.OnItemSelectedListener onItemSelectedListener;

    private TransactionAdapter transactionAdapter;
    private View transactionListFragment;
    private ContentResolver contentResolver;
    private SimpleCursorAdapter groupAdapter;
    private CursorAdapter paymentListAdapter;
    private ListView paymentListView;
    private static final int CURSOR_LOADER_TRANSACTIONS_NAME = 0;
    private static final String TRANSACTION_NAME_FILTER = "transactionName";

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
        groupAdapter.getCursor().moveToFirst();

        paymentListAdapter = initPaymentListAdapter();
        initOrRestartLoaderWithGroupId();
    }

    private CursorAdapter initPaymentListAdapter() {
        final Cursor payments = getPaymentCoursorForCurrentGroup();
        Log.d(TAG, dumpCursorToString(payments));
        return new PaymentAdapter(this.getContext(), payments);
    }

    private Cursor getPaymentCoursorForCurrentGroup() {
        return new DatabaseHelper(this.getContext())
                .getNewestPaymentsWithUserNamesForGroup(getCurrentGroupInternalId());
    }

    private void refreshPaymentAdapter() {
        final Cursor cursor = getPaymentCoursorForCurrentGroup();
        Log.d(TAG, dumpCursorToString(cursor));
        paymentListAdapter.swapCursor(cursor);
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
        final String selection = UserTable.TABLE_NAME + "." + UserTable.COLUMN_USER_ID + " = ?";
        final String[] selectionArgs = {Login.getInstance().getUser().getUserId()};
        final Cursor query = contentResolver.query(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI, projection, selection, selectionArgs, sortOrder);

        final String[] columns = new String[]{GroupTable.COLUMN_NAME};
        final int[] to = new int[]{android.R.id.text1};

        final SimpleCursorAdapter groupAdapter = new SimpleCursorAdapter(this.getContext(), android.R.layout.simple_spinner_item, query, columns, to, 0);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return groupAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        transactionListFragment = inflater.inflate(R.layout.fragment_transaction_list, container, false);

        final SearchView searchView = transactionListFragment.findViewById(R.id.transaction_search);
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
        if(paymentListView == null) {
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
        try {
            onItemSelectedListener = (AdapterView.OnItemSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    context.toString()
                            + " muss OnItemSelectedListener implementieren");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onItemSelectedListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        onItemSelectedListener.onItemSelected(l, v, position, id);
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
        final String selection = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_GROUP_ID + " = ? AND " + GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_NAME + " like ? ";
        final String[] selectionArgs = {getCurrentGroupId(), search};
        return new CursorLoader(getActivity(), DatabaseProvider.CONTENT_GROUP_USER_TRANSACTION_JOIN_URI, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
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
                final Intent intent = new Intent(view.getContext(), AddTransactionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(GROUP_TO_ADD_TO, getCurrentGroupId());
                intent.putExtra(GROUP_CREATED_AT_ADD_TO, getCurrentGroupCreatedAt());
                startActivity(intent);
            }
        };
    }
}
