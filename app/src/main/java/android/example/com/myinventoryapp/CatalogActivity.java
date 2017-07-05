package android.example.com.myinventoryapp;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.example.com.myinventoryapp.data.ProductContract.ProductEntry;
import android.widget.Toast;

import java.util.Locale;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the product data loader
     */
    private static final int LOADER_ID = 0;

    private RecyclerView recyclerView;

    /**
     * Adapter for the RecyclerView
     */
    RecyclerProductCursorAdapter recyclerProductCursorAdapter;

    /**
     * What to display if no data is available
     **/
    View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the RecyclerView which will be populated with the product data
        recyclerView = (RecyclerView) findViewById(R.id.recView);

        // Find empty view for the RecyclerView, so that it only shows when the list has 0 items.
        // Switching visibility states is handled in {@link CatalogActivity.onLoadFinished} because
        // things just work differently with RecyclerViews
        emptyView = findViewById(R.id.empty_view);
        // If the view was found, set it to GONE for now
        if (emptyView != null)
            emptyView.setVisibility(View.GONE);


        // Setup an Adapter to create a list item for each row of product data in the Cursor.
        // There is no product data yet (until the loader finishes) so pass in null for the Cursor.

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(CatalogActivity.this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerProductCursorAdapter = new RecyclerProductCursorAdapter(this, null);
        recyclerView.setAdapter(recyclerProductCursorAdapter);

        //Clicks are assigned in XML Databinding and handled in the Handlers class
        // Kick off the loader
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }



    /**
     * Helper method to insert dummy product data into the database. For debugging purposes only.
     */
    private void insertProduct() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "T-Shirt");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 5);
        String tShirtUri = "android.resource://android.example.com.myinventoryapp/drawable/t_shirt_400";
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, tShirtUri);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, "John Doe");
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, "john.doe@email.com");
        values.put(ProductEntry.COLUMN_PRODUCT_TARGET_GENDER, ProductEntry.GENDER_UNISEX);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 5);

        // Insert a new row for the product into the provider using the ContentResolver.
        // Use the {@link ProductEntry#CONTENT_URI} to indicate that we want to insert
        // into the products database table.
        // Receive the new content URI that will allow us to access product's data in the future.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
        Toast.makeText(this, newUri.toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Helper method to delete all products in the database.
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from warehouse database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_TARGET_GENDER,
                ProductEntry.COLUMN_PRODUCT_QUANTITY};


        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ProductCursorAdapter} with this new cursor containing updated product data
        recyclerProductCursorAdapter.swapCursor(data);
        if (emptyView != null) {
            emptyView.setVisibility(data.getCount() > 0 ? View.GONE : View.VISIBLE);
            recyclerView.setVisibility(data.getCount() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        recyclerProductCursorAdapter.swapCursor(null);
    }
}


