package com.riskitbiskit.lumpiashmompianow.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riskitbiskit.lumpiashmompianow.R;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract.MenuEntry;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //Constants
    public static final int DETAIL_LOADER = 0;

    //Fields
    private Uri mRequestedMenuItemURI;
    private SharedPreferences mSharedPreferences;
    String mItemName;
    String mItemPrice;
    String mItemDescrip;
    String mItemHist;
    int mItemImgRes;
    ArrayList<String> mCartList;

    //Views
    @BindView(R.id.detail_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.detail_image_frame)
    ImageView detailFrame;
    @BindView(R.id.food_description_tv)
    TextView descriptionTV;
    @BindView(R.id.food_price_tv)
    TextView priceTV;
    @BindView(R.id.food_history_tv)
    TextView historyTV;
    @BindView(R.id.detail_border)
    LinearLayout borderLayout;
    @BindView(R.id.detail_fab)
    FloatingActionButton detailFab;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        //setup Toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //pull uri data from intent
        Intent receivingIntent = getIntent();
        mRequestedMenuItemURI = receivingIntent.getData();

        //initialize sharedPreference
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //setup UI with received uri using the database
        getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MenuEntry._ID,
                MenuEntry.COlUMN_ITEM_NAME,
                MenuEntry.COLUMN_ITEM_PRICE,
                MenuEntry.COLUMN_ITEM_DESCRIPTION,
                MenuEntry.COLUMN_ITEM_HISTORY,
                MenuEntry.COLUMN_ITEM_RESOURCE
        };

        return new CursorLoader(this,
                mRequestedMenuItemURI,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            mItemName = data.getString(data.getColumnIndex(MenuEntry.COlUMN_ITEM_NAME));
            mItemPrice = data.getString(data.getColumnIndex(MenuEntry.COLUMN_ITEM_PRICE));
            mItemDescrip = data.getString(data.getColumnIndex(MenuEntry.COLUMN_ITEM_DESCRIPTION));
            mItemHist = data.getString(data.getColumnIndex(MenuEntry.COLUMN_ITEM_HISTORY));
            mItemImgRes = data.getInt(data.getColumnIndex(MenuEntry.COLUMN_ITEM_RESOURCE));

            //set data to relevant views
            Glide.with(getBaseContext()).load(mItemImgRes).into(detailFrame);
            descriptionTV.setText(mItemDescrip);
            priceTV.setText("$" + mItemPrice);
            historyTV.setText(mItemHist);
            //since collapsing toolbar layout used, title must be set here instead of support toolbar
            mCollapsingToolbarLayout.setTitle(mItemName);

            //update the border and FAB color
            updateBorderColor(mItemImgRes);

            //Setup FAB
            detailFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //check to see there is already a list of selected food items in shared pref
                    if (mSharedPreferences.getString(MenuActivity.CHECKOUT_LIST, MenuActivity.EMPTY)
                            .contentEquals(MenuActivity.EMPTY)) {
                        //if not, create a list and add item to list
                        createNewCartList();
                    } else {
                        addItemToCartList();
                    }

                    //tell menu activity everything completely okay
                    setResult(RESULT_OK, null);
                    //finish the activity
                    finish();
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Do nothing
    }

    private void createNewCartList() {
        //create new cart list
        mCartList = new ArrayList<>();
        mCartList.add(mItemName);

        //Convert list to String via Gson
        Gson gson = new Gson();
        String json = gson.toJson(mCartList);

        //Add to shared preference
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(MenuActivity.CHECKOUT_LIST, json);
        editor.apply();

        //let user know that it was added to cart
        Toast.makeText(this, R.string.added_to_cart, Toast.LENGTH_SHORT).show();
    }

    private void addItemToCartList() {
        //get old list first
        String json = mSharedPreferences.getString(MenuActivity.CHECKOUT_LIST, MenuActivity.EMPTY);

        //convert list from string to array list
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        mCartList = gson.fromJson(json, type);

        //check if item is already on the list
        boolean isCopy = false;
        for(int i = 0; i < mCartList.size(); i++) {
            String currentItem = mCartList.get(i);
            if (currentItem.contentEquals(mItemName)) {
                isCopy = true;
                break;
            } else {
                isCopy = false;
            }
        }

        //set action for copy or unique item
        if (isCopy) {
            //if item already in cart, remind user
            Toast.makeText(getBaseContext(), R.string.already_in_cart, Toast.LENGTH_SHORT).show();
        } else {
            //if not, add to list
            mCartList.add(mItemName);

            //convert list back to string to prep for SP saving
            String newJson = gson.toJson(mCartList);

            //Add updated list to shared preference
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(MenuActivity.CHECKOUT_LIST, newJson);
            editor.apply();

            //let user know that it was added to cart
            Toast.makeText(getBaseContext(), R.string.added_to_cart, Toast.LENGTH_SHORT).show();
        }
    }

    //update border and FAB based on image for better UX
    private void updateBorderColor(int itemImgRes) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), itemImgRes);

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                borderLayout.setBackgroundColor(palette.getMutedSwatch().getRgb());
                detailFab.setBackgroundTintList(ColorStateList.valueOf(palette.getMutedSwatch().getRgb()));
            }
        });
    }
}