package com.media.interactive.cs3.hdm.interactivemedia.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;


public class TransactionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LIST_FRAGMENT_NAME = "transaction";

    ContentValues dummyContentValues = new ContentValues();
    private AdapterView.OnItemSelectedListener onItemSelectedListener;

    private SimpleCursorAdapter simpleCursorAdapter;
    private ContentResolver dummyContentResolver;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TransactionFragment() {
        setHasOptionsMenu(true);
    }

    private void addDummyData(int counter) {

        dummyContentValues.put(TransactionTable.COLUMN_AMOUNT, ((counter*1494)%1111) );
        dummyContentValues.put(TransactionTable.COLUMN_PAID_BY, 1);
        dummyContentValues.put(TransactionTable.COLUMN_INFO_NAME, "Transaction " + counter);
        dummyContentValues.put(TransactionTable.COLUMN_INFO_LOCATION, "Ghetto Netto");
        dummyContentResolver.insert(DatabaseProvider.CONTENT_TRANSACTION_URI, dummyContentValues);
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

        for (int i = 0; i < 30; i++) {
            addDummyData(i);
        }

        // Initializing the SimpleCursorAdapter and the CursorLoader
        String[] projection = new String[]{TransactionTable.COLUMN_INFO_NAME, TransactionTable.COLUMN_INFO_CREATED_AT,
                TransactionTable.COLUMN_AMOUNT, TransactionTable.COLUMN_PAID_BY, TransactionTable.COLUMN_INFO_LOCATION};
        getLoaderManager().initLoader(0, null, this);
        simpleCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_transaction, null, projection,
                new int[]{R.id.transaction_title, R.id.transaction_creation_date,
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
        }catch (ClassCastException e){
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
        onItemSelectedListener.onItemSelected(l,v,position,id);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{TransactionTable.COLUMN_ID, TransactionTable.COLUMN_INFO_NAME, TransactionTable.COLUMN_INFO_CREATED_AT,
                TransactionTable.COLUMN_AMOUNT, TransactionTable.COLUMN_PAID_BY, TransactionTable.COLUMN_INFO_LOCATION};
        final CursorLoader cursorLoader = new CursorLoader(getActivity(), DatabaseProvider.CONTENT_TRANSACTION_URI, projection, null, null, null);
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



}