package com.media.interactive.cs3.hdm.interactivemedia.fragments;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.transforms.CircleTransform;


public class MapTransactionFragment extends Fragment implements IMyFragment,
        OnMapReadyCallback {
    private static final String TAG = MapTransactionFragment.class.getSimpleName();
    private GoogleMap mMap;
    private SimpleCursorAdapter groupAdapter;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_map_transaction, menu);

        final MenuItem item = menu.findItem(R.id.map_group_spinner);
        item.setActionView(R.layout.group_spinner);
        final Spinner spinner = item.getActionView().findViewById(R.id.menu_group_spinner);

        groupAdapter = initializeGroupAdapter();
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setMarkers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                setMarkers();
            }
        });

        spinner.setAdapter(groupAdapter);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private String getCurrentGroupId() {
        return groupAdapter.getCursor().getString(groupAdapter.getCursor().getColumnIndex(GroupTable.COLUMN_GROUP_ID));
    }

    private SimpleCursorAdapter initializeGroupAdapter() {

        final String[] projection = {GroupTable.TABLE_NAME + ".*"};
        final String sortOrder = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_CREATED_AT + " DESC";
        final String selection = UserTable.TABLE_NAME + "." + UserTable.COLUMN_USER_ID + " = ? AND " + GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_SYNCHRONIZED + " = 1";
        final String[] selectionArgs = {Login.getInstance().getUser().getUserId()};
        final Cursor query = getActivity().getContentResolver().query(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI, projection, selection, selectionArgs, sortOrder);

        final String[] columns = new String[]{GroupTable.COLUMN_NAME};
        final int[] to = new int[]{android.R.id.text1};

        final SimpleCursorAdapter groupAdapter = new SimpleCursorAdapter(this.getContext(), android.R.layout.simple_spinner_item, query, columns, to, 0);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return groupAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_map_transaction, container, false);

        final MapFragment mapFragment = (MapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setMarkers();
    }


    private void setMarkers() {
        if (mMap != null) {
            mMap.clear();
            final String[] projection = {TransactionTable.TABLE_NAME + ".*"};
            final String sortOrder = TransactionTable.TABLE_NAME + "." + TransactionTable.COLUMN_INFO_CREATED_AT + " DESC";
            final String selection = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_GROUP_ID + " = ? AND " + UserTable.TABLE_NAME + "." + UserTable.COLUMN_USER_ID + " = ?";
            final String[] selectionArgs = {getCurrentGroupId(), Login.getInstance().getUser().getUserId()};
            final Cursor transactions = getActivity().getContentResolver().query(DatabaseProvider.CONTENT_GROUP_USER_TRANSACTION_JOIN_URI, projection, selection, selectionArgs, sortOrder);
            Log.d(TAG, "Transactions Count: " + transactions.getCount());

            LatLngBounds.Builder builder = LatLngBounds.builder();
            int markers = 0;
            while (transactions.moveToNext()) {

                final String name = transactions.getString(transactions.getColumnIndex(TransactionTable.COLUMN_INFO_NAME));
                final String imageUrl = transactions.getString(transactions.getColumnIndex(TransactionTable.COLUMN_INFO_IMAGE_URL));
                final double longitude = transactions.getDouble(transactions.getColumnIndex(TransactionTable.COLUMN_INFO_LOCATION_LONG));
                final double amount = transactions.getDouble(transactions.getColumnIndex(TransactionTable.COLUMN_AMOUNT));
                final double latitude = transactions.getDouble(transactions.getColumnIndex(TransactionTable.COLUMN_INFO_LOCATION_LAT));
                if (longitude != 0 && latitude != 0) {
                    markers += 1;
                    final LatLng position = new LatLng(latitude, longitude);
                    final MarkerOptions markerOptions = new MarkerOptions().position(position).title(name);
                    if (imageUrl != null) {
                        Glide.with(this)
                                .load(imageUrl)
                                .asBitmap()
                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                .centerCrop()
                                .transform(new CircleTransform(getContext())) // applying the image transformer
                                .override(100, 100)
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                        mMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                                    }
                                });
                    } else {
                        mMap.addMarker(markerOptions);
                    }
                    builder = builder.include(position);
                }
            }
            if (markers > 0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
            }
        }
    }

    public MapTransactionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    // TODO: Rename method, update argument and hook method into UI event

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View.OnClickListener getOnFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "Hello from " + TAG);
            }
        };
    }

}
