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

import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.model.Device;

import java.io.IOException;
import java.util.Collection;

public interface DeviceManager {
	
	public Collection<Device> getDevices() ;
	
	public Device findDevice(int deviceID);
	
	public void send(Command command) throws IOException;

}
