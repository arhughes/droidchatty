package cc.hughes.droidchatty2.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.hughes.droidchatty2.net.RootPost;

public class PostCountDatabase extends SQLiteOpenHelper {

    private static final String TAG = "PostCountDatabase";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "post_counts";
    private static final String TABLE_POST_COUNTS = "thread_post_counts";

    private static final String KEY_ID = "id";
    private static final String KEY_COUNT = "count";

    private static final int MAX_ENTRIES = 1000;

    public PostCountDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_POST_COUNTS_TABLE = "CREATE TABLE " + TABLE_POST_COUNTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_COUNT + " INTEGER"
                + ")";

        db.execSQL(CREATE_POST_COUNTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        // pass
    }

    public Map<Integer, Integer> getCounts(List<RootPost> threads) {
        SQLiteDatabase db = getReadableDatabase();

        Log.d(TAG, "Fetching counts for " + threads.size() + " threads.");

        String[] values = new String[threads.size()];
        StringBuilder builder = new StringBuilder();
        builder.append(KEY_ID).append(" IN (");

        for (int i = 0; i < threads.size(); i++) {
            if (i != 0)
                builder.append(",");
            builder.append("?");
            values[i] = String.valueOf(threads.get(i).id);
        }
        builder.append(")");

        Cursor cursor = db.query(TABLE_POST_COUNTS, new String[] { KEY_ID, KEY_COUNT}, builder.toString(), values, null, null, null, null);

        Map<Integer, Integer> existing = new HashMap<Integer, Integer>();

        while (cursor.moveToNext()) {
            existing.put(cursor.getInt(0), cursor.getInt(1));
        }

        return existing;
    }

    public void updateCounts(Map<Integer, Integer> threadCounts) {
        SQLiteDatabase db = getWritableDatabase();

        Log.d(TAG, "Updating counts for " + threadCounts.size() + " threads.");

        String sql = "REPLACE INTO " + TABLE_POST_COUNTS + " VALUES (?, ?)";
        SQLiteStatement statement = db.compileStatement(sql);

        db.beginTransaction();
        try {
            for (Map.Entry<Integer, Integer> entry : threadCounts.entrySet()) {
                statement.bindLong(1, entry.getKey());
                statement.bindLong(2, entry.getValue());
                statement.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    void purge() {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_POST_COUNTS, null);
        cursor.moveToFirst();
        int count = cursor.getCount();

        if (count > MAX_ENTRIES * 1.5) {
            int remove = count - MAX_ENTRIES;

            Log.d(TAG, "Purging " + remove + " old thread count records.");

            StringBuilder builder = new StringBuilder();
            builder.append("DELETE FROM ").append(TABLE_POST_COUNTS)
                    .append(" WHERE ").append(KEY_ID).append(" IN (")
                    .append("SELECT ").append(KEY_ID)
                    .append(" FROM ").append(TABLE_POST_COUNTS)
                    .append(" ORDER BY ").append(KEY_ID)
                    .append(" LIMIT ").append(remove)
                    .append(")");

            db.execSQL(builder.toString());
        }
    }

}
