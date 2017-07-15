package com.ma.gmp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    public static final String DATABASE_NAME = "access_db";
    public static final String NAME_URL_DATA_TABLE = "url_data_table";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_URL_DATA_TABLE  = "CREATE TABLE " + NAME_URL_DATA_TABLE + "(" +
            URLDataTable.TITLE + " TEXT," +URLDataTable.BODY + " TEXT," +
            URLDataTable.IMAGE_NAME + " TEXT," + URLDataTable.WIDTH + " INTEGER ," + URLDataTable.HEIGHT + " INTEGER," +
            URLDataTable.IMAGE + " BLOB);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_URL_DATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NAME_URL_DATA_TABLE);
        onCreate(db);
    }



    public static class URLDataTable {
        public static final String TITLE = "title";
        public static final String BODY = "body";
        public static final String IMAGE_NAME = "image_name";
        public static final String IMAGE = "image_data";
        public static final String WIDTH = "image_width";
        public static final String HEIGHT = "image_height";
    }


}