package com.riskitbiskit.lumpiashmompianow.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.riskitbiskit.lumpiashmompianow.R;
import com.riskitbiskit.lumpiashmompianow.utils.AdapterClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MenuActivity extends AppCompatActivity implements MenuFragment.OnMenuItemClickListener {

    //Constants
    public static final String CHECKOUT_LIST = "checkoutList";
    public static final String EMPTY = "empty";
    public static final String SAVED_INSTANCE_ITEM_ID = "savedItemID";

    //Variables
    private ActionBarDrawerToggle mDrawerToggle;
    private SharedPreferences mSharedPreferences;
    private long mCurrentItemId;
    private Context mContext = this;

    //Views
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.left_drawer)
    ListView mDrawerList;
    @BindView(R.id.menu_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);

        //setup custom toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //get reference of Shared Preference & SP Editor
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        //setup drawer
        setupDrawer();

        //check for saved instance values
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SAVED_INSTANCE_ITEM_ID)) {
                mCurrentItemId = savedInstanceState.getLong(SAVED_INSTANCE_ITEM_ID);
                Log.e("Tag", mCurrentItemId + "");
            }
        }  else {
            mCurrentItemId = 1;
        }

        //determine if user is using a tablet(two panel layout) or phone(single panel layout)
        if (findViewById(R.id.two_panel_layout) != null) {
            //save layout type for orientation changes
            editor.putBoolean(getString(R.string.is_two_panel), true);
            editor.apply();

            //get reference of fragment manager
            FragmentManager fragmentManager = getSupportFragmentManager();

            //checks to see if fragment needs to be initially created
            if (savedInstanceState == null) {
                //if so, create new fragment
                DetailFragment detailFragment = new DetailFragment();
                detailFragment.setRequestedItemUri(mCurrentItemId);

                fragmentManager.beginTransaction()
                        .add(R.id.detail_frag_container, detailFragment)
                        .commit();
            } else {
                //if not, swap new fragment
                DetailFragment newDetailFragment = new DetailFragment();
                newDetailFragment.setRequestedItemUri(mCurrentItemId);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_frag_container, newDetailFragment)
                        .commit();
            }

        } else {
            editor.putBoolean(getString(R.string.is_two_panel), false);
            editor.apply();
        }
    }

    private void setupDrawer() {
        mDrawerLayout.setScrimColor(ContextCompat.getColor(this, android.R.color.transparent));

        mDrawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.menu_options)));

        mDrawerList.setOnItemClickListener(new AdapterClickListener(mContext, mSharedPreferences));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);

        //Performs drawer <-> back button animation
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();
    }

    //for two panel, swaps detail fragment when menu item is clicked
    @Override
    public void onMenuItemClicked(long id) {
        DetailFragment newDetailFragment = new DetailFragment();
        newDetailFragment.setRequestedItemUri(id);

        mCurrentItemId = id;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_frag_container, newDetailFragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save current item, keeps fragment during orientation change, else item 1
        //will be displayed during orientation change every time
        outState.putLong(SAVED_INSTANCE_ITEM_ID, mCurrentItemId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //if any items in the menu is clicked...return true
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //setup shopping cart icon actions
        switch (item.getItemId()) {
            case R.id.menu_home_action:
                Intent intent = new Intent(mContext, OrderActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_home_action_empty:
                Toast.makeText(mContext, getBaseContext().getString(R.string.nothing_in_cart), Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        //determine which icon to show to user based on if items are in basket
        if (mSharedPreferences.getString(CHECKOUT_LIST, EMPTY).contentEquals(EMPTY)) {
            menu.findItem(R.id.menu_home_action).setVisible(false);
            menu.findItem(R.id.menu_home_action_empty).setVisible(true);
        } else {
            menu.findItem(R.id.menu_home_action).setVisible(true);
            menu.findItem(R.id.menu_home_action_empty).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_menu, menu);
        return true;
    }
}
