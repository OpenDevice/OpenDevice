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
import br.com.criativasoft.opendevice.connection.message.ByteMessage;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;
import br.com.criativasoft.opendevice.connection.serialize.MessageSerializer;

import java.io.IOException;
import java.util.Date;

/**
 * This interface defines the capabilities that a connection must have. <br/>
 * The message send will can be serialized using a {@link MessageSerializer} (see {@link #send(Message)})  <br/>
 * Multiple connections can be managed using {@link ConnectionManager} and  <b>DeviceManager</b> in the 'opendevice-core' module.
 * @author Ricardo JL Rufino
 */
public interface DeviceConnection {
	
	/** Connect */
	public void connect() throws ConnectionException;
	
	/** Disconnect */
	public void disconnect() throws ConnectionException;

	public boolean isConnected();
	
	public ConnectionStatus getStatus();

	/**
	 * Sends a message to the connected device on this connection.</br>
	 * The message send will be serialized using {@link MessageSerializer} (Or sent directly if it is a {@link ByteMessage}) </br>
	 * <b>NOTE:</b> Is not recommended direct call to this method, it should be done by DeviceManager (Unless it is a specific integration.).
	 * @param message - Mensage to be send (see {@link SimpleMessage})
	 * @throws IOException
	 */
	public void send(Message message) throws IOException;
	
	public boolean addListener(ConnectionListener listener);
	
	public boolean removeListener(ConnectionListener e);
	
	public void notifyListeners(Message message);
	
	public void setSerializer(MessageSerializer serializer);
	
	public MessageSerializer getSerializer();

  public String getUID();

  public String getApplicationID();

  public DeviceConnection setApplicationID(String id);

  public void setConnectionManager(ConnectionManager manager);

  public ConnectionManager getConnectionManager();


	public Date getFistConnectionDate();

	public Date getLastConnectionDate();

}