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

import br.com.criativasoft.opendevice.connection.*;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandStatus;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import br.com.criativasoft.opendevice.core.connection.MultipleConnection;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class monitor sending commands to the device and your answers to see if the communication is correct.<br/>
 * It is used by the {@link br.com.criativasoft.opendevice.core.BaseDeviceManager DeviceManager}
 * @author Ricardo JL Rufino
 * @date 23/06/2013
 */
public class CommandDelivery implements ConnectionListener {
	
	private static final Logger log = LoggerFactory.getLogger(CommandDelivery.class);
	
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	private DeviceManager manager;
	
	private AtomicInteger cmdCount = new AtomicInteger(0);

    private final Set<SendTask> waitingTask =Collections.synchronizedSet(new HashSet<SendTask>());
	
	public static int MAX_CMD_COUNT = 255;
	

	public CommandDelivery(DeviceManager manager) {
		super();
		this.manager = manager;
	}


    public void addConnection(DeviceConnection connection)  {
        connection.addListener(this);
    }

	public void sendTo(Command command, DeviceConnection connection) throws IOException {

		command.setStatus(CommandStatus.DELIVERED);

        if(OpenDeviceConfig.get().isTenantsEnabled() && ! (connection instanceof MultipleConnection)){
            if(!"*".equals(connection.getApplicationID()) && ! command.getApplicationID().equals(connection.getApplicationID())){
                log.trace("Skipping command for connection : " + connection.getApplicationID());
                return;
            }
        }

        if(connection instanceof MultipleConnection){
            Set<DeviceConnection> connections = ((MultipleConnection) connection).getConnections();
            for (DeviceConnection deviceConnection : connections) {
                sendTo(command, deviceConnection);
            }
        }else if(connection instanceof StreamConnection && !(command instanceof ResponseCommand)){
            sendWithTimeout(command, connection);
        }else{
            connection.send(command);
        }

	}

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

        synchronized (waitingTask){
            for (SendTask sendTask : waitingTask) {
               sendTask.onMessageReceived(message, connection);
            }
        }

    }

    @Override
    public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {

    }

    /**
	 * Returns the next id from the sequence <br/>
	 * // FIXME: This generation logic id probably is not scalable to the level of a service in CLOUD
	 * @param connection
	 */
	protected synchronized int getNextUID(DeviceConnection connection){
		
		int id = cmdCount.incrementAndGet();
		
		if(id > MAX_CMD_COUNT){
			cmdCount.set(1);
			id = 1;
		}		
				
		return id;
	}

    public AtomicInteger getCmdCount() {
        return cmdCount;
    }

    private void sendWithTimeout(Command command, DeviceConnection connection){

        if(!connection.isConnected()){
            log.warn(connection.getClass().getSimpleName() + " not Connected!");
            return;
        }

        if(log.isTraceEnabled()) log.trace("Sends taks: {}, threads: {}", waitingTask.size(), Thread.activeCount());

		final SendTask sendTask = new SendTask(command, connection, this);
        waitingTask.add(sendTask);
        executor.execute(sendTask);

//      TODO: executor.shutdownNow();		
	}


    protected void removeTask(SendTask task){
        waitingTask.remove(task);
    }

	public void stop(){
		executor.shutdownNow();
	}
	
	private class SendTask implements Runnable, ConnectionListener{
		
		private final Command command;
		private DeviceConnection connection;
        private CommandDelivery commandDelivery;

        private int originalID;
		private int newID;
		
        private long start;

        public SendTask(Command command, DeviceConnection connection, CommandDelivery commandDelivery) {
			super();
			this.command = command;
			this.connection = connection;
            this.commandDelivery = commandDelivery;
        }
		
		public Command getCommand() {
			return command;
		}

        @Override
        public void run() {

            this.originalID = command.getTrackingID();
            this.newID = getNextUID(connection);

            command.setTrackingID(newID);

            log.debug("Send and Wait :: "+command.getType() + ", ID:<" + this.newID + ">");

            try {

                start = System.currentTimeMillis();
                connection.send(command);

                // Wait for response (release look) in onMessageReceived
                synchronized(command){
                    command.wait(command.getTimeout());
                }

                // If not received response...
                if(command.getStatus() == CommandStatus.DELIVERED){
                    command.setStatus(CommandStatus.FAIL);
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                commandDelivery.removeTask(this);
            }

        }



		@Override
        public void onMessageReceived(Message message, DeviceConnection connection) {

            if(!(message instanceof ResponseCommand)){
                return;
            }

            ResponseCommand received = (ResponseCommand) message;

            int requestUID = received.getTrackingID();

            if(requestUID == this.newID){

                long time = System.currentTimeMillis() - start;
                log.debug("Response (" + received.getType() + ":" +received.getStatus()+") :: ID:<" + this.newID + "> , time: " + time + "ms");

                command.setResponse(received);

                synchronized(command){
                    command.notifyAll();
                }

                command.setStatus(CommandStatus.SUCCESS); // FIXME: precisa ser o status de acorodo com a resposta,

                // pois pode ser um Internal Error.
                command.setTrackingID(originalID);

            }


		}
		
		@Override
		public void connectionStateChanged(DeviceConnection connection,ConnectionStatus status) {
			// nothing
		}
		
	}
	
}

