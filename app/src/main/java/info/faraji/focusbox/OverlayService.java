package info.faraji.focusbox;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private LinearLayout overlayView;
    private WindowManager.LayoutParams params;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences prefs = getSharedPreferences("focusbox_prefs", MODE_PRIVATE);
        int overlayColor = intent.getIntExtra("overlayColor", prefs.getInt("overlayColor", 0x5933B5E5));
        int overlayAlpha = intent.getIntExtra("overlayAlpha", prefs.getInt("overlayAlpha", 35));
        int overlayHeight = intent.getIntExtra("overlayHeight", prefs.getInt("overlayHeight", 200));
        int overlayY = prefs.getInt("overlayY", 0);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlayView = new LinearLayout(this);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(overlayColor);
        overlayView.setBackground(drawable);

        int alpha = (int) (255 * (overlayAlpha / 100.0f));
        overlayView.getBackground().setAlpha(alpha);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayHeight,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP;
        params.y = overlayY;

        overlayView.setOnTouchListener(new View.OnTouchListener() {
            private int initialY;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialY = params.y;
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(overlayView, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("overlayY", params.y);
                        editor.apply();
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(overlayView, params);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (overlayView != null) windowManager.removeView(overlayView);
        super.onDestroy();
    }
}
