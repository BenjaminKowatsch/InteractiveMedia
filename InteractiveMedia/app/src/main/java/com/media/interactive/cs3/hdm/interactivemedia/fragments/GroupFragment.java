package com.media.interactive.cs3.hdm.interactivemedia.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentProvider;
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
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.media.interactive.cs3.hdm.interactivemedia.GroupAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.activties.AddGroupActivity;
import com.media.interactive.cs3.hdm.interactivemedia.activties.GroupDetailViewActivity;
import com.media.interactive.cs3.hdm.interactivemedia.activties.MainActivity;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;


public class GroupFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, IMyFragment {

    public static final String LIST_FRAGMENT_NAME = "group";
    private static final String TAG = GroupFragment.class.getSimpleName();

    private AdapterView.OnItemSelectedListener onItemSelectedListener;

    private GroupAdapter groupAdapter;
    private static final int CURSOR_LOADER_GROUPS = 0;
    private String GROUP_FILTER = "search";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_group, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);

        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                final Bundle bundle = new Bundle();
                bundle.putString(GROUP_FILTER,s);
                getLoaderManager().restartLoader(CURSOR_LOADER_GROUPS, bundle, GroupFragment.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                final Bundle bundle = new Bundle();
                bundle.putString(GROUP_FILTER,s);
                getLoaderManager().restartLoader(CURSOR_LOADER_GROUPS, bundle, GroupFragment.this);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        getLoaderManager().initLoader(0, null, this);
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

    private void startGroupDetailActivity(Group group){
        final Intent intent = new Intent(this.getActivity(), GroupDetailViewActivity.class);
        final Bundle b = new Bundle();
        b.putLong("id", group.getId());
        b.putString("imageUrl", group.getImageUrl());
        b.putString("name", group.getName());
        b.putString("createdAt", group.getCreatedAt());
        b.putString("groupId", group.getGroupId());
        b.putBoolean("sync", group.getSync());

        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //super.onListItemClick(l, v, position, id);
        //onItemSelectedListener.onItemSelected(l, v, position, id);
        final Group group = ((GroupAdapter.ViewHolder) v.getTag()).getGroup();
        startGroupDetailActivity(group);
        Toast.makeText(v.getContext(), group.getName() + group.getCreatedAt(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String[] selectionArgs = null;
        switch (id) {
            case CURSOR_LOADER_GROUPS:
                if (args != null) {
                    selection =  GroupTable.COLUMN_NAME + " like ? OR " + GroupTable.COLUMN_CREATED_AT + " like ?";
                    final String search = "%"+args.getString(GROUP_FILTER)+"%";
                    selectionArgs = new String[] { search, search };
                }
                break;
             default:
                break;
        }
        final String[] projection = {GroupTable.COLUMN_ID, GroupTable.COLUMN_NAME, GroupTable.COLUMN_CREATED_AT, GroupTable.COLUMN_IMAGE_URL, GroupTable.COLUMN_SYNCHRONIZED};
        final CursorLoader cursorLoader = new CursorLoader(getActivity(),
            DatabaseProvider.CONTENT_GROUP_URI,
            projection, selection,
            selectionArgs, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        if(groupAdapter == null) {
            groupAdapter = new GroupAdapter(getContext(), R.layout.fragment_group, data);
        } else {
            groupAdapter.swapCursor(data);
        }
        setListAdapter(groupAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        groupAdapter.swapCursor(null);
    }

    @Override
    public View.OnClickListener getOnFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Hello from Groupfragment ");
                final Intent intent = new Intent(view.getContext(), AddGroupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
    }
}
