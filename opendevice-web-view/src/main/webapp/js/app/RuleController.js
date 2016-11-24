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
 * Controller for Rules
 *
 * @author Ricardo JL Rufino
 * @date 01/11/16
 */
pkg.controller('RuleController', function ($scope, $location, $timeout, $routeParams, RuleRest) {

    // Alias / Imports
    var DType = od.DeviceType;

    // Private
    // ==========================
    var _this = this;
    var _public = this;

    var odevListeners = []; // required because of our simple-page-model

    // Public
    // ==========================

    this.list = [];

    this.devices = [];

    this.deviceTypes = [];

    this.deviceStates = [];


    $scope.options = {
        conditionTypes : [
            { code : "none", description : "None"},
            { code : "time", description : "Time"},
            { code : "activeTime", description : "Active Time"}
        ]
        ,evalConditionTypes : [
            { code : "EQUALS", description : " == "},
            { code : "DIFERNET", description : " != "}
        ]
        ,timeTypes : [
            { code : "SECOND", description : "Sec"},
            { code : "MINUTE", description : "Min"},
            { code : "HOUR", description : "Hours"}
        ]
    };


    $scope.model = new RuleRest(); // curren editing

    _public.init = function(){

        if($routeParams.id == "new" || $routeParams.id != null){

            _this.deviceTypes = ODev.getTypes();

            _this.deviceTypes.push({code:'all', description : "-- Show All--"});

            if($routeParams.id){
                $scope.model = RuleRest.get({id : $routeParams.id}, function(item){
                    var device = ODev.findDevice(item.resourceID);
                    if(device){
                        $scope.model.deviceType = device.type;
                        _public.filterDevices();
                    }
                })
            }

        } else { //  List Page

            _this.list = RuleRest.query();

            // Defines a list where temporary listeners will be registered
            ODev.setListenerReceiver(odevListeners);

            // Fired by Sync or by Server
            ODev.on("rules_update", function(message){
                $timeout(function(){
                    _this.list = RuleRest.query();
                }, 1000);
            });

        }

        // Destroy Controller Event
        $scope.$on("$destroy", function() {
            ODev.removeListener(odevListeners);
        });


    };

    // ============================================================================================
    // Public Functions
    // ============================================================================================


    _public.save = function(){

        if($scope.model.condition.type == 'none') $scope.model.condition = null;

        $scope.model.$saveOrUpdate(function (response) {
            $.notify({message: "Saved"}, {type:"success"});
            $location.path('/rules');
        });

    };

    _public.delete = function(item, index){
        item.$delete(function(){
            _this.list.splice(index, 1);
        });
    };

    _public.activate = function(item){

        item.$activate(); // send updated value

    };

    _public.filterDevices = function(){

        var type = $scope.model.deviceType;

        if(type == 'all')  _this.devices = ODev.getDevices();
        else  _this.devices = ODev.getDevicesByType(type);

        if(type == DType.BOARD){
            _this.deviceStates = [
                { code : 1, description : "Up"},
                { code : 0, description : "Down"}
            ];
        }else{
            _this.deviceStates = [
                { code : 1, description : "ON"},
                { code : 0, description : "OFF"}
            ];
        }

    };


    // ============================================================================================
    // Private Functionsre
    // ============================================================================================



});

pkg.filter('conditionType', function() {
    return function(obj) {

        if(obj == null) return "None";

        if(obj.type == "activeTime") return "Ative for " + obj.time + " " + obj.intervalType +"s";

        obj = obj || '';

        return obj;
    };
});

pkg.filter('actionType', function() {
    return function(obj) {

        // Obj: {"type":"control","id":146552,"resourceID":213,"value":1}
        if(obj.type == "control"){
            var device = ODev.findDevice(obj.resourceID) || { name : "[Not Found Error]"};
            return "Control " + device.name;
        }

        obj = obj || '';

        return obj;
    };
});

