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
    ANALOG:2,
    ANALOG_SIGNED:3,
    NUMERIC:4,
    FLOAT2:5,
    FLOAT2_SIGNED:6,
    FLOAT4:7,
    CHARACTER:8,
    BOARD:10,
    MANAGER:11,

    isNumeric : function(type){
        return type == od.DeviceType.ANALOG
        || type == od.DeviceType.FLOAT2
        || type == od.DeviceType.FLOAT4
        || type == od.DeviceType.FLOAT2_SIGNED
    }
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
    NUMERIC:3,
    GPIO_DIGITAL:4,
    GPIO_ANALOG:5,
    INFRA_RED:6,

    /** Response to commands like: DIGITAL, POWER_LEVEL, INFRA RED  */
    DEVICE_COMMAND_RESPONSE : 10, // Responsta para comandos como: DIGITAL, POWER_LEVEL, INFRA_RED
    COMMAND_RESPONSE : 11,
    SET_PROPERTY:12,
    ACTION:13,

    PING_REQUEST            :20,
    PING_RESPONSE           :21,
    DISCOVERY_REQUEST       :22,
    DISCOVERY_RESPONSE      :23,
    MEMORY_REPORT           :24,
    CPU_TEMPERATURE_REPORT  :25,
    CPU_USAGE_REPORT        :26,


    GET_DEVICES             :30,
    GET_DEVICES_RESPONSE    :31,
    DEVICE_SAVE             :32,
    DEVICE_SAVE_RESPONSE	:33,
    DEVICE_DEL              :34,
    CLEAR_DEVICES           :35,
    SYNC_DEVICES_ID 		:36,
    SYNC_HISTORY         	:37,
    FIRMWARE_UPDATE         :38,

    GET_CONNECTIONS         :40,
    GET_CONNECTIONS_RESPONSE:41,
    CONNECTION_ADD          :42,
    CONNECTION_ADD_RESPONSE :43,
    CONNECTION_DEL          :44,
    CLEAR_CONNECTIONS       :45,
    CONNECT 		        :46,
    CONNECT_RESPONSE 		:47,

    USER_EVENT              :98,
    USER_COMMAND            :99,

    isDeviceCommand : function(type){
        switch (type) {
            case this.DIGITAL:
                return true;
            case this.ANALOG:
                return true;
            case this.NUMERIC:
                return true;
            default:
                break;
        }
        return false;
    }
};


od.CommandStatus = {
    DELIVERED           :1,
    RECEIVED            :2,
    FAIL                :3,
    EMPTY_DATABASE      :4,
    // Response...
    SUCCESS             :200,
    NOT_FOUND           :404,
    BAD_REQUEST         :400,
    UNAUTHORIZED        :401,
    FORBIDDEN           :403,
    PERMISSION_DENIED   :550,
    INTERNAL_ERROR      :500,
    NOT_IMPLEMENTED     :501
};


