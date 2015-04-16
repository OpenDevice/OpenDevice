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

import br.com.criativasoft.opendevice.connection.ConnectionManager;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.filter.CommandFilter;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;

import java.io.IOException;
import java.util.Collection;

public interface DeviceManager extends ConnectionManager{

    public void setDeviceDao(DeviceDao deviceDao);

    public DeviceDao getDeviceDao();

	public Collection<Device> getDevices() ;
	
	public Device findDeviceByUID(int deviceUID);

    public void addDevice(Device device);

    public void addDevices(Collection<Device> devices);
	
	public void send(Command command) throws IOException;
	
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
     * Connect to a single output connection. <br/>
     * That way you do not need to call addOutput
     * @param connection
     * @throws IOException
     */
    void connect(DeviceConnection connection) throws IOException;

    public void addInput(DeviceConnection connection);

    public void addOutput(DeviceConnection connection);

    public boolean addListener(DeviceListener e);

    public void addFilter(CommandFilter filter);

    public boolean isConnected();

    public boolean hasConnections();

}
