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

    var CType = od.CommandType;
    var _this = this;

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


    this.on = function(){
         this.setValue(1);
    };

    this.off = function(){
         this.setValue(0);
    };

    this.isON = function(){
        return (value == 1)
    };

    this.isOFF = function(){
        return (value == 0)
    };

    this.setValue = function(value){
        this.value = value;

        if(this.manager){
            this.manager.setValue(this.id, this.value);
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


     _init.call(this, data);
};