package com.momobites.www.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.momobites.www.inventory.Data.InventoryContract;
import com.momobites.www.inventory.Data.InventoryContract.Inventories;
import com.momobites.www.inventory.Data.InventoryDBHelper;

import java.util.Random;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.momobites.www.inventory.Data.InventoryContract.Inventories.TABLE_NAME;
import static com.momobites.www.inventory.Data.InventoryDBHelper.DATABASE_NAME;
import static com.momobites.www.inventory.Editor.REQUEST_IMAGE_CAPTURE;
import static com.momobites.www.inventory.R.id.camera;
//import static com.momobites.www.inventory.R.id.supplier;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    // Data Loader Identifier
    private static final int INVENTORY_LOADER = 0;

    /** Adapter for the ListView */
    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Creating and Setting up a list view
        // Find the ListView
        final ListView inventoryListView = (ListView) findViewById(R.id.list_view);

        // Set empty ListView
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        // Adapter Setup
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);

        // Clickable Adapter Setup
        // Setting up a clickable adapter (click listener on each list item)
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, Editor.class);
                // Form the content URI that represents the specific item that was clicked on,
                // by appending the "id" (passed as input to this method) onto the CONTENT_URI
                Uri currentInventoryUri = ContentUris.withAppendedId(Inventories.CONTENT_URI, id);
                // Set the URI on the data field of the intent
                intent.setData(currentInventoryUri);
                // Launch Activity
                startActivity(intent);
            }
        });

        // Initializing Loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    //Helper method to Insert Inventory
    private void insertInventory() {

        // Generating Random Dummy Data
        Random rand = new Random();
        int quantity = rand.nextInt(2) + 10;
        int price = rand.nextInt(400) + 10;
        int sales = quantity + 1;
        String[] strings = {"Toothpase", "Toothbrush", "Soap", "Shampoo", "Towel", "Mouth Wash", "Shaving Kit", "Conditioner",
                "Heated Cushion", "Pen", "Pencil", "Paper"};
        String name = strings[(int) (Math.random() * strings.length)];
        String[] supplier_pool = {"Atlantic Holdings", "Wayne Enterprise", "Acme", "Lex Corps", "Starship Enterprise",
                "Rebel Alliance", "Bajirao Mastane", "Brown Suppliers"};
        String supplier = supplier_pool[(int) (Math.random() * supplier_pool.length)];

        // Create a ContentValues object where column names are the keys,
        // and one single inventory attributes are the values.
        ContentValues values = new ContentValues();
        values.put(Inventories.COLUMN_NAME, name);
        values.put(Inventories.COLUMN_PRICE, price);
        values.put(Inventories.COLUMN_QUANTITY, quantity);
        values.put(Inventories.COLUMN_SUPPLIER, supplier);
        values.put(Inventories.COLUMN_PICTURE, 2);
        values.put(Inventories.COLUMN_SALES, sales);



        // Insert a new row for First Inventory in the database, returning the ID of that new row.
        // OLD METHOD db.insert(TABLE_NAME, null, values);
        Uri newUri = getContentResolver().insert(Inventories.CONTENT_URI, values);
    }

    //Helper method to Delete Inventory Table as a whole
    private void deleteAll() {
        int rowsDeleted = getContentResolver().delete(Inventories.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_new_data:
                Intent intent = new Intent(MainActivity.this, Editor.class);
                startActivity(intent);
                return true;

            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertInventory();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        //Define projections
        String[] projection = {
                Inventories._ID,
                Inventories.COLUMN_NAME,
                Inventories.COLUMN_PRICE,
                Inventories.COLUMN_QUANTITY,
                Inventories.COLUMN_SUPPLIER,
                Inventories.COLUMN_PICTURE,
                Inventories.COLUMN_SALES,};


        // Method using Content Provider
        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                Inventories.CONTENT_URI,        // The content URI
                projection,                     // The colums to return for each row
                null,                           // Selection criteria
                null,                        // Selection Args criteria
                null                    // The sort order for returned rows
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update with this new cursor containing updated data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
