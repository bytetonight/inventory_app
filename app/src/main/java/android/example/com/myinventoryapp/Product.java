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


public class Product {

    private int id;
    private String name;
    /**
     * the price in cents
     */
    private int price;
    private String localizedPrice;
    private String image;
    private String supplierName;
    private String supplierMail;
    private int quantity = 1;


    public Product() {

    }

    public static Product fromCursor(Cursor cursor) {
        Product product = new Product();
        product.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProductEntry._ID)));
        product.name = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        product.price = cursor.getInt(cursor.getColumnIndex(
                ProductEntry.COLUMN_PRODUCT_PRICE));
        product.image = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE));
        product.supplierName = cursor.getString(cursor.getColumnIndex(
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME));
        product.supplierMail = cursor.getString(cursor.getColumnIndex(
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL));

        product.quantity = cursor.getInt(cursor.getColumnIndex(
                ProductEntry.COLUMN_PRODUCT_QUANTITY));


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

    /**
     * Returns the price in Cents (price/100) as String
     *
     * @return
     */
    public int getPrice() {
        return price;
    }

    public void setPrice(String p) {

        if (p.contains(Config.getCurrencySymbol())) {
            p = p.replace(Config.getCurrencySymbol(), "");
        }
        if (p.isEmpty()) {
            price = 0;
            return;
        }
        Double priceDouble = Double.parseDouble(p);
        //convert currency price to cents as that is what the database stores as integer
        price = (int) (priceDouble * (double) 100);
    }

    /**
     * @return price as String with currency symbol example $2.99
     */
    public String getLocalizedPrice() {
        if (price == 0)
            return ""; //This will prevent the App from assuming a new product is an existing one
        return NumberFormat.getCurrencyInstance(Config.USER_LOCALE).format(price / 100.0);
    }

    public void setLocalizedPrice(String localizedPrice) {
        setPrice(localizedPrice);
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
     * I've added the attribute app:imageUrl="@{product.image}" to the ImageView.
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
