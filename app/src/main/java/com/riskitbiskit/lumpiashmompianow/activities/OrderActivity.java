package com.riskitbiskit.lumpiashmompianow.activities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riskitbiskit.lumpiashmompianow.R;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract.MenuEntry;
import com.riskitbiskit.lumpiashmompianow.utils.OrderCursorAdapter;
import com.riskitbiskit.lumpiashmompianow.widget.ReorderWidgetProvider;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        OrderCursorAdapter.OnQuantityChangedListener{

    //Constants
    public static final int ORDER_LOADER = 2;
    public static final String PREVIOUS_ORDER = "previousOrder";

    //Variables
    OrderCursorAdapter mOrderCursorAdapter;
    SharedPreferences mSharedPreferences;
    String[] emailAddress;

    @BindView(R.id.order_toolbar)
    Toolbar orderToolbar;
    @BindView(R.id.order_lv)
    ListView orderListView;
    @BindView(R.id.order_total_tv)
    TextView orderTotalTV;
    @BindView(R.id.submit_order)
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        ButterKnife.bind(this);

        emailAddress = new String[] {getString(R.string.company_email)};

        //setup toolbar
        setSupportActionBar(orderToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //get instance of shared preference
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mOrderCursorAdapter = new OrderCursorAdapter(this, null);

        orderListView.setAdapter(mOrderCursorAdapter);

        getSupportLoaderManager().initLoader(ORDER_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //get shared preference data
        String itemList = mSharedPreferences.getString(MenuActivity.CHECKOUT_LIST, MenuActivity.EMPTY);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> cartList = gson.fromJson(itemList, type);

        //define projection (what columns we want returned)
        String[] projection = {
                MenuEntry._ID,
                MenuEntry.COlUMN_ITEM_NAME,
                MenuEntry.COLUMN_ITEM_PRICE,
                MenuEntry.COLUMN_ITEM_RESOURCE,
                MenuEntry.COLUMN_ITEM_COUNT,
                MenuEntry.COLUMN_ITEM_TOTAL
        };

        //define selectionArgs (what row values we are looking for)
        //convert array list of strings to array of strings
        String[] selectionArgs = cartList.toArray(new String[cartList.size()]);

        //Define selection (which rows we want returned)
        String selection = MenuEntry.COlUMN_ITEM_NAME + " IN (";

        for (String selectionArg : selectionArgs) {
            selection += "?, ";
        }

        selection = selection.substring(0, selection.length() - 2) + ")";

        //Kick off loader
        return new CursorLoader(getBaseContext(),
                MenuEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //swap out the old data with new data
        mOrderCursorAdapter.swapCursor(data);

        //compose email
        final String emailMessage = getEmailMessage(data);

        //handle click event
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //setup intent and send
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse(getString(R.string.mailto)));
                intent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                intent.putExtra(Intent.EXTRA_TEXT, emailMessage);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }

                //Save order data
                saveOrderToForReorder();

                //Update widget
                updateWidget();

                //finish activites
                finishAffinity();

            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mOrderCursorAdapter.swapCursor(null);
    }

    private String getEmailMessage(Cursor data) {

        //starting message
        String emailMessage = "Hello, \n\n New Order \n\n ";

        //starting total
        double orderTotal = 0.00;

        //move to the first row of cursor
        data.moveToFirst();
        do {
            /*
            * Update Order total
            */
            //Get price
            String price = data.getString(data.getColumnIndex(MenuEntry.COLUMN_ITEM_PRICE));
            Double priceAsDouble = Double.parseDouble(price);

            //Get count
            int count = data.getInt(data.getColumnIndex(MenuEntry.COLUMN_ITEM_COUNT));

            //Calculate item total
            Double itemTotal = priceAsDouble * count;

            //Add to running total
            orderTotal += itemTotal;

            /*
            * Update Email Message
            */
            String itemName = data.getString(data.getColumnIndex(MenuEntry.COlUMN_ITEM_NAME));

            emailMessage = emailMessage + "Item Name: " + itemName + "\n Quantity: " + count + " \n\n";

        } while (data.moveToNext());

        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        orderTotalTV.setText("$" + String.valueOf(decimalFormat.format(orderTotal)));

        emailMessage = emailMessage + "Order Total: $" + decimalFormat.format(orderTotal)  + "\n\n\n End Order.";

        return emailMessage;
    }

    private void saveOrderToForReorder() {
        //save order data
        String currentItemList = mSharedPreferences.getString(MenuActivity.CHECKOUT_LIST, MenuActivity.EMPTY);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREVIOUS_ORDER, currentItemList);

        //Clean up current list
        editor.putString(MenuActivity.CHECKOUT_LIST, MenuActivity.EMPTY);

        //Apply changes
        editor.apply();
    }

    private void updateWidget() {
        //Used: https://stackoverflow.com/questions/4424723/android-appwidget-wont-update-from-activity, for updating widget from activity
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getBaseContext(), ReorderWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_lv);
        if (appWidgetIds.length > 0) {
            new ReorderWidgetProvider().onUpdate(getBaseContext(), appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onQuantityChanged() {
        getSupportLoaderManager().restartLoader(ORDER_LOADER, null, this);
    }
}
