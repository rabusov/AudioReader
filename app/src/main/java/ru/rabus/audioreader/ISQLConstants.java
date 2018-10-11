package ru.rabus.audioreader;
import android.provider.BaseColumns;

/**
 * Created by Сергей on 21.01.2018.
 */

public interface ISQLConstants extends BaseColumns{
    public static final String LOG_TAG = "SQLSTMT";
    //public static final String CREATE_TABLE_PHRASE = "CREATE TABLE ";
    //public static final String SELECT_FROM_PHRASE = "SELECT * FROM ";
    //public static final int CREATE_TABLE_LEN = CREATE_TABLE_PHRASE.length(); /// 13;
    //public static final int SELECT_FROM_LEN = SELECT_FROM_PHRASE.length();

    static public final String DATABASE_NAME = "AudioItems.db";
    static public final int DATABASE_VERSION = 2; // Used to correct DB into onUpgrade method
    //
    public static final String TABLE_ITEMS = "Items";
    // returned by server
    public static final String COL_FullName = "FullName";
    public static final String COL_Content  = "Content";
    public static final String COL_FileSize = "FileSize";
    public static final String COL_isExists = "isExists";
    public static final String COL_LastTimePosition = "LastTimePosition";
    //
    public static final String TABLE_LABELS = "ItemLabels";
    //
    public static final String COL_ID_ITEMS = "id_Items";
    public static final String COL_Position = "Position";
    public static final String COL_TITLE    = "Title";
    public static final String COL_COMMENT  = "Comment";
    //
    // for version == 2
    public static final String COL_haveRead = "haveRead";
}
