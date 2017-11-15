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
    SharedPreferences mSharedPreferences;
    int mPreviousItemCount;

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

        //Get reference of Shared Preference
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Updates database if number of items are changed from previous times.
        //Caveat is if changes are made but same number of items remains the same.
        checkForNewMenuItems();

        //check if there was a previous order
        if (mSharedPreferences.getString(OrderActivity.PREVIOUS_ORDER, MenuActivity.EMPTY).contentEquals(MenuActivity.EMPTY)) {
            //if not, remove option to reorder
            reoderFrameLayout.setVisibility(View.GONE);
        } else {
            //if so, show option to reorder
            reoderFrameLayout.setVisibility(View.VISIBLE);
        }

        //initialize youtube player
        mTubePlayerView.initialize(API_KEY, this);

        //upload image to relevant views
        Glide.with(this).load(R.drawable.menu).into(menuContainerImageView);
        Glide.with(this).load(R.drawable.restaurant).into(restaurantContainerImageView);
        Glide.with(this).load(R.drawable.reorder).into(reorderContainerImageView);

        reorderContainerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Update shared preference with old order
                String previousOrder = mSharedPreferences.getString(OrderActivity.PREVIOUS_ORDER, MenuActivity.EMPTY);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
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

        //TODO: refactor- use Dagger in future updates
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
    }

    private void checkForNewMenuItems() {

        //Check what the item count was previously
        mPreviousItemCount = mSharedPreferences.getInt(NUM_ITEMS,0);

        //Check the current item count
        String[] itemNames = getResources().getStringArray(R.array.menu_items_list);

        //Compare item count
        if (mPreviousItemCount != itemNames.length) {
            deleteDatabase();
            createDatabase();
        }
    }

    //Delete entire table
    public void deleteDatabase() {
        //rowsDeleted not used at this time
        getContentResolver().delete(MenuEntry.CONTENT_URI, null, null);
    }

    //Create new table
    public void createDatabase() {

        //Create array of images
        int[] imageResources = new int[] {
            R.drawable.white_rice, R.drawable.garlic_rice, R.drawable.fried_egg,
            R.drawable.lumpia, R.drawable.pancit, R.drawable.sisig, R.drawable.longanisa,
            R.drawable.tocino, R.drawable.adobo, R.drawable.halo};

        //create array of food item names
        String[] itemNames = getResources().getStringArray(R.array.menu_items_list);

        //create reference to SP editor and add new number of food items
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(NUM_ITEMS, itemNames.length);
        editor.apply();

        //create arrays for each type of detail
        String[] itemPrice = getResources().getStringArray(R.array.menu_items_price);
        String[] itemDescription = getResources().getStringArray(R.array.menu_item_descriptions);
        String[] itemHistory = getResources().getStringArray(R.array.menu_item_history);
        String[] itemTotal = getResources().getStringArray(R.array.menu_items_price);

        //go through each item and add to recently cleared database
        for (int i = 0; i < itemNames.length; i++) {
            //Create ContentValues (values of 1 row)
            ContentValues values = new ContentValues();
            values.put(MenuEntry.COlUMN_ITEM_NAME, itemNames[i]);
            values.put(MenuEntry.COLUMN_ITEM_PRICE, itemPrice[i]);
            values.put(MenuEntry.COLUMN_ITEM_DESCRIPTION, itemDescription[i]);
            values.put(MenuEntry.COLUMN_ITEM_HISTORY, itemHistory[i]);
            values.put(MenuEntry.COLUMN_ITEM_RESOURCE, imageResources[i]);
            values.put(MenuEntry.COLUMN_ITEM_TOTAL, itemTotal[i]);

            //add to food items to local database
            getContentResolver().insert(MenuEntry.CONTENT_URI, values);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean videoRestored) {
        if (!videoRestored) {
            youTubePlayer.loadVideo(INTRO_VIDEO);
        }
        //TODO: new feat - autoplay after orientation change
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, R.string.video_failed_to_initialize, Toast.LENGTH_SHORT).show();
    }

}
