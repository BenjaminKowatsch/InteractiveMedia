package com.media.interactive.cs3.hdm.interactivemedia.views;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.RelativeLayout;

import com.media.interactive.cs3.hdm.interactivemedia.adapter.SplitAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.adapter.UserEmailAdapter;

/**
 * Created by benny on 29.12.17.
 */

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private RecyclerItemTouchHelperListener listener;

    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {

            final View foregroundView = getViewForeground(viewHolder);

            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    private RelativeLayout getViewForeground(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof UserEmailAdapter.SimpleViewHolder) {
            return ((UserEmailAdapter.SimpleViewHolder) viewHolder).viewForeground;
        } else if (viewHolder instanceof SplitAdapter.SimpleViewHolder) {
            return ((SplitAdapter.SimpleViewHolder) viewHolder).viewForeground;
        } else {
            throw new IllegalStateException("Unknown ViewHolder Type");
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        final View foregroundView = getViewForeground(viewHolder);
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = getViewForeground(viewHolder);
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        final View foregroundView = getViewForeground(viewHolder);

        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Disable swipe directions if the current item is a NoSwipeViewHolder
        if (viewHolder instanceof UserEmailAdapter.NoSwipeViewHolder) {
            return 0;
        } else if (viewHolder instanceof SplitAdapter.NoSwipeViewHolder) {
            return 0;
        }
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    public interface RecyclerItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}
