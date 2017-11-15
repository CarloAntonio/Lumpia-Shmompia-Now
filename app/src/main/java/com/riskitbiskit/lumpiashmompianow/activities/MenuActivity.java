package com.riskitbiskit.lumpiashmompianow.activities;

import android.content.ContentValues;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.riskitbiskit.lumpiashmompianow.R;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.riskitbiskit.lumpiashmompianow.data.MenuContract.*;

public class MenuActivity extends AppCompatActivity implements MenuFragment.OnMenuItemClickListener {

    //Constants
    public static final String CHECKOUT_LIST = "checkoutList";
    public static final String EMPTY = "empty";
    public static final String SAVED_INSTANCE_ITEM_ID = "savedItemID";

    //Variables
    private String[] mMenuTitles;
    private ActionBarDrawerToggle mDrawerToggle;
    private SharedPreferences sharedPreferences;
    private long currentItemId;

    //Views
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.left_drawer)
    ListView mDrawerList;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);

        //Setup custom toolbar
        setSupportActionBar(mToolbar);

        //Get reference of Shared Preference
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Setup Hamburger Menu
        mMenuTitles = getResources().getStringArray(R.array.menu_options);

        mDrawerLayout.setScrimColor(ContextCompat.getColor(this, android.R.color.transparent));

        mDrawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mMenuTitles));
        mDrawerList.setOnItemClickListener(new AdapterClickListener());

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDrawerToggle.syncState();

        //SaveInstanceState Values
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SAVED_INSTANCE_ITEM_ID)) {
                currentItemId = savedInstanceState.getLong(SAVED_INSTANCE_ITEM_ID);
                Log.e("Tag", currentItemId + "");
            }
        }  else {
            currentItemId = 1;
        }

        if (findViewById(R.id.two_panel_layout) != null) {
            editor.putBoolean(getString(R.string.is_two_panel), true);
            editor.apply();

            FragmentManager fragmentManager = getSupportFragmentManager();

            if (savedInstanceState == null) {
                DetailFragment detailFragment = new DetailFragment();
                detailFragment.setRequestedItemUri(currentItemId);

                fragmentManager.beginTransaction()
                        .add(R.id.detail_frag_container, detailFragment)
                        .commit();
            } else {
                DetailFragment newDetailFragment = new DetailFragment();
                newDetailFragment.setRequestedItemUri(currentItemId);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_frag_container, newDetailFragment)
                        .commit();
            }

        } else {
            editor.putBoolean(getString(R.string.is_two_panel), false);
            editor.apply();
        }
    }

    @Override
    public void onMenuItemClicked(long id) {
        DetailFragment newDetailFragment = new DetailFragment();
        newDetailFragment.setRequestedItemUri(id);

        currentItemId = id;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_frag_container, newDetailFragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(SAVED_INSTANCE_ITEM_ID, currentItemId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //if any items in the menu is clicked...return true
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_home_action:
                Intent intent = new Intent(getBaseContext(), OrderActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_home_action_empty:
                Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.nothing_in_cart), Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (sharedPreferences.getString(CHECKOUT_LIST, EMPTY).contentEquals(EMPTY)) {
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

    public class AdapterClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            //Get Instance of shared preference editor
            SharedPreferences.Editor editor = sharedPreferences.edit();

            switch (position) {
                case 0:
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    if (sharedPreferences.getString(OrderActivity.PREVIOUS_ORDER, MenuActivity.EMPTY).contentEquals(MenuActivity.EMPTY)) {
                        Toast.makeText(getBaseContext(), R.string.no_previous_order, Toast.LENGTH_SHORT).show();
                    } else {
                        //Update shared preference with old order
                        String previousOrder = sharedPreferences.getString(OrderActivity.PREVIOUS_ORDER, MenuActivity.EMPTY);
                        editor.putString(MenuActivity.CHECKOUT_LIST, previousOrder);
                        editor.apply();

                        Intent reorderIntent = new Intent(getBaseContext(), OrderActivity.class);
                        startActivity(reorderIntent);
                    }
                    return;
                case 1:
                    Intent menuIntent = new Intent(getBaseContext(), MenuActivity.class);
                    startActivity(menuIntent);
                    finish();
                    return;
                case 2:
                    if (sharedPreferences.getString(CHECKOUT_LIST, EMPTY).contentEquals(EMPTY)) {
                        Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.nothing_in_cart), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent checkoutIntent = new Intent(getBaseContext(), OrderActivity.class);
                        startActivity(checkoutIntent);
                    }
                    return;
                case 3:
                    //Delete all items in Shared Preferences
                    editor.putString(CHECKOUT_LIST, EMPTY);
                    editor.apply();

                    //Reset all item totals in database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MenuEntry.COLUMN_ITEM_COUNT, getBaseContext().getString(R.string.one));
                    getContentResolver().update(MenuEntry.CONTENT_URI, contentValues, null, null);

                    //Restart activity
                    Intent clearCartIntent = new Intent(getBaseContext(), MenuActivity.class);
                    startActivity(clearCartIntent);
                    finish();
                    return;
                case 4:
                    Intent aboutIntent = new Intent(getBaseContext(), AboutActivity.class);
                    startActivity(aboutIntent);
                    finish();
                    return;
                default:
                    return;
            }
        }
    }
}
