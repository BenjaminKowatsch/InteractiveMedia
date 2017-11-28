package com.media.interactive.cs3.hdm.interactivemedia.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import android.support.v7.app.AppCompatActivity;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.activties.AddGroupActivity;
import com.media.interactive.cs3.hdm.interactivemedia.activties.HomeActivity;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;


public class GroupFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LIST_FRAGMENT_NAME = "group";
    private static final String TAG = "GroupFragment";


    ContentValues dummyContentValues = new ContentValues();
    private AdapterView.OnItemSelectedListener onItemSelectedListener;

    private SimpleCursorAdapter simpleCursorAdapter;
    private ContentResolver dummyContentResolver;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupFragment() {
        setHasOptionsMenu(true);
    }

    private void addDummyData(int counter) {
        dummyContentValues.put(GroupTable.COLUMN_NAME, "Gruppe " + counter);
        dummyContentResolver.insert(DatabaseProvider.CONTENT_GROUP_URI, dummyContentValues);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_group, menu);
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
        String[] projection = new String[]{GroupTable.COLUMN_NAME, GroupTable.COLUMN_CREATED_AT};
        getLoaderManager().initLoader(0, null, this);
        simpleCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_group, null, projection,
                new int[]{R.id.group_title, R.id.group_creation_date}, 0);
        setListAdapter(simpleCursorAdapter);







    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_list, container, false);

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
        final String[] projection = {GroupTable.COLUMN_ID, GroupTable.COLUMN_NAME, GroupTable.COLUMN_CREATED_AT};
        final CursorLoader cursorLoader = new CursorLoader(getActivity(), DatabaseProvider.CONTENT_GROUP_URI, projection, null, null, null);
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
