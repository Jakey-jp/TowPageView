package com.towpageview.jakey.tpvlibrary;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Scroller;


/**
 * 包含两个ScrollView的容器
 */
public class TowPageView extends RelativeLayout implements NestedScrollingParent {

    private ValueAnimator mValueAnimator;

    public enum STATE {
        PULL_TO_NEXT,
        RELEASE_TO_NEXT,
        NEXT_ING,
        PULL_TO_UP,
        RELEASE_TO_UP,
        UP_ING,
        DONE;

    }

    /**
     * 动画速度
     */
    private int SPEED = 8;
    /**
     * 手指滑动距离与下拉头的滑动距离比，中间会随正切函数变化
     */
    private float radio = 2;

    /**
     * 用于计算手滑动的速度
     */
    private VelocityTracker vt;

    private View mTopView;
    private View mBottomView;
    private View mCenterView;

    private boolean canPullDown;
    private boolean canPullUp;
    private STATE mState = STATE.DONE;

    /**
     * 记录当前展示的是哪个view，0是topView，1是bottomView
     */
    private int mCurrentViewIndex = 0;

//    private MyTimer mTimer;


    private ICenterView mICenterView;


    private OnSwitchPageCompletedListener mOnSwitchPageCompletedListener;
    //    private final Handler handler = new MyHandler(this);
    private Scroller mScroller;

    public interface OnSwitchPageCompletedListener {

        /**
         * @mcoy 当切换到上一页面回调
         */
        void onSwitchUpCompleted();

        /**
         * @mcoy 当切换到下一页面回调
         */
        void onSwitchNextCompleted();
    }


    public void setOnSwitchPageCompletedListener(OnSwitchPageCompletedListener listener) {
        mOnSwitchPageCompletedListener = listener;
    }


    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && mState != STATE.UP_ING && mState != STATE.NEXT_ING;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

        if ((getScrollY() > 0 && mCurrentViewIndex == 0) || (getScrollY() < mTopView.getMeasuredHeight() + mCenterView.getMeasuredHeight() && mCurrentViewIndex == 1)) {
            if (processScroll(dy)) {
                consumed[1] = dy;
            }
        }


    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {

        processScroll(dyUnconsumed);

    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {

        if (mState != STATE.DONE) {
            return true;
        }
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    private Boolean processScroll(int dy) {
        if (mState == STATE.UP_ING || mState == STATE.NEXT_ING)
            return false;

        if (pullup(dy) || pulldown(dy)) {
            scrollBy(0, dy);
            return true;
        }
        return false;
    }


    private boolean pulldown(int dy) {
        return (dy < 0 && canPullDown()) || (getScrollY() < mTopView.getMeasuredHeight() + mCenterView.getMeasuredHeight() && dy > 0 && mCurrentViewIndex == 1);
    }

    private boolean canPullDown() {
        NestedScrollView sv = (NestedScrollView) mBottomView;
        canPullDown = mCurrentViewIndex == 1 && sv.getScrollY() == 0;
        return canPullDown;

    }

    private boolean pullup(int dy) {
        return (dy > 0 && canPullUp()) || (getScrollY() > 0 && dy < 0 && mCurrentViewIndex == 0);
    }

    private boolean canPullUp() {
        NestedScrollView sv = (NestedScrollView) mTopView;
        canPullUp = mCurrentViewIndex == 0 && sv.getScrollY() == sv.getChildAt(0).getMeasuredHeight() - sv
                .getMeasuredHeight();
        return canPullUp;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

//                if (!mScroller.isFinished()) {
//                    mScroller.abortAnimation();
//                } else if (mState == STATE.UP_ING || mState == STATE.NEXT_ING) {
//                    mTimer.cancel();
//                }

                break;
            case MotionEvent.ACTION_UP:

                if (mState == STATE.RELEASE_TO_NEXT) {
                    setState(STATE.NEXT_ING);
                } else if (mState == STATE.RELEASE_TO_UP) {
                    setState(STATE.UP_ING);
                } else if (mState == STATE.PULL_TO_NEXT) {
                    smoothScrollTo(0, 500 / mCenterView.getMeasuredHeight() * getScrollY());
                } else if (mState == STATE.PULL_TO_UP) {
                    smoothScrollTo(mTopView.getMeasuredHeight() + mCenterView.getMeasuredHeight(), 500 / mCenterView.getMeasuredHeight() * (mTopView.getMeasuredHeight() + mCenterView.getMeasuredHeight() - getScrollY()));
                }

                break;

        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > mTopView.getMeasuredHeight() + mCenterView.getMeasuredHeight()) {
            y = mTopView.getMeasuredHeight() + mCenterView.getMeasuredHeight();
        }

        if (mState != STATE.NEXT_ING && mState != STATE.UP_ING) {

            MarginLayoutParams lp = (MarginLayoutParams) mCenterView.getLayoutParams();

            if (mCurrentViewIndex == 0) {
                int moveY = getScrollY();
                int dy = y - getScrollY();
                // 根据下拉距离改变比例
                radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * moveY));

                if (moveY > mCenterView.getMeasuredHeight()) {
                    y = (int) (moveY + dy / radio);
                    Log.d("jakey",radio + "," + dy + "," + (int) (dy / radio));
//                    y = moveY;
                }


                lp.bottomMargin = Math.max(y - mCenterView.getMeasuredHeight(), 0);
                mCenterView.setLayoutParams(lp);


                if (y == 0 && mState != STATE.DONE) {
                    setState(STATE.DONE);
                } else if (y > mCenterView.getMeasuredHeight() && mState != STATE.RELEASE_TO_NEXT) {
                    setState(STATE.RELEASE_TO_NEXT);
                } else if (y <= mCenterView.getMeasuredHeight() && mState != STATE.PULL_TO_NEXT) {
                    setState(STATE.PULL_TO_NEXT);
                }


            } else {

                if (mTopView.getMeasuredHeight() > y) {

                    // 根据下拉距离改变比例
                    int moveY = mTopView.getMeasuredHeight() + mCenterView.getMeasuredHeight() - getScrollY();
                    radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * (moveY + lp.topMargin)));
                    lp.topMargin += (mTopView.getMeasuredHeight() - y) / radio;
                    y = mTopView.getMeasuredHeight();

                } else if (getScrollY() == mTopView.getMeasuredHeight() && y > mTopView.getMeasuredHeight()) {
                    int dy = y - getScrollY();

                    y = mTopView.getMeasuredHeight() + Math.max(dy - lp.topMargin, 0);
                    lp.topMargin = Math.max(lp.topMargin - dy, 0);

                } else {
                    lp.topMargin = 0;
                }
                mCenterView.setLayoutParams(lp);


