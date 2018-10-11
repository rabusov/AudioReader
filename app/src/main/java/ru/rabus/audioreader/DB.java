package ru.rabus.audioreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

import ru.rabus.audioreader.Items.AudioItem;
import ru.rabus.audioreader.Items.LabelItem;
import ru.rabus.audioreader.dummy.DummyContent;
import ru.rabus.audioreader.dummy.LabelContent;


/**
 * Created by Сергей on 21.01.2018.
 */

public class DB extends SQLiteOpenHelper implements ISQLConstants {
    static private SQLiteDatabase db = null;
    static private SQLiteDatabase db_readonly = null;
    static private Context context = null;
    static private final String TAG = DB.class.getSimpleName();
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        db = sqLiteDatabase;
        if (db == null) db = getWritableDatabase();
        if (db == null) {
            Log.e(TAG, "DB is null");
        }
        else {
            CreateMainTables();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        db = sqLiteDatabase;
        String stmt = "ALTER TABLE Items ADD COLUMN haveRead INTEGER";
        if (oldVersion==1 && newVersion == 2)
        {
            db.execSQL(stmt);
        }
    }
    public DB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        DB.context = context;
        if (db == null) db = getWritableDatabase();
        if (db_readonly == null) db_readonly = getReadableDatabase();
    }
    private void CreateMainTables( )
    {
       String stmt = "CREATE TABLE IF NOT EXISTS ItemLabels (_id INTEGER PRIMARY KEY AUTOINCREMENT, id_Items INTEGER, Position INTEGER, Title TEXT, Comment TEXT);";
       db.execSQL( stmt.toString() );
       stmt = "CREATE TABLE IF NOT EXISTS Items (_id INTEGER PRIMARY KEY AUTOINCREMENT, FullName TEXT UNIQUE, Content Text, FileSize INTEGER, isExists BOOLEAN, LastTimePosition INTEGER, haveRead INTEGER);";
       db.execSQL( stmt.toString() );
       stmt = "CREATE INDEX IF NOT EXISTS fullname_idx ON Items (FullName)";
       db.execSQL( stmt.toString() );
    }
    public static int[] ListItemsIDS()
    {
        String stmt = "SELECT _id FROM Items ORDER BY 1;";
        return fillIDSArray(stmt.toString(), null);
    }
    public static int[] fillIDSArray(String stmt, String columnname) {
        Cursor Rows = db_readonly.rawQuery(stmt, null);
        int[] listOfID = new int[Rows.getCount()];
        if (Rows != null)
        {
            if (Rows.moveToFirst())
            {
                int i = 0;
                int idx = (columnname != null) ? Rows.getColumnIndex(columnname) : 0;
                do {
                    listOfID[i] = Rows.getInt(idx);
                    i++;
                } while(Rows.moveToNext());
            }
            Rows.close();
        }
        return listOfID;
    }
    public static AudioItem getItem(int id)
    {
        AudioItem li = null;
        StringBuilder stmt = new StringBuilder("SELECT * FROM Items");
        stmt.append(" WHERE _ID=").append(id);
        Cursor Rows = db_readonly.rawQuery(stmt.toString(), null);
        if (Rows.moveToFirst())
        {
            li = getAudioItem(Rows);
        }
        if (Rows != null) Rows.close();
        return li;
    }
    public static AudioItem getAudioItem(Cursor row)
    {
        AudioItem li = null;
        if (row != null)
        {
            li = new AudioItem();
            li.id = row.getInt(row.getColumnIndex(ISQLConstants._ID));
            li.FullName = row.getString(row.getColumnIndex(ISQLConstants.COL_FullName));
            li.Content = row.getString(row.getColumnIndex(ISQLConstants.COL_Content));
            checkFile (li);
            li.LastTimePosition = row.getInt(row.getColumnIndex(ISQLConstants.COL_LastTimePosition));
            li.haveRead = row.getLong(row.getColumnIndex(ISQLConstants.COL_haveRead));
        }
        return li;
    }
    public static void saveItem(@NonNull AudioItem li)
    {
        ContentValues values = new ContentValues();
        checkFile (li);
        values.put(COL_FullName, li.FullName);
        values.put(COL_Content, li.Content);
        values.put(COL_FileSize, li.FileSize);
        values.put(COL_isExists, li.isExists);
        values.put(COL_LastTimePosition, li.LastTimePosition);
        if (li.id == 0){
            // new item
            long newid = db.insertOrThrow(TABLE_ITEMS, null, values);
            li.id = (int) newid;
            DummyContent.addItem(li);
        } else {
            // old one. Update it.
            db.update(TABLE_ITEMS, values, _ID+"="+Integer.toString(li.id), null);
            DummyContent.updItem(li);
        }
    }
    public static void saveLastPosition(@NonNull AudioItem li)
    {
        ContentValues values = new ContentValues();
        values.put(COL_LastTimePosition, li.LastTimePosition);
        if (li.id > 0){
            // old one. Update it.
            db.update(TABLE_ITEMS, values, _ID+"="+Integer.toString(li.id), null);
            DummyContent.updItem(li);
        }
    }
    public static void saveReadDate(@NonNull AudioItem li)
    {
        ContentValues values = new ContentValues();
        Date now = new Date();
        li.haveRead = now.getTime();
        values.put(COL_haveRead, li.haveRead);
        if (li.id > 0){
            // old one. Update it.
            db.update(TABLE_ITEMS, values, _ID+"="+Integer.toString(li.id), null);
            DummyContent.updItem(li);
        }
    }
    public static boolean checkFileAtDB(@NonNull AudioItem li)
    {
        int id =0;
        StringBuilder stmt = new StringBuilder("SELECT _id FROM ").append(TABLE_ITEMS)
                .append(" WHERE ").append(COL_FullName).append(" LIKE '").append(li.FullName).append("';");
        Cursor Rows = db_readonly.rawQuery(stmt.toString(), null);
        if (Rows != null && Rows.moveToFirst())
        {
            id = Rows.getInt(0); Rows.close();
        }
        return (id==0) ? false : true;
    }
    private static void checkFile (@NonNull AudioItem li)
    {

        File f = null;
        try {
            f = new File(new URI(li.FullName));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (f != null && f.exists() && !f.isFile())
        {
            li.FileSize = f.length();
            li.isExists = 1;
        } else li.isExists = 0;
    }
    public static ArrayList<LabelItem> getLabels(int id_items)
    {
        ArrayList<LabelItem> list = null;
        if (db != null)
        {
            Cursor Rows = db_readonly.rawQuery(getLabelsStatement(id_items), null);
            if (Rows != null)
            {
                //Log.d(TAG, "Rows.getCount()=" +  Rows.getCount());
                if (Rows.moveToFirst())
                {
                    list = new ArrayList<LabelItem>();
                    do {
                        LabelItem li = getLabelItem(Rows, id_items);
                        if (li != null) list.add(li);
                    } while(Rows.moveToNext());
                }
                if (Rows != null) Rows.close();
            }
        }
        return list;
    }
    public static String getLabelsStatement(@NonNull int id_items)
    {
        StringBuilder stmt = new StringBuilder("SELECT * FROM ").append(TABLE_LABELS);
        stmt.append(" WHERE id_items=").append(Integer.toString(id_items));
        stmt.append(" ORDER BY Position ASC");
        Log.d(LOG_TAG, stmt.toString());
        return stmt.toString();
    }
    public static LabelItem getLabelItem(Cursor row, int id_items)
    {
        LabelItem li = null;
        if (row != null && id_items>0)
        {
            if (id_items == row.getInt(row.getColumnIndex(ISQLConstants.COL_ID_ITEMS)) ) {
                li = new LabelItem(id_items);
                li.id = row.getInt( row.getColumnIndex(ISQLConstants._ID));
                li.Title = row.getString(row.getColumnIndex(ISQLConstants.COL_TITLE));
                li.Comment = row.getString(row.getColumnIndex(ISQLConstants.COL_COMMENT));
                li.Position = row.getLong(row.getColumnIndex(ISQLConstants.COL_Position));
            }
        }
        return li;
    }
    public static void saveItemLabel(@NonNull LabelItem li)
    {
        ContentValues values = new ContentValues();
        values.put(COL_ID_ITEMS, li.id_items);
        values.put(COL_Position, li.Position);
        values.put(COL_TITLE, li.Title);
        values.put(COL_COMMENT, li.Comment);
        if (li.id == 0){
            // new item
            long newid = db.insertOrThrow(TABLE_LABELS, null, values);
            li.id = (int) newid;
        } else {
            // old one. Update it.
            db.update(TABLE_LABELS, values, _ID+"="+Integer.toString(li.id), null);
        }
        LabelContent.fillItemsList(li.id_items);
    }
    public static void deleteItem(@NonNull AudioItem li)
    {
        deleteItem(li.id);
        DummyContent.delItem(li);
        ((RecyclerView) ItemListActivity.recyclerView).getAdapter().notifyDataSetChanged();
    }
    public static void deleteItem(int id)
    {
        db.beginTransaction();
        try {
            db.delete(TABLE_LABELS, "id_items=" + Integer.toString(id), null);
            db.delete(TABLE_ITEMS, "_id=" + Integer.toString(id), null);
            db.setTransactionSuccessful();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        db.endTransaction();
    }
    public static void deleteLabel(LabelItem item)
    {
        db.beginTransaction();
        try {
            db.delete(TABLE_LABELS, "_id=" + Integer.toString(item.id), null);
            db.setTransactionSuccessful();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        db.endTransaction();
    }
}
