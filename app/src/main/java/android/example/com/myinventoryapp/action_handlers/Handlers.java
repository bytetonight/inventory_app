/*
 * Copyright (c) 2017 bytetonight@gmail.com
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

package android.example.com.myinventoryapp.action_handlers;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.example.com.myinventoryapp.EditorActivity;
import android.example.com.myinventoryapp.data.ProductContract;
import android.example.com.myinventoryapp.models.Product;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.example.com.myinventoryapp.data.ProductContract.ProductEntry;



public class Handlers {

    public void onClickViewProductDetails(View v, Product product) {
        Context context = v.getContext();

        // Create new intent to go to {@link EditorActivity}
        Intent intent = new Intent(context, EditorActivity.class);
        int productId = product.getId();
        // Form the content URI that represents the specific product that was clicked on,
        // by appending the "id" (passed as input to this method) onto the
        // {@link ProductEntry#CONTENT_URI}.
        // For example, the URI would be "content://com.bytetonight.warehouse/products/2"
        // if the product with ID 2 was clicked on.
        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);

        // Set the URI on the data field of the intent
        intent.setData(currentProductUri);

        // Launch the {@link EditorActivity} to display the data for the current product.
        context.startActivity(intent);
    }

    public void onSaleClick(View v, Product product) {
        int productId = product.getId();
        int quantity = Integer.parseInt(product.getQuantity());

        if (quantity > 0)
            --quantity;
        else
            return;

        Context context = v.getContext();

        Uri currentProductUri = ContentUris.withAppendedId(
                ProductContract.ProductEntry.CONTENT_URI, productId);
        Log.v("Handlers", "Uri: " + currentProductUri);

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        int rowsEffected = context.getContentResolver().update(currentProductUri, values, null, null);
    }

    public void changeQuantity(Product product, boolean up) {
        if (up)
            product.incQuantity();
        else
            product.decQuantity();
    }

    public void productImageAction(View v) {
        //The workaround I am using here comes from
        //https://stackoverflow.com/questions/37196284/android-support-library-23-4-0-android-support-v7-widget-tintcontextwrapper-can
        EditorActivity editorActivity = (EditorActivity) getRequiredActivity(v);
        editorActivity.openImageChooser();
        //Would have been nice but
        /**
         * your Activities extend AppCompatActivity.
         * Since Support library version 23.3.0 View.getContext() returns a TintContextWrapper
         * object instead of an Activity. You can extract the Activity as described
         * here: https://stackoverflow.com/a/32973351/6009935
         */
        //((EditorActivity)v.getContext()).trySelector();
    }


    private Activity getRequiredActivity(View req_view) {
        Context context = req_view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
