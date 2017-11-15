package com.riskitbiskit.lumpiashmompianow.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MenuContract {
    //Empty constructor required
    private MenuContract(){}

    //Public Constants
    public static final String SCHEME = "content://";
    public static final String CONTENT_AUTHORITY = "com.riskitbiskit.lumpiashmompianow";
    public static final Uri BASE_CONTENT_URI = Uri.parse(SCHEME + CONTENT_AUTHORITY);
    public static final String PATH_MENU = "menu";

    public static class MenuEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MENU);

        //Table name
        public static final String TABLE_NAME = "menu";

        //Column names
        public static final String _ID = BaseColumns._ID;
        public static final String COlUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_DESCRIPTION = "description";
        public static final String COLUMN_ITEM_HISTORY = "history";
        public static final String COLUMN_ITEM_RESOURCE = "resource";
        public static final String COLUMN_ITEM_COUNT = "count";
        public static final String COLUMN_ITEM_TOTAL = "total";
    }
}
