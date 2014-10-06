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

    od.deviceManager = this; // set global reference

    // Alias
    var _this = this;
    var DEvent = od.DeviceEvent;
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

        var cmd = { 'type' : CType.ON_OFF , 'deviceID' :  deviceID, 'value' : value};
        this.connection.send(cmd);

        var device = this.findDevice(deviceID);

        if(device){
            device.value = value;
            notifyListeners(DEvent.DEVICE_UPDATE, device);
        }

        // TODO :Alterar dados locais (localstorage)

    };

    this.toggleValue = function(deviceID){

        var device = this.findDevice(deviceID);

        if(device && ! device.sensor){
            device.toggleValue();
        }

    };

    this.addDevice = function(){
        // Isso teria no final que salvar na EPROM/Servidor do arduino.
    }


    this.getDevices = function(){

        if(devices && devices.length > 0) return devices; // return from cache...

        // load remote.
        devices = sync(false);

        return devices;
    }

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
    };

    /**
     * Shortcut to {@link addListener}
     */
    this.on = function(){
        this.addListener.apply(this, arguments);
    }

    this.addListener = function(event, listener){

        if(listenersMap[event] === undefined) listenersMap[event] = [];
        listenersMap[event].push(listener);

    };


    function notifyListeners(event, data){

        if(! (listenersMap[event] === undefined)){ // has listeners for this event

            var listeners = listenersMap[event];

            for(var i = 0; i<listeners.length; i++){
                if (typeof listeners[i] === "function") {
                    listeners[i](data);
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
     *
     * @param message
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
                notifyListeners(DEvent.DEVICE_UPDATE, device);
            }
            // TODO: store changes localstore..
        }

    };

    function updateDevice(message){
        var device = _this.findDevice(message.deviceID);
        if(device){
            device.value = message.value;
        }
        return device;
    }

    function _connectionStateChanged(conn, newStatus, oldStatus){
        console.log("DeviceManager._connectionStateChanged :" + newStatus);

        if(od.ConnectionStatus.CONNECTED == newStatus){
            sync(true);
        }
    };

    init(); //
}