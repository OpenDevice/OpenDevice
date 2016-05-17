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
import io.moquette.proto.messages.PublishMessage;
import io.moquette.server.ServerAcceptor;
import io.moquette.server.config.FilesystemConfig;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import io.moquette.server.netty.NettyAcceptor;
import io.moquette.spi.impl.ProtocolProcessor;
import io.moquette.spi.impl.SimpleMessaging;
import io.moquette.spi.security.IAuthenticator;
import io.moquette.spi.security.IAuthorizator;
import io.moquette.spi.security.ISslContextCreator;
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

    private ServerAcceptor m_acceptor;

    private volatile boolean m_initialized;

    private ProtocolProcessor m_processor;

    IConfig config;

    List<? extends InterceptHandler> handlers = Collections.emptyList();

    ISslContextCreator sslCtxCreator;

    IAuthenticator authenticator;

    IAuthorizator authorizator;

    /**
     * Create Moquette bringing the configuration from the file
     * located at m_config/moquette.conf
     */
    public MoquetteServer() throws IOException {
        this(new FilesystemConfig());
    }

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
        final ProtocolProcessor processor = SimpleMessaging.getInstance().init(config, handlers, authenticator, authorizator);

        if (sslCtxCreator == null) {
            sslCtxCreator = new DefaultMoquetteSslContextCreator(config);
        }

        m_acceptor = new NettyAcceptor();
        m_acceptor.initialize(processor, config, sslCtxCreator);
        m_processor = processor;
        m_initialized = true;
    }

    /**
     * Use the broker to publish a message. It's intended for embedding applications.
     * It can be used only after the server is correctly started with startServer.
     *
     * @param msg the message to forward.
     * @throws IllegalStateException if the server is not yet started
     * */
    public void internalPublish(PublishMessage msg) {
        if (!m_initialized) {
            throw new IllegalStateException("Can't publish on a server is not yet started");
        }
        m_processor.internalPublish(msg);
    }

    public void stop() {
        LOG.info("Server stopping...");
        m_acceptor.close();
        SimpleMessaging.getInstance().shutdown();
        m_initialized = false;
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
