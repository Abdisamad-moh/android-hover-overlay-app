package com.example.hoverlockapp;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

public class FloatingService extends Service {
    private static final String TAG = "FloatingService";
    private WindowManager windowManager;
    private View floatingView;
    private View overlayView;
    private GestureDetector gestureDetector;
    private WindowManager.LayoutParams floatingViewParams;

    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't provide binding, so return null
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);

        // Initialize and configure the floating view parameters
        floatingViewParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        floatingViewParams.gravity = Gravity.TOP | Gravity.START;
        floatingViewParams.x = 0;
        floatingViewParams.y = 50;

        windowManager.addView(floatingView, floatingViewParams);

        ImageView imageView = floatingView.findViewById(R.id.floating_image);
        ViewGroup.LayoutParams imageParams = imageView.getLayoutParams();
        imageParams.width = dpToPx(50);  // Convert 50dp to pixels
        imageParams.height = dpToPx(50);
        imageView.setLayoutParams(imageParams);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d(TAG, "Double tap detected.");
                toggleOverlay();
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;  // Necessary to recognize gestures
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        initialX = floatingViewParams.x;
//                        initialY = floatingViewParams.y;
//                        initialTouchX = event.getRawX();
//                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
//                        float dx = event.getRawX() - initialTouchX;
//                        float dy = event.getRawY() - initialTouchY;
//                        floatingViewParams.x = initialX + (int) dx;
//                        floatingViewParams.y = initialY + (int) dy;
//                        windowManager.updateViewLayout(floatingView, floatingViewParams);
                        return true;

                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return false;
            }
        });

        // Create the overlay view
        overlayView = new View(this) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return true; // Block all other touches
            }
        };

        WindowManager.LayoutParams overlayParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
        );
        windowManager.addView(overlayView, overlayParams);
        overlayView.setVisibility(View.GONE);
    }

    private void toggleOverlay() {
        if (overlayView.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "Hiding overlay.");
            overlayView.setVisibility(View.GONE);
            setColorFilter((ImageView) floatingView.findViewById(R.id.floating_image), false);
        } else {
            Log.d(TAG, "Showing overlay.");
            overlayView.setBackgroundResource(R.drawable.shadow_overlay);
            overlayView.setVisibility(View.VISIBLE);
            setColorFilter((ImageView) floatingView.findViewById(R.id.floating_image), true);
        }
        windowManager.removeViewImmediate(floatingView);
        windowManager.addView(floatingView, floatingViewParams);  // Ensure it's on top
    }

    private void setColorFilter(ImageView imageView, boolean locked) {
        if (locked) {
            imageView.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_ATOP);
        } else {
            imageView.clearColorFilter();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
        if (overlayView != null) {
            windowManager.removeView(overlayView);
        }
    }
}
