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

od.view.dashTypes = {
    DYNAMIC_VALUE: { // TODO: Need use Icons (On, OFF) ou background trasholder (ref: http://www.bootply.com/127431)
        id: "DYNAMIC_VALUE",
        name: "Dynamic Value",
        klass: "od.view.DashItemView",
        multipleDevices: false,
        allowSensor : true,
        allowDevice : false,
        deviceTypes: [od.DeviceType.ANALOG, od.DeviceType.NUMERIC],
        fields: [
            // Name, required
            ["aggregation", true],// "(!realtime)"
            ["realtime", false],
            ["period", true]
        ]

    }
};



/**
 * Responsable to render charts and view on dashboard
 * @date 25/04/2015
 * @class DashItemView
 */
od.view.DashItemView = Class.extend(function() {

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


    this.constructor = function (data) {
        this.setModel(data);
    }

    this.render = function ($el) {

    };

    this.onResize = function (force) {
    };

    this.onStartResize = function () {
    };


    this.destroy = function () {
        $(this.el).remove();
    };

    this.update = function (data) {

        var reloadDataset = false;  // some changes in Model, need reload chart

        if(this.model.error) reloadDataset = true;

        if(data.type != this.model.type) reloadDataset = true;

        if(data.realtime != this.model.realtime) reloadDataset = true;

        if(data.monitoredDevices.length != this.model.monitoredDevices.length){
            reloadDataset = true;
        }

        this.setModel(data);

        return reloadDataset;
    };

    this.setModel = function (data) {

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



});

// =====================================================================================================================
// Static Functions
// =====================================================================================================================
od.view.DashItemView.isCompatibleChart = function(type){
    if(type == 'LINE_CHART' || type == 'GAUGE_CHART' || type == 'PIE_CHART' || type == 'AREA_CHART'){
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