/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.connection;

import br.com.criativasoft.opendevice.connection.server.BluetoothServerConnection;

public class StreamConnectionFactory {
	
	/**
	 * @singleton
	 */
	private StreamConnectionFactory(){
	}
	
	public static StreamConnection createUsb(String portName){
		return new UsbConnection(portName);
	}
	
	public static StreamConnection createBluetooth(String uri){
		return new BluetoothConnection(uri);
	}
	
	public static StreamConnection createBluetoothServer(){
		return new BluetoothServerConnection();
	}

    public static StreamConnection createTcp(String uri){
        return new TCPConnection(uri);
    }
}
