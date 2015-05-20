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

var app = angular.module('opendevice.controllers', []);


/**
 * DashboardController for AngularJS
 * @author Ricardo JL Rufino
 * @date 06/07/14
 */
app.controller('DashboardController', ['$timeout', '$http', '$scope', 'DashboardRest', function ($timeout, $http, $scope, DashboardRest /*Service*/ ) {

    // Alias / Imports
    var DCategory = od.DeviceCategory;
    var DType = od.DeviceType;
    var DashItemView = od.view.DashItemView;

    // Private
    // ==========================

    var audioContext;
    var audioPlay;

    var $dashboards; // @HtmlElement - $('.dashboards');
    var $layoutManager; // @Object - jquery.gridster instance

    var _this = this;
    var _public = this;

    // Public
    // ==========================

    this.status = '';
    this.devices = [];
    this.dashboardItems = [ ]; // view
    this.dashboard = { id : 1};
    this.dashboardList = [];

    _public.init = function(){

        $(function(){

            $dashboards = $('.dashboards');

        });

        OpenDevice.connect(function(devices){
            //var list = devices.filter(function(obj) {
            //    return obj.type == od.DeviceType.DIGITAL;
            //});
            _this.devices = devices;
        });


        OpenDevice.onDeviceChange(function(device){
            if(device) {
                if (device.type == od.DeviceType.DIGITAL) {
                    playSound(device);
                }

                $timeout(function(){
                    $scope.$apply(); // sync view
                });
            }
        });

        OpenDevice.on(od.Event.DEVICE_LIST_UPDATE, function(devices){
            _this.devices = devices;
        });

        // Load Dashboard's
        DashboardRest.list({},function(values){

            _this.dashboardList = values;

            angular.forEach(values, function(dashboard, index) {

                if(dashboard.active){

                    _this.activateDash(dashboard);

                    return;
                }

            });

        });

        // AudioContext detection
        try {
            // Fix up for prefixing
            window.AudioContext = window.AudioContext||window.webkitAudioContext;
            audioContext = new AudioContext();
        }catch(e) {
            alert('Web Audio API is not supported in this browser');
        }




    }

    // ============================================================================================
    // Public Functions
    // ============================================================================================

    _public.activateDash = function(dashboard){

        if(!dashboard.active){

            // disable others
            for (var i = 0; i < _this.dashboardList.length; i++) {
                var current = _this.dashboardList[i];
                current.active = false;
            }

            dashboard.active = true;
            DashboardRest.activate({id : dashboard.id}); // save on database
        }

        _this.dashboard = dashboard;

        var items = dashboard.items;

        // Remove Current
        // Destroy LayoutManager
        if(_this.dashboardItems.length > 0){

            for (var i = 0; i < _this.dashboardItems.length; i++) {
                var itemView = _this.dashboardItems[i];
                itemView.destroy();
            }
            _this.dashboardItems = [];

            $layoutManager.destroy();
            $layoutManager = null;

        }


        // TODO: check if $timeout is required
        $timeout(function(){
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                _this.dashboardItems.push(new DashItemView(item));
            }
        },100);


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

    _public.addNewDash = function(){
        //_this.dashboardItems.push( new DashItemView({ configMode : true, title : 'Grafico 3 ', type : 'LINE_CHART', layout : [1,3,1,1], metrics : [{ deviceID : 2 , type : 'realtime'  }]}));
        // NOTE: this will fire: renderCompleteDashItem, to setup appropriate configs

        $scope.$broadcast('newItem');

    };

    _public.addNewView = function(){
        //_this.dashboardItems.push( new DashItemView({ configMode : true, title : 'Grafico 3 ', type : 'LINE_CHART', layout : [1,3,1,1], metrics : [{ deviceID : 2 , type : 'realtime'  }]}));
        // NOTE: this will fire: renderCompleteDashItem, to setup appropriate configs

        $scope.$broadcast('newItem');

    };

    _public.insertNewItem = function(data){

        var item = new DashItemView(data);

        _this.dashboardItems.push(item); // will trigger 'ng-repeat' and 'onRenderDashboardItems'

    };

    _public.notifyUpdateItem = function(model){
        var item = findDashboardItem(model.id);
        item.update(model);
    };

    _public.removeItem = function(index){

        console.log("removendo item:", index);
        var item = _this.dashboardItems[index];

        item.destroy();

        _this.dashboardItems.splice( index, 1 );

        $layoutManager.remove_widget($('li', $dashboards).eq(index));

        DashboardRest.removeItem({id : item.id, dashID : this.dashboard.id});

    };

    _public.editItem = function(index){

        var item = _this.dashboardItems[index];

        $scope.$broadcast('editItem', {
            data: item.model
        });

    };

    _public.updatePeriod = function(index, add){
        var item = _this.dashboardItems[index];

        item.model.periodValue =  (add ? item.model.periodValue + 1 : item.model.periodValue - 1);

        // required for REST Url.
        item.model.dashID = _this.dashboard.id;

        _this.notifyUpdateItem(item.model);
        _this.updatePeriodByGroup(item.model);

        // Save on server
        DashboardRest.saveItem(item.model, function(data){
            console.log('DashboardController::updatePeriod:', data);
        });
    };

    /**
     * Update Range/Period of same group.
     * If occur a period change in one item , all items of group are changed
     * @param model - Item Model
     */
    _public.updatePeriodByGroup = function(model){
        if(model.itemGroup > 0){
            var items = listItemsByGroup(model.itemGroup);
            for (var i = 0; i < items.length; i++) {
                var itemView = items[i];

                // Check if period changed
                if(model.periodValue != itemView.model.periodValue || model.periodType != itemView.model.periodType){
                    console.log('Updating item group: ' + model.itemGroup, itemView.model.title);
                    itemView.model.dashID = _this.dashboard.id;
                    itemView.model.periodValue = model.periodValue;
                    itemView.model.periodType = model.periodType;
                    itemView.update(itemView.model);
                    DashboardRest.saveItem(itemView.model); // save on database.
                }

            }
        }
    } ;

    /**
     * This method is called by angularjs (ngrepeat) when you add a new item to the list: 'dashboardItems'
     * @param container
     */
    _public.onRenderDashboardItems = function() {

        var $items = $dashboards.find('li');

        // Wait to render/css...
        $timeout(function(){

            // Check if layout manager has initialized
            if(!$layoutManager && _this.dashboardItems.length > 0){
                configureLayoutManager();
            }

            console.log('onRenderDashboardItems: length:' + $items.length);

            angular.forEach(_this.dashboardItems, function(item, index) {

                if(!item.initialized){

                    var $item = $($items.get(index));

                    if( ! $item.data('inLayoutManager')){ // new added by user

                        console.log('new added by user');

                        $item.data('inLayoutManager', true);

                        var $w = $layoutManager.add_widget($item, 1, 1);

                        var layout = $layoutManager.serialize($w)[0];
                        item.layout = layout;
                        layout.dashID = _this.dashboard.id;
                        // Save on database.
                        DashboardRest.updateLayout(layout);
                    }

                    console.log('Initializing: ', item);
                    item.init($dashboards, index);

                }
            });
        }, 100);

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

    _public.isChartView = function(type){
        return DashItemView.isCompatibleChart(type);
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


    function findDashboardItem(id){
        var values = _this.dashboardItems;
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
    function listItemsByGroup(itemGroup){
        var values = _this.dashboardItems;
        var items = [];
        if(values){
            for(var i = 0; i < values.length; i++){
                if(values[i].model.itemGroup == itemGroup){
                    items.push(values[i]);
                }
            }
        } else{
            console.warn("Not loaded or empty !");
        }

        return items;
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


    /**
     * Dynamic grid system configuration
     * Builds upon the plugin: jquery.gridster
     */
    function configureLayoutManager(){

        var  gridConf = {
            widget_margins: [5, 5],
            max_cols: 6,
            min_cols: 5,
            //max_rows : 3,
            //extra_cols : 0,
            //max_size_x : 6,
            serialize_params: function($w, wgd) {
                return { id : $w.data('itemid'), layout : "["+wgd.row+","+wgd.col+","+wgd.size_x+","+wgd.size_y+"]" }
            },
            resize: {
                enabled: true,
                // @function: OnResize
                stop : function (e, ui, $widget){
                    var dashView = $widget.scope().item; // FIXME: remove access to scope()
                    dashView.onResize();

                    var item = {
                        dashID : _this.dashboard.id,
                        id : dashView.id,
                        layout : "["+ $widget.data('row') + "," + $widget.data('col')+ "," + $widget.data('sizex')+ "," + $widget.data('sizey') + "]"
                    };

                    // Save on database.
                    DashboardRest.updateLayout(item);
                },
                start: function (e, ui, $widget){
                    var dashView = $widget.scope().item; // FIXME: remove access to scope()
                    dashView.onStartResize();
                }
            },
            draggable : {

                handle: '.dash-move',

                stop : function (e, ui){

                    var changed = $layoutManager.serialize_changed( );

                    console.log("changed:", changed)

                    angular.forEach(changed, function(item, index) {
                        item.dashID = _this.dashboard.id;
                        // Save on database.
                        DashboardRest.updateLayout(item);
                    });

                    $layoutManager.$changed = $([]);

                }
            }
        };

        var blockWidth = $dashboards.width();
        blockWidth = (blockWidth / gridConf.min_cols).toFixed() - 12;
        gridConf.widget_base_dimensions = [blockWidth, 150];

        $layoutManager = $(".dashboards > ul").gridster(gridConf).data('gridster');

        $('li', $dashboards).each(function(){
            $(this).data('inLayoutManager', true);
        });
    }

}]);


// =========================================================================================================
// NewItemController (Dialog in file dashboard.html)
// =========================================================================================================

app.controller('NewItemController', ['$scope','$timeout', 'DashboardRest', function ($scope, $timeout, DashboardRest) {

    // Private
    // ==========================
    var _this = this;
    var _public = this;

    var loading;

    // Public
    // ==========================

    this.supportedTypes  = od.view.availableTypes;

    var defaults = {
        title : '',
        type : 'LINE_CHART',
        realtime : false,
        periodValue : 1,
        periodType : 'MINUTE',
        aggregation : "NONE",
        itemGroup : 0,
        titleVisible : true

    };

    this.current = defaults;

    _public.init = function(){

        $("#new-item-dialog").dialog({
            autoOpen: false,
            title: "New Dashboard View",
            modal: true,
            width: "640",
            open: function( event, ui ) {},
            buttons: [{
                text: "Save",
                click: function() {
                    loading = Ladda.create($('.ui-dialog-buttonset button').get(0));
                    loading.start();
                    _this.save(this);
                }
            }]
        });

        // Event received form DashboardController.editItem
        $scope.$on('editItem', function (scopeDetails, event) {
            _this.open(event.data);
            if(event.data.title) _this.current.titleVisible = true;
        });

        // Event received form DashboardController
        $scope.$on('newItem', function (scopeDetails, event) {
            _this.open(defaults);
        });

    };

    _public.enableAggregation = function(){
        return od.view.DashItemView.requireAggregation(_this.current.type) && ! _this.current.realtime;
    };

    _public.enableRange = function(){
        return (_this.current.type == "LINE_CHART" || _this.current.type == "GAUGE_CHART") ;
    };

    _public.open = function(data){

        console.log(data);
        _this.current = JSON.parse(JSON.stringify(data)); // clone

        _this.selectedType = {id : data.type}; // hack for select

        $("#new-item-dialog").dialog("option", {modal: true}).dialog("open");

    };

    _public.save = function(dialog){

        var dashCtrl = $scope.$parent.dashCtrl;

        // required for REST Url.
        _this.current.dashID = dashCtrl.dashboard.id;

        var isEdit = _this.current.id;

        _this.current.layout = null; // not update layout !

        if(isEdit) dashCtrl.updatePeriodByGroup(_this.current);

        //var itemclone = JSON.parse(JSON.toString(_this.current));

        // Save on server
        var req = DashboardRest.saveItem(_this.current, function(data){
            console.log('NewItemController:: saved item: ', data);
            $(dialog).dialog( "close" );
            if(loading) loading.stop();
            if(!isEdit) dashCtrl.insertNewItem(data);
            else dashCtrl.notifyUpdateItem(data);
        });

    };

}]);