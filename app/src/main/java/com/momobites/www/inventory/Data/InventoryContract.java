package com.momobites.www.inventory.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by prashant on 12/30/2016.
 */

public final class InventoryContract {


    //creating a blank constructor
    private InventoryContract(){
        throw new AssertionError("No InventoryContract instances for you!");
    }

    // Creating URI
    // Creating a string constant whose value is the same as that from the AndroidManifest
    public static final String CONTENT_AUTHORITY = "com.momobites.www.inventory";
    //Creating a Base Uri (i.e adding content://)
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Possible path for table name
    public static final String PATH_INVENTORIES = "Habits";


    //Creating a table
    public static final class Inventories implements BaseColumns {

        /** The content URI to access the inventory data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORIES);

        // Creting MIME Types
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inventories
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORIES;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single inventory.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORIES;


        //Creating a table name representation
        public static final String TABLE_NAME = "Habits";

        //Creating a column representation - - DATA TYPE OF THE CONSTANTS
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER = "supplier";
        public static final String COLUMN_PICTURE = "picture";
        public static final String COLUMN_SALES = "sales";


    }

}
