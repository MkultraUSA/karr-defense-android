package com.codex.karrdefense;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Local SQLite store for documented vehicle-audit findings.
 *
 * Plain SQLiteOpenHelper (no Room / no external deps) so it builds with the
 * existing manual javac + d8 toolchain. The database is app-private
 * (getDatabasePath), so findings stay on the tablet unless the operator
 * exports a report / evidence packet.
 */
public class AuditDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "karr_vehicle_audit.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE = "documented_findings";

    private static final String[] COLUMNS = {
            "_id", "created_at", "session_id", "target_type", "target_id",
            "target_name", "rssi", "category", "severity", "notes"
    };

    public AuditDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "created_at TEXT NOT NULL,"
                + "session_id TEXT,"
                + "target_type TEXT,"
                + "target_id TEXT,"
                + "target_name TEXT,"
                + "rssi INTEGER,"
                + "category TEXT,"
                + "severity TEXT,"
                + "notes TEXT)");
        db.execSQL("CREATE INDEX idx_documented_findings_target ON "
                + TABLE + " (target_type, target_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // MVP: local findings are disposable working data, recreate on schema change.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public long insert(AuditFinding finding) {
        ContentValues values = new ContentValues();
        values.put("created_at", finding.createdAt);
        values.put("session_id", finding.sessionId);
        values.put("target_type", finding.targetType);
        values.put("target_id", finding.targetId);
        values.put("target_name", finding.targetName);
        values.put("rssi", finding.rssi);
        values.put("category", finding.category);
        values.put("severity", finding.severity);
        values.put("notes", finding.notes);
        return getWritableDatabase().insert(TABLE, null, values);
    }

    /** Newest first. */
    public List<AuditFinding> listAll() {
        List<AuditFinding> findings = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE, COLUMNS,
                null, null, null, null, "_id DESC");
        try {
            while (cursor.moveToNext()) {
                findings.add(read(cursor));
            }
        } finally {
            cursor.close();
        }
        return findings;
    }

    public int count() {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE, null);
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    public int delete(long id) {
        return getWritableDatabase().delete(TABLE, "_id = ?",
                new String[] { String.valueOf(id) });
    }

    public int deleteAll() {
        return getWritableDatabase().delete(TABLE, null, null);
    }

    private AuditFinding read(Cursor cursor) {
        return new AuditFinding(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getInt(6),
                cursor.getString(7),
                cursor.getString(8),
                cursor.getString(9));
    }
}