package yasminemassaoudi.grp3.fyourf;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class LocationDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "locations.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "location_history";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public LocationDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_TIMESTAMP + " LONG)";
        db.execSQL(createTable);

        // Insert test data for GeoQuiz
        insertTestData(db);
    }

    /**
     * Insert test location data for GeoQuiz
     */
    private void insertTestData(SQLiteDatabase db) {
        try {
            // Test locations in Tunisia
            ContentValues values1 = new ContentValues();
            values1.put(COLUMN_PHONE, "+21612345678");
            values1.put(COLUMN_LATITUDE, 36.8065);
            values1.put(COLUMN_LONGITUDE, 10.1815);
            values1.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
            db.insert(TABLE_NAME, null, values1);

            ContentValues values2 = new ContentValues();
            values2.put(COLUMN_PHONE, "+21612345678");
            values2.put(COLUMN_LATITUDE, 36.8070);
            values2.put(COLUMN_LONGITUDE, 10.1820);
            values2.put(COLUMN_TIMESTAMP, System.currentTimeMillis() + 30000);
            db.insert(TABLE_NAME, null, values2);

            ContentValues values3 = new ContentValues();
            values3.put(COLUMN_PHONE, "+21612345678");
            values3.put(COLUMN_LATITUDE, 36.8075);
            values3.put(COLUMN_LONGITUDE, 10.1825);
            values3.put(COLUMN_TIMESTAMP, System.currentTimeMillis() + 60000);
            db.insert(TABLE_NAME, null, values3);

            ContentValues values4 = new ContentValues();
            values4.put(COLUMN_PHONE, "+21612345678");
            values4.put(COLUMN_LATITUDE, 36.8080);
            values4.put(COLUMN_LONGITUDE, 10.1830);
            values4.put(COLUMN_TIMESTAMP, System.currentTimeMillis() + 90000);
            db.insert(TABLE_NAME, null, values4);

            ContentValues values5 = new ContentValues();
            values5.put(COLUMN_PHONE, "+21612345678");
            values5.put(COLUMN_LATITUDE, 36.8085);
            values5.put(COLUMN_LONGITUDE, 10.1835);
            values5.put(COLUMN_TIMESTAMP, System.currentTimeMillis() + 120000);
            db.insert(TABLE_NAME, null, values5);

            ContentValues values6 = new ContentValues();
            values6.put(COLUMN_PHONE, "+21612345678");
            values6.put(COLUMN_LATITUDE, 35.7595);
            values6.put(COLUMN_LONGITUDE, 10.5671);
            values6.put(COLUMN_TIMESTAMP, System.currentTimeMillis() + 150000);
            db.insert(TABLE_NAME, null, values6);

            android.util.Log.d("LocationDatabase", "✓ Test data inserted successfully");
        } catch (Exception e) {
            android.util.Log.e("LocationDatabase", "Error inserting test data", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addLocation(String phone, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if entry already exists for this phone number
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID},
                COLUMN_PHONE + "=?", new String[]{phone},
                null, null, null);

        ContentValues values = new ContentValues();
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());

        if (cursor.moveToFirst()) {
            // Update existing entry
            int rowsAffected = db.update(TABLE_NAME, values, COLUMN_PHONE + "=?", new String[]{phone});
            android.util.Log.d("LocationDatabase", "Location updated for " + phone + " at " + latitude + "," + longitude + " - Rows affected: " + rowsAffected);
        } else {
            // Insert new entry
            long result = db.insert(TABLE_NAME, null, values);
            android.util.Log.d("LocationDatabase", "Location added for " + phone + " at " + latitude + "," + longitude + " - Result: " + result);
        }

        cursor.close();
        db.close();
    }

    public List<LocationEntry> getAllLocations() {
        List<LocationEntry> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                LocationEntry entry = new LocationEntry(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                );
                locations.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return locations;
    }

    public void deleteLocation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteAllLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
        android.util.Log.d("LocationDatabase", "All locations deleted");
    }

    public static class LocationEntry {
        public String phone;
        public String phoneNumber; // Alias for compatibility
        public double latitude;
        public double longitude;
        public long timestamp;

        public LocationEntry(String phone, double latitude, double longitude, long timestamp) {
            this.phone = phone;
            this.phoneNumber = phone; // Set both for compatibility
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;
        }
    }
}
