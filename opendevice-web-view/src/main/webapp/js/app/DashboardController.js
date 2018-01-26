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
 * Note: Access this controller from Chrome Debugger
 * angular.element(".content-wrapper").controller()
 * @author Ricardo JL Rufino
 * @date 06/07/14
 */
pkg.controller('DashboardController', ['$timeout', '$http', '$scope', 'DashboardRest', function ($timeout, $http, $scope, DashboardRest /*Service*/ ) {

    // Alias / Imports
    var DCategory = od.DeviceCategory;
    var DType = od.DeviceType;
    var DashItemView = od.view.DashItemView;

    // Private
    // ==========================

    var $dashboards; // @HtmlElement - $('.dashboards');
    var $layoutManager; // @Object - gridster instance
    var keydownListener;

    var _this = this;
    var _public = this;

    // Public
    // ==========================

    this.status = '';
    this.editMode = false;
    this.dashboard = null;
    this.dashboardItems = [ ]; // opendevice/DashItemView.js
    this.dashboardList = [];
    this.itemViewSelected = null;
    this.gridConf = {};
    this.odevListeners = []; // required because of our simple-page-model

    _public.init = function(){

        $(function(){

            $dashboards = $('.dashboards');

            var Key = {
                LEFT: 37,  UP: 38,  RIGHT: 39, DOWN: 40, F2 : 113, ESC : 27
            };

            $(document).on("keydown", keydownListener = function(event){

                if($(event.target).is(":input")){ // avoif affect fields,selects
                    return;
                }

                if(event.keyCode == Key.F2){
                    _this.toggleEdit();
                    $scope.$apply();
                }

                // F3
                if(event.keyCode == 114){
                    _this.addNewView();
                    $scope.$apply();
                    event.preventDefault();
                    event.stopPropagation(); // ignore browser search
                }

                // ESC
                if(event.keyCode == Key.ESC){
                    closeSidebar();

                    if(_this.editMode){
                        _this.toggleEdit(false);
                        $scope.$apply();
                    }

                }

                if (event.ctrlKey || event.metaKey) {
                    switch (String.fromCharCode(event.which).toLowerCase()) {
                        case 's':
                            event.preventDefault();
                            if(_this.editMode) _this.save();
                            break;
                        //case 'f':
                        //    event.preventDefault();
                        //    alert('ctrl-f');
                        //    break;
                        //case 'g':
                        //    event.preventDefault();
                        //    alert('ctrl-g');
                        //    break;
                    }
                }

                // Change chart using Keys
                if(event.keyCode > 48 && event.keyCode < 58){
                    var index = (event.keyCode - 48) - 1;
                    _this.activateDash(_this.dashboardList[index]);
                }

                // Plus (+)
                if (event.keyCode == 187 && _this.itemViewSelected != null) {
                    _this.updatePeriod(_this.itemViewSelected, true);
                }

                // Minus (-)
                if (event.keyCode == 189 && _this.itemViewSelected != null) {
                    _this.updatePeriod(_this.itemViewSelected, false);
                }

            });

            //$scope.$on('$viewContentLoaded', function(){
            //   // View loaded...
            //});

        });



        // Destroy Controller Event
        $scope.$on("$destroy", function() {
            for (var i = 0; i < _this.dashboardItems.length; i++) {
                var itemView = _this.dashboardItems[i];
                itemView.destroy();
            }

            // Unregister listeners on change page.
            ODev.removeListener(_this.odevListeners);

            closeSidebar();

            $(document).off("keydown", keydownListener);
        });

        configureLayoutManager();

        // // Load Dashboard's
        // // Wait for devices full loaded
        // _this.odevListeners.push(ODev.onConnect(function(){
        //     if(!_this.dashboardList.length) _this.syncDashboards();
        // }));

        _this.syncDashboards();

    };

    // ============================================================================================
    // Public Functions
    // ============================================================================================


    _public.updateSidebar = function(){
        // Configure sidebar
        var $side = $("aside.control-sidebar");
        var actions = $(".sidebar-content");
        $side.html(actions);
    }

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
        _this.itemViewSelected = null;

        var items = dashboard.items;

        // Remove Current
        // Clean LayoutManager
        if(_this.dashboardItems.length > 0){

            for (var i = 0; i < _this.dashboardItems.length; i++) {
                var itemView = _this.dashboardItems[i];
                itemView.destroy();
            }

            //$layoutManager.disable();
            //$layoutManager.remove_all_widgets();
            _this.dashboardItems = [];

        }

        // TODO: check if $timeout is required (for angular render)
        $timeout(function(){
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                if(typeof item.layout == "string") {
                    item.layout = JSON.parse(item.layout); // convert from String to Array
                }
                var dashType = od.view.dashTypes[item.type];
                var klass = eval(dashType.klass); // get reference to implementation class
                _this.dashboardItems.push(new klass(item)); // fire 'onRenderDashboardItems'
            }
        });

        closeSidebar();

    };

    _public.syncDashboards = function() {
        // Load Dashboard's
        DashboardRest.list({}, function (values) {

            _this.dashboardList = values;

            angular.forEach(values, function (dashboard, index) {

                if (dashboard.active) {

                    _this.activateDash(dashboard);

                    return;
                }

            });

        });
    }

    _public.addNewDash = function(){
        $scope.$broadcast('newDash'); // fire 'open' in NewDashController
    };


    _public.addNewView = function(type){
        //_this.dashboardItems.push( new DashItemView({ configMode : true, title : 'Grafico 3 ', type : 'LINE_CHART', layout : [1,3,1,1], metrics : [{ deviceID : 2 , type : 'realtime'  }]}));
        // NOTE: this will fire: renderCompleteDashItem, to setup appropriate configs

        if(!type){

            var $side = $("aside.control-sidebar");

            $.AdminLTE.controlSidebar.open($side, true);

            $(".dashboards").one('click', function(){
                $.AdminLTE.controlSidebar.close($side, true);
            });

        }else{
            $scope.$broadcast('newItem', type);
        }

    };

    _public.insertNewItem = function(data){
        _this.toggleEdit(true);
        var dashType = od.view.dashTypes[data.type];
        var klass = eval(dashType.klass); // get reference to implementation class
        _this.dashboardItems.push(new klass(data)); // will trigger 'ng-repeat' and 'onRenderDashboardItems'

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

        DashboardRest.removeItem({id : item.id, dashID : this.dashboard.id});

    };

    _public.editItem = function(index){

        var item = _this.dashboardItems[index];

        $scope.$broadcast('editItem', {
            data: item.model
        });

    };



    /*
     * Change edit mode
     * This is called automatic, using watch
     */
    _public.toggleEdit = function(value){

        if(value == null){
            value = !_this.editMode;
        }

        _this.editMode = value;
        $layoutManager.resizable.enabled = value;
        $layoutManager.draggable.enabled = value;
    };


    /**
     * Save changes in active dashboard
     */
    _public.save = function(event){

        if(!_this.editMode) return;

        if(event){

            var inputs = $(event.target).serializeArray();
            var data = {};
            inputs.forEach(function (element) {
                data[element.name] = element.value;
            });

            // Update Local
            angular.extend(_this.dashboard, data);

            // Copy properties
            data = angular.extend({},_this.dashboard);

            delete data.items; // avoid serialize

            DashboardRest.save(data, function(resp){
                _this.editMode = false;
                $scope.$apply();
            });

        // CTRL + S  (Only save Layout)
        }else{
            _this.editMode = false;
            $scope.$apply();
        }

    };


    _public.delete = function(dasboard){

        if(!dasboard) dasboard = _this.dashboard;

        DashboardRest.delete({id : dasboard.id}, function(){
            _this.syncDashboards();
            _this.toggleEdit(false);
        });

    };


    /**
     * Set current Chart/View to use custom Keyboards
     * @param item
     */
    _public.setItemFocus = function(item){
        _public.itemViewSelected = item;
    };

    /**
     * Update chart/view period
     * Using 'throttle' to avoid multiple request at same time (on Keyboard)
     */
    _public.updatePeriod = Utils.throttle(function(index, add){
        var itemView = _this.dashboardItems[index];

        var periodValue =  (add ? itemView.model.periodValue + 1 : itemView.model.periodValue - 1);

        if(periodValue < 1) return;

        itemView.model.periodValue = periodValue;

        // required for REST Url.
        itemView.model.dashID = _this.dashboard.id;

        _this.notifyUpdateItem(itemView.model);
        _this.updatePeriodByGroup(itemView.model);

        // Save on server
        DashboardRest.saveItem(itemView.model, function(data){
            console.log('DashboardController::updatePeriod:', data);
        });
    }, 200);

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
    _public.onRenderDashboardItems = function(scope) {

        // Wait angular render html to initialize charts.
        $timeout(function(){
            angular.forEach(_this.dashboardItems, function(item, index) {
                if(!item.initialized) {
                    console.log('Initializing Chart/View: ' + item.title, item);
                    var $el = $('.dash-body', $dashboards).eq(index);
                    item.render($el); // init
                }
            });
        },100);

    };

    _public.onGridInit = function(scope) {
        $layoutManager = scope.gridster;
    };

    _public.addItemListener = function ($gridScope, itemView) {

        $gridScope.$on('gridster-item-transition-end', function (event, source) {

            itemView.onResize();

        });
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
     * Check views affected by grad-and-drop, resize
     * @returns {Array}
     */
    function gridDetectLayoutChanges(){

        var changes = [];

        angular.forEach(_this.dashboardItems, function(item, index) {
            var model = item.model;

            if(model.layout == null){
                model.layout = {};
                $.extend(model.layout, item.layout); // clone;
                changes.push(item);
                return;
            }

            if(item.layout.col != model.layout.col ||
                item.layout.row != model.layout.row ||
                item.layout.sizeX != model.layout.sizeX ||
                item.layout.sizeY != model.layout.sizeY){
                $.extend(model.layout, item.layout); // clone;
                changes.push(item);
            }
        });

        return changes;

    };

    /**
     * Dynamic grid system configuration
     * Builds upon the plugin: jquery.gridster
     */
    function configureLayoutManager(){
        _this.gridConf = {
            //margins: [5, 5],
            columns: 6,
            rowHeight : 100,
            avoid_overlapped_widgets : false,
            mobileBreakPoint: 600,
            resizable: {
                enabled: false,
                start: function (e, $ui, $element){
                    var dashView = _this.dashboardItems[$ui.data("index")];
                    dashView.onStartResize();
                },
                stop : function (e, $ui, $element){
                    var itensChanged = gridDetectLayoutChanges();

                    console.log("resize:: views changed:", itensChanged);

                    angular.forEach(itensChanged, function(item, index) {

                        item.model.dashID = _this.dashboard.id;

                        // Hack :: force update after animations (gridster-item-transition-end) see : addItemListener
                        $timeout(function(){
                            item.onResize(true); // force..
                        }, 500);

                        DashboardRest.updateLayout(item.model); // Save on database.
                    });
                }
            },
            draggable : {
                enabled: false,
                handle: '.dash-move',
                stop : function(){
                    var itensChanged = gridDetectLayoutChanges();
                    console.log("views changed:", itensChanged);

                    angular.forEach(itensChanged, function(item, index) {
                        item.model.dashID = _this.dashboard.id;
                        DashboardRest.updateLayout(item.model); // Save on database.
                    });
                }

            }
        };

        // Hack :: on gridster-resized (and window scrollbar shows), the layout change and affect charts
        // This will force charts resize
        $scope.$on("gridster-resized", function(){
            angular.forEach(_this.dashboardItems, function(item, index) {
                item.onResize(true);
            });
        });


        //var blockWidth = $dashboards.width();
        //blockWidth = (blockWidth / _this.gridConf.min_cols).toFixed() - 3;
        //_this.gridConf.widget_base_dimensions = [blockWidth, 150];

    }


    function closeSidebar(){
        var $side = $("aside.control-sidebar");
        $.AdminLTE.controlSidebar.close($side, true);
    }

}]);


