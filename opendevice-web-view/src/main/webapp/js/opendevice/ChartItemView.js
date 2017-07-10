'use strict';
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


// Register new Type
$.extend(od.view.dashTypes, {
    LINE_CHART: {
        id: "LINE_CHART",
        name: "Line Chart",
        klass: "od.view.ChartItemView",
        multipleDevices: true,
        allowSensor : true,
        allowDevice : false,
        deviceTypes: [od.DeviceType.ANALOG],
        fields: [
            // Name, required
            ["realtime", false],
            ["period", true],
        ]
    },
    AREA_CHART: { // Use same chart/code of LINE
        id: "AREA_CHART",
        name: "Area Chart",
        klass: "od.view.ChartItemView",
        multipleDevices: false,
        allowSensor : true,
        allowDevice : true,
        deviceTypes: [od.DeviceType.DIGITAL],
        fields: [
            // Name, required
            ["realtime", false],
            ["period", true],
        ]
    },
    PIE_CHART: {
        id: "PIE_CHART",
        name: "Pie Chart",
        klass: "od.view.ChartItemView",
        multipleDevices: true,
        allowSensor : true,
        allowDevice : false,
        deviceTypes: [od.DeviceType.ANALOG],
        fields: [
            // Name, required
            ["aggregation", true],// "(!realtime)"
            ["realtime", false],
            ["period", true],
        ]

    },
    GAUGE_CHART: {
        id: "GAUGE_CHART",
        name: "Gauge",
        klass: "od.view.ChartItemView",
        multipleDevices: true,
        allowSensor : true,
        allowDevice : false,
        deviceTypes: [od.DeviceType.ANALOG],
        fields: [
            // Name, required
            ["subtype", true, null],
            ["aggregation", true],// "(!realtime)"
            ["realtime", false],
            ["period", true],
        ]

    }
});


/**
 * Responsable to render charts and view on dashboard
 * @date 25/04/2015
 * @class DashItemView
 */
