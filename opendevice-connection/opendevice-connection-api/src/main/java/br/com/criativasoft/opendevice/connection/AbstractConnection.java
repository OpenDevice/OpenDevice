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

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base class for Connections
 * @author Ricardo JL Rufino
 * @date 04/09/2011 13:18:48
 */
public abstract class AbstractConnection implements DeviceConnection {

    protected static final Logger log = LoggerFactory.getLogger(AbstractConnection.class);

    private static ExecutorService executor;

    private boolean useThreadPool =  false;

	private final Set<ConnectionListener> listeners = Collections.synchronizedSet(new HashSet<ConnectionListener>());

	private ConnectionStatus status = ConnectionStatus.DISCONNECTED;
	
	private MessageSerializer<?, ?> serializer;

    private String uid = UUID.randomUUID().toString();

    private String applicationID;

    private ConnectionManager manager;

    /**
     * Enables notification of listeners in separate threads if the implementations of listeners slow to run (eg sending an email)
     * @param useThreadPool - Default false
     */
    public void setUseThreadPool(boolean useThreadPool) {
        this.useThreadPool = useThreadPool;
        if(useThreadPool) executor = Executors.newCachedThreadPool();
    }

    /**
	 * Notify All Listeners about received command.
	 */
	public void notifyListeners(final Message message) {

        if(listeners.isEmpty())  log.warn("No listener was registered ! use: addListener");

        if(message.getConnectionUUID() == null) {
            message.setConnectionUUID(this.getUID());
        }

        synchronized (listeners){

            Iterator<ConnectionListener> iterator = listeners.iterator();

            while(iterator.hasNext()){
                final ConnectionListener listener = iterator.next();

                if(useThreadPool){
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            listener.onMessageReceived(message, AbstractConnection.this);
                        }
                    });
                }else{
                    listener.onMessageReceived(message, AbstractConnection.this);
                }
            }


        }
	}

    public boolean tryReconnect(int times, long interval){

        if(isConnected()) return true;

        try{

            log.debug("try reconnect["+times+"] ...");

            connect();

            return isConnected();

        }catch (Exception ex){
            times--;

            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {}

            if(times == 0) return false;

            tryReconnect(times, interval);

        }

        return false;
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
        synchronized (listeners){
            for (ConnectionListener listener : listeners) {
                listener.connectionStateChanged(this, status);
            }
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

	public synchronized boolean addListener(ConnectionListener e) {
        if(!listeners.contains(e)){
            listeners.add(e);
            return true;
        }

		return false;
	}

	@Override
	public  boolean removeListener(ConnectionListener e) {
        synchronized (listeners){
            return listeners.remove(e);
        }
	}

	public boolean addAll(Collection<? extends ConnectionListener> c) {
		return listeners.addAll(c);
	}

	public Set<ConnectionListener> getListeners() {
		return listeners;
	}

    @Override
    public String getUID() {
        return uid;
    }

    @Override
    public DeviceConnection setApplicationID(String applicationID) {
        this.applicationID = applicationID;
        return this;
    }

    @Override
    public String getApplicationID() {
        return applicationID;
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
