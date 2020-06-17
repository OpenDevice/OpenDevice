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
import br.com.criativasoft.opendevice.core.extension.PersistenceExtension;
import br.com.criativasoft.opendevice.core.extension.PluginLoader;
import br.com.criativasoft.opendevice.core.extension.ViewExtension;
import br.com.criativasoft.opendevice.core.filter.CommandFilter;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.listener.OnDeviceChangeListener;
import br.com.criativasoft.opendevice.core.metamodel.DeviceHistoryQuery;
import br.com.criativasoft.opendevice.core.model.*;
import br.com.criativasoft.opendevice.core.model.test.DeviceCategoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * This is the base class for device management and input and output connections. <br/>
 * After adding devices ({@link #addDevice(br.com.criativasoft.opendevice.core.model.Device)}) and connections {@link #addOutput(br.com.criativasoft.opendevice.connection.DeviceConnection)},
 * you can monitor the changes by adding a DeviceListener {@link #addListener(DeviceListener)}.
 *
 * @date 23/06/2013
 * @since 0.1.2
 */
public abstract class BaseDeviceManager implements DeviceManager {

    private static DeviceManager instance;

    private static final Logger log = LoggerFactory.getLogger(BaseDeviceManager.class);

    private List<OpenDeviceExtension> extensions = new LinkedList<OpenDeviceExtension>();

    private volatile Set<DeviceListener> listeners = new HashSet<DeviceListener>();

    /**
     * Client connections: Websockets, http, rest, etc ...
     */
    private MultipleConnection inputConnections = new MultipleConnection();

    /**
     * Connection with the physical modules (middleware) or a proxy
     */
    private MultipleConnection outputConnections = new MultipleConnection();

    private Set<CommandFilter> filters = new LinkedHashSet<CommandFilter>();

    private CommandDelivery delivery = new CommandDelivery(this);

    private DiscoveryService discoveryService = new DiscoveryServiceImpl();

    private DeviceCategoryRegistry deviceCategoryRegistry = new DeviceCategoryRegistry();

    // FIXME: remove from here
    private EventHookManager eventManager;

    private DataManager dataManager;

    private Message lastMessage;

    protected DefaultCommandProcessor commandProcessor = new DefaultCommandProcessor(this);

    public BaseDeviceManager() {
        instance = this;
        eventManager = new EventHookManager();

        addListener(eventManager);

        // Load Extensions
        loadExtensions();

    }

    /**
     * @see OpenDeviceExtension
     */
    protected void loadExtensions() {

        try {
            Class.forName("java.util.ServiceLoader");
        } catch (ClassNotFoundException ex) {
            log.error("This java version don't support dynamic loading (ServiceLoader), you need use direct class ex: new BluetoothConnection(addr)");
        }

        // lockup....
//        ServiceLoader<OpenDeviceExtension> service = ServiceLoader.load(OpenDeviceExtension.class);
//        Iterator<OpenDeviceExtension> iterator = service.iterator();

        List<OpenDeviceExtension> found = PluginLoader.load();

        log.info("Found Extensions: " + found.size());

        for (OpenDeviceExtension extension : found) {
            log.info("Loading Extension: " + extension.getName() + ", class: " + extension.getClass());
            try {
                extension.init(this);
                extensions.add(extension);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    /**
     * Get shared global instance of DevinceManager.
     *
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

    public List<OpenDeviceExtension> getExtensions() {
        return extensions;
    }

    public <T> List<T> getExtensions(Class<T> kclass) {
        List<OpenDeviceExtension> extensions = getExtensions();
        List<T> list = new LinkedList();
        for (OpenDeviceExtension extension : extensions) {

            T found = null;

            if (ViewExtension.class.isAssignableFrom(kclass)) {
                found = (T) extension.getViewExtension();
            }

            if (PersistenceExtension.class.isAssignableFrom(kclass)) {
                found = (T) extension.getPersistenceExtension();
            }

            if (found != null) {
                list.add((T) found);
            }

        }
        return list;
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

    /**
     * Find by UID or Name
     *
     * @param device
     * @return
     */
    public Device findDevice(Device device) {
        return (device.getUid() > 0 ? findDeviceByUID(device.getUid()) : findDeviceByName(device.getName()));
    }

    @Override
    public Device findDeviceByUID(int deviceUID) {

        if (deviceUID <= 0) return null;

        Device device = getCurrentContext().getDeviceByUID(deviceUID);

        return device;
    }

    @Override
    public Device findDeviceByName(String name) {

        if (name == null || name.length() == 0) return null;

        Device device = getCurrentContext().getDeviceByName(name);

        if (device == null) getValidDeviceDao().getByName(name);

        return device;
    }

    @Override
    public List<DeviceHistory> getDeviceHistory(DeviceHistoryQuery query) {
        List<DeviceHistory> list = getValidDeviceDao().getDeviceHistory(query);

//        Device device = findDeviceByUID(query.getDeviceUID());
//        if(device != null && query.getPeriodType() != PeriodType.RECORDS  && query.getPeriodType() != PeriodType.MONTH){
//            DeviceHistory last = new DeviceHistory();
//            last.setDeviceID(query.getDeviceID());
//            last.setTimestamp(query.getPeriodEnd().getTime());
//            last.setValue(device.getValue());
//            list.add(last);
//        }

        return list;
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
     * @return true if transaction already active by another thread/component
     */
    public boolean transactionBegin() {
        return false;
    }

    public void transactionEnd() {
    }


    public TenantContext getCurrentContext() {
        return TenantProvider.getCurrentContext();
    }

    public DeviceDao getValidDeviceDao() {
        if (getDataManager().getDeviceDao() == null) throw new IllegalStateException("deviceDao is NULL !");
        return getDataManager().getDeviceDao();
    }

    @Override
    public void addDevice(Device device) {
        if (device == null) throw new IllegalArgumentException("Device is null");

        Device found = findDevice(device);

        if (found == null) {
            getValidDeviceDao().persist(device);
            getCurrentContext().addDevice(device); // add to cache.
            bindDevice(device);
        }
    }

    public void bindDevice(Device device) {
        if (device == null) throw new IllegalArgumentException("Device is null");
        if (!device.isManaged()) {
            device.setManaged(true);
            for (DeviceListener listener : listeners) {
                listener.onDeviceRegistred(device);
            }
        }
    }

    /**
     * Remove existing and add new device
     *
     * @param existing
     * @param device
     */
    private void replaceDevice(Device existing, Device device) {
        removeDevice(existing);
        addDevice(device);
    }

    @Override
    public void updateDevice(Device device) {
        if (device == null) throw new IllegalArgumentException("Device is null");
        getValidDeviceDao().update(device);
        getCurrentContext().updateDevice(device); // add to cache.
    }

    @Override
    public void removeDevice(Device device) {
        if (device == null) throw new IllegalArgumentException("Device is null");

        if (device instanceof Board) {
            Set<PhysicalDevice> devices = ((Board) device).getDevices();
            for (Device physicalDevice : devices) {
                removeDevice(physicalDevice);
            }
        }

        // force load persistent state from database
        if (device.getId() > 0) {
            device = getValidDeviceDao().getById(device.getId());
        }
        getValidDeviceDao().delete(device);
        getCurrentContext().removeDevice(device); // remove from cache
    }

    @Override
    public void addDevices(Collection<Device> devices) {
        for (Device device : devices) {
            addDevice(device);
        }
    }

    @Override
    public Collection<Device> getDevices() {
        return getCurrentContext().getDevices();
    }

    public boolean addListener(DeviceListener e) {
        synchronized (listeners) {
            return listeners.add(e);
        }
    }

    public boolean removeListener(DeviceListener e) {
        synchronized (listeners) {
            return listeners.remove(e);
        }
    }

    @Override
    public void addFilter(CommandFilter filter) {
        filters.add(filter);
    }

    public void onConnected(OnConnectListener e) {
        addConnectionListener(e);
    }

    public void addConnectionListener(ConnectionListener e) {
        if (inputConnections != null) inputConnections.addListener(e);
        if (outputConnections != null) outputConnections.addListener(e);
    }

    public void removeConnectionListener(ConnectionListener e) {
        if (inputConnections != null) inputConnections.removeListener(e);
        if (outputConnections != null) outputConnections.removeListener(e);
    }

    /**
     * Notify All Listeners about device change
     *
     * @param sync - sync state with server
     */
    public synchronized void notifyListeners(Device device, boolean sync) {

        TenantContext currentContext = TenantProvider.getCurrentContext();

        if (!currentContext.isDevicesInSync()) { // ignore events from device syncronization/initialization  (GET_DEVICES_RESPONSE)...

            boolean alreadyExist = transactionBegin();

            // Save history
            DeviceHistory history = new DeviceHistory();
            history.setDeviceID(device.getId());
            history.setValue(device.getValue());
            history.setTimestamp(System.currentTimeMillis());
            history.setApplicationID(device.getApplicationID());
            saveDeviceHistory(device, history);

            if (!alreadyExist) transactionEnd();

            if (sync) {
                try {
                    if (device.getUid() > 0) {
                        CommandType type = DeviceCommand.getCommandType(device.getType());
                        DeviceCommand cmd = new DeviceCommand(type, device.getUid(), device.getValue());
                        if (device.getApplicationID() != null) cmd.setApplicationID(device.getApplicationID());
                        send(cmd);
                    } else {
                        log.debug("Can't sync device with UID = 0 (" + device.getName() + ") ! Need sync with server or enable local id generation");
                    }
                } catch (IOException ex) {
                    log.error(ex.getMessage(), ex);
                }

            }
        }

        // Individual Listeners
        for (final OnDeviceChangeListener listener : device.getListeners()) {
            listener.onDeviceChanged(device);
        }

        if (listeners.isEmpty()) return;

        // Global Listeners
        for (final DeviceListener listener : listeners) {
            listener.onDeviceChanged(device);
        }
    }

    protected void initInputConnections() {
        inputConnections.addListener(connectionListener);
    }

    protected void initOutputConnections() {
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
        if (outputConnections != null) outputConnections.disconnect();
        if (inputConnections != null) inputConnections.disconnect();
    }

    @Override
    public void connect(DeviceConnection connection) throws IOException {
        addOutput(connection);
        connectAll();
    }

    protected void connectAll() throws ConnectionException {

        if (outputConnections != null) outputConnections.connect();
        if (inputConnections != null) inputConnections.connect();

    }

    /**
     * Get remote devices and synchronize (embedded) devices with connections that require additional information such as GPIO. <br/>
     * (An example is the raspberry that already has support built GPIO)
     *
     * @param connection
     * @param request
     */
    protected void syncDevices(DeviceConnection connection, GetDevicesRequest request) {

        if (connection instanceof EmbeddedGPIO) {
            EmbeddedGPIO gpioConn = (EmbeddedGPIO) connection;

            // TODO: this may cause problems if devices is not related to GPIO, like devices from atached microcontrolers (raspberry <-usb-> arduino)
            // This is a convenient way to register devices instantiated but not attached to "Embedded Connection"
            if (getConfig().getBindLocalVariables()) {
                Collection<Device> devices = getDevices();
                if (devices != null) {
                    for (Device device : devices) {
                        if (device instanceof PhysicalDevice) gpioConn.attach(device);
                    }
                } else {
                    log.warn("None device registered !");
                }
            }

            // Sync dynamic devices generated on-the-fly by connection
            Board board = gpioConn.getBoardInfo();
            if (board != null) {
                List<Device> devices = new LinkedList<Device>();
                devices.add(board);
                devices.addAll(board.getDevices());

                OpenDeviceConfig config = ODev.getConfig();
                int nextID = -1;

                DeviceDao dao = getDeviceDao();

                for (Device device : devices) {
                    if (!device.isManaged()) {

                        // Check if exist a device with same name (2th connection) !
                        Device found = findDeviceByName(device.getName());
                        if (found != null) {
                            device.setUID(found.getUid());
                            replaceDevice(found, device);
                        } else {

                            // Device not have ID, Get next ID from database
                            if (device.getUid() <= 0 && !config.isRemoteIDGeneration()) {
                                if (nextID == -1) nextID = dao.getNextUID();
                                device.setUID(nextID++);
                            }

                            if (device.getCategory() != null) {
                                device.setCategory(dao.getCategoryByCode(device.getCategory().getCode())); // update reference
                            }
                            addDevice(device);
                        }

                    }
                }
            }

        }

        if ((connection instanceof StreamConnection || connection instanceof IRemoteClientConnection  /* << ws/rest clients */)
                && outputConnections.exist(connection)) {
            try {
                sendTo(request, connection);
            } catch (IOException e) {
            }
        }

        if (connection instanceof MultipleConnection) {
            Set<DeviceConnection> connections = ((MultipleConnection) connection).getConnections();
            for (DeviceConnection current : connections) {
                syncDevices(current, request);
            }
        }

    }


    public void addInput(DeviceConnection connection) {

        if (inputConnections.getSize() == 0) initInputConnections();

        if (inputConnections.exist(connection)) {
            log.info("Connection with ID: " + connection.getUID() + " already exist !");
            return;
        }

        if (connection instanceof StreamConnection) {
            StreamConnection streamConnection = (StreamConnection) connection;
            if (!(streamConnection.getSerializer() instanceof CommandStreamSerializer)) {
                streamConnection.setStreamReader(new CommandStreamReader()); // data protocol..
            }
        }

        if (connection instanceof DeviceListener) {
            addListener((DeviceListener) connection);
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

    @Override
    public void addConnection(DeviceConnection connection) {
        addOutput(connection);
    }

    @Override
    public void removeConnection(DeviceConnection connection) {
        removeOutput(connection);
        removeInput(connection);
    }

    public void addOutput(DeviceConnection connection) {

        if (outputConnections.getSize() == 0) initOutputConnections();

        if (connection.getApplicationID() == null) {
            connection.setApplicationID(TenantProvider.getCurrentID());
        }

        if (outputConnections.exist(connection)) {
            log.info("Connection with ID: " + connection.getUID() + " already exist !");
            return;
        }

        if (connection instanceof StreamConnection) {
            StreamConnection streamConnection = (StreamConnection) connection;
            if (!(streamConnection.getSerializer() instanceof CommandStreamSerializer)) {
                streamConnection.setStreamReader(new CommandStreamReader()); // data protocol..
            }
        }

        if (connection instanceof ITcpConnection) {
            ((ITcpConnection) connection).setDiscoveryService(discoveryService);
        }

        if (connection instanceof DeviceListener) {
            addListener((DeviceListener) connection);
        }

        delivery.addConnection(connection);

        if (connection.getSerializer() == null) {
            connection.setSerializer(new CommandStreamSerializer()); // data conversion..
        }

        connection.setConnectionManager(this);
        outputConnections.addConnection(connection);
    }


    protected void sendTo(Command command, DeviceConnection connection) throws IOException {
        if (connection != null && connection.isConnected()) {
            if (command.getApplicationID() == null) command.setApplicationID(connection.getApplicationID());
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

    public void send(Command command, boolean output, boolean input) throws IOException {

        if (command.getApplicationID() == null) command.setApplicationID(TenantProvider.getCurrentID());

        if (output && outputConnections.hasConnections()) {

            Set<DeviceConnection> connections = outputConnections.getConnections();
            for (DeviceConnection connection : connections) {
                delivery.sendTo(command, connection);
            }

        }

        if (input && inputConnections.hasConnections()) {

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
    public void sendCommand(String commandName, Object... params) throws IOException {
        send(new UserCommand(commandName, params));
    }

    /**
     * Causes the currently executing thread to sleep (temporarily cease
     * execution)
     *
     * @param millis
     * @see Thread#sleep(long)
     */
    public void delay(int millis) {
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
    public boolean hasConnections() {
        int size = inputConnections.getSize();
        size += outputConnections.getSize();
        return size > 0;
    }

    public boolean isTenantsEnabled() {
        return OpenDeviceConfig.get().isTenantsEnabled();
    }

    /**
     * Checks if a connection is active. Considers the input and output
     */
    @Override
    public boolean isConnected() {

        if (hasConnections()) {

            if (inputConnections.isConnected()) return true;
            if (outputConnections.isConnected()) return true;

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

        if (connection != null) return connection;

        return inputConnections.findConnection(uid);
    }

    protected void saveDeviceHistory(Device device, DeviceHistory history) {
//        transactionBegin();
        history.setApplicationID(device.getApplicationID());
        getDeviceDao().persistHistory(history);
//        transactionEnd();
    }

    protected OpenDeviceConfig getConfig() {
        return OpenDeviceConfig.get();
    }


    private ConnectionListener connectionListener = new ConnectionListener() {

        @Override
        public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
            log.debug("connectionStateChanged :: " + connection.getClass().getSimpleName() + ", status = " + status);

            // Force sync devices with physical modules on connect.
            if (status == ConnectionStatus.CONNECTED && outputConnections.exist(connection)) {
                GetDevicesRequest request = new GetDevicesRequest();
                request.setApplicationID(TenantProvider.getCurrentID());
                syncDevices(connection, request);
            }
        }


        @Override
        public void onMessageReceived(Message message, DeviceConnection connection) {

            if (message == null) return;

            lastMessage = message;

            if (!(message instanceof Command)) {
                log.debug("Message received : " + message);
                return;
            }

            transactionBegin();

            try {

                Command command = (Command) message;

                if (command.getApplicationID() == null || command.getApplicationID().length() == 0) {
                    command.setApplicationID(connection.getApplicationID());
                }

                boolean filtred = false;

                if (!filters.isEmpty()) {

                    for (CommandFilter filter : filters) {

                        if (!filter.filter(command, connection)) {
                            if (log.isTraceEnabled())
                                log.debug("Message blocked by filter: " + filter.getClass().getSimpleName());
                            filtred = true;
                        }

                    }

                }

                if (!filtred) commandProcessor.onMessageReceived(message, connection);

            } finally {
                transactionEnd();
            }

        }
    };


}
