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

import static com.riskitbiskit.lumpiashmompianow.data.MenuContract.*;

public class OrderCursorAdapter extends CursorAdapter{
    public static final int QUANTITY_LOADER = 2;

    private OnQuantityChangedListener mListener;

    private Activity mActivity;

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
        //Find item views
        ImageView imageView = view.findViewById(R.id.checkout_img);
        TextView nameTV = view.findViewById(R.id.order_item_name);
        TextView itemTotalTV = view.findViewById(R.id.item_total_tv);
        Button minusButton = view.findViewById(R.id.minus_button);
        final Button plusButton = view.findViewById(R.id.plus_button);
        final TextView orderQuant = view.findViewById(R.id.order_quant);

        //Get values from cursor
        int imageResID = cursor.getInt(cursor.getColumnIndex(MenuEntry.COLUMN_ITEM_RESOURCE));
        String itemName = cursor.getString(cursor.getColumnIndex(MenuEntry.COlUMN_ITEM_NAME));
        String itemPrice = cursor.getString(cursor.getColumnIndex(MenuEntry.COLUMN_ITEM_PRICE));
        final int itemCount = cursor.getInt(cursor.getColumnIndex(MenuEntry.COLUMN_ITEM_COUNT));
        String itemTotal = cursor.getString(cursor.getColumnIndex(MenuEntry.COLUMN_ITEM_TOTAL));

        //Create Usable Uri
        final int itemID = cursor.getInt(cursor.getColumnIndex(MenuEntry._ID));
        final Uri itemUri = ContentUris.withAppendedId(MenuEntry.CONTENT_URI, itemID);

        //Price as an int
        final double price = Double.parseDouble(itemPrice);
        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        //Toal as an int
//        final double itemTotalAsDouble = Double.parseDouble(itemTotal);

        //Item total
        double itemTotalAsDouble = price * itemCount;

        //Set relevant views
        Glide.with(context).load(imageResID).into(imageView);
        nameTV.setText(itemName);
        orderQuant.setText(String.valueOf(itemCount));
        itemTotalTV.setText("$" + String.valueOf(decimalFormat.format(itemTotalAsDouble)));

        //Setup add quantity updates
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //updates count column in database

                if (itemCount < 20) {
                    //calculate new values
                    int newCount = itemCount + 1;
                    double newTotal = newCount * price;

                    //Update database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MenuEntry.COLUMN_ITEM_COUNT, newCount);
                    contentValues.put(MenuEntry.COLUMN_ITEM_TOTAL, newTotal);
                    mActivity.getContentResolver().update(itemUri, contentValues, null, null);

                    //update listview to reflect new database
                    mListener.onQuantityChanged();
                }
            }
        });

        //Setup minus quantity updates
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (itemCount > 1) {
                    //calculate new values
                    int newCount = itemCount - 1;
                    double newTotal = newCount * price;

                    //Update database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MenuEntry.COLUMN_ITEM_COUNT, newCount);
                    contentValues.put(MenuEntry.COLUMN_ITEM_TOTAL, newTotal);
                    mActivity.getContentResolver().update(itemUri, contentValues, null, null);

                    //update listview to reflect new database
                    mListener.onQuantityChanged();
                }
            }
        });
    }

    public interface OnQuantityChangedListener {
        void onQuantityChanged();
    }
}
