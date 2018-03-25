package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class ExtendedEditText extends EditText {

    public ExtendedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttrs(context, attrs);
    }

    public ExtendedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public ExtendedEditText(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private Drawable mLineDrawable;
    private int mLineDrawableHeight;
    private int mLineDrawableOffset;

    public Drawable getLineDrawable() {
        return mLineDrawable;
    }

    public void setLineDrawable(Drawable drawable) {
        mLineDrawable = drawable;
        invalidate();
    }

    public void setLineDrawable(int resourceId) {
        Drawable drawable = getContext().getResources().getDrawable(resourceId);
        setLineDrawable(drawable);
    }

    public int getLineDrawableHeight() {
        return mLineDrawableHeight;
    }

    public void setLineDrawableHeight(int lineDrawableHeight) {
        mLineDrawableHeight = lineDrawableHeight;
        invalidate();
    }

    public int getLineDrawableOffset() {
        return mLineDrawableOffset;
    }

    public void setLineDrawableOffset(int lineDrawableOffset) {
        mLineDrawableOffset = lineDrawableOffset;
        invalidate();
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtendedEditText);
        String typeFaceAssetPath = a.getString(R.styleable.ExtendedEditText_typeFaceAssetPath);
        Typeface typeface = TypefaceHelper.get(context, typeFaceAssetPath);
        if (typeface != null) {
            Typeface oldTypeface = getTypeface();
            if (oldTypeface != null) {
                int oldTypefaceStyle = oldTypeface.getStyle();
                setTypeface(typeface, oldTypefaceStyle);
            } else {
                setTypeface(typeface);
            }
        }
        mLineDrawable = a.getDrawable(R.styleable.ExtendedEditText_lineDrawable);
        mLineDrawableHeight = a.getDimensionPixelSize(R.styleable.ExtendedEditText_lineDrawableHeight, 1);
        mLineDrawableOffset = a.getDimensionPixelSize(R.styleable.ExtendedEditText_lineDrawableOffset, 0);
        a.recycle();
    }

    public void addInputFilter(InputFilter... addFilters) {
        InputFilter[] oldFilters = getFilters();
        InputFilter[] newFilters = new InputFilter[oldFilters.length + addFilters.length];
        System.arraycopy(oldFilters, 0, newFilters, 0, oldFilters.length);
        System.arraycopy(addFilters, 0, newFilters, oldFilters.length, addFilters.length);
        setFilters(newFilters);
    }

    private Rect mRect = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLineDrawable != null) {
            int lineCount = getLineCount();
            for (int i = 0; i < lineCount; i++) {
                getLineBounds(i, mRect);
                float top = mRect.bottom - mLineDrawableHeight + mLineDrawableOffset;
                float bottom = mRect.bottom + mLineDrawableOffset;
                mLineDrawable.setBounds(mRect.left, (int) top, mRect.right, (int) bottom);
                mLineDrawable.draw(canvas);
            }
        }
    }

    private View mClearView;

    private void updateClearView() {
        if (mClearView != null) {
            if (isEnabled()) {
                mClearView.setVisibility(TextUtils.isEmpty(getText()) ? View.GONE : View.VISIBLE);
            } else {
                mClearView.setVisibility(View.GONE);
            }
        }
    }

    public void setClearView(View view) {
        mClearView = view;
        updateClearView();
        mClearView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setText(null);
            }
        });
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        updateClearView();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateClearView();
    }

}
