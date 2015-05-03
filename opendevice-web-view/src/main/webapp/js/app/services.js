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

var app = angular.module('opendevice.services', ['ngResource']);

app.factory('Dashboard', ['$resource', function($resource){

    return $resource('dashboards/:id', { id: '@id', dashID : '@dashID' }, {
        items: {method:'GET', url : "dashboards/:id/items", isArray:true},
        updateLayout: {method:'PUT', url : "dashboards/:dashID/updateLayout"}
    });

}]);
