package com.riskitbiskit.lumpiashmompianow.activities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.riskitbiskit.lumpiashmompianow.R;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.riskitbiskit.lumpiashmompianow.data.MenuContract.*;

public class MainActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener{

    //Constants
    public static final String API_KEY = "AIzaSyCyNJM9eJF9Vs1GChSah_MQApssqYoNMtQ";
    public static final String INTRO_VIDEO = "HITJz8oiYxw";
    public static final String NUM_ITEMS = "numItems";

    //Variables
    SharedPreferences sharedPreferences;

    //Views
    @BindView(R.id.menu_container)
    ImageView menuContainerImageView;
    @BindView(R.id.restaurant_container)
    ImageView restaurantContainerImageView;
    @BindView(R.id.reorder_container)
    ImageView reorderContainerImageView;
    @BindView(R.id.youtube_view)
    YouTubePlayerView mTubePlayerView;
    @BindView(R.id.main_reorder_view)
    FrameLayout reoderFrameLayout;
    @BindView(R.id.adView)
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        checkForNewMenuItems(this);

        //Get reference of Shared Preference
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getString(OrderActivity.PREVIOUS_ORDER, MenuActivity.EMPTY).contentEquals(MenuActivity.EMPTY)) {
            reoderFrameLayout.setVisibility(View.GONE);
        } else {
            reoderFrameLayout.setVisibility(View.VISIBLE);
        }

        mTubePlayerView.initialize(API_KEY, this);

        Glide.with(this).load(R.drawable.menu).into(menuContainerImageView);
        Glide.with(this).load(R.drawable.restaurant).into(restaurantContainerImageView);
        Glide.with(this).load(R.drawable.reorder).into(reorderContainerImageView);

        reorderContainerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Update shared preference with old order
                String previousOrder = sharedPreferences.getString(OrderActivity.PREVIOUS_ORDER, MenuActivity.EMPTY);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(MenuActivity.CHECKOUT_LIST, previousOrder);
                editor.apply();

                Intent intent = new Intent(getBaseContext(), OrderActivity.class);
                startActivity(intent);
            }
        });

        menuContainerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), MenuActivity.class);
                startActivity(intent);
            }
        });

        restaurantContainerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), AboutActivity.class);
                startActivity(intent);
            }
        });

        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
    }

    private void checkForNewMenuItems(Context context) {
        //Get a reference to sharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //Check what the item count was previously
        int previousItemCount = sharedPreferences.getInt(NUM_ITEMS,0);

        //Check the current item count
        String[] itemNames = context.getResources().getStringArray(R.array.menu_items_list);

        //Compare item count
        if (previousItemCount != itemNames.length) {
            deleteDatabase(context);
            createDatabase(context);
        }
    }

    //Delete entire table, not currently used
    public void deleteDatabase(Context context) {
        //rowsDeleted not used at this time
        int rowsDeleted = context.getContentResolver().delete(MenuEntry.CONTENT_URI, null, null);
    }

    public void createDatabase(Context context) {
        int[] imageResources = new int[] {R.drawable.white_rice,
                R.drawable.garlic_rice, R.drawable.fried_egg, R.drawable.lumpia, R.drawable.pancit,
                R.drawable.sisig, R.drawable.longanisa, R.drawable.tocino, R.drawable.adobo, R.drawable.halo};

        String[] itemNames = context.getResources().getStringArray(R.array.menu_items_list);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(NUM_ITEMS, itemNames.length);
        editor.apply();

        String[] itemPrice = context.getResources().getStringArray(R.array.menu_items_price);

        String[] itemDescription = context.getResources().getStringArray(R.array.menu_item_descriptions);

        String[] itemHistory = context.getResources().getStringArray(R.array.menu_item_history);

        String[] itemTotal = context.getResources().getStringArray(R.array.menu_items_price);

        for (int i = 0; i < itemNames.length; i++) {
            //Create ContentValues (values of 1 row)
            ContentValues values = new ContentValues();
            values.put(MenuEntry.COlUMN_ITEM_NAME, itemNames[i]);
            values.put(MenuEntry.COLUMN_ITEM_PRICE, itemPrice[i]);
            values.put(MenuEntry.COLUMN_ITEM_DESCRIPTION, itemDescription[i]);
            values.put(MenuEntry.COLUMN_ITEM_HISTORY, itemHistory[i]);
            values.put(MenuEntry.COLUMN_ITEM_RESOURCE, imageResources[i]);
            values.put(MenuEntry.COLUMN_ITEM_TOTAL, itemTotal[i]);

            //responseUri not used at this time
            Uri responseUri = context.getContentResolver().insert(MenuEntry.CONTENT_URI, values);
        }
    }

    //Currently not used
    private void updateDatabase(Context context) {
        int[] imageResources = new int[] {R.drawable.white_rice,
                R.drawable.garlic_rice, R.drawable.fried_egg, R.drawable.lumpia, R.drawable.pancit,
                R.drawable.sisig, R.drawable.longanisa, R.drawable.tocino, R.drawable.adobo, R.drawable.halo};

        String[] itemNames = context.getResources().getStringArray(R.array.menu_items_list);

        String[] itemPrice = context.getResources().getStringArray(R.array.menu_items_price);

        String[] itemDescription = context.getResources().getStringArray(R.array.menu_item_descriptions);

        String[] itemHistory = context.getResources().getStringArray(R.array.menu_item_history);

        for (int i = 0; i < itemNames.length; i++) {
            //Create ContentValues (values of 1 row)
            ContentValues values = new ContentValues();
            values.put(MenuEntry.COlUMN_ITEM_NAME, itemNames[i]);
            values.put(MenuEntry.COLUMN_ITEM_PRICE, itemPrice[i]);
            values.put(MenuEntry.COLUMN_ITEM_DESCRIPTION, itemDescription[i]);
            values.put(MenuEntry.COLUMN_ITEM_HISTORY, itemHistory[i]);
            values.put(MenuEntry.COLUMN_ITEM_RESOURCE, imageResources[i]);

            Uri currentUri = ContentUris.withAppendedId(MenuEntry.CONTENT_URI, i);

            //responseUri not used at this time
            int numRowsUpdated = context.getContentResolver().update(currentUri, values, null, null);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean videoRestored) {
        
        if (!videoRestored) {
            youTubePlayer.loadVideo(INTRO_VIDEO);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, R.string.video_failed_to_initialize, Toast.LENGTH_SHORT).show();
    }

}
