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

package no_unit;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// Test to receive ping request from physical modules
public class PingResponseTest extends LocalDeviceManager implements ConnectionListener {

    List<Command> received = new LinkedList<Command>();

    AtomicInteger count = new AtomicInteger();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start() throws IOException {

        addConnectionListener(this);

        connect(out.bluetooth("00:11:06:14:04:57"));

    }


    @Override
    public void connectionStateChanged(final DeviceConnection connection, ConnectionStatus status) {

    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

        if(message instanceof SimpleCommand && ((SimpleCommand) message).getType() == CommandType.PING ){
            SimpleCommand ping = (SimpleCommand) message;

            received.add(ping);

            // force error's
            try {
                send(new GetDevicesRequest());
            } catch (IOException e) {
            }

            System.err.println("Stats: Received = " + received.size());

        }

    }
}
