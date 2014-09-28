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

import br.com.criativasoft.opendevice.connection.*;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;

import java.io.IOException;

/**
 * In this example it is possible to use the computer just as a proxy 
 * between an Android(using Bluetooth) and Arduino  when you do not have a bluetooth shield 
 * 
 * In android use: https://play.google.com/store/apps/details?id=es.pymasde.blueterm 
 * In arduino/similar use: samples/arduino/UsbConnection
 * 
 * Fron android send 1 and 0 value to blink !
 * 
 * @author Ricardo JL Rufino
 * @date 17/06/2014
 */
public class BluetoothServerProxy {
	
	public static void main(String[] args) throws Exception {

        // FIXME: CONVERTER PARA API DO OPENDEVICE-CORE !!

		StreamConnection serverConnection = StreamConnectionFactory.createBluetoothServer();
		final DeviceConnection usbConnection = StreamConnectionFactory.createUsb("/dev/ttyACM0");
		
		serverConnection.addListener(new ConnectionListener() {
			@Override
			public void onMessageReceived(Message message, DeviceConnection connection) {
				
				SimpleMessage stream = (SimpleMessage) message;
				
				try {
					String value = new String(stream.getBytes());
					
					System.out.println("SERVER:READ:"+value);
					connection.send(new SimpleMessage("SERVER:READ:" + value));  // Only ECHO
					
					if("1".equals(value) || "ON".equalsIgnoreCase(value)){
						
						connection.send(SimpleMessage.HIGH);
						
					}else if("0".equals(value) || "OFF".equalsIgnoreCase(value)){
						
						connection.send(SimpleMessage.LOW);
						
						// ((StreamConnection)connection).write(0);  // Using CAST
						
					}
					
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			

			@Override
			public void connectionStateChanged(DeviceConnection connection,ConnectionStatus status) {
				try {
					if(connection.isConnected()){
						System.out.println("Connected !");
						connection.send(new SimpleMessage("SERVER:OK"));  // ECHO
					}else{
						System.out.println("Disconnected !");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		});
		
		usbConnection.connect();
		serverConnection.connect(); // FIXME: is bloking !!!
	}
}
