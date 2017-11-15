package com.riskitbiskit.lumpiashmompianow.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.riskitbiskit.lumpiashmompianow.R;
import com.riskitbiskit.lumpiashmompianow.activities.AboutActivity;
import com.riskitbiskit.lumpiashmompianow.activities.MenuActivity;
import com.riskitbiskit.lumpiashmompianow.activities.OrderActivity;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract;

import static com.riskitbiskit.lumpiashmompianow.activities.MenuActivity.CHECKOUT_LIST;
import static com.riskitbiskit.lumpiashmompianow.activities.MenuActivity.EMPTY;
import static com.riskitbiskit.lumpiashmompianow.data.MenuContract.MenuEntry.CONTENT_URI;

public class AdapterClickListener implements AdapterView.OnItemClickListener {

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public AdapterClickListener (Context context, SharedPreferences sharedPreferences) {
        mContext = context;
        mSharedPreferences = sharedPreferences;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        SharedPreferences.Editor mEditor = mSharedPreferences.edit();

        switch (position) {
            case 0:
                if (mSharedPreferences.getString(OrderActivity.PREVIOUS_ORDER, MenuActivity.EMPTY).contentEquals(MenuActivity.EMPTY)) {
                    Toast.makeText(mContext, R.string.no_previous_order, Toast.LENGTH_SHORT).show();
                } else {
                    //Update shared preference with old order
                    String previousOrder = (mSharedPreferences.getString(OrderActivity.PREVIOUS_ORDER, MenuActivity.EMPTY));
                    mEditor.putString(MenuActivity.CHECKOUT_LIST, previousOrder);
                    mEditor.apply();
                    Intent reorderIntent = new Intent(mContext, OrderActivity.class);
                    mContext.startActivity(reorderIntent);
                }
                return;
            case 1:
                //open menu activity
                Intent menuIntent = new Intent(mContext, MenuActivity.class);
                mContext.startActivity(menuIntent);
                ((Activity) mContext).finish();
                return;
            case 2:
                //check to see if anything is in basket
                if (mSharedPreferences.getString(CHECKOUT_LIST, EMPTY).contentEquals(EMPTY)) {
                    //if nothing in basket, remind user
                    Toast.makeText(mContext, R.string.nothing_in_cart, Toast.LENGTH_SHORT).show();
                } else {
                    //if there is, open order activity
                    Intent checkoutIntent = new Intent(mContext, OrderActivity.class);
                    mContext.startActivity(checkoutIntent);
                }
                return;
            case 3:
                //selete all items in checkout basket
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(CHECKOUT_LIST, EMPTY);
                editor.apply();

                //Reset all item totals in database
                ContentValues contentValues = new ContentValues();
                contentValues.put(MenuContract.MenuEntry.COLUMN_ITEM_COUNT, mContext.getString(R.string.one));
                mContext.getContentResolver().update(CONTENT_URI, contentValues, null, null);

                //TODO: chore: stay in activity, show toast of basket being cleared
                //Restart activity
                Intent clearCartIntent = new Intent(mContext, MenuActivity.class);
                mContext.startActivity(clearCartIntent);
                ((Activity) mContext).finish();
                return;
            case 4:
                Intent aboutIntent = new Intent(mContext, AboutActivity.class);
                mContext.startActivity(aboutIntent);
                ((Activity) mContext).finish();
                return;
            default:
        }
    }
}
