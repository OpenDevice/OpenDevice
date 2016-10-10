/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

'use strict';

var pkg = angular.module('opendevice.controllers');

/**
 * DashboardController for AngularJS
 *
 *  Note: Access this controller from Chrome Debugger
 * angular.element(".content-wrapper").controller()
 * @author Ricardo JL Rufino
 * @date 06/07/14
 */
pkg.controller('DeviceController', ['$timeout', '$http', '$scope', 'DashboardRest', function ($timeout, $http, $scope, DashboardRest /*Service*/ ) {

    // Alias / Imports
    var DCategory = od.DeviceCategory;
    var DType = od.DeviceType;
    var DashItemView = od.view.DashItemView;

    // Private
    // ==========================
    var audioContext;
    var audioPlay;

    var _this = this;
    var _public = this;

    // Public
    // ==========================

    this.devices = [];

    _public.init = function(){

        $(function(){

            var Key = {
                LEFT: 37,  UP: 38,  RIGHT: 39, DOWN: 40, F2 : 113
            };

            //
            // $(document.body).on("keydown", function(event){
            //
            //     if($(event.target).is(":input")){ // avoif affect fields,selects
            //         return;
            //     }
            //
            //     // Change chart using Keys
            //     if(event.keyCode > 48 && event.keyCode < 58){
            //         var index = (event.keyCode - 48) - 1;
            //         _this.activateDash(_this.dashboardList[index]);
            //     }
            //
            //     if (event.keyCode == Key.UP && _this.itemViewSelected != null) {
            //         _this.updatePeriod(_this.itemViewSelected, true);
            //     }
            //     if (event.keyCode == Key.DOWN && _this.itemViewSelected != null) {
            //         _this.updatePeriod(_this.itemViewSelected, false);
            //     }
            //
            // });

        });

        _this.devices = ODev.getDevices();

        ODev.onChange(function(device){
            if(device) {
                if (device.type == od.DeviceType.DIGITAL) {
                    // playSound(device);
                }

                $timeout(function(){
                    $scope.$apply(); // sync view
                });
            }
        });


        ODev.on(od.Event.DEVICE_LIST_UPDATE, function(devices){
            _this.devices = devices;
        });

        // AudioContext detection
        try {
            // Fix up for prefixing
            window.AudioContext = window.AudioContext||window.webkitAudioContext;
            audioContext = new AudioContext();
        }catch(e) {
            alert('Web Audio API is not supported in this browser');
        }


    };

    // ============================================================================================
    // Public Functions
    // ============================================================================================


    _public.send = function(data){

        //_this.devices = getDevices();
        _this.$apply();

    };

    /**
     * Function called by the View when a button is clicked
     */
    _public.toggleValue = function(id){
        OpenDevice.toggleValue(id);
    };

    /**
     * Send value to all devices.
     * @param value
     */
    _public.sendToAll = function(value){

        var devices =  OpenDevice.getDevices();
        for(var i = 0; i < devices.length; i++){
            if(!devices[i].sensor)
                devices[i].setValue(value);
        }

    };

    /** Force sync devices from physical module */
    _public.syncDevices = function(){
        OpenDevice.sync(true, true);
    };

    /** Get Icon for device */
    _public.getIcon = function(id){
        var device = findDevice(id);
        var cname = "";

        if(device.category == DCategory.GENERIC && !(device.sensor)){
            cname += "ic-lightbulb-";
        }

        if(device.category == DCategory.LAMP){
            cname += "ic-lightbulb-";
        }

        if(device.category == DCategory.POWER_SOURCE){
            cname += "ic-battery-";
        }

        if(device.sensor){
            cname += "ic-sensor-";
        }

        if(device.value == 1){
            cname += "on";
        }else{
            cname += "off";
        }

        return cname;
    };

    _public.isControllableDevice = function(device){

        if(device.type == od.DeviceType.DIGITAL) return true;

        return false;
    };
    _public.isAnalogDevice = function(device){

        if(device.type == od.DeviceType.ANALOG) return true;

        return false;
    };


    // ============================================================================================
    // Private Functions
    // ============================================================================================


    /**
     * Find device by ID
     * @param {Number} id
     * @returns {*}
     */
    function findDevice(deviceID){
        var devices = _this.devices;
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
    }

    function playSound(device){

        if(audioContext && device.type == od.DeviceType.DIGITAL){
            audioPlay = audioContext.createOscillator();
            audioPlay.type = 3;
            if(device.value == 0){
                audioPlay.frequency.value = 700;
            }else{
                audioPlay.frequency.value = 800;
            }
            audioPlay.connect(audioContext.destination);

            var now = audioContext.currentTime;

            if(audioPlay && audioPlay.noteOn){
                audioPlay.noteOn( now );
                audioPlay.noteOff( now + 0.05 ); // "beep" (in seconds)
            }else{
                console.error("audioPlay not working !");
            }
        }
    }
}]);

