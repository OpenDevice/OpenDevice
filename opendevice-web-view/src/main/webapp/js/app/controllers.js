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
// NOTE: Small/Simple controllers is here

var pkg = angular.module('opendevice.controllers');

pkg.controller('PageController', ['$http', '$scope', function ( $http, $scope ) {

    // Alias / Imports
    var _this = this;
    var _public = this;

    // Public
    // ==========================

    _public.logout = function(){
        ODev.logout(function(){
            window.location = "/login.html?message=Logged%20out";
        });
    };

    _public.replace = function(target, el) {
        $(target).html("");
        $(target).append($(el));
    };

}]);