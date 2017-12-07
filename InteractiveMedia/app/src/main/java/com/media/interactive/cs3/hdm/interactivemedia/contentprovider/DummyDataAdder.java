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

        groups.add(new Group("Group1","group_image_1.jpg",new int[] {0,1,2}));
        groups.add(new Group("Group2","group_image_2.jpg",new int[] {3,4,5}));
        groups.add(new Group("Group3","group_image_3.jpg",new int[] {5,6,7,8,9}));

    }

    private void addSingleTransaction(int counter) {
        ContentValues dummyContentValues = new ContentValues();

        dummyContentValues.put(TransactionTable.COLUMN_AMOUNT, String.valueOf((counter*1494)%1111));
        dummyContentValues.put(TransactionTable.COLUMN_PAID_BY,  String.valueOf(1));

        dummyContentValues.put(TransactionTable.COLUMN_INFO_NAME, "Transaction " + counter);
        dummyContentValues.put(TransactionTable.COLUMN_INFO_LOCATION, "Ghetto Netto");
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
            dummyContentValues.put(GroupTable.COLUMN_IMAGE_URL,group.getImage_url());
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
        dummyContentValues.put(GroupTable.COLUMN_IMAGE_URL,groupL.getImage_url());
        Uri id = dummyContentResolver.insert(DatabaseProvider.CONTENT_GROUP_URI, dummyContentValues);
        String[] uriParts = id.toString().split("/");
        groupL.setId(Integer.parseInt(uriParts[1]));
        Log.i(TAG, "++++++++++++++++++ New group ID: "+ groupL.getId());


    }

    private void addUsersToGroups() {
        for (int i=0;i<groups.size(); i++ ) {
            Group group = groups.get(i);
            for (int j=0;j<group.getUsers().length; j++ ) {
                ContentValues dummyContentValues = new ContentValues();
                dummyContentValues.put(GroupUserTable.COLUMN_GROUP_ID, group.getId());
                dummyContentValues.put(GroupUserTable.COLUMN_USER_ID, users.get(group.getUsers()[j]).getId());
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


    private class Group {
        private String name;
        private String image_url;
        private int[] users;
        private int id;

        public int getId() {
            return id;
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

        public Group(String name, String image_url, int[] users) {
            this.name = name;
            this.image_url = image_url;
            this.users = users;
        }

        public String[] getAsArray() {
            return new String[]{
                    this.name,
                    this.image_url};
        }

        public int [] getUsers () {
            return users;
        }


    }

}
