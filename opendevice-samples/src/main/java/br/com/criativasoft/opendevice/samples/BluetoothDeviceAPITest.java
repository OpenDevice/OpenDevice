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
import br.com.criativasoft.opendevice.connection.message.GPIO;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandStreamReader;
import br.com.criativasoft.opendevice.core.command.CommandStreamSerializer;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;

import java.io.IOException;


public class BluetoothDeviceAPITest {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		StreamConnection connection = StreamConnectionFactory.createBluetooth("20:13:01:24:01:93");

        // StreamConnection connection = StreamConnectionFactory.createTcp("192.168.0.101:8282");

		connection.setSerializer(new CommandStreamSerializer()); // data conversion..
		connection.setStreamReader(new CommandStreamReader()); // data protocol..

        connection.addListener(new ConnectionListener() {
            @Override
            public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
                System.out.println("connectionStateChanged :  " + status);
            }

            @Override
            public void onMessageReceived(Message message, DeviceConnection connection) {
                System.out.println("onMessageReceived:" + message.getClass().getSimpleName());
                if(message instanceof Command){
                    System.out.println("onMessageReceived:Command:"+((Command) message).getType());
                }else{
                    System.out.println("onMessageReceived:" + message.toString());
                }
            }
        });


        connection.connect();

        long delay= 2000;

        while(connection.isConnected()){
            connection.send(DeviceCommand.ON_OFF(1, GPIO.HIGH));
            Thread.sleep(delay);
            connection.send(DeviceCommand.ON_OFF(1, GPIO.LOW));
            Thread.sleep(delay);

            connection.send(DeviceCommand.ON_OFF(2, GPIO.HIGH));
            Thread.sleep(delay);
            connection.send(DeviceCommand.ON_OFF(2, GPIO.LOW));
            Thread.sleep(delay);

            connection.send(DeviceCommand.ON_OFF(3, GPIO.HIGH));
            Thread.sleep(delay);
            connection.send(DeviceCommand.ON_OFF(3, GPIO.LOW));
            Thread.sleep(delay);
        }

	}

}
