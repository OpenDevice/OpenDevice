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
};