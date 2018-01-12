package com.media.interactive.cs3.hdm.interactivemedia;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;

/**
 * Created by benny on 05.01.18.
 */

public class GroupCursorLoader extends CursorLoader {

    private DatabaseHelper databaseHelper;
    private String userId;
    private String searchString;

    public GroupCursorLoader(Context context, DatabaseHelper databaseHelper, String userId, String searchString) {
        super(context);
        this.databaseHelper = databaseHelper;
        this.userId = userId;
        this.searchString = searchString;
    }

    public GroupCursorLoader(Context context, DatabaseHelper databaseHelper, String userId) {
        this(context, databaseHelper, userId, "");
    }

    @Override
    public Cursor loadInBackground() {
        return databaseHelper.getAllGroupsByUserId(userId, searchString);
    }

}
