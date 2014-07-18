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

package br.com.criativasoft.opendevice.core.connection;

import br.com.criativasoft.opendevice.connection.DeviceConnection;

public interface ConnectionFactory {
	
	public DeviceConnection getHttpConnection(ConnectionProperties properties);
	
//	public DeviceConnection getWebSocketConnection(ConnectionProperties properties);
	
	public DeviceConnection getBluetoothConnection(ConnectionProperties properties);
	
	
	// SERVER
	
	public DeviceConnection getHttpServerConnection(ConnectionProperties properties);

//	public DeviceConnection getWebSocketServerConnection(ConnectionProperties properties);

	public DeviceConnection getBluetoothServerConnection(ConnectionProperties properties);

}
