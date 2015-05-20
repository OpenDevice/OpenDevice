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

/**
 *
 * @date 25/04/2015
 * @class DashItemView
 */
od.DashItemView = (function() {

    //Chart.defaults.global.responsive = true;
    Chart.defaults.global.maintainAspectRatio = false;


    /**
     * DashItemView
     * @param config
     * @constructor
     */
    var MODULE = function DashItemView(data) {

        this.chart;
        this.el;
        this.initialized = false;
        this.configMode = false;

        // Copy all Atributes
        for (var attrname in data) {
            if(attrname.lastIndexOf("$", 0) == -1){ // ignore angularjs trash
                this[attrname] = data[attrname];
            }
        }


        this.data = {
            labels: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"],
            datasets: [
                {
                    label: "My First dataset",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: [65, 59, 80, 81, 56, 55, 40]
                },
                {
                    label: "My Second dataset",
                    fillColor: "rgba(151,187,205,0.2)",
                    strokeColor: "rgba(151,187,205,1)",
                    pointColor: "rgba(151,187,205,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(151,187,205,1)",
                    data: [28, 48, 40, 19, 86, 27, 90]
                }
            ]
        };

    };


    // ==========================================================================
    // Public
    // ==========================================================================

    var _public = MODULE.prototype;

    _public.init = function(container, index){

        this.el = $("li", container).eq(index);

        if(this.type == 'LINE_CHART'){
            var config = {scaleShowVerticalLines : false};

            var ctx = $("canvas", this.el).get(0).getContext("2d");
            this.chart = new Chart(ctx).Line(this.data, config);
        }

        this.initialized = true;

    };

    _public.onResize = function(){
        this.chart.destroy();
        var ctx = $("canvas", this.el).get(0).getContext("2d");
        this.chart = new Chart(ctx).Line(this.data);
    };

    _public.test = function(){
        alert('test on ' +  this.title)
    };

    _public.destroy = function(){
        if(this.chart) this.chart.destroy();
    }

    // ==========================================================================
    // Private
    // ==========================================================================

    function anotherPrivate(){
        alert('private:' + this.id);
    }

    return MODULE;
}());




od.BaseService = (function() {

    // private
    // ===================

    var fileExtension = 'mp3';

    /**
     *
     * @param config
     * @constructor
     */
    var MODULE = function BaseService(config) {

        // Copy all Atributes
        for (var attrname in config) { this[attrname] = config[attrname]; }

        this.el;
        this.configMode;

    };

    // ==========================================================================
    // Public
    // ==========================================================================

    var _public = MODULE.prototype;

    _public.init = function (episode) {
        this.anotherPublic();
        anotherPrivate.call(this);
    };

    _public.anotherPublic = function (episode) {
        alert('public:' + this.id);
     };

    // ==========================================================================
    // Private
    // ==========================================================================

    function anotherPrivate(){
        alert('private:' + this.id);
    }

    // ==========================================================================
    // STATIC ???????
    // ==========================================================================

    // TODO !

    return MODULE;
}());