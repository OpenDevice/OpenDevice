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
import java.util.*;

/**
 * This is the base class for device management and input and output connections. <br/>
 * After adding devices ({@link #addDevice(br.com.criativasoft.opendevice.core.model.Device)}) and connections {@link #addOutput(br.com.criativasoft.opendevice.connection.DeviceConnection)},
 * you can monitor the changes by adding a DeviceListener {@link #addListener(br.com.criativasoft.opendevice.core.model.DeviceListener)}.
 * @since 0.1.2
 * @date 23/06/2013
 */
public abstract class BaseDeviceManager implements ConnectionListener, DeviceManager {
	
	private static final Logger log = LoggerFactory.getLogger(BaseDeviceManager.class);
	
	/**Client connections: Websockets, http, rest, etc ...*/
	private MultipleConnection inputConnections;
	
	/** Connection with the physical modules (middleware) or a proxy  */
	private MultipleConnection outputConnections;
	
	private CommandDelivery delivery = new CommandDelivery(this);

    private DeviceDao deviceDao;

    private Message lastMessage;

    private DeviceListener thisListener = new DeviceManagerListener();

    @Override
    public Device findDeviceByUID(int deviceUID) {
        return getValidDeviceDao().getByUID(deviceUID);
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
        device.addListener(thisListener);
    }

    @Override
    public void addDevices(Collection<Device> devices) {
        for (Device device : devices){
            addDevice(device);
        }
    }

    @Override
    public Collection<Device> getDevices() {
        return getValidDeviceDao().listAll();
    }

    public boolean addListener(DeviceListener e) {
        return listeners.add(e);
    }

    public void addConnectionListener(ConnectionListener e) {
        if(inputConnections != null) inputConnections.addListener(e);
        if(outputConnections != null) outputConnections.addListener(e);
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
            if(! (streamConnection.getSerializer() instanceof CommandStreamSerializer)){
                streamConnection.setSerializer(new CommandStreamSerializer()); // data conversion..
                streamConnection.setStreamReader(new CommandStreamReader()); // data protocol..
            }
        }

		outputConnections.addConnection(connection);
	}


    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

        this.lastMessage = message;

        if (!(message instanceof Command)) {
            log.debug("Message received : " + message);
            return;
        }

        Command command = (Command) message;

        if(command.getApplicationID() == null || command.getApplicationID().length() == 0){
            command.setApplicationID(connection.getApplicationID());
        }

        log.debug("Command Received (from: " + connection.getClass().getSimpleName() + ") : " + CommandType.getByCode(command.getType().getCode()).toString());

        CommandType type = command.getType();

        // Comandos de ON_OFF e similares..
        if (DeviceCommand.isCompatible(type)) {

            DeviceCommand deviceCommand = (DeviceCommand) command;

            int deviceID = deviceCommand.getDeviceID();
            long value = deviceCommand.getValue();

            Device device = findDeviceByUID(deviceID);

            if (device != null) {
                device.setValue(value);
            }

            // Se foi recebido pelo modulo físico(Bluetooth/USB/Wifi), não precisa ser gerenciado pelo CommandDelivery
            // basta ser enviado para os clientes..
            if (outputConnections != null && outputConnections.exist(connection)) {
                try {
                    if(inputConnections != null){
                        log.debug("Sending to input connections...");
                        inputConnections.send(command); // Não precisa de time-out.
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Comando recebido pelos clientes (WebSockets / Rest / etc...)
            // Ele deve ser enviado para o modulo físico, e monitorar a resposta.
            if (inputConnections != null && inputConnections.exist(connection)) {
                log.debug("Sending to output connections...");
                try {
                    sendTo(deviceCommand, outputConnections);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else if (type == CommandType.GET_DEVICES) {

            GetDevicesRequest request = (GetDevicesRequest) message;
            List<Device> devices = new LinkedList<Device>();

            if(request.getFilter() <= 0) devices.addAll(getDevices());

            if(request.getFilter() == GetDevicesRequest.FILTER_BY_ID){
                Object id = request.getFilterValue();
                if(id instanceof Integer){
                    Device device = findDeviceByUID((Integer) id);
                    if(device != null) devices.add(device);
                }
            }

            GetDevicesResponse response = new GetDevicesResponse(devices, command.getConnectionUUID());
            response.setApplicationID(command.getApplicationID());

            try {

                connection.send(response);

            } catch (CommandException e) {
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

        } else if (type == CommandType.DEVICE_COMMAND_RESPONSE) {
            ResponseCommand responseCommand = (ResponseCommand) command;
            log.debug("ResponseStatus: " + responseCommand.getStatus());
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

    /**
     * Causes the currently executing thread to sleep (temporarily cease
     * execution)
     * @param millis
     * @see Thread#sleep(long)
     */
    protected void delay(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    final class DeviceManagerListener implements DeviceListener {

        @Override
        public void onDeviceChanged(Device device) {

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
    }


}
