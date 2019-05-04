/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */



/** @namespace */
var od = od || {};

/** global instance. @type {{od.DeviceConnection}} */
od.connection = {};

od.ConnectionStatus = {
    CONNECTING: 1,
    CONNECTED : 2,
    DISCONNECTING :3,
    DISCONNECTED : 4,
    // LOGGINGIN : 5,
    FAIL : 6
};

/**
 * Represent a connection with server.
 * // TODO: adicionar documentação...
 * @param config
 * @constructor
 */
od.DeviceConnection = function(config){
    var _this = this;

    // Alias
    var Status = od.ConnectionStatus;
    // Private
    var socket = window.atmosphere || $.atmosphere;
    var serverConnection;
    var listeners = [];

    od.connection = this; // set global instance

    // public
    this.status = Status.DISCONNECTED;
    this.config = config;

    init(config);

    function init(_config){
        // _config.dropHeaders = false;

        if(_config["contentType"] == undefined)       _config["contentType"] = "application/json";
        if(_config["transport"] == undefined)         _config["transport"]   = "websocket";
        if(_config["fallbackTransport"] == undefined) _config["fallbackTransport"] = "long-polling";
        if(_config["reconnectInterval"] == undefined) _config["reconnectInterval"] = 5000;
        if(_config["maxReconnectOnClose"] == undefined) _config["maxReconnectOnClose"] = 5;

        _config.enableProtocol = false;

        _config.onError = function (response) {
            console.log("Connection.onError");
            setConnectionStatus(Status.FAIL);
        };

        _config.onMessage = function (response) {
            _onMessageReceived(response);
        };

        _config.onMessagePublished = function (response) {
            console.log("Connection.onMessagePublished");
        };

        // -----------------

        _config.onClose = function (response) {
            console.log("Connection.onClose");
            setConnectionStatus(Status.DISCONNECTED);
        };

        _config.onOpen = function (response) {
            console.log("Connection.onOpen");
            setConnectionStatus(Status.CONNECTED);
        };

        _config.onReopen = function (response) {
            console.log("Connection.onReopen");
            setConnectionStatus(Status.CONNECTED);
        };

        _config.onReconnect = function (response) {
            console.log("Connection.onReconnect");
            setConnectionStatus(Status.CONNECTING);
        };

        _config.onTransportFailure = function (response) {
            console.log("Connection.onTransportFailure");
            setConnectionStatus(Status.FAIL);
        };

        _config.onFailureToReconnect = function (response) {
            console.log("Connection.onFailureToReconnect");
            setConnectionStatus(Status.DISCONNECTED);
        };

        _config.onClientTimeout = function (response) {
            console.log("Connection.onClientTimeout");
            setConnectionStatus(Status.FAIL);
        };


    }

    this.connect = function(){
        if(_this.status != Status.CONNECTED){
            _this.config.url = _this.getUrl();
            console.log("Connection to: " + _this.config.url);
            serverConnection = socket.subscribe(_this.config);
            setConnectionStatus(Status.CONNECTING);
        }else{
            console.log("Already Connected");
        }
        return _this;
    };

    this.getUrl = function(){
        return od.serverURL + "/ws/device/" + od.appID;
    };

    this.send = function(data){
        // FIX: bug no atmophere que não enviar os headers da primeira conexao // TODO: registrar ticket
        // Somente na re-conexao ele passa a enviar...
        // NOTA: Isso já foi RESOLVIDO! injetando o "@Context AtmosphereResource", mas de qualquer maneira continua
        // existindo esse problema no atmophere
        // data["connectionUUID"] = serverConnection.getUUID();

        serverConnection.push(JSON.stringify(data));

    };

    this.addListener = function(listener){
        listeners.push(listener);
    };

    this.isConnected = function(){
        return _this.status == Status.CONNECTED;
    };

    this.getConnectionUUID = function(){
        return serverConnection.getUUID();
    };

    function notifyListeners(data){
        for(var i = 0; i<listeners.length; i++){
            var listener = listeners[i]["onMessageReceived"];
            if (typeof listener === "function") {
                listener(_this, data);
            }
        }
    }

    function setConnectionStatus(status){

        if(_this.status != status){
            for(var i = 0; i<listeners.length; i++){
                var listener = listeners[i]["connectionStateChanged"];
                if (typeof listener === "function") {
                    listener(_this, status, _this.status);
                }
            }
        }

        _this.status = status;

    }

    function _onMessageReceived(response){

        var data = null;

        // KeepAlive
        if(response.responseBody == "X") return;


        try {
            data = JSON.parse(response.responseBody);
        }catch(err) {
            console.error("Can't parse response: <<" + response.responseBody + ">>");
            console.error(err.stack);
            return;
        }

        // HACK: Atmosphere server not allow return statuscode > 400. The 'status' is in the message
        if(data.status && (data.status == 401 || data.status == 403)){
            console.warn("Authorization Required");
            notifyListeners({"type" : od.CommandType.CONNECT_RESPONSE,
                "status" : od.CommandStatus.UNAUTHORIZED});
            return;
        }

        if(data) {
            console.log("Connection.onMessageReceived(from:" + response.request.uuid + ") -> " + response.responseBody);
            notifyListeners(data);
        }

    }

};