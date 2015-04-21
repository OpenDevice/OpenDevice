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

od.APP_ID_NAME = "AppID";

od.version = "1.0";
od.appID = "*";
od.serverURL = 'http://'+window.location.host;

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
    onDeviceChange : manager.onDeviceChange,
    findDevice : manager.findDevice,
    getDevices : manager.getDevices,
    setValue : manager.setValue,
    toggleValue : manager.toggleValue,
    contains : manager.contains,

    setAppID : function(appID){
        od.appID = appID;
    },

    setServer : function(serverURL){
        od.serverURL = serverURL;
    },

    connect : function(callback){

        OpenDevice.on(od.Event.CONNECTED, function(){
            var devices = OpenDevice.getDevices();
            if(callback) callback(devices);
        });

        connection.connect();
    },

    rest : function(path){
        var response = $.ajax({
                type: "GET",
                url: od.serverURL + path,
                headers : {
                    'X-AppID' : od.appID
                },
                async: false
        }).responseText;

        // TODO: fazer tratamento dos possíveis erros (como exceptions e servidor offline)

        return JSON.parse(response);
    },

    /** Try to find APPID URL->Cookie->LocalStore */
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


        od.appID = getQueryParam(od.APP_ID_NAME);

        if(od.appID != null) return od.appID;

        od.appID = getCookie(od.APP_ID_NAME);

        if(od.appID != null) return od.appID;

        if( window.localStorage ){
            od.appID = window.localStorage.getItem(od.APP_ID_NAME)
        }

        return od.appID;
    }


};

})();


/**
 * REST Interface: Devices
 */

OpenDevice.devices = {

    path : "/device",

    get : function(deviceID){
        return OpenDevice.rest(OpenDevice.devices.path + "/" + deviceID);
    },

    value : function(deviceID, value){

        if(value != null){
            return OpenDevice.rest(OpenDevice.devices.path + "/" + deviceID + "/value/" + value);
        }else{
            return OpenDevice.rest(OpenDevice.devices.path + "/" + deviceID + "/value");
        }
    },

    list : function(deviceID){
        return OpenDevice.rest(OpenDevice.devices.path + "/list");
    }

};
