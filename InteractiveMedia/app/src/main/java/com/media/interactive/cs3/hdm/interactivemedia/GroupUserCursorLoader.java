package com.media.interactive.cs3.hdm.interactivemedia;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;

/**
 * Created by benny on 05.01.18.
 */

public class GroupUserCursorLoader extends CursorLoader {

    private DatabaseHelper databaseHelper;
    private long groupId;

    public GroupUserCursorLoader(Context context, DatabaseHelper databaseHelper, long groupId) {
        super(context);
        this.databaseHelper = databaseHelper;
        this.groupId = groupId;
    }

    @Override
    public Cursor loadInBackground() {
        return databaseHelper.getAllGroupAndUsersByGroup(groupId);
    }
}
