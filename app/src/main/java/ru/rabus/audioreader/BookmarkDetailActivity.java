package ru.rabus.audioreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;

public class BookmarkDetailActivity extends BaseCompactActivity {
    public static int id_item = 0, id_label = 0, position = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

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
            if (b != null){
              if(b.containsKey(ARG_ITEM_ID)) {
                  // Load the dummy content specified by the fragment
                  // arguments. In a real-world scenario, use a Loader
                  // to load content from a content provider.
                  id_item = b.getInt(ARG_ITEM_ID, 0);
              }
              if (b.containsKey(ARG_BOOKMARK_ID)) {
                    id_label = b.getInt(ARG_BOOKMARK_ID, 0);
              }
              if (b.containsKey(ARG_SEEKPOSITION)){
                    position = b.getInt(ARG_SEEKPOSITION, 0);
                }
            }

            Bundle arguments = new Bundle();
            arguments.putInt(ARG_ITEM_ID, id_item);
            arguments.putInt(ARG_BOOKMARK_ID, id_label);
            arguments.putInt(ARG_SEEKPOSITION, ItemDetailFragment.getCurrentPosition());
            BookmarkDetailFragment fragment = new BookmarkDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
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
            NavUtils.navigateUpTo(this, new Intent(this, ItemDetailActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
