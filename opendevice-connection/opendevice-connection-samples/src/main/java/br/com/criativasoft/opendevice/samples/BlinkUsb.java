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

package br.com.criativasoft.opendevice.samples;

import br.com.criativasoft.opendevice.connection.*;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;

import java.util.Collection;

/**
 * USE: samples/arduino/UsbConnection ou samples/arduino/BluetoothConnection
 * @author Ricardo JL Rufino
 * @date 17/06/2014
 */
public class BlinkUsb {
	public static void main(String[] args) throws Exception {

        Collection<String> portNames = UsbConnection.listAvailablePortNames();
        System.out.println("AvaiblePort: " + portNames);

        StreamConnection connection = StreamConnectionFactory.createUsb("/dev/ttyACM0");

        connection.addListener(listener);
		connection.connect();

        while(true){
			connection.send(SimpleMessage.ON);
			Thread.sleep(500);
			connection.send(SimpleMessage.OFF);
			Thread.sleep(500);
		}

	}

    private static ConnectionListener listener = new ConnectionListener() {
        @Override
        public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
            System.out.println("connectionStateChanged -> " + status);
        }

        @Override
        public void onMessageReceived(Message message, DeviceConnection connection) {
            System.out.println("onMessageReceived -> " + message);
        }
    };
}
