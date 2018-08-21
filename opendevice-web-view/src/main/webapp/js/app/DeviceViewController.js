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
 * Controller for Boards and Devices
 *
 * Note: Access this controller from Chrome Debugger
 * angular.element(".content-wrapper").controller()
 * @author Ricardo JL Rufino
 * @date 06/10/19
 */
pkg.controller('DeviceViewController', function ($scope, $routeParams, $timeout, $http, ConnectionRest, DashboardRest) {

    // Alias / Imports
    var DCategory = od.DeviceCategory;
    var DType = od.DeviceType;
    var CType = od.CommandType;

    // Private
    // ==========================
    var _this = this;
    var _public = this;
    var odevListeners = []; // required because of our simple-page-model

    // Public
    // ==========================
    this.device;
    this.board; // used in URL
    this.exportUrl;

    this.pageNumber = 1;
    this.historyOptions = {
        periodType : 'RECORDS',
        periodValue : 100
    };

    this.history = [];
    this.deviceIcons = [];
    this.historyCharts = []; // od.view.ChartItemView
    this.chartViewOptions = {
        "periodValue": 1,
        "periodType": "HOUR"
    };

    _public.init = function(){

        // Wait for devices full loaded
        if(!ODev.isConnected()){
            odevListeners.push(ODev.onConnect(function(){
                _this.init();
            }));
            // return; (allow off-line)
        }

        this.device = ODev.get($routeParams.deviceID);
        this.board = ODev.get(this.device.parentID);
        this.exportUrl = "/api/devices/"+this.device.id+"/export";

        _public.editDevice(this.device);

        DashboardRest.deviceIcons(function(data){
            data.forEach(function(item){
                _this.deviceIcons.push({id:item, name: item });
            });
        });


        loadHistory.call(this);

        // Destroy Controller Event
        $scope.$on("$destroy", function() {

            for (var i = 0; i < _this.historyCharts.length; i++) {
                var view = _this.historyCharts[i];
                view.destroy();
            }
            for (var i = 0; i < _this.devicesCtrls.length; i++) {
                var view = _this.devicesCtrls[i];
                view.destroy();
            }

            // Unregister listeners on change page.
            ODev.removeListener(odevListeners);
        });

        // // Watch for changes in Chart view options
        $scope.$watch('ctrl.historyOptions', function() {
            loadHistory.call(_this);
        }, true);


        // Defines a list where temporary listeners will be registered
        ODev.setListenerReceiver(odevListeners);



    };

    // ============================================================================================
    // Public Functions
    // ============================================================================================

    _public.editDevice = function(device){

        _this.formDevice = angular.copy(device);

        // Remove invalid attributes.
        delete _this.formDevice.listeners;
        delete _this.formDevice.manager;
        delete _this.formDevice.devices;
        delete _this.formDevice.parent;

    };

    _public.save = function(event){

        var $btn = $(event.target).find("button:submit");
        $btn.data("loading-text", "Saving...");
        $btn.button('loading');

        _this.formDevice.uid = _this.formDevice.id; // change to UID (the server property)

        ODev.save(_this.formDevice, function(state){
            if($btn) $btn.button('reset');
            if(state){
                $.notify({message: "Saved"}, {type:"success"});
            }else{
                $.notify({message: "Error on Save"}, {type:"error"});
            }
        });

    };

    _public.deleteHistory = function(){

        ODev.deleteHitory(_this.device).then(function( data, textStatus, jqXHR ) {
            $.notify({message: "Removed"}, {type:"warning"});
            _this.history = [];
        }).fail(function(req){
            if(req.responseJSON && req.responseJSON.message){
                $.notify({message: req.responseJSON.message});
            }
        });
    };


    _public.getIcon = function(){
        if(this.formDevice.icon){
            return "images/devices/on/" + this.formDevice.icon;
        }else {
            return "images/boards/preview_board.svg";
        }
    };

    _public.nextPage = function(){
        if(_this.pageNumber > 1 && _this.history.length == 0) return;
        _this.pageNumber++;
        loadHistory.call(this);
    };

    _public.prevPage = function(){
        if(_this.pageNumber == 1) return;
        _this.pageNumber--;
        loadHistory.call(this);
    };

    // ============================================================================================
    // Private Functionsre
    // ============================================================================================

    function loadHistory(){

        if(_this.pageNumber <= 0) return;

        var query = {
            deviceID : _this.device.id,
            periodType : _this.historyOptions.periodType,
            periodValue : _this.historyOptions.periodValue,
            aggregation : 'NONE',
            order       : 'DESC',
            maxResults : 100,
            pageNumber : _this.pageNumber
        };

        var spinner = new Spinner().spin();
        $('.spinner',$("#device-history .box-header")).remove();
        $("#device-history .box-header").append(spinner.el);

        OpenDevice.history(query, function (data) {

            console.log("Loaded Data ["+(data.length)+"]: "+_this.device.description);

            spinner.stop();

            $scope.$apply(function () {
                _this.history = data;
            });

        },function (jq,status,message) {
            var message = (jq.responseJSON.message || message);
            console.error(message);
        });
    }


});

