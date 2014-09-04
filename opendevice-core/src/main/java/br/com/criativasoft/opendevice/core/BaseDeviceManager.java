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
import br.com.criativasoft.opendevice.connection.StreamConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.*;
import br.com.criativasoft.opendevice.core.connection.MultipleConnection;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseDeviceManager implements ConnectionListener, DeviceManager, DeviceListener {
	
	private static final Logger log = LoggerFactory.getLogger(BaseDeviceManager.class);
	
	/**Client connections: Websockets, http, rest, etc ...*/
	private MultipleConnection inputConnections;
	
	/** Connection with the physical modules (middleware) or a proxy  */
	private MultipleConnection outputConnections;
	
	private CommandDelivery delivery = new CommandDelivery(this);

    private DeviceDao deviceDao;

    private Message lastMessage;

    @Override
    public Device findDeviceByUID(long deviceID) {
        return getValidDeviceDao().getByUID(deviceID);
    }

    public void setDeviceDao(DeviceDao deviceDao) {
        this.deviceDao = deviceDao;
    }

    public DeviceDao getDeviceDao() {
        return deviceDao;
    }

    public DeviceDao getValidDeviceDao() {
        if(deviceDao == null) throw new IllegalStateException("deviceDao is NULL !");
        return deviceDao;
    }

    private volatile Set<DeviceListener> listeners = new HashSet<DeviceListener>();

    @Override
    public void addDevice(Device device) {
        getValidDeviceDao().persist(device);
        device.addListener(this);
    }

    @Override
    public Collection<Device> getDevices() {
        return getValidDeviceDao().listAll();
    }

    public boolean addListener(DeviceListener e) {
        return listeners.add(e);
    }

    /**
     * Notify All Listeners about received command.
     */
    public void notifyListeners(Device device) {

        if(listeners.isEmpty()) return;

        for (final DeviceListener listener : listeners) {
            listener.onDeviceChanged(device);
        }
    }

    protected void initInputConnections(){
		inputConnections = new MultipleConnection();
		inputConnections.addListener(this);
	}
	
	protected void initOutputConnections(){
		outputConnections = new MultipleConnection();
		outputConnections.addListener(this);
	}

    @Override
    public void connect() throws IOException {
        connectAll();
    }

    protected void connectAll() throws ConnectionException{
        if(outputConnections != null) outputConnections.connect();
        if(inputConnections != null) inputConnections.connect();
	}

    protected void disconnectAll(){

        if(inputConnections != null) {
            try {
                inputConnections.disconnect();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }

        if(outputConnections != null){
            try {
                outputConnections.disconnect();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }

    }
	
	public void addInput(DeviceConnection connection){
		
		if(inputConnections == null) initInputConnections();
		
		inputConnections.addConnection(connection);
		
	}

	public void addOutput(DeviceConnection connection){
		
		if(outputConnections == null) initOutputConnections();

        if(connection instanceof StreamConnection){
            StreamConnection streamConnection = (StreamConnection) connection;
            streamConnection.setSerializer(new CommandStreamSerializer()); // data conversion..
            streamConnection.setStreamReader(new CommandStreamReader()); // data protocol..
        }
		
		outputConnections.addConnection(connection);
	}


    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

        this.lastMessage = message;

        if(!(message instanceof Command)){
            log.debug("Message received : " + message);
            return;
        }

        Command command = (Command) message;

		log.debug("Command received : " + CommandType.getByCode(command.getType().getCode()).toString());
		
		CommandType type = command.getType();

		if (type == CommandType.GET_DEVICES) {
			
			GetDevicesResponse response = new GetDevicesResponse(getDevices(), command.getConnectionUUID());

			try {
				
				connection.send(response);
				
			} catch (CommandException e) {
				log.error(e.getMessage(), e);
			} catch (IOException e) {
                log.error(e.getMessage(), e);
            }

        // Comandos de ON_OFF e similares..
		}else if(DeviceCommand.isCompatible(type)){
			
			DeviceCommand deviceCommand = (DeviceCommand) command;
			
			int deviceID = deviceCommand.getDeviceID();
			long value = deviceCommand.getValue();
			
			Device device = findDeviceByUID(deviceID);
			
			if(device != null){
				device.setValue(value);
			}
			
			// Se foi recebido pelo modulo físico(Bluetooth/USB/Wifi), não precisa ser gerenciado pelo CommandDelivery
			// basta ser enviado para os clientes..
			if(outputConnections != null && outputConnections.exist(connection)){
				log.debug("Sending to input connections...");
                try {
                    inputConnections.send(command); // Não precisa de time-out.
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
			
			// Comando recebido pelos clientes (WebSockets / Rest / etc...)
			// Ele deve ser enviado para o modulo físico, e monitorar a resposta. 
			if(inputConnections != null && inputConnections.exist(connection)){
				log.debug("Sending to output connections...");
                try {
                    sendTo(deviceCommand, outputConnections);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
			
		}		

	}


    @Override
    public void onDeviceChanged(Device device) {

        notifyListeners(device);

        try {

            // Ignore changes fired in 'onMessageReceived'
            if(lastMessage instanceof  DeviceCommand){

                DeviceCommand command = (DeviceCommand) lastMessage;

                if(device.getUid() == command.getDeviceID() && device.getValue() == command.getValue()){
                    return;
                }
            }

            if (device.getType() == DeviceType.DIGITAL) {
                DeviceCommand cmd = new DeviceCommand(CommandType.ON_OFF, device.getUid(), device.getValue());
                send(cmd);
            }

            if (device.getType() == DeviceType.ANALOG) {
                DeviceCommand cmd = new DeviceCommand(CommandType.ANALOG, device.getUid(), device.getValue());
                send(cmd);
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
	
	protected void sendTo(Command command, DeviceConnection connection) throws  IOException {
		if(connection != null && connection.isConnected()){
			delivery.sendTo(command, connection);
		}
	}
	
	@Override
	public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
		log.debug("connectionStateChanged :: "+ connection.getClass().getSimpleName() + ", status = " + status);
	}
	

	@Override
	public void send(Command command) throws IOException {
		
		if(outputConnections != null){
			delivery.sendTo(command, outputConnections);
		}
		
		if(inputConnections != null){
			delivery.sendTo(command, inputConnections);
		}
		
	}




}