                if (y == mTopView.getMeasuredHeight() + mCenterView.getMeasuredHeight() && mState != STATE.DONE) {
                    setState(STATE.DONE);
                } else if (y == mTopView.getMeasuredHeight() && mState != STATE.RELEASE_TO_UP) {
                    setState(STATE.RELEASE_TO_UP);
                } else if (lp.topMargin == 0 && mState != STATE.PULL_TO_UP) {
                    setState(STATE.PULL_TO_UP);
                }


            }
        }

        if (y != getScrollY()) {
            super.scrollTo(x, y);
        }
    }

//    private static class MyHandler extends Handler {
//
//
//        private final WeakReference<TowPageView> mScrollViewContainerWeakReference;
//
//        public MyHandler(TowPageView scrollViewContainer) {
//            mScrollViewContainerWeakReference = new WeakReference<>(scrollViewContainer);
//
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            TowPageView scrollViewContainer = mScrollViewContainerWeakReference.get();
//            if (null != scrollViewContainer) {
//                scrollViewContainer.handleMsg(msg);
//            }
//
//        }
//    }

//    private void handleMsg(Message msg) {
//
//        if (mState != STATE.DONE) {
//
//            MarginLayoutParams lp = (MarginLayoutParams) mCenterView.getLayoutParams();
//
//            if (mState == STATE.UP_ING) {
//                SPEED = (int) (8 + 5 * Math.tan(Math.PI / 2 / getMeasuredHeight() * lp.topMargin));
//                lp.topMargin = Math.max(lp.topMargin - SPEED, 0);
//                mCenterView.setLayoutParams(lp);
//
//                if (lp.topMargin == 0) {
//                    mTimer.cancel();
//                    setState(STATE.DONE);
//                    mCurrentViewIndex = 0;
//                }
//            } else if (mState == STATE.NEXT_ING) {
//                SPEED = (int) (8 + 5 * Math.tan(Math.PI / 2 / getMeasuredHeight() * lp.bottomMargin));
//                lp.bottomMargin = Math.max(lp.bottomMargin - SPEED, 0);
//                mCenterView.setLayoutParams(lp);
//
//                if (lp.bottomMargin == 0) {
//                    mTimer.cancel();
//                    setState(STATE.DONE);
//                    mCurrentViewIndex = 1;
//                }
//            }
//
//        } else {
//            mTimer.cancel();
//        }
//    }


    public TowPageView(Context context) {
        this(context, null);

    }

    public TowPageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public TowPageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init() {
        mICenterView = new DefaultCenterView(getContext());
//        mTimer = new MyTimer(handler);
//        mScroller = new Scroller(getContext());
    }

    public void setCenterView(ICenterView iCenterView) {
        if (iCenterView != null) {
            mICenterView = iCenterView;
        }
    }


    private void setState(STATE state) {
        mState = state;
        if (mState == STATE.UP_ING) {
            mICenterView.OnUping();
            smoothScrollTo(0, 400);
//            mTimer.schedule(5);
            marginAnimator(400);
        } else if (mState == STATE.NEXT_ING) {
            mICenterView.OnNexting();
            smoothScrollTo(mTopView.getMeasuredHeight() + mCenterView.getMeasuredHeight(), 400);
//            mTimer.schedule(5);
            marginAnimator(400);
        } else if (mState == STATE.RELEASE_TO_NEXT) {
            mICenterView.OnReleaseToNext();
        } else if (mState == STATE.RELEASE_TO_UP) {
            mICenterView.OnReleaseToUp();
        } else if (mState == STATE.PULL_TO_NEXT) {
            mICenterView.OnPullToNext();
        } else if (mState == STATE.PULL_TO_UP) {
            mICenterView.OnPullToUp();
        } else if (mState == STATE.DONE) {
            mICenterView.OnDone();
        }
    }

    private void marginAnimator(int duration) {
        final MarginLayoutParams lp = (MarginLayoutParams) mCenterView.getLayoutParams();

        if (mState == STATE.UP_ING) {
            mValueAnimator = ValueAnimator.ofInt(lp.topMargin, 0);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int margin = (int) animation.getAnimatedValue();
                    lp.topMargin = margin;
                    mCenterView.setLayoutParams(lp);

                    if (margin == 0) {
                        mCurrentViewIndex = 0;
                        if (mOnSwitchPageCompletedListener != null) {
                            mOnSwitchPageCompletedListener.onSwitchUpCompleted();
                        }
                        setState(STATE.DONE);
                    }
                }
            });

        } else if (mState == STATE.NEXT_ING) {
            mValueAnimator = ValueAnimator.ofInt(lp.bottomMargin, 0);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int margin = (int) animation.getAnimatedValue();
                    lp.bottomMargin = margin;
                    mCenterView.setLayoutParams(lp);

                    if (margin == 0) {
                        mCurrentViewIndex = 1;
                        if (mOnSwitchPageCompletedListener != null) {
                            mOnSwitchPageCompletedListener.onSwitchNextCompleted();
                        }
                        TowPageView.this.setState(STATE.DONE);
                    }
                }
            });
        }

        mValueAnimator.setDuration(duration);
        mValueAnimator.start();


    }

    public void smoothScrollTo(int y, int duration) {
//        int scrollY = getScrollY();
//        int deltaY = y - scrollY;
//        //1500秒内滑向y
//        mScroller.startScroll(0, scrollY, 0, deltaY, duration);
//        invalidate();
        smoothAnnimator(y, duration);
    }

    //    @Override
