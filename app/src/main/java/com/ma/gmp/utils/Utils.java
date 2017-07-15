package com.ma.gmp.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.ma.gmp.GMPApplication;
import com.ma.gmp.db.DatabaseHelper;
import com.ma.gmp.network.controller.MainActivityController;
import com.ma.gmp.ui.activity.MainActivity;

import java.nio.ByteBuffer;

public class Utils {
    private final String TAG = "Utils";

    public Bitmap resizeImage(Bitmap bitmap, Context context) {
        if (bitmap == null)
            return Bitmap.createBitmap(0, 0, Bitmap.Config.ARGB_4444);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float maxImageSize;
        if (width > height)
            maxImageSize = height;
        else
            maxImageSize = width;
        if (maxImageSize > metrics.widthPixels) {
            maxImageSize = metrics.widthPixels/2;
        }
        if (maxImageSize > metrics.heightPixels)
            maxImageSize = metrics.heightPixels/2;
        float ratio = Math.min(
                maxImageSize / bitmap.getWidth(),
                maxImageSize / bitmap.getHeight());
        width = Math.round(ratio * bitmap.getWidth());
        height = Math.round(ratio * bitmap.getHeight());
        return Bitmap.createScaledBitmap(bitmap, width,
                height, true);
    }

    public void storeDataInDb(final String name, final Bitmap bitmap, final GMPApplication application) {
        new Thread() {
            @Override
            public void run() {
                ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getRowBytes() * bitmap.getHeight());
                bitmap.copyPixelsToBuffer(byteBuffer);
                byte[] byteArray = byteBuffer.array();
                ContentValues contentValues = new ContentValues();
                contentValues.put(DatabaseHelper.URLDataTable.IMAGE_NAME, name);
                contentValues.put(DatabaseHelper.URLDataTable.IMAGE, byteArray);
                contentValues.put(DatabaseHelper.URLDataTable.HEIGHT, bitmap.getHeight());
                contentValues.put(DatabaseHelper.URLDataTable.WIDTH, bitmap.getWidth());
                String query = new StringBuilder(50).append("Select * from ").append(DatabaseHelper.NAME_URL_DATA_TABLE).append(" where ").append(DatabaseHelper.URLDataTable.IMAGE_NAME).append(" = '").append(name).append("'").toString();
                SQLiteDatabase db = null;
                try {
                    DatabaseHelper databaseHelperInstance = new DatabaseHelper(application.getApplicationContext());
                    db = databaseHelperInstance.getWritableDatabase();
                    boolean state = db.rawQuery(query, null).moveToFirst();
                    if (state) {
                        db.update(DatabaseHelper.NAME_URL_DATA_TABLE, contentValues, DatabaseHelper.URLDataTable.IMAGE_NAME + "='" + name + "'", null);
                    } else {
                        db.insert(DatabaseHelper.NAME_URL_DATA_TABLE, null, contentValues);
                    }
                } catch (SQLiteException sql) {
                    sql.printStackTrace();
                } finally {
                    if (db != null) db.close();
                }
                Log.d(TAG, name + "----image being saved in mobile device----");
            }
        }.start();
    }

    public void getDataFromDb(final String name, final MainActivity activity, final MainActivityController.Result result) {
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "getImageFromDb~~~>run  " + name);
                boolean success = false;
                SQLiteDatabase db = null;
                Cursor cursor = null;
                try {
                    DatabaseHelper databaseHelperInstance = new DatabaseHelper(activity.getApplicationContext());
                    db = databaseHelperInstance.getReadableDatabase();
                    cursor = db.rawQuery(new StringBuilder(50).append("select * from ").append(DatabaseHelper.NAME_URL_DATA_TABLE).append(" where ").append(DatabaseHelper.URLDataTable.IMAGE_NAME).append("='").append(name).append("'").toString(), null);
                    if (cursor.moveToFirst()) {
                        int width = cursor.getInt(1);
                        int height = cursor.getInt(2);
                        byte[] byteArray = cursor.getBlob(3);
                        byte[] decodedString = Base64.decode(new String(byteArray), Base64.DEFAULT);
                        Bitmap bitmap = resizeImage(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length), activity.getApplicationContext());
                        ((GMPApplication) activity.getApplication()).getNetworkHandlerInstance().putImage(name, bitmap);
                        /*activity.getHandler().post(new Thread() {
                            public void run() {
                                Log.d(TAG, "@getImageFromDb~~~>: " + name);
                                result.setSuccessResultData(name);
                            }
                        });*/
                        success = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (db != null)
                        db.close();
                    if (cursor != null)
                        cursor.close();
                }
                if (!success) {
//                    activity.getHandler().post(new Thread() {
//                        public void run() {
//                            result.setFailureResultData(name);
//                        }
//                    });
                }
            }
        }.start();
    }



}

