package org.apache.cordova.inappwebview;

import org.apache.cordova.CordovaPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by admin on 2017/9/1.
 */

public class VisitedHistory {

    private List<String> historyUrls = new ArrayList<String>();
    private Set<String> ignoreUrls = new HashSet<String>();

    public VisitedHistory(CordovaPreferences preferences){
        //忽略文件记录
        String needIgnore = preferences.getString("VisitedHistoryIgnores","appError.html,hfcgxt.cn");
        for (String ig:needIgnore.split(",")){
            ignoreUrls.add(ig);
        }
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

    public void doIgnoreHistory(String url){
        ignoreUrls.add(url);
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
        String history = "";
        boolean isFind = true;
        //是否忽略
        while(isFind){
            history = removeLastHistory();
            isFind = false;
            for(String ig:ignoreUrls){
                if(history.contains(ig)){
                    isFind = true;
                    break;
                }
            }
        }
        return history;
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
        //是否忽略
        /*for(String ig:ignoreUrls){
            boolean match = url.contains(ig);
            if(match){
               return false;
            }
        }*/
        //是否刷新,刷新不保存
        if(historyUrls.get(historyUrls.size()-1).equals(url)){
            return false;
        }
        //登录页面不记录
        if(url.contains("login.html") && !historyUrls.get(historyUrls.size()-1).contains("index.html")){
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
