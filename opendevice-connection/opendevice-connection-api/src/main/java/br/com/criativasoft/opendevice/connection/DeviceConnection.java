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

import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.serialize.MessageSerializer;

import java.io.IOException;

/**
 * This interface defines the capabilities that a connection must have.
 * @author Ricardo JL Rufino
 */
public interface DeviceConnection {
	
	/** Connect */
	public void connect() throws ConnectionException;
	
	/** Disconnect */
	public void disconnect() throws ConnectionException;

	public boolean isConnected();
	
	public ConnectionStatus getStatus();
	
	public void send(Message message) throws IOException;
	
	public boolean addListener(ConnectionListener listener);
	
	public boolean removeListener(ConnectionListener e);
	
	public void notifyListeners(Message message);
	
	public void setSerializer(MessageSerializer<?, ?> serializer);
	
	public MessageSerializer<?, ?> getSerializer();

    public String getUID();

    public String getApplicationID();

    public DeviceConnection setApplicationID(String id);

    public void setConnectionManager(ConnectionManager manager);

    public ConnectionManager getConnectionManager();

}