od.Event = {
    DEVICE_LIST_UPDATE : "devicesUpdate",
    DEVICE_CHANGED : "deviceChanged",
    CONNECTION_CHANGE : "connectionCgange",
    CONNECTED : "connected",
    DISCONNECTED : "disconnected",
    LOGIN_FAILURE : "loginFail"
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

    // Private
    var CType = od.CommandType;
    var _this = this;

    // Public
    this.type = od.DeviceType.DIGITAL;
    this.listeners = [];

    function _init(data){

        this.id = data.id;
        this.manager = od.deviceManager;

        // Dynamic Properties and Funtions
        this.updateRawData(data);
    }

    /**
     * Update dynamic Properties and Funtions
     * @param data
     */
    this.updateRawData = function(data){

        for (var attrname in data) this[attrname] = data[attrname];

        for (var property in this.properties) this[property] = this.properties[property];

        if(!this.description) this.description = this.name;

        this.actions.forEach(function(method) {
            _this[method] = function(){
                console.log('Calling remote action: ' + method + ", params: ", arguments);
                var paramlist = [];
                for(var i in arguments) paramlist.push(arguments[i]);
                _this.manager.send({type : CType.ACTION, deviceID : _this.id, action : method, params : paramlist });
            }
        });
    };
    function notifyListeners(){

    }

    this.on = function(){
         this.setValue(1, true);
    };

    this.off = function(){
         this.setValue(0, true);
    };

    this.isON = function(){
        return (this.value == 1)
    };

    this.isOFF = function(){
        return (this.value == 0)
    };

    this.setValue = function(value, sync){

        sync = typeof sync !== 'undefined' ? sync : true; // default true


        // Only fire events if change... (or is Numeric (RFID/etc..))
        if(this.type == od.DeviceType.NUMERIC || this.value != value){

            this.value = value;
            this.lastUpdate = new Date().getTime();

            if(this.manager){
                this.manager.notifyDeviceListeners(this, sync);
            }

        }

    };

    this.toggle = function(){
        var value = 0;
        if(this.value == 0) value = 1;
        else if(this.value == 1) value = 0;
        this.setValue(value);
    };

    /** @deprecated */
    this.toggleValue = this.toggle;

    /**
     * Register a listener to monitor changes in this Device.
     * @param {function} listener
     * @param {Object} [context] - Context to execute listener
     * @returns {{context: *, listener: *}} - return registred listener (used in #removeListener)
     */
    this.onChange = function(listener, context){
        var eventDef = {"context":context, "listener" : listener};
        this.listeners.push(eventDef);
        return eventDef;
    };

    /**
     *
     * @param eventDef {{context: *, listener: *}}
     */
    this.removeListener = function(eventDef){
        var index = this.listeners.indexOf(eventDef);
        if(index >= 0){
            this.listeners.splice(index, 1);
        }
    };

    this.applyChanges = function(device){
        for (var attr in device) {
            if (device.hasOwnProperty(attr) &&  typeof device[attr] != "function" )
                this[attr] = device[attr];
        }
    };

    // Initialize device data.
    _init.call(this, data);
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

        _config.enableProtocol = false;

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
        if(_this.status != Status.CONNECTED){
            _this.config.url = _this.getUrl();
            console.log("Connection to: " + _this.config.url);
            serverConnection = socket.subscribe(_this.config);
            setConnectionStatus(Status.CONNECTING);
        }else{
            console.log("Already Connected");
        }
        return _this;
    };

    this.getUrl = function(){
        return od.serverURL + "/ws/device/" + od.appID;
    };

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
        return _this.status == Status.CONNECTED;
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

        _this.status = status;

        for(var i = 0; i<listeners.length; i++){
            var listener = listeners[i]["connectionStateChanged"];
            if (typeof listener === "function") {
                listener(_this, status, _this.status);
            }
        }

    }

    function _onMessageReceived(response){

        var data = null;

        // KeepAlive
        if(response.responseBody == "X") return;


        try {
            data = JSON.parse(response.responseBody);
        }catch(err) {
            console.error("Can't parse response: <<" + response.responseBody + ">>");
            console.error(err.stack);
            return;
        }

        // HACK: Atmosphere server not allow return statuscode > 400. The 'status' is in the message
        if(data.status && (data.status == 401 || data.status == 403)){
            console.warn("Authorization Required");
            notifyListeners({"type" : od.CommandType.CONNECT_RESPONSE,
                "status" : od.CommandStatus.UNAUTHORIZED});
            return;
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
    var initialized = false;

    od.deviceManager = this; // set global reference
    // Alias
    var DEvent = od.Event;
    var CType = od.CommandType;
    var DeviceType = od.DeviceType;

    // Private
    var types = [];
    var storage = new od.DeviceStorage();

    var listenersMap = {};
    var listenerReceiver = []; // current listerners (for single page model)

    // public
    this.connection = connection || od.connection;


    function init(){
        _this.connection.addListener({
            "onMessageReceived" : _onMessageReceived,
            "connectionStateChanged" : _connectionStateChanged
        });
    }

    this.setValue = function(deviceID, value){

        var device = _this.findDevice(deviceID);

        if(device){
            device.setValue(value);
        }

    };

    this.toggleValue = function(deviceID){

        var device = _this.findDevice(deviceID);

        if(device && ! device.sensor){
            device.toggle();
        }

    };

    this.send = function(cmd){
        _this.connection.send(cmd);
    };

    this.addDevice = function(){
        // Isso teria no final que salvar na EPROM/Servidor do arduino.
    };


    this.removeDevice = function(device){

        return ODev.devices.delete(device.id, function(){

            var devices = _this.getDevices();

            var index = devices.indexOf(device);
            if(index >= 0) devices.splice(index, 1);

            // TODO: if is a board need remove chids.
            notifyListeners(DEvent.DEVICE_LIST_UPDATE, devices);
        });

    };

    this.deleteHitory = function(device){
        return ODev.devices.deleteHistory(device.id);
    };

    this.getDevices = function(){

        var devices = storage.getDevices();

        if(devices && devices.length > 0){
            initialized = true;
            return devices; // return from cache...
        }

        devices = this.sync(/*notify=*/false); // load remote

        return devices;
    };

    this.getDevicesByType = function(type, devices){

        if(!devices) devices = this.getDevices();

        var found = [];

        if(devices) devices.forEach(function(device){
            if(device.type == type) found.push(device);
        });

        return found;
    };

    this.getDevicesByBoard = function(boardID){

        var devices = this.getDevices();

        var found = [];

        if(devices) devices.forEach(function(device){
            if(device.parentID == boardID && device.type != DeviceType.BOARD) found.push(device);
        });

        return found;
    };


    this.getBoards = function(){
        return this.getDevicesByType(DeviceType.BOARD);
    };

    this.getTypes = function(){

        if(types.length == 0){
            ODev.devices.listTypes(function(response){
                if(response.length > 0){
                    response.forEach(function(item){ types.push(item)})
                }
            });
        }

        return types;
    };

    /**
     * Find device by ID in List or in currently loaded devices
     * @param deviceID
     * @param deviceList (Optional) if not provide, current devices are considered
     * @returns {*}
     */
    this.findDevice = function(deviceID, deviceList){

        if(!deviceList) deviceList = this.getDevices();

        if(deviceList){
            for(var i = 0; i < deviceList.length; i++){
                if(deviceList[i].id == deviceID){
                    return deviceList[i];
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
     * @param {Boolean} forceSync - force sync with physical modules ( async )
     * @returns {Array}
     */
    this.sync = function(notify, forceSync){

        // load remote.
        var devices = _getDevicesRemote();

        // force sync (send GetDeviceRequest for all physical devices)
        if(forceSync || (devices && devices.length == 0)) {
            _this.send({type : CType.GET_DEVICES, forceSync : forceSync});
        }

        // Map devices and Parents
        if(devices){
            devices.forEach(function(item){
                if(item.parentID){
                    item.parent = _this.findDevice(item.parentID, devices);
                }
            });
        }

        if(notify === true) notifyListeners(DEvent.DEVICE_LIST_UPDATE, devices);

        return devices;
    };

    /**
     * Save or update device
     * @device
     * @param {function(status)} callback executed when device is saved.
     */
    this.save = function(device, callback){
        ODev.send({
            type : CType.DEVICE_SAVE, device : device
        });

        var found = _this.findDevice(device.id);

        // TODO: may be best wait the response before fire listeners
        if(found != null){
            found.applyChanges(device);
            _this.notifyDeviceListeners(found, /*sync=*/false);
            if(callback) callback.call(found, true);
        }

    };

    /**
     * Shortcut to {@link addListener}
     */
    this.on = function(event, listener){
        return _this.addListener(event, listener);
    };

    // FIXME: rename to onChange
    this.onDeviceChange = function (listener){
        _this.addListener(od.Event.DEVICE_CHANGED, listener);
    };

    this.onConnect = function (listener){
        if(_this.isConnected()){
            var devices = OpenDevice.getDevices();
            if(listener) listener(devices);
            return {event : od.Event.CONNECTED}; // fake listener
        }else{
            return this.on(od.Event.CONNECTED, function(){
                var devices = OpenDevice.getDevices();
                if(listener) listener(devices);
            });
        }
    };

    /**
     * Remove listener
     * @param {( string|Object[]|{event: *, listener: *})} eventDef  -  Event name (String) or Object
     * @param {function} [listener]
     */
    this.removeListener = function(eventDef, listener){

        var event;
        if(eventDef instanceof Array) {

            if(listener == listenerReceiver) listenerReceiver = null; // clear temporary listeners

            for (var i = 0; i < eventDef.length; i++) {
                var def = eventDef[i];
                this.removeListener(def);
            }
        } else if(typeof eventDef == "object") {
            event = eventDef["event"];
            listener = eventDef["listener"];
        }else{
            event =  eventDef;
        }

        if(listenersMap[event] != null){
            var listeners = listenersMap[event];
            var index = listeners.indexOf(listener);
            if(index >= 0) listeners.splice(index, 1);
        }
    };

    /**
     *
     * @param event
     * @param listener
     * @returns {{event: *, listener: *}} Listener definition (used in removeListener)
     */
    this.addListener = function(event, listener){

        if(listenersMap[event] === undefined) listenersMap[event] = [];
        listenersMap[event].push(listener);

        var eventlistener = { "event" : event, "listener" : listener };

        // See: setListenerReceiver
        if(listenerReceiver) listenerReceiver.push(eventlistener);

        return eventlistener;
    };

    /**
     * Defines a list where temporary listeners will be registered. Useful when you are working on a single page application
     * @param listeners
     * @returns {{event: Event, listener: *}}
     */
    this.setListenerReceiver = function(listeners){

        listenerReceiver = listeners;
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

    this.isConnected = function(){
        return _this.connection.isConnected();
    };

    this.notifyDeviceListeners = function(device, sync){

        if(sync){
            var cmd = { 'type' : device.type , 'deviceID' :  device.id, 'value' : device.value};
            _this.connection.send(cmd);
        }

        // Notify Individual Listeners
        for (var i = 0; i < device.listeners.length; i++) {

            if(typeof device.listeners[i] == "function"){
                device.listeners[i](device.value, device.id);
            }else{
                var listener = device.listeners[i]["listener"];
                listener.call(device.listeners[i]["context"], device.value, device.id);
            }

        }

        // Notify Global Listeners
        notifyListeners(DEvent.DEVICE_CHANGED, device);

    };

    this.notifyListeners = function(event, data){
        notifyListeners(event, data);
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

    /**
     *
     * @returns Array[]
     * @private
     */
    function _getDevicesRemote(){

        var response = OpenDevice.devices.list(); // rest !

        var devices = [];

        if(response.length > 0){
            storage.updateDevices(response);
        }

        for(var i = 0; i < response.length; i++ ){
            var device = new od.Device(response[i]);
            if(typeof Object.observe != "undefined"){
                Object.observe(device, _onPropertyChange);
            }else console.warn("Object.observe not supported in this browser.");
            devices.push(device);
        }

        initialized = true;

        return devices;
    }

    /**
     *
     * @param event
     * @private
     */
    function _onPropertyChange(event){

        if(event.length > 0 && event[0].type == "update" && event[0].name != "value"){

            var device = event[0].object;

            _this.send({type : CType.SET_PROPERTY, deviceID : device.id, property : event[0].name, value : device[event[0].name] });
        }

    }

    /**
     * @private
     */
    function _onMessageReceived(conn, message){

        // HACK: Bug in broadcast(atmosphere), is sending back same command.
        if(CType.isDeviceCommand(message.type) && conn.getConnectionUUID() == message.connectionUUID ){
            return;
        }

        //  Not Logged
        if (message.type == CType.CONNECT_RESPONSE && message.status == od.CommandStatus.UNAUTHORIZED){
            notifyListeners(DEvent.LOGIN_FAILURE, od.CommandStatus.UNAUTHORIZED);
            return;
        }

        //  Custom User Event
        if (message.type == CType.USER_EVENT){
            notifyListeners(message.name, message);
            return;
        }

        //od.CommandType.CONNECT_RESPONSE

        // Device changed in another client..
        if(CType.isDeviceCommand(message.type)){
            // console.log("Device changed in another client..");

            var device = _this.findDevice(message.deviceID);

            if(device){
                device.setValue(message.value, false);
            }
        }

        // Force load new list from server
        // TODO: It would be interesting if the devices list were already in response
        if(message.type == CType.GET_DEVICES_RESPONSE){
            // load remote.
            var devices = _getDevicesRemote();

            if(devices && devices.length > 0) notifyListeners(DEvent.DEVICE_LIST_UPDATE, devices);
        }

    }


    function _connectionStateChanged(conn, newStatus, oldStatus){
        console.log("DeviceManager._connectionStateChanged :" + newStatus);

        notifyListeners(DEvent.CONNECTION_CHANGE, newStatus);

        if(od.ConnectionStatus.CONNECTED == newStatus){
            notifyListeners(DEvent.CONNECTED, _this.getDevices());
        }

        if(od.ConnectionStatus.DISCONNECTED == newStatus){
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


/** @namespace */
var od = od || {};


/**
 * Handle device cache storage
 * @param {DeviceConnection} connection (Optional)
 * @constructor
 */
od.DeviceStorage = function(){

    var _this = this;
    //var initialized = false;

    // Private
    var devices = []; // od.Device

    this.getDevices = function(){

        // return from MEMORY cache...
        if(devices.length > 0) return devices;

        // return from storage
        // var data  = localStorage.getItem(od.DEVICES_STORAGE_ID);
        // if(data){
        //     devices = convertDevice(JSON.parse(data));
        //     return devices;
        // }

        return devices;
    };

    /**
     * Update devices
     * @param data - Row device data
     */
    this.updateDevices = function(response){

        localStorage.setItem(od.DEVICES_STORAGE_ID,  JSON.stringify(response));

        sync(response);
    };

    function sync(list){
        for (var i = 0; i < list.length; i++) {
            var obj = list[i];
            var device = find(obj.id);

            // New
            if(!device){
                devices.push(new od.Device(obj));
            }else{
                device.updateRawData(obj);
            }
        }
    }

    function find(deviceID){
        for (var i = 0; i < devices.length; i++) {
            var device = devices[i];
            if(device.id == deviceID){
                return device;
            }
        }
    }

    function convertDevice(response){
        var devices = [];
        for(var i = 0; i < response.length; i++ ){
            var device = new od.Device(response[i]);
            devices.push(device);
        }
        return devices;
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

    setServer : function(serverURL){
        od.serverURL = serverURL;
    },

    connect : function(_conn){
        if(_conn) connection = _conn;
        connection.connect();
    },

    // TODO: try do Rest over WS
    rest : function(path, options){

        var request = {
            type: "GET",
            url: od.serverURL + path,
            headers : {
                'Authorization' : "Bearer " + od.appID
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


        od.appID = getQueryParam(od.SESSION_ID);

        if(od.appID != null) return od.appID;

        od.appID = getCookie(od.SESSION_ID);

        if(od.appID != null) return od.appID;

        if( window.localStorage ){
            od.appID = window.localStorage.getItem(od.SESSION_ID)
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
