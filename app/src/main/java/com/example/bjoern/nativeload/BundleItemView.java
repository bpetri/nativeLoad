package com.example.bjoern.nativeload;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * Created by bjoern on 29.05.15.
 */
public class BundleItemView extends TextView {
    public BundleItemView (Context context, AttributeSet ats, int ds) {
        super(context, ats, ds);
        init();
    }
    public BundleItemView (Context context) {
        super(context);
        init();
    }
    public BundleItemView (Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
    }
    @Override
    public void onDraw(Canvas canvas) {
// Use the base TextView to render the text.
        super.onDraw(canvas);
    }
}
