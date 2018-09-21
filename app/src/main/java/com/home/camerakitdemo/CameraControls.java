package com.home.camerakitdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;

public class CameraControls extends LinearLayout {

    private int cameraViewId = -1, coverViewId = -1;
    private boolean pendingVideoCapture, capturingVideo;
    private long captureDownTime, captureStartTime;
    private Context context;
    private CameraView cameraView;
    private View coverView;
    private ShowImageDialog showImageDialog;
    private Bitmap bitmap;

    @BindView(R.id.facingButton)
    ImageView facingButton;

    public CameraControls(Context context) {
        this(context, null);
        this.context = context;
    }

    public CameraControls(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public CameraControls(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        LayoutInflater.from(getContext()).inflate(R.layout.camera_controls, this);
        ButterKnife.bind(this);
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CameraControls,
                    0, 0);
            try {
                cameraViewId = a.getResourceId(R.styleable.CameraControls_camera, -1);
                coverViewId = a.getResourceId(R.styleable.CameraControls_cover, -1);
            } finally {
                a.recycle();
            }
        }
    }

    /**
     * onAttachedToWindow运行在onResume之后
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (cameraViewId != -1) {
            View view = getRootView().findViewById(cameraViewId);
            if (view instanceof CameraView) {
                cameraView = (CameraView) view;
                cameraView.bindCameraKitListener(this);
                setFacingImageBasedOnCamera();
            }
        }
        if (coverViewId != -1) {
            View view = getRootView().findViewById(coverViewId);
            if (view != null) {
                coverView = view;
                coverView.setVisibility(GONE);
            }
        }
    }

    private void setFacingImageBasedOnCamera() {
        if (cameraView.isFacingFront()) {
            facingButton.setImageResource(R.drawable.ic_facing_back);
        } else {
            facingButton.setImageResource(R.drawable.ic_facing_front);
        }
    }

    /**
     * 將取得的圖片檔 透過Dialog呈現
     */
    public void imageCaptured(CameraKitImage image) {
        byte[] jpeg = image.getJpeg();
        bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
        showImageDialog =
                new ShowImageDialog(
                        context, R.style.ShowImageDialog, bitmap);
        showImageDialog.setOnSaveListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Save", Toast.LENGTH_SHORT).show();
            }
        });
        showImageDialog.setOnExitListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageDialog.dismiss();
            }
        });
        showImageDialog.show();
    }

    /**
     * 拍照
     */
    @OnTouch(R.id.captureButton)
    boolean onTouchCapture(View view, MotionEvent motionEvent) {
        handleViewTouchFeedback(view, motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                cameraView.setFlash(CameraKit.Constants.FLASH_ON);
                captureDownTime = System.currentTimeMillis();
                pendingVideoCapture = true;
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (pendingVideoCapture) {
                            capturingVideo = true;
                            cameraView.captureVideo();
                        }
                    }
                }, 250);
                break;
            }
            case MotionEvent.ACTION_UP: {
                cameraView.setFlash(CameraKit.Constants.FLASH_ON);
                pendingVideoCapture = false;

                if (capturingVideo) {
                    capturingVideo = false;
                    cameraView.stopVideo();
                } else {
                    captureStartTime = System.currentTimeMillis();
                    cameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
                        @Override
                        public void callback(CameraKitImage event) {
                            imageCaptured(event);
                        }
                    });
                }
                break;
            }
        }
        return true;
    }

    /**
     * 翻轉鏡頭
     */
    @OnTouch(R.id.facingButton)
    boolean onTouchFacing(final View view, MotionEvent motionEvent) {
        handleViewTouchFeedback(view, motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP: {
                coverView.setAlpha(0);
                coverView.setVisibility(VISIBLE);
                coverView.animate()
                        .alpha(1)
                        .setStartDelay(0)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if (cameraView.isFacingFront()) {
                                    cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                                    changeViewImageResource((ImageView) view, R.drawable.ic_facing_front);
                                } else {
                                    cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                                    changeViewImageResource((ImageView) view, R.drawable.ic_facing_back);
                                }
                                coverView.animate()
                                        .alpha(0)
                                        .setStartDelay(200)
                                        .setDuration(300)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                coverView.setVisibility(GONE);
                                            }
                                        })
                                        .start();
                            }
                        })
                        .start();
                break;
            }
        }
        return true;
    }

    /**
     * 針對按下放開的行為, 設定對應的動畫效果
     */
    boolean handleViewTouchFeedback(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                touchDownAnimation(view);
                return true;
            }
            case MotionEvent.ACTION_UP: {
                touchUpAnimation(view);
                return true;
            }
            default: {
                return true;
            }
        }
    }

    /**
     * 縮小動畫
     */
    void touchDownAnimation(View view) {
        view.animate()
                .scaleX(0.88f)
                .scaleY(0.88f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    /**
     * 還原動畫
     */
    void touchUpAnimation(View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    /**
     * 將圖片呈現轉360度的動畫效果, 並且在轉完之前 變成另一張圖片
     */
    void changeViewImageResource(final ImageView imageView, @DrawableRes final int resId) {
        imageView.setRotation(0);
        imageView.animate()
                .rotationBy(360)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .start();

        /** view自带的定时器, 120毫秒之後執行內容 */
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource(resId);
            }
        }, 120);
    }
}
