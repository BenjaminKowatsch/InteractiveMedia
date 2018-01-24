package com.media.interactive.cs3.hdm.interactivemedia.fragments;

import android.app.Activity;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.activties.AddGroupActivity;
import com.media.interactive.cs3.hdm.interactivemedia.activties.GroupDetailViewActivity;
import com.media.interactive.cs3.hdm.interactivemedia.adapter.GroupAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.data.DatabaseProviderHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.database.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.util.CallbackListener;

import org.json.JSONObject;




/**
 * The Class GroupFragment.
 */
public class GroupFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, IFragment {

    /**
     * The Constant TAG.
     */
    private static final String TAG = GroupFragment.class.getSimpleName();

    /**
     * The Constant CURSOR_LOADER_GROUPS.
     */
    private static final int CURSOR_LOADER_GROUPS = 0;

    /**
     * The Constant CURSOR_LOADER_USER_ID.
     */
    private static final int CURSOR_LOADER_USER_ID = 1;

    /**
     * The group adapter.
     */
    private GroupAdapter groupAdapter;

    /**
     * The helper.
     */
    private DatabaseProviderHelper helper;

    /**
     * The group filter.
     */
    private static final String GROUP_FILTER = "search";

    /**
     * The user id filter.
     */
    private static final String USER_ID_FILTER = "userId";

    /**
     * The user data completed.
     */
    private CallbackListener<JSONObject, Exception> userDataCompleted;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupFragment() {
        setHasOptionsMenu(true);
    }


    /* (non-Javadoc)
     * @see android.app.Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_group, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);

        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                final Bundle bundle = new Bundle();
                bundle.putString(GROUP_FILTER, s);
                getLoaderManager().restartLoader(CURSOR_LOADER_GROUPS, bundle, GroupFragment.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                final Bundle bundle = new Bundle();
                bundle.putString(GROUP_FILTER, s);
                getLoaderManager().restartLoader(CURSOR_LOADER_GROUPS, bundle, GroupFragment.this);
                return true;
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
        Log.d(TAG, "onCreate Fragment");
        setHasOptionsMenu(true);

        helper = new DatabaseProviderHelper(getActivity().getContentResolver());

        initOrRestartLoaderWithUserId();
    }

    /**
     * Inits the or restart loader with user id.
     */
    private void initOrRestartLoaderWithUserId() {
        final String userId = Login.getInstance().getUser().getUserId();
        Log.d(TAG, "Loading UserId: " + userId);
        final Bundle bundle = new Bundle();
        bundle.putString(USER_ID_FILTER, userId);
        if (getLoaderManager().getLoader(CURSOR_LOADER_USER_ID) == null
            || getLoaderManager().getLoader(CURSOR_LOADER_USER_ID).isStarted() == false) {
            getLoaderManager().initLoader(CURSOR_LOADER_USER_ID, bundle, GroupFragment.this);
        } else {
            getLoaderManager().restartLoader(CURSOR_LOADER_USER_ID, bundle, GroupFragment.this);
        }
    }

    /* (non-Javadoc)
     * @see android.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_group_list, container, false);

        return view;
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_USER_ID, null, GroupFragment.this);
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onAttach(android.content.Context)
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        userDataCompleted = new CallbackListener<JSONObject, Exception>() {
            @Override
            public void onSuccess(JSONObject response) {
                Activity activity = getActivity();
                if (isAdded() && activity != null) {
                    initOrRestartLoaderWithUserId();
                }
            }

            @Override
            public void onFailure(Exception error) {

            }
        };
        Login.getInstance().addOnUserDataSetListener(userDataCompleted);
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onDetach()
     */
    @Override
    public void onDetach() {
        super.onDetach();
        Login.getInstance().removeOnUserDataSetListener(userDataCompleted);
    }

    /**
     * Start group detail activity.
     *
     * @param group the group
     */
    private void startGroupDetailActivity(Group group) {
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

    /* (non-Javadoc)
     * @see android.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Group group = ((GroupAdapter.ViewHolder) v.getTag()).getGroup();
        startGroupDetailActivity(group);
    }

    /* (non-Javadoc)
     * @see android.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String userId = Login.getInstance().getUser().getUserId();
        String search = "%%";
        switch (id) {
            case CURSOR_LOADER_GROUPS:
                if (args != null) {
                    search = "%" + args.getString(GROUP_FILTER) + "%";
                }
                break;
            case CURSOR_LOADER_USER_ID:
                if (args != null) {
                    userId = args.getString(USER_ID_FILTER);
                }
                break;
            default:
                break;
        }
        if (userId == null) {
            userId = "";
        }
        Log.d(TAG, "onCreateLoader: " + userId);
        final String[] projection = {GroupTable.TABLE_NAME + ".*"};
        final String selection = "(" + UserTable.TABLE_NAME + "." + UserTable.COLUMN_USER_ID + " = ? ) AND"
            + " ( " + GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_NAME + " like ? OR "
            + GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_CREATED_AT + " like ? ) ";
        final String[] selectionArgs = {userId, search, search};
        final String sortOrder = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_CREATED_AT + " DESC";
        return new CursorLoader(getContext(), DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI,
            projection, selection, selectionArgs, sortOrder);
    }

    /* (non-Javadoc)
     * @see android.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "Data: " + data.getCount());
        if (groupAdapter == null) {
            groupAdapter = new GroupAdapter(getContext(), R.layout.fragment_group, data);
        } else {
            groupAdapter.swapCursor(data);
        }
        setListAdapter(groupAdapter);
    }

    /* (non-Javadoc)
     * @see android.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        groupAdapter.swapCursor(null);
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.fragments.IFragment#getOnFabClickListener()
     */
    @Override
    public View.OnClickListener getOnFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(view.getContext(), AddGroupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
    }
}
