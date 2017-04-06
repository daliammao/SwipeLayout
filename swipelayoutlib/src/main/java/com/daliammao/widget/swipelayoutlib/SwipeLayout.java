package com.daliammao.widget.swipelayoutlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.daliammao.widget.swipelayoutlib.handler.SwipeEnabledHandler;
import com.daliammao.widget.swipelayoutlib.utils.UiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: zhoupengwei
 * @time:16/3/31-上午9:38
 * @Email: 496946423@qq.com
 * @desc: 暴露在外滑动的组件
 */
public class SwipeLayout extends FrameLayout {
    private static final int DRAG_LEFT = 1;
    private static final int DRAG_RIGHT = 2;
    private static final int DRAG_TOP = 4;
    private static final int DRAG_BOTTOM = 8;

    private int mTouchSlop;

    //当前滑动方向
    private DragEdge mCurrentDragEdge = DragEdge.Right;
    //当前滑动方式getPaddingTop
    private ShowMode mCurrentShowMode = ShowMode.PullOut;

    private ViewDragHelper mDragHelper;
    private GestureDetector mGestureDetector;
    //滑动的目标距离
    private int mDragDistance = 0;

    //保存每边的view
    private Map<DragEdge, View> mDragEdges = new HashMap<>();

    private float[] mEdgeSwipesOffset = new float[4];

    private List<SwipeListener> mSwipeListeners = new ArrayList<>();
    private DoubleClickListener mDoubleClickListener;
    private SwipeEnabledHandler mSwipeEnableHandler;

    //组件可否滑动打开的标志
    private boolean mSwipeEnabled = true;
    //针对与各个方向是否可以滑动打开的标志
    private boolean[] mSwipesEnabled = new boolean[]{true, true, true, true};
    //点击关闭的标志
    private boolean mClickToClose = false;
    private float mWillOpenPercentAfterOpen = 0.75f;
    private float mWillOpenPercentAfterClose = 0.25f;

    private int mEventCounter = 0;
    private boolean mIsBeingDragged;
    private float sX = -1, sY = -1;
    private Rect hitSurfaceRect;
    //保存上次滑动的百分比
    private float lastOpenPercent = 0;

    //滑动的方向
    public enum DragEdge {
        Left,
        Top,
        Right,
        Bottom
    }

    //滑动的方式
    public enum ShowMode {
        LayDown,//前景和后景呈层次性前后滑动
        PullOut//前景和背景呈连贯性上下滑动
    }

    //打开的状态
    public enum Status {
        Middle,
        Open,
        Close
    }

