var app = angular.module('opendevice.services', ['ngResource']);


// =====================================
// REST
// =====================================

app.factory('DashboardRest', ['$resource', function($resource){

    return $resource('/middleware/dashboards/:id', { id: '@id', dashID : '@dashID' }, { // configure defauls
        list: {method:'GET', url : "/middleware/dashboards", isArray:true,
            transformResponse: function(list){

                list = angular.fromJson(list);

                for (var i = 0; i < list.length; i++) {

                    var items = list[i].items;

                    for (var j = 0; j < items.length; j++) {
                        var item = items[j];

                        try {
                            if(typeof item.layout == "string") {
                                item.layout = JSON.parse(item.layout); // convert from String to Array
                            }
                        }catch (e){ item.layout = null;}

                        try {
                            if(typeof item.viewOptions == "string") {
                                item.viewOptions = JSON.parse(item.viewOptions); // convert from String to Array
                            }
                        }catch (e){ console.error('Error on viewOptions', e.stack); }

                        // if(!item.layout) item.layout = "[1,1,1,1]";

                    }

                }

                return list;
        }},
        activate: {method:'GET', url : "/middleware/dashboards/:id/activate"},
        items: {method:'GET', url : "/middleware/dashboards/:id/items", isArray:true},
        updateLayout: {method:'PUT', url : "/middleware/dashboards/:dashID/updateLayout",
            transformRequest: function(data) {
                var toSend = angular.copy(data);
                toSend.layout =  JSON.stringify(toSend.layout); // on server is a String
                if(toSend.viewOptions && toSend.viewOptions != "null")
                    toSend.viewOptions = JSON.stringify(toSend.viewOptions); // on server is a String
                return JSON.stringify(toSend);
            }
        },
        save: {method:'POST', url : "/middleware/dashboards"},
        saveItem: {method:'POST', url : "/middleware/dashboards/:dashID/item",
            transformRequest: function(data) {

                var toSend = angular.copy(data);

                if(toSend.layout && toSend.layout != "null")
                    toSend.layout =  JSON.stringify(toSend.layout); // on server is a String

                if(toSend.viewOptions && toSend.viewOptions != "null")
                    toSend.viewOptions = JSON.stringify(toSend.viewOptions); // on server is a String

                return JSON.stringify(toSend);
            },transformResponse: function(item, headersGetter){

                item = angular.fromJson(item);

                if(typeof item.viewOptions == "string" && item.viewOptions != "null") {
                    item.viewOptions = JSON.parse(item.viewOptions); // convert from String to Array
                }

                if(typeof item.layout == "string") {
                    item.layout = JSON.parse(item.layout); // convert from String to Array
                }

                return item;
            }
        },
        removeItem: {method:'DELETE', url : "/middleware/dashboards/:dashID/item"},
        deviceIcons: {method:'GET', url : "/middleware/dashboards/deviceIcons", isArray:true},
    });

}]);



app.factory('AccountRest', ['$resource', function($resource){

    var PATH = "/api/accounts";

    return $resource(PATH+'/:id', { id: '@id' }, { // configure defauls
        listUsers: {method:'GET', url : PATH+"/users", isArray:true},
        lisKeys: {method:'GET', url : PATH+"/keys", isArray:true},
        addUser: {method:'POST', url : PATH+"/users"},
        deleteUser: {method:'DELETE', url : PATH+"/users/:id"}
    });

}]);

app.factory('ConnectionRest', ['$resource', function($resource){

    var PATH = "/middleware/connections/:id";

    return $resource(PATH, { id: '@id' }, { // configure defauls
        discovery: {method:'GET', url : PATH+"/discovery", isArray:true},
    });

}]);

app.factory('RuleRest', ['$resource', function($resource){

    var PATH = "/middleware/rules/:id";

    var resource = $resource(PATH, { id: '@id' }, {
        activate: {method:'PUT', url : PATH+"/activate"},
        update: {method:'PUT'},
    })

    resource.prototype.$saveOrUpdate = function(callback) {
        if (this.id) {
            return this.$update(callback);
        } else {
            return this.$save(callback);
        }
    };

    return resource;

}]);

app.factory('JobRest', ['$resource', function($resource){

    var PATH = "/middleware/jobs/:id";

    var resource = $resource(PATH, { id: '@id' }, {
        activate: {method:'PUT', url : PATH+"/activate"},
        update: {method:'PUT'},
    })

    resource.prototype.$saveOrUpdate = function(callback) {
        if (this.id) {
            return this.$update(callback);
        } else {
            return this.$save(callback);
        }
    };

    return resource;

}]);


app.factory('FirmwareRest', ['$resource', function($resource){

    var PATH = "/middleware/firmwares/:id";

    return $resource(PATH, { id: '@id' }, { // configure defauls
        sendUpdate: {method:'GET', url : PATH+"/sendUpdate", isArray:false},
    });

}]);


// =====================================
// Utils
// =====================================


/**
 * A generic confirmation for risky actions.
 * Usage: Add attributes: ng-really-message="Are you sure"? ng-really-click="takeAction()" function
 */
app.directive('ngReallyClick', [function() {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            element.bind('click', function() {

                var $dialog = $('#confirm-dialog');

                var message = attrs.ngReallyMessage;

                if(message) $dialog.find(".modal-title").text(message);

                $dialog.modal('show');

                $dialog.find("button:last").one('click', function(){
                    scope.$apply(attrs.ngReallyClick);
                    $dialog.modal('hide');
                });

            });
        }
    }
}]);
