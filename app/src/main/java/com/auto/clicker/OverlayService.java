package com.auto.clicker;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class OverlayService extends Service {
    public static boolean isRunning = false;
    WindowManager wm;
    View overlayView;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;

        // Parse rules
        try {
            String rulesJson = intent.getStringExtra("rules");
            AutoClickService.rules.clear();
            if (rulesJson != null) {
                JSONArray arr = new JSONArray(rulesJson);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    AutoClickService.rules.add(new AutoClickService.Rule(
                        obj.getString("trigger"),
                        obj.getString("action"),
                        obj.optString("typeText", "")
                    ));
                }
            }
        } catch (Exception e) {}

        showOverlay();
        return START_STICKY;
    }

    void showOverlay() {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 10;
        params.y = 100;

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay, null);

        Button btnStop = overlayView.findViewById(R.id.btnStop);
        Button btnPause = overlayView.findViewById(R.id.btnPause);
        TextView tvStatus = overlayView.findViewById(R.id.tvStatus);

        tvStatus.setText("● RUNNING");

        btnStop.setOnClickListener(v -> {
            stopSelf();
        });

        final boolean[] paused = {false};
        btnPause.setOnClickListener(v -> {
            paused[0] = !paused[0];
            if (paused[0]) {
                AutoClickService.rules.clear();
                tvStatus.setText("⏸ PAUSED");
                btnPause.setText("▶");
            } else {
                tvStatus.setText("● RUNNING");
                btnPause.setText("⏸");
            }
        });

        wm.addView(overlayView, params);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        AutoClickService.rules.clear();
        if (overlayView != null) wm.removeView(overlayView);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
