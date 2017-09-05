package org.apache.cordova.inappwebview;

import android.content.Context;

import org.apache.cordova.LOG;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2017/9/5.
 */

public class CacheLocalHtml {

    public final static String LOG_TAG = "CacheLocalHtml";

    private final static String START_PAGE = "index.html";
    private final static String WWW_FOLDER = "www";
    private static final String FILE_PREFIX = "file://";
    private static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";

    private Context context;

    private static Map<String,String> htmlMap = new HashMap<String,String>();
    private String dataDirPrefix = "null";



    public CacheLocalHtml(Context context){
        this.context = context;
    }

    public void doCheckUpdateCache(String url){
        if(url.startsWith(FILE_PREFIX) && !url.startsWith(dataDirPrefix)){
            if(!url.endsWith(START_PAGE)){
                LOG.d(LOG_TAG,"checkUpdateCache not update");
                return;
            }
            LOG.d(LOG_TAG,"checkUpdateCache before changed:" + dataDirPrefix);

            dataDirPrefix = url.substring(0, url.indexOf(START_PAGE));
            LOG.d(LOG_TAG,"checkUpdateCache after changed:" + dataDirPrefix);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String[] htmls = null;
                    if(dataDirPrefix.startsWith(LOCAL_ASSETS_FOLDER)){
                        htmls =  getAssets(WWW_FOLDER);
                    }else{
                        htmls = getDirFiles(dataDirPrefix);
                    }
                    htmlMap.clear();
                    for (String name : htmls) {
                        htmlMap.put(name, dataDirPrefix + name);
                    }
                }
            }).start();
        }
    }

    public String getByName(String name){
        return htmlMap.get(name);
    }
    /**
     *
     * @param dir
     * @return
     */
    public String[] getAssets(String dir){
        try {
            return context.getAssets().list(dir);
        }catch (IOException e){
            LOG.e(LOG_TAG,e.getMessage());
        }
        return null;
    }


    /**
     *
     * @param dir
     * @return
     */
    public String[] getDirFiles(String dir){
        String wwwPath = dataDirPrefix.replace("file://", "");
        if (wwwPath.endsWith("/")) {
            wwwPath = wwwPath.substring(0, wwwPath.length() - 1);
        }
        File wwwDir = new File(wwwPath);
        if (wwwDir.exists() && wwwDir.isDirectory()) {
            return wwwDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith(".html")) {
                        return true;
                    }
                    return false;
                }
            });
        }
        return null;
    }
}