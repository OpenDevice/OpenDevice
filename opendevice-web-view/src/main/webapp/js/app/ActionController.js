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
 * Controller for Actions
 *
 * @author Ricardo JL Rufino
 * @date 01/11/16
 */
pkg.controller('ActionController', function ($scope) {

    // Alias / Imports
    var DType = od.DeviceType;

    // Private
    // ==========================
    var _this = this;
    var _public = this;


    // Public
    // ==========================

    this.list = [];

    this.targetDevices = []; // for actions

    $scope.options = {
        actionTypes : [
            { code : "control", description : "Control Devices"},
            { code : "email", description : "Email Notification"},
            { code : "whatsapp", description : "Whatsapp Notification"},
            { code : "script", description : "Execute Script"},
            { code : "action", description : "Saved Action(s)"}
        ]
    };

    _public.init = function(){

        _this.targetDevices = ODev.getDevices();

    };


    _public.groupDevices = function (item){
        if (item.parent)
            return item.parent.title;
        else
            return "Standalone"
    };

    // ============================================================================================
    // Private Functionsre
    // ============================================================================================

});
