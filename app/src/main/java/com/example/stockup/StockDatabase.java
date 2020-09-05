package com.example.stockup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class StockDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "db2";
    private static final String DATABASE_TABLE = "table1";

    private static final String SYMBOL = "symbol";
    private static final String NAME = "name";
    private static final String PRICE = "price";
    private static final String CHANGE = "change";
    private static final String CHANGE_PERCENTAGE = "ChangePercentage";

    StockDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + DATABASE_TABLE + "(" +
                SYMBOL + " TEXT NOT NULL, " +
                NAME + " TEXT NOT NULL, " +
                PRICE + " DOUBLE NOT NULL, " +
                CHANGE + " DOUBLE NOT NULL, " +
                CHANGE_PERCENTAGE + " DOUBLE NOT NULL)";

        db.execSQL(query);
    }


    //get newer version
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= newVersion) {
            return;
        }

        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }


    //add one stock to database
    public void addStock(Stock stock) {
        Log.d(TAG, "addStock: Adding " + stock.getSymbol());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues c = new ContentValues();

        c.put(SYMBOL, stock.getSymbol());
        c.put(NAME, stock.getName());
        c.put(PRICE, stock.getLatestPrice());
        c.put(CHANGE, stock.getChange());
        c.put(CHANGE_PERCENTAGE, stock.getChangePercent());

        long key = db.insert(DATABASE_TABLE, null, c);

        Log.d(TAG, "addStock: Added " + key + " stock");
    }


    //delete a stock from database
    public void deleteStock(String symbol) {
        Log.d(TAG, "deleteStock: Deleting Stock " + symbol);

        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.delete(DATABASE_TABLE, SYMBOL + "=?", new String[]{symbol});

        Log.d(TAG, "deleteStock: " + count);

        db.close();
    }


    //get all stocks at once
    public List<Stock> getStocks() {
        List<Stock> allStocks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(DATABASE_TABLE,
                new String[]{SYMBOL, NAME, PRICE, CHANGE,CHANGE_PERCENTAGE},
                null,
                null,
                null,
                null,
                null);

        if(cursor != null) {
            cursor.moveToFirst();

            for(int i = 0; i < cursor.getCount(); i++) {
                String symbol = cursor.getString(0);
                String name = cursor.getString(1);
                double price = cursor.getDouble(2);
                double change = cursor.getDouble(3);
                double changeP = cursor.getDouble(4);

                Stock stock = new Stock(symbol, name, price, change, changeP);
                allStocks.add(stock);
                cursor.moveToNext();
            }
            cursor.close();
        }

        Log.d(TAG, "getStock: GETTING STOCKS");

        return allStocks;
    }
}
