package com.riskitbiskit.lumpiashmompianow.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.riskitbiskit.lumpiashmompianow.data.MenuContract.MenuEntry;


public class MenuDBHelper extends SQLiteOpenHelper {
    //Private constructors
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "menu.db";

    //Entries
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + MenuEntry.TABLE_NAME + " (" +
            MenuEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MenuEntry.COlUMN_ITEM_NAME + " TEXT NOT NULL, " +
            MenuEntry.COLUMN_ITEM_PRICE + " TEXT NOT NULL, " +
            MenuEntry.COLUMN_ITEM_DESCRIPTION + " TEXT NOT NULL, " +
            MenuEntry.COLUMN_ITEM_HISTORY + " TEXT NOT NULL, " +
            MenuEntry.COLUMN_ITEM_RESOURCE + " INTEGER NOT NULL, " +
            MenuEntry.COLUMN_ITEM_COUNT + " INTEGER NOT NULL DEFAULT 1, " +
            //TODO: chore - remove unused column
            MenuEntry.COLUMN_ITEM_TOTAL + " TEXT NOT NULL" +
            ");";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE " + MenuEntry.TABLE_NAME;

    public MenuDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }
}
