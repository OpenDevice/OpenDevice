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
import br.com.criativasoft.opendevice.core.model.Sensor;

import java.util.Collection;

public class GetDevicesResponse extends ResponseCommand implements ExtendedCommand {

	public static final CommandType TYPE = CommandType.GET_DEVICES_RESPONSE;

	private Collection<Device> devices;

	private static final long serialVersionUID = -1023397181880070237L;

	public GetDevicesResponse(Collection<Device> devices, String connectionUUID) {
		super(TYPE, CommandStatus.CREATED, connectionUUID);
		this.devices = devices;
	}

	public Collection<Device> getDevices() {
		return devices;
	}

	@Override
	public void deserializeExtraData(String extradata) {

	}

	@Override
	public String serializeExtraData() {
		StringBuffer sb = new StringBuffer();
        // [ID, PIN, VALUE, TARGET, SENSOR?, TYPE]

        sb.append(devices.size());
        sb.append(Command.DELIMITER);

        for (Device device : devices) {
            sb.append("[");
            sb.append(device.getId()).append(",");
            if(device.getGpio() != null){
                sb.append(device.getGpio().getPin()).append(",");
            }else {
                sb.append(0).append(",");
            }
            sb.append(device.getValue()).append(",");
            sb.append(-1).append(",");
            sb.append((device instanceof Sensor ? 1 : 0)).append(",");
            sb.append(device.getType().getCode());
            sb.append("]");
            sb.append(";");
        }

        if(! devices.isEmpty()){
            sb.deleteCharAt(sb.length()-1);
        }

		return sb.toString();
	}
}
