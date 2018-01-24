package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.app.AlertDialog;
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
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.adapter.UserEmailAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.data.DatabaseProviderHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;
import com.media.interactive.cs3.hdm.interactivemedia.util.CallbackListener;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;
import com.media.interactive.cs3.hdm.interactivemedia.views.NonScrollRecyclerView;
import com.media.interactive.cs3.hdm.interactivemedia.views.RecyclerItemTouchHelper;
import com.media.interactive.cs3.hdm.interactivemedia.volley.AuthorizedJsonObjectRequest;
import com.media.interactive.cs3.hdm.interactivemedia.volley.RestRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * The Class AddGroupActivity.
 */
public class AddGroupActivity extends ImagePickerActivity implements View.OnClickListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    /**
     * The Constant TAG.
     */
    private static final String TAG = AddGroupActivity.class.getSimpleName();

    /**
     * The group name.
     */
    private EditText groupName;

    /**
     * The add new user.
     */
    private Button addNewUser;

    /**
     * The save group.
     */
    private Button saveGroup;

    /**
     * The linear layout.
     */
    private LinearLayout linearLayout;

    /**
     * The recycler view.
     */
    private NonScrollRecyclerView recyclerView;

    /**
     * The user list.
     */
    private List<User> userList;

    /**
     * The m adapter.
     */
    private UserEmailAdapter mAdapter;

    /**
     * The helper.
     */
    private DatabaseProviderHelper helper;

    /**
     * The to add.
     */
    private Group toAdd;

    /**
     * On swiped.
     *
     * @param viewHolder the view holder
     * @param direction  the direction
     * @param position   the position
     */
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

    /**
     * On create.
     *
     * @param savedInstanceState the saved instance state
     */
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
        recyclerView = (NonScrollRecyclerView) findViewById(R.id.recycler_view);

        saveGroup.setEnabled(false);

        helper = new DatabaseProviderHelper(getContentResolver());

        addNewUser.setOnClickListener(this);
        addNewUser.setOnClickListener(this);
        saveGroup.setOnClickListener(this);

        userList = new ArrayList<>();
        mAdapter = new UserEmailAdapter(this, userList);

        final Button cancel = findViewById(R.id.bn_group_save_cancel);
        cancel.setOnClickListener(this);

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
        final UUID randomUuid = UUID.randomUUID();
        final String randomFilename = randomUuid.toString() + ".png";
        initImagePickerActivity(R.id.et_group_image, randomFilename, false);

        // Get email of current user
        final User admin = new User();
        admin.setEmail(Login.getInstance().getUser().getEmail());
        userList.add(admin);
    }

    /**
     * On click.
     *
     * @param view the view
     */
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
            case R.id.bn_group_save_cancel:
                finish();
                break;
            default:
                Log.e(TAG, "Unhandled onclick event.");
                break;
        }
    }

    /**
     * Creates a dialog to enable the user to add valid emails to the current group.
     */
    private void showAddEmailDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        final LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_user_email, null);
        dialogBuilder.setView(dialogView);

        final TextView errorMessage = (TextView) dialogView.findViewById(R.id.add_user_mail_error);

        final EditText editText = (EditText) dialogView.findViewById(R.id.add_user_email);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        dialogBuilder.setTitle(R.string.AddEmailDialogTitle);
        dialogBuilder.setMessage(R.string.AddEmailDialogMessage);
        dialogBuilder.setPositiveButton(R.string.AddEmailDialogPositiveButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                userList.add(new User(editText.getText().toString()));
            }
        });
        dialogBuilder.setNegativeButton(R.string.AddEmailDialogNegativeButton, new DialogInterface.OnClickListener() {
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

    /**
     * Gathers the data input from the UI and sets it to the group data model.
     * Tries to upload the group optional group image. If the upload succeeds the group data will be uploaded next.
     * When no group image was selected the group data will be uploaded immediately.
     *
     * @throws JSONException the JSON exception
     */
    private void saveGroup() throws JSONException {

        // Save into database as unsynchronized group
        toAdd = new Group();
        toAdd.setName(groupName.getText().toString());
        toAdd.setCreatedAt(Helper.getDateTime());
        toAdd.setSync(false);
        toAdd.setUsers(userList);

        // Upload group image if sending the group data was successful
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
                    .concat(getString(R.string.requestPathDownload)).concat(imageName);
                toAdd.setImageUrl(newImageUrl);
                try {
                    sendToBackend(toAdd);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception error) {
                Log.d(TAG, error.getMessage());
                try {
                    sendToBackend(toAdd);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Saves the data group object into the database marked as 'unsynchronized'.
     * Tries to upload the the group data. If the upload succeeds the group is marked as 'synchronized'.
     *
     * @param group The group data object to be uploaded
     * @throws JSONException Converting the group data into an JSON object may throw an error.
     */
    private void sendToBackend(final Group group) throws JSONException {
        helper.insertGroupAtDatabase(group);
        final String url = getResources().getString(R.string.web_service_url).concat(getString(R.string.requestPathCreateGroup));
        Log.d(TAG, "url: " + url);
        final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
            Request.Method.POST, url, group.toJson(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, response.toString());
                // Update group and user data
                try {
                    if (response.getBoolean("success") == true) {
                        helper.updateGroupAndUsers(group, response);
                        makeToast(getString(R.string.messageUpdatedGroup));
                    } else {
                        makeToast(getString(R.string.errorMessageUpdatedGroup));
                    }
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                makeToast(getString(R.string.requestErrorMessageCreateGroup));
                finish();
            }
        });
        RestRequestQueue.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);

    }


}
