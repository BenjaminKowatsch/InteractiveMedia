package com.media.interactive.cs3.hdm.interactivemedia.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
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

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;


public class TransactionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, IMyFragment {

    public static final String LIST_FRAGMENT_NAME = "transaction";
    private static final String TAG = "TransactionFragment";


    private AdapterView.OnItemSelectedListener onItemSelectedListener;

    private SimpleCursorAdapter simpleCursorAdapter;
    //private ContentResolver dummyContentResolver;

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

        //dummyContentResolver = getActivity().getContentResolver();

        // Initializing the SimpleCursorAdapter and the CursorLoader
        String[] projection = new String[] {TransactionTable.COLUMN_INFO_NAME, TransactionTable.COLUMN_INFO_CREATED_AT,
            TransactionTable.COLUMN_AMOUNT, TransactionTable.COLUMN_PAID_BY, TransactionTable.COLUMN_INFO_LOCATION};
        getLoaderManager().initLoader(0, null, this);
        simpleCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_transaction, null, projection,
            new int[] {R.id.transaction_title, R.id.transaction_creation_date,
                R.id.transaction_amount, R.id.transaction_payed_by, R.id.transaction_location}, 0);
        setListAdapter(simpleCursorAdapter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_list, container, false);

        return view;
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
            TransactionTable.COLUMN_PAID_BY,
            TransactionTable.COLUMN_INFO_LOCATION};
        final CursorLoader cursorLoader = new CursorLoader(getActivity(),
            DatabaseProvider.CONTENT_TRANSACTION_URI, projection,
            null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        simpleCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        simpleCursorAdapter.swapCursor(null);
    }


    @Override
    public View.OnClickListener getOnFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Hello from " + TAG);
                final Intent intent = new Intent(view.getContext(), AddTransactionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
    }
}
