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
import br.com.criativasoft.opendevice.connection.IFirmwareConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.serialize.MessageSerializer;
import br.com.criativasoft.opendevice.core.command.FirmwareUpdateCommand;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Ricardo JL Rufino
 * @date 07/01/16
 */
public class MQTTResource extends AbstractConnection implements IFirmwareConnection {
    private static final Logger log  = LoggerFactory.getLogger(MQTTResource.class);

    private String topic;
    private MoquetteServer server;
    private String deviceName;

    public MQTTResource(MoquetteServer server, String topic, String name) {
        this.server = server;
        this.topic = topic;
        this.deviceName = name;
        setStatus(ConnectionStatus.CONNECTED);
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

        MqttQoS qoS = MqttQoS.AT_LEAST_ONCE;

        // FIX to Avoid duplicate Message do Device, after reset.
        if(message instanceof FirmwareUpdateCommand){
            qoS = MqttQoS.AT_MOST_ONCE; // fire and forget”
        }

        MqttPublishMessage publish = MqttMessageBuilders.publish()
                .topicName(topic)
                .retained(qoS.value() > 0)
                .qos(qoS)
                .payload(Unpooled.copiedBuffer(ByteBuffer.wrap(serializer.serialize(message))))
                .build();

        server.internalPublish(publish);

    }

    /**
     * Return the name of connected device.
     * @return
     */
    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public String toString() {
        return "MQTTResource["+topic+"]";
    }
}
