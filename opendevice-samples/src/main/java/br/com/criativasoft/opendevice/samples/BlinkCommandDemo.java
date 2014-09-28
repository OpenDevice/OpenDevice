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
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandStreamReader;
import br.com.criativasoft.opendevice.core.command.CommandStreamSerializer;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.connection.Connections;


/**
 *
 * Tutorial: https://opendevice.atlassian.net/wiki/display/DOC/A.+First+Steps+with+OpenDevice
 * For arduino/energia use: opendevice-hardware-libraries/arduino/OpenDevice/examples/UsbConnection
 * For arduino(with bluetooth): opendevice-hardware-libraries/arduino/OpenDevice/examples/BluetoothConnection
 * @author Ricardo JL Rufino
 * @date 17/02/2014
 */
public class BlinkCommandDemo implements ConnectionListener {

    public BlinkCommandDemo() throws Exception {

        DeviceConnection conn = Connections.out.usb();
        conn.addListener(this);
        conn.connect();
        long delay = 500;

        while(conn.isConnected()) {
            conn.send(DeviceCommand.ON(1)); // '1' is Device ID not pin !
            Thread.sleep(delay);
            conn.send(DeviceCommand.OFF(1));
            Thread.sleep(delay);
        }

        System.out.println("TERMINATED !");
    }
	
	public static void main(String[] args) throws Exception {
        new BlinkCommandDemo();
	}


    // ------------------------------------------------------------
    // ------------- ConnectionListener Impl --------------------------

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {
        String type = message.getClass().getSimpleName();
        System.out.println("onMessageReceived("+type+"): "+ message);
    }

    @Override
    public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
        System.out.println("connectionStateChanged :  " + status);
    }
}
