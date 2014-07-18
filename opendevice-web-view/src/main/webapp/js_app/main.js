'use strict';

// Declare app level module which depends on filters, and services
var app = angular.module('opendevice', [
    'ngRoute',
    //'myApp.filters',
    //'myApp.directives',
    'opendevice.services',
    'opendevice.controllers'
]);

// Constants
// ===================
app.constant('opendevice_url', 'http://'+window.location.host);
app.constant('clientID', 'fake-client-123-123');

app.config(['$routeProvider', function($routeProvider) {
    //$routeProvider.when('/view1', {templateUrl: 'partials/partial1.html', controller: 'MyCtrl1'});
    //$routeProvider.when('/view2', {templateUrl: 'partials/partial2.html', controller: 'MyCtrl2'});
    //$routeProvider.otherwise({redirectTo: '/view1'});
}]);

app.config(['$interpolateProvider', function($interpolateProvider) {
    $interpolateProvider.startSymbol('${');
    $interpolateProvider.endSymbol('}');
}]);