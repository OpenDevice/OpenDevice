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

package br.com.criativasoft.opendevice.core.command;

import br.com.criativasoft.opendevice.core.model.Device;

import java.util.Collection;

public class GetDevicesResponse extends ResponseCommand {

	public static final CommandType TYPE = CommandType.GET_DEVICES_RESPONSE;

	private Collection<Device> devices;

	private static final long serialVersionUID = -1023397181880070237L;

	public GetDevicesResponse(Collection<Device> devices, String connectionUUID) {
		super(TYPE, connectionUUID);
		this.devices = devices;
	}

	public Collection<Device> getDevices() {
		return devices;
	}

}
