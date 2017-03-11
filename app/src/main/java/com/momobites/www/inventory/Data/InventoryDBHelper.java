package com.momobites.www.inventory.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.momobites.www.inventory.Data.InventoryContract.Inventories;

/**
 * Created by prash on 12/30/2016.
 */

public class InventoryDBHelper extends SQLiteOpenHelper {


    //FIRST STEP
    // Creating database name and version
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";

    //SECOND STEP
    // Create a constructor
    public InventoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //STEP THREE
    // Creating onCreate method which helps create SQLite command to create a table
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the INVENTORY table
        String SQL_CREATE_INVENTORIES_TABLE =
                "CREATE TABLE " + Inventories.TABLE_NAME + " ("
                        + Inventories._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + Inventories.COLUMN_NAME + " TEXT, "
                        + Inventories.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                        + Inventories.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                        + Inventories.COLUMN_SUPPLIER + " TEXT, "
                        + Inventories.COLUMN_PICTURE + " TEXT, "
                        + Inventories.COLUMN_SALES + " INTEGER NOT NULL DEFAULT 0 ) ; " ;

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORIES_TABLE);
    }

    //STEP FOUR
    // Creates table update command
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
