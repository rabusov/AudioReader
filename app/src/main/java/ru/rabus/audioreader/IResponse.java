package ru.rabus.audioreader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Сергей on 21.01.2018.
 */

public interface IResponse {
    public static final String SECURITY_ACTION = "com.rabus.audioreader.SECURITY";
    public static final String SECURITY_ACTION_FILL_START = "com.rabus.audioreader.security.FILLSTART";
    public static final String SECURITY_ACTION_FILL_READY = "com.rabus.http.audioreader.FILLREADY";
    //
    public static final String RESPONSE_TITLE = "Response";
    public static final String RESPONSE_ERROR_TITLE = "Error";
    public static final String RESPONSE_OK = "OK";
    public static final String RESPONSE_MESSAGE = "MESSAGE";
    public static final String RESPONSE_BAD = "BAD";
    public static final String RESPONSE_NODATA = "No Data";
    public static final String RESPONSE_BADPARSE = "Parser is null";

    public static final String ARG_BOOKMARK_ID = "bookmark_id";
    public static final String APP_PREFERENCES = "AudioReaderSettings";
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_SEEKPOSITION = "seekposition";

    public static final String APP_PREFERENCES_SECTOSEEK = "spinnersec";

    public static final int PERMISSION_REQUEST_CODE = 12;
    public static final int RESPONSE_BROWSE_FILE_CODE = 1960;
    public static final int RESPONSE_BOOKMARK_LIST = 1962;
    public static final int RESPONSE_BOOKMARK_ITEM = 1989;

    public static DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
    public static int MS_IN_SEC = 1000;
    public static int MS_IN_MIN = MS_IN_SEC*60;
    public static int MS_IN_HOUR = MS_IN_MIN*60;
}
