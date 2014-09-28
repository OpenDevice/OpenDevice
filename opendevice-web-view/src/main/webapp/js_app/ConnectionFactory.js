
//app.provider('DeviceConnection', ['$window', function(win) {
//    return od.DeviceConnection;
//}]);


var app = angular.module('opendevice.services', []);

/**
 * Provide singleton instance of DeviceConfiguration  to Controllers.
 * @param opendevice_url - injected from constants
 * @returns {od.DeviceConnection}
 */
app.factory('connection', function(opendevice_url, applicationID) {

        var config = { url: opendevice_url, applicationID: applicationID, logLevel : 'debug', reconnectInterval : 5000, maxReconnectOnClose : 10};

        var connection = new od.DeviceConnection(config);

        connection.connect();

        // return the service instance
        return connection;
});

app.factory('manager', function(connection) {
    return new od.DeviceManager(connection);
});