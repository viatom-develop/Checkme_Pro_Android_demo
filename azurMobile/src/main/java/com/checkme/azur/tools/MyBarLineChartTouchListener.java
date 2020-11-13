package com.checkme.azur.tools;


import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.data.BarLineScatterCandleData;
import com.github.mikephil.charting.data.BarLineScatterCandleRadarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.OnChartGestureListener;
import com.github.mikephil.charting.utils.Highlight;

/**
 * TouchListener for Bar-, Line-, Scatter- and CandleStickChart with handles all
 * touch interaction. Longpress == Zoom out. Double-Tap == Zoom in.
 *
 * @author Philipp Jahoda
 */
public class MyBarLineChartTouchListener<T extends BarLineChartBase<? extends BarLineScatterCandleData<? extends BarLineScatterCandleRadarDataSet<? extends Entry>>>>
        extends SimpleOnGestureListener implements OnTouchListener {

    /** the original touch-matrix from the chart */
    private Matrix mMatrix1 = new Matrix();

    /** matrix for saving the original matrix state */
    private Matrix mSavedMatrix = new Matrix();

    /** point where the touch action started */
    private PointF mTouchStartPoint = new PointF();

    /** center between two pointers (fingers on the display) */
    private PointF mTouchPointCenter = new PointF();

    // states
    private static final int NONE = 0;
    private static final int DRAG = 1;

    private static final int X_ZOOM = 2;
    private static final int Y_ZOOM = 3;
    private static final int PINCH_ZOOM = 4;
    private static final int POST_ZOOM = 5;

    /** integer field that holds the current touch-state */
    private int mTouchMode = NONE;

    private float mSavedXDist = 1f;
    private float mSavedYDist = 1f;
    private float mSavedDist = 1f;

    /** the last highlighted object */
    private Highlight mLastHighlighted;

    /** the chart the listener represents */
    private T mChart1;

    /** the gesturedetector used for detecting taps and longpresses, ... */
    private GestureDetector mGestureDetector;

    //Joe add
    private Matrix mMatrix2 = new Matrix();
    private T mChart2;

    public MyBarLineChartTouchListener(T chart1,  T chart2, Matrix matrix1, Matrix matrix2) {
        this.mChart1 = chart1;
        this.mMatrix1 = matrix1;
        this.mChart2 = chart2;
        this.mMatrix2 = matrix2;

        mGestureDetector = new GestureDetector(chart1.getContext(), this);//warning single
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mTouchMode == NONE) {
            mGestureDetector.onTouchEvent(event);
        }

        if (!mChart1.isDragEnabled() && !mChart1.isScaleEnabled())
            return true;

        if (!mChart2.isDragEnabled() && !mChart2.isScaleEnabled())
            return true;

        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                saveTouchStart(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:

                if (event.getPointerCount() >= 2) {

                    mChart1.disableScroll();
                    mChart2.disableScroll();

                    saveTouchStart(event);

                    // get the distance between the pointers on the x-axis
                    mSavedXDist = getXDist(event);

                    // get the distance between the pointers on the y-axis
                    mSavedYDist = getYDist(event);

                    // get the total distance between the pointers
                    mSavedDist = spacing(event);

                    if (mSavedDist > 10f) {

                        if (mChart1.isPinchZoomEnabled() && mChart2.isPinchZoomEnabled()) {
                            mTouchMode = PINCH_ZOOM;
                        } else {
//                            if (mSavedXDist > mSavedYDist)
//                                mTouchMode = X_ZOOM;
//                            else
//                                mTouchMode = Y_ZOOM;
                            //warning 屏蔽Y缩放
                            mTouchMode = X_ZOOM;
                        }
                    }

                    // determine the touch-pointer center
                    midPoint(mTouchPointCenter, event);
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (mTouchMode == DRAG) {

                    mChart1.disableScroll();
                    mChart2.disableScroll();

                    if (mChart1.isDragEnabled() && mChart2.isDragEnabled())
                        performDrag(event);

                } else if (mTouchMode == X_ZOOM || mTouchMode == Y_ZOOM || mTouchMode == PINCH_ZOOM) {

                    mChart1.disableScroll();
                    mChart2.disableScroll();

                    if (mChart1.isScaleEnabled() && mChart2.isScaleEnabled())
                        performZoom(event);

                } else if (mTouchMode == NONE
                        && Math.abs(distance(event.getX(), mTouchStartPoint.x, event.getY(),
                        mTouchStartPoint.y)) > 25f) {

                    if (mChart1.hasNoDragOffset() && mChart2.hasNoDragOffset()) {

                        if (!mChart1.isFullyZoomedOut() && !mChart2.isFullyZoomedOut())
                            mTouchMode = DRAG;

                    } else {
                        mTouchMode = DRAG;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                mTouchMode = NONE;
                mChart1.enableScroll();
                mChart2.enableScroll();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mTouchMode = POST_ZOOM;
                break;
        }

        // Perform the transformation, update the chart
        mMatrix1 = mChart1.getTransformer().refresh(mMatrix1, mChart1);
        mMatrix2 = mChart2.getTransformer().refresh(mMatrix2, mChart2);

        return true; // indicate event was handled
    }

    /**
     * ################ ################ ################ ################
     */
    /** BELOW CODE PERFORMS THE ACTUAL TOUCH ACTIONS */

    /**
     * Saves the current Matrix state and the touch-start point.
     *
     * @param event
     */
    private void saveTouchStart(MotionEvent event) {

        mSavedMatrix.set(mMatrix1);
        mTouchStartPoint.set(event.getX(), event.getY());
    }

    /**
     * Performs all necessary operations needed for dragging.
     *
     * @param event
     */
    private void performDrag(MotionEvent event) {

        mMatrix1.set(mSavedMatrix);
        mMatrix2.set(mSavedMatrix);
        PointF dragPoint = new PointF(event.getX(), event.getY());

        // check if axis is inverted
        if (!mChart1.isInvertYAxisEnabled()) {
            mMatrix1.postTranslate(dragPoint.x - mTouchStartPoint.x, dragPoint.y
                    - mTouchStartPoint.y);
        } else {
            mMatrix1.postTranslate(dragPoint.x - mTouchStartPoint.x, -(dragPoint.y
                    - mTouchStartPoint.y));
        }

        if (!mChart2.isInvertYAxisEnabled()) {
            mMatrix2.postTranslate(dragPoint.x - mTouchStartPoint.x, dragPoint.y
                    - mTouchStartPoint.y);
        } else {
            mMatrix2.postTranslate(dragPoint.x - mTouchStartPoint.x, -(dragPoint.y
                    - mTouchStartPoint.y));
        }
    }

    /**
     * Performs the all operations necessary for pinch and axis zoom.
     *
     * @param event
     */
    private void performZoom(MotionEvent event) {

        if (event.getPointerCount() >= 2) {

            // get the distance between the pointers of the touch
            // event
            float totalDist = spacing(event);

            if (totalDist > 10f) {

                // get the translation
                PointF t = getTrans(mTouchPointCenter.x, mTouchPointCenter.y);

                // take actions depending on the activated touch
                // mode
                if (mTouchMode == PINCH_ZOOM) {

                    float scale = totalDist / mSavedDist; // total
                    // scale

                    mMatrix1.set(mSavedMatrix);
                    mMatrix1.postScale(scale, scale, t.x, t.y);

                    mMatrix2.set(mSavedMatrix);
                    mMatrix2.postScale(scale, scale, t.x, t.y);

                } else if (mTouchMode == X_ZOOM) {

                    float xDist = getXDist(event);
                    float scaleX = xDist / mSavedXDist; // x-axis
                    // scale

                    mMatrix1.set(mSavedMatrix);
                    mMatrix1.postScale(scaleX, 1f, t.x, t.y);

                    mMatrix2.set(mSavedMatrix);
                    mMatrix2.postScale(scaleX, 1f, t.x, t.y);

                } else if (mTouchMode == Y_ZOOM) {

                    float yDist = getYDist(event);
                    float scaleY = yDist / mSavedYDist; // y-axis
                    // scale

                    mMatrix1.set(mSavedMatrix);

                    // y-axis comes from top to bottom, revert y
                    mMatrix1.postScale(1f, scaleY, t.x, t.y);

                    mMatrix2.set(mSavedMatrix);

                    // y-axis comes from top to bottom, revert y
                    mMatrix2.postScale(1f, scaleY, t.x, t.y);

                }
            }
        }
    }

    /**
     * ################ ################ ################ ################
     */
    /** DOING THE MATH BELOW ;-) */

    /**
     * returns the distance between two points
     *
     * @param eventX
     * @param startX
     * @param eventY
     * @param startY
     * @return
     */
    private static float distance(float eventX, float startX, float eventY, float startY) {
        float dx = eventX - startX;
        float dy = eventY - startY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Determines the center point between two pointer touch points.
     *
     * @param point
     * @param event
     */
    private static void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2f, y / 2f);
    }

    /**
     * returns the distance between two pointer touch points
     *
     * @param event
     * @return
     */
    private static float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * calculates the distance on the x-axis between two pointers (fingers on
     * the display)
     *
     * @param e
     * @return
     */
    private static float getXDist(MotionEvent e) {
        float x = Math.abs(e.getX(0) - e.getX(1));
        return x;
    }

    /**
     * calculates the distance on the y-axis between two pointers (fingers on
     * the display)
     *
     * @param e
     * @return
     */
    private static float getYDist(MotionEvent e) {
        float y = Math.abs(e.getY(0) - e.getY(1));
        return y;
    }

    /**
     * returns the correct translation depending on the provided x and y touch
     * points
     *
     * @param e
     * @return
     */
    public PointF getTrans(float x, float y) {

        float xTrans = x - mChart1.getOffsetLeft();
        float yTrans = 0f;

        // check if axis is inverted
        if (!mChart1.isInvertYAxisEnabled()) {
            yTrans = -(mChart1.getMeasuredHeight() - y - mChart1.getOffsetBottom());
        } else {
            yTrans = -(y - mChart1.getOffsetTop());
        }

        return new PointF(xTrans, yTrans);
    }

    /**
     * ################ ################ ################ ################
     */
    /** GETTERS AND GESTURE RECOGNITION BELOW */

    /**
     * returns the matrix object the listener holds
     *
     * @return
     */
    public Matrix getMatrix() {
        return mMatrix1;
    }

    /**
     * returns the touch mode the listener is currently in
     *
     * @return
     */
    public int getTouchMode() {
        return mTouchMode;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {

        //warning single
        OnChartGestureListener l = mChart1.getOnChartGestureListener();

        if (l != null) {
            l.onChartDoubleTapped(e);
            return super.onDoubleTap(e);
        }

        // check if double-tap zooming is enabled
        if (mChart1.isDoubleTapToZoomEnabled()) {

            PointF trans = getTrans(e.getX(), e.getY());

            mChart1.zoom(1.4f, 1.4f, trans.x, trans.y);

            Log.i("BarlineChartTouch", "Double-Tap, Zooming In, x: " + trans.x + ", y: " + trans.y);
        }

        // check if double-tap zooming is enabled
        if (mChart2.isDoubleTapToZoomEnabled()) {

            PointF trans = getTrans(e.getX(), e.getY());

            mChart2.zoom(1.4f, 1.4f, trans.x, trans.y);

            Log.i("BarlineChartTouch", "Double-Tap, Zooming In, x: " + trans.x + ", y: " + trans.y);
        }

        return super.onDoubleTap(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {

        //warning single
        OnChartGestureListener l = mChart1.getOnChartGestureListener();

        if (l != null) {

            l.onChartLongPressed(e);
        } else if (mTouchMode == NONE) {

            mChart1.fitScreen();
            mChart2.fitScreen();

            Log.i("BarlineChartTouch",
                    "Longpress, resetting zoom and drag, adjusting chart bounds to screen.");

            // PointF trans = getTrans(e.getX(), e.getY());
            //
            // mChart.zoomOut(trans.x, trans.y);
            //
            // Log.i("BarlineChartTouch", "Longpress, Zooming Out, x: " +
            // trans.x +
            // ", y: " + trans.y);
        }
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        //warning single
        OnChartGestureListener l = mChart1.getOnChartGestureListener();

        if (l != null) {

            l.onChartSingleTapped(e);
        }

        Highlight h = mChart1.getHighlightByTouchPoint(e.getX(), e.getY());

        if (h == null || h.equalTo(mLastHighlighted)) {
            mChart1.highlightTouch(null);
            mChart2.highlightTouch(null);
            mLastHighlighted = null;
        } else {
            mLastHighlighted = h;
            mChart1.highlightTouch(h);
            mChart2.highlightTouch(h);
        }

        return super.onSingleTapUp(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return super.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        OnChartGestureListener l = mChart1.getOnChartGestureListener();

        if (l != null)
            l.onChartFling(e1, e2, velocityX, velocityY);

        return super.onFling(e1, e2, velocityX, velocityY);
    }
}

