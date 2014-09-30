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
    this.id = data.id;
    this.name = data.name;
    this.type = data.type;
    this.category = data.category;
    this.value = data.value;
    this.sensor = data.sensor;
    this.manager = od.deviceManager;

    this.setValue = function(value){
        this.value = value;

        if(this.manager){
            this.manager.setValue(this.id, this.value);
        }
    }

    this.toggleValue = function(){
        var value = 0;
        if(this.value == 0) value = 1;
        else if(this.value == 1) value = 0;
        this.setValue(value);
    }

};