od.view.ChartItemView = od.view.DashItemView.extend(function() {

    // Private
    // ======================

    var chart;
    var realtimeListeners = [];

    // Public
    // ======================

    this.el; // reference to div.dash-body
    this.initialized = false;
    this.configMode = false;
    this.resizing = false;
    this.data = [];


    // ==========================================================================
    // Public
    // ==========================================================================

    this.render = function ($el) {


        this.el = $el;

        if (this.model.realtime) {

            initChart.call(this);

            for (var i = 0; i < this.model.monitoredDevices.length; i++) {
                var device = ODev.get(this.model.monitoredDevices[i]);
                if(device){
                    var listener = device.onChange(updateRealtimeData, this);
                    realtimeListeners.push(listener);
                }else{
                    console.log("Falied on render, not found device: " + this.model.monitoredDevices[i] + ", index: " + i);
                }

            }

        }else{

            try{

                loadData.call(this);

            }catch(e){console.error("Error initializing Chart: " +this.model.title + "("+this.model.type+")",e.stack);}

        }

    };

    this.onResize = function (force) {
        $(this.el).show();
        if(chart && (this.resizing || force) ){
            this.resizing = false;
            var $el = $(this.el);
            var $chart = $(".highcharts-container", $el);
            $chart.css("width", "100%");
            chart.reflow();
        }
    };

    this.onStartResize = function () {
        if(chart){
            $(this.el).hide(); // hide for performance reasons
        }
        this.resizing = true;
    };


    this.destroy = function () {
        if (chart){

            removeRealtimeListeners.call(this);

            try{chart.destroy();}catch (e){ console.error(e);} // FIX: avoid error if chart previos fail.
        }
        $(this.el).empty();
    };

    this.update = function (data) {

        var reloadDataset = this.super.update(data);

        if(chart && data.viewOptions && this.model.viewOptions){
            if(chart && data.viewOptions && data.viewOptions.max != this.model.viewOptions.max) reloadDataset = true;
        }

        if(reloadDataset){
            this.destroy();
            this.render(this.el);
        }else{
            if(!data.realtime) loadData.call(this);
        }

    };



    // ==========================================================================
    // Private
    // ==========================================================================

    function initChart(data) {

        // Create series
        var devices = this.model.monitoredDevices;
        var deviceSeries = [];
        var showLegends = false; // TODO: FROM CONFIG
        var viewOptions = this.model.viewOptions;
        this.initialized = true;


        // =============================================================================================

        if (this.model.type == 'LINE_CHART' || this.model.type == 'AREA_CHART') {

            for (var i = 0; i < devices.length; i++) {
                var device = OpenDevice.findDevice(devices[i]);

                var dserie = {
                    name: device.name,
                    showInLegend: showLegends
                };

                if(device.type == od.DeviceType.DIGITAL){
                    dserie.step = 'left'; // make chart ON/OFF style
                }

                if(this.model.realtime){
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
                }else{
                    dserie.data = data[i]
                }

                deviceSeries.push(dserie);
            }

            // ===========================================================================
            // LINE_CHART for ANALOG
            // ===========================================================================

            var device = OpenDevice.findDevice(devices[0]);

            if(od.DeviceType.isNumeric(device.type)){

                // chart = $(this.el).highcharts({
                chart = $(this.el).highcharts({
                    chart: {
                        type: (this.model.type == 'LINE_CHART' ? 'spline' : 'areaspline'),
                        zoomType: 'xy',
                        panning : true,
                        panKey : 'shift',
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
                        //  spline: { animation: false, enableMouseTracking: false, stickyTracking: true, shadow: false, dataLabels: { style: { textShadow: false } } },
                        series: {
                            animation: false,
                            states: { hover: false }
                        },
                        spline: {
                            marker: { enabled: false },
                        },
                        areaspline: {
                            marker: {enabled: false}
                        }
                    },

                    series: deviceSeries
                }).highcharts();

            }

            // ===========================================================================
            // LINE_CHART for DIGITAL
            // ===========================================================================

            if(device.type == od.DeviceType.DIGITAL){

                chart = $(this.el).highcharts({
                    chart: {
                        type: (this.model.type == 'LINE_CHART' ? 'line' : 'area'),
                        zoomType: 'x',
                        panning : true,
                        panKey : 'shift',
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
                        title: { text: null  }, // Value title
                        min : 0,
                        max : 1
                    },
                    tooltip: {
                        // valueSuffix: '°C'
                    },
                    credits: {
                        enabled: false
                    },
                    plotOptions: {
                        line: {
                            marker: {enabled: false}
                        },
                        area: {
                            marker: {enabled: false}
                        }
                    },
                    series: deviceSeries
                }).highcharts();

            }

        }

        // ===========================================================================
        // GAUGE_CHART MODEL 1
        // ===========================================================================

        if (this.model.type == 'GAUGE_CHART' && this.model.viewOptions.subtype == "SOLID") {

            chart = $(this.el).highcharts({

                chart: {
                    type: 'solidgauge'
                },

                title: null,

                pane: {
                    center: ['50%', '95%'],
                    size: '150%',
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
                        animation : false,
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
                    name: 'Value',
                    data: [0],
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

        // ===========================================================================
        // GAUGE_CHART MODEL 2
        // ===========================================================================

        if (this.model.type == 'GAUGE_CHART' && this.model.viewOptions.subtype == "NORMAL") {

            var config = {

                chart: {
                    type: 'gauge',
                    plotBackgroundColor: null,
                    plotBackgroundImage: null,
                    plotBorderWidth: 0,
                    plotShadow: false
                },

                title: null,
                credits: {enabled: false},

                pane: {
                    startAngle: -150,
                    endAngle: 150,

                    // Border effects
                    background: [{
                        backgroundColor: {
                            linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                            stops: [
                                [0, '#FFF'],
                                [1, '#333']
                            ]
                        },
                        borderWidth: 0,
                        outerRadius: '109%'
                    }, {
                        backgroundColor: {
                            linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                            stops: [
                                [0, '#333'],
                                [1, '#FFF']
                            ]
                        },
                        borderWidth: 1,
                        outerRadius: '107%'
                    }, {
                        // default background
                    }, {
                        backgroundColor: '#DDD',
                        borderWidth: 0,
                        outerRadius: '105%',
                        innerRadius: '103%'
                    }]
                },

                // the value axis
                yAxis: {
                    minorTickInterval: 'auto',
                    minorTickWidth: 1,
                    minorTickLength: 10,
                    minorTickPosition: 'inside',
                    minorTickColor: '#666',

                    tickPixelInterval: 30,
                    tickWidth: 2,
                    tickPosition: 'inside',
                    tickLength: 10,
                    tickColor: '#666',
                    labels: {
                        step: 2,
                        rotation: 'auto'
                    },
                    title: {
                        text: null
                    }
                },

                series: [{
                    name: this.model.title,
                    data: [0],
                    dataLabels : {
                        // borderRadius: 5,
                        //backgroundColor: "rgba(244,109,67, 0.7)",
                        borderWidth: 0,
                        //borderColor: "#F46D43",
                        style: {
                            fontSize: "25px"
                        },
                        // color: "#c2c2c2",
                        // crop: false,
                        // overflow: "none",
                        // formatter: function () {
                        //     return '<span style="font-size: 30px;">' + this.point.y + '</span>';
                        // },
                        // y: -65,
                        y: 30
                        // zIndex: 10
                    }
                }]
            };


            // Generate bands
            // =========================================
            var min =  (viewOptions && viewOptions.min ? parseInt(viewOptions.min) : 0);
            var max = (viewOptions && viewOptions.max ? parseInt(viewOptions.max) : 100);

            config.yAxis.min = min;
            config.yAxis.max = max;

            var stepDiff = Math.round((max - min) / 3);
            config.yAxis["plotBands"] = [];
            var colors = [ '#55BF3B', '#DDDF0D', '#DF5353']

            var step = min;
            for(var i = 0; i < 3; i++){
                config.yAxis["plotBands"].push({
                    from: step,
                    to: step+stepDiff,
                    color: colors[i]
                });
                step+=stepDiff;
            }

            config.yAxis["plotBands"][2].to = max; // fix max

            chart = $(this.el).highcharts(config).highcharts();

        }

        // ===========================================================================
        // PIE_CHART
        // ===========================================================================

        if (this.model.type == 'PIE_CHART') {

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
            chart = $(this.el).highcharts({
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

        this.data = [];

        var devices = this.model.monitoredDevices;

        for (var i = 0; i < devices.length; i++) {
            loadDataFor.call(this, devices[i], i);
        }

    }


    /**@see loadData */
    function loadDataFor(deviceID, index) {

        // console.log(this.title + ", Load: " + index);

        var query = {
            'deviceID' : deviceID,
            'periodType': this.model.periodType,
            'periodValue': this.model.periodValue,
            'periodEnd': this.model.periodEnd,
            'aggregation': this.model.aggregation
        };

        var spinner = new Spinner().spin();
        $('.spinner',this.el).remove();
        $(this.el).append(spinner.el);

        var _this = this;

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
                    initChart.call(_this, _this.data);
                }

            }else{

                // FIXME: THIS IS FOR DYNAMIC VALUE (MOVED TO ANOTHER CLASS)
                $('.text-value',_this.el).text(value);

                $(_this.el).textfill({maxFontPixels : 65, explicitWidth : $(_this.el).width() - 20});
            }

        },function (jq,status,message) {
            _this.el.empty();
            var message = (jq.responseJSON.message || message);
            _this.el.append("<span class='itemview-error'>"+message+"</span>");
        });

    }

    function removeRealtimeListeners() {
        this.model.monitoredDevices.forEach(function(deviceID, index) {
            var device = ODev.get(deviceID);
            var list = realtimeListeners[index];
            console.log("Removing Listeners : " + deviceID);
            device.removeListener(list);
        });
    }

    /**
     * This function is called when the device has changed (OpenDevice.onDeviceChange)
     * @param device
     */
    function updateRealtimeData(value, deviceID){

        var index = $.inArray(deviceID, this.model.monitoredDevices);

        if(index > -1){

            if(chart){

                if(this.model.type == "LINE_CHART"){
                    chart.series[index].addPoint([ (new Date()).getTime(), value], /*redraw=*/ true, /*shift=*/true, /*animation=*/true);
                }else if(this.model.type == "GAUGE_CHART"){
                    chart.series[index].points[0].update([0, value]);
                }else if(this.model.type == "PIE_CHART"){
                    chart.series[0].data[index].update(value);
                }

                // FIXME: this is for DYNAMIC TEXT
            }else{
                $('.text-value',this.el).text(value);

                $(this.el).textfill({maxFontPixels : 65, explicitWidth : $(this.el).width() - 20});
            }

        }

    }
});
