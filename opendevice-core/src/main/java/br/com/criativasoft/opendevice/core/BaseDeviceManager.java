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
import br.com.criativasoft.opendevice.connection.discovery.DiscoveryService;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.*;
import br.com.criativasoft.opendevice.core.connection.EmbeddedGPIO;
import br.com.criativasoft.opendevice.core.connection.MultipleConnection;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.discovery.DiscoveryServiceImpl;
import br.com.criativasoft.opendevice.core.event.EventHookManager;
import br.com.criativasoft.opendevice.core.extension.OpenDeviceExtension;
import br.com.criativasoft.opendevice.core.filter.CommandFilter;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.listener.OnDeviceChangeListener;
import br.com.criativasoft.opendevice.core.metamodel.DeviceHistoryQuery;
import br.com.criativasoft.opendevice.core.model.*;
import br.com.criativasoft.opendevice.core.model.test.DeviceCategoryRegistry;
import br.com.criativasoft.opendevice.core.model.test.GenericDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * This is the base class for device management and input and output connections. <br/>
 * After adding devices ({@link #addDevice(br.com.criativasoft.opendevice.core.model.Device)}) and connections {@link #addOutput(br.com.criativasoft.opendevice.connection.DeviceConnection)},
 * you can monitor the changes by adding a DeviceListener {@link #addListener(DeviceListener)}.
 * @since 0.1.2
 * @date 23/06/2013
 */
public abstract class BaseDeviceManager implements DeviceManager {

    private static DeviceManager instance;
	
	private static final Logger log = LoggerFactory.getLogger(BaseDeviceManager.class);

    private List<OpenDeviceExtension> extensions  = new LinkedList<OpenDeviceExtension>();

    private volatile Set<DeviceListener> listeners = new HashSet<DeviceListener>();

	/** Client connections: Websockets, http, rest, etc ...*/
	private MultipleConnection inputConnections = new MultipleConnection();
	
	/** Connection with the physical modules (middleware) or a proxy  */
	private MultipleConnection outputConnections = new MultipleConnection();

    private Set<CommandFilter> filters = new LinkedHashSet<CommandFilter>();
	
	private CommandDelivery delivery = new CommandDelivery(this);

    private DiscoveryService discoveryService = new DiscoveryServiceImpl();

    private DeviceCategoryRegistry deviceCategoryRegistry = new DeviceCategoryRegistry();

    // FIXME: remove from here
    private EventHookManager eventManager;

    private DataManager dataManager;

    private Message lastMessage;

    private List<Device> partialDevices = new LinkedList<Device>(); // Devices from partial GetDevicesResponse

    public BaseDeviceManager(){
        instance = this;
        eventManager = new EventHookManager();

        addListener(eventManager);

        // Load Extensions
        loadExtensions();

    }

    /**
     * @see OpenDeviceExtension
     */
    protected void loadExtensions(){

        try{
            Class.forName("java.util.ServiceLoader");
        }catch(ClassNotFoundException ex){
            log.error("This java version don't support dynamic loading (ServiceLoader), you need use direct class ex: new BluetoothConnection(addr)");
        }

        // lockup....
        ServiceLoader<OpenDeviceExtension> service = ServiceLoader.load(OpenDeviceExtension.class);

        Iterator<OpenDeviceExtension> iterator = service.iterator();

        if(iterator.hasNext()){
            OpenDeviceExtension extension = iterator.next();
            log.info("Loading Extension: " + extension.getName() + ", class: " + extension.getClass());
            extension.init(this);
            extensions.add(extension);
        }

    }

    /**
     * Get shared global instance of DevinceManager.
     * @return
     */
    public static BaseDeviceManager getInstance() {
        return (BaseDeviceManager) instance;
    }

    public EventHookManager getEventManager() {
        return eventManager;
    }

    public DiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    public CommandDelivery getCommandDelivery() {
        return delivery;
    }

    public DeviceCategory getCategory(Class<? extends DeviceCategory> klass) {
        return deviceCategoryRegistry.getCategory(klass);
    }

    public DeviceCategory getCategory(int code) {
        return deviceCategoryRegistry.getCategory(code);
    }

    public void addCategory(Class<? extends DeviceCategory> klass) {
        deviceCategoryRegistry.add(klass);
    }

    @Override
    public Device findDeviceByUID(int deviceUID) {

        if(deviceUID <= 0) return null;

        Device device = getCurrentContext().getDeviceByUID(deviceUID);

        return device;
    }

    @Override
    public Device findDeviceByName(String name) {

        if(name == null || name.length() == 0) return null;

        Device device = getCurrentContext().getDeviceByName(name);

        return device;
    }

    @Override
    public List<DeviceHistory> getDeviceHistory(DeviceHistoryQuery query) {
        return getValidDeviceDao().getDeviceHistory(query);
    }

    @Override
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    @Override
    public void setDeviceDao(DeviceDao deviceDao) {
        getDataManager().setDeviceDao(deviceDao);
    }

    @Override
    public DeviceDao getDeviceDao() {
        return getDataManager().getDeviceDao();
    }

    /**
     *
     * @return true if transaction already active by another thread/component
     */
    public boolean transactionBegin(){ return false; }

    public void transactionEnd(){}


    public TenantContext getCurrentContext(){
        return TenantProvider.getCurerntContext();
    }

    public DeviceDao getValidDeviceDao() {
        if(getDataManager().getDeviceDao() == null) throw new IllegalStateException("deviceDao is NULL !");
        return getDataManager().getDeviceDao();
    }

    @Override
    public void addDevice(Device device) {
        if(device == null) throw new IllegalArgumentException("Device is null");
        if(findDeviceByUID(device.getUid()) == null) {
            getValidDeviceDao().persist(device);
            getCurrentContext().addDevice(device); // add to chache.
            for(DeviceListener listener: listeners){
                listener.onDeviceRegistred(device);
            }
        }
    }

    @Override
    public void removeDevice(Device device) {
        if(device == null) throw new IllegalArgumentException("Device is null");

        if(device instanceof Board){
            Set<PhysicalDevice> devices = ((Board) device).getDevices();
            for (PhysicalDevice physicalDevice : devices) {
                removeDevice(physicalDevice);
            }
        }

        device = getValidDeviceDao().getById(device.getId());
        getValidDeviceDao().delete(device);
        getCurrentContext().removeDevice(device); // remove from cache
    }

    @Override
    public void addDevices(Collection<Device> devices) {
        for (Device device : devices){
            addDevice(device);
        }
    }

    @Override
    public Collection<Device> getDevices() {
        return getCurrentContext().getDevices();
    }

    public boolean addListener(DeviceListener e) {
        return listeners.add(e);
    }

    @Override
    public void addFilter(CommandFilter filter) {
        filters.add(filter);
    }

    public void onConnected(OnConnectListener e) {
        addConnectionListener(e);
    }

    public void addConnectionListener(ConnectionListener e) {
        if(inputConnections != null) inputConnections.addListener(e);
        if(outputConnections != null) outputConnections.addListener(e);
    }

    /**
     * Notify All Listeners about device change
     * @param sync - sync state with server
     */
    public synchronized void notifyListeners(Device device, boolean sync) {

        boolean alreadyExist =transactionBegin();
        saveDeviceHistory(device);
        if(!alreadyExist) transactionEnd();

        if(sync){

            try {
                CommandType type = DeviceCommand.getCommandType(device.getType());
                DeviceCommand cmd = new DeviceCommand(type, device.getUid(), device.getValue());
                send(cmd);
            }catch (IOException ex){
                log.error(ex.getMessage(), ex);
            }

        }

        // Individual Listeners
        for (final OnDeviceChangeListener listener : device.getListeners()) {
            listener.onDeviceChanged(device);
        }

        if(listeners.isEmpty()) return;

        // Global Listeners
        for (final DeviceListener listener : listeners) {
            listener.onDeviceChanged(device);
        }
    }

    protected void initInputConnections(){
		inputConnections.addListener(connectionListener);
	}
	
	protected void initOutputConnections(){
		outputConnections.addListener(connectionListener);
	}

    @Override
    public void connect() throws IOException {

        connectAll();

    }

    @Override
    public void stop() {
        try {
            disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getCommandDelivery().stop();
        getDiscoveryService().stop();
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
     * @param request
     */
    protected void syncDevices(DeviceConnection connection, GetDevicesRequest request){

        if(connection instanceof EmbeddedGPIO){
            EmbeddedGPIO gpioConn = (EmbeddedGPIO) connection;
            Collection<Device> devices = getDevices();
            if(devices != null){
                for (Device device : devices){
                    if(device instanceof PhysicalDevice) gpioConn.attach((PhysicalDevice) device);
                }
            }else{
                log.warn("None device registered !");
            }
        }

        if((connection instanceof StreamConnection || connection instanceof IWSConnection )
                && outputConnections.exist(connection)){
            try {
                sendTo(request, connection);
            } catch (IOException e) {}
        }

        if(connection instanceof MultipleConnection){
            Set<DeviceConnection> connections = outputConnections.getConnections();
            for (DeviceConnection current : connections) {
                syncDevices(current, request);
            }
        }

    }


	public void addInput(DeviceConnection connection){
		
		if(inputConnections.getSize() == 0) initInputConnections();

        if(inputConnections.exist(connection)){
            log.info("Connection with ID: " + connection.getUID()+ " already exist !");
            return;
        }

        if(connection instanceof StreamConnection){
            StreamConnection streamConnection = (StreamConnection) connection;
            if(! (streamConnection.getSerializer() instanceof CommandStreamSerializer)){
                streamConnection.setStreamReader(new CommandStreamReader()); // data protocol..
            }
        }

        connection.setSerializer(new CommandStreamSerializer()); // data conversion..
        connection.setConnectionManager(this);
        connection.setApplicationID(TenantProvider.getCurrentID());
		inputConnections.addConnection(connection);
		
	}

    @Override
    public void removeInput(DeviceConnection connection) {
        log.info("Remove input connection: {}", connection);
        inputConnections.removeConnection(connection);
    }

    @Override
    public void removeOutput(DeviceConnection connection) {
        log.info("Remove output connection: {}", connection);
        outputConnections.removeConnection(connection);
    }

    public void addOutput(DeviceConnection connection){
		
		if(outputConnections.getSize() == 0) initOutputConnections();

        if(outputConnections.exist(connection)){
            log.info("Connection with ID: " + connection.getUID()+ " already exist !");
            return;
        }

        if(connection instanceof StreamConnection){
            StreamConnection streamConnection = (StreamConnection) connection;
            if(! (streamConnection.getSerializer() instanceof CommandStreamSerializer)){
                streamConnection.setStreamReader(new CommandStreamReader()); // data protocol..
            }
        }

        if(connection instanceof ITcpConnection){
            ((ITcpConnection) connection).setDiscoveryService(discoveryService);
        }

        delivery.addConnection(connection);

        if(connection.getSerializer() == null){
            connection.setSerializer(new CommandStreamSerializer()); // data conversion..
        }

        connection.setApplicationID(TenantProvider.getCurrentID());
        connection.setConnectionManager(this);
		outputConnections.addConnection(connection);
	}


	protected void sendTo(Command command, DeviceConnection connection) throws  IOException {
		if(connection != null && connection.isConnected()){
            if(command.getApplicationID() == null) command.setApplicationID(connection.getApplicationID());
			delivery.sendTo(command, connection);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see br.com.criativasoft.opendevice.core.DeviceManager#send(br.com.criativasoft.opendevice.core.command.Command)
	 */
	@Override
	public void send(Command command) throws IOException {
        send(command, true, true);
	}

    /*
     * (non-Javadoc)
     * @see br.com.criativasoft.opendevice.core.DeviceManager#send(br.com.criativasoft.opendevice.core.command.Command)
     */
    public void send(Command command, boolean output, boolean input) throws IOException {

        if(command.getApplicationID() == null) command.setApplicationID(TenantProvider.getCurrentID());

        if(output && outputConnections.hasConnections()){

            Set<DeviceConnection> connections = outputConnections.getConnections();
            for (DeviceConnection connection : connections) {
                delivery.sendTo(command, connection);
            }

        }

        if(input && inputConnections.hasConnections()){

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
    public void delay(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    public boolean isTenantsEnabled(){
        return OpenDeviceConfig.get().isTenantsEnabled();
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

    public MultipleConnection getOutputConnections() {
        return outputConnections;
    }

    public MultipleConnection getInputConnections() {
        return inputConnections;
    }

    @Override
    public DeviceConnection findConnection(String uid) {

        DeviceConnection connection = outputConnections.findConnection(uid);

        if(connection != null) return connection;

        return inputConnections.findConnection(uid);
    }

    protected OpenDeviceConfig getConfig(){
        return OpenDeviceConfig.get();
    }

    protected void saveDeviceHistory(Device device){
//        transactionBegin();
        DeviceHistory history = new DeviceHistory();
        history.setDeviceID(device.getId());
        history.setValue(device.getValue());
        history.setTimestamp(System.currentTimeMillis());
        getDeviceDao().persistHistory(history);
//        transactionEnd();
    }

    private void onMessageReceivedImpl(Message message, DeviceConnection connection){

        Command command = (Command) message;

        OpenDeviceConfig config = OpenDeviceConfig.get();

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


        CommandType type = command.getType();

        if(log.isDebugEnabled()) log.debug("Command Received - Type: {} (from: " + connection.toString() + ")", type.toString());

        // Comandos de DIGITAL e similares..
        if (DeviceCommand.isCompatible(type) || type == CommandType.INFRA_RED) {

            DeviceCommand deviceCommand = (DeviceCommand) command;

            int deviceID = deviceCommand.getDeviceID();
            long value = deviceCommand.getValue();

            Device device = findDeviceByUID(deviceID);

            if(log.isDebugEnabled()) log.debug("Device Change. ID:{}, Value:{}", deviceID, value);

            if(device != null){
                if(device.getType() == Device.NUMERIC){ // fire the event 'onChange' every time a reading is taken
                    device.setValue(value, false);
                }else if (device.getValue() != value){ // for ANALOG, DIGITAL.
                    device.setValue(value, false);
                }else{ // not changed
                    return;
                }
            }

            // If it is received by the physical module (Bluetooth / USB / Wifi), need not be managed by CommandDelivery
            // just be sent to client conenctions ..
            if (outputConnections != null && outputConnections.exist(connection)) {
                try {
                    if(inputConnections != null && inputConnections.getSize() > 0){
                        log.debug("Sending to input connections...");
                        inputConnections.send(command);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Command received by clients (WebSockets / Rest / etc ...)
            // It must be sent to the physical module, and monitor the response.
            if (inputConnections != null && inputConnections.exist(connection)) {

                if(outputConnections.hasConnections()){
                    log.debug("Sending to output connections ("+outputConnections.getSize()+")...");
                    try {
                        sendTo(deviceCommand, outputConnections);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(config.isBroadcastInputs()){
                    try {
                        Set<DeviceConnection> inputs = inputConnections.getConnections();
                        for (DeviceConnection input : inputs) {
                            if(input != connection) inputConnections.send(deviceCommand);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (type == CommandType.SET_PROPERTY) {

            SetPropertyCommand cmd = (SetPropertyCommand) command;

            int deviceID = cmd.getDeviceID();

            Device device = findDeviceByUID(deviceID);

            if(device instanceof GenericDevice){
                GenericDevice genericDevice = (GenericDevice) device;
                genericDevice.setProperty(cmd.getProperty(), cmd.getValue());
                try {
                    if(genericDevice.getConnection() == null) log.warn("Device '" + device + "'  has no connection !");
                    else genericDevice.getConnection().send(cmd);
                } catch (IOException e) {
                    e.printStackTrace(); // TODO: melhor tratamento..
                }
            }

            // FIXME ? oque precisa ser feito ainda
            // no caso da camera não precisar jogar em todos imouts
            // acho que agora vai ser a hora de fazer o mapeamento dos devices.
        } else if (type == CommandType.ACTION) {

            ActionCommand cmd = (ActionCommand) command;

            Device device = findDeviceByUID(cmd.getDeviceID());

            if(device instanceof GenericDevice){
                GenericDevice genericDevice = (GenericDevice) device;
                // TODO: falta a logica interna de execucao das actions... (usar listeners normais ?)
                //genericDevice.setProperty(cmd.getAction(), cmd.getValue());
                try {
                    genericDevice.getConnection().send(cmd);
                } catch (IOException e) {
                    e.printStackTrace(); // TODO: melhor tratamento..
                }
            }


        } else if (type == CommandType.PING_REQUEST) {

            Command pingResponse = new SimpleCommand(CommandType.PING_RESPONSE, 0);
            try {
                connection.send(pingResponse);
            } catch (IOException e) {
            }

        } else if (type == CommandType.GET_DEVICES) {

            GetDevicesRequest request = (GetDevicesRequest) message;

            // Received GET_DEVICES with ForceSync
            if(request.isForceSync() && inputConnections.exist(connection)){

                if(outputConnections.hasConnections()){
                    log.debug("Sending to output connections...");
                    syncDevices(outputConnections, request);
                }

            }else{
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
                response.setConnectionUUID(command.getConnectionUUID());

                try {

                    connection.send(response);

                } catch (CommandException e) {
                    log.error(e.getMessage(), e);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }

        } else if (type == CommandType.DEVICE_COMMAND_RESPONSE) {

//                ResponseCommand responseCommand = (ResponseCommand) command;
            // log.debug("ResponseStatus: " + responseCommand.getStatus());

        } else if (type == CommandType.CONNECT_RESPONSE) {

            ResponseCommand response = (ResponseCommand) command;

            if(response.getStatus() == CommandStatus.UNAUTHORIZED){
                try {
                    log.info("The access information is invalid or are not configured (Authorization Required)");
                    connection.disconnect();
                } catch (ConnectionException e) {
                }
            }

        } else if (type == CommandType.GET_DEVICES_RESPONSE) {

            GetDevicesResponse response = (GetDevicesResponse) command;

            partialDevices.addAll(response.getDevices());

            if(!response.isLast()){
                return;
            }

            Collection<Device> loadDevices = partialDevices;

            // Resolver Parent (Boards)
            GetDevicesResponse.resolveParents(loadDevices);

            log.info("Loaded Devices: " + loadDevices.size() + " , from: " + connection.getClass().getSimpleName());
            DeviceDao dao = getValidDeviceDao();
            boolean syncIds = false; // firmware use dynamic ids
            int nextID = -1;

            for (Device device : loadDevices) {
                log.debug(" - " + device.toString());

                Device found = findDeviceByUID(device.getUid());

                // Fallback, recovery previous device cleared/replaced
                // If the name/id does not match, the name has priority
                if(found == null || !found.getName().equals(device.getName())){
                    found = findDeviceByName(device.getName());
                    device.setUID(0); // clear, need resyc
                }

                if(found == null){

                    // Device not have ID, Get next ID from database
                    if(device.getUid() <= 0){
                        if(nextID == -1) nextID = dao.getNextUID();
                        syncIds = true;
                        device.setUID(nextID++);
                        // TODO: Notify Client Applications ??
                    }

                    device.setApplicationID(response.getApplicationID());
                    if(device.getCategory() != null) {
                        device.setCategory(dao.getCategoryByCode(device.getCategory().getCode())); // update reference
                    }
                    addDevice(device);
                }else{

                    // Firmware has ben cleared/replaced
                    // This will help recover IDs.
                    if(device.getUid() <= 0 || device.getUid() != found.getUid()){
                        device.setUID(found.getUid());
                        syncIds = true;
                    }

                    found.setValue(device.getValue());

                }

            }

            if(syncIds){

                try {
                    sendTo(new SyncDevicesIdCommand(loadDevices), connection);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }

            }


            partialDevices.clear();
        }
    }

    private ConnectionListener connectionListener = new ConnectionListener() {

        @Override
        public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
            log.debug("connectionStateChanged :: "+ connection.getClass().getSimpleName() + ", status = " + status);

            // Force sync devices with physical modules on connect.
            if(status == ConnectionStatus.CONNECTED && outputConnections.exist(connection)){
                GetDevicesRequest request = new GetDevicesRequest();
                request.setApplicationID(OpenDeviceConfig.LOCAL_APP_ID);
                syncDevices(connection, request);
            }
        }


        @Override
        public void onMessageReceived(Message message, DeviceConnection connection) {

            if(message == null) return;

            lastMessage = message;

            if (!(message instanceof Command)) {
                log.debug("Message received : " + message);
                return;
            }

            transactionBegin();

            try{
                onMessageReceivedImpl(message, connection);
            } finally {
                transactionEnd();
            }

        }
    };


}
