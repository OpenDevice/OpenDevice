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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommandFactory {
	
	public static final String DELIMITER = ";";
	
	private CommandFactory(){}
	
	/**
	 * Parse command encoded as String.
	 * @param data - received data from bluetooth or usb
	 */
	public static Command parse(String data) throws CommandException{
		
		if(data.startsWith("DB:")) return null;
		
		String[] split = data.split(DELIMITER);
		
		CommandType type = CommandType.getByCode(Integer.parseInt(split[0]));
	
		Command command = null;
		
		if(DeviceCommand.isCompatible(type)){

			int deviceID = Integer.parseInt(split[2]);
			int value = Integer.parseInt(split[3]);
			command = new DeviceCommand(type, deviceID, value);

		}else if(type == CommandType.DEVICE_COMMAND_RESPONSE){
			String id = split[1];
			int status = Integer.parseInt(split[3]);	// Command.value
			
			command = new ResponseCommand(CommandStatus.getByCode(status)); // TODO: pode ser necessário o connection UUID

        // Received: GET_DEVICES_RESPONSE;ID;[ID, PIN, VALUE, TARGET, SENSOR?, TYPE];[ID, PIN, VALUE, TARGET, SENSOR?, TYPE];....
        }else if(type == CommandType.GET_DEVICES_RESPONSE){ // Returned list of devices.

            String reqID = split[1];
            List<Device> devices = new ArrayList<Device>();
            command = new GetDevicesResponse(devices, reqID);

            for (int i = 2; i < split.length; i++) {
                String deviceStr = split[i].substring(1, split[i].length()-1);
                String[] deviceSplit = deviceStr.split(",");
                DeviceType deviceType =  DeviceType.getByCode(Integer.parseInt(deviceSplit[5]));
                int uid = Integer.parseInt(deviceSplit[0]);
                long value = Long.parseLong(deviceSplit[2]);
                devices.add(new Device(uid, "Device "+uid, deviceType, DeviceCategory.GENERIC, value));
            }

        }else{
			throw new CommandException("Can't parse command type : " + type + ", data: " + data);
		}
		
		command.setTimestamp(new Date());
		
		return command;
	}

}
