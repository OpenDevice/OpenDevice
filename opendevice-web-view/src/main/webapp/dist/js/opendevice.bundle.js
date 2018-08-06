/*
 * Copyright 2015 Async-IO.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Atmosphere.js
 * https://github.com/Atmosphere/atmosphere-javascript
 *
 * API reference
 * https://github.com/Atmosphere/atmosphere/wiki/jQuery.atmosphere.js-API
 *
 * Highly inspired by
 * - Portal by Donghwan Kim http://flowersinthesand.github.io/portal/
 */
(function (root, factory) {
    if (typeof define === "function" && define.amd) {
        // AMD
        define(factory);
    } else if(typeof exports !== 'undefined') {
        // CommonJS
        module.exports = factory();
    } else {
        // Browser globals, Window
        root.atmosphere = factory();
    }
}(this, function () {

    "use strict";

    var atmosphere = {},
        guid,
        offline = false,
        requests = [],
        callbacks = [],
        uuid = 0,
        hasOwn = Object.prototype.hasOwnProperty;

    atmosphere = {
        version: "2.3.3-javascript",
        onError: function (response) {
        },
        onClose: function (response) {
        },
        onOpen: function (response) {
        },
        onReopen: function (response) {
        },
        onMessage: function (response) {
        },
        onReconnect: function (request, response) {
        },
        onMessagePublished: function (response) {
        },
        onTransportFailure: function (errorMessage, _request) {
        },
        onLocalMessage: function (response) {
        },
        onFailureToReconnect: function (request, response) {
        },
        onClientTimeout: function (request) {
        },
        onOpenAfterResume: function (request) {
        },

        /**
         * Creates an object based on an atmosphere subscription that exposes functions defined by the Websocket interface.
         *
         * @class WebsocketApiAdapter
         * @param {Object} request the request object to build the underlying subscription
         * @constructor
         */
        WebsocketApiAdapter: function (request) {
            var _socket, _adapter;

            /**
             * Overrides the onMessage callback in given request.
             *
             * @method onMessage
             * @param {Object} e the event object
             */
            request.onMessage = function (e) {
                _adapter.onmessage({data: e.responseBody});
            };

            /**
             * Overrides the onMessagePublished callback in given request.
             *
             * @method onMessagePublished
             * @param {Object} e the event object
             */
            request.onMessagePublished = function (e) {
                _adapter.onmessage({data: e.responseBody});
            };

            /**
             * Overrides the onOpen callback in given request to proxy the event to the adapter.
             *
             * @method onOpen
             * @param {Object} e the event object
             */
            request.onOpen = function (e) {
                _adapter.onopen(e);
            };

            _adapter = {
                close: function () {
                    _socket.close();
                },

                send: function (data) {
                    _socket.push(data);
                },

                onmessage: function (e) {
                },

                onopen: function (e) {
                },

                onclose: function (e) {
                },

                onerror: function (e) {

                }
            };
            _socket = new atmosphere.subscribe(request);

            return _adapter;
        },

        AtmosphereRequest: function (options) {

            /**
             * {Object} Request parameters.
             *
             * @private
             */
            var _request = {
                timeout: 300000,
                method: 'GET',
                headers: {},
                contentType: '',
                callback: null,
                url: '',
                data: '',
                suspend: true,
                maxRequest: -1,
                reconnect: true,
                maxStreamingLength: 10000000,
                lastIndex: 0,
                logLevel: 'info',
                requestCount: 0,
                fallbackMethod: 'GET',
                fallbackTransport: 'streaming',
                transport: 'long-polling',
                webSocketImpl: null,
                webSocketBinaryType: null,
                dispatchUrl: null,
                webSocketPathDelimiter: "@@",
                enableXDR: false,
                rewriteURL: false,
                attachHeadersAsQueryString: true,
                executeCallbackBeforeReconnect: false,
                readyState: 0,
                withCredentials: false,
                trackMessageLength: false,
                messageDelimiter: '|',
                connectTimeout: -1,
                reconnectInterval: 0,
                dropHeaders: true,
                uuid: 0,
                async: true,
                shared: false,
                readResponsesHeaders: false,
                maxReconnectOnClose: 5,
                enableProtocol: true,
                disableDisconnect: false,
                pollingInterval: 0,
                heartbeat: {
                    client: null,
                    server: null
                },
                ackInterval: 0,
                closeAsync: false,
                reconnectOnServerError: true,
                handleOnlineOffline: true,
                onError: function (response) {
                },
                onClose: function (response) {
                },
                onOpen: function (response) {
                },
                onMessage: function (response) {
                },
                onReopen: function (request, response) {
                },
                onReconnect: function (request, response) {
                },
                onMessagePublished: function (response) {
                },
                onTransportFailure: function (reason, request) {
                },
                onLocalMessage: function (request) {
                },
                onFailureToReconnect: function (request, response) {
                },
                onClientTimeout: function (request) {
                },
                onOpenAfterResume: function (request) {
                }
            };

            /**
             * {Object} Request's last response.
             *
             * @private
             */
            var _response = {
                status: 200,
                reasonPhrase: "OK",
                responseBody: '',
                messages: [],
                headers: [],
                state: "messageReceived",
                transport: "polling",
                error: null,
                request: null,
                partialMessage: "",
                errorHandled: false,
                closedByClientTimeout: false,
                ffTryingReconnect: false
            };

            /**
             * {websocket} Opened web socket.
             *
             * @private
             */
            var _websocket = null;

            /**
             * {SSE} Opened SSE.
             *
             * @private
             */
            var _sse = null;

            /**
             * {XMLHttpRequest, ActiveXObject} Opened ajax request (in case of http-streaming or long-polling)
             *
             * @private
             */
            var _activeRequest = null;

            /**
             * {Object} Object use for streaming with IE.
             *
             * @private
             */
            var _ieStream = null;

            /**
             * {Object} Object use for jsonp transport.
             *
             * @private
             */
            var _jqxhr = null;

            /**
             * {boolean} If request has been subscribed or not.
             *
             * @private
             */
            var _subscribed = true;

            /**
             * {number} Number of test reconnection.
             *
             * @private
             */
            var _requestCount = 0;

            /**
             * The Heartbeat interval send by the server.
             * @type {int}
             * @private
             */
            var _heartbeatInterval = 0;

            /**
             * The Heartbeat bytes send by the server.
             * @type {string}
             * @private
             */
            var _heartbeatPadding = 'X';

            /**
             * {boolean} If request is currently aborted.
             *
             * @private
             */
            var _abortingConnection = false;

            /**
             * A local "channel' of communication.
             *
             * @private
             */
            var _localSocketF = null;

            /**
             * The storage used.
             *
             * @private
             */
            var _storageService;

            /**
             * Local communication
             *
             * @private
             */
            var _localStorageService = null;

            /**
             * A Unique ID
             *
             * @private
             */
            var guid = atmosphere.util.now();

            /** Trace time */
            var _traceTimer;

            /** Key for connection sharing */
            var _sharingKey;

            /**
             * {boolean} If window beforeUnload event has been called.
             * Flag will be reset after 5000 ms
             *
             * @private
             */
            var _beforeUnloadState = false;

            // Automatic call to subscribe
            _subscribe(options);

            /**
             * Initialize atmosphere request object.
             *
             * @private
             */
            function _init() {
                _subscribed = true;
                _abortingConnection = false;
                _requestCount = 0;

                _websocket = null;
                _sse = null;
                _activeRequest = null;
                _ieStream = null;
            }

            /**
             * Re-initialize atmosphere object.
             *
             * @private
             */
            function _reinit() {
                _clearState();
                _init();
            }

            /**
             * Returns true if the given level is equal or above the configured log level.
             *
             * @private
             */
            function _canLog(level) {
                if (level == 'debug') {
                    return _request.logLevel === 'debug';
                } else if (level == 'info') {
                    return _request.logLevel === 'info' || _request.logLevel === 'debug';
                } else if (level == 'warn') {
                    return _request.logLevel === 'warn' || _request.logLevel === 'info' || _request.logLevel === 'debug';
                } else if (level == 'error') {
                    return _request.logLevel === 'error' || _request.logLevel === 'warn' || _request.logLevel === 'info' || _request.logLevel === 'debug';
                } else {
                    return false;
                }
            }

            function _debug(msg) {
                if (_canLog('debug')) {
                    atmosphere.util.debug(new Date() + " Atmosphere: " + msg);
                }
            }

            /**
             *
             * @private
             */
            function _verifyStreamingLength(ajaxRequest, rq) {
                // Wait to be sure we have the full message before closing.
                if (_response.partialMessage === "" && (rq.transport === 'streaming') && (ajaxRequest.responseText.length > rq.maxStreamingLength)) {
                    return true;
                }
                return false;
            }

            /**
             * Disconnect
             *
             * @private
             */
            function _disconnect() {
                if (_request.enableProtocol && !_request.disableDisconnect && !_request.firstMessage) {
                    var query = "X-Atmosphere-Transport=close&X-Atmosphere-tracking-id=" + _request.uuid;

                    atmosphere.util.each(_request.headers, function (name, value) {
                        var h = atmosphere.util.isFunction(value) ? value.call(this, _request, _request, _response) : value;
                        if (h != null) {
                            query += "&" + encodeURIComponent(name) + "=" + encodeURIComponent(h);
                        }
                    });

                    var url = _request.url.replace(/([?&])_=[^&]*/, query);
                    url = url + (url === _request.url ? (/\?/.test(_request.url) ? "&" : "?") + query : "");

                    var rq = {
                        connected: false
                    };
                    var closeR = new atmosphere.AtmosphereRequest(rq);
                    closeR.connectTimeout = _request.connectTimeout;
                    closeR.attachHeadersAsQueryString = false;
                    closeR.dropHeaders = true;
                    closeR.url = url;
                    closeR.contentType = "text/plain";
                    closeR.transport = 'polling';
                    closeR.method = 'GET';
                    closeR.data = '';
                    closeR.heartbeat = null;
                    if (_request.enableXDR) {
                        closeR.enableXDR = _request.enableXDR
                    }
                    closeR.async = _request.closeAsync;
                    _pushOnClose("", closeR);
                }
            }

            /**
             * Close request.
             *
             * @private
             */
            function _close() {
                _debug("Closing (AtmosphereRequest._close() called)");

                _abortingConnection = true;
                if (_request.reconnectId) {
                    clearTimeout(_request.reconnectId);
                    delete _request.reconnectId;
                }

                if (_request.heartbeatTimer) {
                    clearTimeout(_request.heartbeatTimer);
                }

                _request.reconnect = false;
                _response.request = _request;
                _response.state = 'unsubscribe';
                _response.responseBody = "";
                _response.status = 408;
                _response.partialMessage = "";
                _invokeCallback();
                _disconnect();
                _clearState();
            }

            function _clearState() {
                _response.partialMessage = "";
                if (_request.id) {
                    clearTimeout(_request.id);
                }

                if (_request.heartbeatTimer) {
                    clearTimeout(_request.heartbeatTimer);
                }

                // https://github.com/Atmosphere/atmosphere/issues/1860#issuecomment-74707226
                if(_request.reconnectId) {
                    clearTimeout(_request.reconnectId);
                    delete _request.reconnectId;
                }

                if (_ieStream != null) {
                    _ieStream.close();
                    _ieStream = null;
                }
                if (_jqxhr != null) {
                    _jqxhr.abort();
                    _jqxhr = null;
                }
                if (_activeRequest != null) {
                    _activeRequest.abort();
                    _activeRequest = null;
                }
                if (_websocket != null) {
                    if (_websocket.canSendMessage) {
                        _debug("invoking .close() on WebSocket object");
                        _websocket.close();
                    }
                    _websocket = null;
                }
                if (_sse != null) {
                    _sse.close();
                    _sse = null;
                }
                _clearStorage();
            }

            function _clearStorage() {
                // Stop sharing a connection
                if (_storageService != null) {
                    // Clears trace timer
                    clearInterval(_traceTimer);
                    // Removes the trace
                    document.cookie = _sharingKey + "=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/";
                    // The heir is the parent unless unloading
                    _storageService.signal("close", {
                        reason: "",
                        heir: !_abortingConnection ? guid : (_storageService.get("children") || [])[0]
                    });
                    _storageService.close();
                }
                if (_localStorageService != null) {
                    _localStorageService.close();
                }
            }

            /**
             * Subscribe request using request transport. <br>
             * If request is currently opened, this one will be closed.
             *
             * @param {Object} Request parameters.
             * @private
             */
            function _subscribe(options) {
                _reinit();

                _request = atmosphere.util.extend(_request, options);
                // Allow at least 1 request
                _request.mrequest = _request.reconnect;
                if (!_request.reconnect) {
                    _request.reconnect = true;
                }
            }

            /**
             * Check if web socket is supported (check for custom implementation provided by request object or browser implementation).
             *
             * @returns {boolean} True if web socket is supported, false otherwise.
             * @private
             */
            function _supportWebsocket() {
                return _request.webSocketImpl != null || window.WebSocket || window.MozWebSocket;
            }

            /**
             * Check if server side events (SSE) is supported (check for custom implementation provided by request object or browser implementation).
             *
             * @returns {boolean} True if web socket is supported, false otherwise.
             * @private
             */
            function _supportSSE() {
                // Origin parts
                var url = atmosphere.util.getAbsoluteURL(_request.url.toLowerCase());
                var parts = /^([\w\+\.\-]+:)(?:\/\/([^\/?#:]*)(?::(\d+))?)?/.exec(url);
                var crossOrigin = !!(parts && (
                    // protocol
                    parts[1] != window.location.protocol ||
                    // hostname
                    parts[2] != window.location.hostname ||
                    // port
                    (parts[3] || (parts[1] === "http:" ? 80 : 443)) != (window.location.port || (window.location.protocol === "http:" ? 80 : 443))
                ));
                return window.EventSource && (!crossOrigin || !atmosphere.util.browser.safari || atmosphere.util.browser.vmajor >= 7);
            }

            /**
             * Open request using request transport. <br>
             * If request transport is 'websocket' but websocket can't be opened, request will automatically reconnect using fallback transport.
             *
             * @private
             */
            function _execute() {
                // Shared across multiple tabs/windows.
                if (_request.shared) {
                    _localStorageService = _local(_request);
                    if (_localStorageService != null) {
                        if (_canLog('debug')) {
                            atmosphere.util.debug("Storage service available. All communication will be local");
                        }

                        if (_localStorageService.open(_request)) {
                            // Local connection.
                            return;
                        }
                    }

                    if (_canLog('debug')) {
                        atmosphere.util.debug("No Storage service available.");
                    }
                    // Wasn't local or an error occurred
                    _localStorageService = null;
                }

                // Protocol
                _request.firstMessage = uuid == 0 ? true : false;
                _request.isOpen = false;
                _request.ctime = atmosphere.util.now();

                // We carry any UUID set by the user or from a previous connection.
                if (_request.uuid === 0) {
                    _request.uuid = uuid;
                }
                _response.closedByClientTimeout = false;

                if (_request.transport !== 'websocket' && _request.transport !== 'sse') {
                    _executeRequest(_request);

                } else if (_request.transport === 'websocket') {
                    if (!_supportWebsocket()) {
                        _reconnectWithFallbackTransport("Websocket is not supported, using request.fallbackTransport (" + _request.fallbackTransport
                            + ")");
                    } else {
                        _executeWebSocket(false);
                    }
                } else if (_request.transport === 'sse') {
                    if (!_supportSSE()) {
                        _reconnectWithFallbackTransport("Server Side Events(SSE) is not supported, using request.fallbackTransport ("
                            + _request.fallbackTransport + ")");
                    } else {
                        _executeSSE(false);
                    }
                }
            }

            function _local(request) {
                var trace, connector, orphan, name = "atmosphere-" + request.url, connectors = {
                    storage: function () {
                        function onstorage(event) {
                            if (event.key === name && event.newValue) {
                                listener(event.newValue);
                            }
                        }

                        if (!atmosphere.util.storage) {
                            return;
                        }

                        var storage = window.localStorage,
                            get = function (key) {
                                return atmosphere.util.parseJSON(storage.getItem(name + "-" + key));
                            },
                            set = function (key, value) {
                                storage.setItem(name + "-" + key, atmosphere.util.stringifyJSON(value));
                            };

                        return {
                            init: function () {
                                set("children", get("children").concat([guid]));
                                atmosphere.util.on(window, "storage", onstorage);
                                return get("opened");
                            },
                            signal: function (type, data) {
                                storage.setItem(name, atmosphere.util.stringifyJSON({
                                    target: "p",
                                    type: type,
                                    data: data
                                }));
                            },
                            close: function () {
                                var children = get("children");

                                atmosphere.util.off(window, "storage", onstorage);
                                if (children) {
                                    if (removeFromArray(children, request.id)) {
                                        set("children", children);
                                    }
                                }
                            }
                        };
                    },
                    windowref: function () {
                        var win = window.open("", name.replace(/\W/g, ""));

                        if (!win || win.closed || !win.callbacks) {
                            return;
                        }

                        return {
                            init: function () {
                                win.callbacks.push(listener);
                                win.children.push(guid);
                                return win.opened;
                            },
                            signal: function (type, data) {
                                if (!win.closed && win.fire) {
                                    win.fire(atmosphere.util.stringifyJSON({
                                        target: "p",
                                        type: type,
                                        data: data
                                    }));
                                }
                            },
                            close: function () {
                                // Removes traces only if the parent is alive
                                if (!orphan) {
                                    removeFromArray(win.callbacks, listener);
                                    removeFromArray(win.children, guid);
                                }
                            }

                        };
                    }
                };

                function removeFromArray(array, val) {
                    var i, length = array.length;

                    for (i = 0; i < length; i++) {
                        if (array[i] === val) {
                            array.splice(i, 1);
                        }
                    }

                    return length !== array.length;
                }

                // Receives open, close and message command from the parent
                function listener(string) {
                    var command = atmosphere.util.parseJSON(string), data = command.data;

                    if (command.target === "c") {
                        switch (command.type) {
                            case "open":
                                _open("opening", 'local', _request);
                                break;
                            case "close":
                                if (!orphan) {
                                    orphan = true;
                                    if (data.reason === "aborted") {
                                        _close();
                                    } else {
                                        // Gives the heir some time to reconnect
                                        if (data.heir === guid) {
                                            _execute();
                                        } else {
                                            setTimeout(function () {
                                                _execute();
                                            }, 100);
                                        }
                                    }
                                }
                                break;
                            case "message":
                                _prepareCallback(data, "messageReceived", 200, request.transport);
                                break;
                            case "localMessage":
                                _localMessage(data);
                                break;
                        }
                    }
                }

                function findTrace() {
                    var matcher = new RegExp("(?:^|; )(" + encodeURIComponent(name) + ")=([^;]*)").exec(document.cookie);
                    if (matcher) {
                        return atmosphere.util.parseJSON(decodeURIComponent(matcher[2]));
                    }
                }

                // Finds and validates the parent socket's trace from the cookie
                trace = findTrace();
                if (!trace || atmosphere.util.now() - trace.ts > 1000) {
                    return;
                }

                // Chooses a connector
                connector = connectors.storage() || connectors.windowref();
                if (!connector) {
                    return;
                }

                return {
                    open: function () {
                        var parentOpened;

                        // Checks the shared one is alive
                        _traceTimer = setInterval(function () {
                            var oldTrace = trace;
                            trace = findTrace();
                            if (!trace || oldTrace.ts === trace.ts) {
                                // Simulates a close signal
                                listener(atmosphere.util.stringifyJSON({
                                    target: "c",
                                    type: "close",
                                    data: {
                                        reason: "error",
                                        heir: oldTrace.heir
                                    }
                                }));
                            }
                        }, 1000);

                        parentOpened = connector.init();
                        if (parentOpened) {
                            // Firing the open event without delay robs the user of the opportunity to bind connecting event handlers
                            setTimeout(function () {
                                _open("opening", 'local', request);
                            }, 50);
                        }
                        return parentOpened;
                    },
                    send: function (event) {
                        connector.signal("send", event);
                    },
                    localSend: function (event) {
                        connector.signal("localSend", atmosphere.util.stringifyJSON({
                            id: guid,
                            event: event
                        }));
                    },
                    close: function () {
                        // Do not signal the parent if this method is executed by the unload event handler
                        if (!_abortingConnection) {
                            clearInterval(_traceTimer);
                            connector.signal("close");
                            connector.close();
                        }
                    }
                };
            }

            function share() {
                var storageService, name = "atmosphere-" + _request.url, servers = {
                    // Powered by the storage event and the localStorage
                    // http://www.w3.org/TR/webstorage/#event-storage
                    storage: function () {
                        function onstorage(event) {
                            // When a deletion, newValue initialized to null
                            if (event.key === name && event.newValue) {
                                listener(event.newValue);
                            }
                        }

                        if (!atmosphere.util.storage) {
                            return;
                        }

                        var storage = window.localStorage;

                        return {
                            init: function () {
                                // Handles the storage event
                                atmosphere.util.on(window, "storage", onstorage);
                            },
                            signal: function (type, data) {
                                storage.setItem(name, atmosphere.util.stringifyJSON({
                                    target: "c",
                                    type: type,
                                    data: data
                                }));
                            },
                            get: function (key) {
                                return atmosphere.util.parseJSON(storage.getItem(name + "-" + key));
                            },
                            set: function (key, value) {
                                storage.setItem(name + "-" + key, atmosphere.util.stringifyJSON(value));
                            },
                            close: function () {
                                atmosphere.util.off(window, "storage", onstorage);
                                storage.removeItem(name);
                                storage.removeItem(name + "-opened");
                                storage.removeItem(name + "-children");
                            }

                        };
                    },
                    // Powered by the window.open method
                    // https://developer.mozilla.org/en/DOM/window.open
                    windowref: function () {
                        // Internet Explorer raises an invalid argument error
                        // when calling the window.open method with the name containing non-word characters
                        var neim = name.replace(/\W/g, ""), container = document.getElementById(neim), win;

                        if (!container) {
                            container = document.createElement("div");
                            container.id = neim;
                            container.style.display = "none";
                            container.innerHTML = '<iframe name="' + neim + '" />';
                            document.body.appendChild(container);
                        }

                        win = container.firstChild.contentWindow;

                        return {
                            init: function () {
                                // Callbacks from different windows
                                win.callbacks = [listener];
                                // In IE 8 and less, only string argument can be safely passed to the function in other window
                                win.fire = function (string) {
                                    var i;

                                    for (i = 0; i < win.callbacks.length; i++) {
                                        win.callbacks[i](string);
                                    }
                                };
                            },
                            signal: function (type, data) {
                                if (!win.closed && win.fire) {
                                    win.fire(atmosphere.util.stringifyJSON({
                                        target: "c",
                                        type: type,
                                        data: data
                                    }));
                                }
                            },
                            get: function (key) {
                                return !win.closed ? win[key] : null;
                            },
                            set: function (key, value) {
                                if (!win.closed) {
                                    win[key] = value;
                                }
                            },
                            close: function () {
                            }
                        };
                    }
                };

                // Receives send and close command from the children
                function listener(string) {
                    var command = atmosphere.util.parseJSON(string), data = command.data;

                    if (command.target === "p") {
                        switch (command.type) {
                            case "send":
                                _push(data);
                                break;
                            case "localSend":
                                _localMessage(data);
                                break;
                            case "close":
                                _close();
                                break;
                        }
                    }
                }

                _localSocketF = function propagateMessageEvent(context) {
                    storageService.signal("message", context);
                };

                function leaveTrace() {
                    document.cookie = _sharingKey + "=" +
                        // Opera's JSON implementation ignores a number whose a last digit of 0 strangely
                        // but has no problem with a number whose a last digit of 9 + 1
                        encodeURIComponent(atmosphere.util.stringifyJSON({
                            ts: atmosphere.util.now() + 1,
                            heir: (storageService.get("children") || [])[0]
                        })) + "; path=/";
                }

                // Chooses a storageService
                storageService = servers.storage() || servers.windowref();
                storageService.init();

                if (_canLog('debug')) {
                    atmosphere.util.debug("Installed StorageService " + storageService);
                }

                // List of children sockets
                storageService.set("children", []);

                if (storageService.get("opened") != null && !storageService.get("opened")) {
                    // Flag indicating the parent socket is opened
                    storageService.set("opened", false);
                }
                // Leaves traces
                _sharingKey = encodeURIComponent(name);
                leaveTrace();
                _traceTimer = setInterval(leaveTrace, 1000);

                _storageService = storageService;
            }

            /**
             * @private
             */
            function _open(state, transport, request) {
                if (_request.shared && transport !== 'local') {
                    share();
                }

                if (_storageService != null) {
                    _storageService.set("opened", true);
                }

                request.close = function () {
                    _close();
                };

                if (_requestCount > 0 && state === 're-connecting') {
                    request.isReopen = true;
                    _tryingToReconnect(_response);
                } else if (_response.error == null) {
                    _response.request = request;
                    var prevState = _response.state;
                    _response.state = state;
                    var prevTransport = _response.transport;
                    _response.transport = transport;

                    var _body = _response.responseBody;
                    _invokeCallback();
                    _response.responseBody = _body;

                    _response.state = prevState;
                    _response.transport = prevTransport;
                }
            }

            /**
             * Execute request using jsonp transport.
             *
             * @param request {Object} request Request parameters, if undefined _request object will be used.
             * @private
             */
            function _jsonp(request) {
                // When CORS is enabled, make sure we force the proper transport.
                request.transport = "jsonp";

                var rq = _request, script;
                if ((request != null) && (typeof (request) !== 'undefined')) {
                    rq = request;
                }

                _jqxhr = {
                    open: function () {
                        var callback = "atmosphere" + (++guid);

                        function _reconnectOnFailure() {
                            rq.lastIndex = 0;

                            if (rq.openId) {
                                clearTimeout(rq.openId);
                            }

                            if (rq.heartbeatTimer) {
                                clearTimeout(rq.heartbeatTimer);
                            }

                            if (rq.reconnect && _requestCount++ < rq.maxReconnectOnClose) {
                                _open('re-connecting', rq.transport, rq);
                                _reconnect(_jqxhr, rq, request.reconnectInterval);
                                rq.openId = setTimeout(function () {
                                    _triggerOpen(rq);
                                }, rq.reconnectInterval + 1000);
                            } else {
                                _onError(0, "maxReconnectOnClose reached");
                            }
                        }

                        function poll() {
                            var url = rq.url;
                            if (rq.dispatchUrl != null) {
                                url += rq.dispatchUrl;
                            }

                            var data = rq.data;
                            if (rq.attachHeadersAsQueryString) {
                                url = _attachHeaders(rq);
                                if (data !== '') {
                                    url += "&X-Atmosphere-Post-Body=" + encodeURIComponent(data);
                                }
                                data = '';
                            }

                            var head = document.head || document.getElementsByTagName("head")[0] || document.documentElement;

                            script = document.createElement("script");
                            script.src = url + "&jsonpTransport=" + callback;
                            //script.async = rq.async;
                            script.clean = function () {
                                script.clean = script.onerror = script.onload = script.onreadystatechange = null;
                                if (script.parentNode) {
                                    script.parentNode.removeChild(script);
                                }

                                if (++request.scriptCount === 2) {
                                    request.scriptCount = 1;
                                    _reconnectOnFailure();
                                }

                            };
                            script.onload = script.onreadystatechange = function () {
                                _debug("jsonp.onload");
                                if (!script.readyState || /loaded|complete/.test(script.readyState)) {
                                    script.clean();
                                }
                            };

                            script.onerror = function () {
                                _debug("jsonp.onerror");
                                request.scriptCount = 1;
                                script.clean();
                            };

                            head.insertBefore(script, head.firstChild);
                        }

                        // Attaches callback
                        window[callback] = function (msg) {
                            _debug("jsonp.window");
                            request.scriptCount = 0;
                            if (rq.reconnect && rq.maxRequest === -1 || rq.requestCount++ < rq.maxRequest) {

                                // _readHeaders(_jqxhr, rq);
                                if (!rq.executeCallbackBeforeReconnect) {
                                    _reconnect(_jqxhr, rq, rq.pollingInterval);
                                }

                                if (msg != null && typeof msg !== 'string') {
                                    try {
                                        msg = msg.message;
                                    } catch (err) {
                                        // The message was partial
                                    }
                                }
                                var skipCallbackInvocation = _trackMessageSize(msg, rq, _response);
                                if (!skipCallbackInvocation) {
                                    _prepareCallback(_response.responseBody, "messageReceived", 200, rq.transport);
                                }

                                if (rq.executeCallbackBeforeReconnect) {
                                    _reconnect(_jqxhr, rq, rq.pollingInterval);
                                }
                                _timeout(rq);
                            } else {
                                atmosphere.util.log(_request.logLevel, ["JSONP reconnect maximum try reached " + _request.requestCount]);
                                _onError(0, "maxRequest reached");
                            }
                        };
                        setTimeout(function () {
                            poll();
                        }, 50);
                    },
                    abort: function () {
                        if (script && script.clean) {
                            script.clean();
                        }
                    }
                };
                _jqxhr.open();
            }

            /**
             * Build websocket object.
             *
             * @param location {string} Web socket url.
             * @returns {websocket} Web socket object.
             * @private
             */
            function _getWebSocket(location) {
                if (_request.webSocketImpl != null) {
                    return _request.webSocketImpl;
                } else {
                    if (window.WebSocket) {
                        return new WebSocket(location);
                    } else {
                        return new MozWebSocket(location);
                    }
                }
            }

            /**
             * Build web socket url from request url.
             *
             * @return {string} Web socket url (start with "ws" or "wss" for secure web socket).
             * @private
             */
            function _buildWebSocketUrl() {
                return _attachHeaders(_request, atmosphere.util.getAbsoluteURL(_request.webSocketUrl || _request.url)).replace(/^http/, "ws");
            }

            /**
             * Build SSE url from request url.
             *
             * @return a url with Atmosphere's headers
             * @private
             */
            function _buildSSEUrl() {
                var url = _attachHeaders(_request);
                return url;
            }

            /**
             * Open SSE. <br>
             * Automatically use fallback transport if SSE can't be opened.
             *
             * @private
             */
            function _executeSSE(sseOpened) {

                _response.transport = "sse";

                var location = _buildSSEUrl();

                if (_canLog('debug')) {
                    atmosphere.util.debug("Invoking executeSSE");
                    atmosphere.util.debug("Using URL: " + location);
                }

                if (sseOpened && !_request.reconnect) {
                    if (_sse != null) {
                        _clearState();
                    }
                    return;
                }

                try {
                    _sse = new EventSource(location, {
                        withCredentials: _request.withCredentials
                    });
                } catch (e) {
                    _onError(0, e);
                    _reconnectWithFallbackTransport("SSE failed. Downgrading to fallback transport and resending");
                    return;
                }

                if (_request.connectTimeout > 0) {
                    _request.id = setTimeout(function () {
                        if (!sseOpened) {
                            _clearState();
                        }
                    }, _request.connectTimeout);
                }

                _sse.onopen = function (event) {
                    _debug("sse.onopen");
                    _timeout(_request);
                    if (_canLog('debug')) {
                        atmosphere.util.debug("SSE successfully opened");
                    }

                    if (!_request.enableProtocol) {
                        if (!sseOpened) {
                            _open('opening', "sse", _request);
                        } else {
                            _open('re-opening', "sse", _request);
                        }
                    } else if (_request.isReopen) {
                        _request.isReopen = false;
                        _open('re-opening', _request.transport, _request);
                    }

                    sseOpened = true;

                    if (_request.method === 'POST') {
                        _response.state = "messageReceived";
                        _sse.send(_request.data);
                    }
                };

                _sse.onmessage = function (message) {
                    _debug("sse.onmessage");
                    _timeout(_request);

                    if (!_request.enableXDR && window.location.host && message.origin && message.origin !== window.location.protocol + "//" + window.location.host) {
                        atmosphere.util.log(_request.logLevel, ["Origin was not " + window.location.protocol + "//" + window.location.host]);
                        return;
                    }

                    _response.state = 'messageReceived';
                    _response.status = 200;

                    message = message.data;
                    var skipCallbackInvocation = _trackMessageSize(message, _request, _response);

                    // https://github.com/remy/polyfills/blob/master/EventSource.js
                    // Since we polling.
                    /* if (_sse.URL) {
                     _sse.interval = 100;
                     _sse.URL = _buildSSEUrl();
                     } */

                    if (!skipCallbackInvocation) {
                        _invokeCallback();
                        _response.responseBody = '';
                        _response.messages = [];
                    }
                };

                _sse.onerror = function (message) {
                    _debug("sse.onerror");
                    clearTimeout(_request.id);

                    if (_request.heartbeatTimer) {
                        clearTimeout(_request.heartbeatTimer);
                    }

                    if (_response.closedByClientTimeout) {
                        return;
                    }

                    _invokeClose(sseOpened);
                    _clearState();

                    if (_abortingConnection) {
                        atmosphere.util.log(_request.logLevel, ["SSE closed normally"]);
                    } else if (!sseOpened) {
                        _reconnectWithFallbackTransport("SSE failed. Downgrading to fallback transport and resending");
                    } else if (_request.reconnect && (_response.transport === 'sse')) {
                        if (_requestCount++ < _request.maxReconnectOnClose) {
                            _open('re-connecting', _request.transport, _request);
                            if (_request.reconnectInterval > 0) {
                                _request.reconnectId = setTimeout(function () {
                                    _executeSSE(true);
                                }, _request.reconnectInterval);
                            } else {
                                _executeSSE(true);
                            }
                            _response.responseBody = "";
                            _response.messages = [];
                        } else {
                            atmosphere.util.log(_request.logLevel, ["SSE reconnect maximum try reached " + _requestCount]);
                            _onError(0, "maxReconnectOnClose reached");
                        }
                    }
                };
            }

            /**
             * Open web socket. <br>
             * Automatically use fallback transport if web socket can't be opened.
             *
             * @private
             */
            function _executeWebSocket(webSocketOpened) {

                _response.transport = "websocket";

                var location = _buildWebSocketUrl(_request.url);
                if (_canLog('debug')) {
                    atmosphere.util.debug("Invoking executeWebSocket, using URL: " + location);
                }

                if (webSocketOpened && !_request.reconnect) {
                    if (_websocket != null) {
                        _clearState();
                    }
                    return;
                }

                _websocket = _getWebSocket(location);
                if (_request.webSocketBinaryType != null) {
                    _websocket.binaryType = _request.webSocketBinaryType;
                }

                if (_request.connectTimeout > 0) {
                    _request.id = setTimeout(function () {
                        if (!webSocketOpened) {
                            var _message = {
                                code: 1002,
                                reason: "",
                                wasClean: false
                            };
                            _websocket.onclose(_message);
                            // Close it anyway
                            try {
                                _clearState();
                            } catch (e) {
                            }
                            return;
                        }

                    }, _request.connectTimeout);
                }

                _websocket.onopen = function (message) {
                    _debug("websocket.onopen");
                    _timeout(_request);
                    offline = false;

                    if (_canLog('debug')) {
                        atmosphere.util.debug("Websocket successfully opened");
                    }

                    var reopening = webSocketOpened;

                    if (_websocket != null) {
                        _websocket.canSendMessage = true;
                    }

                    if (!_request.enableProtocol) {
                        webSocketOpened = true;
                        if (reopening) {
                            _open('re-opening', "websocket", _request);
                        } else {
                            _open('opening', "websocket", _request);
                        }
                    }

                    if (_websocket != null) {
                        if (_request.method === 'POST') {
                            _response.state = "messageReceived";
                            _websocket.send(_request.data);
                        }
                    }
                };

                _websocket.onmessage = function (message) {
                    _debug("websocket.onmessage");
                    _timeout(_request);

                    // We only consider it opened if we get the handshake data
                    // https://github.com/Atmosphere/atmosphere-javascript/issues/74
                    if (_request.enableProtocol) {
                        webSocketOpened = true;
                    }

                    _response.state = 'messageReceived';
                    _response.status = 200;

                    message = message.data;
                    var isString = typeof (message) === 'string';
                    if (isString) {
                        var skipCallbackInvocation = _trackMessageSize(message, _request, _response);
                        if (!skipCallbackInvocation) {
                            _invokeCallback();
                            _response.responseBody = '';
                            _response.messages = [];
                        }
                    } else {
                        message = _handleProtocol(_request, message);
                        if (message === "")
                            return;

                        _response.responseBody = message;
                        _invokeCallback();
                        _response.responseBody = null;
                    }
                };

                _websocket.onerror = function (message) {
                    _debug("websocket.onerror");
                    clearTimeout(_request.id);

                    if (_request.heartbeatTimer) {
                        clearTimeout(_request.heartbeatTimer);
                    }
                };

                _websocket.onclose = function (message) {
                    _debug("websocket.onclose");
                    clearTimeout(_request.id);
                    if (_response.state === 'closed')
                        return;

                    var reason = message.reason;
                    if (reason === "") {
                        switch (message.code) {
                            case 1000:
                                reason = "Normal closure; the connection successfully completed whatever purpose for which it was created.";
                                break;
                            case 1001:
                                reason = "The endpoint is going away, either because of a server failure or because the "
                                    + "browser is navigating away from the page that opened the connection.";
                                break;
                            case 1002:
                                reason = "The endpoint is terminating the connection due to a protocol error.";
                                break;
                            case 1003:
                                reason = "The connection is being terminated because the endpoint received data of a type it "
                                    + "cannot accept (for example, a text-only endpoint received binary data).";
                                break;
                            case 1004:
                                reason = "The endpoint is terminating the connection because a data frame was received that is too large.";
                                break;
                            case 1005:
                                reason = "Unknown: no status code was provided even though one was expected.";
                                break;
                            case 1006:
                                reason = "Connection was closed abnormally (that is, with no close frame being sent).";
                                break;
                        }
                    }

                    if (_canLog('warn')) {
                        atmosphere.util.warn("Websocket closed, reason: " + reason + ' - wasClean: ' + message.wasClean);
                    }

                    if (_response.closedByClientTimeout || (_request.handleOnlineOffline && offline)) {
                        // IFF online/offline events are handled and we happen to be offline, we stop all reconnect attempts and
                        // resume them in the "online" event (if we get here in that case, something else went wrong as the
                        // offline handler should stop any reconnect attempt).
                        //
                        // On the other hand, if we DO NOT handle online/offline events, we continue as before with reconnecting
                        // even if we are offline. Failing to do so would stop all reconnect attemps forever.
                        if (_request.reconnectId) {
                            clearTimeout(_request.reconnectId);
                            delete _request.reconnectId;
                        }
                        return;
                    }

                    _invokeClose(webSocketOpened);

                    _response.state = 'closed';

                    if (_abortingConnection) {
                        atmosphere.util.log(_request.logLevel, ["Websocket closed normally"]);
                    } else if (!webSocketOpened) {
                        _reconnectWithFallbackTransport("Websocket failed on first connection attempt. Downgrading to " + _request.fallbackTransport + " and resending");

                    } else if (_request.reconnect && _response.transport === 'websocket' ) {
                        _clearState();
                        if (_requestCount++ < _request.maxReconnectOnClose) {
                            _open('re-connecting', _request.transport, _request);
                            if (_request.reconnectInterval > 0) {
                                _request.reconnectId = setTimeout(function () {
                                    _response.responseBody = "";
                                    _response.messages = [];
                                    _executeWebSocket(true);
                                }, _request.reconnectInterval);
                            } else {
                                _response.responseBody = "";
                                _response.messages = [];
                                _executeWebSocket(true);
                            }
                        } else {
                            atmosphere.util.log(_request.logLevel, ["Websocket reconnect maximum try reached " + _requestCount]);
                            if (_canLog('warn')) {
                                atmosphere.util.warn("Websocket error, reason: " + message.reason);
                            }
                            _onError(0, "maxReconnectOnClose reached");
                        }
                    }
                };

                var ua = navigator.userAgent.toLowerCase();
                var isAndroid = ua.indexOf("android") > -1;
                if (isAndroid && _websocket.url === undefined) {
                    // Android 4.1 does not really support websockets and fails silently
                    _websocket.onclose({
                        reason: "Android 4.1 does not support websockets.",
                        wasClean: false
                    });
                }
            }

            function _handleProtocol(request, message) {

                var nMessage = message;
                if (request.transport === 'polling') return nMessage;

                if (request.enableProtocol && request.firstMessage && atmosphere.util.trim(message).length !== 0) {
                    var pos = request.trackMessageLength ? 1 : 0;
                    var messages = message.split(request.messageDelimiter);

                    if (messages.length <= pos + 1) {
                        // Something went wrong, normally with IE or when a message is written before the
                        // handshake has been received.
                        return nMessage;
                    }

                    request.firstMessage = false;
                    request.uuid = atmosphere.util.trim(messages[pos]);

                    if (messages.length <= pos + 2) {
                        atmosphere.util.log('error', ["Protocol data not sent by the server. " +
                        "If you enable protocol on client side, be sure to install JavascriptProtocol interceptor on server side." +
                        "Also note that atmosphere-runtime 2.2+ should be used."]);
                    }

                    _heartbeatInterval = parseInt(atmosphere.util.trim(messages[pos + 1]), 10);
                    _heartbeatPadding = messages[pos + 2];

                    if (request.transport !== 'long-polling') {
                        _triggerOpen(request);
                    }
                    uuid = request.uuid;
                    nMessage = "";

                    // We have trailing messages
                    pos = request.trackMessageLength ? 4 : 3;
                    if (messages.length > pos + 1) {
                        for (var i = pos; i < messages.length; i++) {
                            nMessage += messages[i];
                            if (i + 1 !== messages.length) {
                                nMessage += request.messageDelimiter;
                            }
                        }
                    }

                    if (request.ackInterval !== 0) {
                        setTimeout(function () {
                            _push("...ACK...");
                        }, request.ackInterval);
                    }
                } else if (request.enableProtocol && request.firstMessage && atmosphere.util.browser.msie && +atmosphere.util.browser.version.split(".")[0] < 10) {
                    // In case we are getting some junk from IE
                    atmosphere.util.log(_request.logLevel, ["Receiving unexpected data from IE"]);
                } else {
                    _triggerOpen(request);
                }
                return nMessage;
            }

            function _timeout(_request) {
                clearTimeout(_request.id);
                if (_request.timeout > 0 && _request.transport !== 'polling') {
                    _request.id = setTimeout(function () {
                        _onClientTimeout(_request);
                        _disconnect();
                        _clearState();
                    }, _request.timeout);
                }
            }

            function _onClientTimeout(_request) {
                _response.closedByClientTimeout = true;
                _response.state = 'closedByClient';
                _response.responseBody = "";
                _response.status = 408;
                _response.messages = [];
                _invokeCallback();
            }

            function _onError(code, reason) {
                _clearState();
                clearTimeout(_request.id);
                _response.state = 'error';
                _response.reasonPhrase = reason;
                _response.responseBody = "";
                _response.status = code;
                _response.messages = [];
                _invokeCallback();
            }

            /**
             * Track received message and make sure callbacks/functions are only invoked when the complete message has been received.
             *
             * @param message
             * @param request
             * @param response
             */
            function _trackMessageSize(message, request, response) {
                message = _handleProtocol(request, message);
                if (message.length === 0)
                    return true;

                response.responseBody = message;

                if (request.trackMessageLength) {
                    // prepend partialMessage if any
                    message = response.partialMessage + message;

                    var messages = [];
                    var messageStart = message.indexOf(request.messageDelimiter);
                    if (messageStart != -1) {
                        while (messageStart !== -1) {
                            var str = message.substring(0, messageStart);
                            var messageLength = +str;
                            if (isNaN(messageLength))
                                throw new Error('message length "' + str + '" is not a number');
                            messageStart += request.messageDelimiter.length;
                            if (messageStart + messageLength > message.length) {
                                // message not complete, so there is no trailing messageDelimiter
                                messageStart = -1;
                            } else {
                                // message complete, so add it
                                messages.push(message.substring(messageStart, messageStart + messageLength));
                                // remove consumed characters
                                message = message.substring(messageStart + messageLength, message.length);
                                messageStart = message.indexOf(request.messageDelimiter);
                            }
                        }

                        /* keep any remaining data */
                        response.partialMessage = message;

                        if (messages.length !== 0) {
                            response.responseBody = messages.join(request.messageDelimiter);
                            response.messages = messages;
                            return false;
                        } else {
                            response.responseBody = "";
                            response.messages = [];
                            return true;
                        }
                    }
                }
                response.responseBody = message;
                response.messages = [message];
                return false;
            }

            /**
             * Reconnect request with fallback transport. <br>
             * Used in case websocket can't be opened.
             *
             * @private
             */
            function _reconnectWithFallbackTransport(errorMessage) {
                atmosphere.util.log(_request.logLevel, [errorMessage]);

                if (typeof (_request.onTransportFailure) !== 'undefined') {
                    _request.onTransportFailure(errorMessage, _request);
                } else if (typeof (atmosphere.util.onTransportFailure) !== 'undefined') {
                    atmosphere.util.onTransportFailure(errorMessage, _request);
                }

                _request.transport = _request.fallbackTransport;
                var reconnectInterval = _request.connectTimeout === -1 ? 0 : _request.connectTimeout;
                if (_request.reconnect && _request.transport !== 'none' || _request.transport == null) {
                    _request.method = _request.fallbackMethod;
                    _response.transport = _request.fallbackTransport;
                    _request.fallbackTransport = 'none';
                    if (reconnectInterval > 0) {
                        _request.reconnectId = setTimeout(function () {
                            _execute();
                        }, reconnectInterval);
                    } else {
                        _execute();
                    }
                } else {
                    _onError(500, "Unable to reconnect with fallback transport");
                }
            }

            /**
             * Get url from request and attach headers to it.
             *
             * @param request {Object} request Request parameters, if undefined _request object will be used.
             *
             * @returns {Object} Request object, if undefined, _request object will be used.
             * @private
             */
            function _attachHeaders(request, url) {
                var rq = _request;
                if ((request != null) && (typeof (request) !== 'undefined')) {
                    rq = request;
                }

                if (url == null) {
                    url = rq.url;
                }

                // If not enabled
                if (!rq.attachHeadersAsQueryString)
                    return url;

                // If already added
                if (url.indexOf("X-Atmosphere-Framework") !== -1) {
                    return url;
                }

                url += (url.indexOf('?') !== -1) ? '&' : '?';
                url += "X-Atmosphere-tracking-id=" + rq.uuid;
                url += "&X-Atmosphere-Framework=" + atmosphere.version;
                url += "&X-Atmosphere-Transport=" + rq.transport;

                if (rq.trackMessageLength) {
                    url += "&X-Atmosphere-TrackMessageSize=" + "true";
                }

                if (rq.heartbeat !== null && rq.heartbeat.server !== null) {
                    url += "&X-Heartbeat-Server=" + rq.heartbeat.server;
                }

                if (rq.contentType !== '') {
                    //Eurk!
                    url += "&Content-Type=" + (rq.transport === 'websocket' ? rq.contentType : encodeURIComponent(rq.contentType));
                }

                if (rq.enableProtocol) {
                    url += "&X-atmo-protocol=true";
                }

                atmosphere.util.each(rq.headers, function (name, value) {
                    var h = atmosphere.util.isFunction(value) ? value.call(this, rq, request, _response) : value;
                    if (h != null) {
                        url += "&" + encodeURIComponent(name) + "=" + encodeURIComponent(h);
                    }
                });

                return url;
            }

            function _triggerOpen(rq) {
                if (!rq.isOpen) {
                    rq.isOpen = true;
                    _open('opening', rq.transport, rq);
                } else if (rq.isReopen) {
                    rq.isReopen = false;
                    _open('re-opening', rq.transport, rq);
                } else if (_response.state === 'messageReceived' && (rq.transport === 'jsonp' || rq.transport === 'long-polling')) {
                    _openAfterResume(_response);
                } else {
                    return;
                }

                _startHeartbeat(rq);
            }

            function _startHeartbeat(rq) {
                if (rq.heartbeatTimer != null) {
                    clearTimeout(rq.heartbeatTimer);
                }

                if (!isNaN(_heartbeatInterval) && _heartbeatInterval > 0) {
                    var _pushHeartbeat = function () {
                        if (_canLog('debug')) {
                            atmosphere.util.debug("Sending heartbeat");
                        }
                        _push(_heartbeatPadding);
                        rq.heartbeatTimer = setTimeout(_pushHeartbeat, _heartbeatInterval);
                    };
                    rq.heartbeatTimer = setTimeout(_pushHeartbeat, _heartbeatInterval);
                }
            }

            /**
             * Execute ajax request. <br>
             *
             * @param request {Object} request Request parameters, if undefined _request object will be used.
             * @private
             */
            function _executeRequest(request) {
                var rq = _request;
                if ((request != null) || (typeof (request) !== 'undefined')) {
                    rq = request;
                }

                rq.lastIndex = 0;
                rq.readyState = 0;

                // CORS fake using JSONP
                if ((rq.transport === 'jsonp') || ((rq.enableXDR) && (atmosphere.util.checkCORSSupport()))) {
                    _jsonp(rq);
                    return;
                }

                if (atmosphere.util.browser.msie && +atmosphere.util.browser.version.split(".")[0] < 10) {
                    if ((rq.transport === 'streaming')) {
                        if (rq.enableXDR && window.XDomainRequest) {
                            _ieXDR(rq);
                        } else {
                            _ieStreaming(rq);
                        }
                        return;
                    }

                    if ((rq.enableXDR) && (window.XDomainRequest)) {
                        _ieXDR(rq);
                        return;
                    }
                }

                var reconnectFExec = function (force) {
                    rq.lastIndex = 0;
                    _requestCount++; // Increase also when forcing reconnect as _open checks _requestCount
                    if (force || (rq.reconnect && _requestCount <= rq.maxReconnectOnClose)) {
                        var delay = force ? 0 : request.reconnectInterval; // Reconnect immediately if the server resumed the connection (timeout)
                        _response.ffTryingReconnect = true;
                        _open('re-connecting', request.transport, request);
                        _reconnect(ajaxRequest, rq, delay);
                    } else {
                        _onError(0, "maxReconnectOnClose reached");
                    }
                };

                var reconnectF = function (force){
                    if(atmosphere._beforeUnloadState){
                        // ATMOSPHERE-JAVASCRIPT-143: Delay reconnect to avoid reconnect attempts before an actual unload (we don't know if an unload will happen, yet)
                        atmosphere.util.debug(new Date() + " Atmosphere: reconnectF: execution delayed due to _beforeUnloadState flag");
                        setTimeout(function () {
                            reconnectFExec(force);
                        }, 5000);
                    }else {
                        reconnectFExec(force);
                    }
                };

                var disconnected = function () {
                    // Prevent onerror callback to be called
                    _response.errorHandled = true;
                    _clearState();
                    reconnectF(false);
                };

                if (rq.force || (rq.reconnect && (rq.maxRequest === -1 || rq.requestCount++ < rq.maxRequest))) {
                    rq.force = false;

                    var ajaxRequest = atmosphere.util.xhr();
                    ajaxRequest.hasData = false;

                    _doRequest(ajaxRequest, rq, true);

                    if (rq.suspend) {
                        _activeRequest = ajaxRequest;
                    }

                    if (rq.transport !== 'polling') {
                        _response.transport = rq.transport;

                        ajaxRequest.onabort = function () {
                            _debug("ajaxrequest.onabort")
                            _invokeClose(true);
                        };

                        ajaxRequest.onerror = function () {
                            _debug("ajaxrequest.onerror")
                            _response.error = true;
                            _response.ffTryingReconnect = true;
                            try {
                                _response.status = XMLHttpRequest.status;
                            } catch (e) {
                                _response.status = 500;
                            }

                            if (!_response.status) {
                                _response.status = 500;
                            }
                            if (!_response.errorHandled) {
                                _clearState();
                                reconnectF(false);
                            }
                        };
                    }

                    ajaxRequest.onreadystatechange = function () {
                        _debug("ajaxRequest.onreadystatechange, new state: " + ajaxRequest.readyState);
                        if (_abortingConnection) {
                            _debug("onreadystatechange has been ignored due to _abortingConnection flag");
                            return;
                        }

                        _response.error = null;
                        var skipCallbackInvocation = false;
                        var update = false;

                        if (rq.transport === 'streaming' && rq.readyState > 2 && ajaxRequest.readyState === 4) {
                            _clearState();
                            reconnectF(false);
                            return;
                        }

                        rq.readyState = ajaxRequest.readyState;

                        if (rq.transport === 'streaming' && ajaxRequest.readyState >= 3) {
                            update = true;
                        } else if (rq.transport === 'long-polling' && ajaxRequest.readyState === 4) {
                            update = true;
                        }
                        _timeout(_request);

                        if (rq.transport !== 'polling') {
                            // MSIE 9 and lower status can be higher than 1000, Chrome can be 0
                            var status = 200;
                            if (ajaxRequest.readyState === 4) {
                                status = ajaxRequest.status > 1000 ? 0 : ajaxRequest.status;
                            }

                            if (!rq.reconnectOnServerError && (status >= 300 && status < 600)) {
                                _onError(status, ajaxRequest.statusText);
                                return;
                            }

                            if (status >= 300 || status === 0) {
                                disconnected();
                                return;
                            }

                            // Firefox incorrectly send statechange 0->2 when a reconnect attempt fails. The above checks ensure that onopen is not called for these
                            if ((!rq.enableProtocol || !request.firstMessage) && ajaxRequest.readyState === 2) {
                                // Firefox incorrectly send statechange 0->2 when a reconnect attempt fails. The above checks ensure that onopen is not called for these
                                // In that case, ajaxRequest.onerror will be called just after onreadystatechange is called, so we delay the trigger until we are
                                // guarantee the connection is well established.
                                if (atmosphere.util.browser.mozilla && _response.ffTryingReconnect) {
                                    _response.ffTryingReconnect = false;
                                    setTimeout(function () {
                                        if (!_response.ffTryingReconnect) {
                                            _triggerOpen(rq);
                                        }
                                    }, 500);
                                } else {
                                    _triggerOpen(rq);
                                }
                            }

                        } else if (ajaxRequest.readyState === 4) {
                            update = true;
                        }

                        if (update) {
                            var responseText = ajaxRequest.responseText;
                            _response.errorHandled = false;

                            // IE behave the same way when resuming long-polling or when the server goes down.
                            if (rq.transport === 'long-polling' && atmosphere.util.trim(responseText).length === 0) {
                                // For browser that aren't support onabort
                                if (!ajaxRequest.hasData) {
                                    reconnectF(true);
                                } else {
                                    ajaxRequest.hasData = false;
                                }
                                return;
                            }
                            ajaxRequest.hasData = true;

                            _readHeaders(ajaxRequest, _request);

                            if (rq.transport === 'streaming') {
                                if (!atmosphere.util.browser.opera) {
                                    var message = responseText.substring(rq.lastIndex, responseText.length);
                                    skipCallbackInvocation = _trackMessageSize(message, rq, _response);

                                    rq.lastIndex = responseText.length;
                                    if (skipCallbackInvocation) {
                                        return;
                                    }
                                } else {
                                    atmosphere.util.iterate(function () {
                                        if (_response.status !== 500 && ajaxRequest.responseText.length > rq.lastIndex) {
                                            try {
                                                _response.status = ajaxRequest.status;
                                                _response.headers = atmosphere.util.parseHeaders(ajaxRequest.getAllResponseHeaders());

                                                _readHeaders(ajaxRequest, _request);

                                            } catch (e) {
                                                _response.status = 404;
                                            }
                                            _timeout(_request);

                                            _response.state = "messageReceived";
                                            var message = ajaxRequest.responseText.substring(rq.lastIndex);
                                            rq.lastIndex = ajaxRequest.responseText.length;

                                            skipCallbackInvocation = _trackMessageSize(message, rq, _response);
                                            if (!skipCallbackInvocation) {
                                                _invokeCallback();
                                            }

                                            if (_verifyStreamingLength(ajaxRequest, rq)) {
                                                _reconnectOnMaxStreamingLength(ajaxRequest, rq);
                                                return;
                                            }
                                        } else if (_response.status > 400) {
                                            // Prevent replaying the last message.
                                            rq.lastIndex = ajaxRequest.responseText.length;
                                            return false;
                                        }
                                    }, 0);
                                }
                            } else {
                                skipCallbackInvocation = _trackMessageSize(responseText, rq, _response);
                            }
                            var closeStream = _verifyStreamingLength(ajaxRequest, rq);

                            try {
                                _response.status = ajaxRequest.status;
                                _response.headers = atmosphere.util.parseHeaders(ajaxRequest.getAllResponseHeaders());

                                _readHeaders(ajaxRequest, rq);
                            } catch (e) {
                                _response.status = 404;
                            }

                            if (rq.suspend) {
                                _response.state = _response.status === 0 ? "closed" : "messageReceived";
                            } else {
                                _response.state = "messagePublished";
                            }

                            var isAllowedToReconnect = !closeStream && request.transport !== 'streaming' && request.transport !== 'polling';
                            if (isAllowedToReconnect && !rq.executeCallbackBeforeReconnect) {
                                _reconnect(ajaxRequest, rq, rq.pollingInterval);
                            }

                            if (_response.responseBody.length !== 0 && !skipCallbackInvocation)
                                _invokeCallback();

                            if (isAllowedToReconnect && rq.executeCallbackBeforeReconnect) {
                                _reconnect(ajaxRequest, rq, rq.pollingInterval);
                            }

                            if (closeStream) {
                                _reconnectOnMaxStreamingLength(ajaxRequest, rq);
                            }
                        }
                    };

                    try {
                        ajaxRequest.send(rq.data);
                        _subscribed = true;
                    } catch (e) {
                        atmosphere.util.log(rq.logLevel, ["Unable to connect to " + rq.url]);
                        _onError(0, e);
                    }

                } else {
                    if (rq.logLevel === 'debug') {
                        atmosphere.util.log(rq.logLevel, ["Max re-connection reached."]);
                    }
                    _onError(0, "maxRequest reached");
                }
            }

            function _reconnectOnMaxStreamingLength(ajaxRequest, rq) {
                _response.messages = [];
                rq.isReopen = true;
                _close();
                _abortingConnection = false;
                _reconnect(ajaxRequest, rq, 500);
            }

            /**
             * Do ajax request.
             *
             * @param ajaxRequest Ajax request.
             * @param request Request parameters.
             * @param create If ajax request has to be open.
             */
            function _doRequest(ajaxRequest, request, create) {
                // Prevent Android to cache request
                var url = request.url;
                if (request.dispatchUrl != null && request.method === 'POST') {
                    url += request.dispatchUrl;
                }
                url = _attachHeaders(request, url);
                url = atmosphere.util.prepareURL(url);

                if (create) {
                    ajaxRequest.open(request.method, url, request.async);
                    if (request.connectTimeout > 0) {
                        request.id = setTimeout(function () {
                            if (request.requestCount === 0) {
                                _clearState();
                                _prepareCallback("Connect timeout", "closed", 200, request.transport);
                            }
                        }, request.connectTimeout);
                    }
                }

                if (_request.withCredentials && _request.transport !== 'websocket') {
                    if ("withCredentials" in ajaxRequest) {
                        ajaxRequest.withCredentials = true;
                    }
                }

                if (!_request.dropHeaders) {
                    ajaxRequest.setRequestHeader("X-Atmosphere-Framework", atmosphere.version);
                    ajaxRequest.setRequestHeader("X-Atmosphere-Transport", request.transport);

                    if (request.heartbeat !== null && request.heartbeat.server !== null) {
                        ajaxRequest.setRequestHeader("X-Heartbeat-Server", ajaxRequest.heartbeat.server);
                    }

                    if (request.trackMessageLength) {
                        ajaxRequest.setRequestHeader("X-Atmosphere-TrackMessageSize", "true");
                    }
                    ajaxRequest.setRequestHeader("X-Atmosphere-tracking-id", request.uuid);

                    atmosphere.util.each(request.headers, function (name, value) {
                        var h = atmosphere.util.isFunction(value) ? value.call(this, ajaxRequest, request, create, _response) : value;
                        if (h != null) {
                            ajaxRequest.setRequestHeader(name, h);
                        }
                    });
                }

                if (request.contentType !== '') {
                    ajaxRequest.setRequestHeader("Content-Type", request.contentType);
                }
            }

            function _reconnect(ajaxRequest, request, delay) {

                if (_response.closedByClientTimeout) {
                    return;
                }

                if (request.reconnect || (request.suspend && _subscribed)) {
                    var status = 0;
                    if (ajaxRequest && ajaxRequest.readyState > 1) {
                        status = ajaxRequest.status > 1000 ? 0 : ajaxRequest.status;
                    }
                    _response.status = status === 0 ? 204 : status;
                    _response.reason = status === 0 ? "Server resumed the connection or down." : "OK";

                    clearTimeout(request.id);
                    if (request.reconnectId) {
                        clearTimeout(request.reconnectId);
                        delete request.reconnectId;
                    }

                    if (delay > 0) {
                        // For whatever reason, never cancel a reconnect timeout as it is mandatory to reconnect.
                        _request.reconnectId = setTimeout(function () {
                            _executeRequest(request);
                        }, delay);
                    } else {
                        _executeRequest(request);
                    }
                }
            }

            function _tryingToReconnect(response) {
                response.state = 're-connecting';
                _invokeFunction(response);
            }

            function _openAfterResume(response) {
                response.state = 'openAfterResume';
                _invokeFunction(response);
                response.state = 'messageReceived';
            }

            function _ieXDR(request) {
                if (request.transport !== "polling") {
                    _ieStream = _configureXDR(request);
                    _ieStream.open();
                } else {
                    _configureXDR(request).open();
                }
            }

            function _configureXDR(request) {
                var rq = _request;
                if ((request != null) && (typeof (request) !== 'undefined')) {
                    rq = request;
                }

                var transport = rq.transport;
                var lastIndex = 0;
                var xdr = new window.XDomainRequest();
                var reconnect = function () {
                    if (rq.transport === "long-polling" && (rq.reconnect && (rq.maxRequest === -1 || rq.requestCount++ < rq.maxRequest))) {
                        xdr.status = 200;
                        _ieXDR(rq);
                    }
                };

                var rewriteURL = rq.rewriteURL || function (url) {
                        // Maintaining session by rewriting URL
                        // http://stackoverflow.com/questions/6453779/maintaining-session-by-rewriting-url
                        var match = /(?:^|;\s*)(JSESSIONID|PHPSESSID)=([^;]*)/.exec(document.cookie);

                        switch (match && match[1]) {
                            case "JSESSIONID":
                                return url.replace(/;jsessionid=[^\?]*|(\?)|$/, ";jsessionid=" + match[2] + "$1");
                            case "PHPSESSID":
                                return url.replace(/\?PHPSESSID=[^&]*&?|\?|$/, "?PHPSESSID=" + match[2] + "&").replace(/&$/, "");
                        }
                        return url;
                    };

                // Handles open and message event
                xdr.onprogress = function () {
                    handle(xdr);
                };
                // Handles error event
                xdr.onerror = function () {
                    // If the server doesn't send anything back to XDR will fail with polling
                    if (rq.transport !== 'polling') {
                        _clearState();
                        if (_requestCount++ < rq.maxReconnectOnClose) {
                            if (rq.reconnectInterval > 0) {
                                rq.reconnectId = setTimeout(function () {
                                    _open('re-connecting', request.transport, request);
                                    _ieXDR(rq);
                                }, rq.reconnectInterval);
                            } else {
                                _open('re-connecting', request.transport, request);
                                _ieXDR(rq);
                            }
                        } else {
                            _onError(0, "maxReconnectOnClose reached");
                        }
                    }
                };

                // Handles close event
                xdr.onload = function () {
                };

                var handle = function (xdr) {
                    clearTimeout(rq.id);
                    var message = xdr.responseText;

                    message = message.substring(lastIndex);
                    lastIndex += message.length;

                    if (transport !== 'polling') {
                        _timeout(rq);

                        var skipCallbackInvocation = _trackMessageSize(message, rq, _response);

                        if (transport === 'long-polling' && atmosphere.util.trim(message).length === 0)
                            return;

                        if (rq.executeCallbackBeforeReconnect) {
                            reconnect();
                        }

                        if (!skipCallbackInvocation) {
                            _prepareCallback(_response.responseBody, "messageReceived", 200, transport);
                        }

                        if (!rq.executeCallbackBeforeReconnect) {
                            reconnect();
                        }
                    }
                };

                return {
                    open: function () {
                        var url = rq.url;
                        if (rq.dispatchUrl != null) {
                            url += rq.dispatchUrl;
                        }
                        url = _attachHeaders(rq, url);
                        xdr.open(rq.method, rewriteURL(url));
                        if (rq.method === 'GET') {
                            xdr.send();
                        } else {
                            xdr.send(rq.data);
                        }

                        if (rq.connectTimeout > 0) {
                            rq.id = setTimeout(function () {
                                if (rq.requestCount === 0) {
                                    _clearState();
                                    _prepareCallback("Connect timeout", "closed", 200, rq.transport);
                                }
                            }, rq.connectTimeout);
                        }
                    },
                    close: function () {
                        xdr.abort();
                    }
                };
            }

            function _ieStreaming(request) {
                _ieStream = _configureIE(request);
                _ieStream.open();
            }

            function _configureIE(request) {
                var rq = _request;
                if ((request != null) && (typeof (request) !== 'undefined')) {
                    rq = request;
                }

                var stop;
                var doc = new window.ActiveXObject("htmlfile");

                doc.open();
                doc.close();

                var url = rq.url;
                if (rq.dispatchUrl != null) {
                    url += rq.dispatchUrl;
                }

                if (rq.transport !== 'polling') {
                    _response.transport = rq.transport;
                }

                return {
                    open: function () {
                        var iframe = doc.createElement("iframe");

                        url = _attachHeaders(rq);
                        if (rq.data !== '') {
                            url += "&X-Atmosphere-Post-Body=" + encodeURIComponent(rq.data);
                        }

                        // Finally attach a timestamp to prevent Android and IE caching.
                        url = atmosphere.util.prepareURL(url);

                        iframe.src = url;
                        doc.body.appendChild(iframe);

                        // For the server to respond in a consistent format regardless of user agent, we polls response text
                        var cdoc = iframe.contentDocument || iframe.contentWindow.document;

                        stop = atmosphere.util.iterate(function () {
                            try {
                                if (!cdoc.firstChild) {
                                    return;
                                }

                                var res = cdoc.body ? cdoc.body.lastChild : cdoc;
                                if (res.omgThisIsBroken) {
                                    // Cause an exception when res is null, to trigger a reconnect...
                                }
                                var readResponse = function () {
                                    // Clones the element not to disturb the original one
                                    var clone = res.cloneNode(true);

                                    // If the last character is a carriage return or a line feed, IE ignores it in the innerText property
                                    // therefore, we add another non-newline character to preserve it
                                    clone.appendChild(cdoc.createTextNode("."));

                                    var text = clone.innerText;

                                    text = text.substring(0, text.length - 1);
                                    return text;

                                };

                                // To support text/html content type
                                if (!cdoc.body || !cdoc.body.firstChild || cdoc.body.firstChild.nodeName.toLowerCase() !== "pre") {
                                    // Injects a plaintext element which renders text without interpreting the HTML and cannot be stopped
                                    // it is deprecated in HTML5, but still works
                                    var head = cdoc.head || cdoc.getElementsByTagName("head")[0] || cdoc.documentElement || cdoc;
                                    var script = cdoc.createElement("script");

                                    script.text = "document.write('<plaintext>')";

                                    head.insertBefore(script, head.firstChild);
                                    head.removeChild(script);

                                    // The plaintext element will be the response container
                                    res = cdoc.body.lastChild;
                                }

                                if (rq.closed) {
                                    rq.isReopen = true;
                                }

                                // Handles message and close event
                                stop = atmosphere.util.iterate(function () {
                                    var text = readResponse();
                                    if (text.length > rq.lastIndex) {
                                        _timeout(_request);

                                        _response.status = 200;
                                        _response.error = null;

                                        // Empties response every time that it is handled
                                        res.innerText = "";
                                        var skipCallbackInvocation = _trackMessageSize(text, rq, _response);
                                        if (skipCallbackInvocation) {
                                            return "";
                                        }

                                        _prepareCallback(_response.responseBody, "messageReceived", 200, rq.transport);
                                    }

                                    rq.lastIndex = 0;

                                    if (cdoc.readyState === "complete") {
                                        _invokeClose(true);
                                        _open('re-connecting', rq.transport, rq);
                                        if (rq.reconnectInterval > 0) {
                                            rq.reconnectId = setTimeout(function () {
                                                _ieStreaming(rq);
                                            }, rq.reconnectInterval);
                                        } else {
                                            _ieStreaming(rq);
                                        }
                                        return false;
                                    }
                                }, null);

                                return false;
                            } catch (err) {
                                _response.error = true;
                                _open('re-connecting', rq.transport, rq);
                                if (_requestCount++ < rq.maxReconnectOnClose) {
                                    if (rq.reconnectInterval > 0) {
                                        rq.reconnectId = setTimeout(function () {
                                            _ieStreaming(rq);
                                        }, rq.reconnectInterval);
                                    } else {
                                        _ieStreaming(rq);
                                    }
                                } else {
                                    _onError(0, "maxReconnectOnClose reached");
                                }
                                doc.execCommand("Stop");
                                doc.close();
                                return false;
                            }
                        });
                    },

                    close: function () {
                        if (stop) {
                            stop();
                        }

                        doc.execCommand("Stop");
                        _invokeClose(true);
                    }
                };
            }

            /**
             * Send message. <br>
             * Will be automatically dispatch to other connected.
             *
             * @param {Object, string} Message to send.
             * @private
             */
            function _push(message) {

                if (_localStorageService != null) {
                    _pushLocal(message);
                } else if (_activeRequest != null || _sse != null) {
                    _pushAjaxMessage(message);
                } else if (_ieStream != null) {
                    _pushIE(message);
                } else if (_jqxhr != null) {
                    _pushJsonp(message);
                } else if (_websocket != null) {
                    _pushWebSocket(message);
                } else {
                    _onError(0, "No suspended connection available");
                    atmosphere.util.error("No suspended connection available. Make sure atmosphere.subscribe has been called and request.onOpen invoked before trying to push data");
                }
            }

            function _pushOnClose(message, rq) {
                if (!rq) {
                    rq = _getPushRequest(message);
                }
                rq.transport = "polling";
                rq.method = "GET";
                rq.withCredentials = false;
                rq.reconnect = false;
                rq.force = true;
                rq.suspend = false;
                rq.timeout = 1000;
                _executeRequest(rq);
            }

            function _pushLocal(message) {
                _localStorageService.send(message);
            }

            function _intraPush(message) {
                // IE 9 will crash if not.
                if (message.length === 0)
                    return;

                try {
                    if (_localStorageService) {
                        _localStorageService.localSend(message);
                    } else if (_storageService) {
                        _storageService.signal("localMessage", atmosphere.util.stringifyJSON({
                            id: guid,
                            event: message
                        }));
                    }
                } catch (err) {
                    atmosphere.util.error(err);
                }
            }

            /**
             * Send a message using currently opened ajax request (using http-streaming or long-polling). <br>
             *
             * @param {string, Object} Message to send. This is an object, string message is saved in data member.
             * @private
             */
            function _pushAjaxMessage(message) {
                var rq = _getPushRequest(message);
                _executeRequest(rq);
            }

            /**
             * Send a message using currently opened ie streaming (using http-streaming or long-polling). <br>
             *
             * @param {string, Object} Message to send. This is an object, string message is saved in data member.
             * @private
             */
            function _pushIE(message) {
                if (_request.enableXDR && atmosphere.util.checkCORSSupport()) {
                    var rq = _getPushRequest(message);
                    // Do not reconnect since we are pushing.
                    rq.reconnect = false;
                    _jsonp(rq);
                } else {
                    _pushAjaxMessage(message);
                }
            }

            /**
             * Send a message using jsonp transport. <br>
             *
             * @param {string, Object} Message to send. This is an object, string message is saved in data member.
             * @private
             */
            function _pushJsonp(message) {
                _pushAjaxMessage(message);
            }

            function _getStringMessage(message) {
                var msg = message;
                if (typeof (msg) === 'object') {
                    msg = message.data;
                }
                return msg;
            }

            /**
             * Build request use to push message using method 'POST' <br>. Transport is defined as 'polling' and 'suspend' is set to false.
             *
             * @return {Object} Request object use to push message.
             * @private
             */
            function _getPushRequest(message) {
                var msg = _getStringMessage(message);

                var rq = {
                    connected: false,
                    timeout: 60000,
                    method: 'POST',
                    url: _request.url,
                    contentType: _request.contentType,
                    headers: _request.headers,
                    reconnect: true,
                    callback: null,
                    data: msg,
                    suspend: false,
                    maxRequest: -1,
                    logLevel: 'info',
                    requestCount: 0,
                    withCredentials: _request.withCredentials,
                    async: _request.async,
                    transport: 'polling',
                    isOpen: true,
                    attachHeadersAsQueryString: true,
                    enableXDR: _request.enableXDR,
                    uuid: _request.uuid,
                    dispatchUrl: _request.dispatchUrl,
                    enableProtocol: false,
                    messageDelimiter: '|',
                    trackMessageLength: _request.trackMessageLength,
                    maxReconnectOnClose: _request.maxReconnectOnClose,
                    heartbeatTimer: _request.heartbeatTimer,
                    heartbeat: _request.heartbeat
                };

                if (typeof (message) === 'object') {
                    rq = atmosphere.util.extend(rq, message);
                }

                return rq;
            }

            /**
             * Send a message using currently opened websocket. <br>
             *
             */
            function _pushWebSocket(message) {
                var msg = atmosphere.util.isBinary(message) ? message : _getStringMessage(message);
                var data;
                try {
                    if (_request.dispatchUrl != null) {
                        data = _request.webSocketPathDelimiter + _request.dispatchUrl + _request.webSocketPathDelimiter + msg;
                    } else {
                        data = msg;
                    }

                    if (!_websocket.canSendMessage) {
                        atmosphere.util.error("WebSocket not connected.");
                        return;
                    }

                    _websocket.send(data);

                } catch (e) {
                    _websocket.onclose = function (message) {
                    };
                    _clearState();

                    _reconnectWithFallbackTransport("Websocket failed. Downgrading to " + _request.fallbackTransport + " and resending " + message);
                    _pushAjaxMessage(message);
                }
            }

            function _localMessage(message) {
                var m = atmosphere.util.parseJSON(message);
                if (m.id !== guid) {
                    if (typeof (_request.onLocalMessage) !== 'undefined') {
                        _request.onLocalMessage(m.event);
                    } else if (typeof (atmosphere.util.onLocalMessage) !== 'undefined') {
                        atmosphere.util.onLocalMessage(m.event);
                    }
                }
            }

            function _prepareCallback(messageBody, state, errorCode, transport) {

                _response.responseBody = messageBody;
                _response.transport = transport;
                _response.status = errorCode;
                _response.state = state;

                _invokeCallback();
            }

            function _readHeaders(xdr, request) {
                if (!request.readResponsesHeaders) {
                    if (!request.enableProtocol) {
                        request.uuid = guid;
                    }
                }
                else {
                    try {

                        var tempUUID = xdr.getResponseHeader('X-Atmosphere-tracking-id');
                        if (tempUUID && tempUUID != null) {
                            request.uuid = tempUUID.split(" ").pop();
                        }
                    } catch (e) {
                    }
                }
            }

            function _invokeFunction(response) {
                _f(response, _request);
                // Global
                _f(response, atmosphere.util);
            }

            function _f(response, f) {
                switch (response.state) {
                    case "messageReceived":
                        _debug("Firing onMessage");
                        _requestCount = 0;
                        if (typeof (f.onMessage) !== 'undefined')
                            f.onMessage(response);

                        if (typeof (f.onmessage) !== 'undefined')
                            f.onmessage(response);
                        break;
                    case "error":
                        var dbgReasonPhrase = (typeof(response.reasonPhrase) != 'undefined') ? response.reasonPhrase : 'n/a';
                        _debug("Firing onError, reasonPhrase: " + dbgReasonPhrase);
                        if (typeof (f.onError) !== 'undefined')
                            f.onError(response);

                        if (typeof (f.onerror) !== 'undefined')
                            f.onerror(response);
                        break;
                    case "opening":
                        delete _request.closed;
                        _debug("Firing onOpen");
                        if (typeof (f.onOpen) !== 'undefined')
                            f.onOpen(response);

                        if (typeof (f.onopen) !== 'undefined')
                            f.onopen(response);
                        break;
                    case "messagePublished":
                        _debug("Firing messagePublished");
                        if (typeof (f.onMessagePublished) !== 'undefined')
                            f.onMessagePublished(response);
                        break;
                    case "re-connecting":
                        _debug("Firing onReconnect");
                        if (typeof (f.onReconnect) !== 'undefined')
                            f.onReconnect(_request, response);
                        break;
                    case "closedByClient":
                        _debug("Firing closedByClient");
                        if (typeof (f.onClientTimeout) !== 'undefined')
                            f.onClientTimeout(_request);
                        break;
                    case "re-opening":
                        delete _request.closed;
                        _debug("Firing onReopen");
                        if (typeof (f.onReopen) !== 'undefined')
                            f.onReopen(_request, response);
                        break;
                    case "fail-to-reconnect":
                        _debug("Firing onFailureToReconnect");
                        if (typeof (f.onFailureToReconnect) !== 'undefined')
                            f.onFailureToReconnect(_request, response);
                        break;
                    case "unsubscribe":
                    case "closed":
                        var closed = typeof (_request.closed) !== 'undefined' ? _request.closed : false;

                        if (!closed) {
                            _debug("Firing onClose (" + response.state + " case)");
                            if (typeof (f.onClose) !== 'undefined') {
                                f.onClose(response);
                            }

                            if (typeof (f.onclose) !== 'undefined') {
                                f.onclose(response);
                            }
                        } else {
                            _debug("Request already closed, not firing onClose (" + response.state + " case)");
                        }
                        _request.closed = true;
                        break;
                    case "openAfterResume":
                        if (typeof (f.onOpenAfterResume) !== 'undefined')
                            f.onOpenAfterResume(_request);
                        break;
                }
            }

            function _invokeClose(wasOpen) {
                if (_response.state !== 'closed') {
                    _response.state = 'closed';
                    _response.responseBody = "";
                    _response.messages = [];
                    _response.status = !wasOpen ? 501 : 200;
                    _invokeCallback();
                }
            }

            /**
             * Invoke request callbacks.
             *
             * @private
             */
            function _invokeCallback() {
                var call = function (index, func) {
                    func(_response);
                };

                if (_localStorageService == null && _localSocketF != null) {
                    _localSocketF(_response.responseBody);
                }

                _request.reconnect = _request.mrequest;

                var isString = typeof (_response.responseBody) === 'string';
                var messages = (isString && _request.trackMessageLength) ? (_response.messages.length > 0 ? _response.messages : ['']) : new Array(
                    _response.responseBody);
                for (var i = 0; i < messages.length; i++) {

                    if (messages.length > 1 && messages[i].length === 0) {
                        continue;
                    }
                    _response.responseBody = (isString) ? atmosphere.util.trim(messages[i]) : messages[i];

                    if (_localStorageService == null && _localSocketF != null) {
                        _localSocketF(_response.responseBody);
                    }

                    if ((_response.responseBody.length === 0 ||
                        (isString && _heartbeatPadding === _response.responseBody)) && _response.state === "messageReceived") {
                        continue;
                    }

                    _invokeFunction(_response);

                    // Invoke global callbacks
                    if (callbacks.length > 0) {
                        if (_canLog('debug')) {
                            atmosphere.util.debug("Invoking " + callbacks.length + " global callbacks: " + _response.state);
                        }
                        try {
                            atmosphere.util.each(callbacks, call);
                        } catch (e) {
                            atmosphere.util.log(_request.logLevel, ["Callback exception" + e]);
                        }
                    }

                    // Invoke request callback
                    if (typeof (_request.callback) === 'function') {
                        if (_canLog('debug')) {
                            atmosphere.util.debug("Invoking request callbacks");
                        }
                        try {
                            _request.callback(_response);
                        } catch (e) {
                            atmosphere.util.log(_request.logLevel, ["Callback exception" + e]);
                        }
                    }
                }
            }

            this.subscribe = function (options) {
                _subscribe(options);
                _execute();
            };

            this.execute = function () {
                _execute();
            };

            this.close = function () {
                _close();
            };

            this.disconnect = function () {
                _disconnect();
            };

            this.getUrl = function () {
                return _request.url;
            };

            this.push = function (message, dispatchUrl) {
                if (dispatchUrl != null) {
                    var originalDispatchUrl = _request.dispatchUrl;
                    _request.dispatchUrl = dispatchUrl;
                    _push(message);
                    _request.dispatchUrl = originalDispatchUrl;
                } else {
                    _push(message);
                }
            };

            this.getUUID = function () {
                return _request.uuid;
            };

            this.pushLocal = function (message) {
                _intraPush(message);
            };

            this.enableProtocol = function (message) {
                return _request.enableProtocol;
            };

            this.init = function () {
                _init();
            };

            this.request = _request;
            this.response = _response;
        }
    };

    atmosphere.subscribe = function (url, callback, request) {
        if (typeof (callback) === 'function') {
            atmosphere.addCallback(callback);
        }

        if (typeof (url) !== "string") {
            request = url;
        } else {
            request.url = url;
        }

        // https://github.com/Atmosphere/atmosphere-javascript/issues/58
        uuid = ((typeof (request) !== 'undefined') && typeof (request.uuid) !== 'undefined') ? request.uuid : 0;

        var rq = new atmosphere.AtmosphereRequest(request);
        rq.execute();

        requests[requests.length] = rq;
        return rq;
    };

    atmosphere.unsubscribe = function () {
        if (requests.length > 0) {
            var requestsClone = [].concat(requests);
            for (var i = 0; i < requestsClone.length; i++) {
                var rq = requestsClone[i];
                rq.close();
                clearTimeout(rq.response.request.id);

                if (rq.heartbeatTimer) {
                    clearTimeout(rq.heartbeatTimer);
                }
            }
        }
        requests = [];
        callbacks = [];
    };

    atmosphere.unsubscribeUrl = function (url) {
        var idx = -1;
        if (requests.length > 0) {
            for (var i = 0; i < requests.length; i++) {
                var rq = requests[i];

                // Suppose you can subscribe once to an url
                if (rq.getUrl() === url) {
                    rq.close();
                    clearTimeout(rq.response.request.id);

                    if (rq.heartbeatTimer) {
                        clearTimeout(rq.heartbeatTimer);
                    }

                    idx = i;
                    break;
                }
            }
        }
        if (idx >= 0) {
            requests.splice(idx, 1);
        }
    };

    atmosphere.addCallback = function (func) {
        if (atmosphere.util.inArray(func, callbacks) === -1) {
            callbacks.push(func);
        }
    };

    atmosphere.removeCallback = function (func) {
        var index = atmosphere.util.inArray(func, callbacks);
        if (index !== -1) {
            callbacks.splice(index, 1);
        }
    };

    atmosphere.util = {
        browser: {},

        parseHeaders: function (headerString) {
            var match, rheaders = /^(.*?):[ \t]*([^\r\n]*)\r?$/mg, headers = {};
            while (match = rheaders.exec(headerString)) {
                headers[match[1]] = match[2];
            }
            return headers;
        },

        now: function () {
            return new Date().getTime();
        },

        isArray: function (array) {
            return Object.prototype.toString.call(array) === "[object Array]";
        },

        inArray: function (elem, array) {
            if (!Array.prototype.indexOf) {
                var len = array.length;
                for (var i = 0; i < len; ++i) {
                    if (array[i] === elem) {
                        return i;
                    }
                }
                return -1;
            }
            return array.indexOf(elem);
        },

        isBinary: function (data) {
            // True if data is an instance of Blob, ArrayBuffer or ArrayBufferView
            return /^\[object\s(?:Blob|ArrayBuffer|.+Array)\]$/.test(Object.prototype.toString.call(data));
        },

        isFunction: function (fn) {
            return Object.prototype.toString.call(fn) === "[object Function]";
        },

        getAbsoluteURL: function (url) {
            if (typeof (document.createElement) === 'undefined') {
                // assuming the url to be already absolute when DOM is not supported
                return url;
            }
            var div = document.createElement("div");

            // Uses an innerHTML property to obtain an absolute URL
            div.innerHTML = '<a href="' + url + '"/>';

            // encodeURI and decodeURI are needed to normalize URL between IE and non-IE,
            // since IE doesn't encode the href property value and return it - http://jsfiddle.net/Yq9M8/1/
            return encodeURI(decodeURI(div.firstChild.href));
        },

        prepareURL: function (url) {
            // Attaches a time stamp to prevent caching
            var ts = atmosphere.util.now();
            var ret = url.replace(/([?&])_=[^&]*/, "$1_=" + ts);

            return ret + (ret === url ? (/\?/.test(url) ? "&" : "?") + "_=" + ts : "");
        },

        trim: function (str) {
            if (!String.prototype.trim) {
                return str.toString().replace(/(?:(?:^|\n)\s+|\s+(?:$|\n))/g, "").replace(/\s+/g, " ");
            } else {
                return str.toString().trim();
            }
        },

        param: function (params) {
            var prefix, s = [];

            function add(key, value) {
                value = atmosphere.util.isFunction(value) ? value() : (value == null ? "" : value);
                s.push(encodeURIComponent(key) + "=" + encodeURIComponent(value));
            }

            function buildParams(prefix, obj) {
                var name;

                if (atmosphere.util.isArray(obj)) {
                    atmosphere.util.each(obj, function (i, v) {
                        if (/\[\]$/.test(prefix)) {
                            add(prefix, v);
                        } else {
                            buildParams(prefix + "[" + (typeof v === "object" ? i : "") + "]", v);
                        }
                    });
                } else if (Object.prototype.toString.call(obj) === "[object Object]") {
                    for (name in obj) {
                        buildParams(prefix + "[" + name + "]", obj[name]);
                    }
                } else {
                    add(prefix, obj);
                }
            }

            for (prefix in params) {
                buildParams(prefix, params[prefix]);
            }

            return s.join("&").replace(/%20/g, "+");
        },

        storage: function () {
            try {
                return !!(window.localStorage && window.StorageEvent);
            } catch (e) {
                //Firefox throws an exception here, see
                //https://bugzilla.mozilla.org/show_bug.cgi?id=748620
                return false;
            }
        },

        iterate: function (fn, interval) {
            var timeoutId;

            // Though the interval is 0 for real-time application, there is a delay between setTimeout calls
            // For detail, see https://developer.mozilla.org/en/window.setTimeout#Minimum_delay_and_timeout_nesting
            interval = interval || 0;

            (function loop() {
                timeoutId = setTimeout(function () {
                    if (fn() === false) {
                        return;
                    }

                    loop();
                }, interval);
            })();

            return function () {
                clearTimeout(timeoutId);
            };
        },

        each: function (obj, callback, args) {
            if (!obj) return;
            var value, i = 0, length = obj.length, isArray = atmosphere.util.isArray(obj);

            if (args) {
                if (isArray) {
                    for (; i < length; i++) {
                        value = callback.apply(obj[i], args);

                        if (value === false) {
                            break;
                        }
                    }
                } else {
                    for (i in obj) {
                        value = callback.apply(obj[i], args);

                        if (value === false) {
                            break;
                        }
                    }
                }

                // A special, fast, case for the most common use of each
            } else {
                if (isArray) {
                    for (; i < length; i++) {
                        value = callback.call(obj[i], i, obj[i]);

                        if (value === false) {
                            break;
                        }
                    }
                } else {
                    for (i in obj) {
                        value = callback.call(obj[i], i, obj[i]);

                        if (value === false) {
                            break;
                        }
                    }
                }
            }

            return obj;
        },

        extend: function (target) {
            var i, options, name;

            for (i = 1; i < arguments.length; i++) {
                if ((options = arguments[i]) != null) {
                    for (name in options) {
                        target[name] = options[name];
                    }
                }
            }

            return target;
        },
        on: function (elem, type, fn) {
            if (elem.addEventListener) {
                elem.addEventListener(type, fn, false);
            } else if (elem.attachEvent) {
                elem.attachEvent("on" + type, fn);
            }
        },
        off: function (elem, type, fn) {
            if (elem.removeEventListener) {
                elem.removeEventListener(type, fn, false);
            } else if (elem.detachEvent) {
                elem.detachEvent("on" + type, fn);
            }
        },

        log: function (level, args) {
            if (window.console) {
                var logger = window.console[level];
                if (typeof logger === 'function') {
                    logger.apply(window.console, args);
                }
            }
        },

        warn: function () {
            atmosphere.util.log('warn', arguments);
        },

        info: function () {
            atmosphere.util.log('info', arguments);
        },

        debug: function () {
            atmosphere.util.log('debug', arguments);
        },

        error: function () {
            atmosphere.util.log('error', arguments);
        },
        xhr: function () {
            try {
                return new window.XMLHttpRequest();
            } catch (e1) {
                try {
                    return new window.ActiveXObject("Microsoft.XMLHTTP");
                } catch (e2) {
                }
            }
        },
        parseJSON: function (data) {
            return !data ? null : window.JSON && window.JSON.parse ? window.JSON.parse(data) : new Function("return " + data)();
        },
        // http://github.com/flowersinthesand/stringifyJSON
        stringifyJSON: function (value) {
            var escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g, meta = {
                '\b': '\\b',
                '\t': '\\t',
                '\n': '\\n',
                '\f': '\\f',
                '\r': '\\r',
                '"': '\\"',
                '\\': '\\\\'
            };

            function quote(string) {
                return '"' + string.replace(escapable, function (a) {
                        var c = meta[a];
                        return typeof c === "string" ? c : "\\u" + ("0000" + a.charCodeAt(0).toString(16)).slice(-4);
                    }) + '"';
            }

            function f(n) {
                return n < 10 ? "0" + n : n;
            }

            return window.JSON && window.JSON.stringify ? window.JSON.stringify(value) : (function str(key, holder) {
                var i, v, len, partial, value = holder[key], type = typeof value;

                if (value && typeof value === "object" && typeof value.toJSON === "function") {
                    value = value.toJSON(key);
                    type = typeof value;
                }

                switch (type) {
                    case "string":
                        return quote(value);
                    case "number":
                        return isFinite(value) ? String(value) : "null";
                    case "boolean":
                        return String(value);
                    case "object":
                        if (!value) {
                            return "null";
                        }

                        switch (Object.prototype.toString.call(value)) {
                            case "[object Date]":
                                return isFinite(value.valueOf()) ? '"' + value.getUTCFullYear() + "-" + f(value.getUTCMonth() + 1) + "-"
                                + f(value.getUTCDate()) + "T" + f(value.getUTCHours()) + ":" + f(value.getUTCMinutes()) + ":" + f(value.getUTCSeconds())
                                + "Z" + '"' : "null";
                            case "[object Array]":
                                len = value.length;
                                partial = [];
                                for (i = 0; i < len; i++) {
                                    partial.push(str(i, value) || "null");
                                }

                                return "[" + partial.join(",") + "]";
                            default:
                                partial = [];
                                for (i in value) {
                                    if (hasOwn.call(value, i)) {
                                        v = str(i, value);
                                        if (v) {
                                            partial.push(quote(i) + ":" + v);
                                        }
                                    }
                                }

                                return "{" + partial.join(",") + "}";
                        }
                }
            })("", {
                "": value
            });
        },

        checkCORSSupport: function () {
            if (atmosphere.util.browser.msie && !window.XDomainRequest && +atmosphere.util.browser.version.split(".")[0] < 11) {
                return true;
            } else if (atmosphere.util.browser.opera && +atmosphere.util.browser.version.split(".") < 12.0) {
                return true;
            }

            // KreaTV 4.1 -> 4.4
            else if (atmosphere.util.trim(navigator.userAgent).slice(0, 16) === "KreaTVWebKit/531") {
                return true;
            }
            // KreaTV 3.8
            else if (atmosphere.util.trim(navigator.userAgent).slice(-7).toLowerCase() === "kreatel") {
                return true;
            }

            // Force older Android versions to use CORS as some version like 2.2.3 fail otherwise
            var ua = navigator.userAgent.toLowerCase();
            var androidVersionMatches = ua.match(/.+android ([0-9]{1,2})/i),
                majorVersion = parseInt((androidVersionMatches && androidVersionMatches[0]) || -1, 10);
            if (!isNaN(majorVersion) && majorVersion > -1 && majorVersion < 3) {
                return true;
            }
            return false;
        }
    };

    guid = atmosphere.util.now();

    // Browser sniffing
    (function () {
        var ua = navigator.userAgent.toLowerCase(),
            match = /(chrome)[ \/]([\w.]+)/.exec(ua) ||
                /(opera)(?:.*version|)[ \/]([\w.]+)/.exec(ua) ||
                /(msie) ([\w.]+)/.exec(ua) ||
                /(trident)(?:.*? rv:([\w.]+)|)/.exec(ua) ||
                ua.indexOf("android") < 0 && /version\/(.+) (safari)/.exec(ua) ||
                ua.indexOf("compatible") < 0 && /(mozilla)(?:.*? rv:([\w.]+)|)/.exec(ua) ||
                [];

        // Swaps variables
        if (match[2] === "safari") {
            match[2] = match[1];
            match[1] = "safari";
        }
        atmosphere.util.browser[match[1] || ""] = true;
        atmosphere.util.browser.version = match[2] || "0";
        atmosphere.util.browser.vmajor = atmosphere.util.browser.version.split(".")[0];

        // Trident is the layout engine of the Internet Explorer
        // IE 11 has no "MSIE: 11.0" token
        if (atmosphere.util.browser.trident) {
            atmosphere.util.browser.msie = true;
        }

        // The storage event of Internet Explorer and Firefox 3 works strangely
        if (atmosphere.util.browser.msie || (atmosphere.util.browser.mozilla && +atmosphere.util.browser.version.split(".")[0] === 1)) {
            atmosphere.util.storage = false;
        }
    })();

    atmosphere.util.on(window, "unload", function (event) {
        atmosphere.util.debug(new Date() + " Atmosphere: " + "unload event");
        atmosphere.unsubscribe();
    });

    atmosphere.util.on(window, "beforeunload", function (event) {
        atmosphere.util.debug(new Date() + " Atmosphere: " + "beforeunload event");

        // ATMOSPHERE-JAVASCRIPT-143: Delay reconnect to avoid reconnect attempts before an actual unload (we don't know if an unload will happen, yet)
        atmosphere._beforeUnloadState = true;
        setTimeout(function () {
            atmosphere.util.debug(new Date() + " Atmosphere: " + "beforeunload event timeout reached. Reset _beforeUnloadState flag");
            atmosphere._beforeUnloadState = false;
        }, 5000);
    });

    // Pressing ESC key in Firefox kills the connection
    // for your information, this is fixed in Firefox 20
    // https://bugzilla.mozilla.org/show_bug.cgi?id=614304
    atmosphere.util.on(window, "keypress", function (event) {
        if (event.charCode === 27 || event.keyCode === 27) {
            if (event.preventDefault) {
                event.preventDefault();
            }
        }
    });

    atmosphere.util.on(window, "offline", function () {
        atmosphere.util.debug(new Date() + " Atmosphere: offline event");
        offline = true;
        if (requests.length > 0) {
            var requestsClone = [].concat(requests);
            for (var i = 0; i < requestsClone.length; i++) {
                var rq = requestsClone[i];
                if(rq.request.handleOnlineOffline) {
                    rq.close();
                    clearTimeout(rq.response.request.id);

                    if (rq.heartbeatTimer) {
                        clearTimeout(rq.heartbeatTimer);
                    }
                }
            }
        }
    });

    atmosphere.util.on(window, "online", function () {
        atmosphere.util.debug(new Date() + " Atmosphere: online event");
        if (requests.length > 0) {
            for (var i = 0; i < requests.length; i++) {
                if(requests[i].request.handleOnlineOffline) {
                    requests[i].init();
                    requests[i].execute();
                }
            }
        }
        offline = false;
    });

    return atmosphere;
}));
/* jshint eqnull:true, noarg:true, noempty:true, eqeqeq:true, evil:true, laxbreak:true, undef:true, browser:true, indent:false, maxerr:50 *//* ---------- *//*
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

var od = od || {};

// Like OpenDevice JAVA-API
od.DeviceType = {
    DIGITAL:1,
    ANALOG:2,
    ANALOG_SIGNED:3,
    NUMERIC:4,
    FLOAT2:5,
    FLOAT2_SIGNED:6,
    FLOAT4:7,
    CHARACTER:8,
    BOARD:10,
    MANAGER:11,

    isAnalog : function(type){
        return type == od.DeviceType.ANALOG
        || type == od.DeviceType.FLOAT2
        || type == od.DeviceType.FLOAT4
        || type == od.DeviceType.FLOAT2_SIGNED
    }
};

// Like OpenDevice JAVA-API
od.DeviceCategory = {
    LAMP:1,
    FAN:2,
    GENERIC:3,
    POWER_SOURCE : 4,
    GENERIC_SENSOR: 50,
    IR_SENSOR: 51
};



od.CommandType = {
    DIGITAL:1,
    ANALOG:2,
    NUMERIC:3,
    GPIO_DIGITAL:4,
    GPIO_ANALOG:5,
    INFRA_RED:6,

    /** Response to commands like: DIGITAL, POWER_LEVEL, INFRA RED  */
    DEVICE_COMMAND_RESPONSE : 10, // Responsta para comandos como: DIGITAL, POWER_LEVEL, INFRA_RED
    COMMAND_RESPONSE : 11,
    SET_PROPERTY:12,
    ACTION:13,

    PING_REQUEST            :20,
    PING_RESPONSE           :21,
    DISCOVERY_REQUEST       :22,
    DISCOVERY_RESPONSE      :23,
    MEMORY_REPORT           :24,
    CPU_TEMPERATURE_REPORT  :25,
    CPU_USAGE_REPORT        :26,


    GET_DEVICES             :30,
    GET_DEVICES_RESPONSE    :31,
    DEVICE_SAVE             :32,
    DEVICE_SAVE_RESPONSE	:33,
    DEVICE_DEL              :34,
    CLEAR_DEVICES           :35,
    SYNC_DEVICES_ID 		:36,
    SYNC_HISTORY         	:37,
    FIRMWARE_UPDATE         :38,

    GET_CONNECTIONS         :40,
    GET_CONNECTIONS_RESPONSE:41,
    CONNECTION_ADD          :42,
    CONNECTION_ADD_RESPONSE :43,
    CONNECTION_DEL          :44,
    CLEAR_CONNECTIONS       :45,
    CONNECT 		        :46,
    CONNECT_RESPONSE 		:47,

    USER_EVENT              :98,
    USER_COMMAND            :99,

    isDeviceCommand : function(type){
        switch (type) {
            case this.DIGITAL:
                return true;
            case this.ANALOG:
                return true;
            case this.NUMERIC:
                return true;
            default:
                break;
        }
        return false;
    }
};


od.CommandStatus = {
    DELIVERED           :1,
    RECEIVED            :2,
    FAIL                :3,
    EMPTY_DATABASE      :4,
    // Response...
    SUCCESS             :200,
    NOT_FOUND           :404,
    BAD_REQUEST         :400,
    UNAUTHORIZED        :401,
    FORBIDDEN           :403,
    PERMISSION_DENIED   :550,
    INTERNAL_ERROR      :500,
    NOT_IMPLEMENTED     :501
};


od.Event = {
    DEVICE_LIST_UPDATE : "devicesUpdate",
    DEVICE_CHANGED : "deviceChanged",
    CONNECTION_CHANGE : "connectionChange",
    CONNECTED : "connected",
    DISCONNECTED : "disconnected",
    LOGIN_FAILURE : "loginFail"
};/*
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

/** @namespace */
var od = od || {};


/**
 * Represent a Device
 * @param data - JSON
 * @constructor
 */
od.Device = function(data){

    // Private
    var CType = od.CommandType;
    var _this = this;

    // Public
    this.type = od.DeviceType.DIGITAL;
    this.listeners = [];

    function _init(data){

        this.id = data.id;
        this.manager = od.deviceManager;

        // Dynamic Properties and Funtions
        this.updateRawData(data);
    }

    /**
     * Update dynamic Properties and Funtions
     * @param data
     */
    this.updateRawData = function(data){

        for (var attrname in data) this[attrname] = data[attrname];

        for (var property in this.properties) this[property] = this.properties[property];

        if(!this.description) this.description = this.name;

        this.actions.forEach(function(method) {
            _this[method] = function(){
                console.log('Calling remote action: ' + method + ", params: ", arguments);
                var paramlist = [];
                for(var i in arguments) paramlist.push(arguments[i]);
                _this.manager.send({type : CType.ACTION, deviceID : _this.id, action : method, params : paramlist });
            }
        });
    };

    function notifyListeners(){

    }

    this.on = function(){
         this.setValue(1, true);
    };

    this.off = function(){
         this.setValue(0, true);
    };

    this.isON = function(){
        return (this.value == 1)
    };

    this.isOFF = function(){
        return (this.value == 0)
    };

    this.setValue = function(value, sync){

        sync = typeof sync !== 'undefined' ? sync : true; // default true

        // // Do data conversions
        // if(this.type == od.DeviceType.FLOAT2){
        //     value = value / 100;
        // }else if(this.type == od.DeviceType.FLOAT4){
        //     value = value / 10000;
        // }else if(this.type == od.DeviceType.FLOAT2_SIGNED){
        //     value = value / 100;
        // }else if(this.type == od.DeviceType.ANALOG_SIGNED){
        //     alert("ANALOG_SIGNED - conversion not implemented");
        // }

        // Only fire events if change... (or is Numeric (RFID/etc..))
        if(this.type == od.DeviceType.NUMERIC || this.value != value){

            this.value = value;
            this.lastUpdate = new Date().getTime();

            if(this.manager){
                this.manager.notifyDeviceListeners(this, sync);
            }

        }

    };

    this.toggle = function(){
        var value = 0;
        if(this.value == 0) value = 1;
        else if(this.value == 1) value = 0;
        this.setValue(value);
    };

    /** @deprecated */
    this.toggleValue = this.toggle;

    /**
     * Register a listener to monitor changes in this Device.
     * @param {function} listener - Receive params VALUE, ID
     * @param {Object} [context] - Context to execute listener
     * @returns {{context: *, listener: *}} - return registred listener (used in #removeListener)
     */
    this.onChange = function(listener, context){
        var eventDef = {"context":context, "listener" : listener};
        this.listeners.push(eventDef);
        return eventDef;
    };

    /**
     *
     * @param eventDef {{context: *, listener: *}}
     */
    this.removeListener = function(eventDef){
        var index = this.listeners.indexOf(eventDef);
        if(index >= 0){
            this.listeners.splice(index, 1);
        }
    };

    this.applyChanges = function(device){
        for (var attr in device) {
            if (device.hasOwnProperty(attr) &&  typeof device[attr] != "function" )
                this[attr] = device[attr];
        }
    };

    // Initialize device data.
    _init.call(this, data);
};/*
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

        _this.status = status;

        for(var i = 0; i<listeners.length; i++){
            var listener = listeners[i]["connectionStateChanged"];
            if (typeof listener === "function") {
                listener(_this, status, _this.status);
            }
        }

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

};/*
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

/** global instance. @type {{od.DeviceManager}} */
od.deviceManager = {};


/**
 * DeviceManager
 * @param {DeviceConnection} connection (Optional)
 * @constructor
 */
od.DeviceManager = function(connection){
    var _this = this;
    var initialized = false;

    od.deviceManager = this; // set global reference
    // Alias
    var DEvent = od.Event;
    var CType = od.CommandType;
    var DeviceType = od.DeviceType;

    // Private
    var types = [];
    var storage = new od.DeviceStorage();

    var listenersMap = {};
    var listenerReceiver = []; // current listerners (for single page model)

    // public
    this.connection = connection || od.connection;


    function init(){
        _this.connection.addListener({
            "onMessageReceived" : _onMessageReceived,
            "connectionStateChanged" : _connectionStateChanged
        });
    }

    this.setValue = function(deviceID, value){

        var device = _this.findDevice(deviceID);

        if(device){
            device.setValue(value);
        }

    };

    this.toggleValue = function(deviceID){

        var device = _this.findDevice(deviceID);

        if(device && ! device.sensor){
            device.toggle();
        }

    };

    this.send = function(cmd){
        _this.connection.send(cmd);
    };

    this.addDevice = function(){
        // Isso teria no final que salvar na EPROM/Servidor do arduino.
    };


    this.removeDevice = function(device){

        return ODev.devices.delete(device.id, function(){

            var devices = _this.getDevices();

            var index = devices.indexOf(device);
            if(index >= 0) devices.splice(index, 1);

            // TODO: if is a board need remove chids.
            notifyListeners(DEvent.DEVICE_LIST_UPDATE, devices);
        });

    };

    this.deleteHitory = function(device){
        return ODev.devices.deleteHistory(device.id);
    };

    this.getDevices = function(){

        var devices = storage.getDevices();

        if(devices && devices.length > 0){
            initialized = true;
            return devices; // return from cache...
        }

        devices = this.sync(/*notify=*/false); // load remote

        return devices;
    };

    this.getDevicesByType = function(type, devices){

        if(!devices) devices = this.getDevices();

        var found = [];

        if(devices) devices.forEach(function(device){
            if(device.type == type) found.push(device);
        });

        return found;
    };

    this.getDevicesByBoard = function(boardID){

        var devices = this.getDevices();

        var found = [];

        if(devices) devices.forEach(function(device){
            if(device.parentID == boardID && device.type != DeviceType.BOARD) found.push(device);
        });

        return found;
    };


    this.getBoards = function(){
        return this.getDevicesByType(DeviceType.BOARD);
    };

    this.getTypes = function(){

        if(types.length == 0){
            ODev.devices.listTypes(function(response){
                if(response.length > 0){
                    response.forEach(function(item){ types.push(item)})
                }
            });
        }

        return types;
    };

    /**
     * Find device by ID in List or in currently loaded devices
     * @param deviceID
     * @param deviceList (Optional) if not provide, current devices are considered
     * @returns {*}
     */
    this.findDevice = function(deviceID, deviceList){

        if(!deviceList) deviceList = this.getDevices();

        if(deviceList){
            for(var i = 0; i < deviceList.length; i++){
                if(deviceList[i].id == deviceID){
                    return deviceList[i];
                }
            }
        } else{
            console.warn("Devices not loaded or empty !");
        }

        return null;
    };

    /**
     * Sync Devices with server
     * @param {Boolean} notify - if true notify listeners
     * @param {Boolean} forceSync - force sync with physical modules ( async )
     * @returns {Array}
     */
    this.sync = function(notify, forceSync){

        // load remote.
        var devices = _getDevicesRemote();

        // force sync (send GetDeviceRequest for all physical devices)
        if(forceSync || (devices && devices.length == 0)) {
            _this.send({type : CType.GET_DEVICES, forceSync : forceSync});
        }

        // Map devices and Parents
        if(devices){
            devices.forEach(function(item){
                if(item.parentID){
                    item.parent = _this.findDevice(item.parentID, devices);
                }
            });
        }

        if(notify === true) notifyListeners(DEvent.DEVICE_LIST_UPDATE, devices);

        return devices;
    };

    /**
     * Save or update device
     * @device
     * @param {function(status)} callback executed when device is saved.
     */
    this.save = function(device, callback){
        ODev.send({
            type : CType.DEVICE_SAVE, device : device
        });

        var found = _this.findDevice(device.id);

        // TODO: may be best wait the response before fire listeners
        if(found != null){
            found.applyChanges(device);
            _this.notifyDeviceListeners(found, /*sync=*/false);
            if(callback) callback.call(found, true);
        }

    };

    /**
     * Shortcut to {@link addListener}
     */
    this.on = function(event, listener){
        return _this.addListener(event, listener);
    };

    // FIXME: rename to onChange
    this.onDeviceChange = function (listener){
        _this.addListener(od.Event.DEVICE_CHANGED, listener);
    };

    this.onConnect = function (listener){
        if(_this.isConnected()){
            var devices = OpenDevice.getDevices();
            if(listener) listener(devices);
            return {event : od.Event.CONNECTED}; // fake listener
        }else{
            return this.on(od.Event.CONNECTED, function(){
                var devices = OpenDevice.getDevices();
                if(listener) listener(devices);
            });
        }
    };

    /**
     * Remove listener
     * @param {( string|Object[]|{event: *, listener: *})} eventDef  -  Event name (String) or Object
     * @param {function} [listener]
     */
    this.removeListener = function(eventDef, listener){

        var event;
        if(eventDef instanceof Array) {

            if(listener == listenerReceiver) listenerReceiver = null; // clear temporary listeners

            for (var i = 0; i < eventDef.length; i++) {
                var def = eventDef[i];
                this.removeListener(def);
            }
        } else if(typeof eventDef == "object") {
            event = eventDef["event"];
            listener = eventDef["listener"];
        }else{
            event =  eventDef;
        }

        if(listenersMap[event] != null){
            var listeners = listenersMap[event];
            var index = listeners.indexOf(listener);
            if(index >= 0) listeners.splice(index, 1);
        }
    };

    /**
     *
     * @param event
     * @param listener
     * @returns {{event: *, listener: *}} Listener definition (used in removeListener)
     */
    this.addListener = function(event, listener){

        if(listenersMap[event] === undefined) listenersMap[event] = [];
        listenersMap[event].push(listener);

        var eventlistener = { "event" : event, "listener" : listener };

        // See: setListenerReceiver
        if(listenerReceiver) listenerReceiver.push(eventlistener);

        return eventlistener;
    };

    /**
     * Defines a list where temporary listeners will be registered. Useful when you are working on a single page application
     * @param listeners
     * @returns {{event: Event, listener: *}}
     */
    this.setListenerReceiver = function(listeners){

        listenerReceiver = listeners;
    };

    /**
     * Check if device is in the list passed by parameter or in internal list
     * @param device
     * @param list (Optional)
     * @returns {boolean}
     */
    this.contains = function(device, list){
        if(list == null) list = _this.getDevices();

        for(var i = 0; i<list.length; i++){

            if(typeof list[i] == "object"){
                if(device.id == list[i].id){
                    return true;
                }
            }else{
                if(device.id == list[i]){
                    return true;
                }
            }
        }

        return false;
    };

    this.isConnected = function(){
        return _this.connection.isConnected();
    };

    this.notifyDeviceListeners = function(device, sync){

        if(sync){
            var cmd = { 'type' : device.type , 'deviceID' :  device.id, 'value' : device.value};
            _this.connection.send(cmd);
        }

        // Notify Individual Listeners
        for (var i = 0; i < device.listeners.length; i++) {

            if(typeof device.listeners[i] == "function"){
                device.listeners[i](device.value, device.id);
            }else{
                var listener = device.listeners[i]["listener"];
                listener.call(device.listeners[i]["context"], device.value, device.id);
            }

        }

        // Notify Global Listeners
        notifyListeners(DEvent.DEVICE_CHANGED, device);

    };

    this.notifyListeners = function(event, data){
        notifyListeners(event, data);
    };

    function notifyListeners(event, data){

        if(! (listenersMap[event] === undefined)){ // has listeners for this event

            var listeners = listenersMap[event];

            for(var i = 0; i<listeners.length; i++){
                if (typeof listeners[i] === "function") {
                    try{
                        listeners[i](data);
                    }catch (error){
                        console.log(error);
                    }

                }
            }

        }
    }

    /**
     *
     * @returns Array[]
     * @private
     */
    function _getDevicesRemote(){

        var response = OpenDevice.devices.list(); // rest !

        var devices = [];

        if(response.length > 0){
            storage.updateDevices(response);
        }

        for(var i = 0; i < response.length; i++ ){
            var device = new od.Device(response[i]);
            if(typeof Object.observe != "undefined"){
                Object.observe(device, _onPropertyChange);
            }else console.warn("Object.observe not supported in this browser.");
            devices.push(device);
        }

        initialized = true;

        return devices;
    }

    /**
     *
     * @param event
     * @private
     */
    function _onPropertyChange(event){

        if(event.length > 0 && event[0].type == "update" && event[0].name != "value"){

            var device = event[0].object;

            _this.send({type : CType.SET_PROPERTY, deviceID : device.id, property : event[0].name, value : device[event[0].name] });
        }

    }

    /**
     * @private
     */
    function _onMessageReceived(conn, message){

        // HACK: Bug in broadcast(atmosphere), is sending back same command.
        if(CType.isDeviceCommand(message.type) && conn.getConnectionUUID() == message.connectionUUID ){
            return;
        }

        //  Not Logged
        if (message.type == CType.CONNECT_RESPONSE && message.status == od.CommandStatus.UNAUTHORIZED){
            notifyListeners(DEvent.LOGIN_FAILURE, od.CommandStatus.UNAUTHORIZED);
            return;
        }

        //  Custom User Event
        if (message.type == CType.USER_EVENT){
            notifyListeners(message.name, message);
            return;
        }

        //od.CommandType.CONNECT_RESPONSE

        // Device changed in another client..
        if(CType.isDeviceCommand(message.type)){
            // console.log("Device changed in another client..");

            var device = _this.findDevice(message.deviceID);

            if(device){
                device.setValue(message.value, false);
            }
        }

        // Force load new list from server
        // TODO: It would be interesting if the devices list were already in response
        if(message.type == CType.GET_DEVICES_RESPONSE){
            // load remote.
            var devices = _getDevicesRemote();

            if(devices && devices.length > 0) notifyListeners(DEvent.DEVICE_LIST_UPDATE, devices);
        }

    }


    function _connectionStateChanged(conn, newStatus, oldStatus){
        console.log("DeviceManager._connectionStateChanged :" + newStatus);

        notifyListeners(DEvent.CONNECTION_CHANGE, newStatus);

        if(od.ConnectionStatus.CONNECTED == newStatus){
            notifyListeners(DEvent.CONNECTED, _this.getDevices());
        }

        if(od.ConnectionStatus.DISCONNECTED == newStatus){
            notifyListeners(DEvent.DISCONNECTED);
        }


    }

    init(); //
};/*
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


/**
 * Handle device cache storage
 * @param {DeviceConnection} connection (Optional)
 * @constructor
 */
od.DeviceStorage = function(){

    var _this = this;
    //var initialized = false;

    // Private
    var devices = []; // od.Device

    this.getDevices = function(){

        // return from MEMORY cache...
        if(devices.length > 0) return devices;

        // return from storage
        // var data  = localStorage.getItem(od.DEVICES_STORAGE_ID);
        // if(data){
        //     devices = convertDevice(JSON.parse(data));
        //     return devices;
        // }

        return devices;
    };

    /**
     * Update devices
     * @param data - Row device data
     */
    this.updateDevices = function(response){

        localStorage.setItem(od.DEVICES_STORAGE_ID,  JSON.stringify(response));

        sync(response);
    };

    function sync(list){
        for (var i = 0; i < list.length; i++) {
            var obj = list[i];
            var device = find(obj.id);

            // New
            if(!device){
                devices.push(new od.Device(obj));
            }else{
                device.updateRawData(obj);
            }
        }
    }

    function find(deviceID){
        for (var i = 0; i < devices.length; i++) {
            var device = devices[i];
            if(device.id == deviceID){
                return device;
            }
        }
    }

    function convertDevice(response){
        var devices = [];
        for(var i = 0; i < response.length; i++ ){
            var device = new od.Device(response[i]);
            devices.push(device);
        }
        return devices;
    }
};/*
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

var od = od || {};

od.SESSION_ID = "AuthToken"; // For cookie/localstore search
od.DEVICES_STORAGE_ID = "odev_devices"; //

od.version = "0.3.2";
od.appID = "*"; // ApyKey Value
od.serverURL = window.location.origin;

var OpenDevice = (function () {

    var connection = new od.DeviceConnection({logLevel : 'debug'});
    var manager = new od.DeviceManager(connection);

// Exported Methods / Vars
return {

    appID : od.appID,
    serverURL : od.serverURL,
    manager : manager,

    // Manager delegate
    on : manager.on,
    removeListener : manager.removeListener,
    notifyListeners : manager.notifyListeners,
    setListenerReceiver : manager.setListenerReceiver,
    onDeviceChange : manager.onDeviceChange,
    onChange : manager.onDeviceChange,
    onConnect : manager.onConnect,
    isConnected : manager.isConnected,
    findDevice : manager.findDevice,
    get : manager.findDevice,
    removeDevice : manager.removeDevice,
    deleteHitory : manager.deleteHitory,
    getDevices : manager.getDevices,
    getDevicesByType : manager.getDevicesByType,
    getDevicesByBoard : manager.getDevicesByBoard,
    getBoards : manager.getBoards,
    getTypes : manager.getTypes,
    setValue : manager.setValue,
    toggleValue : manager.toggleValue,
    contains : manager.contains,
    sync : manager.sync,
    save : manager.save,
    send : manager.send,

    setAppID : function(appID){
        od.appID = appID;
    },

    setApyKey : function(appID){
        od.appID = appID;
    },

    setServer : function(serverURL){
        od.serverURL = serverURL;
    },

    connect : function(_conn){
        if(_conn) connection = _conn;
        connection.connect();
    },

    // TODO: try do Rest over WS
    rest : function(path, options){

        var request = {
            type: "GET",
            url: od.serverURL + path,
            headers : {
                'Authorization' : "ApiKey " + od.appID
            },
            async: false // FIXME: isso não é recomendado...
        };

        $.extend(request, options || {});

        var response = $.ajax(request);

        response.fail(function(){
            console.error("Rest fail, status ("+response.status+"): " + response.responseText );
            if(response.status == 401){
                manager.notifyListeners(od.Event.LOGIN_FAILURE, od.CommandStatus.UNAUTHORIZED);
            }
        });

        // TODO: fazer tratamento dos possíveis erros (como exceptions e servidor offline ou 404)

        // For async = false
        if(request.async == null || request.async == false){
            if(response.status == 200 && response.responseText.length > 0){
                return JSON.parse(response.responseText)
            }else{
                return null;
            }
        }else{
            return response;
        }

    },


    history : function(query, callback, errorCallback){
        jQuery.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Authorization' : "Bearer " + od.appID
            },
            type: 'POST',
            url: od.serverURL + OpenDevice.devices.path + "/" + query.deviceID + "/history",
            data: JSON.stringify(query),
            dataType: 'json',
            async: true,
            success: callback,
            error : errorCallback
        });
    },


    logout : function(callback){
        return $.get(od.serverURL +"/api/auth/logout", callback);
    },

    /** Try to find APPID(AuthToken) URL->Cookie->LocalStore */
    findAppID : function(){

        /** Get URL query param */
        function getQueryParam(name){
            var qs = (function(a) {
                if (a == "") return {};
                var b = {};
                for (var i = 0; i < a.length; ++i)
                {
                    var p=a[i].split('=', 2);
                    if (p.length == 1)
                        b[p[0]] = "";
                    else
                        b[p[0]] = decodeURIComponent(p[1].replace(/\+/g, " "));
                }
                return b;
            })(window.location.search.substr(1).split('&'));

            return qs[name];
        }

        /** Get URL query param */
        function getCookie(name) {
          var value = "; " + document.cookie;
          var parts = value.split("; " + name + "=");
          if (parts.length == 2) return parts.pop().split(";").shift();
        }


        od.appID = getQueryParam(od.SESSION_ID);

        if(od.appID != null) return od.appID;

        od.appID = getCookie(od.SESSION_ID);

        if(od.appID != null) return od.appID;

        if( window.localStorage ){
            od.appID = window.localStorage.getItem(od.SESSION_ID)
        }

        return od.appID;
    }


};

})();


var ODev = OpenDevice;

/**
 * REST Interface: Devices
 */

OpenDevice.devices = {

    path : "/api/devices",

    get : function(deviceID){
        return OpenDevice.rest(this.path + "/" + deviceID);
    },

    value : function(deviceID, value){

        if(value != null){
            return OpenDevice.rest(this.path + "/" + deviceID + "/value/" + value);
        }else{
            return OpenDevice.rest(this.path + "/" + deviceID + "/value");
        }
    },

    list : function(){
        return OpenDevice.rest(this.path + "/");
    },

    listTypes : function(callback, errorCallback){
        return OpenDevice.rest(this.path + "/types", { async : true, success : callback, error : errorCallback});
    },

    sync : function(){
        return OpenDevice.rest(this.path + "/sync");
    },

    delete : function(uid, callback, errorCallback){
        return OpenDevice.rest(this.path + "/" + uid, { async : true, type : "DELETE", success : callback, error : errorCallback});
    },

    deleteHistory : function(uid, callback, errorCallback){
        return OpenDevice.rest(this.path + "/" + uid + "/history", { async : true, type : "DELETE", success : callback, error : errorCallback});
    }

};
