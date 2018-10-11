package ru.rabus.audioreader;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.file.StandardWatchEventKinds;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import ru.rabus.audioreader.Items.AudioItem;
import ru.rabus.audioreader.Items.LabelItem;
import ru.rabus.audioreader.dummy.DummyContent;
import ru.rabus.audioreader.dummy.LabelContent;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements IResponse {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    static public Fragment me;
    static private final String LOG_TAG = DB.class.getSimpleName();
    static public View recyclerView;

    private TextView mTextDebug;
    private ScrollView mScrollContainer;
    private SeekBar mSeekbarAudio;
    private boolean mUserIsSeeking = false;
    private int lastposition = 0;
    private static int seekposition = 0;
    public static String lastpositionStr = "", seekpositionStr = "";
    private TextView tvseekposition = null;
    //
    static Button mPlayButton;
    static Spinner spinner;
    static public int currentState;
    PlaybackListener pbl;
    //
    public static SharedPreferences mSettings;

    static public PlayerAdapter mPlayerAdapter = null;

    AlertDialog.Builder ad;
    static AlertDialog.Builder adBookMark;
    public static LabelItem itemLabel;
    private static Context mContext;
    /**
     * The dummy content this fragment is presenting.
     */
    static private AudioItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }
    public static void setSeekPosition(int position) {
        mPlayerAdapter.seekTo(position);
        if (currentState != PlaybackInfoListener.State.PLAYING)
            mPlayerAdapter.play();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        Activity activity = this.getActivity();
        mSettings = activity.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            Bundle b = getActivity().getIntent().getExtras();
            int id = 0;
            if (b != null) {
                id = b.getInt(ARG_ITEM_ID, 0);
            }
            if (id == 0) id = ItemDetailActivity.id_item;
            //int id = getArguments().getInt(ARG_ITEM_ID);
            mItem = setItem(id);
            //ItemDetailActivity.id_item = id;
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

            if (appBarLayout != null && mItem != null) {
                appBarLayout.setTitle(mItem.toString());
            }
        }
        me = this;
        //

    }

    // Для возможности установить значение "снаружи" фрагмента
    public AudioItem setItem(int id) {
        return DummyContent.ITEM_MAP.get(id);
    }

    public static int getCurrentPosition() {
        return seekposition;
    }

    private void initializeUI(View rootView) {

        recyclerView = rootView.findViewById(R.id.label_list);
        assert recyclerView != null;
        ItemDetailActivity.setupRecyclerView((RecyclerView) recyclerView);
        ((RecyclerView) ItemDetailFragment.recyclerView).getAdapter().notifyDataSetChanged();

        tvseekposition = rootView.findViewById(R.id.seekposition_text);
        lastposition = (int) mItem.FileSize;
        // зададим 0-е смещение по GMT
        // для того, чтобы не прибавлялось автоматом (3 часа) смещения во времени по GMT
        df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        lastpositionStr = df.format(new Date(lastposition));

        seekposition = (mItem != null) ? mItem.LastTimePosition : 0;
        seekpositionStr = df.format(new Date(seekposition));

        Button bDeleteButton = (Button) rootView.findViewById(R.id.button_delete);
        bDeleteButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // we need to ask if delete it exactly
                        bDeleteButton.setBackgroundResource(R.mipmap.delete_64_q);
                        ad.show();
                    }
                });
        currentState = PlaybackInfoListener.State.NOT_DEFINED;
        Button backButton = (Button) rootView.findViewById(R.id.button_back_8);

        Button forwardButton = (Button) rootView.findViewById(R.id.button_forward_8);
        spinner = (Spinner) rootView.findViewById(R.id.spinner_sec);
        if (mSettings != null && mSettings.contains(APP_PREFERENCES_SECTOSEEK)) {
            int i = mSettings.getInt(APP_PREFERENCES_SECTOSEEK, 0);
            if (i < 0) i = 0;
            if (i >= spinner.getCount()) i = spinner.getCount() - 1;
            spinner.setSelection(i);
        } else spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt(APP_PREFERENCES_SECTOSEEK, (int) id);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        backButton.setOnClickListener(x -> seekBack());

        forwardButton.setOnClickListener(x -> seekForward());

        mSeekbarAudio = (SeekBar) rootView.findViewById(R.id.seekbar_audio);
        mSeekbarAudio.setMax(lastposition);
        mSeekbarAudio.setProgress(seekposition);
        mSeekbarAudio.setClickable(true);
        mSeekbarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //Log.d(LOG_TAG, "onStopTrackingTouch");
                int position = seekBar.getProgress();
                mPlayerAdapter.seekTo(position);
                seekpositionStr = df.format(new Date(position));
                mUserIsSeeking = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Log.d(LOG_TAG, "onStartTrackingTouch");
                mUserIsSeeking = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mUserIsSeeking = fromUser;
                if (fromUser) {
                    mPlayerAdapter.seekTo(progress);
                }
            }
        });
        mScrollContainer = (ScrollView) rootView.findViewById(R.id.scroll_container);
        mTextDebug = (TextView) rootView.findViewById(R.id.text_debug);

        mPlayButton = (Button) rootView.findViewById(R.id.button_play);

        mPlayButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (currentState == PlaybackInfoListener.State.PLAYING) {
                            mPlayerAdapter.pause();
                        } else mPlayerAdapter.play();
                    }
                });
        //
        ShowSeekPosition();
        //
        ad = new AlertDialog.Builder(mContext);
        ad.setTitle(getString(R.string.deldialog_title));  // заголовок
        ad.setMessage(mItem.toString() + "\n" + getString(R.string.deldialog_message) + "\n(" + mItem.Content + ")"); // сообщение
        ad.setPositiveButton(getString(R.string.delete_button_title_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(mContext, getString(R.string.maybe),
                        Toast.LENGTH_LONG).show();
                DB.deleteItem(mItem);
                restoreDelButtonBackground(rootView);
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().remove(me).commit();
                Objects.requireNonNull(getActivity()).finish();
                //getActivity().getFragmentManager().popBackStack();
            }

        });
        ad.setNegativeButton(getString(R.string.delete_button_title_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(mContext, getString(R.string.rightchoice), Toast.LENGTH_LONG)
                        .show();
                restoreDelButtonBackground(rootView);
            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(mContext, getString(R.string.makenexttime),
                        Toast.LENGTH_LONG).show();
                restoreDelButtonBackground(rootView);
            }
        });
        //------------------ adBookMark
        adBookMark = new AlertDialog.Builder(mContext);
        adBookMark.setTitle(mContext.getString(R.string.delbookmark));  // заголовок
        adBookMark.setPositiveButton(mContext.getString(R.string.delete_button_title_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(mContext, mContext.getString(R.string.maybe),
                        Toast.LENGTH_LONG).show();
                LabelContent.delItem(itemLabel);
                DB.deleteLabel(itemLabel);
                ((RecyclerView) ItemDetailFragment.recyclerView).getAdapter().notifyDataSetChanged();
            }

        });

        adBookMark.setNegativeButton(mContext.getString(R.string.delete_button_title_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(mContext, mContext.getString(R.string.rightchoice), Toast.LENGTH_LONG)
                        .show();
            }
        });
        adBookMark.setCancelable(true);
        adBookMark.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(mContext, mContext.getString(R.string.makenexttime),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    private void restoreDelButtonBackground(View rootView)
    {
        ((Button) rootView.findViewById(R.id.button_delete)).setBackgroundResource(R.mipmap.delete_64);
    }
    public static void deleteBookmarkLabel(LabelItem item)
    {
        itemLabel = item;
        if (itemLabel != null)
        adBookMark.setMessage(mContext.getString(R.string.delbookmark_question) + "\n " + itemLabel.Title + " ?");
        adBookMark.show();
    }
    private void initializePlaybackController() {

        MediaPlayerHolder _MediaPlayerHolder = new MediaPlayerHolder(mContext);
        Log.d(LOG_TAG, "initializePlaybackController: created MediaPlayerHolder");
        // create class with callbacks for abstract class PlaybackInfoListener
        pbl = new PlaybackListener(); // non static
        _MediaPlayerHolder.setPlaybackInfoListener(pbl);
        // static private mPlayerAdapter
        mPlayerAdapter = _MediaPlayerHolder;
        Log.d(LOG_TAG, "initializePlaybackController: MediaPlayerHolder progress callback set");

    }

    public void ShowSeekPosition() {
        tvseekposition.setText("" + seekpositionStr + " (" + lastpositionStr + ")");
    }

    public void seekForward() {
        int millisec = getMillisecundsFromListofsec2();
        if (pbl != null) pbl.onLogUpdated("seekForward millisec:" + millisec);
        if (millisec > 0) mPlayerAdapter.forward(millisec);
    }

    public void seekBack() {
        int millisec = getMillisecundsFromListofsec2();
        if (pbl != null) pbl.onLogUpdated("seekBack millisec:" + millisec);
        if (millisec > 0) mPlayerAdapter.back(millisec);
    }

    private int getMillisecundsFromListofsec2() {
        int rc = 0;

        switch ((int) spinner.getSelectedItemId()) {
            case 0:
                rc = 500;
                break;
            case 1:
                rc = 1000;
                break;
            case 2:
                rc = 2000;
                break;
            case 3:
                rc = 4000;
                break;
            case 4:
                rc = 8000;
                break;
            case 5:
                rc = 16000;
                break;
            case 6:
                rc = 32000;
                break;
            case 7:
                rc = 64000;
                break;
        }

        return rc;
    }

    private int getMillisecundsFromListofsec() {
        int rc = 0;
        switch ((int) spinner.getSelectedItemId()) {
            case 0:
                rc = 500;
                break;
            case 1:
                rc = 1000;
                break;
            case 2:
                rc = 2000;
                break;
            case 3:
                rc = 3000;
                break;
            case 4:
                rc = 5000;
                break;
            case 5:
                rc = 10000;
                break;
            case 6:
                rc = 15000;
                break;
            case 7:
                rc = 25000;
                break;
            case 8:
                rc = 50000;
                break;
        }
        return rc;
    }

    /* onCreateView - Система вызывает этот метод при первом отображении пользовательского интерфейса фрагмента на дисплее.
    Для прорисовки пользовательского интерфейса фрагмента следует возвратить из этого метода объект View,
     который является корневым в макете фрагмента.
     Если фрагмент не имеет пользовательского интерфейса, можно возвратить null
 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        initializeUI(rootView);
        initializePlaybackController();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mItem != null) {
            //Log.d(LOG_TAG, "before LoadMedia(" + mItem.FullName + ")");
            mPlayerAdapter.loadMedia(mItem.FullName, mItem.LastTimePosition);
            //Log.d(LOG_TAG, "LoadMedia(" + mItem.FullName + ")");
            Log.d(LOG_TAG, "onStart: created MediaPlayer");
        }
    }

    @Override
    public void onDetach() {
        mPlayerAdapter.release();
        mPlayerAdapter = null;
        super.onDetach();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // setMediaPlayer();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
// realisation of methods of the abstract class PlaybackInfoListener
// with access to UI objects of
////////////////////////////////////////////////////////////////////////////////////////////////////
//////////                          PlaybackListener                               /////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
    public class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onDurationChanged(int duration) {
            lastposition = duration;
            mSeekbarAudio.setMax(duration);
            lastpositionStr = df.format(new Date(lastposition));

            Log.d(LOG_TAG, String.format("setPlaybackDuration: setMax(%d)", duration));
        }

        @Override
        public void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                seekpositionStr = df.format(new Date(position));
                mSeekbarAudio.setProgress(position, true);
                Log.d(LOG_TAG, String.format("setPlaybackPosition: setProgress(%d)", position));
                seekposition = position;
                mItem.LastTimePosition = position;
                DB.saveLastPosition(mItem);
                ShowSeekPosition();
            }
        }

        @Override
        public void onStateChanged(@State int state) {
            String stateToString = PlaybackInfoListener.convertStateToString(state);
            onLogUpdated(String.format("onStateChanged(%s)", stateToString));
            Resources myResources = getResources();
            Drawable myIcon = myResources.getDrawable(R.mipmap.bigplay_196, null); // получим значок
            currentState = state;
            switch (state) {
                case State.PLAYING:
                    mPlayButton.setContentDescription(getString(R.string.pause));
                    myIcon = myResources.getDrawable(R.mipmap.pause_96, null); // получим значок
                    break;
                case State.PAUSED:
                    mPlayButton.setContentDescription(getString(R.string.play));
                    break;
                case State.RESET:
                    mPlayButton.setContentDescription(getString(R.string.play));
                    break;
            }
            mPlayButton.setBackground(myIcon);
        }

        @Override
        public void onPlaybackCompleted() {
            ((MediaPlayerHolder) mPlayerAdapter).onPlaybackCompleted();
            DB.saveReadDate(mItem);
            mPlayerAdapter.reset();
            onLogUpdated("onPlaybackCompleted - reset");
        }

        @Override
        public void onLogUpdated(String message) {
            if (mTextDebug != null) {
                mTextDebug.append(message);
                mTextDebug.append("\n");
                // Moves the scrollContainer focus to the end.
                mScrollContainer.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                mScrollContainer.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
            }
        }
    }
}