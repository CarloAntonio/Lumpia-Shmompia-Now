package com.riskitbiskit.lumpiashmompianow.utils;

import android.content.Context;
import android.database.Cursor;
import android.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.riskitbiskit.lumpiashmompianow.R;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract.MenuEntry;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MenuCursorAdapter extends CursorAdapter {
    @BindView(R.id.food_iv)
    ImageView menuItemIV;

    @BindView(R.id.food_name_tv)
    TextView foodNameTV;

    public MenuCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.item_food, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ButterKnife.bind(this, view);

        int itemResource = cursor.getInt(cursor.getColumnIndex(MenuEntry.COLUMN_ITEM_RESOURCE));
        String itemName = cursor.getString(cursor.getColumnIndex(MenuEntry.COlUMN_ITEM_NAME));

        Glide.with(context).load(itemResource).into(menuItemIV);
        foodNameTV.setText(itemName);
    }
}
