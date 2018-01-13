package com.media.interactive.cs3.hdm.interactivemedia.contentprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupUserTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;

import java.util.ArrayList;

/**
 * Created by Pirmin Rehm on 02.12.2017.
 */

public class DummyDataAdder {

    private static final String TAG = "DummyDataAdder";
    private Context context;

    ContentResolver dummyContentResolver;


    ArrayList<User> users = new ArrayList<User>();
    ArrayList<Group> groups = new ArrayList<Group>();


    public DummyDataAdder(Context context) {
        this.context = context;
        dummyContentResolver = context.getContentResolver();

        for (int i=0; i<10; i++) {
            users.add(new User("User "+i,"user_image_"+i+".jpg","user"+i+"@example.com"));
        }

        groups.add(new Group("Group1","https://pbs.twimg.com/profile_images/916254721274515458/72vChEJI.jpg",null, null,false ));
        groups.add(new Group("Group2","twimg.com/profile_images/916254721274515458/72vChEJI.jpg",null, null,true ));
        groups.add(new Group("Group3","https://pbs.twimg.com/profile_images/916254721274515458/72vChEJI.jpg",null, null,false ));

    }

    private void addSingleTransaction(int counter) {
        ContentValues dummyContentValues = new ContentValues();

        dummyContentValues.put(TransactionTable.COLUMN_AMOUNT, String.valueOf((counter*1494)%1111));
        dummyContentValues.put(TransactionTable.COLUMN_PAID_BY,  String.valueOf(1));

        dummyContentValues.put(TransactionTable.COLUMN_INFO_NAME, "Transaction " + counter);
        Uri id = dummyContentResolver.insert(DatabaseProvider.CONTENT_TRANSACTION_URI, dummyContentValues);
        Log.i(TAG,"URI of new created transaction: "+id.toString());

    }

    private void addUsers() {
        for (int i=0;i<users.size(); i++ ) {
            User user = users.get(i);
            ContentValues dummyContentValues = new ContentValues();
            dummyContentValues.put(UserTable.COLUMN_USERNAME, user.getName());
            dummyContentValues.put(UserTable.COLUMN_IMAGE_URL,  user.getImage_url());
            dummyContentValues.put(UserTable.COLUMN_EMAIL, user.getEmail());
            dummyContentValues.put(UserTable.COLUMN_SYNCHRONIZED, false);
            Uri id = dummyContentResolver.insert(DatabaseProvider.CONTENT_USER_URI, dummyContentValues);
            String[] uriParts = id.toString().split("/");
            user.setId(Integer.parseInt(uriParts[1]));
        }
    }

    private void addGroups() {
        for (int i=0;i<groups.size(); i++ ) {
            Group group = groups.get(i);
            ContentValues dummyContentValues = new ContentValues();
            dummyContentValues.put(GroupTable.COLUMN_NAME,group.getName());
            dummyContentValues.put(GroupTable.COLUMN_IMAGE_URL,group.getImageUrl());
            dummyContentValues.put(GroupTable.COLUMN_SYNCHRONIZED,group.getSync());
            Uri id = dummyContentResolver.insert(DatabaseProvider.CONTENT_GROUP_URI, dummyContentValues);
            String[] uriParts = id.toString().split("/");
            group.setId(Integer.parseInt(uriParts[1]));
        }

        //delete and readd group for testing purpose
        Group groupL = groups.get(groups.size()-1);
        String [] args = {String.valueOf(groupL.getId())};
        int delId = dummyContentResolver.delete(DatabaseProvider.CONTENT_GROUP_URI,GroupTable.COLUMN_ID + "=? ",args);
        Log.i(TAG, "++++++++++++++++++ Deleted groups: " +delId + "+++++ with id: "+ groupL.getId());

        ContentValues dummyContentValues = new ContentValues();
        dummyContentValues.put(GroupTable.COLUMN_NAME,groupL.getName());
        dummyContentValues.put(GroupTable.COLUMN_IMAGE_URL,groupL.getImageUrl());
        dummyContentValues.put(GroupTable.COLUMN_SYNCHRONIZED,groupL.getSync());
        Uri id = dummyContentResolver.insert(DatabaseProvider.CONTENT_GROUP_URI, dummyContentValues);
        String[] uriParts = id.toString().split("/");
        groupL.setId(Integer.parseInt(uriParts[1]));
        Log.i(TAG, "++++++++++++++++++ New group ID: "+ groupL.getId());

    }

    private void addUsersToGroups() {
        for (int i=0;i<groups.size(); i++ ) {
            Group group = groups.get(i);
            for (int j=0;j<group.getUsers().size(); j++ ) {
                ContentValues dummyContentValues = new ContentValues();
                dummyContentValues.put(GroupUserTable.COLUMN_GROUP_ID, group.getId());
                dummyContentValues.put(GroupUserTable.COLUMN_USER_ID, users.get(j).getId());
                dummyContentResolver.insert(DatabaseProvider.CONTENT_GROUP_USER_URI, dummyContentValues);
            }
        }
    }


    public void addAllDummyData() {
        this.addUsers();
        this.addGroups();

        this.addUsersToGroups();


        //this.addTransactions(10);
    }



    private class User {
        private String name;
        private String image_url;
        private String email;
        private int id;

        public User(String name, String image_url, String email) {
            this.name = name;
            this.image_url = image_url;
            this.email = email;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getImage_url() {
            return image_url;
        }

        public String getEmail() {
            return email;
        }

        public int getId() {
            return id;
        }

        public String[] getAsArray() {
            return new String[]{
                this.name,
                this.image_url,
                this.email };
        }
    }

}
