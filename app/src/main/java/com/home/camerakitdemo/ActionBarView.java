package com.home.camerakitdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class ActionBarView extends LinearLayout {

    public ActionBarView(Context context) {
        super(context);
        initView(context);
    }

    public ActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        attributesSettings(context, attrs);
    }

    public ActionBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.action_bar, this);
    }

    private void attributesSettings(Context context, AttributeSet attrs) {
    }
}
