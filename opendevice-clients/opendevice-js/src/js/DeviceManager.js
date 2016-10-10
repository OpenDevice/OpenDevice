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

        var device = _this.findDevice(deviceID);

        if(device){
            device.setValue(value);
        }

        // TODO :Alterar dados locais (localstorage)

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

    this.notifyDeviceListeners = function(device, sync){

        if(sync){
            var cmd = { 'type' : device.type , 'deviceID' :  device.id, 'value' : device.value};
            _this.connection.send(cmd);
        }

        // Notify Individual Listeners
        for (var i = 0; i < device.listeners.length; i++) {
            device.listeners[i](device.value, device.id);
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
            var device = new od.Device(response[i]);
            if(typeof Object.observe != "undefined"){
                Object.observe(device, _onPropertyChange);
            }else console.warn("Object.observe not supported in this browser.");
            devices.push(device);
        }

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

        //od.CommandType.CONNECT_RESPONSE

        // Device changed in another client..
        if(CType.isDeviceCommand(message.type)){
            console.log("Device changed in another client..");

            var device = _this.findDevice(message.deviceID);

            if(device){
                device.setValue(message.value, false);
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
};