/*
 * Copyright (c) 2017. bytetonight@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.example.com.myinventoryapp;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.example.com.myinventoryapp.action_handlers.Handlers;
import android.example.com.myinventoryapp.config.Config;
import android.example.com.myinventoryapp.data.ProductContract;
import android.example.com.myinventoryapp.databinding.ActivityEditorBinding;
import android.example.com.myinventoryapp.models.Product;
import android.example.com.myinventoryapp.models.ProductMonitor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;


public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, ProductMonitor {

    private static final int LOADER_ID = 0;
    private ActivityEditorBinding binding;
    private Product product;
    private Uri productContentUri = null;
    /**
     * I could just as well query my model
     * example if ( product.propertyChanged )
     * but for now I'll let my product tell the Activity that a change occurred in the model
     */
    private boolean productHasChanged = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale.setDefault(Config.USER_LOCALE);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_editor);
        binding.setHandlers(new Handlers());
        product = new Product(this);
        binding.setProduct(product);

        Intent intent = getIntent();
        productContentUri = intent.getData();

        if (isNewProduct()) {
            setTitle(getString(R.string.editor_title_add_product));
            binding.productImageAction.setText(getString(R.string.editor_add_image_text));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_title_edit_product));
            binding.productImageAction.setText(getString(R.string.editor_replace_image_text));
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    /**
     * Determine whether current Product is a new Record
     * or an already existing Record by evaluating the ContentUri
     *
     * @return true if ContentUri is null, otherwise false
     */
    private boolean isNewProduct() {
        return productContentUri == null;
    }

    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        if (isResolvedActivity(intent))
            startActivityForResult(Intent.createChooser(intent, getString(R.string.intent_select_image)), 0);
    }

    /**
     * This interface is the contract for receiving the results for permission requests.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    /**
     * Coming back from the image chooser
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();
                product.setImage(imageUri.toString());
                binding.productImageAction.setText(getString(R.string.editor_replace_image_text));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!productHasChanged) {
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

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        // AND the order more item
        if (productContentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
            menuItem = menu.findItem(R.id.order_more);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Insert Product
                if (persistProduct()) {
                    // Go back to CatalogActivity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Order More" menu option
            case R.id.order_more:
                requestStock();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                /**
                 * If the product hasn't changed, continue with navigating up to parent activity
                 * which is the {@link CatalogActivity}.
                 */

                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestStock() {
        Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:" + product.getSupplierMail().trim()));
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.product_request_subject));

        String msg = String.format(
                getString(R.string.product_request_message),
                product.getName().trim());
        intent.putExtra(android.content.Intent.EXTRA_TEXT, msg);

        if (isResolvedActivity(intent))
            startActivity(intent);
        else
            Toast.makeText(this, R.string.no_app_installed, Toast.LENGTH_SHORT).show();
    }

    private boolean isResolvedActivity(Intent intent) {
        return intent.resolveActivityInfo(getPackageManager(), 0) != null;
    }

    private BigDecimal currencyToBigDecimal(String currency)
    {
        // Create a DecimalFormat that fits your requirements
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Config.USER_LOCALE);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        String pattern = "#,##0.0#";
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        decimalFormat.setParseBigDecimal(true);

        // parse the string

        BigDecimal bigDecimal = null;
        try {
            bigDecimal = (BigDecimal) decimalFormat.parse(currency);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return  bigDecimal;
    }

    private boolean persistProduct() {

        /**
         * Passing !isValidFormData() means the input was Ok
         */
        if (!isValidFormData())
            return false;


        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, product.getName());
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, product.getQuantity());
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_TARGET_GENDER, 0);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, product.getSupplierName());
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, product.getSupplierMail());
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, product.getImage());




        // Determine whether we need to call
        // getContentResolver().insert or getContentResolver().update

        if (isNewProduct()) {
            // This is a NEW product, so INSERT a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(
                    ProductContract.ProductEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so UPDATE the product with content
            // URI: productContentUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because productContentUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(productContentUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                if (productHasChanged) {
                    Toast.makeText(this, getString(R.string.editor_update_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        return true;
    }

    /**
     * Low level sanity checks and notifications when a field does not meet the requirements
     * @return true if all fields meet requirements, else false
     */
    private boolean isValidFormData() {
        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (isNewProduct() &&
                TextUtils.isEmpty(product.getName()) && product.getPrice() == 0 &&
                TextUtils.isEmpty(product.getSupplierName())
                && TextUtils.isEmpty(product.getSupplierMail()) &&
                TextUtils.isEmpty(product.getImage())) {
            // Since no fields were modified, we can return early without creating a new product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            finish();
            return false;
        }

        if (TextUtils.isEmpty(product.getName())) {
            Toast.makeText(this,
                    String.format(
                            getString(R.string.field_is_mandatory),
                            getString(R.string.label_product_name)
                    ), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (product.getPrice() == 0) {
            Toast.makeText(this,
                    String.format(
                            getString(R.string.field_is_mandatory),
                            getString(R.string.label_product_price)
                    ), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(product.getSupplierName())) {
            Toast.makeText(this,
                    String.format(
                            getString(R.string.field_is_mandatory),
                            getString(R.string.label_supplier_name)
                    ), Toast.LENGTH_SHORT).show();
            return false;
        }



        if (TextUtils.isEmpty(product.getSupplierMail())) {
            Toast.makeText(this,
                    String.format(
                            getString(R.string.field_is_mandatory),
                            getString(R.string.label_supplier_email)
                    ), Toast.LENGTH_SHORT).show();
            return false;
        }



        if (TextUtils.isEmpty(product.getImage())) {
            Toast.makeText(this,
                    String.format(
                            getString(R.string.field_is_mandatory),
                            getString(R.string.label_product_image)
                    ), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (productContentUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the productContentUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(productContentUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL};

        return new CursorLoader(this,
                productContentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            // Moved to Product Model
            product = Product.fromCursor(this, cursor);
            binding.setProduct(product);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    @Override
    public void onPropertyChanged(String property) {
        productHasChanged = true;
        //Toast.makeText(this, property+" changed", Toast.LENGTH_SHORT).show();
    }
}