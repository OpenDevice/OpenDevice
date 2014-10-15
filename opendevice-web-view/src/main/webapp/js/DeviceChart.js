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


/**
 * Class responsible for monitoring devices and plot the data on a graph.
 * This class uses the Flot library (http://www.flotcharts.org/)
 */
var DeviceChart = function (el, devicesIds) {

    var _this = this;

    var devices = [];
    var xcounter = 0;
    var data;

    var $chart;

    init();

    function init(){

        OpenDevice.on(od.Event.DEVICE_CHANGED, function(device){
            if(OpenDevice.contains(device, devices)){
                updateChart(device);
            }
        });

        OpenDevice.on(od.Event.CONNECTED, function(device){
            if(devicesIds){
                for(var i = 0; i<devicesIds.length; i++){
                    devices.push(OpenDevice.findDevice(devicesIds[i]));
                }
                initChart();
            }
        });



    }

    function clear(){
        data = [];
        for(var i = 0; i < devices.length; i++){
            var serie = { label : devices[i].name, deviceID :  devices[i].id};
            serie.data = [0,0];
            data.push(serie);
        }
    }

    function initChart(nseries){

        clear(); // init data array.

        $chart = $.plot(el, data, {
            series: {
                shadowSize: 0	// Drawing is faster without shadows,
                ,points: { show: false }
                ,lines: { show: true }
            },
            yaxis: {
                min: 0,
                max: 1100
            },
            xaxis: {
                show: false,
                min: 0,
                max: 100
            }
        });
    }

    function updateChart(device){

        var serie = getSerieIndex(device);

        if(xcounter == $chart.getXAxes()[0].max){
            clear();
            xcounter = 0;
        }


        for(var i = 0; i < data.length; i++){
            if(i == serie){
                data[i]['data'].push([xcounter, device.value]);
            }else{ // use same data
                var ds = data[i]['data'];
                var val = ds[ds.length - 1];
                data[i]['data'].push([xcounter, val[1]]);
            }
        }

        xcounter++;

        $chart.setData(data);
        $chart.draw();
    }

    function getSerieIndex(device){

        for(var i = 0; i < data.length; i++){
            if(data[i].deviceID == device.id){
                return i;
            }

        }

        return 0;
    }


    // Exported Methods / Vars
    return {

        addDevice : function(device){
            clear();
            devices.push(device);
        }

    };

};
