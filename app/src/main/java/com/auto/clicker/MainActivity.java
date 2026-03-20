package com.auto.clicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    WebView wv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wv = findViewById(R.id.webview);
        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);

        wv.addJavascriptInterface(new Bridge(), "Android");
        wv.setWebViewClient(new WebViewClient());
        wv.loadUrl("file:///android_asset/index.html");

        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Overlay permission দাও — যেকোনো app এর উপরে control panel দেখাতে")
                .setPositiveButton("Grant", (d, w) -> {
                    Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                })
                .setCancelable(false)
                .show();
        }

        // Check accessibility
        if (!isAccessibilityEnabled()) {
            new AlertDialog.Builder(this)
                .setTitle("Accessibility Required")
                .setMessage("Accessibility Service enable কর — screen text read করতে")
                .setPositiveButton("Grant", (d, w) -> {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                })
                .setCancelable(false)
                .show();
        }
    }

    boolean isAccessibilityEnabled() {
        try {
            int enabled = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED);
            if (enabled == 1) {
                String services = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                if (services != null) {
                    return services.contains(getPackageName());
                }
            }
        } catch (Exception e) {}
        return false;
    }

    class Bridge {
        @JavascriptInterface
        public void startService(String rulesJson) {
            runOnUiThread(() -> {
                Intent i = new Intent(MainActivity.this, OverlayService.class);
                i.putExtra("rules", rulesJson);
                startService(i);
                Toast.makeText(MainActivity.this, "Service Started!", Toast.LENGTH_SHORT).show();
            });
        }

        @JavascriptInterface
        public void stopService() {
            runOnUiThread(() -> {
                stopService(new Intent(MainActivity.this, OverlayService.class));
                Toast.makeText(MainActivity.this, "Service Stopped!", Toast.LENGTH_SHORT).show();
            });
        }

        @JavascriptInterface
        public String generateName() {
            String[] first = {"Rahim","Karim","Jamal","Nasir","Faruk","Hasan","Alim","Rafi","Sami","Nabil","Omar","Yusuf","Adam","Ibrahim","Zaid"};
            String[] last = {"Ahmed","Islam","Khan","Hossain","Rahman","Uddin","Ali","Mia","Sheikh","Alam","Chowdhury","Sarkar","Das","Roy","Paul"};
            String f = first[(int)(Math.random()*first.length)];
            String l = last[(int)(Math.random()*last.length)];
            return f + " " + l;
        }

        @JavascriptInterface
        public String generateUsername() {
            String[] prefix = {"user","md","mr","bd","pro","dev","the","real","just","only"};
            String[] mid = {"rahim","karim","jamal","nasir","faruk","hasan","alim","rafi","sami","nabil"};
            String p = prefix[(int)(Math.random()*prefix.length)];
            String m = mid[(int)(Math.random()*mid.length)];
            int num = (int)(Math.random()*9999);
            return p + "_" + m + num;
        }

        @JavascriptInterface
        public boolean isServiceRunning() {
            return OverlayService.isRunning;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        wv.evaluateJavascript("updateStatus()", null);
    }
}
