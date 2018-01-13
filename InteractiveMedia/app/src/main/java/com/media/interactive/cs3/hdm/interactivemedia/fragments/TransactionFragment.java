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

import com.media.interactive.cs3.hdm.interactivemedia.GroupAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.TransactionAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;

import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.GROUP_TO_ADD_TO;


public class TransactionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, IMyFragment {

    private static final String TAG = TransactionFragment.class.getSimpleName();
    private Spinner groupSelection;

    private AdapterView.OnItemSelectedListener onItemSelectedListener;

    private TransactionAdapter transactionAdapter;
    private View transactionListFragment;
    private ContentResolver contentResolver;
    private SimpleCursorAdapter groupAdapter;
    private static final String GROUP_ID_FILTER = "groupId";
    private static final int CURSOR_LOADER_GROUP_ID = 0;

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

        contentResolver = getActivity().getContentResolver();

        groupAdapter = initializeGroupAdapter();
        groupAdapter.getCursor().moveToFirst();

        initOrRestartLoaderWithGroupId();
    }

    private void initOrRestartLoaderWithGroupId() {
        final Bundle bundle = new Bundle();
        bundle.putString(GROUP_ID_FILTER, getCurrentGroupId());
        if (getLoaderManager().getLoader(CURSOR_LOADER_GROUP_ID) == null || getLoaderManager().getLoader(CURSOR_LOADER_GROUP_ID).isStarted() == false) {
            getLoaderManager().initLoader(CURSOR_LOADER_GROUP_ID, bundle, TransactionFragment.this);
        } else {
            getLoaderManager().restartLoader(CURSOR_LOADER_GROUP_ID, bundle, TransactionFragment.this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initOrRestartLoaderWithGroupId();
    }

    private String getCurrentGroupId() {
        return groupAdapter.getCursor().getString(groupAdapter.getCursor().getColumnIndex(GroupTable.COLUMN_GROUP_ID));
    }

    private SimpleCursorAdapter initializeGroupAdapter() {

        final String[] projection = { GroupTable.TABLE_NAME + ".*"};
        final String sortOrder = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_CREATED_AT + " DESC";
        final String selection = UserTable.TABLE_NAME + "." + UserTable.COLUMN_USER_ID + " = ?";
        final String[] selectionArgs = {Login.getInstance().getUser().getUserId()};
        final Cursor query = contentResolver.query(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI, projection, selection, selectionArgs, sortOrder);

        final String[] columns = new String[] { GroupTable.COLUMN_NAME };
        final int[] to = new int[] { android.R.id.text1 };

        final SimpleCursorAdapter groupAdapter = new SimpleCursorAdapter(this.getContext(), android.R.layout.simple_spinner_item, query, columns, to, 0);
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
                initOrRestartLoaderWithGroupId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                initOrRestartLoaderWithGroupId();
            }
        });
        initOrRestartLoaderWithGroupId();
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
        final String[] projection = { TransactionTable.TABLE_NAME + ".*"};
        final String sortOrder = TransactionTable.TABLE_NAME + "." + TransactionTable.COLUMN_INFO_CREATED_AT + " DESC";
        final String selection = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_GROUP_ID + " = ?";
        final String[] selectionArgs = {getCurrentGroupId()};
        return new CursorLoader(getActivity(),DatabaseProvider.CONTENT_GROUP_TRANSACTION_JOIN_URI, projection, selection, selectionArgs, sortOrder);
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
                startActivity(intent);
            }
        };
    }
}
