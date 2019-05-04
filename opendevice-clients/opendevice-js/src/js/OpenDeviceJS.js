/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

var od = od || {};

od.SESSION_ID = "AuthToken"; // For cookie/localstore search
od.DEVICES_STORAGE_ID = "odev_devices"; //

od.version = "0.3.2";
od.appID = "*"; // ApyKey Value
od.serverURL = window.location.origin;

var OpenDevice = (function () {

    var connection = new od.DeviceConnection({logLevel : 'debug'});
    var manager = new od.DeviceManager(connection);

// Exported Methods / Vars
return {

    appID : od.appID,
    serverURL : od.serverURL,
    manager : manager,

    // Manager delegate
    on : manager.on,
    removeListener : manager.removeListener,
    notifyListeners : manager.notifyListeners,
    setListenerReceiver : manager.setListenerReceiver,
    onDeviceChange : manager.onDeviceChange,
    onChange : manager.onDeviceChange,
    onConnect : manager.onConnect,
    isConnected : manager.isConnected,
    findDevice : manager.findDevice,
    get : manager.findDevice,
    removeDevice : manager.removeDevice,
    deleteHitory : manager.deleteHitory,
    getDevices : manager.getDevices,
    getDevicesByType : manager.getDevicesByType,
    getDevicesByBoard : manager.getDevicesByBoard,
    getBoards : manager.getBoards,
    getTypes : manager.getTypes,
    setValue : manager.setValue,
    toggleValue : manager.toggleValue,
    contains : manager.contains,
    sync : manager.sync,
    save : manager.save,
    send : manager.send,

    setAppID : function(appID){
        od.appID = appID;
    },

    setApyKey : function(appID){
        od.appID = appID;
    },

    setServer : function(serverURL){
        od.serverURL = serverURL;
    },

    connect : function(_conn){
        if(_conn) connection = _conn;
        if(od.appID == "*"){
            od.appID = OpenDevice.findAppID()
            console.log("Using APP.ID:", od.appID);
        }
        connection.connect();
    },

    // TODO: try do Rest over WS
    rest : function(path, options){

        var request = {
            type: "GET",
            url: od.serverURL + path,
            headers : {
                'Authorization' : "ApiKey " + od.appID
            },
            async: false // FIXME: isso não é recomendado...
        };

        $.extend(request, options || {});

        var response = $.ajax(request);

        response.fail(function(){
            console.error("Rest fail, status ("+response.status+"): " + response.responseText );
            if(response.status == 401){
                manager.notifyListeners(od.Event.LOGIN_FAILURE, od.CommandStatus.UNAUTHORIZED);
            }
        });

        // TODO: fazer tratamento dos possíveis erros (como exceptions e servidor offline ou 404)

        // For async = false
        if(request.async == null || request.async == false){
            if(response.status == 200 && response.responseText.length > 0){
                return JSON.parse(response.responseText)
            }else{
                return null;
            }
        }else{
            return response;
        }

    },


    history : function(query, callback, errorCallback){
        jQuery.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Authorization' : "Bearer " + od.appID
            },
            type: 'POST',
            url: od.serverURL + OpenDevice.devices.path + "/" + query.deviceID + "/history",
            data: JSON.stringify(query),
            dataType: 'json',
            async: true,
            success: callback,
            error : errorCallback
        });
    },


    logout : function(callback){
        return $.get(od.serverURL +"/api/auth/logout", callback);
    },

    /** Try to find APPID(AuthToken) URL->Cookie->LocalStore */
    findAppID : function(){

        /** Get URL query param */
        function getQueryParam(name){
            var qs = (function(a) {
                if (a == "") return {};
                var b = {};
                for (var i = 0; i < a.length; ++i)
                {
                    var p=a[i].split('=', 2);
                    if (p.length == 1)
                        b[p[0]] = "";
                    else
                        b[p[0]] = decodeURIComponent(p[1].replace(/\+/g, " "));
                }
                return b;
            })(window.location.search.substr(1).split('&'));

            return qs[name];
        }

        /** Get URL query param */
        function getCookie(name) {
          var value = "; " + document.cookie;
          var parts = value.split("; " + name + "=");
          if (parts.length == 2) return parts.pop().split(";").shift();
        }


        var appID = getQueryParam(od.SESSION_ID);

        if(appID != null) return appID;

        appID = getCookie(od.SESSION_ID);

        if(appID != null) return appID;

        if( window.localStorage ){
            appID = window.localStorage.getItem(od.SESSION_ID)
            if(appID != null) return appID;
        }

        return "*"; // APPID for Local/Single user mode
    }


};

})();


var ODev = OpenDevice;

/**
 * REST Interface: Devices
 */

OpenDevice.devices = {

    path : "/api/devices",

    get : function(deviceID){
        return OpenDevice.rest(this.path + "/" + deviceID);
    },

    value : function(deviceID, value){

        if(value != null){
            return OpenDevice.rest(this.path + "/" + deviceID + "/value/" + value);
        }else{
            return OpenDevice.rest(this.path + "/" + deviceID + "/value");
        }
    },

    list : function(){
        return OpenDevice.rest(this.path + "/");
    },

    listTypes : function(callback, errorCallback){
        return OpenDevice.rest(this.path + "/types", { async : true, success : callback, error : errorCallback});
    },

    sync : function(){
        return OpenDevice.rest(this.path + "/sync");
    },

    delete : function(uid, callback, errorCallback){
        return OpenDevice.rest(this.path + "/" + uid, { async : true, type : "DELETE", success : callback, error : errorCallback});
    },

    deleteHistory : function(uid, callback, errorCallback){
        return OpenDevice.rest(this.path + "/" + uid + "/history", { async : true, type : "DELETE", success : callback, error : errorCallback});
    }

};
