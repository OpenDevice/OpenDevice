'use strict';

var urlParams;

// Declare app level module which depends on filters, and services
var app = angular.module('opendevice', [
    'ngRoute',
    //'opendevice.filters',
    //'opendevice.directives',
    //'opendevice.services',
    'opendevice.controllers'
]);

// Constants
// ===================
// app.constant('opendevice_url', 'http://'+window.location.host);
// OpenDevice.setAppID = OpenDevice.findAppID() || 'clientname-123456x';
OpenDevice.setAppID = "*";

app.config(['$routeProvider', function($routeProvider) {
    //$routeProvider.when('/view1', {templateUrl: 'partials/partial1.html', controller: 'MyCtrl1'});
    //$routeProvider.when('/view2', {templateUrl: 'partials/partial2.html', controller: 'MyCtrl2'});
    //$routeProvider.otherwise({redirectTo: '/view1'});
}]);

// Configuration to Work like JSP templates
app.config(['$interpolateProvider', function($interpolateProvider) {
    $interpolateProvider.startSymbol('${');
    $interpolateProvider.endSymbol('}');
}]);


