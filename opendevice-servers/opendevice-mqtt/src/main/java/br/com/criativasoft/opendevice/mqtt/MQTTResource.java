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

import br.com.criativasoft.opendevice.connection.AbstractConnection;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.serialize.MessageSerializer;
import io.moquette.proto.messages.AbstractMessage;
import io.moquette.proto.messages.PublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Ricardo JL Rufino
 * @date 07/01/16
 */
public class MQTTResource extends AbstractConnection {
    private static final Logger log  = LoggerFactory.getLogger(MQTTResource.class);

    private String topic;
    private MoquetteServer server;

    public MQTTResource( MoquetteServer server, String topic) {
        this.server = server;
        this.topic = topic;
    }

    @Override
    public void connect() throws ConnectionException {
        setStatus(ConnectionStatus.CONNECTED);
    }

    @Override
    public void disconnect() throws ConnectionException {
        setStatus(ConnectionStatus.DISCONNECTED);
    }

    @Override
    public void send(Message message) throws IOException {

        MessageSerializer serializer = getSerializer();
        PublishMessage publishMessage = new PublishMessage();
        publishMessage.setTopicName(topic);
        publishMessage.setPayload(ByteBuffer.wrap(serializer.serialize(message)));
        publishMessage.setQos(AbstractMessage.QOSType.MOST_ONE);
        server.internalPublish(publishMessage);

    }
}
