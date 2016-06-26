package com.danielkim.soundrecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;

public class DBHelper extends SQLiteOpenHelper {
    private Context mContext;

    private static final String LOG_TAG = "DBHelper";

    private static OnDatabaseChangedListener mOnDatabaseChangedListener;

    public static final String DATABASE_NAME = "application.db";
    private static final int DATABASE_VERSION = 1;

    public static abstract class SavedRecordings implements BaseColumns {
        public static final String RECORDINGS_TABLE_NAME = "saved_recordings";

        public static final String COLUMN_NAME_RECORDING_NAME = "recording_name";
        public static final String COLUMN_NAME_RECORDING_FILE_PATH = "file_path";
        public static final String COLUMN_NAME_RECORDING_LENGTH = "length";
        public static final String COLUMN_NAME_TIME_ADDED = "time_added";
    }

    public static abstract class Audiobooks implements BaseColumns {
        public static final String AUDIOBOOKS_TABLE_NAME = "audiobooks";

        public static final String COLUMN_NAME_AUDIOBOOK_ALIAS = "audiobook_alias";
        public static final String COLUMN_NAME_RECORDING_FILE_PATH = "file_path";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_AUDIOBOOK_LENGTH = "length";
        public static final String COLUMN_NAME_TIME_LAST_OPENED = "time_last_opened";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_RECORDINGS_TABLE =
            "CREATE TABLE " + SavedRecordings.RECORDINGS_TABLE_NAME + " (" +
                    SavedRecordings._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    SavedRecordings.COLUMN_NAME_RECORDING_NAME + TEXT_TYPE + COMMA_SEP +
                    SavedRecordings.COLUMN_NAME_RECORDING_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                    SavedRecordings.COLUMN_NAME_RECORDING_LENGTH + " INTEGER " + COMMA_SEP +
                    SavedRecordings.COLUMN_NAME_TIME_ADDED + " INTEGER " + ")";
    private static final String SQL_CREATE_AUDIOBOOKS_TABLE =
            "CREATE TABLE " + Audiobooks.AUDIOBOOKS_TABLE_NAME + " (" +
                    Audiobooks._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    Audiobooks.COLUMN_NAME_AUDIOBOOK_ALIAS + TEXT_TYPE + COMMA_SEP +
                    Audiobooks.COLUMN_NAME_RECORDING_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                    Audiobooks.COLUMN_NAME_POSITION + " INTEGER " + COMMA_SEP +
                    Audiobooks.COLUMN_NAME_AUDIOBOOK_LENGTH + " INTEGER " + COMMA_SEP +
                    Audiobooks.COLUMN_NAME_TIME_LAST_OPENED + " INTEGER " + ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RECORDINGS_TABLE);
        db.execSQL(SQL_CREATE_AUDIOBOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static void setOnDatabaseChangedListener(OnDatabaseChangedListener listener) {
        mOnDatabaseChangedListener = listener;
    }

    public void synchronizeAudiobooksWithFileSystem(String audiobooksStoragePath) {
        File folder = new File(audiobooksStoragePath);
        File[] listOfFile = folder.listFiles();
        if (listOfFile == null) {
            return;
        }
        List<File> files = new ArrayList<>(asList(listOfFile));
        for (File file : files) {
            if (file.isFile() && !hasAudiobook(file)) {
                addAudiobook(file);
            }
        }

        for (int index = 0; index < getAudiobookCount(); index++) {
            String persistedPath = getAudiobookItemAt(index).getFilePath();
            if (!files.contains(new File(persistedPath))) {
                removeAudiobookItem(persistedPath);
            }
        }
    }

    public RecordingItem getRecordingItemAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                SavedRecordings._ID,
                SavedRecordings.COLUMN_NAME_RECORDING_NAME,
                SavedRecordings.COLUMN_NAME_RECORDING_FILE_PATH,
                SavedRecordings.COLUMN_NAME_RECORDING_LENGTH,
                SavedRecordings.COLUMN_NAME_TIME_ADDED
        };
        Cursor c = db.query(SavedRecordings.RECORDINGS_TABLE_NAME, projection, null, null, null, null, null);
        if (c.moveToPosition(position)) {
            RecordingItem item = new RecordingItem();
            item.setId(c.getInt(c.getColumnIndex(SavedRecordings._ID)));
            item.setName(c.getString(c.getColumnIndex(SavedRecordings.COLUMN_NAME_RECORDING_NAME)));
            item.setFilePath(c.getString(c.getColumnIndex(SavedRecordings.COLUMN_NAME_RECORDING_FILE_PATH)));
            item.setLength(c.getInt(c.getColumnIndex(SavedRecordings.COLUMN_NAME_RECORDING_LENGTH)));
            item.setTime(c.getLong(c.getColumnIndex(SavedRecordings.COLUMN_NAME_TIME_ADDED)));
            c.close();
            return item;
        }
        return null;
    }

    public Audiobook getAudiobookItemAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                Audiobooks._ID,
                Audiobooks.COLUMN_NAME_AUDIOBOOK_ALIAS,
                Audiobooks.COLUMN_NAME_RECORDING_FILE_PATH,
                Audiobooks.COLUMN_NAME_POSITION,
                Audiobooks.COLUMN_NAME_AUDIOBOOK_LENGTH,
                Audiobooks.COLUMN_NAME_TIME_LAST_OPENED
        };
        Cursor c = db.query(Audiobooks.AUDIOBOOKS_TABLE_NAME, projection, null, null, null, null, null);
        if (c.moveToPosition(position)) {
            Audiobook item = new Audiobook();
            item.setId(c.getInt(c.getColumnIndex(Audiobooks._ID)));
            item.setName(c.getString(c.getColumnIndex(Audiobooks.COLUMN_NAME_AUDIOBOOK_ALIAS)));
            item.setFilePath(c.getString(c.getColumnIndex(Audiobooks.COLUMN_NAME_RECORDING_FILE_PATH)));
            item.setPosition(c.getLong(c.getColumnIndex(Audiobooks.COLUMN_NAME_POSITION)));
            item.setDuration(c.getLong(c.getColumnIndex(Audiobooks.COLUMN_NAME_AUDIOBOOK_LENGTH)));
            item.setLastOpened(c.getLong(c.getColumnIndex(Audiobooks.COLUMN_NAME_TIME_LAST_OPENED)));
            c.close();
            return item;
        }
        return null;
    }

    public void removeRecordingItemWithId(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = {String.valueOf(id)};
        db.delete(SavedRecordings.RECORDINGS_TABLE_NAME, "_ID=?", whereArgs);
    }

    public int getRecordingsCount() {
        return getRecordCount(SavedRecordings.RECORDINGS_TABLE_NAME);
    }

    public int getAudiobookCount() {
        return getRecordCount(Audiobooks.AUDIOBOOKS_TABLE_NAME);
    }

    private int getRecordCount(String tableName) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {SavedRecordings._ID};
        Cursor c = db.query(tableName, projection, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    public Context getContext() {
        return mContext;
    }

    public class RecordingComparator implements Comparator<RecordingItem> {

        public int compare(RecordingItem item1, RecordingItem item2) {
            Long o1 = item1.getTime();
            Long o2 = item2.getTime();
            return o2.compareTo(o1);
        }
    }
    public long addRecording(String recordingName, String filePath, long length) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SavedRecordings.COLUMN_NAME_RECORDING_NAME, recordingName);
        cv.put(SavedRecordings.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
        cv.put(SavedRecordings.COLUMN_NAME_RECORDING_LENGTH, length);
        cv.put(SavedRecordings.COLUMN_NAME_TIME_ADDED, System.currentTimeMillis());
        long rowId = db.insert(SavedRecordings.RECORDINGS_TABLE_NAME, null, cv);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }

        return rowId;
    }

    public void renameRecordingItem(RecordingItem item, String recordingName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SavedRecordings.COLUMN_NAME_RECORDING_NAME, recordingName);
        db.update(SavedRecordings.RECORDINGS_TABLE_NAME, cv,
                SavedRecordings._ID + "=" + item.getId(), null);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }
    }

    public void renameAudiobook(Audiobook item, String newName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Audiobooks.COLUMN_NAME_AUDIOBOOK_ALIAS, newName);
        db.update(Audiobooks.AUDIOBOOKS_TABLE_NAME, contentValues,
                Audiobooks._ID + "=" + item.getId(), null);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }
    }

    private boolean hasAudiobook(File file) {
        SQLiteDatabase db = getReadableDatabase();
        String[] whereArgs = {String.valueOf(file.getAbsolutePath())};
        String[] projection = {Audiobooks._ID};
        Cursor cursor = db.query(Audiobooks.AUDIOBOOKS_TABLE_NAME, projection, Audiobooks.COLUMN_NAME_RECORDING_FILE_PATH + "=?", whereArgs, null, null, null);
        boolean found = cursor.getCount() == 1;
        cursor.close();
        return found;
    }

    private void removeAudiobookItem(String path) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = {String.valueOf(path)};
        db.delete(Audiobooks.AUDIOBOOKS_TABLE_NAME, Audiobooks.COLUMN_NAME_RECORDING_FILE_PATH + "=?", whereArgs);
    }

    private long addAudiobook(File file) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Audiobooks.COLUMN_NAME_AUDIOBOOK_ALIAS, file.getName());
        contentValues.put(Audiobooks.COLUMN_NAME_RECORDING_FILE_PATH, file.getAbsolutePath());
        contentValues.put(Audiobooks.COLUMN_NAME_POSITION, 0);
        contentValues.put(Audiobooks.COLUMN_NAME_AUDIOBOOK_LENGTH, MediaHelper.calculateAudiobookDuration(file.getAbsolutePath()));
        contentValues.put(Audiobooks.COLUMN_NAME_TIME_LAST_OPENED, System.currentTimeMillis());
        long rowId = db.insert(Audiobooks.AUDIOBOOKS_TABLE_NAME, null, contentValues);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }

        return rowId;
    }

}
