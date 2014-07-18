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

import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.serialize.MessageSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base class for Connections
 * @author Ricardo JL Rufino
 * @date 04/09/2011 13:18:48
 */
public abstract class AbstractConnection implements DeviceConnection {

    protected static final Logger log = LoggerFactory.getLogger(AbstractConnection.class);

    private static ExecutorService executor = Executors.newCachedThreadPool();

	private Set<ConnectionListener> listeners = new HashSet<ConnectionListener>();

	private ConnectionStatus status = ConnectionStatus.DISCONNECTED;
	
	private MessageSerializer<?, ?> serializer;

	/**
	 * Notify All Listeners about received command.
	 */
	public void notifyListeners(final Message message) {

        if(listeners.isEmpty())  log.warn("No listener was registered ! use: addListener");

		for (final ConnectionListener listener : listeners) {
            // TODO: Add config to use in SYNC MODE.
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onMessageReceived(message, AbstractConnection.this);
                }
            });
        }
	}
	
	protected boolean hasListeners(){
		return ! listeners.isEmpty();
	}
	
	// =======================================================
	// Set's / Get's
	// =======================================================

	protected void setStatus(ConnectionStatus status) {
		this.status = status;
        if(listeners.isEmpty())  log.warn("No listener was registered ! use: addListener");
		for (ConnectionListener listener : listeners) {
			listener.connectionStateChanged(this, status);
		}
	}
	
	@Override
	public void setSerializer(MessageSerializer<?, ?> serializer) {
		this.serializer = serializer;
	}
	
	@Override
	public MessageSerializer<?, ?> getSerializer() {
		return this.serializer;
	}

	@Override
	public ConnectionStatus getStatus() {
		return status;
	}

	@Override
	public boolean isConnected() {
		return status == ConnectionStatus.CONNECTED;
	}

	public boolean addListener(ConnectionListener e) {
		return listeners.add(e);
	}

	@Override
	public boolean removeListener(ConnectionListener e) {
		return listeners.remove(e);
	}

	public boolean addAll(Collection<? extends ConnectionListener> c) {
		return listeners.addAll(c);
	}

	public Set<ConnectionListener> getListeners() {
		return listeners;
	}

}
