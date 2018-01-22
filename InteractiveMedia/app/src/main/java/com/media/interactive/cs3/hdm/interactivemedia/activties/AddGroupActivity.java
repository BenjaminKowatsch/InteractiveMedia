package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.media.interactive.cs3.hdm.interactivemedia.CallbackListener;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.RestRequestQueue;
import com.media.interactive.cs3.hdm.interactivemedia.authorizedrequests.AuthorizedJsonObjectRequest;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.DatabaseProviderHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;
import com.media.interactive.cs3.hdm.interactivemedia.recyclerview.NonScrollRecyclerView;
import com.media.interactive.cs3.hdm.interactivemedia.recyclerview.RecyclerItemTouchHelper;
import com.media.interactive.cs3.hdm.interactivemedia.recyclerview.UserEmailAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddGroupActivity extends ImagePickerActivity implements View.OnClickListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

  private final static String TAG = AddGroupActivity.class.getSimpleName();

  private EditText groupName;
  private Button addNewUser;
  private Button saveGroup;
  private Button cancel;

  private LinearLayout linearLayout;
  private NonScrollRecyclerView recyclerView;
  private List<User> userList;
  private UserEmailAdapter mAdapter;

  private DatabaseProviderHelper helper;
  //TODO: Remove content resolver
  private ContentResolver contentResolver;
  private Group toAdd;

  @Override
  public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
    if (viewHolder instanceof UserEmailAdapter.SimpleViewHolder) {
      // get the removed item name to display it in snack bar
      final User user = userList.get(viewHolder.getAdapterPosition());

      // backup of removed item for undo purpose
      final User deletedUser = userList.get(viewHolder.getAdapterPosition());
      final int deletedIndex = viewHolder.getAdapterPosition();

      // remove the item from recycler view
      mAdapter.removeUser(viewHolder.getAdapterPosition());

      // showing snack bar with Undo option
      final Snackbar snackbar = Snackbar
          .make(linearLayout, user.getEmail() + " removed from user list", Snackbar.LENGTH_LONG);
      snackbar.setAction("UNDO", new View.OnClickListener() {
        @Override
        public void onClick(View view) {

          // undo is selected, restore the deleted item
          mAdapter.restoreUser(deletedUser, deletedIndex);
        }
      });
      snackbar.setActionTextColor(Color.YELLOW);
      snackbar.show();
    }

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_group);

    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle("");

    linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
    groupName = (EditText) findViewById(R.id.et_group_name);
    final TextView groupNameError = (TextView) findViewById(R.id.et_group_name_error);
    addNewUser = (Button) findViewById(R.id.bn_group_add_user);
    saveGroup = (Button) findViewById(R.id.bn_group_save);
    cancel = (Button) findViewById(R.id.bn_group_cancel);
    recyclerView = (NonScrollRecyclerView) findViewById(R.id.recycler_view);

    saveGroup.setEnabled(false);

    contentResolver = getContentResolver();
    helper = new DatabaseProviderHelper(contentResolver);

    addNewUser.setOnClickListener(this);
    addNewUser.setOnClickListener(this);
    saveGroup.setOnClickListener(this);
    cancel.setOnClickListener(this);

    userList = new ArrayList<>();
    mAdapter = new UserEmailAdapter(this, userList);

    final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
    recyclerView.setLayoutManager(mLayoutManager);
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    recyclerView.setAdapter(mAdapter);

    final ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
    new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

    groupName.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void afterTextChanged(Editable editable) {
        if (Helper.isGroupNameValid(editable.toString())) {
          groupNameError.setVisibility(View.GONE);
          saveGroup.setEnabled(true);
        } else {
          groupNameError.setVisibility(View.VISIBLE);
          saveGroup.setEnabled(false);
        }
      }
    });
    final UUID randomUUID = UUID.randomUUID();
    final String randomFilename = randomUUID.toString() + ".png";
    initImagePickerActivity(R.id.et_group_image, randomFilename, false);

    // Get email of current user
    final User admin = new User();
    String adminEmail = Login.getInstance().getUser().getEmail();
    if (adminEmail != null) {
      admin.setEmail(Login.getInstance().getUser().getEmail());
    } else {
      // debug purpose: TODO comment
      admin.setEmail("Admin.User@gmail.com");
    }

    userList.add(admin);
  }


  private void showAddEmailDialog() {
    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
    final LayoutInflater inflater = this.getLayoutInflater();
    final View dialogView = inflater.inflate(R.layout.add_user_email, null);
    dialogBuilder.setView(dialogView);

    final TextView errorMessage = (TextView) dialogView.findViewById(R.id.add_user_mail_error);

    final EditText editText = (EditText) dialogView.findViewById(R.id.add_user_email);
    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

    dialogBuilder.setTitle("Add new user email");
    dialogBuilder.setMessage("Enter email below");
    dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        userList.add(new User(editText.getText().toString()));
      }
    });
    dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        //pass
      }
    });
    final AlertDialog alertDialog = dialogBuilder.create();
    alertDialog.show();

    final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
    positiveButton.setEnabled(false);

    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void afterTextChanged(Editable editable) {
        if (Helper.isEmailValid(editable.toString())) {
          errorMessage.setVisibility(View.GONE);
          positiveButton.setEnabled(true);
        } else {
          errorMessage.setVisibility(View.VISIBLE);
          positiveButton.setEnabled(false);
        }
      }
    });

  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.bn_group_add_user:
        showAddEmailDialog();
        break;
      case R.id.bn_group_save:
        try {
          saveGroup();
        } catch (JSONException e) {
          e.printStackTrace();
        }
        break;
      case R.id.bn_group_cancel:
        finish();
        break;
      default:
        Log.e(TAG, "Unhandled onclick event.");
        break;
    }
  }

  private void saveGroup() throws JSONException {

    // Save into database as unsynchronized group
    toAdd = new Group();
    toAdd.setName(groupName.getText().toString());
    toAdd.setCreatedAt(Helper.getDateTime());
    toAdd.setSync(false);
    toAdd.setUsers(userList);

    // Upload group image if sending the group data was successfull
    if (getCurrentPhotoPath() != null) {
      toAdd.setImageUrl(getCurrentPhotoPath());
    }
    Log.d(TAG, toAdd.toJson().toString());
    uploadImage(new CallbackListener<JSONObject, Exception>() {
      @Override
      public void onSuccess(JSONObject response) {
        JSONObject payload = null;
        String imageName = null;
        try {
          payload = response.getJSONObject("payload");
          imageName = payload.getString("path");
          Log.d(TAG, "Path returned: " + payload.getString("path"));
        } catch (JSONException e) {
          e.printStackTrace();
        }
        final String newImageUrl = getResources().getString(R.string.web_service_url)
            .concat("/v1/object-store/download?filename=").concat(imageName);
        toAdd.setImageUrl(newImageUrl);
        try {
          sendToBackend(toAdd);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onFailure(Exception error) {
        makeToast(error.getMessage());
        try {
          sendToBackend(toAdd);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    });

  }

  private void sendToBackend(final Group group) throws JSONException {
    helper.insertGroupAtDatabase(group);
    final String url = getResources().getString(R.string.web_service_url).concat("/v1/groups/");
    Log.d(TAG, "url: " + url);
    final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
        Request.Method.POST, url, group.toJson(), new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject response) {

        Log.d(TAG, response.toString());
        // Update group and user data
        try {
          if (response.getBoolean("success") == true) {
            final JSONObject payload = response.getJSONObject("payload");
            final String groupId = payload.getString("groupId");
            // Update group values
            final ContentValues groupUpdateValues = new ContentValues();
            groupUpdateValues.put(GroupTable.COLUMN_GROUP_ID, groupId);
            groupUpdateValues.put(GroupTable.COLUMN_SYNCHRONIZED, true);
            final String groupSelection = GroupTable.COLUMN_ID.concat(" = ?");
            final String[] groupSelectionArgs = {String.valueOf(group.getId())};
            contentResolver.update(DatabaseProvider.CONTENT_GROUP_URI, groupUpdateValues, groupSelection, groupSelectionArgs);
            // Update user values
            final JSONArray users = payload.getJSONArray("users");
            for (int i = 0; i < users.length(); i++) {
              final JSONObject jsonObject = (JSONObject) users.get(i);
              final ContentValues userUpdateValues = new ContentValues();
              final String userEmail = jsonObject.getString("email");
              if (jsonObject.has("username")) {
                userUpdateValues.put(UserTable.COLUMN_USERNAME, jsonObject.getString("username"));
              }
              try{
                userUpdateValues.put(UserTable.COLUMN_IMAGE_URL, jsonObject.getString("imageUrl"));
              }catch(JSONException error){

              }
              userUpdateValues.put(UserTable.COLUMN_USER_ID, jsonObject.getString("userId"));
              userUpdateValues.put(UserTable.COLUMN_SYNCHRONIZED, true);
              final String userSelection = UserTable.COLUMN_EMAIL.concat(" = ?");
              final String[] userSelectionArgs = {userEmail};
              contentResolver.update(DatabaseProvider.CONTENT_USER_URI, userUpdateValues, userSelection, userSelectionArgs);
            }
            makeToast("Updated group and users.");
          } else {
            makeToast("Error while creating group at backend.");
          }
          finish();
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        makeToast("Error while sending the group to backend.");
        finish();
      }
    });
    RestRequestQueue.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);

  }


}
