package com.example.hoverlockapp;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class FloatingService extends Service {
    private static final String TAG = "FloatingService";
    private WindowManager windowManager;
    private View floatingView;
    private View overlayView;
    private GestureDetector gestureDetector;

    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't provide binding, so return null
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);

        // Configure the floating button parameters
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 50;

        windowManager.addView(floatingView, params);

        ImageView imageView = floatingView.findViewById(R.id.floating_image);
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
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    imageView.performClick();  // Ensure this call is made for accessibility
                    return true;
                }
                return false;
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform actions when the image is clicked, if needed
            }
        });

        imageView.performClick(); // Manually call performClick to satisfy accessibility features

        // Create the overlay view
        overlayView = new View(this) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                Rect rect = new Rect();
                floatingView.getGlobalVisibleRect(rect);

                // Expand the touch area
                rect.left -= 10;  // Expand left edge
                rect.right += 10; // Expand right edge
                rect.top -= 10;   // Expand top edge
                rect.bottom += 10; // Expand bottom edge

                if (rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    Log.d(TAG, "Touch within bounds of floating button, forwarding.");
                    imageView.dispatchTouchEvent(event); // Manually forward the event to the imageView
                    return false;
                }
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

//    private void toggleOverlay() {
//        if (overlayView.getVisibility() == View.VISIBLE) {
//            Log.d(TAG, "Overlay is now hidden.");
//            overlayView.setVisibility(View.GONE);
//        } else {
//            Log.d(TAG, "Overlay is now visible.");
//            overlayView.setVisibility(View.VISIBLE);
//        }
//    }
    private void toggleOverlay() {
        if (overlayView.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "Hiding overlay.");
            overlayView.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "Showing overlay.");
            overlayView.setBackgroundResource(R.drawable.shadow_overlay);
            overlayView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
        if (overlayView != null) windowManager.removeView(overlayView);
    }
}
