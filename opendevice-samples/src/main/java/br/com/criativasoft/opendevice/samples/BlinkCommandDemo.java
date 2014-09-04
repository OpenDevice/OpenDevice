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


/**
 * @author Ricardo JL Rufino
 * @date 17/02/2014
 */
public class BlinkCommandDemo implements ConnectionListener {

    public BlinkCommandDemo() throws Exception {

        StreamConnection conn = StreamConnectionFactory.createUsb(UsbConnection.getFirstAvailable());
        // StreamConnection connection = StreamConnectionFactory.createBluetooth("20:13:01:24:01:93");
        // StreamConnection connection = StreamConnectionFactory.createTcp("192.168.0.101:8282");

        // Configure OpenDevices Commands Protocol
        conn.setSerializer(new CommandStreamSerializer()); // for de/serialization..
        conn.setStreamReader(new CommandStreamReader());   // for reading streams

        conn.connect();

        long delay= 300;

        while(conn.isConnected()) {
            conn.send(DeviceCommand.ON(1)); // '1' is Device ID not pin !
            Thread.sleep(delay);
            conn.send(DeviceCommand.OFF(1));
            Thread.sleep(delay);
        }
    }
	
	public static void main(String[] args) throws Exception {
        new BlinkCommandDemo();
	}


    // ------------- ConnectionListener Impl --------------------------
    // ------------------------------------------------------------

    @Override
    public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
        System.out.println("connectionStateChanged :  " + status);
    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {
        String type = message.getClass().getSimpleName();

        if(message instanceof Command){
            System.out.println("onMessageReceived("+type+"): "+((Command) message).getType());
        }else{
            System.out.println("onMessageReceived("+type+"): "+message.toString());
        }
    }
}
