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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.media.interactive.cs3.hdm.interactivemedia.GroupCursorLoader;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;

import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.GROUP_TO_ADD_TO;


public class TransactionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, IMyFragment {

    private static final String TAG = "TransactionFragment";
    private Spinner groupSelection;

    private AdapterView.OnItemSelectedListener onItemSelectedListener;

    private SimpleCursorAdapter simpleCursorAdapter;
    private View transactionListFragment;
    private ContentResolver dummyContentResolver;
    private SimpleCursorAdapter groupAdapter;
    private DatabaseHelper databaseHelper;

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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        dummyContentResolver = getActivity().getContentResolver();
        databaseHelper = new DatabaseHelper(this.getContext());

        groupAdapter = initializeGroupAdapter();
        groupAdapter.getCursor().moveToFirst();

        // Initializing the SimpleCursorAdapter and the CursorLoader
        initializeTransactionsForCurrentGroup();
    }

    private void initializeTransactionsForCurrentGroup() {
        Cursor cursor = databaseHelper.getTransactionsForGroup(getCurrentGroupId());
        String[] projection = new String[] {TransactionTable.COLUMN_INFO_NAME, TransactionTable.COLUMN_INFO_CREATED_AT,
            TransactionTable.COLUMN_AMOUNT, TransactionTable.COLUMN_PAID_BY};
        getLoaderManager().initLoader(0, null, this);
        simpleCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_transaction, cursor, projection,
            new int[] {R.id.transaction_title, R.id.transaction_creation_date,
                R.id.transaction_amount, R.id.transaction_payed_by}, 0);
        setListAdapter(simpleCursorAdapter);
    }

    private void updateTransactionsForCurrentGroup() {
        Cursor cursor = databaseHelper.getTransactionsForGroup(getCurrentGroupId());
        simpleCursorAdapter.swapCursor(cursor);
    }

    private long getCurrentGroupId() {
        return groupAdapter.getCursor().getLong(0);
    }

    private SimpleCursorAdapter initializeGroupAdapter() {
        GroupCursorLoader loader = new GroupCursorLoader(this.getContext(), databaseHelper,  Login.getInstance().getUser().getUserId());
        Cursor query = loader.loadInBackground();

        String[] columns = new String[] { GroupTable.COLUMN_NAME };
        int[] to = new int[] { android.R.id.text1 };

        SimpleCursorAdapter groupAdapter = new SimpleCursorAdapter(this.getContext(), android.R.layout.simple_spinner_item, query, columns, to, 0);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return groupAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        transactionListFragment = inflater.inflate(R.layout.fragment_transaction_list, container, false);
        groupSelection = transactionListFragment.findViewById(R.id.spinner_group_selection);
        groupSelection.setAdapter(groupAdapter);
        groupSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateTransactionsForCurrentGroup();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                updateTransactionsForCurrentGroup();
            }
        });
        updateTransactionsForCurrentGroup();
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
        String[] projection = new String[] {
            TransactionTable.COLUMN_ID,
            TransactionTable.COLUMN_INFO_NAME,
            TransactionTable.COLUMN_INFO_CREATED_AT,
            TransactionTable.COLUMN_AMOUNT,
            TransactionTable.COLUMN_PAID_BY};
        final CursorLoader cursorLoader = new CursorLoader(getActivity(),
            DatabaseProvider.CONTENT_TRANSACTION_URI, projection,
            null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        simpleCursorAdapter.swapCursor(data);
        updateTransactionsForCurrentGroup();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        updateTransactionsForCurrentGroup();
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
                startActivity(intent);
            }
        };
    }
}
