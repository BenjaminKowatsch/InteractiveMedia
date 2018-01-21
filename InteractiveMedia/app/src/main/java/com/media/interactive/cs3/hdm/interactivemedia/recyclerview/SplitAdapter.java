package com.media.interactive.cs3.hdm.interactivemedia.recyclerview;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.EvenSplit;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.Split;

import java.util.ArrayList;
import java.util.List;

public class SplitAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private final List<SplitHolder> list;

    private final static int NO_SWIPE_ITEM_TYPE = 0;
    private final static int SWIPE_ITEM_TYPE = 1;

    public SplitAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        // Choose last item to be not swipeable
        if (position == getItemCount() - 1) {
            return NO_SWIPE_ITEM_TYPE;
        }
        return SWIPE_ITEM_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == NO_SWIPE_ITEM_TYPE) {
            final View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.no_swipe_recycler_view_item, parent, false);
            return new NoSwipeViewHolder(itemView);
        } else {
            final View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.simple_recycler_view_item, parent, false);
            return new SimpleViewHolder(itemView);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final SplitHolder split = list.get(position);
        if (holder.getItemViewType() == NO_SWIPE_ITEM_TYPE) {
            final NoSwipeViewHolder rootMyViewHolder = (NoSwipeViewHolder) holder;
            rootMyViewHolder.name.setText(split.toString());
        } else {
            final SimpleViewHolder simpleViewHolder = (SimpleViewHolder) holder;
            simpleViewHolder.name.setText(split.toString());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public Split buildSplit() {
        return new EvenSplit();
    }

    public void add(Split split) {
        list.add(0, new SplitHolder(split, null));
    }

    public final class SplitHolder {
        private Split split;
        private Transaction source;

        public SplitHolder(Split split, Transaction source) {
            this.split = split;
            this.source = source;
        }
    }

    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public RelativeLayout viewBackground, viewForeground;

        public SimpleViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.recycler_view_text);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);
        }
    }

    public class NoSwipeViewHolder extends SplitAdapter.SimpleViewHolder {
        public NoSwipeViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.recycler_view_text);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);
        }

    }
}