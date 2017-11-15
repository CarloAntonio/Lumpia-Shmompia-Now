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

    //Constants
    public static final int DETAIL_FRAG_LOADER = 5;

    //Private Variables
    private SharedPreferences mSharedPreferences;
    private Uri requestedItemUri;

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

    //Setter Methods
    public void setRequestedItemUri (long id) {
        requestedItemUri = ContentUris.withAppendedId(MenuEntry.CONTENT_URI, id);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initialize sharedPreference
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

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

        CursorLoader cursorLoader = new CursorLoader(getContext(),
                requestedItemUri,
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
            descriptionTV.setText(itemDesc);
            priceTV.setText("$" + itemPrice);
            historyTV.setText(itemHist);

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

                        //Redraw options menu
                        getActivity().invalidateOptionsMenu();

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

                        //Check if it is a copy
                        if (isCopy) {
                            Toast.makeText(getContext(), getContext().getString(R.string.already_in_cart), Toast.LENGTH_SHORT).show();
                        } else {
                            //Add to list
                            cartList.add(itemName);
                            String newJson = gson.toJson(cartList);

                            //Add updated list to shared preference
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString(MenuActivity.CHECKOUT_LIST, newJson);
                            editor.apply();

                            Toast.makeText(getContext(), getContext().getString(R.string.added_to_cart), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
