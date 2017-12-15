/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.mqtt;

import io.moquette.BrokerConstants;
import io.moquette.interception.InterceptHandler;
import io.moquette.server.Server;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import io.moquette.spi.security.IAuthenticator;
import io.moquette.spi.security.IAuthorizator;
import io.moquette.spi.security.ISslContextCreator;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 07/01/16
 */
public class MoquetteServer {

    private static final Logger LOG = LoggerFactory.getLogger(MoquetteServer.class);

    private Server mqttBroker = new Server();

    IConfig config;

    List<? extends InterceptHandler> handlers = Collections.emptyList();

    ISslContextCreator sslCtxCreator;

    IAuthenticator authenticator;

    IAuthorizator authorizator;


    /**
     * Create the server with the given properties.
     *
     * Its suggested to at least have the following properties:
     * <ul>
     *  <li>port</li>
     *  <li>password_file</li>
     * </ul>
     */
    public MoquetteServer(Properties configProps) throws IOException {
        this(new MemoryConfig(configProps));
    }

    /**
     * Create Moquette bringing the configuration files from the given Config implementation.
     */
    public MoquetteServer(IConfig config) throws IOException {
       this.config = config;
    }



    public void start() throws IOException {
        if (handlers == null) {
            handlers = Collections.emptyList();
        }

        final String handlerProp = System.getProperty("intercept.handler");
        if (handlerProp != null) {
            config.setProperty("intercept.handler", handlerProp);
        }
        LOG.info("Persistent store file: " + config.getProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME));
        if(authenticator != null){
            config.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, "false");
        }

        if (sslCtxCreator == null) {
            sslCtxCreator = new DefaultMoquetteSslContextCreator(config);
        }

        mqttBroker.startServer(config, handlers, sslCtxCreator, authenticator, authorizator);
    }

    /**
     * Use the broker to publish a message. It's intended for embedding applications.
     * It can be used only after the server is correctly started with startServer.
     *
     * @param msg the message to forward.
     * @throws IllegalStateException if the server is not yet started
     * */
    public void internalPublish(MqttPublishMessage msg) {
        mqttBroker.internalPublish(msg, "mqttServer");
    }

    public void stop() {
        LOG.info("Server stopping...");
        mqttBroker.stopServer();
        LOG.info("Server stopped");
    }


    public void setAuthenticator(IAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setAuthorizator(IAuthorizator authorizator) {
        this.authorizator = authorizator;
    }

    public void setHandlers(List<? extends InterceptHandler> handlers) {
        this.handlers = handlers;
    }

    public void setSslCtxCreator(ISslContextCreator sslCtxCreator) {
        this.sslCtxCreator = sslCtxCreator;
    }
}
