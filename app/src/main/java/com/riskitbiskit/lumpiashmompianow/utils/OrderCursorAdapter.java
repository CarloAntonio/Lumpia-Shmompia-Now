package com.riskitbiskit.lumpiashmompianow.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.riskitbiskit.lumpiashmompianow.R;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.riskitbiskit.lumpiashmompianow.data.MenuContract.*;

public class OrderCursorAdapter extends CursorAdapter{

    //Constants
    public static final int QUANTITY_LOADER = 2;

    //Fields
    private OnQuantityChangedListener mListener;
    private Activity mActivity;

    //Views
    @BindView(R.id.checkout_img)
    ImageView mItemImageIV;
    @BindView(R.id.order_item_name)
    TextView mItemNameTV;
    @BindView(R.id.item_total_tv)
    TextView mItemTotalTV;
    @BindView(R.id.minus_button)
    Button mMinusButton;
    @BindView(R.id.plus_button)
    Button mPlusButton;
    @BindView(R.id.order_quant)
    TextView mOrderQuantTV;

    public OrderCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mActivity = (Activity) context;

        if (context instanceof OnQuantityChangedListener) {
            mListener = (OnQuantityChangedListener) context;
        } else {
            throw new RuntimeException(context.toString() + context.getString(R.string.must_implemen_quant_listener));
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.item_checkout, viewGroup, false);
    }


    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        //bind views
        ButterKnife.bind(this, view);

        //get values from cursor
        int imageResID = cursor.getInt(cursor.getColumnIndex(MenuEntry.COLUMN_ITEM_RESOURCE));
        String itemName = cursor.getString(cursor.getColumnIndex(MenuEntry.COlUMN_ITEM_NAME));
        String itemPrice = cursor.getString(cursor.getColumnIndex(MenuEntry.COLUMN_ITEM_PRICE));
        final int itemCount = cursor.getInt(cursor.getColumnIndex(MenuEntry.COLUMN_ITEM_COUNT));
        //TODO: chore - remove unused var
        String itemTotal = cursor.getString(cursor.getColumnIndex(MenuEntry.COLUMN_ITEM_TOTAL));

        //create Usable Uri
        final int itemID = cursor.getInt(cursor.getColumnIndex(MenuEntry._ID));
        final Uri itemUri = ContentUris.withAppendedId(MenuEntry.CONTENT_URI, itemID);

        //price as a double
        final double price = Double.parseDouble(itemPrice);
        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        //item total
        double itemTotalAsDouble = price * itemCount;

        //set relevant views
        Glide.with(context).load(imageResID).into(mItemImageIV);
        mItemNameTV.setText(itemName);
        mOrderQuantTV.setText(String.valueOf(itemCount));
        mItemTotalTV.setText("$" + String.valueOf(decimalFormat.format(itemTotalAsDouble)));

        //setup add quantity updates
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //updates count column in database as long as it's not already 20
                if (itemCount < 20) {
                    //calculate new values
                    int newCount = itemCount + 1;
                    double newTotal = newCount * price;

                    //update database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MenuEntry.COLUMN_ITEM_COUNT, newCount);
                    contentValues.put(MenuEntry.COLUMN_ITEM_TOTAL, newTotal);
                    mActivity.getContentResolver().update(itemUri, contentValues, null, null);

                    //tell order activity to update it's views to reflect update to database
                    mListener.onQuantityChanged();
                }
            }
        });

        //setup minus quantity updates
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (itemCount > 1) {
                    //calculate new values
                    int newCount = itemCount - 1;
                    double newTotal = newCount * price;

                    //update database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MenuEntry.COLUMN_ITEM_COUNT, newCount);
                    contentValues.put(MenuEntry.COLUMN_ITEM_TOTAL, newTotal);
                    mActivity.getContentResolver().update(itemUri, contentValues, null, null);

                    //tell order activity to update it's views to reflect update to database
                    mListener.onQuantityChanged();
                }
            }
        });
    }

    public interface OnQuantityChangedListener {
        void onQuantityChanged();
    }
}
