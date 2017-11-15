package com.riskitbiskit.lumpiashmompianow.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
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

    //Variables
    private Uri requestedMenuItemURI;
    private SharedPreferences mSharedPreferences;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        //Setup Toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Catch intent
        Intent receivingIntent = getIntent();
        requestedMenuItemURI = receivingIntent.getData();

        //Initialize sharedPreference
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        CursorLoader cursorLoader = new CursorLoader(this,
                requestedMenuItemURI,
                projection,
                null,
                null,
                null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            final String itemName = data.getString(data.getColumnIndex(MenuEntry.COlUMN_ITEM_NAME));
            String itemPrice = data.getString(data.getColumnIndex(MenuEntry.COLUMN_ITEM_PRICE));
            String itemDesc = data.getString(data.getColumnIndex(MenuEntry.COLUMN_ITEM_DESCRIPTION));
            String itemHist = data.getString(data.getColumnIndex(MenuEntry.COLUMN_ITEM_HISTORY));
            int itemImgRes = data.getInt(data.getColumnIndex(MenuEntry.COLUMN_ITEM_RESOURCE));

            //Set data to relevant views
            Glide.with(getBaseContext()).load(itemImgRes).into(detailFrame);
            descriptionTV.setText(itemDesc);
            priceTV.setText("$" + itemPrice);
            historyTV.setText(itemHist);
            getSupportActionBar().setTitle(itemName);

            //Update the border color
            updateBorderColor(itemImgRes);

            //Setup FAB
            detailFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSharedPreferences.getString(MenuActivity.CHECKOUT_LIST, MenuActivity.EMPTY).contentEquals(MenuActivity.EMPTY)) {
                        //if no list, create a list
                        ArrayList<String> cartList = new ArrayList<>();
                        cartList.add(itemName);

                        //Convert list to String via Gson
                        Gson gson = new Gson();
                        String json = gson.toJson(cartList);

                        //Add to shared preference
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putString(MenuActivity.CHECKOUT_LIST, json);
                        editor.apply();

                    } else {
                        //Get old list first
                        String json = mSharedPreferences.getString(MenuActivity.CHECKOUT_LIST, MenuActivity.EMPTY);
                        Gson gson = new Gson();
                        Type type = new TypeToken<ArrayList<String>>() {}.getType();
                        ArrayList<String> cartList = gson.fromJson(json, type);

                        boolean isCopy = false;

                        for(int i = 0; i < cartList.size(); i++) {
                            String currentItem = cartList.get(i);
                            if (currentItem.contentEquals(itemName)) {
                                isCopy = true;
                                break;
                            } else {
                                isCopy = false;
                            }
                        }

                        if (isCopy) {
                            Toast.makeText(getBaseContext(), R.string.already_in_cart, Toast.LENGTH_SHORT).show();
                        } else {
                            //Add to list
                            cartList.add(itemName);
                            String newJson = gson.toJson(cartList);

                            //Add updated list to shared preference
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString(MenuActivity.CHECKOUT_LIST, newJson);
                            editor.apply();

                            Toast.makeText(getBaseContext(), R.string.added_to_cart, Toast.LENGTH_SHORT).show();
                        }
                    }
                    setResult(RESULT_OK, null);
                    finish();
                }
            });
        }
    }

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


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}