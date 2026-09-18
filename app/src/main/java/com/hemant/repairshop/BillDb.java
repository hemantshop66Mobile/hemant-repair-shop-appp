package com.hemantrepairshop.billing;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;

final class BillDb extends SQLiteOpenHelper {
    BillDb(Context context) { super(context, "bills.db", null, 1); }
    @Override public void onCreate(SQLiteDatabase db) { db.execSQL("CREATE TABLE bills(id INTEGER PRIMARY KEY AUTOINCREMENT, created INTEGER, customer TEXT, mobile TEXT, device TEXT, work TEXT, amount REAL, paid INTEGER, notes TEXT)"); }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    long add(String customer,String mobile,String device,String work,double amount,boolean paid,String notes) {
        ContentValues v=new ContentValues(); v.put("created",System.currentTimeMillis());v.put("customer",customer);v.put("mobile",mobile);v.put("device",device);v.put("work",work);v.put("amount",amount);v.put("paid",paid?1:0);v.put("notes",notes);
        return getWritableDatabase().insertOrThrow("bills",null,v);
    }
    Cursor all() { return getReadableDatabase().rawQuery("SELECT * FROM bills ORDER BY id DESC",null); }
}
