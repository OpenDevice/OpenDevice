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

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;

import java.io.IOException;
import java.util.Collection;

public interface DeviceManager {

    public void setDeviceDao(DeviceDao deviceDao);

	public Collection<Device> getDevices() ;
	
	public Device findDeviceByUID(long deviceID);

    public void addDevice(Device device);
	
	public void send(Command command) throws IOException;

    public void connect() throws IOException;

    public void addInput(DeviceConnection connection);

    public void addOutput(DeviceConnection connection);

    public boolean addListener(DeviceListener e);

}
