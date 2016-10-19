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

var urlParams;

// Declare app level module which depends on filters, and services
var app = angular.module('opendevice', [
    'ngRoute',
    'ngSanitize', // for: ui.select
    'ngAnimate',
    'ui.select',
    'gridster',
    //'opendevice.filters',
    //'opendevice.directives',
    'opendevice.services',
    'opendevice.controllers'
]);

angular.module('opendevice.controllers', []);


// Constants
// ===================
// app.constant('opendevice_url', 'http://'+window.location.host);

// Global variables
app.run(function($rootScope) {

    OpenDevice.setAppID(OpenDevice.findAppID()); // find ApiKey in cookie/localstore
    $rootScope.ext = {}; // Extension support
    $rootScope.ext.menu = [];

    ODev.connect();

    ODev.on("loginFail", function(){
        window.location = "/?message=Not%20Logged%20or%20Expired";
    });

    $( document ).ajaxError(function( event, jqXHR, ajaxSettings, thrownError ) {
        //alert("error");
        //window.location = "/?message=Not%20Logged%20or%20Expired";
    });


    $.notifyDefaults({
        type: 'danger',
        allow_dismiss: true,
        delay: 3000
    });


    // Radialize the colors
    Highcharts.getOptions().colors = Highcharts.map(Highcharts.getOptions().colors, function (color) {
        return {
            radialGradient: { cx: 0.5, cy: 0.3, r: 0.7 },
            stops: [
                [0, color],
                [1, Highcharts.Color(color).brighten(-0.3).get('rgb')] // darken
            ]
        };
    });


    Highcharts.setOptions({
        global: {useUTC: false, colorSetup : true}
    });

});

app.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/', {templateUrl: 'pages/dashboard.html', controller: 'DashboardController',  controllerAs: 'ctrl'});
    $routeProvider.when('/boards', {templateUrl: 'pages/boards.html', controller: 'DeviceController',  controllerAs: 'ctrl'});
    $routeProvider.when('/boards/:boardID', {templateUrl: 'pages/devices.html', controller: 'DeviceController',  controllerAs: 'ctrl'});
    $routeProvider.when('/new', {templateUrl: 'pages/new.html', controller: 'DeviceController',  controllerAs: 'ctrl'});
    $routeProvider.when('/users', {templateUrl: 'pages/users.html', controller: 'UserController',  controllerAs: 'ctrl'});
    $routeProvider.when('/connections', {templateUrl: 'pages/connections.html', controller: 'ConnectionController',  controllerAs: 'ctrl'});
    $routeProvider.otherwise({redirectTo: '/'});
}]);

// Configuration to Work like JSP templates
app.config(['$interpolateProvider', function($interpolateProvider) {
    $interpolateProvider.startSymbol('${');
    $interpolateProvider.endSymbol('}');
}]);

app.config(['$animateProvider', function($animateProvider) {
    $animateProvider.classNameFilter(/animate/);
}]);

app.filter('propsFilter', function() {
    return function(items, props) {
        var out = [];

        if (angular.isArray(items)) {
            items.forEach(function(item) {
                var itemMatches = false;

                var keys = Object.keys(props);
                for (var i = 0; i < keys.length; i++) {
                    var prop = keys[i];
                    var text = props[prop].toLowerCase();
                    if (item[prop].toString().toLowerCase().indexOf(text) !== -1) {
                        itemMatches = true;
                        break;
                    }
                }

                if (itemMatches) {
                    out.push(item);
                }
            });
        } else {
            // Let the output be the input untouched
            out = items;
        }

        return out;
    };
});


