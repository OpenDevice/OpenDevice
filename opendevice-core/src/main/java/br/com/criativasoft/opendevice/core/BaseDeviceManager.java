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
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.*;
import br.com.criativasoft.opendevice.core.connection.EmbeddedGPIO;
import br.com.criativasoft.opendevice.core.connection.MultipleConnection;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.filter.CommandFilter;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.DeviceType;

import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
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

    public static DeviceManager instance;
	
	private static final Logger log = LoggerFactory.getLogger(BaseDeviceManager.class);
	
	/**Client connections: Websockets, http, rest, etc ...*/
	private MultipleConnection inputConnections = new MultipleConnection();
	
	/** Connection with the physical modules (middleware) or a proxy  */
	private MultipleConnection outputConnections = new MultipleConnection();

    private Set<CommandFilter> filters = new LinkedHashSet<CommandFilter>();
	
	private CommandDelivery delivery = new CommandDelivery(this);

    private DeviceDao deviceDao;

    private Message lastMessage;

    private DeviceListener thisListener = new DeviceManagerListener();

    public BaseDeviceManager(){
        instance = this;
    }

    /**
     * Get shared global instance of DevinceManager.
     * @return
     */
    public static DeviceManager getInstance() {
        return instance;
    }

    @Override
    public Device findDeviceByUID(int deviceUID) {
        return getValidDeviceDao().getByUID(deviceUID);
    }

    @Override
    public void setDeviceDao(DeviceDao deviceDao) {
        this.deviceDao = deviceDao;
    }

    @Override
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

    @Override
    public void addFilter(CommandFilter filter) {
        filters.add(filter);
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
		inputConnections.addListener(this);
	}
	
	protected void initOutputConnections(){
		outputConnections.addListener(this);
	}

    @Override
    public void connect() throws IOException {

        if(getDevices().isEmpty()) log.warn("No devices registed ! (TIP: Create "+this.getClass().getSimpleName()+" instance before devices or call addDevice !");

        connectAll();
    }

    @Override
    public void disconnect() throws IOException {
        if(outputConnections != null) outputConnections.disconnect();
        if(inputConnections != null) inputConnections.disconnect();
    }

    @Override
    public void connect(DeviceConnection connection) throws IOException {
        addOutput(connection);
        connectAll();
    }

    protected void connectAll() throws ConnectionException{

        if(outputConnections != null) outputConnections.connect();
        if(inputConnections != null) inputConnections.connect();

	}

    /**
     * Synchronize devices with connections that require additional information such as GPIO.
     * (An example is the raspberry that already has support built GPIO)
     * @param connection
     */
    protected void syncDevices(DeviceConnection connection){

        if(connection instanceof EmbeddedGPIO){
            EmbeddedGPIO gpioConn = (EmbeddedGPIO) connection;
            Collection<Device> devices = getDevices();
            if(devices != null){
                for (Device device : devices) gpioConn.attach(device);
            }else{
                log.warn("None device registered !");
            }
        }
        if(connection instanceof StreamConnection){
            try {
                sendTo(new GetDevicesRequest(), connection);
            } catch (IOException e) {}
        }

    }

    protected void disconnectAll(){

        try {
            inputConnections.disconnect();
        } catch (ConnectionException e) {
            e.printStackTrace();
        }

        try {
            outputConnections.disconnect();
        } catch (ConnectionException e) {
            e.printStackTrace();
        }

    }
	
	public void addInput(DeviceConnection connection){
		
		if(inputConnections.getSize() == 0) initInputConnections();

        connection.setConnectionManager(this);
        connection.setApplicationID(TenantProvider.getCurrentID());
		inputConnections.addConnection(connection);
		
	}

	public void addOutput(DeviceConnection connection){
		
		if(outputConnections.getSize() == 0) initOutputConnections();

        if(connection instanceof StreamConnection){
            StreamConnection streamConnection = (StreamConnection) connection;
            if(! (streamConnection.getSerializer() instanceof CommandStreamSerializer)){
                streamConnection.setSerializer(new CommandStreamSerializer()); // data conversion..
                streamConnection.setStreamReader(new CommandStreamReader()); // data protocol..
            }
        }

        connection.setApplicationID(TenantProvider.getCurrentID());
        connection.setConnectionManager(this);
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


        if(!filters.isEmpty()){

            for (CommandFilter filter : filters) {

                if(!filter.filter(command, connection)){
                    if(log.isTraceEnabled()) log.debug("Message blocked by filter: " + filter.getClass().getSimpleName());
                    return;
                }

            }

        }

        if(log.isDebugEnabled()) log.debug("Command Received - Type: {} (from: " + connection.getClass().getSimpleName() + ")", CommandType.getByCode(command.getType().getCode()).toString());

        CommandType type = command.getType();

        // Comandos de DIGITAL e similares..
        if (DeviceCommand.isCompatible(type) || type == CommandType.INFRA_RED) {

            DeviceCommand deviceCommand = (DeviceCommand) command;

            int deviceID = deviceCommand.getDeviceID();
            long value = deviceCommand.getValue();

            Device device = findDeviceByUID(deviceID);

            if(device != null && device.getValue() == value) return; // exist but not changed !

            if (device != null && device.getValue() != value) {

                device.setValue(value);
                notifyListeners(device);

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
                    if(outputConnections.hasConnections()){
                        log.debug("Sending to output connections...");
                        try {
                            sendTo(deviceCommand, outputConnections);
                        } catch (IOException e) {
                            e.printStackTrace();
                    }
                }
            }

        } else if (type == CommandType.GET_DEVICES) {

            GetDevicesRequest request = (GetDevicesRequest) message;
            List<Device> devices = new LinkedList<Device>();

            if(request.getFilter() <= 0) devices.addAll(getDevices());

            if(request.getFilter() == GetDevicesRequest.FILTER_BY_ID){
                Object id = request.getFilterValue();
                if(id instanceof Integer || id instanceof Long){
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

        } else if (type == CommandType.GET_DEVICES_RESPONSE) {

            GetDevicesResponse response = (GetDevicesResponse) command;
            Collection<Device> loadDevices = response.getDevices();

            log.debug("Loaded Devices: " + loadDevices.size());
            DeviceDao dao = getValidDeviceDao();

            for (Device device : loadDevices) {
                Device found = dao.getByUID(device.getUid());
                if(found == null){
                    addDevice(found);
                }else{
                    found.setValue(device.getValue());
                }

            }
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

        if(status == ConnectionStatus.CONNECTED && connection instanceof StreamConnection){

            syncDevices(connection);

        }
	}
	

	/*
	 * (non-Javadoc)
	 * @see br.com.criativasoft.opendevice.core.DeviceManager#send(br.com.criativasoft.opendevice.core.command.Command)
	 */
	@Override
	public void send(Command command) throws IOException {

        if(outputConnections.hasConnections()){

            Set<DeviceConnection> connections = outputConnections.getConnections();
            for (DeviceConnection connection : connections) {
                delivery.sendTo(command, connection);
            }

		}
		
		if(inputConnections.hasConnections()){

            Set<DeviceConnection> connections = inputConnections.getConnections();
            for (DeviceConnection connection : connections) {
                delivery.sendTo(command, connection);
            }

		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see br.com.criativasoft.opendevice.core.DeviceManager#sendCommand(java.lang.String, java.lang.Object[])
	 */
	@Override
    public void sendCommand( String commandName , Object ... params ) throws IOException {
        send(new UserCommand(commandName, params));
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
                    DeviceCommand cmd = new DeviceCommand(CommandType.DIGITAL, device.getUid(), device.getValue());
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

    /**
     * Checks whether a connection has been added
     */
    @Override
    public boolean hasConnections(){
        int size = inputConnections.getSize();
        size += outputConnections.getSize();
        return size > 0;
    }

    /**
     * Checks if a connection is active. Considers the input and output
     */
    @Override
    public boolean isConnected(){

        if(hasConnections()){

            if(inputConnections.isConnected()) return true;
            if(outputConnections.isConnected()) return true;

        }

        return false;
    }

    @Override
    public Collection<DeviceConnection> getConnections() {

        Set<DeviceConnection> newList = new LinkedHashSet<DeviceConnection>();

        newList.addAll(inputConnections.getConnections());
        newList.addAll(outputConnections.getConnections());

        return newList;
    }

    protected OpenDeviceConfig getConfig(){
        return OpenDeviceConfig.get();
    }
}
