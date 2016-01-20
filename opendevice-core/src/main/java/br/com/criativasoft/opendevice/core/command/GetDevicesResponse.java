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
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;

import java.util.Collection;
import java.util.LinkedList;

public class GetDevicesResponse extends ResponseCommand implements ExtendedCommand {

	public static final CommandType TYPE = CommandType.GET_DEVICES_RESPONSE;

	private Collection<Device> devices = new LinkedList<Device>();

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

        String[] split = extradata.split(Command.DELIMITER);

        for (int i = 1; i < split.length; i++) {
            String deviceStr = split[i].substring(1, split[i].length()-1);
            String[] deviceSplit = deviceStr.split(",");
            int uid = Integer.parseInt(deviceSplit[0]);
            long value = Long.parseLong(deviceSplit[2]);
            boolean isSensor = Integer.parseInt(deviceSplit[4]) > 0;

            DeviceType deviceType =  DeviceType.getByCode(Integer.parseInt(deviceSplit[5]));

            if(isSensor){
                Sensor sensor = new Sensor(uid, "Sensor " + uid, deviceType, DeviceCategory.GENERIC);
                sensor.setValue(value);
                devices.add(sensor);
            }else{
                devices.add(new Device(uid, "Device "+uid, deviceType, DeviceCategory.GENERIC, value));
            }
        }
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
