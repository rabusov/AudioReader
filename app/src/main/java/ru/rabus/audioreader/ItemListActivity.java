package ru.rabus.audioreader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import ru.rabus.audioreader.Items.AudioItem;
import ru.rabus.audioreader.dummy.DummyContent;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import android.content.pm.Signature;
import java.util.List;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static ru.rabus.audioreader.DB.checkFileAtDB;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
//
// https://commonsware.com/blog/2016/08/10/uri-access-lifetime-shorter-than-you-might-think.html
//
public class ItemListActivity extends BaseCompactActivity {
    static private final String LOG_TAG = DB.class.getSimpleName();
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    static public View recyclerView;
    private CheckPermission CP = null;
    static final private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10210;
    static final private int MY_PERMISSIONS_REQUEST_WRITE_USER_DICTIONARY = 10310;
    static final private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 10220;
    static final private int MY_PERMISSIONS_REQUEST_READ_USER_DICTIONARY = 10320;
    static final private int MY_PERMISSIONS_REQUEST_SYSTEM_TOOLS = 10110;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // About the right SplashScreen
        // https://habr.com/post/312516/
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
       // dbase.deleteItem(1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // would be added a file
                Snackbar.make(view, R.string.add_audio_file, Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.Browse), mOnClickListener).show();
            }
        });
        // mOnClickListener - defined at BaseCompactActivity
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Handle onclick here;
                Intent audioPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                audioPickerIntent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                audioPickerIntent.setFlags(FLAG_GRANT_WRITE_URI_PERMISSION);
                audioPickerIntent.setType("audio/*");
                //audioPickerIntent.setType("application/ogg");
                //audioPickerIntent.setType("application/x-ogg");
                //audioPickerIntent.setAction(Intent.ACTION_SEARCH);
                //audioPickerIntent.setAction(android.content.Intent.ACTION_PICK);

                //audioPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                //audioPickerIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                /*startActivityForResult(
                        Intent.createChooser(audioPickerIntent, "Select Audio"), RESPONSE_BROWSE_FILE_CODE
                );*/

                startActivityForResult(audioPickerIntent, RESPONSE_BROWSE_FILE_CODE);

// http://qaru.site/questions/1804232/android-picking-a-song-to-play-using-intent
                //audioPickerIntent.setAction(android.content.Intent.ACTION_PICK);
//audioPickerIntent.setData(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);

/// https://ru.stackoverflow.com/questions/539794/Выбор-трека-через-intent
/*
Нашел правильный ACTION для выбора трека, вот код который у меня работает:

Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
intent.addCategory(Intent.CATEGORY_DEFAULT);
Intent fileIntent = Intent.createChooser(intent,"Выбор файла");
startActivityForResult(fileIntent,123);

*/

            }
        };

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        CP = new CheckPermission(this.getApplicationContext(), this);
        if (CP != null) {
            CP.CheckAPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            CP.CheckAPermission(Manifest.permission.READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        // create a Uri for the content provider suggested by the error message
        Uri uri = Uri.parse("content://media/external/");

        // create a package provider string suggested by the error messge.
        String provider = "com.android.providers.media.MediaProvider";
        grantUriPermission(provider, uri, FLAG_GRANT_WRITE_URI_PERMISSION);
        grantUriPermission(provider, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
        askmefirst();
        android.support.v4.widget.NestedScrollView nsv = findViewById(R.id.item_detail_container);
        if (nsv!= null)
        nsv.fullScroll (View.FOCUS_UP);
    }
    private void askmefirst()
    {
        android.content.pm.Signature[] sigs = new Signature[0];
        try {
            sigs = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_SIGNATURES).signatures;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        for (android.content.pm.Signature sig : sigs)
        {
            Log.d(LOG_TAG, "Signature hashcode : " + sig.hashCode());
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (CP != null) {
            CP.CheckAPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            CP.CheckAPermission(Manifest.permission.READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        switch(requestCode)
        {
            case RESPONSE_BROWSE_FILE_CODE:
            {
                if (resultCode == RESULT_OK)
                {
                    File file = null;

                    String path = getPathFromIntentData(data);
                    // проигрывание из контента
                    //http://startandroid.ru/ru/uroki/vse-uroki-spiskom/236-urok-126-media-mediaplayer-audiovideo-pleer-osnovnye-vozmozhnosti.html
                    Uri contentUri = data.getData();
                    Log.d(LOG_TAG, "path: " + path);
                    try {
                        file = new File(new URI(path));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    if (file != null && file.exists()) {
                        Log.d(LOG_TAG, file.getAbsolutePath());
                        AudioItem ai = new AudioItem(path, contentUri.toString(), (long)file.length(), 0, 1, 0);
                        if (!checkFileAtDB(ai)) {
                            dbase.saveItem(ai);
                            //
                            ((RecyclerView) recyclerView).getAdapter().notifyDataSetChanged();
                        } else {
                            ToastAMessage(R.string.AlReadyIsInDB);
                        }
                    }
                }
                break;
            }

        }
        ((RecyclerView) recyclerView).getAdapter().notifyDataSetChanged();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
            case MY_PERMISSIONS_REQUEST_WRITE_USER_DICTIONARY:
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
            case MY_PERMISSIONS_REQUEST_READ_USER_DICTIONARY:
            case MY_PERMISSIONS_REQUEST_SYSTEM_TOOLS:{
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                    {
                        if (permissions[i].contains(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                                grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            /// permission was granted for READ_PHONE_STATE , yay! Do the
                            continue;
                        }
                        if (permissions[i].contains(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                                grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            /// permission was granted for ACCESS_COARSE_LOCATION , yay! Do the
                        }
                    }
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
    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, mTwoPane));
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final List<AudioItem> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioItem item = (AudioItem) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(ARG_ITEM_ID, item.id);
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ARG_ITEM_ID, item.id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(ItemListActivity parent,
                                      List<AudioItem> items,
                                      boolean twoPane) {;
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            //Log.e(LOG_TAG, "Position: " + Integer.toString(position));
            //Log.e(LOG_TAG, Integer.toString(mValues.get(position).id));
            holder.mIdView.setText(Integer.toString(mValues.get(position).id));
            holder.mContentView.setText(mValues.get(position).toString());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }
}
