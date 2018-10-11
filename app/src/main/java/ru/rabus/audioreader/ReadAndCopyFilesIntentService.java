package ru.rabus.audioreader;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import ru.rabus.audioreader.Items.AudioItem;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 *
 * https://commonsware.com/blog/2016/08/10/uri-access-lifetime-shorter-than-you-might-think.html
 */
public class ReadAndCopyFilesIntentService extends IntentService {
    static private final String TAG = ReadAndCopyFilesIntentService.class.getSimpleName();
    private Handler handler;
    private AudioItem item;
    private static final String TEMP_AUDIO_NAME = "tempaudio";
    private PlayerAdapter mPlayerAdapter = null;
    private boolean mUserIsSeeking = false;
    private Context mContext;
    //private DB dbase = null;
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "ru.rabus.audioreader.action.FOO";
    private static final String ACTION_BAZ = "ru.rabus.audioreader.action.BAZ";
    private static final String ACTION_GETITEM = "ru.rabus.audioreader.action.getitem";
    //
    private static final String ACTION_PLAY = "audioreader.action.play";
    private static final String ACTION_PAUSE = "audioreader.action.pause";
    private static final String ACTION_RESET = "audioreader.action.reset";
    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "ru.rabus.audioreader.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "ru.rabus.audioreader.extra.PARAM2";
    private static final String EXTRA_ITEM_ID = "ru.rabus.audioreader.extra.itemid";

    public ReadAndCopyFilesIntentService() {
        super("ReadAndCopyFilesIntentService");
        //dbase = new DB(getApplicationContext());

    }
    private Runnable setContext()
    {
        return new Runnable() {
            @Override
            public void run() {
                mContext = getApplicationContext();
            }
        };
    }
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        handler = new Handler();
        handler.post(setContext());
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ReadAndCopyFilesIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ReadAndCopyFilesIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }
    public static void startActionGetItem(Context context, int itemid) {
        Intent intent = new Intent(context, ReadAndCopyFilesIntentService.class);
        intent.setAction(ACTION_GETITEM);
        intent.putExtra(EXTRA_ITEM_ID, itemid);
        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION);
        context.startService(intent);
    }
    public static void PlayAction(Context context) {
        Intent intent = new Intent(context, ReadAndCopyFilesIntentService.class);
        intent.setAction(ACTION_PLAY);
        context.startService(intent);
    }
    public static void PauseAction(Context context) {
        Intent intent = new Intent(context, ReadAndCopyFilesIntentService.class);
        intent.setAction(ACTION_PAUSE);
        context.startService(intent);
    }
    public static void ResetAction(Context context) {
        Intent intent = new Intent(context, ReadAndCopyFilesIntentService.class);
        intent.setAction(ACTION_RESET);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            } else if (ACTION_GETITEM.equals(action)){
                //handleActionGetItem(Integer.getInteger(intent.getStringExtra(EXTRA_ITEM_ID)));
                handleActionGetItem(intent.getIntExtra(EXTRA_ITEM_ID, 0));
            } else if (ACTION_PLAY.equals(action)){
                mPlayerAdapter.play();
            }else if (ACTION_PAUSE.equals(action)){
                mPlayerAdapter.pause();
            }else if (ACTION_RESET.equals(action)){
                mPlayerAdapter.reset();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private void handleActionGetItem(Integer itemid) {
        // TODO: Handle action Baz
        //mContext = getApplicationContext();
        MediaPlayerHolder mMediaPlayerHolder = new MediaPlayerHolder(mContext);
        Log.d(TAG, "ReadAndCopyFilesIntentService: created MediaPlayerHolder");
        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
        mPlayerAdapter = mMediaPlayerHolder;

        item = DB.getItem(itemid);
        if (item != null)
        {
            //File af = getTempFile(getApplicationContext());
            //af.deleteOnExit();
            try {
                mPlayerAdapter.loadMedia(new URI(item.FullName), item.LastTimePosition);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
    private static File getTempFile(Context context) {
        File audioFile = new File(context.getExternalCacheDir(), TEMP_AUDIO_NAME);
        audioFile.getParentFile().mkdirs();
        return audioFile;
    }
    public class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onDurationChanged(int duration) {
            //seekBar.setMax(duration);
            Log.d(TAG, String.format("onDurationChanged: duration(%d)", duration));
        }

        @Override
        public void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                //seekBar.setProgress(position, true);
                Log.d(TAG, String.format("onPositionChanged: position(%d)", position));
            }
        }

        @Override
        public void onStateChanged(@State int state) {
            String stateToString = PlaybackInfoListener.convertStateToString(state);
            Log.d(TAG, String.format("onStateChanged(%s)", stateToString));
        }

        @Override
        public void onPlaybackCompleted() {
        }

        @Override
        public void onLogUpdated(String message) {
        }
    }
}
