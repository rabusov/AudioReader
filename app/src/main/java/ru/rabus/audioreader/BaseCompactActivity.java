package ru.rabus.audioreader;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import junit.framework.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Intent;
/**
 * Created by Сергей on 24.01.2018.
 */

public class BaseCompactActivity extends AppCompatActivity implements IResponse {
    static private final String LOG_TAG = DB.class.getSimpleName();
    public DB dbase = null;
    public View.OnClickListener mOnClickListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbase = new DB(getApplicationContext());
    }
    public void ToastAMessage(int res)
    {
        ToastAMessage(getResources().getString(res));
    }

    public void ToastAMessage(String msg)
    {
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }
    /*
    * http://qaru.site/questions/98399/android-howto-parse-url-string-with-spaces-to-uri-object
    * */
    public static String encode(String uriString) {
        if (TextUtils.isEmpty(uriString)) {
            Assert.fail("Uri string cannot be empty!");
            return uriString;
        }
        // getQueryParameterNames is not exist then cannot iterate on queries
        if (Build.VERSION.SDK_INT < 11) {
            return uriString;
        }

        // Check if uri has valid characters
        // See https://tools.ietf.org/html/rfc3986
        Pattern allowedUrlCharacters = Pattern.compile("([A-Za-z0-9_.~:/?\\#\\[\\]@!$&'()*+,;" +
                "=-]|%[0-9a-fA-F]{2})+");
        Matcher matcher = allowedUrlCharacters.matcher(uriString);
        String validUri = null;
        if (matcher.find()) {
            validUri = matcher.group();
        }
        if (TextUtils.isEmpty(validUri) || uriString.length() == validUri.length()) {
            return uriString;
        }

        // The uriString is not encoded. Then recreate the uri and encode it this time
        Uri uri = Uri.parse(uriString);
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(uri.getScheme())
                .authority(uri.getAuthority());
        for (String path : uri.getPathSegments()) {
            uriBuilder.appendPath(path);
        }
        for (String key : uri.getQueryParameterNames()) {
            uriBuilder.appendQueryParameter(key, uri.getQueryParameter(key));
        }
        String correctUrl = uriBuilder.build().toString();
        return correctUrl;
    }
    public String getPathFromIntentData(Intent data)
    {
        Uri chosenImageUri = data.getData();
        String[] projection = { MediaStore.Audio.Media.DATA };

        Cursor cursor = getContentResolver().query(chosenImageUri, projection, null, null, null);
        cursor.moveToFirst();

        //Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(cursor));

        int columnIndex = cursor.getColumnIndex(projection[0]);
        String path = "file://" +encode(cursor.getString(columnIndex));
        cursor.close();

        return path;
    }

    public static String getFileNameFromPath(String path)
    {
        Uri uri = Uri.parse(path);
        return uri.getLastPathSegment();
    }
}
