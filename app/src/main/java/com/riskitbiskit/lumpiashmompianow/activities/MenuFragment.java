package com.riskitbiskit.lumpiashmompianow.activities;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.riskitbiskit.lumpiashmompianow.R;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract;
import com.riskitbiskit.lumpiashmompianow.data.MenuContract.MenuEntry;
import com.riskitbiskit.lumpiashmompianow.utils.MenuCursorAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;


public class MenuFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //Constants
    public static final int MENU_LOADER = 0;

    //Views
    @BindView(R.id.menu_gv)
    GridView menuGridView;

    //Fields
    MenuCursorAdapter mMenuCursorAdapter;
    SharedPreferences mSharedPreferences;
    OnMenuItemClickListener mListener;

    //Interface, connects to Menu Activity
    public interface OnMenuItemClickListener {
        void onMenuItemClicked(long id);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);
        ButterKnife.bind(this, rootView);

        //get reference to shared preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        //setup grid
        mMenuCursorAdapter = new MenuCursorAdapter(getContext(), null);
        menuGridView.setAdapter(mMenuCursorAdapter);

        //handle food item clicks
        menuGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //check if layout is two panel
                Boolean isTwoPanel = mSharedPreferences.getBoolean(getString(R.string.is_two_panel), false);

                if (isTwoPanel) {
                    //if tablet layout, pass the item id to menu activity
                    mListener.onMenuItemClicked(id);

                } else {
                    //if phone layout, open details activity and present user selected item
                    Intent intent = new Intent(getContext(), DetailActivity.class);
                    //pass in menu uri to details activity
                    Uri currentMenuUri = ContentUris.withAppendedId(MenuEntry.CONTENT_URI, id);
                    intent.setData(currentMenuUri);
                    //start activity
                    startActivityForResult(intent, 1);
                }
            }
        });

        //load all items in database and display to user
        getLoaderManager().initLoader(MENU_LOADER, null, this);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //check if activity has implemented the listener
        try {
            mListener = (OnMenuItemClickListener) context;
        } catch (ClassCastException CCE) {
            throw new ClassCastException(context.toString() + getString(R.string.must_implement_menu_listener));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //define projection (column values you want returned)
        String[] projection = {
                MenuEntry._ID,
                MenuEntry.COlUMN_ITEM_NAME,
                MenuEntry.COLUMN_ITEM_PRICE,
                MenuEntry.COLUMN_ITEM_RESOURCE
        };

        //loader that will query Content Provider, return to onLoadFinished
        return new CursorLoader(getContext(),
                MenuEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //swap out the blank place holder with returned data
        mMenuCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMenuCursorAdapter.swapCursor(null);
    }

    //refreshes page so that shopping icon updates correctly
    //TODO: refactor - there may be a better way to do this, check for future date
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Intent refresh = new Intent(getContext(), MenuActivity.class);
            startActivity(refresh);
            getActivity().finish();
        }
    }
}