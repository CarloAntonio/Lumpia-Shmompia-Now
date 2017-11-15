package com.riskitbiskit.lumpiashmompianow.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riskitbiskit.lumpiashmompianow.R;
import com.riskitbiskit.lumpiashmompianow.activities.MenuActivity;
import com.riskitbiskit.lumpiashmompianow.activities.OrderActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MenuViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private SharedPreferences mSharedPreferences;
    private Context mAppContext;
    private String empty;
    private String previousOrder;
    private ArrayList<String> mItems;

    public MenuViewsFactory(Context appContext) {
        mAppContext = appContext;
    }

    @Override
    public void onCreate() {
        mItems = new ArrayList<>();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        empty = mAppContext.getString(R.string.empty);
    }

    @Override
    public void onDataSetChanged() {
        if (mSharedPreferences.getString(OrderActivity.PREVIOUS_ORDER, MenuActivity.EMPTY).contentEquals(MenuActivity.EMPTY)) {
            mItems.add(empty);
        } else {
            previousOrder = mSharedPreferences.getString(OrderActivity.PREVIOUS_ORDER, MenuActivity.EMPTY);
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            mItems = gson.fromJson(previousOrder, type);
        }
    }

    @Override
    public void onDestroy() {
        mItems.clear();
    }

    @Override
    public int getCount() {
        if (mItems.isEmpty()) {
            return 0;
        }

        return mItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //implement changes to one row/view
        if (mItems.isEmpty() || mItems.size() == 0) return null;

        String currentItem = mItems.get(position);
        //Capitalizes first letter of each item
        String capItem = currentItem.substring(0, 1).toUpperCase() + currentItem.substring(1);

        RemoteViews views = new RemoteViews(mAppContext.getPackageName(), R.layout.item_widget);

        views.setTextViewText(R.id.list_item_widget, capItem);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}