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

od.version = "0.3.2";
od.appID = "*";
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
    onDeviceChange : manager.onDeviceChange,
    onChange : manager.onDeviceChange,
    onConnect : manager.onConnect,
    findDevice : manager.findDevice,
    get : manager.findDevice,
    getDevices : manager.getDevices,
    setValue : manager.setValue,
    toggleValue : manager.toggleValue,
    contains : manager.contains,
    sync : manager.sync,
    send : manager.send,

    setAppID : function(appID){
        od.appID = appID;
    },

    setServer : function(serverURL){
        od.serverURL = serverURL;
    },

    connect : function(_conn){
        if(_conn) connection = _conn;
        connection.connect();
    },

    // TODO: try do Rest over WS
    rest : function(path){
        var response = $.ajax({
                type: "GET",
                url: od.serverURL + path,
                headers : {
                    'X-AppID' : od.appID
                },
                async: false // FIXME: isso não é recomendado...
        });

        // TODO: fazer tratamento dos possíveis erros (como exceptions e servidor offline ou 404)
        if(response.status == 200 && response.responseText.length > 0){
            return JSON.parse(response.responseText)
        }else{
            console.error("Rest fail, status ("+response.status+"): " + response.responseText );
            return null;
        }
    },


    history : function(query, callback){
        jQuery.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'X-AppID' : od.appID
            },
            type: 'POST',
            url: od.serverURL +"/device/" + query.deviceID + "/history",
            data: JSON.stringify(query),
            dataType: 'json',
            async: true,
            success: callback
        });
    },


    logout : function(callback){
        return $.get(od.serverURL +"/api/auth/logout", callback);
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


var ODev = OpenDevice;

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

    list : function(){
        return OpenDevice.rest(OpenDevice.devices.path + "/list");
    },

    sync : function(){
        return OpenDevice.rest(OpenDevice.devices.path + "/sync");
    }

};
