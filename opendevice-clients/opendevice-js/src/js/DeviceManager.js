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

    this.addDevice = function(){
        // Isso teria no final que salvar na EPROM/Servidor do arduino.
    };


    this.getDevices = function(){

        if(devices && devices.length > 0) return devices; // return from cache...

        // load remote.
        devices = sync(false);

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
     * @returns {Array}
     */
     function sync(notify){

        // try local storage
        devices =  _getDevicesLocalStorege();
        if(devices && devices.length > 0) return devices;

        // load remote.
        devices = _getDevicesRemote();

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

    this.onDeviceChange = function (listener){
        _this.addListener(od.Event.DEVICE_CHANGED, listener);
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
            sync(true);
            notifyListeners(DEvent.CONNECTED);
        }

        if(od.ConnectionStatus.CONNECTED == newStatus){
            notifyListeners(DEvent.DISCONNECTED);
        }


    }

    init(); //
};