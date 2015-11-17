/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

var od = od || {};

// Like OpenDevice JAVA-API
od.DeviceType = {
    DIGITAL:1,
    ANALOG:2
};

// Like OpenDevice JAVA-API
od.DeviceCategory = {
    LAMP:1,
    FAN:2,
    GENERIC:3,
    POWER_SOURCE : 4,
    GENERIC_SENSOR: 50,
    IR_SENSOR: 51
};

od.CommandType = {
    DIGITAL:1,
    ANALOG:2,
    ANALOG_REPORT:3,
    GPIO_DIGITAL:4,
    GPIO_ANALOG:5,
    PWM:6,
    INFRA_RED:7,

    /** Response to commands like: DIGITAL, POWER_LEVEL, INFRA RED  */
    DEVICE_COMMAND_RESPONSE : 10, // Responsta para comandos como: DIGITAL, POWER_LEVEL, INFRA_RED

    PING : 20,
    PING_RESPONSE : 21,
    MEMORY_REPORT : 22, // Report the amount of memory (displays the current and maximum).
    CPU_TEMPERATURE_REPORT : 23,
    CPU_USAGE_REPORT:24,
    GET_DEVICES : 30,
    GET_DEVICES_RESPONSE : 31,
    USER_COMMAND : 99,

    isDeviceCommand : function(type){
        switch (type) {
            case this.DIGITAL:
                return true;
            case this.ANALOG:
                return true;
            case this.ANALOG_REPORT:
                return true;
            default:
                break;
        }
        return false;
    }
};


od.Event = {
    DEVICE_LIST_UPDATE : "DEVICE_LIST_UPDATE",
    DEVICE_CHANGED : "DEVICE_CHANGED",
    CONNECTION_CHANGE : "CONNECTION_CHANGE",
    CONNECTED : "CONNECTION_CHANGE_CONNECTED",
    DISCONNECTED : "CONNECTION_CHANGE_DISCONNECTED"
};/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

/** @namespace */
var od = od || {};


/**
 * Represent a Device
 * @param data - JSON
 * @constructor
 */
od.Device = function(data){
    this.id = data.id;
    this.name = data.name;
    this.type = data.type;
    this.category = data.category;
    this.value = data.value;
    this.sensor = data.sensor;
    this.manager = od.deviceManager;

    this.on = function(){
         this.setValue(1);
    };

    this.off = function(){
         this.setValue(0);
    };

    this.isON = function(){
        return (value == 1)
    };

    this.isOFF = function(){
        return (value == 0)
    };

    this.setValue = function(value){
        this.value = value;

        if(this.manager){
            this.manager.setValue(this.id, this.value);
        }
    };

    this.toggle = function(){
        var value = 0;
        if(this.value == 0) value = 1;
        else if(this.value == 1) value = 0;
        this.setValue(value);
    }

    /** @deprecated */
    this.toggleValue = this.toggle;

};/*
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



/** @namespace */
var od = od || {};

/** global instance. @type {{od.DeviceConnection}} */
od.connection = {};

od.ConnectionStatus = {
    CONNECTING: 1,
    CONNECTED : 2,
    DISCONNECTING :3,
    DISCONNECTED : 4,
    // LOGGINGIN : 5,
    FAIL : 6
};

/**
 * Represent a connection with server.
 * // TODO: adicionar documentação...
 * @param config
 * @constructor
 */
