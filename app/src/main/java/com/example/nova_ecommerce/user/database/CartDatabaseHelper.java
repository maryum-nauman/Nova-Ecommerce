package com.example.nova_ecommerce.user.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.nova_ecommerce.user.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "nova_cart.db";
    private static final int DB_VERSION = 4;

    public static final String TABLE_CART = "cart";
    public static final String COL_ID = "id";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_PRODUCT_ID = "product_id";
    public static final String COL_CATEGORY_ID = "category_id";
    public static final String COL_NAME = "name";
    public static final String COL_PRICE = "price";
    public static final String COL_IMAGE_URL = "image_url";
    public static final String COL_QUANTITY = "quantity";

    private static CartDatabaseHelper instance;

    public static synchronized CartDatabaseHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new CartDatabaseHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    private CartDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CART + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_ID + " TEXT, "
                + COL_PRODUCT_ID + " TEXT, "
                + COL_CATEGORY_ID + " TEXT, "
                + COL_NAME + " TEXT, "
                + COL_PRICE + " REAL, "
                + COL_IMAGE_URL + " TEXT, "
                + COL_QUANTITY + " INTEGER DEFAULT 1"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        onCreate(db);
    }

    public void addOrIncrement(CartItem item, String userId) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(TABLE_CART,
                new String[]{COL_ID, COL_QUANTITY}, COL_PRODUCT_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{item.getProductId(), userId}, null, null, null);

        if (cursor.moveToFirst()) {
            int qty = cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY));
            String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID));

            ContentValues cv = new ContentValues();
            cv.put(COL_QUANTITY, qty + 1);
            db.update(TABLE_CART, cv, COL_ID + "=?", new String[]{id});
        } else {
            ContentValues cv = new ContentValues();
            cv.put(COL_USER_ID, userId);
            cv.put(COL_PRODUCT_ID, item.getProductId());
            cv.put(COL_CATEGORY_ID, item.getCategoryId());
            cv.put(COL_NAME, item.getName());
            cv.put(COL_PRICE, item.getPrice());
            cv.put(COL_IMAGE_URL, item.getImageUrl());
            cv.put(COL_QUANTITY, 1);

            db.insert(TABLE_CART, null, cv);
        }
        cursor.close();
    }

    public List<CartItem> getAllItems(String userId) {
        List<CartItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_CART, null, COL_USER_ID + "=?", new String[]{userId}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                CartItem item = new CartItem();
                item.setDocId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))));
                item.setProductId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID)));
                item.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY_ID)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
                item.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRICE)));
                item.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_URL)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY)));
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void updateQuantity(String docId, int newQty) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_QUANTITY, newQty);
        db.update(TABLE_CART, cv, COL_ID + "=?", new String[]{docId});
    }

    public void deleteItem(String docId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CART, COL_ID + "=?", new String[]{docId});
    }

    public void clearCart(String userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CART, COL_USER_ID + "=?", new String[]{userId});
    }

    public double getTotal(String userId) {
        double total = 0;
        for (CartItem item : getAllItems(userId)) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }
}