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

package br.com.criativasoft.opendevice.webclient;

import br.com.criativasoft.opendevice.connection.AbstractConnection;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.IWSConnection;
import br.com.criativasoft.opendevice.connection.ReconnectionSupport;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.webclient.io.CommandEncoderDecoder;
import org.atmosphere.wasync.*;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.atmosphere.wasync.impl.DefaultOptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Ricardo JL Rufino
 * @date 10/07/14.
 */
public class WebSocketClientConnection extends AbstractConnection implements ReconnectionSupport, IWSConnection{

    private String url;
    private Socket connection;
    private RequestBuilder request;

    private static final Logger log = LoggerFactory.getLogger(WebSocketClientConnection.class);

    public WebSocketClientConnection(){

    }

    public WebSocketClientConnection(String url) {
        setConnectionURI(url);
    }

    @Override
    public void setConnectionURI(String uri) {
        this.url = buildUrl(uri);
    }

    private String buildUrl(String uri){
        String id = TenantProvider.getCurrentID();

        if(uri.startsWith("http")){
            uri = uri.replace("http", "ws");
        }

        if(!uri.startsWith("ws")){
            uri = "ws://" + uri;
        }

        if(!uri.contains("/device/connection/")){
            uri = uri + "/device/connection/" + id;
        }

        return uri;
    }

    @Override
    public String getConnectionURI() {
        return  this.url;
    }

    @Override
    public void connect() throws ConnectionException {
        try {
            log.debug("connecting...");

            if(!isConnected()){

                initConnection(); // Setup

                connection.open(request.build());

                // setStatus(ConnectionStatus.CONNECTED); NOTE: is fired in Events

            }

        } catch (IOException e) {
            // setStatus(ConnectionStatus.FAIL); (fired on 'connection.on(new Function<IOException>() {')
            throw new ConnectionException(e);
        }

    }

    public void reconnectTo(String url) throws ConnectionException{
        log.debug("re-connect to: " + this.url);

        if(isConnected()) disconnect();

        this.url = url;

        connect();

    }

    @Override
    public void disconnect() throws ConnectionException {
        log.debug("disconnecting... (isConnected: "+isConnected()+")");

        if(isConnected()){
            // Send CLOSE request..
            connection.close();  // will fire event CLOSE on 'wrapped connection'
            setStatus(ConnectionStatus.DISCONNECTING);
        }else{ // set 'disconnected' in case of previous connection fail.
            setStatus(ConnectionStatus.DISCONNECTED);
        }
        connection = null;
    }

    @Override
    public boolean isConnected() {
        return connection != null && (connection.status() == Socket.STATUS.OPEN || connection.status() == Socket.STATUS.REOPENED);
    }

    @Override
    public void send(Message message) throws IOException {
        if(isConnected()){
            connection.fire(message);
        }else{
            log.warn("Can't send command, not Connected !");
        }
    }

    private void initConnection() throws IOException{
        if(connection == null){
            // AtmosphereClient client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
            AtmosphereClient client = ClientFactory.getDefault().newClient(AtmosphereClient.class);

            // FIXME: Workaround for BUG: https://github.com/Atmosphere/wasync/issues/120
            System.setProperty("com.ning.http.client.AsyncHttpClientConfig.acceptAnyCertificate", "true");

            request = client.newRequestBuilder()
                    .method(Request.METHOD.GET)
                    .uri(url)
                    // .trackMessageLength(true)
                    .transport(Request.TRANSPORT.WEBSOCKET)
                    .transport(Request.TRANSPORT.LONG_POLLING);

            CommandEncoderDecoder jacksonSerializer = new CommandEncoderDecoder();
            request.encoder(jacksonSerializer);
            request.decoder(jacksonSerializer);
            DefaultOptionsBuilder clientOptions = client.newOptionsBuilder()
                    .reconnect(true)
                    .reconnectAttempts(10)
                    .pauseBeforeReconnectInSeconds(5);


            // clientOptions.runtime().getConfig().isAcceptAnyCertificate()

            connection = client.create(clientOptions.build());
            initEvents(connection);
        }
    }

    private void initEvents(final Socket connection){

        connection.on(Event.CLOSE, new Function<String>() {
            public void on(String t) {
                setStatus(ConnectionStatus.DISCONNECTED);
            }
        });
        connection.on(Event.REOPENED, new Function<String>() {
            public void on(String t) {
                setStatus(ConnectionStatus.CONNECTED);
            }
        });
        connection.on(Event.OPEN, new Function<String>() {
            public void on(String t) {
                setStatus(ConnectionStatus.CONNECTED);
            }
        });

        connection.on(new Function<IOException>() {
            public void on(IOException ioe) {
                setStatus(ConnectionStatus.FAIL);
                ioe.printStackTrace();
                try {
                    disconnect();
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
        });


        connection.on(Event.MESSAGE, new Function<Command>() {
            public void on(Command cmd) {
                notifyListeners(cmd);
            }
        });
    }


}
