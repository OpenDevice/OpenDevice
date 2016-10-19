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
 * Controller for Users/Accounts
 *
 * @author Ricardo JL Rufino
 * @date 16/10/16
 */
pkg.controller('UserController', function ($scope, AccountRest) {

    // Alias / Imports

    // Private
    // ==========================
    var _this = this;
    var _public = this;


    // Public
    // ==========================

    this.users = [];

    $scope.model = {}; // curren editing

    _public.init = function(){

        _this.users = AccountRest.listUsers();

        $(function(){

        });

    };

    // ============================================================================================
    // Public Functions
    // ============================================================================================


    _public.save = function(user, index){

        AccountRest.addUser(user, function(response){
            if(!user.id) _this.users.push(response);
            else _this.users[$scope.model.index] = response;

            $.notify({message: "Saved"}, {type:"success"});
            $scope.model = {};
        }, function(error) {
            if(error.data && error.data.message){
                $.notify({message: error.data.message});
            }
        });

    };

    _public.edit = function(user, index){

        $scope.model = angular.copy(user);
        $scope.model.index = index;

    };

    _public.delete = function(user, index){
        AccountRest.deleteUser({id : user.id}, function() {
            _this.users.splice(index, 1);
            $.notify({message: "Removed"}, {type:"warning"});
        }, function(error) {
            if(error.data && error.data.message){
                $.notify({message: error.data.message});
            }
        });
    };





    // ============================================================================================
    // Private Functionsre
    // ============================================================================================



});