od.DeviceConnection = function(config){
    var _this = this;

    // Alias
    var Status = od.ConnectionStatus;
    // Private
    var socket = window.atmosphere || $.atmosphere;
    var serverConnection;
    var listeners = [];

    od.connection = this; // set global instance

    // public
    this.status = Status.DISCONNECTED;
    this.config = config;

    init(config);

    function init(_config){
        // _config.dropHeaders = false;

        if(_config["contentType"] == undefined)       _config["contentType"] = "application/json";
        if(_config["transport"] == undefined)         _config["transport"]   = "websocket";
        if(_config["fallbackTransport"] == undefined) _config["fallbackTransport"] = "long-polling";
        if(_config["reconnectInterval"] == undefined) _config["reconnectInterval"] = 5000;
        if(_config["maxReconnectOnClose"] == undefined) _config["maxReconnectOnClose"] = 5;


        _config.onError = function (response) {
            console.log("Connection.onError");
            setConnectionStatus(Status.FAIL);
        };

        _config.onMessage = function (response) {
            _onMessageReceived(response);
        };

        _config.onMessagePublished = function (response) {
            console.log("Connection.onMessagePublished");
        };

        // -----------------

        _config.onClose = function (response) {
            console.log("Connection.onClose");
            setConnectionStatus(Status.DISCONNECTED);
        };

        _config.onOpen = function (response) {
            console.log("Connection.onOpen");
            setConnectionStatus(Status.CONNECTED);
        };

        _config.onReopen = function (response) {
            console.log("Connection.onReopen");
            setConnectionStatus(Status.CONNECTED);
        };

        _config.onReconnect = function (response) {
            console.log("Connection.onReconnect");
            setConnectionStatus(Status.CONNECTING);
        };

        _config.onTransportFailure = function (response) {
            console.log("Connection.onTransportFailure");
            setConnectionStatus(Status.FAIL);
        };

        _config.onFailureToReconnect = function (response) {
            console.log("Connection.onFailureToReconnect");
            setConnectionStatus(Status.DISCONNECTED);
        };

        _config.onClientTimeout = function (response) {
            console.log("Connection.onClientTimeout");
            setConnectionStatus(Status.FAIL);
        };


    }

    this.connect = function(){
        _this.config.url = _this.getUrl();
        console.log("Connection to: " + _this.config.url);
        serverConnection = socket.subscribe(_this.config);
        setConnectionStatus(Status.CONNECTING);
        return _this;
    };

    this.getUrl = function(){
        return od.serverURL + "/device/connection/" + od.appID;
    }

    this.send = function(data){
        // FIX: bug no atmophere que não enviar os headers da primeira conexao // TODO: registrar ticket
        // Somente na re-conexao ele passa a enviar...
        // NOTA: Isso já foi RESOLVIDO! injetando o "@Context AtmosphereResource", mas de qualquer maneira continua
        // existindo esse problema no atmophere
        // data["connectionUUID"] = serverConnection.getUUID();

        serverConnection.push(JSON.stringify(data));

    };

    this.addListener = function(listener){
        listeners.push(listener);
    };

    this.isConnected = function(){
        return _this.status = Status.CONNECTED;
    };

    this.getConnectionUUID = function(){
        return serverConnection.getUUID();
    };

    function notifyListeners(data){
        for(var i = 0; i<listeners.length; i++){
            var listener = listeners[i]["onMessageReceived"];
            if (typeof listener === "function") {
                listener(_this, data);
            }
        }
    }

    function setConnectionStatus(status){

        for(var i = 0; i<listeners.length; i++){
            var listener = listeners[i]["connectionStateChanged"];
            if (typeof listener === "function") {
                listener(_this, status, _this.status);
            }
        }

        _this.status = status;
    }

    function _onMessageReceived(response){

        var data = null;
        try {
            data = JSON.parse(response.responseBody);
        }catch(err) {
            console.warn("Can't parse response: " + response.responseBody, err.stack);

        }

        if(data) {
            console.log("Connection.onMessageReceived(from:" + response.request.uuid + ") -> " + response.responseBody);
            notifyListeners(data);
        }

    }

};/*
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


/** @namespace */
var od = od || {};

/** global instance. @type {{od.DeviceManager}} */
od.deviceManager = {};


/**
 * DeviceManager
 * @param {DeviceConnection} connection (Optional)
 * @constructor
 */
