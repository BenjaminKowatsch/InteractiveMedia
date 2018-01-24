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

    /**
     * The listener.
     */
    private RecyclerItemTouchHelperListener listener;

    /**
     * Instantiates a new recycler item touch helper.
     *
     * @param dragDirs  the drag dirs
     * @param swipeDirs the swipe dirs
     * @param listener  the listener
     */
    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    /**
     * On move.
     *
     * @param recyclerView the recycler view
     * @param viewHolder   the view holder
     * @param target       the target
     * @return true, if successful
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    /**
     * On selected changed.
     *
     * @param viewHolder  the view holder
     * @param actionState the action state
     */
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {

            final View foregroundView = getViewForeground(viewHolder);

            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    /**
     * Gets the view foreground.
     *
     * @param viewHolder the view holder
     * @return the view foreground
     */
    private RelativeLayout getViewForeground(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof UserEmailAdapter.SimpleViewHolder) {
            return ((UserEmailAdapter.SimpleViewHolder) viewHolder).viewForeground;
        } else if (viewHolder instanceof SplitAdapter.SimpleViewHolder) {
            return ((SplitAdapter.SimpleViewHolder) viewHolder).viewForeground;
        } else {
            throw new IllegalStateException("Unknown ViewHolder Type");
        }
    }

    /**
     * On child draw over.
     *
     * @param c                 the c
     * @param recyclerView      the recycler view
     * @param viewHolder        the view holder
     * @param dx                the d X
     * @param dy                the d Y
     * @param actionState       the action state
     * @param isCurrentlyActive the is currently active
     */
    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dx, float dy,
                                int actionState, boolean isCurrentlyActive) {
        final View foregroundView = getViewForeground(viewHolder);
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dx, dy,
            actionState, isCurrentlyActive);
    }

    /**
     * Clear view.
     *
     * @param recyclerView the recycler view
     * @param viewHolder   the view holder
     */
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = getViewForeground(viewHolder);
        getDefaultUIUtil().clearView(foregroundView);
    }

    /**
     * On child draw.
     *
     * @param c                 the c
     * @param recyclerView      the recycler view
     * @param viewHolder        the view holder
     * @param dx                the delta X
     * @param dy                the delta Y
     * @param actionState       the action state
     * @param isCurrentlyActive the is currently active
     */
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dx, float dy,
                            int actionState, boolean isCurrentlyActive) {
        final View foregroundView = getViewForeground(viewHolder);

        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dx, dy,
            actionState, isCurrentlyActive);
    }

    /**
     * On swiped.
     *
     * @param viewHolder the view holder
     * @param direction  the direction
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    /**
     * Gets the swipe dirs.
     *
     * @param recyclerView the recycler view
     * @param viewHolder   the view holder
     * @return the swipe dirs
     */
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

    /**
     * Convert to absolute direction.
     *
     * @param flags           the flags
     * @param layoutDirection the layout direction
     * @return the int
     */
    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    /**
     * The listener interface for receiving recyclerItemTouchHelper events.
     * The class that is interested in processing a recyclerItemTouchHelper
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addRecyclerItemTouchHelperListener</code> method. When
     * the recyclerItemTouchHelper event occurs, that object's appropriate
     * method is invoked.
     *
     * @see RecyclerItemTouchHelperEvent
     */
    public interface RecyclerItemTouchHelperListener {

        /**
         * On swiped.
         *
         * @param viewHolder the view holder
         * @param direction  the direction
         * @param position   the position
         */
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}
