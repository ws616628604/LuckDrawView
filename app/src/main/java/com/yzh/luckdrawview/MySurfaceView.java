package com.yzh.luckdrawview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：YZH
 * <p>
 * 创建时间：2019/1/7 18:32
 * <p>
 * 描述：
 * <p>
 * 修订历史：
 */
public class MySurfaceView extends SurfaceView {
    private TextView mTextView;
    private Paint mPaint;
    private int mColors[];

    private int mIds[];
    private float mRadius;
    private boolean mStart;
    private float mMaxRadius;
    private float mMinRadius;
    private float mSpeed;
    private long mLastTime;
    private String mText = "开始";
    private String mTextStop = "清屏";
    private Matrix mMatrix;
    private RectF mClickRectF;
    private boolean mStop;
    private int mSelectIndex = -1;

    public MySurfaceView(Context context) {
        super(context);
        initView(context);
        SurfaceHolder holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {


            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mStart = true;
                new Thread() {
                    @Override
                    public void run() {
                        boolean add = true;
                        while (mStart) {
                            long time = System.currentTimeMillis();
                            if (add) {
                                mRadius = mRadius + mSpeed;
                                if (mRadius > mMaxRadius) {
                                    add = false;
                                }
                            } else {
                                mRadius = mRadius - mSpeed;
                                if (mRadius < mMinRadius) {
                                    add = true;
                                }
                            }
                            while (System.currentTimeMillis() - time < 33) {
                                Thread.yield();
                            }
                        }
                    }
                }.start();
                new Thread() {
                    @Override
                    public void run() {
                        while (mStart) {
                            drawCircle();
                            while (System.currentTimeMillis() - mLastTime < 33) {
                                Thread.yield();
                            }
                        }
                    }
                }.start();

                Canvas canvas = getHolder().lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK);
                    synchronized (this) {
                        changeText(mText);
                        canvas.save();
                        canvas.setMatrix(mMatrix);
                        mTextView.draw(canvas);
                        canvas.restore();
                    }
                }
                getHolder().unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mStart = false;
            }
        });

        setFocusable(true);
        setFocusableInTouchMode(true);

    }

    private TextView getView(CharSequence text) {
        float bgRadius = dip2px(5);
        int tb = (int) dip2px(5);
        int lr = (int) dip2px(12);
        int bgColor = 0xEFE7E7E7;
        int txColor = 0xde000000;

        // 外部矩形弧度
        float[] outerR = {bgRadius, bgRadius, bgRadius, bgRadius, bgRadius, bgRadius, bgRadius, bgRadius};
        // 内部矩形与外部矩形的距离
        RectF inset = null;
        // 内部矩形弧度
        float[] innerRadii = null;
        RoundRectShape roundRectShape = new RoundRectShape(outerR, inset, innerRadii);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        //指定填充颜色
        Paint paint = shapeDrawable.getPaint();
        paint.setAntiAlias(true);
        paint.setColor(bgColor);
        // 指定填充模式
        paint.setStyle(Paint.Style.FILL);

        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setBackground(shapeDrawable);
        textView.setPadding(lr, tb, lr, tb);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
        textView.setTextColor(txColor);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return textView;
    }

    private void initView(Context context) {
        dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        mRadius = dip2px(40);
        mIds = new int[30];
        for (int i = 0; i < mIds.length; i++) {
            mIds[i] = -1;
        }
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(dip2px(24));
        mColors = new int[10];
        mColors[0] = Color.BLUE;
        mColors[1] = Color.RED;
        mColors[2] = Color.GREEN;
        mColors[3] = Color.YELLOW;
        mColors[4] = Color.CYAN;
        mColors[5] = Color.MAGENTA;
        mColors[6] = Color.DKGRAY;
        mColors[7] = Color.WHITE;
        mColors[8] = Color.LTGRAY;
        mColors[9] = Color.GRAY;


        mMaxRadius = dip2px(50);
        mMinRadius = dip2px(40);
        mSpeed = dip2px(1);
        mTextView = getView(mText);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        int pointerId = event.getPointerId(actionIndex);
        if (event.getAction() == MotionEvent.ACTION_DOWN && mStop && mClickRectF.contains(event.getX(actionIndex), event.getY(actionIndex))) {
            mStop = false;
            synchronized (this) {
                for (int i = 0; i < mIds.length; i++) {
                    mIds[i] = -1;
                }
                mCoordinates.clear();
                mSelectIndex = -1;
                changeText(mText);

            }
            return true;
        }

        if (mStop) {
            return true;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mClickRectF.contains(event.getX(actionIndex), event.getY(actionIndex))) {

                    synchronized (this) {
                        if (mCoordinates.size() == 0) {
                            return true;
                        }
                        mStop = true;
                        changeText(mTextStop);
                    }
                    startSelect();

                    return true;
                }
                for (int i = 0; i < mIds.length; i++) {
                    if (mIds[i] == -1) {
                        mIds[i] = pointerId;
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                for (int i = 0; i < mIds.length; i++) {
                    if (mIds[i] == pointerId) {
                        mIds[i] = -1;
                        break;
                    }
                }
                break;
        }
        synchronized (this) {
            mCoordinates.clear();
            for (int i = 0; i < mIds.length; i++) {
                int id = mIds[i];
                if (id == -1) {
                    continue;
                }
                int pointerIndex = event.findPointerIndex(id);
                float x = event.getX(pointerIndex);
                float y = event.getY(pointerIndex);
                mCoordinates.add(new Coordinate(x, y, mColors[i % mColors.length]));
            }
        }
        drawCircle();

        return true;
    }

    private void changeText(String text) {
        mTextView.setText(text);

        mTextView.measure(0, 0);
        int measuredWidth = mTextView.getMeasuredWidth();
        int measuredHeight = mTextView.getMeasuredHeight();
        mTextView.layout(0, 0, measuredWidth, measuredHeight);
        float x = getWidth() * 0.5f;
        float y = getHeight() * 0.5f;
        mClickRectF = new RectF(x - measuredWidth * 0.5f, y - measuredHeight * 0.5f, x + measuredWidth * 0.5f, y + measuredHeight * 0.5f);
        mMatrix = new Matrix();
        mMatrix.postTranslate(x - measuredWidth * 0.5f, y - measuredHeight * 0.5f);

    }

    private void startSelect() {
        new Thread() {
            @Override
            public void run() {
                int size = mCoordinates.size();
                if (size == 0) {
                    return;
                }
                if (size == 1) {
                    synchronized (this) {
                        if (!mStop) {
                            return;
                        }
                        mSelectIndex = 0;
                        return;
                    }
                }
                int v = (int) (Math.random() * size);

                for (int i = 0; i <= size + v; i++) {
                    synchronized (this) {
                        if (!mStop) {
                            return;
                        }
                        mSelectIndex = i % size;
                    }
                    long timeMillis = System.currentTimeMillis();
                    while (System.currentTimeMillis() - timeMillis < 200) {
                        if (!mStop) {
                            return;
                        }
                        Thread.yield();
                    }
                }
            }
        }.start();
    }

    private List<Coordinate> mCoordinates = new ArrayList<>();

    private class Coordinate {
        private float x;
        private float y;
        private int color;

        public Coordinate(float x, float y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    private void drawCircle() {
        synchronized (this) {
            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.BLACK);

                if (mSelectIndex != -1 && mStop) {
                    Coordinate coordinate = mCoordinates.get(mSelectIndex);
                    mPaint.setColor(0x7FFFFF00);
                    canvas.drawCircle(coordinate.x, coordinate.y, mRadius * 2, mPaint);
                }

                for (Coordinate coordinate : mCoordinates) {

                    mPaint.setColor(coordinate.color);
                    canvas.drawCircle(coordinate.x, coordinate.y, mRadius, mPaint);
                }

                canvas.save();
                canvas.setMatrix(mMatrix);
                mTextView.draw(canvas);
                canvas.restore();
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
        mLastTime = System.currentTimeMillis();
    }

    private DisplayMetrics dm;

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private float dip2px(float dpValue) {
        return dpValue * dm.density;
    }
}
