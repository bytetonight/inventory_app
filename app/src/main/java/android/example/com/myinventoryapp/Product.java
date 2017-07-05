package android.example.com.myinventoryapp;

import android.database.Cursor;
import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.example.com.myinventoryapp.data.ProductContract.ProductEntry;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Created by ByteTonight on 03.07.2017.
 */

public class Product {

    private int id;
    private String name;
    private double price;
    private String image;
    private String supplierName;
    private String supplierMail;
    private int quantity = 1;



    public Product() {

    }

    public static Product fromCursor(Cursor cursor) {
        Product product = new Product();
        product.id = Integer.parseInt( cursor.getString(cursor.getColumnIndex(ProductEntry._ID)) );
        product.name = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        product.price =  Double.parseDouble( cursor.getString(cursor.getColumnIndex(
                ProductEntry.COLUMN_PRODUCT_PRICE)) );
        product.image =  cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE));
        product.supplierName =  cursor.getString(cursor.getColumnIndex(
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME));
        product.supplierMail =  cursor.getString(cursor.getColumnIndex(
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL));

        String tempQuantity = cursor.getString(cursor.getColumnIndex(
                ProductEntry.COLUMN_PRODUCT_QUANTITY));
        if (!tempQuantity.isEmpty())
            product.quantity =  Integer.parseInt(tempQuantity) ;
        else
            product.quantity = 0;
        return product;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return String.valueOf(price);
    }

    public String getLocalePrice() {
        if (price == 0)
            return ""; //This will prevent the App from assuming a new product is an existing one
        return NumberFormat.getCurrencyInstance(Config.USER_LOCALE).format(price);
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierMail() {
        return supplierMail;
    }

    public void setSupplierMail(String supplierMail) {
        this.supplierMail = supplierMail;
    }

    public String getQuantity() {
        return String.valueOf(quantity);
    }

    public void setQuantity(String quantity) {
        if (!quantity.isEmpty())
            this.quantity = Integer.parseInt(quantity);
    }

    /**
     * How this works ?
     * I've added the attribute app:imageUrl="@{poi.previewImageResourceId}" to the ImageView.
     * For the app namespace to work with my imageUrl attribute, I had to declare the namespace
     * in the layout as follows: xmlns:app="http://schemas.android.com/apk/res-auto"
     * THIS IS A MUST
     * Then from the method getProductImage, the annotation bind:imageUrl binds a call to the
     * attribute [ app:imageUrl ] to getProductImage which HAS TO BE static.
     * The value of the XML attribute, as well as the View the attribute is part of, is passed to
     * getProductImage
     *
     * @param imageView
     * @param previewImgResourceId
     */
    @BindingAdapter("bind:imageUrl")
    public static void getProductImage(ImageView imageView, String previewImgResourceId) {
        Glide.with(imageView.getContext()).load(previewImgResourceId).into(imageView);
    }

}
