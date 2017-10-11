package com.example.jfnickly.antifragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import static java.security.AccessController.getContext;

/**
 * TODO: document your custom view class.
 */
public class XListView extends ViewGroup {

    public static int MAX_COLUMN_ROW_COUNT = 12;
    public static float MIN_SIZE = 96;

    private float minDPSize;
    private int targetColumnCount = 1;
    private int actualColumnCount = 1;

    private float columnGap = 0;

    private int targetRowCount = 0; // < 1 = auto size
    private int actualRowCount = 1;

    private float rowGap = 0;


    public XListView(Context context) {
        super(context);
        init(null, 0);
    }

    public XListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public XListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int x = 0, y = 0;
        for(int i = 0; i < count; i++) {

            View child = getChildAt(i);

            if (child.getVisibility() == View.GONE)
                continue;

            x = (int)child.getX();
            y = (int)child.getY();

            child.layout(
                    x,
                    y,
                    x + child.getMeasuredWidth(),
                    y + child.getMeasuredHeight());

            child.requestLayout();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int pWidth = MeasureSpec.getSize(widthMeasureSpec) - (getPaddingLeft() + getPaddingRight()),
            pHeight = MeasureSpec.getSize(heightMeasureSpec) - (getPaddingTop() + getPaddingBottom());

        int tWidth = pWidth, tHeight = pHeight;
        int cWidth = getColumnSize(pWidth);
        int tallestForColumn = 0;



        int x = getPaddingLeft();
        int y = getPaddingTop();
        int onColumn = 0;
        int childHeight = 0;

        int count = getChildCount();
        int pI = 0;
        View lChild = null;
        for(int i = 0; i < count; i++) {

            View child = getChildAt(i);

            if (child.getVisibility() == View.GONE)
                continue;
            lChild = child;
            child.getLayoutParams().width = cWidth;
            child.getLayoutParams().height = LayoutParams.WRAP_CONTENT;

            measureChild(child, cWidth, 0);

            childHeight = child.getHeight();

            if (childHeight > pHeight) {
                child.getLayoutParams().height = pHeight;
                measureChild(child, cWidth, 0);
                childHeight = child.getHeight();
            }

            if (onColumn >= actualColumnCount) {

                for(int j = pI; j < i; j++) {
                    View jChild = getChildAt(j);
                    if (jChild.getVisibility() == GONE)
                        continue;
                    jChild.setMinimumHeight(tallestForColumn);
                }

                y+=tallestForColumn;
                x =getPaddingLeft();
                onColumn = 0;
                tallestForColumn = 0;

                pI = i;
            }

            if (tallestForColumn < childHeight)
                tallestForColumn = childHeight;

            child.layout(x, y, x+cWidth, y+childHeight);

            onColumn++;
            x += cWidth;

        }


        if (lChild != null) {
            int fx = (int)lChild.getX();
            int w = pWidth - fx + getPaddingRight();
            lChild.getLayoutParams().width = w;
            lChild.getLayoutParams().height = lChild.getMeasuredHeight();
            measureChild(lChild, w, 0);
        }



        setMeasuredDimension(tWidth, tHeight);
    }

    private int getColumnSize(int size) {
        float fSize = (float)(size);

        float pSize = fSize/targetColumnCount;

        if (pSize < minDPSize) {
            pSize = minDPSize;
        }

        int cSize = (int)(fSize/pSize);

        pSize = fSize/cSize;

        actualColumnCount = cSize;

        Log.d("TEST", "actualColumnCount = " + actualColumnCount);



        return (int)pSize;


    }

    private int toDP(float value) {
        return (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics());
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.XListView, defStyle, 0);

        minDPSize = toDP(MIN_SIZE);

        targetColumnCount = a.getInt(R.styleable.XListView_columnCount, 1);
        columnGap = a.getDimensionPixelSize(R.styleable.XListView_columnGap, toDP(8));

        if (targetColumnCount < 1)
            targetColumnCount = 1;
        if (targetColumnCount > MAX_COLUMN_ROW_COUNT)
            targetColumnCount = MAX_COLUMN_ROW_COUNT;

        Log.d("TEST", targetColumnCount + ", " + columnGap);
        a.recycle();
    }

}
/*

    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.XListView, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.XListView_exampleString);
        mExampleColor = a.getColor(
                R.styleable.XListView_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.XListView_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.XListView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.XListView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the text.
        canvas.drawText(mExampleString,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }
    }

    public String getExampleString() {
        return mExampleString;
    }
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    public int getExampleColor() {
        return mExampleColor;
    }

    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    public float getExampleDimension() {
        return mExampleDimension;
    }

    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
*/