od.DeviceManager = function(connection){
    var _this = this;
    od.deviceManager = this; // set global reference

    // Alias
    var DEvent = od.Event;
    var CType = od.CommandType;

    // Private
    var devices = [];
    var listenersMap = {};

    // public
    this.connection = connection || od.connection;


    function init(){
        _this.connection.addListener({
            "onMessageReceived" : _onMessageReceived,
            "connectionStateChanged" : _connectionStateChanged
        });
    }

    this.setValue = function(deviceID, value){

        var cmd = { 'type' : CType.DIGITAL , 'deviceID' :  deviceID, 'value' : value};
        _this.connection.send(cmd);

        var device = _this.findDevice(deviceID);

        if(device){
            device.value = value;
            notifyListeners(DEvent.DEVICE_CHANGED, device);
        }

        // TODO :Alterar dados locais (localstorage)

    };

    this.toggleValue = function(deviceID){

        var device = _this.findDevice(deviceID);

        if(device && ! device.sensor){
            device.toggleValue();
        }

    };

    this.send = function(cmd){
        _this.connection.send(cmd);
    }

    this.addDevice = function(){
        // Isso teria no final que salvar na EPROM/Servidor do arduino.
    };


    this.getDevices = function(){

        if(devices && devices.length > 0) return devices; // return from cache...

        // load remote.
        devices = this.sync(false);

        return devices;
    };

    this.findDevice = function(deviceID){
        if(devices){
            for(var i = 0; i < devices.length; i++){
                if(devices[i].id == deviceID){
                    return devices[i];
                }
            }
        } else{
            console.warn("Devices not loaded or empty !");
        }

        return null;
    };

    /**
     * Sync Devices with server
     * @param {Boolean} notify - if true notify listeners
     * @param {Boolean} forceSync - force sync with physical module
     * @returns {Array}
     */
    this.sync = function(notify, forceSync){


        // try local storage
        devices =  _getDevicesLocalStorege();
        if(devices && devices.length > 0) return devices;

        // load remote.
        devices = _getDevicesRemote();

        // fire sync (GetDeviceRequest) on server
        if(forceSync || (devices && devices.length == 0)) {
            // OpenDevice.devices.sync();
            _this.send({type : CType.GET_DEVICES, forceSync : forceSync});
        }

        if(notify === true) notifyListeners(DEvent.DEVICE_LIST_UPDATE, devices);

        // TODO: salvar no localstore..

        return devices;
    }

    /**
     * Shortcut to {@link addListener}
     */
    this.on = function(event, listener){
        _this.addListener(event, listener);
    };

    // FIXME: rename to onChange
    this.onDeviceChange = function (listener){
        _this.addListener(od.Event.DEVICE_CHANGED, listener);
    };

    this.onConnect = function (listener){
        this.on(od.Event.CONNECTED, function(){
            var devices = OpenDevice.getDevices();
            if(listener) listener(devices);
        });
    };

    this.addListener = function(event, listener){

        if(listenersMap[event] === undefined) listenersMap[event] = [];
        listenersMap[event].push(listener);

    };

    /**
     * Check if device is in the list passed by parameter or in internal list
     * @param device
     * @param list (Optional)
     * @returns {boolean}
     */
    this.contains = function(device, list){
        if(list == null) list = _this.getDevices();

        for(var i = 0; i<list.length; i++){

            if(typeof list[i] == "object"){
                if(device.id == list[i].id){
                    return true;
                }
            }else{
                if(device.id == list[i]){
                    return true;
                }
            }
        }

        return false;
    };


    function notifyListeners(event, data){

        if(! (listenersMap[event] === undefined)){ // has listeners for this event

            var listeners = listenersMap[event];

            for(var i = 0; i<listeners.length; i++){
                if (typeof listeners[i] === "function") {
                    try{
                        listeners[i](data);
                    }catch (error){
                        console.log(error);
                    }

                }
            }

        }
    }

    function _getDevicesLocalStorege(){
        return null;
    }

    /**
     *
     * @returns Array[]
     * @private
     */
    function _getDevicesRemote(){

        var response = OpenDevice.devices.list(); // rest !

        var devices = [];

        for(var i = 0; i < response.length; i++ ){
            devices.push(new od.Device(response[i]));
        }

        return devices;
    }

    /**
     * @private
     */
    function _onMessageReceived(conn, message){

        // HACK: Bug in broadcast(atmosphere), is sending back same command.
        if(CType.isDeviceCommand(message.type) && conn.getConnectionUUID() == message.connectionUUID ){
            return;
        }

        // Device changed in another client..
        if(CType.isDeviceCommand(message.type)){
            console.log("Device changed in another client..");

            var device = updateDevice(message);
            if(device){
                notifyListeners(DEvent.DEVICE_CHANGED, device);
            }
            // TODO: store changes localstore..
        }

        // Force load new list from server
        // TODO: It would be interesting if the devices list were already in response
        if(message.type == CType.GET_DEVICES_RESPONSE){
            // load remote.
            var devices = _getDevicesRemote();

            notifyListeners(DEvent.DEVICE_LIST_UPDATE, devices);
        }

    }

    function updateDevice(message){
        var device = _this.findDevice(message.deviceID);
        if(device){
            device.value = message.value;
        }
        return device;
    }

    function _connectionStateChanged(conn, newStatus, oldStatus){
        console.log("DeviceManager._connectionStateChanged :" + newStatus);
        notifyListeners(DEvent.CONNECTION_CHANGE, newStatus);

        if(od.ConnectionStatus.CONNECTED == newStatus){
            notifyListeners(DEvent.CONNECTED, _this.getDevices());
        }

        if(od.ConnectionStatus.CONNECTED == newStatus){
            notifyListeners(DEvent.DISCONNECTED);
        }


    }

    init(); //
};/*
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
    onChange : manager.onDeviceChange,
    onConnect : manager.onConnect,
    findDevice : manager.findDevice,
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

    rest : function(path){
        var response = $.ajax({
                type: "GET",
                url: od.serverURL + path,
                headers : {
                    'X-AppID' : od.appID
                },
                async: false // FIXME: isso não é recomendado...
        }).responseText;

        // TODO: fazer tratamento dos possíveis erros (como exceptions e servidor offline ou 404)

        if(response.length > 0){
            return JSON.parse(response)
        }else{
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
