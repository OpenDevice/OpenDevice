/**
 * Created by ricardo on 06/07/14.
 */

'use strict';

var app = angular.module('opendevice.controllers', []);

/**
 * DashboardController
 * @connection - OpenDevice Connection, injected using 'ConnectionFactory.js'
 */
app.controller('DashboardController', ['$scope','$timeout', function ($scope, $timeout) {

    // Alias
    var DCategory = od.DeviceCategory;
    var DType = od.DeviceType;
    var manager = OpenDevice.manager;

    var audioContext;
    var audioPlay;

    $scope.model = {
        status : '',
        devices: [ ]
    };

    init(); //

    function init(){

        try {
            // Fix up for prefixing
            window.AudioContext = window.AudioContext||window.webkitAudioContext;
            audioContext = new AudioContext();
        }catch(e) {
            alert('Web Audio API is not supported in this browser');
        }

//        manager.on(DEvent.DEVICE_LIST_UPDATE, function(data){
//            $scope.model.devices = data;
//        });

        manager.on(od.Event.CONNECTED, function(data){

            var devices = manager.getDevices();
            var list = devices.filter(function(obj) {
                return obj.type == od.DeviceType.DIGITAL;
            });

            $scope.model.devices = list;
            $scope.$apply();
        });

        manager.on(od.Event.DEVICE_CHANGED, function(data){
            var device = findDevice(data.id);
            if(device){
                console.log("Controller.DEVICE_CHANGED");
                playSound(device);

                $timeout(function(){
                    $scope.$apply(); // sync view
                });
            }
        });

        OpenDevice.connect();

    }

    $scope.send = function(data){

        //$scope.model.devices = manager.getDevices();
        $scope.$apply();

    };

    /**
     * Function called by the View when a button is clicked
     */
    $scope.toggleValue = function(id){
        manager.toggleValue(id);
    };

    /**
     * Send value to all devices.
     * @param value
     */
    $scope.sendToAll = function(value){

        var devices = manager.getDevices();
        for(var i = 0; i < devices.length; i++){
            if(!devices[i].sensor)
                devices[i].setValue(value);
        }

    };

    /** Get Icon for device */
    $scope.getIcon = function(id){
        var device = findDevice(id);
        var cname = "";

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
    }

    /**
     * Find device by ID
     * @param {Number} id
     * @returns {*}
     */
    function findDevice(deviceID){
        var devices = $scope.model.devices;
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
            audioPlay.noteOn( now );
            audioPlay.noteOff( now + 0.05 ); // "beep" (in seconds)
        }
    }

}]);
