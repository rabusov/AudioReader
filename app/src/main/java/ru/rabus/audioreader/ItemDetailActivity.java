package ru.rabus.audioreader;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import ru.rabus.audioreader.Items.LabelItem;
import ru.rabus.audioreader.dummy.DummyContent;
import ru.rabus.audioreader.dummy.LabelContent;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static ru.rabus.audioreader.ItemDetailFragment.recyclerView;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 */
public class ItemDetailActivity extends BaseCompactActivity {
    static private final String LOG_TAG = DB.class.getSimpleName();
    static public int id_item = 0;
    static private EditText inputPos;
    static private EditText inputTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mContext = getApplicationContext();
        //mApp = getApplication();
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        inputPos = new EditText(getApplicationContext());
        inputTitle = new EditText(getApplicationContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, getString(R.string.BookmarksList)
                            , Snackbar.LENGTH_LONG).setAction(getString(R.string.Bookmarks), mOnClickListener).show();
                }
            });
            // mOnClickListener - defined at BaseCompactActivity
            mOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Добавление закладки
                    //dlg.show();
                    Context context = v.getContext();
                    Intent intent = new Intent(context, BookmarkDetailActivity.class);
                    intent.putExtra(ARG_BOOKMARK_ID, 0);
                    intent.putExtra(ARG_ITEM_ID, id_item);
                    intent.putExtra(ARG_SEEKPOSITION, ItemDetailFragment.getCurrentPosition());
                    startActivityForResult(intent, RESPONSE_BOOKMARK_ITEM);
                }
            };
        }
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle b = getIntent().getExtras();
            if (b != null && b.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                id_item=b.getInt(ARG_ITEM_ID, 0);
            } else {

            }

            Bundle arguments = new Bundle();
            arguments.putInt(ARG_ITEM_ID, id_item);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        } else {
            id_item = savedInstanceState.getInt(ARG_ITEM_ID);
            Log.d(LOG_TAG, "onCreate id_item:" + id_item);
        }
        android.support.v4.widget.NestedScrollView nsv = findViewById(R.id.item_detail_container);
        if (nsv!= null)
            nsv.fullScroll (View.FOCUS_UP);
        LinearLayout ll = findViewById(R.id.detail_container);
        if (ll != null)
            ll.scrollTo(0,0);
    }
    /*
    * Допустим пользователь слушает музыку в наушниках и выдергивает их.
    * Если эту ситуацию специально не обработать, звук переключится на динамик телефона
    * и его услышат все окружающие. Было бы хорошо в этом случае встать на паузу.
    * Для этого в Android есть специальный бродкаст AudioManager.ACTION_AUDIO_BECOMING_NOISY
    * */
    final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (ItemDetailFragment.mPlayerAdapter != null)
                    if (ItemDetailFragment.currentState == PlaybackInfoListener.State.PLAYING)
                        ItemDetailFragment.mPlayerAdapter.pause();
            }
        }
    };
    //Подключаем на старте
    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(
                becomingNoisyReceiver,
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        );
    }
    // Убираем при остановке
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(becomingNoisyReceiver);
    }
    static public void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        LabelContent.fillItemsList(id_item);
        if (recyclerView != null)
            recyclerView.setAdapter(new ItemDetailActivity.LabelsRecyclerViewAdapter(LabelContent.ITEMS));

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case RESPONSE_BOOKMARK_ITEM:
                if (resultCode == RESULT_OK)
                {
                    // bookmark_item (labelitem) was added
                    // we need notify recyclerView
                    ((RecyclerView) ItemDetailFragment.recyclerView).getAdapter().notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, ItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
/******************************************************************************
* LabelsRecyclerViewAdapter LabelsRecyclerViewAdapter LabelsRecyclerViewAdapter
*******************************************************************************/

    static public class LabelsRecyclerViewAdapter
            extends RecyclerView.Adapter<ItemDetailActivity.LabelsRecyclerViewAdapter.ViewHolder>
            implements IResponse {
        private static List<LabelItem> mValues;
        private static View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LabelItem item = (LabelItem) view.getTag();
                Context context = view.getContext();
                Intent intent = new Intent(context, BookmarkDetailActivity.class);
                intent.putExtra(ARG_ITEM_ID, item.id_items);
                intent.putExtra(ARG_BOOKMARK_ID, item.id);
                intent.putExtra(ARG_SEEKPOSITION, item.Position);
                context.startActivity(intent);
            }
        };

        private final View.OnClickListener mOnClickListenerPlay = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LabelItem item = (LabelItem) view.getTag();
                ItemDetailFragment.setSeekPosition((int)item.Position);
            }
        };
        private final View.OnClickListener mOnClickListenerDelete = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LabelItem item = (LabelItem) view.getTag();

                ItemDetailFragment.deleteBookmarkLabel(item);

            }
        };


        LabelsRecyclerViewAdapter(List<LabelItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.label_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final LabelsRecyclerViewAdapter.ViewHolder holder, int position) {
            //Log.e(LOG_TAG, "Position: " + Integer.toString(position));
            //Log.e(LOG_TAG, Integer.toString(mValues.get(position).id));
            if (mValues!=null) {
                holder.mIdView.setText(mValues.get(position).Title);
                //holder.mContentView.setText(mValues.get(position).toString());

                holder.itemView.setTag(mValues.get(position));
                holder.itemView.setOnClickListener(mOnClickListener);

                holder.playbutton.setTag(mValues.get(position));
                holder.playbutton.setOnClickListener(mOnClickListenerPlay);
                holder.delbutton.setTag(mValues.get(position));
                holder.delbutton.setOnClickListener(mOnClickListenerDelete);
            }

        }

        @Override
        public int getItemCount() {

            return (mValues==null) ? 0 : mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final android.support.v7.widget.AppCompatImageButton playbutton;
            final android.support.v7.widget.AppCompatImageButton delbutton;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                playbutton = (android.support.v7.widget.AppCompatImageButton)view.findViewById(R.id.id_playbutton);
                delbutton =  (android.support.v7.widget.AppCompatImageButton)view.findViewById(R.id.id_deletebutton);
            }
        }
    }

}
