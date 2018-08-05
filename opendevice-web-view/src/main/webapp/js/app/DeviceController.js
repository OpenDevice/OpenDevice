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
    var CType = od.CommandType;

    // Private
    // ==========================
    var _this = this;
    var _public = this;
    var odevListeners = []; // required because of our simple-page-model

    // Public
    // ==========================

    this.devices = [];
    this.discoveryList = [];

    this.historyCharts = []; // od.view.ChartItemView
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
            odevListeners.push(ODev.onConnect(function(){
                _this.init();
            }));
            // return; (allow off-line)
        }

        if($routeParams.boardID == "standalone"){ // Standalone Devices (DeviceList)

            this.isBoardView = true;

            this.board = { id : "standalone", name : "Standalone Devices", devices : { length : 0}};

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

            _this.historyCharts = charts;
            _this.devicesCtrls = ctrls;
        }


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

        // Watch for changes in Chart view options
        $scope.$watch('ctrl.chartViewOptions', function() {
            updateCharts.call(_this);
        }, true);


        // Defines a list where temporary listeners will be registered
        ODev.setListenerReceiver(odevListeners);

        // Fired by Sync or by Server
        ODev.on(od.Event.DEVICE_LIST_UPDATE, function(message){
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

    /** Dialog to list devices */
    _public.manageDevices = function(){

        $('#manageDevices').modal('show');

        _this.popupDevicesPage = 'list-devices';

    };

    _public.editDevice = function(device){
        $('#manageDevices').modal('hide');
        $("#manageDevices").on('hidden.bs.modal', function (e) {
            window.location = "#/devices/" + device.id;
        });


    };

    _public.delete = function(item, index){

        ODev.removeDevice(_this.devices[index]).then(function( data, textStatus, jqXHR ) {
            $.notify({message: "Removed"}, {type:"warning"});
        }).fail(function(req){
            if(req.responseJSON && req.responseJSON.message){
                $.notify({message: req.responseJSON.message});
            }
        });

    };

    _public.deleteHistory = function(item, index){

        ODev.deleteHitory(_this.devices[index]).then(function( data, textStatus, jqXHR ) {
            $.notify({message: "Removed"}, {type:"warning"});
        }).fail(function(req){
            if(req.responseJSON && req.responseJSON.message){
                $.notify({message: req.responseJSON.message});
            }
        });
    };


    _public.updateApiKeys = function(){
        // Show ApiKeys
        $.get("/api/accounts/keys", function(data){
            if(data && data instanceof Array){
                var $ul = $('#new-board .apiKeyList');
                $ul.empty();
                data.forEach(function(item){
                    var data = encodeURIComponent(location.host + "," + item.key);
                    $ul.append("<li>" + item.key + " ( " + location.host + " ) <br/>" +
                            "<img src='https://chart.googleapis.com/chart?cht=qr&chl="+data+"&chs=250x250' />" +
                            " </li>");
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

    /**
     * Start a tool to generate random data
     */
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

        if(device.type == od.DeviceType.ANALOG
            || device.type == od.DeviceType.FLOAT2
            || device.type == od.DeviceType.FLOAT4
            || device.type == od.DeviceType.FLOAT2_SIGNED) return true;

        return false;
    };

    _public.enableRealtime = function(deviceID, realtime){
        var item = findDashboardItem(deviceID);
        if(item){
            var model = angular.extend({}, item.model, { realtime : realtime});
            item.update(model);
        }
    };

    _public.showFullScreen = function(item){
        // var item = findDashboardItem(deviceID);
        if(item){
            $(item.el).toggleClass('chart-modal');
            item.getChart().reflow();

            $(document).one('keydown', function(e) {
                // ESCAPE key pressed
                if (e.keyCode == 27) {
                    $(item.el).toggleClass('chart-modal');
                    item.getChart().reflow();
                }
            });

        }
    };




    _public.setChartSize = function(kclass){

        var sizeClass = {
            "S" : "col-lg-4",
            "M" : "col-lg-6",
            "L" : "col-lg-12"
        };

        angular.forEach(_this.historyCharts, function(item, index) {

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

            angular.forEach(_this.historyCharts, function(item, index) {
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

        var devices = filterLocalDevices.call(_this); // get devices for current view.

        var ctrls = [];

        // Destroy all
        for (var i = 0; i < _this.devicesCtrls.length; i++) {
            var view = _this.devicesCtrls[i];
            view.destroy();
        }

        // Recreate All
        devices.forEach(function(device){
            var ctrl = createDeviceControler.call(_this, device)
            if(ctrl) ctrls.push(ctrl);
        });

        $scope.$apply(function () {
            _this.devices = devices;
            _this.devicesCtrls = ctrls;
        });

    }

    function updateCharts(){
        angular.forEach(_this.historyCharts, function(item, index) {

            if(item.initialized && _this.chartViewOptions.periodType == "REALTIME") {
                _public.enableRealtime(item.model.monitoredDevices[0], true);
            }

            if(item.initialized && !item.model.realtime) {
                var model = angular.extend({}, item.model, _this.chartViewOptions);
                item.update(model);
            }
        });
    }


    function createSensorChart(device){

        if(device.type == od.DeviceType.DIGITAL || od.DeviceType.isAnalog(device.type)){

            var model = {
                "id": device.id,
                "title": device.title,
                "type": "LINE_CHART",
                // "layout": {"row": 0, "col": 2, "sizeX": 4, "sizeY": 2},
                "monitoredDevices": [device.id],
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

        // if(this.isControllableDevice(device)){

        if(device.type != od.DeviceType.BOARD){
            var model = {
                "type": "GENERIC_VIEW",
                "monitoredDevices": [device.id],
                "periodValue": 1,
                "realtime": true,
                "content": null,
                "scripts": null,
                "viewOptions": {
                   //  "icon": "power.svg",
                    "textON": "ON",
                    "textOFF": "OFF"
                }
            };

            return new GenericDeviceView(model);
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
        var values = _this.historyCharts;
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


    // ===========================================================================================
    // GenericDeviceView
    // ===========================================================================================

    var GenericDeviceView = od.view.DashItemView.extend(function() {

        var _this = this;
        var deviceListeners = [];

        var HTML="";
        HTML += "<div class=\"device-view\">";
        HTML += "    <div class=\"device-view-icon\"><img src=\"/images/devices/lightbulb.png\"/><\/div>";
        HTML += "    <div class=\"device-view-content\">";
        HTML += "       <div class=\"dash-actions\">";
        HTML += "           <span title=\"Options\"><i class=\"fa fa-edit\"><\/i><\/span>";
        HTML += "       <\/div>";
        HTML += "       <span class=\"device-view-title\">Device<\/span>";
        HTML += "       <span class=\"device-view-value\">OFF<\/span>";
        HTML += "    <\/div>";
        HTML += "<\/div>";

        this.render = function ($el) {

            this.el = $el;

            // create HTML

            var _this = this;

            this.model.monitoredDevices.forEach(function(deviceID){

                var device = ODev.get(deviceID);

                if(!device) console.error("Device with id: " + deviceID + " not found, chart: " + _this.model.title);

                if(device){

                    var listener = device.onChange(onDeviceChange, _this);
                    deviceListeners.push(listener);

                    var $device = $(HTML);
                    _this.el.append($device);
                    $device.attr("data-deviceid", deviceID);
                    if(device.type == od.DeviceType.DIGITAL) $device.addClass("device-digital");
                    $device.click(setValue);
                    updateView.call(_this, $device, deviceID); // set values

                    var actions = $device.find(".dash-actions span");

                    actions.click(function(event){
                        event.stopPropagation();
                        var deviceID = $(event.currentTarget).parents('.device-view').data("deviceid");
                        window.location = "#/devices/" + deviceID;
                    });


                }
            });

        };

        this.destroy = function () {
            // Remove listeners from devices.
            this.model.monitoredDevices.forEach(function(deviceID, index) {
                var device = ODev.get(deviceID);
                if(device) device.removeListener(deviceListeners[index]);
            });

            this.super.destroy();
        }

        // ==========================================================================
        // Private
        // ==========================================================================

        function onDeviceChange(value, deviceID){
            var $device = $("[data-deviceid="+deviceID+"]", this.el);
            updateView.call(this, $device, deviceID); // use call(this, ) becouse is a OpenDevice inner event
        }

        function updateView($device, deviceID){
            var device = ODev.get(deviceID);
            $(".device-view-title", $device).text(device.title);

            var $value = $(".device-view-value", $device);

            var icon;
            if(device.type == od.DeviceType.DIGITAL){
                $value.text(device.isON() ? this.model.viewOptions.textON : this.model.viewOptions.textOFF);
                $value.removeClass("on off");
                $value.addClass(device.isON() ? "on" : "off");
                var iconName = device.icon || "power.svg";
                icon = device.isON() ? "on/" + iconName : "off/" + iconName;

            }else{
                var iconName = device.icon || "temp.svg";
                icon = "on/" + iconName;
                $value.text(device.value);
            }

            $("img", $device).attr('src', "/images/devices/" + icon);
        }

        function setValue(event){
            var deviceID = $(event.currentTarget).data("deviceid");
            OpenDevice.toggleValue(deviceID);
        }

    });
});

