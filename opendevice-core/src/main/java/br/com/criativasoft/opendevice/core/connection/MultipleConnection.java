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

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionManager;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.serialize.MessageSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class MultipleConnection implements DeviceConnection, ConnectionListener {
	
	private static final Logger log = LoggerFactory.getLogger(MultipleConnection.class);
	
	private Set<DeviceConnection> connections = new LinkedHashSet<DeviceConnection>();
	private Set<ConnectionListener> listeners = Collections.synchronizedSet(new HashSet<ConnectionListener>());
	
	private ConnectionStatus status = ConnectionStatus.DISCONNECTED;

    private ConnectionManager manager;

    private boolean broadcast = false;

    private String uid = UUID.randomUUID().toString();

    /**
     * Broadcast the received commands to other connections
     */
    public void enableBroadcast(){
        broadcast = true;
    }
	  
	public synchronized boolean addListener(ConnectionListener e) {
		boolean value = false;

        if(!listeners.contains(e)){
            listeners.add(e);
        }

		for (DeviceConnection connection : connections) {
			if(connection.addListener(e)){
				value = true;
			}
		}
		
		return value;
	}
	
	@Override
	public synchronized boolean removeListener(ConnectionListener e) {
		boolean value = false;
		listeners.remove(e);
		
		for (DeviceConnection connection : connections) {
			if(connection.removeListener(e)){
				value = true;
			}
		}
		
		return value;
	}

	public void connect() throws ConnectionException {
		IOException lastException = null;
	
		for (DeviceConnection connection : connections) {
			if(connection != null && ! connection.isConnected()){
				try{
					connection.connect();
				}catch(IOException e){
					lastException = e;
				}
			}
		}

		if(lastException != null) throw new ConnectionException(lastException.getMessage(), lastException);
		
	}

	public void disconnect() throws ConnectionException{
		for (DeviceConnection connection : connections) {
			if(connection != null && connection.isConnected()){
				connection.disconnect();
			}
		}
	}

	public boolean isConnected() {
		
		for (DeviceConnection connection : connections) {
			if(connection.isConnected()){
				return true;
			}
		}

		return false;
	}
	
	@Override
	public ConnectionStatus getStatus() {
		return status;
	}
	
	public boolean isAllConnected() {
		
		for (DeviceConnection connection : connections) {
			if(!connection.isConnected()){
				return false;
			}
		}
		
		return true;		
	}
	
	public void send(Message message) throws IOException {

		for (DeviceConnection connection : connections) {
			if(connection != null && connection.isConnected()){
				connection.send(message);
			}
		}
	}
	

	public DeviceConnection addConnection(DeviceConnection connection) {

		for (ConnectionListener listener : listeners) {
			connection.addListener(listener);
		}
		connection.addListener(this); // only to broadcast.
		connections.add(connection);
		return connection;
	}

	public void addAllConnections(Collection<? extends DeviceConnection> connection) {

		for (DeviceConnection deviceConnection : connection) {
			addConnection(deviceConnection);
		}
		
	}

	public Set<DeviceConnection> getConnections() {
		return connections;
	}
	
	public <T> T getConnection(Class<T> klass){
		for (DeviceConnection connection : connections) {

			if(connection.getClass().equals(klass) || klass.isAssignableFrom(connection.getClass())){
				return (T) connection;
			}
			
		}
		
		return null;
	}
	
	public boolean exist(DeviceConnection conn){
		
		if(conn == this) return true;
		
		for (DeviceConnection connection : connections) {

			if(connection == conn){
				return true;
			}
			
		}
		
		return false;		
		
	}
	
	/**
	 * returns the number of connections added
	 */
	public int getSize(){
		return getConnections().size();
	}

    public boolean hasConnections(){
        return getSize() > 0;
    }

    @Override
    public String getUID() {
        return uid;
    }


    @Override
    public String getApplicationID() {
        return null;
    }

    @Override
    public DeviceConnection setApplicationID(String id) {
        return this;
    }

    /**
	 * Repassa o comando que foi recebido, para as outras conexões.
	 */
	private void broadcastCommand(Message message, DeviceConnection fromConnection){
		Set<DeviceConnection> connections = this.getConnections();
		
		if(fromConnection != null){
			
			for (DeviceConnection deviceConnection : connections) {
				
				if(deviceConnection != fromConnection){
					try {
						log.debug("broadcastCommand to connection: " + deviceConnection);
						deviceConnection.send(message);
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				}
				
			}
			
		}
	}

    @Override
    public void onMessageReceived(Message command, DeviceConnection connection) {
        if(broadcast){
            broadcastCommand(command, connection);
        }
	}

	@Override
	public void notifyListeners(Message command) {
		for (DeviceConnection connection : connections) {
			connection.notifyListeners(command);
		}
	}

    @Override
	public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
		
		// UPDATE STATUS;
		Set<DeviceConnection> connections = getConnections();
		
		boolean connected = false;
		
		for (DeviceConnection deviceConnection : connections) {
			if(deviceConnection.isConnected()) connected = true;
		}
		
		if(connected) status = ConnectionStatus.CONNECTED;
		if(!connected) status = ConnectionStatus.DISCONNECTED;
		
	}


    @Override
    public void setSerializer(MessageSerializer<?, ?> serializer) {
        // IGNORE
    }

    @Override
    public MessageSerializer<?, ?> getSerializer() {
        return null; // IGNORE
    }

    @Override
    public void setConnectionManager(ConnectionManager manager) {
        this.manager = manager;
    }

    @Override
    public ConnectionManager getConnectionManager() {
        return this.manager;
    }
}

