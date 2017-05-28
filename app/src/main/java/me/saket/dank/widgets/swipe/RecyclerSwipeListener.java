package me.saket.dank.widgets.swipe;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import me.saket.dank.utils.SimpleRecyclerViewOnChildAttachStateChangeListener;

/**
 * Not using {@link ItemTouchHelper}, because it only supports "dismissing" items with swipe. There's no concept of them snapping back.
 */
public class RecyclerSwipeListener extends RecyclerView.SimpleOnItemTouchListener {

  private final GestureDetector gestureDetector;
  private boolean isSwiping;
  private SwipeableLayout viewBeingSwiped;

  public RecyclerSwipeListener(RecyclerView recyclerView) {
    gestureDetector = createSwipeGestureDetector(recyclerView);

    // Reset animation if the View gets detached for recycling.
    recyclerView.addOnChildAttachStateChangeListener(new SimpleRecyclerViewOnChildAttachStateChangeListener() {
      @Override
      public void onChildViewDetachedFromWindow(View view) {
        ViewHolderWithSwipeActions viewHolder = (ViewHolderWithSwipeActions) recyclerView.getChildViewHolder(view);
        viewHolder.getSwipeableLayout().setSwipeTranslation(0f);
      }
    });
  }

  private GestureDetector createSwipeGestureDetector(RecyclerView recyclerView) {
    return new GestureDetector(recyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
      @Override
      public boolean onDown(MotionEvent e) {
        // RecyclerView#findChildViewUnder() requires touch coordinates relative to
        // itself. So rawX() and rawY() will not be correct.
        View childViewUnder = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (childViewUnder != null) {
          ViewHolderWithSwipeActions viewHolder = (ViewHolderWithSwipeActions) recyclerView.getChildViewHolder(childViewUnder);
          viewBeingSwiped = viewHolder.getSwipeableLayout();
        }

        return false;
      }

      @Override
      public boolean onScroll(MotionEvent fromEvent, MotionEvent toEvent, float distanceX, float distanceY) {
        if (viewBeingSwiped == null) {
          return false;
        }

        // The swipe should start horizontally, but we'll let the gesture continue in any direction after that.
        boolean isHorizontalSwipe = Math.abs(distanceX) > Math.abs(distanceY) * 2;
        isSwiping = !viewBeingSwiped.isSettlingBackToPosition() && isHorizontalSwipe;

        if (!isSwiping) {
          return false;
        }

        float newTranslationX = viewBeingSwiped.getSwipeTranslation() - distanceX;
        viewBeingSwiped.setSwipeTranslation(newTranslationX);
        return true;
      }
    });
  }

  @Override
  public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent event) {
    //return handleTouch(recyclerView, event);
    gestureDetector.onTouchEvent(event);
    if (isSwiping) {
      recyclerView.requestDisallowInterceptTouchEvent(true);
      return true;
    }

    //noinspection RedundantIfStatement
    if (viewBeingSwiped != null && viewBeingSwiped.isSettlingBackToPosition()) {
      return true;
    }

    return false;
  }

  @Override
  public void onTouchEvent(RecyclerView recyclerView, MotionEvent event) {
    //handleTouch(recyclerView, event);
    gestureDetector.onTouchEvent(event);

    if (isSwiping && event.getAction() == MotionEvent.ACTION_UP) {
      viewBeingSwiped.handleOnRelease();
    }

    if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
      isSwiping = false;
      viewBeingSwiped.animateBackToPosition();
    }
  }
}