// =========================================================================================================
// New Dashboard (Dialog in file dashboard.html)
// =========================================================================================================

pkg.controller('NewDashController', ['$scope','$timeout', 'DashboardRest', function ($scope, $timeout, DashboardRest) {

    // Private
    // ==========================
    var _this = this;
    var _public = this;
    var $el = $("#new-dash");

    var defaults = {
        title : ''
    };

    this.current = defaults;

    _public.init = function(){

        // Event received form DashboardController
        $scope.$on('editDash', function (scopeDetails, event) {
            _this.open(event.data);
            if(event.data.title) _this.current.titleVisible = true;
        });

        // Event received form DashboardController
        $scope.$on('newDash', function (scopeDetails, event) {
            _this.open(defaults);
        });

    };

    _public.open = function(data){

        console.log("Open Dialog", data);

        if(!data) data = defaults;

        _this.current = JSON.parse(JSON.stringify(data)); // clone

        $el.modal('show');

    };

    _public.save = function(event){

        var ctrl = $scope.$parent.ctrl;

        var isEdit = _this.current.id;

        var $btn = $el.find("button:submit");
        $btn.data("loading-text", "Saving...");
        $btn.button('loading');

        if(!isEdit) _this.current.active = true;

        // Save on server
        var req = DashboardRest.save(_this.current, function(data){
            console.log('NewDashController:: saved item: ', data);
            if($btn) $btn.button('reset');
            _this.current = defaults; // clear form
            if(!isEdit){
                ctrl.syncDashboards();
            }
            //else ctrl.notifyUpdateItem(data);
            $el.modal('hide');
        });

    };
}]);

