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

package br.com.criativasoft.opendevice.mqtt;

import br.com.criativasoft.opendevice.connection.AbstractConnection;
import br.com.criativasoft.opendevice.connection.ConnectionManager;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.IMQTTServerConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.Request;
import br.com.criativasoft.opendevice.connection.serialize.MessageSerializer;
import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.GetDevicesRequest;
import io.moquette.BrokerConstants;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.*;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;

import java.io.IOException;
import java.util.Properties;

import static java.util.Arrays.asList;

/**
 * TODO: PENDING DOC
 *
 * @author Ricardo JL Rufino
 * @date 08/07/14.
 */
public class MQTTServerConnection extends AbstractConnection implements IMQTTServerConnection {

    private  int port = BrokerConstants.PORT;
    private MoquetteServer server;
    private ConnectionManager manager;

    public MQTTServerConnection() {
        super();
    }

    public MQTTServerConnection(int port) {
        super();
        this.port = port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void setConnectionManager(ConnectionManager manager) {
        super.setConnectionManager(manager);
        this.manager = manager;
    }

    @Override
    public Message notifyAndWait(Request message) {
        return null;
    }

    @Override
    public void connect() throws ConnectionException {
        try {
            log.debug("connecting...");
            if(!isConnected()){

                initConnection(); // Setup

                //initServerEvents();

                log.debug("Starting server on port: " + port);
                server.start();

                setStatus(ConnectionStatus.CONNECTED);

            }

        } catch (IOException e) {
            throw new ConnectionException(e);
        }

    }

    private void initConnection() throws IOException {

        if (server == null) {
            Properties params = new Properties();
            params.put(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(port));

            IConfig config = new MemoryConfig(params);
            server = new MoquetteServer(config);
            server.setHandlers(asList(serverListener));
        }
    }

    @Override
    public void disconnect() throws ConnectionException {
        if(isConnected()){
            server.stop(); // TODO: nome redundante
        }
    }

    @Override
    public void send(Message message) throws IOException {

    }

    private InterceptHandler serverListener = new InterceptHandler() {

        @Override
        public void onConnect(InterceptConnectMessage msg) {
            System.err.println("onConnect - " + msg.getClientID());

            String appID = msg.getClientID().split("/")[0];
            TenantProvider.setCurrentID(appID);

            String moduleName = msg.getClientID().split("/")[1];
            String connUID = msg.getClientID();


        }

        @Override
        public void onDisconnect(InterceptDisconnectMessage msg) {
            String appID = msg.getClientID().split("/")[1];
            TenantProvider.setCurrentID(appID);
        }

        @Override
        public void onPublish(InterceptPublishMessage msg) {
            System.err.println("<<"+msg.getClientID()+">> Received on topic: " + msg.getTopicName() + " content: " + new String(msg.getPayload().array()));

            String appID = msg.getClientID().split("/")[0];
            String moduleName = msg.getClientID().split("/")[1];
            String connUID = msg.getClientID();

            TenantProvider.setCurrentID(appID);

            MQTTResource connection = (MQTTResource) manager.findConnection(connUID);

            if(connection == null){
                log.warn("Connection not found ! ID : " + connUID);
                return;
            }

            // Received from Devices : /appID/out
            if(msg.getTopicName().contains(appID + "/out")){

                MessageSerializer serializer = getSerializer();

                Message message = serializer.parse(msg.getPayload().array()); // TODO May be delay to send to clients (send direct ?).

                log.debug("Received command: " + message);

                connection.notifyListeners(message);

            }
        }

        @Override
        public void onSubscribe(InterceptSubscribeMessage msg) {
            System.err.println("onSubscribe - " + msg.getClientID() + ", on : " + msg.getTopicFilter());
            String appID = msg.getClientID().split("/")[0];
            TenantProvider.setCurrentID(appID);


            if(manager instanceof BaseDeviceManager){

                BaseDeviceManager manager = (BaseDeviceManager) MQTTServerConnection.this.manager;

                // Received from Devices ( Subscribe in ProjectID/in/ModuleName)
                if(msg.getTopicFilter().startsWith(appID + "/in/")){

                    MQTTResource resource = (MQTTResource) manager.findConnection(msg.getClientID());

                    if(resource ==  null){
                        resource = new MQTTResource(server, msg.getTopicFilter());
                        resource.setUid(msg.getClientID());
                        manager.addOutput(resource);
                    }

                    // Syncronize devices...
                    try {
                        resource.send(new GetDevicesRequest());
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }

            }

        }

        @Override
        public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
            System.err.println("onUnsubscribe - " + msg.getClientID());
        }
    };
}
