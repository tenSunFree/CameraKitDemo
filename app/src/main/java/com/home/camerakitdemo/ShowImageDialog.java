package com.home.camerakitdemo;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 自定义Dialog
 */
public class ShowImageDialog extends Dialog {

    private Bitmap bitmap;
    private TextView saveTextView, exitTextView;
    private ImageView imageView;
    private String title, content;
    private View.OnClickListener onSaveListener, onExitListener;

    public ShowImageDialog(Context context, Bitmap bitmap) {
        super(context);
        this.bitmap = bitmap;
    }

    /**
     * @param context 上下文
     * @param theme   给dialog设置的主题
     */
    public ShowImageDialog(Context context, int theme, Bitmap bitmap) {
        super(context, theme);
        this.bitmap = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_show_image);

        initializationView();
        initializeBusinessDetails();
    }

    private void initializationView() {
        imageView = findViewById(R.id.imageView);
        saveTextView = findViewById(R.id.saveTextView);
        exitTextView = findViewById(R.id.exitTextView);
    }

    private void initializeBusinessDetails() {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        if (onSaveListener != null) {
            saveTextView.setVisibility(View.VISIBLE);
            saveTextView.setOnClickListener(onSaveListener);
        } else {
            saveTextView.setVisibility(View.GONE);
        }
        if (onExitListener != null) {
            exitTextView.setVisibility(View.VISIBLE);
            exitTextView.setOnClickListener(onExitListener);
        } else {
            exitTextView.setVisibility(View.GONE);
        }
    }

    public void setTitleText(String title) {
        this.title = title;
    }

    public void setContentText(String content) {
        this.content = content;
    }

    public void setOnSaveListener(View.OnClickListener onSaveListener) {
        this.onSaveListener = onSaveListener;
    }

    public void setOnExitListener(View.OnClickListener onExitListener) {
        this.onExitListener = onExitListener;
    }
}