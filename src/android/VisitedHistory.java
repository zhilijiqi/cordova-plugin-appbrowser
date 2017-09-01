package org.apache.cordova.inappwebview;

import org.apache.cordova.CordovaPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/9/1.
 */

public class VisitedHistory {

    private List<String> historyUrls = new ArrayList<String>();

    private String notKeep = "login.html";

    public VisitedHistory(CordovaPreferences preferences){
        notKeep = preferences.getString("HistoryNotKeep","login.html");
    }
    /**
     * 更新浏览器记录
     * @param url
     * @param isReload
     */
    public void doUpdateVisitedHistory(String url,boolean isReload) {
        if(canKeep(url)){
            if(isReload && !historyUrls.isEmpty()){
                removeLastHistory();
            }
            historyUrls.add(url);
        }
    }

    /**
     * 上次浏览记录
     * @return
     */
    public String getLastVisitedHistory() {
        if(historyUrls.isEmpty()){
            return null;
        }
        removeLastHistory();
        if(historyUrls.isEmpty()){
            return null;
        }
        return removeLastHistory();
    }

    /**
     * 移除最新记录
     * @return
     */
    public String removeLastHistory(){
        if(historyUrls.isEmpty())
            return null;
        return historyUrls.remove(historyUrls.size()-1);
    }

    /**
     * 是否需要保存
     * @param url
     * @return
     */
    public boolean canKeep(String url){
        if(historyUrls.isEmpty()){
            return true;
        }
        //登录页面不记录
        if(url.contains(notKeep) && !historyUrls.get(historyUrls.size()-1).contains("index.html")){
            removeLastHistory();
            //return false;
        }

        if(url.contains("index.html")){
            historyUrls.clear();
        }
        /*if(url.contains("index.html") && historyUrls.get(historyUrls.size()-1).contains("index.html")){
            removeLastHistory();

            return true;
        }*/

        return true;

    }
}
