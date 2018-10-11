package ru.rabus.audioreader;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class CheckPermission
{
    private Activity mActivity;
    private Context mContext;
    public CheckPermission(Context context, Activity activity)
    {
        mActivity = activity;
        mContext = context;
    }
    public void CheckPermissions(String[] permissions, int[] CONSTANTS_OFREGUEST)
    {
        for (int i =0; i < permissions.length; i++)
        {
            CheckAPermission(permissions[i], CONSTANTS_OFREGUEST[i]);
        }
    }
    public void CheckAPermission(String permission, int requestNumber)
    {
        if (ContextCompat.checkSelfPermission(mContext,
                permission)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                    permission)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(mActivity,
                        new String[]{permission},
                        requestNumber);

                // requestNumber is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }
            //
            // Code to continue see at onRequestPermissionsResult
            //
        }
    }

}
