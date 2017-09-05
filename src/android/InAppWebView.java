package org.apache.cordova.inappwebview;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class InAppWebView extends CordovaPlugin {

    private static final String LOG_TAG = "InAppWebView";

    private static String networkType = "";

    private String urlError = null;
    private static String failingUrl;

    private String jsFile = null;
    private String jsLoader = null;

    private String urlFlag = null;
    private String urlRewriteHost = null;

    private static VisitedHistory visitedHistory = null;
    private static CacheLocalHtml cacheLocalHtml = null;

    @Override
    protected void pluginInitialize() {
        super.initialize(cordova, webView);

        urlError = preferences.getString("UrlError", "appError.html");

        if(visitedHistory == null) {
            visitedHistory = new VisitedHistory(preferences);
        }
        if(cacheLocalHtml == null) {
            cacheLocalHtml = new CacheLocalHtml(cordova.getActivity());
        }
        jsFile = preferences.getString("LoadJsFile", "app.js");
        if(jsFile != null){
            StringBuffer buf = new StringBuffer();
            //jsWrapper = "(function(d) { var c = d.createElement('script'); c.src = %s; d.body.appendChild(c); })(document)";
            buf.append("var _app = _app || [];");
            buf.append("window._app = _app;");
            buf.append("(function(d){");
            buf.append("var app = document.createElement('script');");
            buf.append("app.type='text/javascript';");
            buf.append("app.async = true;");
            buf.append("app.src = '"+jsFile+"';");
            buf.append("var jtag = d.getElementsByTagName('script');");
            buf.append("if(jtag == undefined){d.body.appendChild(app);return;}");
            buf.append("var s = jtag[0];");
            buf.append("s.parentNode.insertBefore(app, s);");
            buf.append("})(document);");
            jsLoader = buf.toString();
        }
        urlFlag = preferences.getString("UrlFlag", "app=1");
        urlRewriteHost = preferences.getString("UrlRewriteHost", null);
    }
    @Override
    public Uri remapUri(Uri uri) {
        if(uri.toString().indexOf("app.js")>-1){
            LOG.d(LOG_TAG,"remapUri-"+uri);
        }
        return null;
    }

    @Override
    public Object onMessage(String id, Object data) {
        LOG.d(LOG_TAG,"id:"+id+",data:"+data);

        if("onPageStarted".equals(id)){
            visitedHistory.doUpdateVisitedHistory(data.toString(),false);
            cacheLocalHtml.doCheckUpdateCache(data.toString());

            return rewriteHttpUrlFlag(data.toString());
        }else if("onPageFinished".equals(id)){
            if(jsLoader != null && jsLoader.length() > 0) {
                injectDeferredObject(jsLoader, null);
            }
        }else if("networkconnection".equals(id)){
            if("none".equals(data.toString())) {
                networkType = data.toString();
                showToast("无法获取网络信息");
            }
        }else if("onReceivedError".equals(id)) {
            JSONObject d = (JSONObject)data;
            try {
                int code = d.getInt("errorCode");
                if(code ==-2 || code == -6){
                    if(code == -2){
                        showToast("无法获取网络信息");
                    }else if(code == -6){
                        showToast("网络连接超时");
                    }
                    this.onReceivedError(d.getInt("errorCode"), d.getString("description"), d.getString("url"));
                    return data;
                }
            } catch (JSONException e) {
                LOG.e(LOG_TAG,e.getMessage());
            }
        }else if("WebViewEngine".equals(id)){
            doWebViewEngine(data.toString());
        }
        return null;
    }


    /**
     * 重写URL,增加标示
     * @param url
     * @return
     */
    private Boolean rewriteHttpUrlFlag(String url) {
        if(urlFlag == null || !url.startsWith("http") || url.contains(urlFlag)){
            return null;
        }else{
            String host = Uri.parse(url).getHost();
            if(urlRewriteHost != null && !host.contains(urlRewriteHost)){
                return null;
            }
            if(url.indexOf("?")>-1){
                url = url + "&" + urlFlag;
            }else{
                url = url + "?" + urlFlag;
            }
            final String newUrl = url + "&t=" + System.currentTimeMillis();
            //移除最后一个记录
            visitedHistory.removeLastHistory();
            cordova.getActivity().runOnUiThread(new Runnable() {
                @SuppressLint("NewApi")
                @Override
                public void run() {
                    webView.loadUrl(newUrl);
                }
            });
        }
        return false;
    }

    @Override
    public Boolean shouldAllowNavigation(String url) {
        LOG.d(LOG_TAG,"shouldAllowNavigation-"+url);
        return null;
    }

    public Boolean shouldAllowRequest(String url) {
        LOG.d(LOG_TAG,"shouldAllowRequest-"+url);
        return null;
    }

    /**
     * 是否打开系统浏览器
     * @param url
     * @return
     */
    @Override
    public Boolean shouldOpenExternalUrl(String url) {
        LOG.d(LOG_TAG,"shouldOpenExternalUrl-"+url);
        return null;
    }

    /**
     * 外部页面跳转到app内部页面
     * @param url           The URL that is trying to be loaded in the Cordova webview.
     * @return
     */
    public boolean onOverrideUrlLoading(String url) {
        LOG.d(LOG_TAG,"onOverrideUrlLoading:"+url);
        if(url.contains(urlRewriteHost)){
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            if(TextUtils.isEmpty(path) || !path.endsWith(".html")){
                return false;
            }
            while(path.startsWith("/")){
                path = path.substring(1 , path.length());
            }

            String lf = cacheLocalHtml.getByName(path);
            if(!TextUtils.isEmpty(lf) &&
                    !TextUtils.isEmpty(uri.getQuery())){
                lf =  lf + "?" + uri.getQuery();
            }
            final String fileUrl = lf;
            if(!TextUtils.isEmpty(fileUrl)){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @SuppressLint("NewApi")
                    @Override
                    public void run() {
                        webView.loadUrlIntoView(fileUrl,false);
                    }
                });
                return true;
            }
        }

        return false;
    }

    private void injectDeferredObject(String source, String jsWrapper) {
        if (webView!=null) {
            String scriptToInject;
            if (jsWrapper != null) {
                org.json.JSONArray jsonEsc = new org.json.JSONArray();
                jsonEsc.put(source);
                String jsonRepr = jsonEsc.toString();
                String jsonSourceString = jsonRepr.substring(1, jsonRepr.length()-1);
                scriptToInject = String.format(jsWrapper, jsonSourceString);
            } else {
                scriptToInject = source;
            }
            final String finalScriptToInject = scriptToInject;
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @SuppressLint("NewApi")
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                        // This action will have the side-effect of blurring the currently focused element
                        webView.loadUrl("javascript:" + finalScriptToInject);
                    } else {
                        webView.getEngine().evaluateJavascript(finalScriptToInject, null);
                    }
                }
            });
        } else {
            LOG.d(LOG_TAG, "Can't inject code into the system browser");
        }
    }

    public void onReceivedError(final int errorCode, final String description, final String failingUrl) {
        final String errorFile = cacheLocalHtml.getByName(urlError);

        if ((errorFile != null) && (!failingUrl.equals(errorFile)) && (webView != null)) {
            this.failingUrl = failingUrl;
            // Load URL on UI thread
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    webView.loadUrlIntoView(errorFile,false);
                }
            });
        }
    }

    @Override
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        LOG.v(LOG_TAG, "Executing action: " + action);
        if ("errorUrl".equals(action)) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, failingUrl);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        }else if ("load".equals(action)) {
            String page = args.getString(0);
            boolean isInnerRes = args.getBoolean(1);
            if(isInnerRes){
                page = cacheLocalHtml.getByName(page);
            }
            final String loadUrl = page;
            if(loadUrl!=null && loadUrl.length()>0){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        webView.clearHistory();
                        webView.loadUrlIntoView(loadUrl,false);
                    }
                });
            }
            return true;
        }else if ("goBack".equals(action)) {
            doWebViewEngine(action);
            return true;
        }
        return false;
    }

    /**
     * 处理
     * @param action
     */
    public void doWebViewEngine(String action){
        if("goBack".equals(action)){
            final String url = visitedHistory.getLastVisitedHistory();
            if(!TextUtils.isEmpty(url)){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        webView.loadUrlIntoView(url,false);
                    }
                });
            }else{
                webView.getPluginManager().postMessage("exit",null);
            }
        }
    }
    /**
     * 显示提示信息
     * @param msg
     */
    private void showToast(String msg){
        Toast.makeText(cordova.getActivity().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 检查网络状态
     * @return
     */
    private boolean checkNetWorkInfo(){
        if("none".equals(networkType)){
            return false;
        }
        return true;
    }
}