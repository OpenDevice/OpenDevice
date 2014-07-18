/**
 * Created by ricardo on 06/07/14.
 */

'use strict';

var app = angular.module('opendevice.controllers', []);

/**
 * DashboardController
 * @connection - OpenDevice Connection, injected using 'ConnectionFactory.js'
 */
app.controller('DashboardController', ['$scope','manager', function ($scope, manager) {

    // Alias
    var DEvent = od.DeviceEvent;
    var DCategory = od.DeviceCategory;
    var DType = od.DeviceType;

    $scope.model = {
        status : '',
        devices: [ ]
    };

    init(); //

    function init(){

        manager.on(DEvent.DEVICE_LIST_UPDATE, function(data){
            $scope.model.devices = data;
            alert('data updated !!');
        });

        manager.on(DEvent.DEVICE_UPDATE, function(data){
           var device = findDevice(data.id);
            $scope.$apply(function() {
                device.name = data.name;
                device.value = data.value;
            });
        });

        $scope.model.devices = manager.getDevices();
    }

    $scope.send = function(data){

        //$scope.model.devices = manager.getDevices();
        $scope.$apply();

    };

    $scope.toggleValue = function(id){
        var device = findDevice(id);

        if(device.value == 0) device.value = 1;
        else if(device.value == 1) device.value = 0;

        manager.setValue(device.id, device.value);

        //$scope.$apply();
        //alert(device.name);
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


}]);
