

var exec = require('cordova/exec');

var InAppWebView = {
      errorUrl:function(callback,errorCallback) {
        exec(callback, errorCallback, "InAppWebView", "errorUrl", []);
    	},
        load: function (eventname) {
            exec(null, null, "InAppWebView", "load", []);
        }
};

module.exports = InAppWebView;