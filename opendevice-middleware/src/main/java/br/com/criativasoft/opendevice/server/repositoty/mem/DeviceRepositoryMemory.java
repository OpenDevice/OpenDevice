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

package br.com.criativasoft.opendevice.server.repositoty.mem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.server.repositoty.DeviceRepository;

public class DeviceRepositoryMemory implements DeviceRepository {

	private List<Device> devices = new ArrayList<Device>();
	
	public DeviceRepositoryMemory(Collection<Device> devices) {
		super();
		this.devices.addAll(devices);
	}

	@Override
	public List<Device> listAll() {
		return devices;
	}

	@Override
	public Device findByDeviceID(int deviceID) {
		for (Device device : devices) {
			if (deviceID == device.getUid()) {
				return device;
			}
		}

		return null;
	}

}
