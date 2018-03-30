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

package android.example.com.myinventoryapp.models;

import android.content.Context;
import android.database.Cursor;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.example.com.myinventoryapp.config.Config;
import android.example.com.myinventoryapp.data.ProductContract.ProductEntry;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;


public class Product extends BaseObservable {

    private ProductMonitor productMonitor;
    private int id;
    private String name;
    /**
     * the price in cents
     */
    private int price;
    private String localizedPrice;
    private String image = "";
    private String supplierName = "";
    private String supplierMail = "";
    private int quantity = 1;


    public Product() {

    }

    public Product(Context context) {
        productMonitor = (ProductMonitor) context;
    }

    public static Product fromCursor(Context context, Cursor cursor) {
        Product product = new Product(context);
        int idIndex = cursor.getColumnIndex(ProductEntry._ID);
        int nameIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int imageIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
        int supplierNameIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        int supplierEmailIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
        int quantityIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

        //Check if the columns actually exist
        if (idIndex > -1)
            product.id = Integer.parseInt(cursor.getString(idIndex));

        if (nameIndex > -1)
            product.name = cursor.getString(nameIndex);

        if (priceIndex > -1)
            product.price = cursor.getInt(priceIndex);

        if (imageIndex > -1)
            product.image = cursor.getString(imageIndex);

        if (supplierNameIndex > -1)
            product.supplierName = cursor.getString(supplierNameIndex);

        if (supplierEmailIndex > -1)
            product.supplierMail = cursor.getString(supplierEmailIndex);

        if (quantityIndex > -1)
            product.quantity = cursor.getInt(quantityIndex);


        return product;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!name.equals(this.name)) {
            raisePropertyChangedEvent("name");
            this.name = name;
        }
    }

    /**
     * Returns the price in Cents (price/100) as String
     *
     * @return
     */
    public int getPrice() {
        return price;
    }

    /**
     *
     * @param priceString is the String coming from the price EditText
     */
    public void setPrice(String priceString) {
        //The commented out code works with various currencies BUT if
        //I use Local.GERMANY, priceString will contain an unknown character, just not the €
        //which is not replaced, and which will cause the cast to Double to fail
        priceString = priceString.replaceAll("[^0-9.,]+","");
        priceString = priceString.replaceAll("[,]+",".");
        /*if (p.contains(Config.getCurrencySymbol())) {
            p = p.replace(Config.getCurrencySymbol(), "");
        }*/

        if (priceString.isEmpty()) {
            price = 0;
            return;
        }
        Double priceDouble = Double.parseDouble(priceString);
        //convert currency price to cents as that is what the database stores as integer
        int tempPrice = (int) (priceDouble * (double) 100);
        if (tempPrice != price) {
            raisePropertyChangedEvent("price");
            price = tempPrice;
        }
    }

    /**
     * @return price as String with currency symbol example $2.99
     */
    public String getLocalizedPrice() {
        if (price == 0)
            return null; //This will prevent the App from assuming a new product is an existing one
        return NumberFormat.getCurrencyInstance().format(price / 100.0);
    }

    public void setLocalizedPrice(String localizedPrice) {
        setPrice(localizedPrice);
        if (!localizedPrice.equals(getLocalizedPrice())) {
            raisePropertyChangedEvent("localizedPrice");
        }
    }

    @Bindable
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
        notifyPropertyChanged(android.example.com.myinventoryapp.BR.image);
        raisePropertyChangedEvent("image");
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {

        if (!this.supplierName.equals(supplierName)) {
            raisePropertyChangedEvent("supplierName");
            this.supplierName = supplierName;
        }
    }

    public String getSupplierMail() {
        return supplierMail;
    }

    public void setSupplierMail(String supplierMail) {
        if (!this.supplierMail.equals(supplierMail)) {
            this.supplierMail = supplierMail;
            raisePropertyChangedEvent("supplierName");
        }
    }

    @Bindable
    public String getQuantity() {
        return String.valueOf(quantity);
    }

    public void setQuantity(String quantity) {
        if (!quantity.isEmpty()) {
            int tempQuantity = Integer.parseInt(quantity);
            if (tempQuantity != this.quantity) {
                this.quantity = tempQuantity;
                raisePropertyChangedEvent("quantity");
            }
        }
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

    public void incQuantity() {
        ++quantity;
        notifyPropertyChanged(android.example.com.myinventoryapp.BR.quantity);
    }

    public void decQuantity() {
        if (quantity > 1) {
            --quantity;
            notifyPropertyChanged(android.example.com.myinventoryapp.BR.quantity);
        }
    }

    private void raisePropertyChangedEvent(String which) {
        if (productMonitor != null)
            productMonitor.onPropertyChanged(which);
    }


}
