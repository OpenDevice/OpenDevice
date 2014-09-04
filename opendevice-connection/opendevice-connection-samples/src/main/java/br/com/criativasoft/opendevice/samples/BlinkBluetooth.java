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

import br.com.criativasoft.opendevice.connection.StreamConnection;
import br.com.criativasoft.opendevice.connection.StreamConnectionFactory;

/**
 * USE: samples/arduino/BluetoothConnection
 * @author Ricardo JL Rufino
 * @date 17/06/2014
 */
public class BlinkBluetooth {
	
	public static void main(String[] args) throws Exception {
		
//       00:11:09:25:04:75 - bt1(ardu)
//       00:11:06:14:04:57 - bt2(st)
//       20:13:01:24:01:93 - BT-MCU-3(st)
		
		StreamConnection connection = StreamConnectionFactory.createBluetooth("20:13:01:24:01:93");

		connection.connect();
		// connection.setSerialReader(new AmarinoSerialReader());

        System.out.println("Connected !");

		while(true){
			connection.write((byte)1);
			Thread.sleep(500);
			connection.write((byte)0);
			Thread.sleep(500);
		}

		
	}
}
