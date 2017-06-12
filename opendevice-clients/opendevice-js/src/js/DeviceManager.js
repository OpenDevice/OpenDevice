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
    var devices = [];
    var types = [];

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
            var index = devices.indexOf(device);
            if(index >= 0) devices.splice(index, 1);

            // TODO: if is a board need remove chids.
            notifyListeners(DEvent.DEVICE_LIST_UPDATE, devices);
        });

    };

    this.getDevices = function(){

        if(devices.length > 0) return devices; // return from cache...

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

        if(!deviceList) deviceList = devices;

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
        devices = _getDevicesRemote();

        // force sync (send GetDeviceRequest for all devices)
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
        return _this.connection.isConnected() && initialized;
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
};