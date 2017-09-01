package org.apache.cordova.inappwebview.engine;

import android.content.Context;

import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.LOG;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;

/**
 * Created by admin on 2017/9/1.
 */

public class SystemWebViewEngineEnhanced extends SystemWebViewEngine implements CordovaWebViewEngine {

    private final String LOG_TAG = "SystemWebViewEngineEnhanced";

    public SystemWebViewEngineEnhanced(Context context, CordovaPreferences preferences) {
        this(new SystemWebView(context), preferences);
    }

    public SystemWebViewEngineEnhanced(SystemWebView webView) {
        this(webView, null);
    }

    public SystemWebViewEngineEnhanced(SystemWebView webView, CordovaPreferences preferences) {
        super(webView,preferences);
    }

    @Override
    public boolean goBack() {
        pluginManager.postMessage("WebViewEngine","goBack");
        //boolean result = super.goBack();
        //LOG.i(LOG_TAG,"super.goBack() == " + result);
        return true;
    }
}
