package com.webtool.pro;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.CookieManager;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.widget.Toast;

public class MainActivity extends Activity {
    WebView wvUI, wv1, wv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wvUI = findViewById(R.id.webviewUI);
        wv1  = findViewById(R.id.webview1);
        wv2  = findViewById(R.id.webview2);

        setupWebView(wv1);
        setupWebView(wv2);
        setupWebView(wvUI);

        wvUI.addJavascriptInterface(new JSBridge(), "Android");

        wvUI.loadUrl("file:///android_asset/index.html");
    }

    void setupWebView(WebView wv) {
        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setAllowFileAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setUserAgentString("Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);
        wv.setWebViewClient(new WebViewClient());
        wv.setWebChromeClient(new WebChromeClient());
    }

    class JSBridge {
        @android.webkit.JavascriptInterface
        public void loadWV1(String url) { runOnUiThread(() -> wv1.loadUrl(url)); }

        @android.webkit.JavascriptInterface
        public void loadWV2(String url) { runOnUiThread(() -> wv2.loadUrl(url)); }

        @android.webkit.JavascriptInterface
        public String getUrlWV1() { try { return wv1.getUrl() != null ? wv1.getUrl() : ""; } catch(Exception e) { return ""; } }

        @android.webkit.JavascriptInterface
        public String getUrlWV2() { try { return wv2.getUrl() != null ? wv2.getUrl() : ""; } catch(Exception e) { return ""; } }

        @android.webkit.JavascriptInterface
        public void copyText(String text) {
            runOnUiThread(() -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("url", text));
                Toast.makeText(MainActivity.this, "Copied!", Toast.LENGTH_SHORT).show();
            });
        }

        @android.webkit.JavascriptInterface
        public void goBackWV1() { runOnUiThread(() -> { if(wv1.canGoBack()) wv1.goBack(); }); }

        @android.webkit.JavascriptInterface
        public void goBackWV2() { runOnUiThread(() -> { if(wv2.canGoBack()) wv2.goBack(); }); }

        @android.webkit.JavascriptInterface
        public void refreshWV1() { runOnUiThread(() -> wv1.reload()); }

        @android.webkit.JavascriptInterface
        public void refreshWV2() { runOnUiThread(() -> wv2.reload()); }
    }

    @Override
    public void onBackPressed() {
        if (wv2.canGoBack()) wv2.goBack();
        else if (wv1.canGoBack()) wv1.goBack();
        else super.onBackPressed();
    }
}
