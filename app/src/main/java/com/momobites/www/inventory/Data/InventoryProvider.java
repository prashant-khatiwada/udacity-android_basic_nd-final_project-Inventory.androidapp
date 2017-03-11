package com.momobites.www.inventory.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.momobites.www.inventory.Data.InventoryContract.Inventories;

import android.support.annotation.Nullable;
import android.util.Log;

import static android.R.attr.name;

/**
 * Created by prash on 12/31/2016.
 */

public class InventoryProvider extends ContentProvider {

    // Database helper object
    private InventoryDBHelper mDBHelper;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    // Creating a URIMatcher
    private static final int INVENTORY = 100;
    private static final int INVENTORY_ID = 101;
    // No Match Clause
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // For the whole table
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORIES, INVENTORY);
        // for a single row
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORIES + "/#", INVENTORY_ID);
    }


    // Create and Initialize DBHelper object and gain access to the local database
    // Variable created is a global variable
    @Override
    public boolean onCreate() {
        // creating and initializing dbhelper object
        mDBHelper = new InventoryDBHelper(getContext());
        return true;
    }


    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDBHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // For the INVENTORY code.
                cursor = database.query(Inventories.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case INVENTORY_ID:
                // For INVENTORY_ID code
                selection = Inventories._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the  table where the _id equals # to return a
                // Cursor containing that row of the table.
                cursor = database.query(Inventories.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertInventory(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // Extension to Uri Insert method
    public Uri insertInventory(Uri uri, ContentValues values) {
        // Check that the Inventory Name is not null
        String name = values.getAsString(Inventories.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Inventory requires a name");
        }

        // Check that the Inventory Price is not null
        Integer price = values.getAsInteger(Inventories.COLUMN_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Inventory requires a price");
        }

        // Check that the Inventory Supplier is not null
        String supplier = values.getAsString(Inventories.COLUMN_SUPPLIER);
        if (supplier == null) {
            throw new IllegalArgumentException("Inventory requires Supplier Name");
        }

        // Check that the Inventory Quantity is not null
        Integer quantity = values.getAsInteger(Inventories.COLUMN_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Inventory requires Quantity number");
        }


        // No need to check the picture, sales.

        // Get  database in writing mode
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(Inventories.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return Inventories.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return Inventories.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(Inventories.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                // Delete a single row given by the ID in the URI
                selection = Inventories._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(Inventories.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, values, selection, selectionArgs);
            case INVENTORY_ID:
                selection = Inventories._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the COLUMN_NAME key is present
        if (values.containsKey(Inventories.COLUMN_NAME)) {
            // Check that the Inventory Name is not null
            String name = values.getAsString(Inventories.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Inventory requires a name");
            }
        }

        // If the COLUMN_PRICE key is present
        if (values.containsKey(Inventories.COLUMN_PRICE)) {
            // Check that the Inventory Price is not null
            Integer price = values.getAsInteger(Inventories.COLUMN_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Inventory requires a price");
            }
        }

        // If the COLUMN_SUPPLIER key is present
        if (values.containsKey(Inventories.COLUMN_SUPPLIER)) {
            // Check that the Inventory Supplier is not null
            String supplier = values.getAsString(Inventories.COLUMN_SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Inventory requires Supplier Name");
            }
        }

        // If the COLUMN_QUANTITY key is present
        if (values.containsKey(Inventories.COLUMN_QUANTITY)) {
            // Check that the Inventory Quantity is not null
            Integer quantity = values.getAsInteger(Inventories.COLUMN_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("Inventory requires Quantity number");
            }
        }

        // No need to check the Inventory Sales and Shipment, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(Inventories.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

}
