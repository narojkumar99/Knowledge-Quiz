package com.paperplanes.knowquiz.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by abdularis on 25/04/17.
 */

public class SimpleSQLiteOpenHelper {
    private static final String TAG = SimpleSQLiteOpenHelper.class.getSimpleName();

    private static final String ASSET_DB_DIR = "databases/";
    private static String DB_PATH = "";
    private final Context mContext;
    private String mDbName;
    private int mDbVersion;
    private SQLiteDatabase mDatabase;

    public SimpleSQLiteOpenHelper(@NonNull Context context, @NonNull String dbName, int dbVersion) {
        DB_PATH = context.getFilesDir().getParentFile().getPath() + "/" + ASSET_DB_DIR;
        mDatabase = null;
        mContext = context;
        mDbName = dbName;
        mDbVersion = dbVersion;
    }

    public SQLiteDatabase getDatabase() {
        if (mDatabase != null ) {
            if (!mDatabase.isOpen()) {
                mDatabase = null;
            }
            else {
                return mDatabase;
            }
        }

        SQLiteDatabase db = openDatabase();
        int currVersion = db.getVersion();

        if (currVersion != mDbVersion) {
            Log.v(TAG, "Curr. db version: " + currVersion + ", replaced with version: " + mDbVersion);

            onBeforeReplaced(db);
            try {
                copyDbFromAsset();
            } catch (IOException e) {
                Log.e(TAG, "FailedToCopyDatabase: " + e.toString());
            }
            db.setVersion(mDbVersion);
            onAfterReplaced(db);
        }

        mDatabase = db;
        return db;
    }

    public void onBeforeReplaced(SQLiteDatabase db) {
    }

    public void onAfterReplaced(SQLiteDatabase db) {
    }

    private SQLiteDatabase openDatabase() {
        SQLiteDatabase db = null;
        try {
            db = mContext.openOrCreateDatabase(mDbName, Context.MODE_PRIVATE, null);
        } catch (SQLiteException ex) {
            Log.e(TAG, ex.toString());
        }
        return db;
    }

    private void copyDbFromAsset() throws IOException {
        InputStream inputDbFile = mContext.getAssets().open(ASSET_DB_DIR + mDbName);
        OutputStream outFile = new FileOutputStream(DB_PATH + mDbName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputDbFile.read(buffer)) > 0) {
            outFile.write(buffer, 0, length);
        }

        outFile.flush();
        outFile.close();
        inputDbFile.close();
    }

}