    //滑动的动作
    public enum SwipeAction {
        SwipeLeftTo,
        SwipeLeftBack,
        SwipeTopTo,
        SwipeTopBack,
        SwipeRightTo,
        SwipeRightBack,
        SwipeBottomTo,
        SwipeBottomBack,
    }

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDragHelper = ViewDragHelper.create(this, mDragHelperCallback);
        mGestureDetector = new GestureDetector(getContext(), mGestureListener);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout, 0, 0);
        if (arr != null) {
            int dragEdgeChoices = arr.getInt(R.styleable.SwipeLayout_swipe_drag_edge, DRAG_RIGHT);
            mEdgeSwipesOffset[DragEdge.Left.ordinal()] = arr.getDimension(R.styleable.SwipeLayout_swipe_left_edge_offset, 0);
            mEdgeSwipesOffset[DragEdge.Right.ordinal()] = arr.getDimension(R.styleable.SwipeLayout_swipe_right_edge_offset, 0);
            mEdgeSwipesOffset[DragEdge.Top.ordinal()] = arr.getDimension(R.styleable.SwipeLayout_swipe_top_edge_offset, 0);
            mEdgeSwipesOffset[DragEdge.Bottom.ordinal()] = arr.getDimension(R.styleable.SwipeLayout_swipe_bottom_edge_offset, 0);
            setClickToClose(arr.getBoolean(R.styleable.SwipeLayout_swipe_click_to_close, mClickToClose));

            if ((dragEdgeChoices & DRAG_LEFT) == DRAG_LEFT) {
                mDragEdges.put(DragEdge.Left, null);
            }
            if ((dragEdgeChoices & DRAG_TOP) == DRAG_TOP) {
                mDragEdges.put(DragEdge.Top, null);
            }
            if ((dragEdgeChoices & DRAG_RIGHT) == DRAG_RIGHT) {
                mDragEdges.put(DragEdge.Right, null);
            }
            if ((dragEdgeChoices & DRAG_BOTTOM) == DRAG_BOTTOM) {
                mDragEdges.put(DragEdge.Bottom, null);
            }

            int ordinal = arr.getInt(R.styleable.SwipeLayout_swipe_show_mode, ShowMode.PullOut.ordinal());
            mCurrentShowMode = ShowMode.values()[ordinal];
            arr.recycle();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        updateBottomViews();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isSwipeEnabled()) {
            return false;
        }

        Status status = getOpenStatus();

        if (mClickToClose && status == Status.Open && isTouchOnSurface(ev)) {
            return true;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDragHelper.processTouchEvent(ev);
                mIsBeingDragged = false;
                sX = ev.getRawX();
                sY = ev.getRawY();
                //如果是在中间状态(滚动),应该拦截触摸
                if (getOpenStatus() == Status.Middle) {
                    mIsBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                checkCanDrag(ev);
                SwipeAction action = transSwipeAction(mCurrentDragEdge, status);

                float distanceX = ev.getRawX() - sX;
                float distanceY = ev.getRawY() - sY;
                float angle = Math.abs(distanceY / distanceX);
                angle = (float) Math.toDegrees(Math.atan(angle));

                //是否调用mSwipeEnableHandler
                boolean callSwipeEnable = false;

                if (mSwipeEnableHandler != null) {
                    if (angle < 45) {
                        if (distanceX < 0 && (action == SwipeAction.SwipeRightTo || action == SwipeAction.SwipeLeftBack)) {
                            callSwipeEnable = true;
                        } else if (distanceX > 0 && (action == SwipeAction.SwipeLeftTo || action == SwipeAction.SwipeRightBack)) {
                            callSwipeEnable = true;
                        }
                    } else {
                        if (distanceY < 0 && (action == SwipeAction.SwipeBottomTo || action == SwipeAction.SwipeTopBack)) {
                            callSwipeEnable = true;
                        } else if (distanceY > 0 && (action == SwipeAction.SwipeTopTo || action == SwipeAction.SwipeBottomBack)) {
                            callSwipeEnable = true;
                        }
                    }
                }

                if (callSwipeEnable) {
                    if (mSwipeEnableHandler.onSwipeEnabled(SwipeLayout.this, action)) {
                        if (mIsBeingDragged) {
                            parentInterceptTouchEvent(true);
                        }
                        return true;
                    } else {
                        mIsBeingDragged = false;
                        break;
                    }
                } else {
                    if (mIsBeingDragged) {
                        parentInterceptTouchEvent(true);
                    }
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                mDragHelper.processTouchEvent(ev);
                break;
            default://handle other action, such as ACTION_POINTER_DOWN/UP
                mDragHelper.processTouchEvent(ev);
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isSwipeEnabled()) return super.onTouchEvent(event);

        int action = event.getActionMasked();
        mGestureDetector.onTouchEvent(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDragHelper.processTouchEvent(event);
                sX = event.getRawX();
                sY = event.getRawY();


            case MotionEvent.ACTION_MOVE: {
                //the drag state and the direction are already judged at onInterceptTouchEvent
                checkCanDrag(event);
                if (mIsBeingDragged) {
                    parentInterceptTouchEvent(true);
                    mDragHelper.processTouchEvent(event);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mDragHelper.processTouchEvent(event);
                break;

            default://handle other action, such as ACTION_POINTER_DOWN/UP
                mDragHelper.processTouchEvent(event);
        }

        return super.onTouchEvent(event) || mIsBeingDragged || action == MotionEvent.ACTION_DOWN;
    }

    private ViewDragHelper.Callback mDragHelperCallback = new ViewDragHelper.Callback() {
        //在拖拽前是否关闭
        boolean isCloseBeforeDrag = true;

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            boolean result = child == getSurfaceView() || getBottomViews().contains(child);
            if (result) {
                isCloseBeforeDrag = getOpenStatus() == Status.Close;
            }
            return result;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mDragDistance;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mDragDistance;
        }

        //横向滑动
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == getSurfaceView()) {
                switch (mCurrentDragEdge) {
                    case Top:
                    case Bottom:
                        return getPaddingLeft();
                    case Left:
                        if (left < getPaddingLeft()) return getPaddingLeft();
                        if (left > getPaddingLeft() + mDragDistance)
                            return getPaddingLeft() + mDragDistance;
                        break;
                    case Right:
                        if (left > getPaddingLeft()) return getPaddingLeft();
                        if (left < getPaddingLeft() - mDragDistance)
                            return getPaddingLeft() - mDragDistance;
                        break;
                }
            } else if (getCurrentBottomView() == child) {

                switch (mCurrentDragEdge) {
                    case Top:
                    case Bottom:
                        return getPaddingLeft();
                    case Left:
                        if (mCurrentShowMode == ShowMode.PullOut) {
                            if (left > getPaddingLeft()) return getPaddingLeft();
                        }
                        break;
                    case Right:
                        if (mCurrentShowMode == ShowMode.PullOut) {
                            if (left < getMeasuredWidth() - mDragDistance) {
                                return getMeasuredWidth() - mDragDistance;
                            }
                        }
                        break;
                }
            }
            return left;
        }

        //纵向滑动
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (child == getSurfaceView()) {
                switch (mCurrentDragEdge) {
                    case Left:
                    case Right:
                        return getPaddingTop();
                    case Top:
                        if (top < getPaddingTop()) return getPaddingTop();
                        if (top > getPaddingTop() + mDragDistance)
                            return getPaddingTop() + mDragDistance;
                        break;
                    case Bottom:
                        if (top < getPaddingTop() - mDragDistance) {
                            return getPaddingTop() - mDragDistance;
                        }
                        if (top > getPaddingTop()) {
                            return getPaddingTop();
                        }
                }
            } else {
                View surfaceView = getSurfaceView();
                int surfaceViewTop = surfaceView == null ? 0 : surfaceView.getTop();
                switch (mCurrentDragEdge) {
                    case Left:
                    case Right:
                        return getPaddingTop();
                    case Top:
                        if (mCurrentShowMode == ShowMode.PullOut) {
                            if (top > getPaddingTop()) return getPaddingTop();
                        } else {
                            if (surfaceViewTop + dy < getPaddingTop())
                                return getPaddingTop();
                            if (surfaceViewTop + dy > getPaddingTop() + mDragDistance)
                                return getPaddingTop() + mDragDistance;
                        }
                        break;
                    case Bottom:
                        if (mCurrentShowMode == ShowMode.PullOut) {
                            if (top < getMeasuredHeight() - mDragDistance)
                                return getMeasuredHeight() - mDragDistance;
                        } else {
                            if (surfaceViewTop + dy >= getPaddingTop())
                                return getPaddingTop();
                            if (surfaceViewTop + dy <= getPaddingTop() - mDragDistance)
                                return getPaddingTop() - mDragDistance;
                        }
                }
            }
            return top;
        }

        //手指释放的时候回调
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            processHandRelease(xvel, yvel, isCloseBeforeDrag);
            for (SwipeListener l : mSwipeListeners) {
                l.onHandRelease(SwipeLayout.this, mCurrentDragEdge, xvel, yvel);
            }

            invalidate();
        }

        //view在拖动过程坐标发生变化时会调用此方法,包括两个时间段:手动拖动和自动滚动
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            View surfaceView = getSurfaceView();
            if (surfaceView == null) return;
            View currentBottomView = getCurrentBottomView();

            int evLeft = surfaceView.getLeft();
            int evTop = surfaceView.getTop();

            if (changedView == surfaceView) {

                if (mCurrentShowMode == ShowMode.PullOut && currentBottomView != null) {
                    if (mCurrentDragEdge == DragEdge.Left || mCurrentDragEdge == DragEdge.Right) {
                        currentBottomView.offsetLeftAndRight(dx);
                    } else {
                        currentBottomView.offsetTopAndBottom(dy);
                    }
                }

            } else if (getBottomViews().contains(changedView)) {

                if (mCurrentShowMode == ShowMode.PullOut) {
                    surfaceView.offsetLeftAndRight(dx);
                    surfaceView.offsetTopAndBottom(dy);
                } else {
                    Rect rect = computeBottomLayDown(mCurrentDragEdge);
                    if (currentBottomView != null) {
                        currentBottomView.layout(rect.left, rect.top, rect.right, rect.bottom);
                    }

                    int newLeft = surfaceView.getLeft() + dx, newTop = surfaceView.getTop() + dy;

                    if (mCurrentDragEdge == DragEdge.Left && newLeft < getPaddingLeft())
                        newLeft = getPaddingLeft();
                    else if (mCurrentDragEdge == DragEdge.Right && newLeft > getPaddingLeft())
                        newLeft = getPaddingLeft();
                    else if (mCurrentDragEdge == DragEdge.Top && newTop < getPaddingTop())
                        newTop = getPaddingTop();
                    else if (mCurrentDragEdge == DragEdge.Bottom && newTop > getPaddingTop())
                        newTop = getPaddingTop();

                    surfaceView.layout(newLeft, newTop, newLeft + getMeasuredWidth(), newTop + getMeasuredHeight());
                }
            }

            dispatchSwipeEvent(evLeft, evTop, dx, dy, isCloseBeforeDrag);

            invalidate();

        }
    };

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mClickToClose && isTouchOnSurface(e)) {
                close();
            }
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mDoubleClickListener != null) {
                View target;
                View bottom = getCurrentBottomView();
                View surface = getSurfaceView();
                if (bottom != null && e.getX() > bottom.getLeft() && e.getX() < bottom.getRight()
                        && e.getY() > bottom.getTop() && e.getY() < bottom.getBottom()) {
                    target = bottom;
                } else {
                    target = surface;
                }
                mDoubleClickListener.onDoubleClick(SwipeLayout.this, target == surface);
            }
            return true;
        }
    };

    private boolean isTouchOnSurface(MotionEvent ev) {
        View surfaceView = getSurfaceView();
        if (surfaceView == null) {
            return false;
        }
        if (hitSurfaceRect == null) {
            hitSurfaceRect = new Rect();
        }
        surfaceView.getHitRect(hitSurfaceRect);
        return hitSurfaceRect.contains((int) ev.getX(), (int) ev.getY());
    }

    private void parentInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private void checkCanDrag(MotionEvent ev) {
        if (mIsBeingDragged) return;

        Status status = getOpenStatus();

        if (status == Status.Middle) {
            mIsBeingDragged = true;
            return;
        }

        float distanceX = ev.getRawX() - sX;
        float distanceY = ev.getRawY() - sY;
        float angle = Math.abs(distanceY / distanceX);
        angle = (float) Math.toDegrees(Math.atan(angle));
        if (getOpenStatus() == Status.Close) {
            DragEdge dragEdge;
            if (angle < 45) {
                if (distanceX > 0 && isLeftSwipeEnabled()) {
                    dragEdge = DragEdge.Left;
                } else if (distanceX < 0 && isRightSwipeEnabled()) {
                    dragEdge = DragEdge.Right;
                } else return;

            } else {
                if (distanceY > 0 && isTopSwipeEnabled()) {
                    dragEdge = DragEdge.Top;
                } else if (distanceY < 0 && isBottomSwipeEnabled()) {
                    dragEdge = DragEdge.Bottom;
                } else return;
            }
            setCurrentDragEdge(dragEdge);
        }

        boolean doNothing = false;
        if (mCurrentDragEdge == DragEdge.Right) {
            boolean suitable = (status == Status.Open && distanceX > mTouchSlop)
                    || (status == Status.Close && distanceX < -mTouchSlop);
            suitable = suitable || (status == Status.Middle);

            if (angle > 30 || !suitable) {
                doNothing = true;
            }
        }

        if (mCurrentDragEdge == DragEdge.Left) {
            boolean suitable = (status == Status.Open && distanceX < -mTouchSlop)
                    || (status == Status.Close && distanceX > mTouchSlop);
            suitable = suitable || status == Status.Middle;

            if (angle > 30 || !suitable) {
                doNothing = true;
            }
        }

        if (mCurrentDragEdge == DragEdge.Top) {
            boolean suitable = (status == Status.Open && distanceY < -mTouchSlop)
                    || (status == Status.Close && distanceY > mTouchSlop);
            suitable = suitable || status == Status.Middle;

            if (angle < 60 || !suitable) {
                doNothing = true;
            }
        }

        if (mCurrentDragEdge == DragEdge.Bottom) {
            boolean suitable = (status == Status.Open && distanceY > mTouchSlop)
                    || (status == Status.Close && distanceY < -mTouchSlop);
            suitable = suitable || status == Status.Middle;

            if (angle < 60 || !suitable) {
                doNothing = true;
            }
        }
        mIsBeingDragged = !doNothing;
    }

    /**
     * 过程中的前view释放事件
     *
     * @param xvel                 xVelocity
     * @param yvel                 yVelocity
     * @param isCloseBeforeDragged the open state before drag
     */
    protected void processHandRelease(float xvel, float yvel, boolean isCloseBeforeDragged) {
        float minVelocity = mDragHelper.getMinVelocity();
        View surfaceView = getSurfaceView();
        DragEdge currentDragEdge = mCurrentDragEdge;
        if (currentDragEdge == null || surfaceView == null) {
            return;
        }
        float willOpenPercent = (isCloseBeforeDragged ? mWillOpenPercentAfterClose : mWillOpenPercentAfterOpen);
        float openPercent = getOpenPercent();
        if (currentDragEdge == DragEdge.Left) {
            if (xvel > minVelocity) open();
            else if (xvel < -minVelocity) close();
            else {
                if (openPercent > willOpenPercent) open();
                else close();
            }
        } else if (currentDragEdge == DragEdge.Right) {
            if (xvel > minVelocity) close();
            else if (xvel < -minVelocity) open();
            else {
                if (openPercent > willOpenPercent) open();
                else close();
            }
        } else if (currentDragEdge == DragEdge.Top) {
            if (yvel > minVelocity) open();
            else if (yvel < -minVelocity) close();
            else {
                if (openPercent > willOpenPercent) open();
                else close();
            }
        } else if (currentDragEdge == DragEdge.Bottom) {
            if (yvel > minVelocity) close();
            else if (yvel < -minVelocity) open();
            else {
                if (openPercent > willOpenPercent) open();
                else close();
            }
        }
    }

    protected void dispatchSwipeEvent(int surfaceLeft, int surfaceTop, int dx, int dy, boolean isCloseBeforeDragged) {
        DragEdge edge = getDragEdge();
        boolean open = true;
        if (edge == DragEdge.Left) {
            if (dx < 0) open = false;
        } else if (edge == DragEdge.Right) {
            if (dx > 0) open = false;
        } else if (edge == DragEdge.Top) {
            if (dy < 0) open = false;
        } else if (edge == DragEdge.Bottom) {
            if (dy > 0) open = false;
        }

        dispatchSwipeEvent(surfaceLeft, surfaceTop, open, isCloseBeforeDragged);
    }

    protected void dispatchSwipeEvent(int surfaceLeft, int surfaceTop, boolean open, boolean isCloseBeforeDragged) {
        safeBottomView();
        Status status = getOpenStatus();
        float openPercent = getOpenPercent();
        float willOpenPercent = (isCloseBeforeDragged ? mWillOpenPercentAfterClose : mWillOpenPercentAfterOpen);
        int willOpenType = -1;//记录触发onDragOpenToClose(记录值为0)还是onDragCloseToOpen(记录值为1) 其他值均为无效

        if (openPercent > willOpenPercent && lastOpenPercent <= willOpenPercent) {
            willOpenType = 1;
        } else if (openPercent < willOpenPercent && lastOpenPercent >= willOpenPercent) {
            willOpenType = 0;
        }

        lastOpenPercent = openPercent;

        if (!mSwipeListeners.isEmpty()) {
            mEventCounter++;
            for (SwipeListener l : mSwipeListeners) {
                if (mEventCounter == 1) {
                    if (open) {
                        l.onStartOpen(this, mCurrentDragEdge);
                    } else {
                        l.onStartClose(this, mCurrentDragEdge);
                    }
                }
                if (willOpenType == 0) {
                    l.onDragOpenToClose(isCloseBeforeDragged);
                } else if (willOpenType == 1) {
                    l.onDragCloseToOpen(isCloseBeforeDragged);
                }

                l.onUpdate(SwipeLayout.this, mCurrentDragEdge, surfaceLeft - getPaddingLeft(), surfaceTop - getPaddingTop(), isCloseBeforeDragged);
            }

            if (status == Status.Close) {
                for (SwipeListener l : mSwipeListeners) {
                    l.onClose(SwipeLayout.this, mCurrentDragEdge);
                }
                mEventCounter = 0;
            }

            if (status == Status.Open) {
                View currentBottomView = getCurrentBottomView();
                if (currentBottomView != null) {
                    currentBottomView.setEnabled(true);
                }
                for (SwipeListener l : mSwipeListeners) {
                    l.onOpen(SwipeLayout.this, mCurrentDragEdge);
                }
                mEventCounter = 0;
            }
        }
    }

    /**
     * 平滑打开前view
     */
    public void open() {
        open(true, true);
    }

    public void open(boolean smooth) {
        open(smooth, true);
    }

    public void open(boolean smooth, boolean notify) {
        View surface = getSurfaceView(), bottom = getCurrentBottomView();
        if (surface == null) {
            return;
        }
        boolean isCloseBeforeDrag = getOpenStatus() == Status.Close;

        int dx, dy;
        Rect rect = computeSurfaceLayoutArea(true);
        if (smooth) {
            mDragHelper.smoothSlideViewTo(surface, rect.left, rect.top);
        } else {
            dx = rect.left - surface.getLeft();
            dy = rect.top - surface.getTop();
            surface.layout(rect.left, rect.top, rect.right, rect.bottom);
            if (getShowMode() == ShowMode.PullOut) {
                Rect bRect = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, rect);
                if (bottom != null) {
                    bottom.layout(bRect.left, bRect.top, bRect.right, bRect.bottom);
                }
            }
            if (notify) {
                dispatchSwipeEvent(rect.left, rect.top, dx, dy, isCloseBeforeDrag);
            } else {
                safeBottomView();
            }
        }
        invalidate();
    }

    public void open(DragEdge edge) {
        setCurrentDragEdge(edge);
        open(true, true);
    }

    public void open(boolean smooth, DragEdge edge) {
        setCurrentDragEdge(edge);
        open(smooth, true);
    }

    public void open(boolean smooth, boolean notify, DragEdge edge) {
        setCurrentDragEdge(edge);
        open(smooth, notify);
    }

    /**
     * 平滑关闭前view.
     */
    public void close() {
        close(true, true);
    }

    public void close(boolean smooth) {
        close(smooth, true);
    }

    /**
     * 关闭前view
     *
     * @param smooth smoothly or not.
     * @param notify if notify all the listeners.
     */
    public void close(boolean smooth, boolean notify) {
        View surface = getSurfaceView();
        if (surface == null) {
            return;
        }
        boolean isCloseBeforeDrag = getOpenStatus() == Status.Close;

        int dx, dy;
        if (smooth)
            mDragHelper.smoothSlideViewTo(getSurfaceView(), getPaddingLeft(), getPaddingTop());
        else {
            Rect rect = computeSurfaceLayoutArea(false);
            dx = rect.left - surface.getLeft();
            dy = rect.top - surface.getTop();
            surface.layout(rect.left, rect.top, rect.right, rect.bottom);
            if (notify) {
                dispatchSwipeEvent(rect.left, rect.top, dx, dy, isCloseBeforeDrag);
            } else {
                safeBottomView();
            }
        }
        invalidate();
    }

    public void toggle() {
        toggle(true);
    }

    public void toggle(boolean smooth) {
        if (getOpenStatus() == Status.Open)
            close(smooth);
        else if (getOpenStatus() == Status.Close) open(smooth);
    }

    protected void layoutPullOut() {
        View surfaceView = getSurfaceView();
        Rect surfaceRect = computeSurfaceLayoutArea(getOpenStatus() == Status.Open ? true : false);

        if (surfaceView != null) {
            surfaceView.layout(surfaceRect.left, surfaceRect.top, surfaceRect.right, surfaceRect.bottom);
            bringChildToFront(surfaceView);
        }
        View currentBottomView = getCurrentBottomView();
        Rect bottomViewRect = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, surfaceRect);

        if (currentBottomView != null) {
            currentBottomView.layout(bottomViewRect.left, bottomViewRect.top, bottomViewRect.right, bottomViewRect.bottom);
        }
    }

    protected void layoutLayDown() {
        View surfaceView = getSurfaceView();
        Rect surfaceRect = computeSurfaceLayoutArea(getOpenStatus() == Status.Open ? true : false);

        if (surfaceView != null) {
            surfaceView.layout(surfaceRect.left, surfaceRect.top, surfaceRect.right, surfaceRect.bottom);
            bringChildToFront(surfaceView);
        }
        View currentBottomView = getCurrentBottomView();
        Rect bottomViewRect = computeBottomLayoutAreaViaSurface(ShowMode.LayDown, surfaceRect);

        if (currentBottomView != null) {
            currentBottomView.layout(bottomViewRect.left, bottomViewRect.top, bottomViewRect.right, bottomViewRect.bottom);
        }
    }

    /**
     * 一个辅助函数来计算前View的矩形区域.
     *
     * @param open open status or close status.
     */
    private Rect computeSurfaceLayoutArea(boolean open) {
        int l = getPaddingLeft(), t = getPaddingTop();
        if (open) {
            if (mCurrentDragEdge == DragEdge.Left)
                l = getPaddingLeft() + mDragDistance;
            else if (mCurrentDragEdge == DragEdge.Right)
                l = getPaddingLeft() - mDragDistance;
            else if (mCurrentDragEdge == DragEdge.Top)
                t = getPaddingTop() + mDragDistance;
            else t = getPaddingTop() - mDragDistance;
        }

        return new Rect(l, t, l + getMeasuredWidth(), t + getMeasuredHeight());
    }

    /**
     * 一个辅助函数来计算后View打开后的的矩形区域(根据打开后前View的区域和打开的距离)
     *
     * @param mode        打开方式
     * @param surfaceArea 打开后前View的矩形区域
     * @return
     */
    private Rect computeBottomLayoutAreaViaSurface(ShowMode mode, Rect surfaceArea) {
        Rect rect = surfaceArea;
        View bottomView = getCurrentBottomView();

        int bl = 0, bt = 0, br = 0, bb = 0;
        if (mode == ShowMode.PullOut) {
            bl = rect.left;
            bt = rect.top;
            br = rect.right;
            bb = rect.bottom;

            if (mCurrentDragEdge == DragEdge.Left)
                bl = rect.left - mDragDistance;
            else if (mCurrentDragEdge == DragEdge.Right)
                bl = rect.right;
            else if (mCurrentDragEdge == DragEdge.Top)
                bt = rect.top - mDragDistance;
            else bt = rect.bottom;

            if (mCurrentDragEdge == DragEdge.Left || mCurrentDragEdge == DragEdge.Right) {
                bb = rect.bottom;
                br = bl + (bottomView == null ? 0 : bottomView.getMeasuredWidth());
            } else {
                bb = bt + (bottomView == null ? 0 : bottomView.getMeasuredHeight());
                br = rect.right;
            }
        } else if (mode == ShowMode.LayDown) {
            bl = getPaddingLeft();
            bt = getPaddingTop();
            br = bl+getMeasuredWidth();
            bb = bt+getMeasuredHeight();
            if (mCurrentDragEdge == DragEdge.Left)
                br = bl + mDragDistance;
            else if (mCurrentDragEdge == DragEdge.Right)
                bl = br - mDragDistance;
            else if (mCurrentDragEdge == DragEdge.Top)
                bb = bt + mDragDistance;
            else bt = bb - mDragDistance;

        }
        return new Rect(bl, bt, br, bb);
    }

    /**
     * 一个辅助函数来计算后View打开后的的矩形区域(根据打开的距离)
     *
     * @param dragEdge
     * @return
     */
    private Rect computeBottomLayDown(DragEdge dragEdge) {
        int bl = getPaddingLeft(), bt = getPaddingTop();
        int br, bb;
        if (dragEdge == DragEdge.Right) {
            bl = getMeasuredWidth() - mDragDistance;
        } else if (dragEdge == DragEdge.Bottom) {
            bt = getMeasuredHeight() - mDragDistance;
        }
        if (dragEdge == DragEdge.Left || dragEdge == DragEdge.Right) {
            br = bl + mDragDistance;
            bb = bt + getMeasuredHeight();
        } else {
            br = bl + getMeasuredWidth();
            bb = bt + mDragDistance;
        }
        return new Rect(bl, bt, br, bb);
    }

    private SwipeAction transSwipeAction(DragEdge dragedge, Status state) {
        switch (dragedge) {
            default:
            case Left:
                return state == Status.Open ? SwipeAction.SwipeLeftBack : SwipeAction.SwipeLeftTo;
            case Top:
                return state == Status.Open ? SwipeAction.SwipeTopBack : SwipeAction.SwipeTopTo;
            case Right:
                return state == Status.Open ? SwipeAction.SwipeRightBack : SwipeAction.SwipeRightTo;
            case Bottom:
                return state == Status.Open ? SwipeAction.SwipeBottomBack : SwipeAction.SwipeBottomTo;
        }
    }

    public void onViewRemoved(View child) {
        for (Map.Entry<DragEdge, View> entry : mDragEdges.entrySet()) {
            if (entry.getValue() == child) {
                mDragEdges.remove(entry.getKey());
            }
        }
    }

    public Map<DragEdge, View> getDragEdgeMap() {
        return mDragEdges;
    }

    public float getOpenPercent() {
        DragEdge currentDragEdge = getDragEdge();
        float openPercent = 0;

        if (currentDragEdge == DragEdge.Left) {
            openPercent = 1f * getSurfaceView().getLeft() / mDragDistance;
        } else if (currentDragEdge == DragEdge.Right) {
            openPercent = 1f * (-getSurfaceView().getLeft()) / mDragDistance;
        } else if (currentDragEdge == DragEdge.Top) {
            openPercent = 1f * getSurfaceView().getTop() / mDragDistance;
        } else if (currentDragEdge == DragEdge.Bottom) {
            openPercent = 1f * (-getSurfaceView().getTop()) / mDragDistance;
        }
        return openPercent;
    }

    private float getCurrentOffset() {
        if (mCurrentDragEdge == null) return 0;
        return mEdgeSwipesOffset[mCurrentDragEdge.ordinal()];
    }

    private void setCurrentDragEdge(DragEdge dragEdge) {
        mCurrentDragEdge = dragEdge;
        updateBottomViews();
    }

    private void updateBottomViews() {
        View currentBottomView = getCurrentBottomView();
        if (currentBottomView != null) {
            if (mCurrentDragEdge == DragEdge.Left || mCurrentDragEdge == DragEdge.Right) {
                mDragDistance = currentBottomView.getMeasuredWidth() - UiUtil.dp2px(getContext(), getCurrentOffset());
            } else {
                mDragDistance = currentBottomView.getMeasuredHeight() - UiUtil.dp2px(getContext(), getCurrentOffset());
            }
        }

        if (mCurrentShowMode == ShowMode.PullOut) {
            layoutPullOut();
        } else if (mCurrentShowMode == ShowMode.LayDown) {
            layoutLayDown();
        }

        safeBottomView();
    }

    /**
     * 防止底部视图得到任何接触事件.特别是在LayDown模式
     */
    private void safeBottomView() {
        Status status = getOpenStatus();
        List<View> bottoms = getBottomViews();

        if (status == Status.Close) {
            for (View bottom : bottoms) {
                if (bottom != null && bottom.getVisibility() != INVISIBLE) {
                    bottom.setVisibility(INVISIBLE);
                }
            }
        } else {
            View currentBottomView = getCurrentBottomView();
            if (currentBottomView != null && currentBottomView.getVisibility() != VISIBLE) {
                currentBottomView.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * 返回前景view.如果没有子view,则返回null.
     */
    public View getSurfaceView() {
        if (getChildCount() == 0) return null;
        return getChildAt(getChildCount() - 1);
    }

    /**
     * 获取当前的底部view
     */
    @Nullable
    public View getCurrentBottomView() {
        List<View> bottoms = getBottomViews();
        if (mCurrentDragEdge.ordinal() < bottoms.size()) {
            return bottoms.get(mCurrentDragEdge.ordinal());
        }
        return null;
    }

    /**
     * 根据指定的滑动方向获取底部View
     *
     * @param dragEdge
     * @return
     */
    @Nullable
    public View getBottomView(DragEdge dragEdge) {
        if (dragEdge == null) {
            return null;
        }

        return getBottomViews().get(dragEdge.ordinal());
    }

    /**
     * @return 获得所有的底部view:左,上,右,下(该方向没有设置则返回null)
     */
    public List<View> getBottomViews() {
        ArrayList<View> bottoms = new ArrayList<View>();
        for (DragEdge dragEdge : DragEdge.values()) {
            bottoms.add(mDragEdges.get(dragEdge));
        }
        return bottoms;
    }

    /**
     * 有两种不的显示方式
     * {@link com.daliammao.widget.swipelayoutlib.SwipeLayout.ShowMode}.PullOut and
     * {@link com.daliammao.widget.swipelayoutlib.SwipeLayout.ShowMode}.LayDown.
     *
     * @param mode
     */
    public void setShowMode(ShowMode mode) {
        mCurrentShowMode = mode;
        requestLayout();
    }

    public ShowMode getShowMode() {
        return mCurrentShowMode;
    }

    public DragEdge getDragEdge() {
        return mCurrentDragEdge;
    }

    /**
     * 设置拖动距离,它会通过这个值设置底部视图的宽度或高度.
     *
     * @param max max distance in dp unit
     */
    public void setDragDistance(int max) {
        if (max < 0) max = 0;
        mDragDistance = UiUtil.dp2px(getContext(), max);
        requestLayout();
    }

    public int getDragDistance() {
        return mDragDistance;
    }

    public void clearDragEdge() {
        mDragEdges.clear();
    }

    public void setDrag(DragEdge dragEdge, int childId) {
        clearDragEdge();
        addDrag(dragEdge, childId);
    }

    public void setDrag(DragEdge dragEdge, View child) {
        clearDragEdge();
        addDrag(dragEdge, child);
    }

    public void addDrag(DragEdge dragEdge, int childId) {
        addDrag(dragEdge, findViewById(childId), null);
    }

    public void addDrag(DragEdge dragEdge, View child) {
        addDrag(dragEdge, child, null);
    }

    public void addDrag(DragEdge dragEdge, View child, ViewGroup.LayoutParams params) {
        if (child == null) return;

        if (params == null) {
            params = generateDefaultLayoutParams();
        }
        if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params);
        }
        int gravity = -1;
        switch (dragEdge) {
            case Left:
                gravity = Gravity.LEFT;
                break;
            case Right:
                gravity = Gravity.RIGHT;
                break;
            case Top:
                gravity = Gravity.TOP;
                break;
            case Bottom:
                gravity = Gravity.BOTTOM;
                break;
        }
        if (params instanceof LayoutParams) {
            ((LayoutParams) params).gravity = gravity;
        }
        addView(child, 0, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child == null) return;
        int gravity = Gravity.NO_GRAVITY;
        try {
            gravity = (Integer) params.getClass().getField("gravity").get(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (gravity > 0) {
            gravity = GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this));

            if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
                mDragEdges.put(DragEdge.Left, child);
            }
            if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
                mDragEdges.put(DragEdge.Right, child);
            }
            if ((gravity & Gravity.TOP) == Gravity.TOP) {
                mDragEdges.put(DragEdge.Top, child);
            }
            if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
                mDragEdges.put(DragEdge.Bottom, child);
            }
        } else {
            for (Map.Entry<DragEdge, View> entry : mDragEdges.entrySet()) {
                if (entry.getValue() == null) {
                    //means used the drag_edge attr, the no gravity child should be use set
                    mDragEdges.put(entry.getKey(), child);
                    break;
                }
            }
        }
        if (child.getParent() == this) {
            return;
        }
        super.addView(child, index, params);
    }

    /**
     * 获得打开的状态.
     *
     * @return {@link com.daliammao.widget.swipelayoutlib.SwipeLayout.Status} Open , Close or
     * Middle.
     */
    public Status getOpenStatus() {
        View surfaceView = getSurfaceView();
        if (surfaceView == null) {
            return Status.Close;
        }
        int surfaceLeft = surfaceView.getLeft();
        int surfaceTop = surfaceView.getTop();
        if (surfaceLeft == getPaddingLeft() && surfaceTop == getPaddingTop()) return Status.Close;

        if (surfaceLeft == (getPaddingLeft() - mDragDistance) || surfaceLeft == (getPaddingLeft() + mDragDistance)
                || surfaceTop == (getPaddingTop() - mDragDistance) || surfaceTop == (getPaddingTop() + mDragDistance))
            return Status.Open;

        return Status.Middle;
    }

    public boolean isClickToClose() {
        return mClickToClose;
    }

    public void setClickToClose(boolean mClickToClose) {
        this.mClickToClose = mClickToClose;
    }

    public void setSwipeEnabled(boolean enabled) {
        mSwipeEnabled = enabled;
    }

    public boolean isSwipeEnabled() {
        return mSwipeEnabled;
    }

    public boolean addSwipeListener(SwipeListener l) {
        return mSwipeListeners.add(l);
    }

    public boolean removeSwipeListener(SwipeListener l) {
        return mSwipeListeners.remove(l);
    }

    public void removeAllSwipeListener() {
        mSwipeListeners.clear();
    }

    public void setOnDoubleClickListener(DoubleClickListener doubleClickListener) {
        mDoubleClickListener = doubleClickListener;
    }

    public void setSwipeEnableHandler(SwipeEnabledHandler swipeEnableHandler) {
        mSwipeEnableHandler = swipeEnableHandler;
    }

    public boolean isLeftSwipeEnabled() {
        View bottomView = mDragEdges.get(DragEdge.Left);
        return bottomView != null && bottomView.getParent() == this
                && bottomView != getSurfaceView() && mSwipesEnabled[DragEdge.Left.ordinal()];
    }

    public void setLeftSwipeEnabled(boolean leftSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Left.ordinal()] = leftSwipeEnabled;
    }

    public boolean isRightSwipeEnabled() {
        View bottomView = mDragEdges.get(DragEdge.Right);
        return bottomView != null && bottomView.getParent() == this
                && bottomView != getSurfaceView() && mSwipesEnabled[DragEdge.Right.ordinal()];
    }

    public void setRightSwipeEnabled(boolean rightSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Right.ordinal()] = rightSwipeEnabled;
    }

    public boolean isTopSwipeEnabled() {
        View bottomView = mDragEdges.get(DragEdge.Top);
        return bottomView != null && bottomView.getParent() == this
                && bottomView != getSurfaceView() && mSwipesEnabled[DragEdge.Top.ordinal()];
    }

    public void setTopSwipeEnabled(boolean topSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Top.ordinal()] = topSwipeEnabled;
    }

    public boolean isBottomSwipeEnabled() {
        View bottomView = mDragEdges.get(DragEdge.Bottom);
        return bottomView != null && bottomView.getParent() == this
                && bottomView != getSurfaceView() && mSwipesEnabled[DragEdge.Bottom.ordinal()];
    }

    public void setBottomSwipeEnabled(boolean bottomSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Bottom.ordinal()] = bottomSwipeEnabled;
    }

    /***
     * Returns the percentage of revealing at which the view below should the view finish opening
     * if it was already open before dragging
     *
     * @returns The percentage of view revealed to trigger, default value is 0.25
     */
    public float getWillOpenPercentAfterOpen() {
        return mWillOpenPercentAfterOpen;
    }

    /***
     * Allows to stablish at what percentage of revealing the view below should the view finish opening
     * if it was already open before dragging
     *
     * @param willOpenPercentAfterOpen The percentage of view revealed to trigger, default value is 0.25
     */
    public void setWillOpenPercentAfterOpen(float willOpenPercentAfterOpen) {
        this.mWillOpenPercentAfterOpen = willOpenPercentAfterOpen;
    }

    /***
     * Returns the percentage of revealing at which the view below should the view finish opening
     * if it was already closed before dragging
     *
     * @returns The percentage of view revealed to trigger, default value is 0.25
     */
    public float getWillOpenPercentAfterClose() {
        return mWillOpenPercentAfterClose;
    }

    /***
     * Allows to stablish at what percentage of revealing the view below should the view finish opening
     * if it was already closed before dragging
     *
     * @param willOpenPercentAfterClose The percentage of view revealed to trigger, default value is 0.75
     */
    public void setWillOpenPercentAfterClose(float willOpenPercentAfterClose) {
        this.mWillOpenPercentAfterClose = willOpenPercentAfterClose;
    }

    public interface SwipeListener {
        void onStartOpen(SwipeLayout layout, DragEdge edge);

        void onOpen(SwipeLayout layout, DragEdge edge);

        void onStartClose(SwipeLayout layout, DragEdge edge);

        void onClose(SwipeLayout layout, DragEdge edge);

        void onUpdate(SwipeLayout layout, DragEdge edge, int leftOffset, int topOffset, boolean isCloseBeforeDragged);

        void onHandRelease(SwipeLayout layout, DragEdge edge, float xvel, float yvel);

        /**
         * 滑动到一定程度,原先松开手指控件打开,现在松开手指关闭,刚刚到达这个程度触发.
         *
         * @param isCloseBeforeDragged
         */
        void onDragOpenToClose(boolean isCloseBeforeDragged);

        /**
         * 滑动到一定程度,原先松开手指控件关闭,现在松开手指打开,刚刚到达这个程度触发.
         *
         * @param isCloseBeforeDragged
         */
        void onDragCloseToOpen(boolean isCloseBeforeDragged);
    }

    public interface DoubleClickListener {
        void onDoubleClick(SwipeLayout layout, boolean surface);
    }
}
