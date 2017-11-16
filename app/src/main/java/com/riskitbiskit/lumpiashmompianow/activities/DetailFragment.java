package com.riskitbiskit.lumpiashmompianow.activities;

import android.content.ContentUris;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riskitbiskit.lumpiashmompianow.R;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract.MenuEntry;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //TODO: Refactor - add this fragment to detail activity, whiddle down excess code

    //Constants
    public static final int DETAIL_FRAG_LOADER = 5;

    //Private Variables
    private SharedPreferences mSharedPreferences;
    private Uri requestedItemUri;
    ArrayList<String> mCartList;
    String mItemName;
    String mItemPrice;
    String mItemDescrip;
    String mItemHist;
    int mItemImgRes;

    @BindView(R.id.frag_food_description_tv)
    TextView descriptionTV;
    @BindView(R.id.frag_food_price_tv)
    TextView priceTV;
    @BindView(R.id.frag_food_history_tv)
    TextView historyTV;
    @BindView(R.id.frag_detail_border)
    LinearLayout borderLayout;
    @BindView(R.id.frag_detail_fab)
    FloatingActionButton detailFab;

    //setter method
    public void setRequestedItemUri (long id) {
        requestedItemUri = ContentUris.withAppendedId(MenuEntry.CONTENT_URI, id);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        //bind views
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //initialize shared preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        //kick-off call to database
        getActivity().getSupportLoaderManager().restartLoader(DETAIL_FRAG_LOADER, null, this);

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

        return new CursorLoader(getContext(),
                requestedItemUri,
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

            //Set data to relevant views
            descriptionTV.setText(mItemDescrip);
            priceTV.setText("$" + mItemPrice);
            historyTV.setText(mItemHist);

            //Update the border color
            updateBorderColor(mItemImgRes);

            //Setup FAB
            detailFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSharedPreferences.getString(MenuActivity.CHECKOUT_LIST, MenuActivity.EMPTY).contentEquals(MenuActivity.EMPTY)) {
                        //if no list, create a list
                        createNewCartList();
                    } else {
                        //get old list first
                        addItemToCartList();
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //do nothing
    }

    private void createNewCartList() {
        //create new cart list
        mCartList = new ArrayList<>();
        mCartList.add(mItemName);

        //convert list to String via Gson
        Gson gson = new Gson();
        String json = gson.toJson(mCartList);

        //add to shared preference
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(MenuActivity.CHECKOUT_LIST, json);
        editor.apply();

        //redraw options menu
        getActivity().invalidateOptionsMenu();

        //let user know that it was added to cart
        Toast.makeText(getContext(), R.string.added_to_cart, Toast.LENGTH_SHORT).show();
    }

    private void addItemToCartList() {
        //get old list first
        String json = mSharedPreferences.getString(MenuActivity.CHECKOUT_LIST, MenuActivity.EMPTY);

        //convert list from string to array list
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        mCartList = gson.fromJson(json, type);

        //check if item is already on the list
        boolean isCopy = false;
        for (int i = 0; i < mCartList.size(); i++) {
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
            Toast.makeText(getContext(), R.string.already_in_cart, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), R.string.added_to_cart, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBorderColor(int itemImgRes) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), itemImgRes);

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                if (palette.getMutedSwatch() != null) {
                    borderLayout.setBackgroundColor(palette.getMutedSwatch().getRgb());
                    detailFab.setBackgroundTintList(ColorStateList.valueOf(palette.getMutedSwatch().getRgb()));
                }
            }
        });
    }

}
