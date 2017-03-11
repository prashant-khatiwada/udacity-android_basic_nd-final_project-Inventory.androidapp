package com.momobites.www.inventory;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.momobites.www.inventory.Data.InventoryContract;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.data;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.view.View.GONE;

import static com.momobites.www.inventory.Data.InventoryProvider.LOG_TAG;
import static com.momobites.www.inventory.R.id.container_sales;
import static com.momobites.www.inventory.R.id.imageView;

import static com.momobites.www.inventory.R.id.quantity;
import static com.momobites.www.inventory.R.id.required_field;
import static com.momobites.www.inventory.R.id.sell_button;
import static com.momobites.www.inventory.R.id.takepictureButton;


public class Editor extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the data loader
    private static final int EXISTING_INVENTORY_LOADER = 0;
    // Content URI for the existing inventory (null if its new inventory)
    private Uri mCurrentInventoryUri;
    private Uri mUri;
    private Bitmap mBitmap;
    String mCurrentPhotoPath;

    // Creating EditText fields
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierEditText;
    private TextView mSalesTextView;
    private Button mCameraButton;
    private Button mGalleryButton;
    private ImageView mImageView;

    // Boolean flag that keeps track of whether the data has changed or not
    private boolean mInventoryHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mInventoryHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };

    // creating function for picture
    private boolean isGalleryPicture = false;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int MY_PERMISSIONS_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        // Sale - quantity update
        Button saleUpdate = (Button) findViewById(sell_button);
        LinearLayout categorySale = (LinearLayout) findViewById(container_sales);
        LinearLayout takePictureButton = (LinearLayout) findViewById(takepictureButton);
        TextView requiredField = (TextView) findViewById(required_field);
        // Set a click listener
        saleUpdate.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Sale is clicked on.
            @Override
            public void onClick(View view) {

                int sales;
                if (mSalesTextView.getText().toString().isEmpty()) {
                    sales = 0;
                } else {
                    sales = Integer.parseInt(mSalesTextView.getText().toString());
                }

                int quantity;
                if (mQuantityEditText.getText().toString().isEmpty()) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(mQuantityEditText.getText().toString());
                }

                if (quantity > 0) {
                    sales = sales + 1;
                    quantity = quantity - 1;
                    mSalesTextView.setText(String.valueOf(sales));
                    mQuantityEditText.setText(String.valueOf(quantity));

                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.Inventories.COLUMN_QUANTITY, quantity);
                    values.put(InventoryContract.Inventories.COLUMN_SALES, sales);
                    int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);

                    if (rowsAffected == 0) {
                        Toast.makeText(getApplicationContext(), "Error Updating Product", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Order Item", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();

        // If the intent DOES NOT contain a pet content URI, then we know that we are
        // creating a new inventory.
        if (mCurrentInventoryUri == null) {
            // do not show the Sale Update category
            categorySale.setVisibility(GONE);
            takePictureButton.setVisibility(GONE);
            // This is a new pet, so change the app bar to say "Add Inventory"
            setTitle(getString(R.string.editor_activity_title_new_inventory));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Show the sale update button
            saleUpdate.setVisibility(View.VISIBLE);
            requiredField.setVisibility(View.GONE);
            takePictureButton.setVisibility(GONE);
            // Otherwise this is an existing pet, so change app bar to say "Edit Inventory"
            setTitle(getString(R.string.editor_activity_title_edit_inventory));
            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_inventory_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_inventory_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_inventory_quantity);
        mSupplierEditText = (EditText) findViewById(R.id.edit_inventory_supplier);
        mSalesTextView = (TextView) findViewById(R.id.editor_sale_number);
        // Camera and Gallery Binding
        mCameraButton = (Button) findViewById(R.id.camera);
        mGalleryButton = (Button) findViewById(R.id.gallery);
        mImageView = (ImageView) findViewById(R.id.imageView);

        // Setup OnTouchListeners
        // To know if the item has been touched or modified by user
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);


        // New Code
        // Code for Image
        ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mImageView.setImageBitmap(Utility.getBitmapFromUri(mImageView, Editor.this, mUri));
            }
        });
        requestPermissions();
    }

    // Get user input from editor and save the inventory into database
    private void saveInventory() {
        // Read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String salesString = mSalesTextView.getText().toString().trim();

        // PHOTO related code
        String photoString;
        if (mUri != null) {
            photoString = mUri.toString();
        } else {
            photoString = "";
        }

        // Check if this is supposed to be a new inventory
        // and check if all the fields in the editor are blank
        if (mCurrentInventoryUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(salesString) &&
                TextUtils.isEmpty(supplierString))

        {
            // Since no fields were modified, we can return early without creating a new inventory.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Toast.makeText(this, "You must enter data", Toast.LENGTH_SHORT).show();
            Intent i = getIntent();
            finish();
            startActivity(i);
            return;
        }

        // if inventory fields are not empty
        // Create a ContentValues object where column names are the keys,
        // and one single inventory attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryContract.Inventories.COLUMN_NAME, nameString);
        values.put(InventoryContract.Inventories.COLUMN_SUPPLIER, supplierString);
        // values.put(InventoryContract.Inventories.COLUMN_PICTURE, sale);
        values.put(InventoryContract.Inventories.COLUMN_PICTURE, photoString);

        // For price
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(InventoryContract.Inventories.COLUMN_PRICE, price);

        // For quantity
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(InventoryContract.Inventories.COLUMN_QUANTITY, quantity);

        // For sales
        int sales = 0;
        values.put(InventoryContract.Inventories.COLUMN_SALES, sales);

        // Determine if this is a new or existing Inventory
        // by checking if mCurrentInventoryUri is null or not
        if (mCurrentInventoryUri == null) {

            if (TextUtils.isEmpty(priceString) || TextUtils.isEmpty(nameString) || TextUtils.isEmpty(quantityString)) {
                Toast.makeText(this, "Name, Price and Quantity are Required Data Type",
                        Toast.LENGTH_SHORT).show();
                Intent i = getIntent();
                finish();
                startActivity(i);
                return;

            }

            // This is a NEW inventory, so insert a new item into the provider,
            // returning the content URI
            Uri newUri = getContentResolver().insert(InventoryContract.Inventories.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING inventory,
            // so update it with content URI: mCurrentInventoryUri
            // and pass in the new ContentValues.
            // Pass in null for the selection and selection args
            int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_insert_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentInventoryUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // If the inventory hasn't changed, continue with handling back button press
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    // Prompt the user to confirm if they want to Discard
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.action_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.action_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the inventory.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    // Show Delete Confirmation Dialog
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the Inventory.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the Inventory.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    // Delete the inventory in the Database
    private void deleteItem() {
        // Only perform the delete if this is an existing inventory.
        if (mCurrentInventoryUri != null) {
            // Call the ContentResolver to delete the inventory at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentUri
            // content URI already identifies the inventory that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }

        }
        // close the activity
        finish();
    }

    // Button Linked - method to use the order button
    public void orderItemEditor(View view) {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_EMAIL, "emailaddress@emailaddress.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject: Order Up Details");
        intent.putExtra(Intent.EXTRA_TEXT, "Product Name: " + nameString +
                "\n Current Price: " + priceString +
                "\n Quantity in Stock: " + quantityString +
                "\n \n Order Quantity: ");
        // Start the new activity
        startActivity(intent);
    }


    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
            }
        } else {
            mCameraButton.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mCameraButton.setEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    // Button Linked - method to open Gallery to select Image
    public void openImageSelector(View view) {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Button Linked - method to open up a camera
    public void takePicture(View view) {
        // Create a new intent to open the takePictureIntent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mUri = FileProvider.getUriForFile(this,
                        "com.momobites.com.inventory.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }


    }

    // method to create a unique image file name
    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    // Image Import
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.i(LOG_TAG, "Received an \"Activity Result\"");
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());
                mBitmap = Utility.getBitmapFromUri(mImageView, Editor.this, mUri);
                mImageView.setImageBitmap(mBitmap);
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                isGalleryPicture = true;
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.i(LOG_TAG, "Uri: " + mUri.toString());

            mBitmap = Utility.getBitmapFromUri(mImageView, Editor.this, mUri);
            ;
            mImageView.setImageBitmap(mBitmap);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            isGalleryPicture = false;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define projections
        String[] projection = {
                InventoryContract.Inventories._ID,
                InventoryContract.Inventories.COLUMN_NAME,
                InventoryContract.Inventories.COLUMN_PRICE,
                InventoryContract.Inventories.COLUMN_QUANTITY,
                InventoryContract.Inventories.COLUMN_SUPPLIER,
                InventoryContract.Inventories.COLUMN_PICTURE,
                InventoryContract.Inventories.COLUMN_SALES,};


        // Method using Content Provider
        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentInventoryUri,        // The content URI
                projection,                     // The columns to return for each row
                null,                           // Selection criteria
                null,                        // Selection Args criteria
                null                    // The sort order for returned rows
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // if the cursor is null or there is less than 1 row in the cursor = GET OUT
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }


        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Select Columns of Inventory interested in
            // Figure out the index of each column
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.Inventories.COLUMN_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.Inventories.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.Inventories.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryContract.Inventories.COLUMN_SUPPLIER);
            int pictureColumnIndex = cursor.getColumnIndex(InventoryContract.Inventories.COLUMN_PICTURE);
            int salesColumnIndex = cursor.getColumnIndex(InventoryContract.Inventories.COLUMN_SALES);

            // Extract out the value from the Cursor
            String currentName = cursor.getString(nameColumnIndex);
            int currentPrice = cursor.getInt(priceColumnIndex);
            int currentQuantity = cursor.getInt(quantityColumnIndex);
            String currentSupplier = cursor.getString(supplierColumnIndex);
            String iSales = cursor.getString(salesColumnIndex);


            // OLD int currentShipment = cursor.getInt(pictureColumnIndex);
            String iPhoto = cursor.getString(pictureColumnIndex);
            if (!iPhoto.isEmpty()) {
                mUri = Uri.parse(iPhoto);
                mBitmap = Utility.getBitmapFromUri(mImageView, Editor.this, mUri);
                mImageView.setImageBitmap(mBitmap);
            }


            // Update the views on the screen with the value from the database
            mNameEditText.setText(currentName);
            mPriceEditText.setText(Integer.toString(currentPrice));
            mQuantityEditText.setText(Integer.toString(currentQuantity));
            mSupplierEditText.setText(currentSupplier);
            mSalesTextView.setText(iSales);


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
        mSalesTextView.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                saveInventory();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
