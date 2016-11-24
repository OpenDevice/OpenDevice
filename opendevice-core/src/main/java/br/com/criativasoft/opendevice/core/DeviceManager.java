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
import br.com.criativasoft.opendevice.connection.ConnectionManager;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.filter.CommandFilter;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.metamodel.DeviceHistoryQuery;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceHistory;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface DeviceManager extends ConnectionManager{

    public void setDeviceDao(DeviceDao deviceDao);

    public void setDataManager(DataManager dataManager);

    public DataManager getDataManager();

    public DeviceDao getDeviceDao();

	public Collection<Device> getDevices();
	
	public Device findDeviceByUID(int deviceUID);

    public Device findDeviceByName(String name);

    public void addDevice(Device device);

    public void removeDevice(Device device);

    public void addDevices(Collection<Device> devices);
	
	public void send(Command command) throws IOException;

    public void send(Command command, boolean output, boolean input) throws IOException;
	
	/**
	 * This will call a user-defined command, allowing you to perform custom method calls directly on device. <br/>
	 * This is an easy way to extend OpenDevice protocol, but note that in some situations it is best to implement 
	 * a new device class and work with objects instead of separate functions.
	 * @param commandName - name command informed the device side
	 * @param params (Optional) - Parameters to be sent to the function
	 */
	public void sendCommand( String commandName , Object ... params ) throws IOException;

    public void connect() throws IOException;

    public void disconnect() throws IOException;

    /**
     * Stop and disconnect all
     */
    public void stop();

    /**
     * Connect to a single output connection. <br/>
     * That way you do not need to call addOutput
     * @param connection
     * @throws IOException
     */
    void connect(DeviceConnection connection) throws IOException;

    public void addInput(DeviceConnection connection);

    public void addOutput(DeviceConnection connection);

    public void removeOutput(DeviceConnection connection);

    public void removeInput(DeviceConnection connection);

    public boolean addListener(DeviceListener e);

    public void addConnectionListener(ConnectionListener e);

    public void addFilter(CommandFilter filter);

    public boolean isConnected();

    public boolean hasConnections();

    public List<DeviceHistory> getDeviceHistory(DeviceHistoryQuery query);

    public DeviceCategory getCategory(Class<? extends DeviceCategory> klass);

    /**
     * Notify All Listeners about device change
     * @param sync - sync state with server
     */
    public void notifyListeners(Device device, boolean sync);
}
