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
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.GetDevicesRequest;
import br.com.criativasoft.opendevice.core.command.NotificationEvent;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.core.util.StringUtils;
import io.moquette.BrokerConstants;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.*;
import io.moquette.spi.impl.subscriptions.Topic;
import io.moquette.spi.security.IAuthenticator;
import io.moquette.spi.security.IAuthorizator;
import io.moquette.spi.security.ISslContextCreator;
import io.netty.handler.ssl.JdkSslServerContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.File;
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
    private BaseDeviceManager manager;

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
        if(manager instanceof DeviceManager){
            this.manager = (BaseDeviceManager) manager;
        }
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

            final OpenDeviceConfig oconfig = ODev.getConfig();

            Properties params = new Properties();
//            params.put(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(port));
            if(oconfig.getCertificateFile() != null) params.put(BrokerConstants.SSL_PORT_PROPERTY_NAME, Integer.toString(8883));
            params.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, OpenDeviceConfig.getDataDirectory() + File.separator + "moquette_data.mapdb");
            params.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, "false");

            server = new MoquetteServer(params);
            server.setHandlers(asList(serverListener));


            // IAuthenticator based on ApplicationID
            if(oconfig.isAuthRequired() || oconfig.isTenantsEnabled()){
                server.setAuthenticator(new IAuthenticator() {
                    @Override
                    public boolean checkValid(String clientID, String username, byte[] password) {
                        return TenantProvider.getTenantProvider().exist(username);
                    }
                });


                server.setAuthorizator(new IAuthorizator() {
                    @Override
                    public boolean canWrite(Topic topic, String user, String client) {

                        if(topic.isEmpty()) return false;

                        String first = topic.getTokens().get(0).toString();

                        if (first.equals(user)) {
                            return true;
                        }

                        return false;
                    }

                    @Override
                    public boolean canRead(Topic topic, String user, String client) {
                        return canWrite(topic, user, client);
                    }
                });
            }

            String certificate = oconfig.getCertificateFile();
            if(!StringUtils.isEmpty(certificate)){

                final File cert = new File(certificate);
                if(!cert.exists()) throw new IllegalArgumentException("Certificate not found !");
                final File key = new File(oconfig.getCertificateKey());
                if(!key.exists()) throw new IllegalArgumentException("Certificate key must be provided !");

                server.setSslCtxCreator(new ISslContextCreator() {
                    @Override
                    public SSLContext initSSLContext() {
                        try {
                            JdkSslServerContext sslContext = (JdkSslServerContext) SslContext.newServerContext(SslProvider.JDK, cert, key, oconfig.getCertificatePass());
                            return sslContext.context();
                        } catch (SSLException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
            }

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
        public String getID() {
            return "MQTTServerConnection";
        }

        @Override
        public Class<?>[] getInterceptedMessageTypes() {
            return InterceptHandler.ALL_MESSAGE_TYPES;
        }

        @Override
        public void onConnect(InterceptConnectMessage msg) {
            log.debug("onConnect: {}", msg.getClientID());

            // MQTT ClientID: APIKEY/ModuleName

            String appID = msg.getClientID().split("/")[0];
            TenantProvider.setCurrentID(appID);

            String moduleName = msg.getClientID().split("/")[1];
            String connUID = msg.getClientID();


        }

        @Override
        public void onDisconnect(InterceptDisconnectMessage msg) {
            log.debug("onDisconnect: {}", msg.getClientID());
            onDisconnectionOrLost(msg.getClientID());
        }


        @Override
        public void onConnectionLost(InterceptConnectionLostMessage msg) {
            log.debug("onConnectionLost: {}", msg.getClientID());
            onDisconnectionOrLost(msg.getClientID());
        }

        private void onDisconnectionOrLost(String clientID){

            String appID = clientID.split("/")[0];
            String moduleName = clientID.split("/")[1];

            TenantProvider.setCurrentID(appID);

            BaseDeviceManager manager = (BaseDeviceManager) MQTTServerConnection.this.manager;

            MQTTResource resource = (MQTTResource) manager.findConnection(clientID);

            if(resource == null) resource = (MQTTResource) manager.findConnection(appID +"/in/"+moduleName);

            if (resource != null) {
                manager.removeOutput(resource);
            }

            // Notify UI interface
            try {
                manager.send(new NotificationEvent("Device Disconnected", moduleName, "warning"), false, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onPublish(InterceptPublishMessage msg) {

            byte[] data = new byte[msg.getPayload().capacity()];
            msg.getPayload().getBytes(0, data);

            System.err.println("<<"+msg.getClientID()+">> Received on topic: " + msg.getTopicName() + " content: " + new String(data));

            msg.getPayload();

            String appID = msg.getClientID().split("/")[0];
            String moduleName = msg.getClientID().split("/")[1];

            String connUID = msg.getTopicName();

            TenantProvider.setCurrentID(appID);

            // Received from Devices : /appID/out
            if(msg.getTopicName().contains(appID + "/out")){

                MQTTResource connection = (MQTTResource) manager.findConnection(connUID.replaceAll("/out/", "/in/"));

                if(connection == null){
                    log.warn("Connection not found ! ID: " + connUID);
                    return;
                }

                MessageSerializer serializer = getSerializer();

                Message message = serializer.parse(data);
                if(message instanceof Command) {
                    message.setConnectionUUID(connection.getUID());
                    ((Command) message).setApplicationID(appID);
                }

                log.debug("Received command: " + message);

//              BaseDeviceManager defaultManager = (BaseDeviceManager) manager;
//              defaultManager.transactionBegin();
                connection.notifyListeners(message);
//              defaultManager.transactionEnd();

            }
        }

        @Override
        public void onSubscribe(InterceptSubscribeMessage msg) {
            log.debug("onSubscribe: {} on {}", msg.getClientID(), msg.getTopicFilter());
            String appID = msg.getClientID().split("/")[0];
            String moduleName = msg.getClientID().split("/")[1];
            TenantProvider.setCurrentID(appID);

            // Received from Devices ( Subscribe in ApplicationID/in/ModuleName
            if(msg.getTopicFilter().startsWith(appID + "/in/")){

                MQTTResource resource = (MQTTResource) manager.findConnection(msg.getTopicFilter());

                if(resource ==  null){
                    resource = new MQTTResource(server, msg.getTopicFilter(), moduleName);
                    resource.setUid(msg.getTopicFilter());
                    resource.setApplicationID(appID);
                    manager.addOutput(resource);
                }

                // Syncronize devices...
                try {
                    resource.send(new GetDevicesRequest());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }


                // Notify UI interface
                try {
                    manager.send(new NotificationEvent("Device Connected", moduleName, "info"), false, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        }

        @Override
        public void onUnsubscribe(InterceptUnsubscribeMessage msg) {


        }

        @Override
        public void onMessageAcknowledged(InterceptAcknowledgedMessage msg) {

        }
    };
}
