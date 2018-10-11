package ru.rabus.audioreader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.rabus.audioreader.Items.AudioItem;
import ru.rabus.audioreader.Items.LabelItem;

public class LabelsRecyclerViewAdapter
        extends RecyclerView.Adapter<LabelsRecyclerViewAdapter.ViewHolder>
        implements IResponse
{
    private final Fragment mParentActivity;
    private final List<LabelItem> mValues;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LabelItem item = (LabelItem) view.getTag();
            Context context = view.getContext();
            Intent intent = new Intent(context, BookmarkDetailFragment.class);
            intent.putExtra(ARG_ITEM_ID, item.id_items);
            intent.putExtra(ARG_BOOKMARK_ID, item.id);
            intent.putExtra(ARG_SEEKPOSITION, item.Position);
            context.startActivity(intent);
        }
    };

    LabelsRecyclerViewAdapter(Fragment parent,
                                  List<LabelItem> items
                                  ) {;
        mValues = items;
        mParentActivity = parent;

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
        holder.mIdView.setText(Integer.toString(mValues.get(position).id));
        holder.mContentView.setText(mValues.get(position).toString());

        holder.itemView.setTag(mValues.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mIdView;
        final TextView mContentView;

        ViewHolder(View view) {
            super(view);
            mIdView = (TextView) view.findViewById(R.id.id_text);
            mContentView = (TextView) view.findViewById(R.id.content);
        }
    }
}