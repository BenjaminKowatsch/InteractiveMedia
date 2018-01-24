package com.media.interactive.cs3.hdm.interactivemedia.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.ConstantDeduction;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.Split;

import java.util.ArrayList;
import java.util.List;

import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.CURRENCY_FORMAT;

public class SplitAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private final List<Split> list;
    private final List<User> userList;

    private final static int NO_SWIPE_ITEM_TYPE = 0;
    private final static int SWIPE_ITEM_TYPE = 1;

    public SplitAdapter(Context context, List<Split> list, List<User> usersInGroup) {
        this.context = context;
        this.list = list;
        this.userList = usersInGroup;
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
                    .inflate(R.layout.no_swipe_split_recycler_view_item, parent, false);
            return new NoSwipeViewHolder(itemView);
        } else {
            final View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.split_recycler_view_item, parent, false);
            return new SimpleViewHolder(itemView);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Split split = list.get(position);
        if (holder.getItemViewType() == NO_SWIPE_ITEM_TYPE) {
            final NoSwipeViewHolder rootMyViewHolder = (NoSwipeViewHolder) holder;
            rootMyViewHolder.name.setText(createSplitText(position, split));
        } else {
            final SimpleViewHolder simpleViewHolder = (SimpleViewHolder) holder;
            simpleViewHolder.name.setText(createSplitText(position, split));
        }
    }

    @NonNull
    private String createSplitText(int position, Split split) {
        if (list.size() == 1 && position == 0) {
            // even split is always in list & not removable
            return "Split evenly between everyone in the group";
        } else if (list.size() > 1) {
            if (position < list.size() - 1) {
                final ConstantDeduction constantDeduction = (ConstantDeduction) split;
                final String paymentInfo = findUserNameFor(constantDeduction.getToUserId())
                        + " pays " + CURRENCY_FORMAT.format(constantDeduction.getAmount());
                if (position == 0) {
                    return "First " + paymentInfo;
                } else {
                    return "then " + paymentInfo;
                }
            } else {
                return "then the remainder is split between everyone in the group.";
            }
        } else {
            return "Error!?";
        }
    }

    private String findUserNameFor(String toUserId) {
        for (User user : userList) {
            if (user.getUserId().equals(toUserId)) {
                return user.getUsername();
            }
        }
        return toUserId;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void removeSplit(int position) {
        list.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void restoreSplit(Split split, int position) {
        list.add(position, split);
        // notify item added by position
        notifyItemInserted(position);
    }

    public void update(List<Split> splitList) {
        final List<Split> copy = new ArrayList<>();
        copy.addAll(splitList);
        list.clear();
        list.addAll(copy);
        notifyDataSetChanged();
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