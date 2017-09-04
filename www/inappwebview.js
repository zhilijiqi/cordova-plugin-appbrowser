

var exec = require('cordova/exec');

var InAppWebView = {
      errorUrl:function(callback,errorCallback) {
        exec(callback, errorCallback, "InAppWebView", "errorUrl", []);
    	},
        load: function (eventname) {
            exec(null, null, "InAppWebView", "load", []);
        },
        goBack:function (i) {
           exec(null, null, "InAppWebView", "goBack", [i]);
        }
};

window.history.back=InAppWebView.goBack;
history.back=InAppWebView.goBack;
window.history.go=InAppWebView.goBack;
history.go=InAppWebView.goBack;


window.history.go=function(i){
}