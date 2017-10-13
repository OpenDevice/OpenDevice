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
 * Controller for ConnectionController.js
 *
 * @author Ricardo JL Rufino
 * @date 16/10/16
 */
pkg.controller('ConnectionController', function ($scope,$route, ConnectionRest) {

    // Alias / Imports

    // Private
    // ==========================
    var _this = this;
    var _public = this;
    // var updateInterval;


    // Public
    // ==========================

    this.list = [];

    $scope.model = {}; // curren editing

    _public.init = function(){

        _this.update();

        // Destroy Controller Event
        $scope.$on("$destroy", function() {
            // $interval.cancel(updateInterval);
        });


        $scope.alertNew = localStorage.getItem($route.current.controller+'.alertNew') || true;
        if($scope.alertNew === "false") $scope.alertNew = false;

    };

    // ============================================================================================
    // Public Functions
    // ============================================================================================

    _public.edit = function(user, index){

        $scope.model = angular.copy(user);
        $scope.model.index = index;

    };

    _public.update = function(){

        _this.list = ConnectionRest.query();

    };

    _public.disconnect = function(uuid){

        ConnectionRest.delete({id : uuid}, function(){
            _this.update();
            $.notify({message: "Disconnected"}, {type:"success"});
        }, function(error) {
            if(error.data && error.data.message){
                $.notify({message: error.data.message});
            }
        });

    };


    _public.disableAlert = function(name){
        localStorage.setItem($route.current.controller+'.'+name, 'false');
    };


    // ============================================================================================
    // Private Functionsre
    // ============================================================================================


});

