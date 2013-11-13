package net.shuttleplay.shuttle.common;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class QrHistory extends SQLiteOpenHelper
{
    private static final String DB_NAME = "qrhistory";
    private static final String CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS " +
            "qrhistory (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                    "rawtext TEXT, " +
                    "timestamp INTEGER);";
    private static final int VERSION = 1;

    public QrHistory(Context context)
    {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        throw new UnsupportedOperationException("Upgrade database " + DB_NAME
                + " is unsupported");
    }

    public void addText(String text)
    {
        Date date = new Date();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("rawtext", text);
        cv.put("timestamp", date.getTime());
        db.insert("qrhistory", null, cv);
    }

    public Cursor query()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("qrhistory", null, null, null, null, null, BaseColumns._ID +  " DESC");
    }

    public int getCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("qrhistory", new String[]{"count(*)"}, null, null, null, null, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public void deleteAll()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("qrhistory", null, null);
    }
}
