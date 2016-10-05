'use strict';

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

od.view = od.view || {};

od.view.availableTypes = {
    DYNAMIC_VALUE: "Dynamic Value",
    LINE_CHART:"Line Chart",
    PIE_CHART:"Pie Chart",
    GAUGE_CHART:"Gauge"
};

/**
 * Responsable to render charts and view on dashboard
 * @date 25/04/2015
 * @class DashItemView
 */
od.view.DashItemView = function(data) {

    var _public = this;
    var _this = this;

    // Private
    // ======================

    var chart;

    // Public
    // ======================

    this.el; // reference to div.dash-body
    this.initialized = false;
    this.configMode = false;
    this.resizing = false;
    this.data = [];

    function DashItemView(data){
        _this.setModel(data);
    }

    // ==========================================================================
    // Public
    // ==========================================================================



    _public.init = function (container, index) {

        _public.el = $('.dash-body', container).eq(index);

        if (od.view.DashItemView.isCompatibleChart(_this.model.type)) {
            try{

                loadData();

            }catch(e){console.error("Error initializing Chart: " +_this.model.title + "("+_this.model.type+")",e.stack);}
        }else if (_this.model.type == 'DYNAMIC_VALUE') {

        }

    };

    _public.onResize = function (force) {
        $(this.el).show();
        if(chart && (_this.resizing || force) ){
            _this.resizing = false;
            var $el = $(this.el);
            var $chart = $(".highcharts-container", $el);
            //chart.setSize($(this.el).width(), $(this.el).height(), false);
            chart.reflow ();
        }
    };

    _public.onStartResize = function () {
        if(chart){
            $(this.el).hide(); // hide for performance reasons
        }

        _this.resizing = true;
    };


    _public.destroy = function () {
        // TODO: remember to remove DEVICE LISTENERS (real-time)
        if (chart){
            try{chart.destroy();}catch (e){ console.error(e);} // FIX: avoid error if chart previos fail.
            $(this.el).remove();
        }
    };

    _public.update = function (data) {

        var reloadDataset = false;  // some changes in Model, need reload chart

        if(data.type != _this.model.type) reloadDataset = true;

        if(chart && data.viewOptions && _this.model.viewOptions){
            if(chart && data.viewOptions && data.viewOptions.max != _this.model.viewOptions.max) reloadDataset = true;
        }

        if(data.monitoredDevices.length != _this.model.monitoredDevices.length){
            reloadDataset = true;
        }

        this.setModel(data);

        if(reloadDataset){
            chart.destroy();
            if(!data.realtime) loadData();
        }else{
            if(!data.realtime) loadData();
        }

    };

    _public.setModel = function (data) {

        if (typeof  data.layout == 'string') data.layout = JSON.parse(data.layout); // convert from String to Array

        // Copy all Atributes
        for (var attrname in data) {
            if (attrname.lastIndexOf("$", 0) == -1) { // ignore angular.js trash
                this[attrname] = data[attrname];
            } else {
                data[attrname] = null; // ignore angular.js trash
            }
        }

        this["layout"] = $.extend({}, data.layout); // make copy (not reference) to detect changes.

        this.model = data;
    };



    // ==========================================================================
    // Private
    // ==========================================================================

    function initChart(data) {

        // Create series
        var devices = _this.model.monitoredDevices;
        var deviceSeries = [];
        var showLegends = false; // TODO: FROM CONFIG
        var viewOptions = _this.model.viewOptions;
        _this.initialized = true;

        if (_this.model.type == 'LINE_CHART') {

            for (var i = 0; i < devices.length; i++) {
                var device = OpenDevice.findDevice(devices[i]);

                var dserie = {
                    name: device.name,
                    showInLegend: showLegends,
                    data : data[i]
                };

                if(_this.model.realtime){
                    dserie.data = (function () {
                        // generate an array of random data
                        var data = [],
                            time = (new Date()).getTime(),
                            i;

                        for (i = -30; i <= 0; i += 1) {
                            data.push({
                                x: time + i * 1000,
                                y: 0
                            });
                        }
                        return data;
                    }());
                }

                deviceSeries.push(dserie);
            }

            chart = $(_this.el).highcharts({
                chart: {
                    type: 'spline',
                    zoomType: 'x',
                    margin: [ 10, 10, 25, 43]
                },
                title: {
                    text: '', style: {display: 'none'}
                },
                xAxis: {
                    type: 'datetime',
                    dateTimeLabelFormats: { // don't display the dummy year
                        second: '%H:%M:%S',
                        month: '%e. %b',
                        year: '%b'
                    },
                    title: {
                        text: '', style: {display: 'none'}
                    }
                },
                yAxis: {
                    title: {
                        text: null // Value title
                    },
                    min: (viewOptions && viewOptions.min ? viewOptions.min : 0),
                    max: (viewOptions && viewOptions.max ? viewOptions.max : null)
                },

                tooltip: {
                    headerFormat: '<b>{series.name}</b><br>',
                    pointFormat: '{point.x:%e. %b (%H:%M:%S)}: <b>{point.y}</b>'
                },
                credits: {
                    enabled: false
                },

                plotOptions: {
                    series: {
                        animation: false,
                        states: { hover: false }
                    },
                    spline: {
                        marker: { enabled: false }
                    }
                },

                series: deviceSeries
            }).highcharts();

        }

        if (_this.model.type == 'GAUGE_CHART') {

            chart = $(_this.el).highcharts({

                chart: {
                    type: 'solidgauge'
                },

                title: null,

                pane: {
                    center: ['50%', '95%'],
                    size: '180%',
                    startAngle: -90,
                    endAngle: 90,
                    background: {
                        backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || '#EEE',
                        innerRadius: '60%',
                        outerRadius: '100%',
                        shape: 'arc'
                    }
                },

                tooltip: {
                    enabled: false
                },

                plotOptions: {
                    solidgauge: {
                        dataLabels: {
                            y: -22,
                            borderWidth: 0,
                            useHTML: true
                        }
                    }
                },
                yAxis: {
                    min: (viewOptions && viewOptions.min ? viewOptions.min : 0),
                    max: (viewOptions && viewOptions.max ? viewOptions.max : null),
                    title: {
                        y: -80
                    },
                    stops: [ // TODO: FROM CONFIG
                        [0.1, '#55BF3B'], // green
                        [0.5, '#DDDF0D'], // yellow
                        [0.9, '#DF5353']  // red
                    ],
                    lineWidth: 0,
                    minorTickInterval: null,
                    tickPixelInterval: 400,
                    tickWidth: 0,
                    labels: {y: 16}
                },

                credits: {enabled: false},

                series: [{
                    name: 'Speed',
                    data: [10],
                    dataLabels: {
                        format: '<div style="text-align:center"><span style="font-size:22px;color:' +
                        ((Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black') + '">{y}</span><br/>'
                    },
                    tooltip: {
                        valueSuffix: ' N/A'
                    }
                }]

            }).highcharts();


        }

        if (_this.model.type == 'PIE_CHART') {

            // Build Series
            for (var i = 0; i < devices.length; i++) {
                var id = devices[i];
                var serie = {
                    name: OpenDevice.findDevice(id).name,
                    y : OpenDevice.findDevice(id).value
                };
                if(i == 0) {serie.sliced = true;serie.selected = true;};
                deviceSeries.push(serie);
            }

            // Build the chart
            chart = $(_this.el).highcharts({
                chart: {
                    plotBackgroundColor: null,
                    plotBorderWidth: null,
                    plotShadow: false
                },
                title: null,
                credits: {enabled: false},
                tooltip: {
                    pointFormat: '<b>{point.y} ({point.percentage:.1f} %)</b>'
                },
                plotOptions: {
                    pie: {
                        allowPointSelect: true,
                        cursor: 'pointer',
                        dataLabels: {
                            enabled: true, // TODO configuration > pie-semi-circle
                            format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                            style: {
                                color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                            },
                            connectorColor: 'silver'
                        }
                        // todo: configuration > pie-semi-circle
                        //startAngle: -90,
                        //endAngle: 90,
                        //center: ['50%', '75%']
                    }
                },
                series: [{
                    type: 'pie',
                    //innerSize: '50%', // TODO configuration > pie-semi-circle
                    name: 'Browser share',
                    data: deviceSeries
                }]
            }).highcharts();

        }
    }

    function loadData() {

        _this.data = [];

        var devices = _this.model.monitoredDevices;

        for (var i = 0; i < devices.length; i++) {
            loadDataFor(devices[i], i);
        }

    }


    /**@see loadData */
    function loadDataFor(deviceID, index) {

        // console.log(_this.title + ", Load: " + index);

        var query = {
            'deviceID' : deviceID,
            'periodType': _this.model.periodType,
            'periodValue': _this.model.periodValue,
            'aggregation': _this.model.aggregation
        };

        var spinner = new Spinner().spin();
        $('.spinner',_this.el).remove();
        $(_this.el).append(spinner.el);

        OpenDevice.history(query, function (response) {


            var data = [];
            for (var i = 0; i < response.length; i++) {
                data.push([response[i].timestamp, response[i].value]);
            }

            spinner.stop();

            var value = 0;

            if(data.length > 0 && ( _this.model.aggregation || _this.model.aggregation != "NONE")){
                var value  = data[0][1];
                if(value % 1 != 0){
                    value = data[0][1].toFixed(2);
                    value = Number(value);
                }
            }

            // FIXME: this musb by dynamic (detection of initialization type)
            if(od.view.DashItemView.isCompatibleChart(_this.model.type)){

                // console.log(_this.title + ", Load: " + index + " [done]");
                _this.data.push(data);

                // all loaded
                if(_this.data.length == _this.model.monitoredDevices.length){
                    initChart(_this.data);
                }

            }else{

                $('.text-value',_this.el).text(value);

                $(_this.el).textfill({maxFontPixels : 65, explicitWidth : $(_this.el).width() - 20});
            }

        });

    }


    /**
     * This function is called when the device has changed (OpenDevice.onDeviceChange)
     * @param device
     */
    function updateRealtimeData(device){

        var index = $.inArray(device.id, _this.model.monitoredDevices);

        if(index > -1){

            var value = device.value;

            if(chart){

                if(_this.model.type == "LINE_CHART"){
                    chart.series[index].addPoint([ (new Date()).getTime(), value], true, true);
                }else if(_this.model.type == "GAUGE_CHART"){
                    chart.series[index].points[0].update([0, value]);
                }else if(_this.model.type == "PIE_CHART"){
                    chart.series[0].data[index].update(value);
                }

            }else{
                $('.text-value',_this.el).text(value);

                $(_this.el).textfill({maxFontPixels : 65, explicitWidth : $(_this.el).width() - 20});
            }


        }

    }

    // Call 'constructor'/init
    DashItemView(data);
};

// =====================================================================================================================
// Static Functions
// =====================================================================================================================
od.view.DashItemView.isCompatibleChart = function(type){
    if(type == 'LINE_CHART' || type == 'GAUGE_CHART' || type == 'PIE_CHART'){
        return true;
    }
    return false;
};

od.view.DashItemView.requireAggregation = function(type){
    if(type == 'DYNAMIC_VALUE' || type == 'GAUGE_CHART' || type == 'PIE_CHART'){
        return true;
    }
    return false;
};


/*

Problemas, dificuldade de ter variaveis privadas na herança (elas se comportam como variáveis estáticas)
   - se for criar funcoes de acesso (http://stackoverflow.com/a/21862415/955857) mas tem problemas de memoria pois cria
     uma função para cada instância.



 */

//od.BaseService = (function() {
//
//    // private
//    // ===================
//
//    var fileExtension = 'mp3';
//
//    /**
//     *
//     * @param config
//     * @constructor
//     */
//    var MODULE = function BaseService(config) {
//
//        // Copy all Atributes
//        for (var attrname in config) { this[attrname] = config[attrname]; }
//
//        this.el;
//        this.configMode;
//
//    };
//
//    // ==========================================================================
//    // Public
//    // ==========================================================================
//
//    var _public = MODULE.prototype;
//
//    _public.init = function (episode) {
//        this.anotherPublic();
//        anotherPrivate.call(this);
//    };
//
//    _public.anotherPublic = function (episode) {
//        alert('public:' + this.id);
//     };
//
//    // ==========================================================================
//    // Private
//    // ==========================================================================
//
//    function anotherPrivate(){
//        alert('private:' + this.id);
//    }
//
//    // ==========================================================================
//    // STATIC ???????
//    // ==========================================================================
//
//    // TODO !
//
//    return MODULE;
//}());