//    public void computeScroll() {
//        super.computeScroll();
//        if (mScroller.computeScrollOffset()) {
//            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
//            //通过不断的重绘不断的调用computeScroll方法
//            postInvalidate();
////            invalidate();
//        } else if (mState == STATE.UP_ING) {
//            if (mOnSwitchPageCompletedListener != null) {
//                mOnSwitchPageCompletedListener.onSwitchUpCompleted();
//            }
//        } else if (mState == STATE.NEXT_ING) {
//            if (mOnSwitchPageCompletedListener != null) {
//                mOnSwitchPageCompletedListener.onSwitchNextCompleted();
//            }
//        }
//
//    }

    private void smoothAnnimator(int y, int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(getScrollY(), y);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int sy = (int) animation.getAnimatedValue();
                TowPageView.this.scrollTo(0, sy);

            }
        });

        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }


    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int childTop = t;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            childTop += lp.topMargin;
            child.layout(l, childTop, r, childTop + child.getMeasuredHeight());
            childTop += child.getMeasuredHeight() + lp.bottomMargin;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        addView(mICenterView.getView(), 1);
        mTopView = getChildAt(0);
        mCenterView = getChildAt(1);
        mBottomView = getChildAt(2);

    }


//class MyTimer {
//    private Handler handler;
//    private Timer timer;
//    private MyTask mTask;
//
//    public MyTimer(Handler handler) {
//        this.handler = handler;
//        timer = new Timer();
//    }
//
//    public void schedule(long period) {
//        if (mTask != null) {
//            mTask.cancel();
//            mTask = null;
//        }
//        mTask = new MyTask(handler);
//        timer.schedule(mTask, 0, period);
//    }
//
//    public void cancel() {
//        if (mTask != null) {
//            mTask.cancel();
//            mTask = null;
//        }
//    }
//
//    class MyTask extends TimerTask {
//        private Handler handler;
//
//        public MyTask(Handler handler) {
//            this.handler = handler;
//        }
//
//        @Override
//        public void run() {
//            handler.obtainMessage().sendToTarget();
//        }
//
//    }
//}
}
