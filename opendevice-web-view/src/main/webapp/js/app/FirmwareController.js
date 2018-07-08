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

var pkg = angular.module('opendevice.controllers');

pkg.controller('FirmwareController', function ($timeout, $http, $scope, FirmwareRest, ConnectionRest) {

    var _this = this;
    var _public = this;

    $scope.model = new FirmwareRest(); // curren editing

    this.list = [];
    this.connections = [];

    this.selectedFirmware;

    _public.init = function(){

        _this.list = FirmwareRest.query();

    };


    _public.save = function(e){

        e.preventDefault();
        var formData = new FormData(e.target);

        $.ajax({
            url: "/middleware/firmwares",
            type: 'POST',
            data: formData,
            success: function (data) {
                _this.list = FirmwareRest.query();
                $.notify({message: "Saved"}, {type:"success"});
                $scope.$apply();
            },
            cache: false,
            contentType: false,
            processData: false
        });

    };

    _public.delete = function(item, index){
        item.$delete(function(){
            _this.list.splice(index, 1);
            $.notify({message: "Removed"}, {type:"success"});
        }, function(error) {
            if(error.data && error.data.message){
                $.notify({message: error.data.message});
            }
        });
    };

    _public.showConnections = function(item){

        _public.selectedFirmware = item;
        // item.$activate(); // send updated value

        _this.connections = ConnectionRest.query();
        // returnList.$promise.then(function(list) {
        //     _this.connections = returnList;
        // });
    };

    _public.download = function(item){
        window.location = "/middleware/firmwares/download/" + item.uuid; // USE: FirmwareDownloadRest
    };

    _public.deploy = function(connectionUUID){

        _public.selectedFirmware.$sendUpdate({"connection" : connectionUUID},function(){
            $.notify({message: "Request Send"}, {type:"success"});
        }, function(error) {
            if(error.data && error.data.message){
                $.notify({message: error.data.message});
            }
        });


    };
});