// =========================================================================================================
// NewItemController / New Chart (Dialog in file dashboard.html)
// =========================================================================================================

pkg.controller('NewItemController', ['$scope','$timeout', 'DashboardRest', function ($scope, $timeout, DashboardRest) {

    // Private
    // ==========================
    var _this = this;
    var _public = this;
    var $el = $("#new-item-dialog");


    // Public
    // ==========================

    this.supportedTypes  = od.view.dashTypes;

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

    this.deviceIcons = [];
    this.devices = [];

    // ====================================
    //  Public
    // ====================================

    _public.init = function(){

        // Event received form DashboardController.editItem
        $scope.$on('editItem', function (scopeDetails, event) {
            _this.open(event.data);
            if(event.data.title) _this.current.titleVisible = true;
        });

        // Event received form DashboardController
        $scope.$on('newItem', function (event, type) {
            var options = angular.copy(defaults);

            options.type = type;

            _this.open(options);
        });

        DashboardRest.deviceIcons(function(data){
            data.forEach(function(item){
                _this.deviceIcons.push({id:item, name: item });
            });
        });

    };

    _public.onSelectType = function(){

        setTypeDefaults(_this.current); //update

        var fields = getAllFields();
        if(fields) fields.each(function(){
            var $field = $(this);
            var name = $field.data("fname");
            if(isEnabled(name)){
                $field.show();
            }else{
                $field.hide();
            }
        });
    };


    // FIXME: Remove
    _public.enableAggregation = function(){
        return od.view.DashItemView.requireAggregation(_this.current.type) && ! _this.current.realtime;
    };

    // FIXME: Remove
    _public.enableRange = function(){
        return (_this.current.type == "LINE_CHART" || _this.current.type == "GAUGE_CHART") ;
    };

    _public.open = function(data){

        console.log("Open Dialog", data);

        _this.current = angular.copy(data);

        _this.devices = ODev.getDevices();

        _this.selectedType = {id : data.type}; // hack for select

        _this.onSelectType(); // show/hide fields

        var periodEnd = $el.find("input[name=periodEnd]");

        if(!periodEnd.data("range-enabled")){

            periodEnd.data("range-enabled", true);

            periodEnd.daterangepicker({
                "singleDatePicker": true,
                "timePicker": true,
                "timePicker24Hour": true,
                "linkedCalendars": false,
                "showCustomRangeLabel": false,
                "autoApply": true,
                "autoUpdateInput": false,
                locale: {
                    format: 'DD/MM/YY HH:mm'
                },
                "startDate": (_this.current.periodEnd || new Date())
            }, function(start, end, label) {
                console.log("New date range selected: ' + start.format('YYYY-MM-DD') + ' to ' + end.format('YYYY-MM-DD') + ' (predefined range: ' + label + ')");
            });

            periodEnd.on('apply.daterangepicker', function(ev, picker) {
                $(this).val(picker.startDate.format('DD/MM/YY HH:mm'));
                _this.current.periodEnd = picker.startDate.format('DD/MM/YY HH:mm');
            });

            periodEnd.on('cancel.daterangepicker', function(ev, picker) {
                if(_this.current.periodEnd){
                    $(this).val(_this.current.periodEnd);
                }
            });
        }else{
            if(_this.current.periodEnd) periodEnd.data('daterangepicker').setStartDate(_this.current.periodEnd);
        }


        $('#new-item-dialog').modal('show');

    };

    _public.save = function(dialog){

        var ctrl = $scope.$parent.ctrl;

        // required for REST Url.
        _this.current.dashID = ctrl.dashboard.id;

        var isEdit = _this.current.id;

        // _this.current.layout = null; // not update layout !

        var $btn = $el.find("button:submit");
        $btn.data("loading-text", "Saving...");
        $btn.button('loading');

        if(isEdit) ctrl.updatePeriodByGroup(_this.current);

        // Save on server
        var req = DashboardRest.saveItem(_this.current, function(data){
            console.log('NewItemController:: saved item: ', data);
            if($btn) $btn.button('reset');
            $el.modal('hide');
            if(!isEdit) ctrl.insertNewItem(data);
            else ctrl.notifyUpdateItem(data);
            _this.current = defaults;
        });

    };

    _public.selectGroupDevices = function (item){
        if (item.parent)
            return item.parent.title;
        else
            return "Standalone"
    };

    // ====================================
    //  Private
    // ====================================

    function getAllFields(){
        return $("[data-fname]",$el);
    }

    function setTypeDefaults(defaults){

        var dashType = od.view.dashTypes[defaults.type];

        if(!dashType) return defaults;

        var viewOptions = defaults.viewOptions || {};

        dashType.fields.forEach(function(field){
            if(field[2] && !viewOptions[field[0]] ){
                viewOptions[field[0]] = field[2];
            }
        });

        defaults.viewOptions = viewOptions;

        return defaults;
    }

    /**
     * Check if field is enabled/visible
     * @param name
     */
    function isEnabled(name){

        var dashType = od.view.dashTypes[_this.current.type];

        if(!dashType) return false;

        var enabled = false;

        dashType.fields.forEach(function(field){
            if(name == field[0]) enabled = true;
        });

        return enabled;
    }

}]);