package ru.rabus.audioreader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Date;
import java.util.Objects;

import ru.rabus.audioreader.Items.LabelItem;
import ru.rabus.audioreader.dummy.DummyContent;
import ru.rabus.audioreader.dummy.LabelContent;

public class BookmarkDetailFragment extends Fragment implements IResponse {
    public static SharedPreferences mSettings;
    static public Fragment me;
    private LabelItem mItem;
    private Context mContext;
    CollapsingToolbarLayout appBarLayout;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        Activity activity = this.getActivity();
        mSettings = activity.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        int id_item = 0, seekposition = 0;
        Bundle b = getActivity().getIntent().getExtras();
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            if (b != null)
            {
                id_item = b.getInt(ARG_ITEM_ID);
            }
        }
        if (getArguments().containsKey(ARG_SEEKPOSITION)) {
            if (b != null)
            {
                seekposition = b.getInt(ARG_SEEKPOSITION);
            }
        }

        if (getArguments().containsKey(ARG_BOOKMARK_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            int id=0;
            if (b != null)
            {
                id = b.getInt(ARG_BOOKMARK_ID);
            }
            //int id = getArguments().getInt(ARG_ITEM_ID);
            mItem = LabelContent.ITEM_MAP.get(id);
            if (mItem == null) {
                mItem = new LabelItem(id_item);
                mItem.Position = seekposition;
            }
            appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

            if (appBarLayout != null && mItem != null) {

                appBarLayout.setTitle(mItem.toString());
            }
        }
        me = this;
    }
/* onCreateView - Система вызывает этот метод при первом отображении пользовательского интерфейса фрагмента на дисплее.
 Для прорисовки пользовательского интерфейса фрагмента следует возвратить из этого метода объект View,
 который является корневым в макете фрагмента.
 Если фрагмент не имеет пользовательского интерфейса, можно возвратить null
*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bookmark_detail, container, false);

        initializeUI(rootView);

        return rootView;
    }
    private void initializeUI( View rootView )
    {
        EditText title = (EditText) rootView.findViewById(R.id.editNameOfBookmark);
        EditText comment = (EditText) rootView.findViewById(R.id.editComment);
        EditText position = (EditText) rootView.findViewById(R.id.editPositionInTime);
        ImageButton saveButton = (ImageButton) rootView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String s = position.getText().toString();
                    String[] part = s.split("\\.");
                    int h=0, m=0, sec=0, ms=0;
                    if (part.length == 2) {
                        ms = Integer.parseInt(part[1]);
                        String[] parth = part[0].split(":");
                        switch (parth.length)
                        {
                            case 3:
                                h = Integer.parseInt(parth[0]);
                                h *= MS_IN_HOUR;
                            case 2:
                                m = Integer.parseInt(parth[1]);
                                m *= MS_IN_MIN;
                            case 1:
                                sec = Integer.parseInt(parth[2]);
                                sec *= MS_IN_SEC;
                                break;
                        }
                    }
                    mItem.Position = h+m+sec+ms; //Integer.parseInt(s);
                    mItem.Title = title.getText().toString();
                    mItem.Comment = comment.getText().toString();
                    // Save
                    DB.saveItemLabel(mItem);
                    // end exit
                    Objects.requireNonNull((RecyclerView) ItemDetailFragment.recyclerView).setAdapter(new ItemDetailActivity.LabelsRecyclerViewAdapter( LabelContent.ITEMS));
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().remove(me).commit();
                    Objects.requireNonNull(getActivity()).finish();

                }
            });
        title.setText(mItem.Title);
        comment.setText(mItem.Comment);
        position.setText(df.format(new Date(mItem.Position)));

        /*title.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (appBarLayout != null ) {
                    appBarLayout.setTitle(title.getText());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });*/
    }
}
