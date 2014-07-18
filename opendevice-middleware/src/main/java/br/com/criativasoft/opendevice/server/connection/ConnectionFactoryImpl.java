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

package br.com.criativasoft.opendevice.server.connection;

import br.com.criativasoft.opendevice.connection.BluetoothConnection;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.connection.ConnectionFactory;
import br.com.criativasoft.opendevice.core.connection.ConnectionProperties;

public class ConnectionFactoryImpl implements ConnectionFactory{

	@Override
	public DeviceConnection getHttpConnection(ConnectionProperties properties) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public DeviceConnection getWebSocketConnection(ConnectionProperties properties) {
//		return new WSClientConnection(properties.getConnectionURL());
//	}

	@Override
	public DeviceConnection getBluetoothConnection(ConnectionProperties properties) {
		return new BluetoothConnection(properties.getConnectionURL());
	}

	@Override
	public DeviceConnection getHttpServerConnection(ConnectionProperties properties) {
		return null; //new RestExpressConnection(properties.getConnectionPort());
	}

//	@Override
//	public DeviceConnection getWebSocketServerConnection(ConnectionProperties properties) {
//		return new WSServerConnection(properties.getConnectionPort());
//	}

	@Override
	public DeviceConnection getBluetoothServerConnection(ConnectionProperties properties) {
		// TODO Auto-generated method stub
		return null;
	}

}
