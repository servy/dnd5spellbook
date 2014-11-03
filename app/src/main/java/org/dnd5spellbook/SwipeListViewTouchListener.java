package org.dnd5spellbook;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * {@link android.view.View.OnTouchListener} for {@link android.widget.ListView} that
 * provides "swipe to the left" and "swipe to the right" actions for list items.
 * Based on https://github.com/romannurik/android-swipetodismiss
 */
public class SwipeListViewTouchListener implements View.OnTouchListener, AbsListView.OnScrollListener {
    // Cached ViewConfiguration and system-wide constant values
    private int slop;
    private int minFlingVelocity;
    private int maxFlingVelocity;
    private long animationTime;

    // Main properties
    private final ListView listView;
    private OnSwipeCallback callback;
    private int viewWidth = 0;

    protected int getViewWidth() {
        if (viewWidth == 0) {
            viewWidth = listView.getWidth();
        }
        return viewWidth;
    }

    // Transient properties
    private float downX;
    private boolean swiping;
    private VelocityTracker velocityTracker;
    private int downPosition;
    private View downView;
    private boolean paused;

    /**
     * The callback interface used by {@link SwipeListViewTouchListener} to inform its client
     * about a successful swipe of one or more list item positions.
     */
    public interface OnSwipeCallback {
        /**
         * Called when the user has swiped the list item to the left.
         *
         * @param listView    The originating {@link ListView}.
         * @param position    The position of the item that was swiped
         */
        void onSwipeLeft(ListView listView, int position);

        /**
         * Called when the user has swiped the list item to the right.
         *
         * @param listView    The originating {@link ListView}.
         * @param position    The position of the item that was swiped
         */
        void onSwipeRight(ListView listView, int position);
    }

    /**
     * Constructs a new swipe-to-action touch listener for the given list view.
     * Note: {@link ListView#setOnTouchListener} and {@link ListView#setOnScrollListener}
     * will be called to allow this class handle the events. If you need custom
     * listeners you should bind them after calling the constructor on this class
     * and then delegate all touch and scroll events through this listener.
     *
     * @param listView The list view whose items should be swipeable.
     * @param callback The callback to trigger when the user has swiped
     *                 one or more list items.
     */
    public SwipeListViewTouchListener(ListView listView, OnSwipeCallback callback) {
        ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
        this.slop = vc.getScaledTouchSlop();
        this.minFlingVelocity = vc.getScaledMinimumFlingVelocity();
        this.maxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        this.animationTime = listView.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        this.listView = listView;
        this.callback = callback;

        listView.setOnTouchListener(this);
        listView.setOnScrollListener(this);
    }

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
     *
     * @param enabled Whether or not to watch for gestures.
     */
    public void setEnabled(boolean enabled) {
        paused = !enabled;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            {
                if (paused) {
                    return false;
                }

                downView = findTouchedWidget(motionEvent);

                if (downView != null) {
                    downX = motionEvent.getRawX();
                    downPosition = listView.getPositionForView(downView);

                    velocityTracker = VelocityTracker.obtain();
                    velocityTracker.addMovement(motionEvent);
                }
                view.onTouchEvent(motionEvent);
                return true;
            }

            case MotionEvent.ACTION_UP:
            {
                if (velocityTracker == null) {
                    break;
                }

                float deltaX = motionEvent.getRawX() - downX;
                velocityTracker.addMovement(motionEvent);
                velocityTracker.computeCurrentVelocity(500); // 1000 by defaut but it was too much
                float velocityX = Math.abs(velocityTracker.getXVelocity());
                float velocityY = Math.abs(velocityTracker.getYVelocity());
                boolean swipe = false;
                boolean swipeRight = false;

                if (Math.abs(deltaX) > getViewWidth() / 2) {
                    swipe = true;
                    swipeRight = deltaX > 0;
                } else if (minFlingVelocity <= velocityX && velocityX <= maxFlingVelocity && velocityY < velocityX) {
                    swipe = true;
                    swipeRight = velocityTracker.getXVelocity() > 0;
                }
                if (swipe) {
                    // sufficient swipe value
                    final View downView = this.downView; // downView gets null'd before animation ends
                    final int downPosition = this.downPosition;
                    final boolean toTheRight = swipeRight;

                    // TODO: animate background with TransitionDrawable
                    downView.setX(0);
                    downView.setAlpha(1);

                    if (toTheRight) {
                        callback.onSwipeRight(listView, downPosition);
                    }
                    else
                        callback.onSwipeLeft(listView, downPosition);
                } else {
                    // cancel
                    downView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(animationTime)
                            .setListener(null);
                }
                velocityTracker.recycle();
                velocityTracker = null;
                downX = 0;
                downView = null;
                downPosition = ListView.INVALID_POSITION;
                swiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE:
            {
                if (velocityTracker == null || paused) {
                    break;
                }

                velocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - downX;
                if (Math.abs(deltaX) > slop) {
                    swiping = true;
                    listView.requestDisallowInterceptTouchEvent(true);

                    // Cancel ListView's touch (un-highlighting the item)
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    listView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                if (swiping) {
                    downView.setTranslationX(deltaX);

                    // TODO: animate background?
                    // downView.setAlpha(Math.max(0f, Math.min(1f,
                    //      1f - 2f * Math.abs(deltaX) / getViewWidth())));
                    return true;
                }
                break;
            }
        }
        return false;
    }

    /**
     * Finds the child view that was touched (performs a hit test)
     * @param motionEvent the motionEvent with coordinates to find the child view at
     */
    private View findTouchedWidget(MotionEvent motionEvent) {
        Rect rect = new Rect();
        int childCount = listView.getChildCount();
        int[] listViewCoords = new int[2];
        listView.getLocationOnScreen(listViewCoords);
        int x = (int) motionEvent.getRawX() - listViewCoords[0];
        int y = (int) motionEvent.getRawY() - listViewCoords[1];
        for (int i = 0; i < childCount; i++) {
            View child = listView.getChildAt(i);
            child.getHitRect(rect);
            if (rect.contains(x, y)) {
                return child;
            }
        }
        return null;
    }
}
