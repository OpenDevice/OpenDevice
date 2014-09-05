/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.samples.tests;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.UsbConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;

import java.util.Collection;

/**
 * Test Raw Reading..
 * @autor Ricardo JL Rufino
 * @date 27/08/14.
 */
public class TestSerial {

    public static void main(String[] args) throws ConnectionException, InterruptedException {

        Collection<String> portNames = UsbConnection.listAvailablePortNames();
        System.out.println("AvaiblePort: " + portNames);

        UsbConnection connection = new UsbConnection(portNames.iterator().next());

        connection.addListener(new ConnectionListener() {
            @Override
            public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
                System.out.println("connectionStateChanged - " + status);
            }

            @Override
            public void onMessageReceived(Message message, DeviceConnection connection) {
                System.out.println(message);
            }
        });

        connection.connect();

        while(true){
            Thread.sleep(1000);
        }

    }
}
