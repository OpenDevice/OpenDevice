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

package br.com.criativasoft.opendevice.core;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandStatus;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class monitor sending commands to the device and your answers to see if the communication is correct.<br/>
 * It is used by the {@link br.com.criativasoft.opendevice.core.BaseDeviceManager DeviceManager}
 * @author Ricardo JL Rufino
 * @date 23/06/2013
 */
public class CommandDelivery {
	
	private static final Logger log = LoggerFactory.getLogger(CommandDelivery.class);
	
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	private DeviceManager manager;
	
	private AtomicInteger cmdCount = new AtomicInteger(0);
	
	public static int MAX_CMD_COUNT = 9999;
	
	/** Timeout in seconds */
	public static int MAX_TIMEOUT = 5;
	
	public CommandDelivery(DeviceManager manager) {
		super();
		this.manager = manager;
	}

	public void sendTo(Command command, DeviceConnection connection) throws IOException {
		
		command.setStatus(CommandStatus.DELIVERED);
		
		if(command instanceof ResponseCommand){
			
			connection.send(command);
			
		}else{
			
			sendWithTimeout(command, connection);
			
		}
		
		
	}
	
	/**
	 * Returns the next id from the sequence <br/>
	 * // FIXME: This generation logic id probably is not scalable to the level of a service in CLOUD
	 * @param connection
	 */
	protected synchronized String getNextUID(DeviceConnection connection){
		
		int id = cmdCount.incrementAndGet();
		
		if(id > MAX_CMD_COUNT){
			cmdCount.set(1);
			id = 1;
		}		
				
		return Integer.toString(id);
	}

	private void sendWithTimeout(Command command, DeviceConnection connection){

        if(!connection.isConnected()){
            log.warn(connection.getClass().getSimpleName() + " not Connected!");
            return;
        }
		
		final SendTask sendTask = new SendTask(command, connection);
		
		executor.execute(new Runnable() {
			 
			@Override
			public void run() {
				Future<Boolean> future = executor.submit(sendTask);
				try {
					future.get(MAX_TIMEOUT, TimeUnit.SECONDS);
				} catch (TimeoutException e) {

                    log.error("Response not received ! Command:"+ sendTask.getCommand().getConnectionUUID() + ", Connection:" + sendTask.getConnection());

                    sendTask.restoreComand();

					// TODO: Notify not received...
					
				} catch (Exception e) {
					log.error(e.getMessage(), e);
                    future.cancel(true);
				} finally{
					 sendTask.restoreComand();
				}
			}
 
		});		
		

//      TODO: executor.shutdownNow();		
	}
	
	public void stop(){
		executor.shutdown();
	}
	
	private class SendTask implements Callable<Boolean>, ConnectionListener{
		
		private Command command;
		private DeviceConnection connection;
		
		private String originalID;
		private String newID;
		
		private Object lock = new Object();
		
		public SendTask(Command command, DeviceConnection connection) {
			super();
			this.command = command;
			this.connection = connection;
		}
		
		public Command getCommand() {
			return command;
		}
		
		public DeviceConnection getConnection() {
			return connection;
		}

		@Override
		public Boolean call() throws Exception {
			
			this.originalID = command.getUid();
			this.newID = getNextUID(connection); 
			
			connection.addListener(this);
			command.setUid(newID);
			
			log.debug("Send and Wait reponse :: Cmd.SEQ:<" + this.newID + ">, UID: "+this.originalID);
			
			connection.send(command);
			
			synchronized(lock){
				lock.wait();
			}
			
			return true;
		}
		
		/**
		 * release lock and restore ID
		 */
		public void restoreComand(){
			
			synchronized(lock){
				lock.notifyAll();
			}
			
			command.setUid(originalID);
			connection.removeListener(this);
		}

		@Override
        public void onMessageReceived(Message message, DeviceConnection connection) {

            if(!(message instanceof Command)){
                return;
            }

            Command received = (Command) message;

			if(received instanceof ResponseCommand){

				String requestUID = received.getUid();

				if(requestUID != null && requestUID.equals(this.newID)){

					log.debug("Response received :: Cmd.SEQ:<" + this.newID + ">, UID: "+this.originalID);

					restoreComand(); // release lock and restore ID

				}

			}

		}
		
		@Override
		public void connectionStateChanged(DeviceConnection connection,ConnectionStatus status) {
			// nothing
		}
		
	}
	
}

