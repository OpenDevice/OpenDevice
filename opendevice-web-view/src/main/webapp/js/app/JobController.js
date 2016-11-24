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
 * Controller for Rules
 *
 * @author Ricardo JL Rufino
 * @date 01/11/16
 */
pkg.controller('JobController', function ($scope, $location, $timeout, $routeParams, JobRest) {

    // Private
    // ==========================
    var _this = this;
    var _public = this;

    var odevListeners = []; // required because of our simple-page-model

    // Public
    // ==========================

    this.list = [];

    $scope.model = new JobRest(); // curren editing

    _public.init = function(){

        if($routeParams.id == "new" || $routeParams.id != null){

            $scope.model.type = "cron"; // Default...

            if($routeParams.id != "new") {
                $scope.model = JobRest.get({id: $routeParams.id}, function (item) {
                    $('#cronInput').jqCronGetInstance().setCron(item.cronExpression);
                });
            }

            $('#cronInput').jqCron({
                enabled_minute: true,
                multiple_dom: true,
                multiple_month: true,
                multiple_mins: true,
                multiple_dow: true,
                multiple_time_hours: true,
                multiple_time_minutes: true,
                default_period: 'day',
                no_reset_button: false,
                lang: 'en',
                bind_method: {
                    set: function($element, value) {
                        $scope.model.cronExpression = value;
                        $element.val(value);
                    }
                }
            });

        } else { //  List Page

            _this.list = JobRest.query();

            // Defines a list where temporary listeners will be registered
            ODev.setListenerReceiver(odevListeners);

            // Fired by Sync or by Server
            ODev.on("jobs_update", function(message){
                $timeout(function(){
                    _this.list = JobRest.query();
                }, 1000);
            });


        }

        // Destroy Controller Event
        $scope.$on("$destroy", function() {
            ODev.removeListener(odevListeners);
        });


    };

    // ============================================================================================
    // Public Functions
    // ============================================================================================


    _public.save = function(){

        $scope.model.$saveOrUpdate(function (response) {
            $.notify({message: "Saved"}, {type:"success"});
            $location.path('/jobs');
        });

    };

    _public.delete = function(item, index){
        item.$delete(function(){
            _this.list.splice(index, 1);
        });
    };

    _public.activate = function(item){

        item.$activate(); // send updated value

    };

    _public.onRenderItems = function(){
        $timeout(function(){
            $('.cronspec').jqCron({
                disable : true,
                enabled_minute: true,
                multiple_dom: true,
                multiple_month: true,
                multiple_mins: true,
                multiple_dow: true,
                multiple_time_hours: true,
                multiple_time_minutes: true,});
        }, 200);
    };


    // ============================================================================================
    // Private Functionsre
    // ============================================================================================



});


pkg.filter('actionType', function() {
    return function(obj) {

        // Obj: {"type":"control","id":146552,"resourceID":213,"value":1}
        if(obj.type == "control"){
            var device = ODev.findDevice(obj.resourceID) || { name : "[Not Found Error]"};
            return "Control " + device.name;
        }

        obj = obj || '';

        return obj;
    };
});

