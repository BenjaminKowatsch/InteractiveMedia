package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.fragments.GroupFragment;
import com.media.interactive.cs3.hdm.interactivemedia.fragments.IMyFragment;
import com.media.interactive.cs3.hdm.interactivemedia.fragments.TransactionFragment;

public class HomeActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    AdapterView.OnItemSelectedListener {

    private static final String TAG = "HomeActivity";

    private FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        displayFragment(R.id.nav_groups);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void displayFragment(int id) {

        Fragment fragment = null;

        switch (id) {
            case R.id.nav_groups:

                fab.show();
                Log.d(TAG, "item with id nav_groups was selected");
                fragment = new GroupFragment();
                break;
            case R.id.nav_transactions:
                fab.show();
                Log.d(TAG, "item with id nav_transactions was selected");
                fragment = new TransactionFragment();
                break;
            case R.id.nav_logout:
                Login.getInstance().logout(this)
                    .thenAccept((voidParam) -> {
                        Log.d(TAG, "Successfully logged out.");
                        Toast.makeText(HomeActivity.this, "Sucessfully logged out.", Toast.LENGTH_SHORT).show();
                        final Intent toLogin = new Intent(HomeActivity.this, LoginActivity.class);
                        toLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(toLogin);
                        finish();
                    })
                    .exceptionally(error -> {
                        Log.d(TAG, "Failed during logout.");
                        Toast.makeText(HomeActivity.this, "Logout failed", Toast.LENGTH_SHORT).show();
                        throw new RuntimeException(error.getMessage());
                    });


                break;
            default:
                fab.hide();
                Log.e(TAG, "No item id was selected");
                break;
        }
        if (fragment != null) {
            IMyFragment myFragment = (IMyFragment) fragment;
            fab.setOnClickListener(myFragment.getOnFabClickListener());
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_home, fragment);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        displayFragment(id);

        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Toast.makeText(this, parent.toString()
            + " Selected Group/Transaction at position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(this, "Selected nothing ", Toast.LENGTH_SHORT).show();
    }
}
