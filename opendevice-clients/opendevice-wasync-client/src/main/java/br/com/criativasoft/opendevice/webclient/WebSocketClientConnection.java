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
import br.com.criativasoft.opendevice.connection.ReconnectionSupport;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.webclient.io.CommandEncoderDecoder;
import org.atmosphere.wasync.*;

import java.io.IOException;

/**
 * @autor Ricardo JL Rufino
 * @date 10/07/14.
 */
public class WebSocketClientConnection extends AbstractConnection implements ReconnectionSupport {

    private String url;
    private Socket connection;

    public WebSocketClientConnection(String url) {
        this.url = url;
    }

    @Override
    public void connect() throws ConnectionException {
        try {
            log.debug("connecting...");
            if(!isConnected()){

                initConnection(); // Setup

                // server.start();
                log.debug("internal socket server [ok]");

                setStatus(ConnectionStatus.CONNECTED);


            }

        } catch (IOException e) {
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
        log.debug("disconnecting... (isConnected: + "+isConnected()+")");

        if(connection != null && isConnected()){
            connection.close();  // will fire event on 'conection'
            setStatus(ConnectionStatus.DISCONNECTING);
        }else{ // set 'disconnected' in case of previous connection fail.
            setStatus(ConnectionStatus.DISCONNECTED);
        }
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
            Client client = ClientFactory.getDefault().newClient();

            RequestBuilder request = client.newRequestBuilder()
                    .method(Request.METHOD.GET)
                    .uri(url)
                    // .trackMessageLength(true)
                    .transport(Request.TRANSPORT.WEBSOCKET);

            CommandEncoderDecoder jacksonSerializer = new CommandEncoderDecoder();
            request.encoder(jacksonSerializer);
            request.decoder(jacksonSerializer);

            connection = client.create();
            initEvents(connection);
            connection.open(request.build());

        }
    }

    private void initEvents(final Socket connection){

        connection.on(Event.CLOSE, new Function<String>() {
            public void on(String t) {
                setStatus(ConnectionStatus.DISCONNECTED);
            }
        }).on(Event.REOPENED, new Function<String>() {
            public void on(String t) {
                setStatus(ConnectionStatus.CONNECTED);
            }
        }).on(new Function<IOException>() {
            public void on(IOException ioe) {
                ioe.printStackTrace();
            }
        }).on(Event.OPEN, new Function<String>() {
            public void on(String t) {
                setStatus(ConnectionStatus.CONNECTED);
            }
        });
    }


}
