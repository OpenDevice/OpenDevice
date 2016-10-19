/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

/** @namespace */
var od = od || {};


/**
 * Represent a Device
 * @param data - JSON
 * @constructor
 */
od.Device = function(data){

    // Private
    var CType = od.CommandType;
    var _this = this;

    // Public
    this.type = od.DeviceType.DIGITAL;
    this.listeners = [];

    function _init(data){

        this.id = data.id;
        this.manager = od.deviceManager;


        // Dynamic Properties and Funtions

        for (var attrname in data) this[attrname] = data[attrname];

        for (var property in this.properties) this[property] = this.properties[property];

        this.actions.forEach(function(method) {
            _this[method] = function(){
                console.log('Calling remote action: ' + method + ", params: ", arguments);
                var paramlist = [];
                for(var i in arguments) paramlist.push(arguments[i]);
                _this.manager.send({type : CType.ACTION, deviceID : _this.id, action : method, params : paramlist });
            }
        });

    }

    function notifyListeners(){

    }

    this.on = function(){
         this.setValue(1, true);
    };

    this.off = function(){
         this.setValue(0, true);
    };

    this.isON = function(){
        return (this.value == 1)
    };

    this.isOFF = function(){
        return (this.value == 0)
    };

    this.setValue = function(value, sync){

        sync = typeof sync !== 'undefined' ? sync : true; // default true

        if(this.type == od.DeviceType.NUMERIC || this.value != value){

            this.value = value;
            this.lastUpdate = new Date().getTime();

            if(this.manager){
                this.manager.notifyDeviceListeners(this, sync);
            }

        }

    };

    this.toggle = function(){
        var value = 0;
        if(this.value == 0) value = 1;
        else if(this.value == 1) value = 0;
        this.setValue(value);
    };

    /** @deprecated */
    this.toggleValue = this.toggle;

    /**
     * Register a listener to monitor changes in this Device.
     * @param {function} listener
     * @param {Object} [context] - Context to execute listener
     * @returns {{context: *, listener: *}} - return registred listener (used in #removeListener)
     */
    this.onChange = function(listener, context){
        var eventDef = {"context":context, "listener" : listener};
        this.listeners.push(eventDef);
        return eventDef;
    };

    /**
     *
     * @param eventDef {{context: *, listener: *}}
     */
    this.removeListener = function(eventDef){
        var index = this.listeners.indexOf(eventDef);
        if(index >= 0){
            this.listeners.splice(index, 1);
        }
    };

    // Initialize device data.
    _init.call(this, data);
};