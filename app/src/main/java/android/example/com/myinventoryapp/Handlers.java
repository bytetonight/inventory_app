package android.example.com.myinventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.example.com.myinventoryapp.data.ProductContract;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.example.com.myinventoryapp.data.ProductContract.ProductEntry;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import static android.R.attr.id;


/**
 * Created by ByteTonight on 05.06.2017.
 */

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


}
