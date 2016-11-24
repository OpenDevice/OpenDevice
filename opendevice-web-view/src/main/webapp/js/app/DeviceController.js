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
pkg.controller('DeviceController', function ($scope, $routeParams, $timeout, $http, ConnectionRest) {

    // Alias / Imports
    var DCategory = od.DeviceCategory;
    var DType = od.DeviceType;

    // Private
    // ==========================
    var _this = this;
    var _public = this;
    var odevListeners = []; // required because of our simple-page-model

    // Public
    // ==========================

    this.devices = [];
    this.discoveryList = [];

    this.sensorsCharts = []; // od.view.ChartItemView
    this.devicesCtrls = []; // od.view.DigitalCtrlView
    this.chartViewOptions = {
        "periodValue": 1,
        "periodType": "HOUR"
    };

    this.isBoardView = false;
    this.newBordPage = 'initialHelp';

    this.board;

    _public.init = function(){

        // Wait for devices full loaded
        if(!ODev.isConnected()){
            _this.odevListeners.push(ODev.onConnect(function(){
                _this.init();
            }));
            return;
        }

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

        if($routeParams.boardID == "standalone"){ // Standalone Devices (DeviceList)

            this.isBoardView = true;

            this.board = { id : "standalone", name : "Standalone Devices", devices : { length : standalone.length}};

        } else if($routeParams.boardID){ // Board Details (DeviceList)

            this.board = ODev.get($routeParams.boardID);

            this.isBoardView = true;

        } else { // Boards List Page

            this.isBoardView = false;

            this.board = null;

        }

        this.devices = filterLocalDevices.call(this);

        // Create Controllers and Charts for devices
        if(this.isBoardView){

            var charts = [];
            var ctrls = [];

            this.devices.forEach(function(device){

                var chart = createSensorChart.call(_this, device);
                if(chart) charts.push(chart);

                var ctrl = createDeviceControler.call(_this, device)
                if(ctrl) ctrls.push(ctrl);

            });

            _this.sensorsCharts = charts;
            _this.devicesCtrls = ctrls;
        }


        // Destroy Controller Event
        $scope.$on("$destroy", function() {

            for (var i = 0; i < _this.sensorsCharts.length; i++) {
                var view = _this.sensorsCharts[i];
                view.destroy();
            }
            for (var i = 0; i < _this.devicesCtrls.length; i++) {
                var view = _this.devicesCtrls[i];
                view.destroy();
            }

            // Unregister listeners on change page.
            ODev.removeListener(odevListeners);
        });

        $scope.$watch('ctrl.chartViewOptions', function() {
            updateCharts.call(_this);
        }, true);


        // Defines a list where temporary listeners will be registered
        ODev.setListenerReceiver(odevListeners);

        // Fired by Sync or by Server
        ODev.on(od.Event.DEVICE_LIST_UPDATE, function(devices){
            updateDevices();
        });

    };

    // ============================================================================================
    // Public Functions
    // ============================================================================================


    _public.newBoard = function(){

        $('#new-board').modal('show');

        _this.updateApiKeys();

        _this.newBordPage = 'initialHelp';


    };


    _public.delete = function(item, index){

        ODev.removeDevice(_this.devices[index]);

    };



    _public.updateApiKeys = function(){
        // Show ApiKeys
        $.get("/api/accounts/keys", function(data){
            if(data && data instanceof Array){
                var $ul = $('#new-board .apiKeyList');
                $ul.empty();
                data.forEach(function(item){
                    $ul.append("<li>" + item.key + " ( " + item.appName + " ) </li>")
                });
            }
        });
    };

    _public.selectBoardConnection = function(type){

        _this.newBordPage = 'local';

        _this.startDiscovery(type);

    };

    _public.startDiscovery = function(type){

        if(!type) alert("Require type param");

        _this.discoveryList = ConnectionRest.discovery({type : type});

    };

    /**
     * Create a new Connection (for local devices)
     * @param connection
     */
    _public.boardConnect = function(info){

        ConnectionRest.save(info, function(){
            // $('#new-board').modal('hide');
            ODev.sync(true, true); // fire: DEVICE_LIST_UPDATE
            $.notify({message: "Saved"}, {type:"success"});
        }, function(error) {
            if(error.data && error.data.message){
                $.notify({message: error.data.message});
            }
        });

    };


    _public.startSimulation = function(deviceID, interval, start){

        if(deviceID == null){

            $('#simulation').modal('show');

            _public.listSimulation();

        }else{

            $http.get("/tests/simulation/"+(start ? "start" : "stop")+"/"+deviceID+"?interval=" + interval).then(function(){

                if(start) _this.enableRealtime(deviceID, true);

                _public.listSimulation(); // update list

            }, function(response){ // error

                if(response.status == 503) {
                    alert("Maximum simulations reached. Stop simulations or increases in settings");
                }else{
                    console.error("Error starting simulation", response);
                    alert("Error starting simulation");
                }
            });
        }

    };

    _public.restoreSimulation = function(){
        $http.get("/tests/simulation/list").then(function(response){
            var devices = response.data;
            if(devices && devices instanceof Array){
                for(var i = 0; i < devices.length; i++){
                    _this.enableRealtime(devices[i].id, true);
                }
            }
        });
    };

    _public.listSimulation = function(){
        $http.get("/tests/simulation/list").then(function(response){
            var data = response.data;
            if(data && data instanceof Array){
                $scope["simulaionList"] = data;
            }
        });
    };

    _public.inSimulation = function(deviceID){
        var devices = $scope["simulaionList"];
        if(devices){
            var found = ODev.findDevice(deviceID, devices);
            return found != null;
        }else{
            return false;
        }
    };


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
            cname += "/images/devices/lightbulb_";
        }

        if(device.category == DCategory.LAMP){
            cname += "/images/devices/lightbulb_";
        }

        if(device.category == DCategory.POWER_SOURCE){
            cname += "/images/devices/battery_";
        }

        if(device.sensor){
            cname += "/images/devices/sensor_";
        }

        if(device.value == 1){
            cname += "on.png";
        }else{
            cname += "off.png";
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

    _public.enableRealtime = function(deviceID, realtime){
        var item = findDashboardItem(deviceID);
        if(item){
            var model = angular.extend({}, item.model, { realtime : realtime});
            item.update(model);
        }
    };

    _public.setChartSize = function(kclass){

        var sizeClass = {
            "S" : "col-lg-4",
            "M" : "col-lg-6",
            "L" : "col-lg-12"
        };

        angular.forEach(_this.sensorsCharts, function(item, index) {

            var $container = $(".sensors-container");
            var $el = $('.sensors-view', $container).eq(index);

            item.onStartResize();

            var old = $el.data("sizeclass");
            if(old) $el.removeClass(old);

            $el.addClass(sizeClass[kclass]);
            $el.data("sizeclass", sizeClass[kclass]);

            setTimeout(function(){
                item.onResize(true);
            }, 400);

        });

    };

    _public.onRenderChartItems = function(scope){

        var $container = $(".sensors-container");

        // Wait angular render html to initialize charts.
        $timeout(function(){

            _public.setChartSize("S");

            // $("#char-size-ctrl button").last().button("toggle");

            angular.forEach(_this.sensorsCharts, function(item, index) {
                console.log('Initializing Chart/View: ' + item.title, item);
                if(!item.initialized) {
                    var $el = $('.sensor-chart-body', $container).eq(index);
                    item.render($el);
                }
            });
        });

        // Verify running simulations
        $timeout(function(){
           _this.restoreSimulation();
        }, 1000);

        return false;
    };

    _public.onRenderDeviceItems = function(scope){

        var $container = $(".devices-container");

        // Wait angular render html to initialize charts.
        $timeout(function(){

            angular.forEach(_this.devicesCtrls, function(item, index) {
                console.log('Initializing Device/View: ' + item.title, item);
                if(!item.initialized) {
                    var $el = $('.devices-view', $container).eq(index);
                    item.render($el);
                }
            });
        });

        return false;
    };


    // ============================================================================================
    // Private Functionsre
    // ============================================================================================

    function updateDevices(){

        var devices = filterLocalDevices.call(_this);

        var ctrls = [];

        for (var i = 0; i < _this.devicesCtrls.length; i++) {
            var view = _this.devicesCtrls[i];
            view.destroy();
        }

        devices.forEach(function(device){
            var ctrl = createDeviceControler.call(_this, device)
            if(ctrl) ctrls.push(ctrl);
        });

        _this.devicesCtrls = ctrls;

        _this.devices = devices;

    }

    function updateCharts(){
        angular.forEach(_this.sensorsCharts, function(item, index) {
            if(item.initialized && !item.model.realtime) {
                var model = angular.extend({}, item.model, _this.chartViewOptions);
                item.update(model);
            }
        });
    }


    function createSensorChart(sensor){

        if(this.isAnalogDevice(sensor)){
            var model = {
                "id": sensor.id,
                "title": sensor.name,
                "type": "LINE_CHART",
                // "layout": {"row": 0, "col": 2, "sizeX": 4, "sizeY": 2},
                "monitoredDevices": [sensor.id],
                "aggregation": "NONE",
                "itemGroup": 0,
                "realtime": false,
                "content": null,
                "scripts": null,
                "viewOptions": {}
            };

            angular.extend(model, this.chartViewOptions); // general options

            return new od.view.ChartItemView(model);
        }

        return null;
    }

    function createDeviceControler(device){

        if(this.isControllableDevice(device)){
            var model = {
                "type": "DIGITAL_CONTROLLER",
                "monitoredDevices": [device.id],
                "periodValue": 1,
                "realtime": true,
                "content": null,
                "scripts": null,
                "viewOptions": {
                    "iconON": "lightbulb_on.png",
                    "iconOFF": "lightbulb_off.png",
                    "textON": "ON",
                    "textOFF": "OFF"
                }
            };

            return new od.view.DigitalCtrlView(model);
        }else{

            return null;

        }


    }

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


    /**
     * Filter the devices (ODev) corresponding to selected board
     * @returns {Array}
     */
    function filterLocalDevices(){

        var devices = [];

        if(this.board && this.board.id == "standalone"){ // Standalone Devices

            devices = ODev.getDevicesByBoard(0);

        } else if(this.board && this.board.id){ // Board Details (DeviceList)

            devices = ODev.getDevicesByBoard(this.board.id);

        } else { // Open Boards Page

            devices = ODev.getBoards();

            var standalone = ODev.getDevicesByBoard(0);

            // Add another devices in fake Board
            if(standalone && standalone.length > 0){

                var Board = { id : "standalone", name : "Standalone Devices", devices : { length : standalone.length}};

                devices.push(Board);
            }

        }

        return devices;
    }

    function findDashboardItem(id){
        var values = _this.sensorsCharts;
        if(values){
            for(var i = 0; i < values.length; i++){
                if(values[i].id == id){
                    return values[i];
                }
            }
        } else{
            console.warn("Not loaded or empty !");
        }

        return null;
    }
});

