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
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.GetDevicesRequest;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.core.util.StringUtils;
import io.moquette.BrokerConstants;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.*;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
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
    private DeviceManager manager;

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
            this.manager = (DeviceManager) manager;
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
            Properties params = new Properties();
//            params.put(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(port));
            params.put(BrokerConstants.SSL_PORT_PROPERTY_NAME, Integer.toString(8883));

            IConfig config = new MemoryConfig(params);
            server = new MoquetteServer(config);
            server.setHandlers(asList(serverListener));

            // SSL Support
            final OpenDeviceConfig oconfig = OpenDeviceConfig.get();
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

            String appID = msg.getClientID().split("/")[1];
            TenantProvider.setCurrentID(appID);
        }

        @Override
        public void onPublish(InterceptPublishMessage msg) {
            System.err.println("<<"+msg.getClientID()+">> Received on topic: " + msg.getTopicName() + " content: " + new String(msg.getPayload().array()));

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

                Message message = serializer.parse(msg.getPayload().array()); // TODO May be delay to send to clients (send direct json ?).
                if(message instanceof Command) {
                    message.setConnectionUUID(connection.getUID());
                    ((Command) message).setApplicationID(appID);
                }

                log.debug("Received command: " + message);

                connection.notifyListeners(message);

            }
        }

        @Override
        public void onSubscribe(InterceptSubscribeMessage msg) {
            log.debug("onSubscribe: {} on {}", msg.getClientID(), msg.getTopicFilter());
            String appID = msg.getClientID().split("/")[0];
            TenantProvider.setCurrentID(appID);

            // Received from Devices ( Subscribe in ProjectID/in/ModuleName)
            if(msg.getTopicFilter().startsWith(appID + "/in/")){

                MQTTResource resource = (MQTTResource) manager.findConnection(msg.getTopicFilter());

                if(resource ==  null){
                    resource = new MQTTResource(server, msg.getTopicFilter());
                    resource.setUid(msg.getTopicFilter());
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

        @Override
        public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
            log.debug("onUnsubscribe: {} on {}", msg.getClientID(), msg.getTopicFilter());

            String appID = msg.getClientID().split("/")[0];

            TenantProvider.setCurrentID(appID);

            BaseDeviceManager manager = (BaseDeviceManager) MQTTServerConnection.this.manager;

            if(msg.getTopicFilter().startsWith(appID + "/in/")) {

                MQTTResource resource = (MQTTResource) manager.findConnection(msg.getClientID());

                if (resource != null) {
                    manager.removeOutput(resource);
                }
            }

        }
    };
}
