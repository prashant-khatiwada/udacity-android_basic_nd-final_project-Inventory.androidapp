package com.momobites.www.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.momobites.www.inventory.Data.InventoryContract;

/**
 * Created by prashant on 1/4/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {
    // Creating a Constructor
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }

    // Making a new blank view item
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        View view =  LayoutInflater.from(context).inflate(R.layout.list_items, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        final TextView salesTextView = (TextView) view.findViewById(R.id.sell_number);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        Button saleButton = (Button) view.findViewById(R.id.list_sale_button);

        // Find the columns of pet attributes that we're interested in
        final String idColumn = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.Inventories._ID));
        final int nameColumnIndex = cursor.getColumnIndex(InventoryContract.Inventories.COLUMN_NAME);
        int salesColumnIndex = cursor.getColumnIndex(InventoryContract.Inventories.COLUMN_SALES);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.Inventories.COLUMN_QUANTITY);
        final int priceColumnIndex = cursor.getColumnIndex(InventoryContract.Inventories.COLUMN_PRICE);

        // Read the pet attributes from the Cursor for the current pet
        final String iName = cursor.getString(nameColumnIndex);
        String iQuantity = cursor.getString(quantityColumnIndex);
        final String iPrice = cursor.getString(priceColumnIndex);
        String iSales = cursor.getString(salesColumnIndex);

        Log.e("DATA", String.valueOf(nameColumnIndex));

        // Update the TextViews with the attributes for the current inventory
        nameTextView.setText(iName);
        quantityTextView.setText(iQuantity);
        priceTextView.setText(iPrice);
        salesTextView.setText(iSales);

        final Uri currentProductUri = ContentUris.withAppendedId(InventoryContract.Inventories.CONTENT_URI, Long.parseLong(idColumn));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int sales;
                if (salesTextView.getText().toString().isEmpty()) {
                    sales = 0;
                } else {
                    sales = Integer.parseInt(salesTextView.getText().toString());
                }

                int quantity;
                if (quantityTextView.getText().toString().isEmpty()) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(quantityTextView.getText().toString());
                }

                if (quantity > 0) {
                    sales = sales + 1;
                    quantity = quantity - 1;
                    salesTextView.setText(String.valueOf(sales));
                    quantityTextView.setText(String.valueOf(quantity));

                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.Inventories.COLUMN_NAME, iName);
                    values.put(InventoryContract.Inventories.COLUMN_QUANTITY, quantity);
                    values.put(InventoryContract.Inventories.COLUMN_SALES, sales);
                    values.put(InventoryContract.Inventories.COLUMN_PRICE, iPrice);
                    //values.put(InventoryContract.Inventories.COLUMN_SUPPLIER, iSupplier);

                    int rowsAffected = context.getContentResolver().update(currentProductUri, values, null, null);
                    if (rowsAffected == 0) {
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.error_updating_product), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(v.getContext(), "Order Item", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }
}
