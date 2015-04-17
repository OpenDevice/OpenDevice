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

import br.com.criativasoft.opendevice.core.command.ext.IrCommand;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;
import br.com.criativasoft.opendevice.core.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class CommandFactory {
	
	public static final String DELIMITER = Command.DELIMITER;
	
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
			long value = Long.parseLong(split[3]);   
			command = new DeviceCommand(type, deviceID, value);
			
		}else if(SimpleCommand.isCompatible(type)){
            
            long value = Long.parseLong(split[3]);
            command = new SimpleCommand(type, value);
                    
		// Format: INFRA_RED;ID;VALUE;IR_PROTOCOL;LENGTH?;BYTE1...BYTEX?
		// If IR_PROTOCOL is RAW, 'LENGTH' and BYTE ARRAY EXIST. 
		}else if(type == CommandType.INFRA_RED){
		    
            int deviceID = Integer.parseInt(split[2]);
            long value = Long.parseLong(split[3]);
            command = new IrCommand(deviceID, value);	
            
		}else if(type == CommandType.DEVICE_COMMAND_RESPONSE){
			String id = split[1];
			int status = Integer.parseInt(split[3]);	// Command.value

			command = new ResponseCommand(CommandStatus.getByCode(status));
            command.setTrackingID(Integer.parseInt(id));

        // Format: GET_DEVICES_RESPONSE;ID;[ID, PIN, VALUE, TARGET, SENSOR?, TYPE];[ID, PIN, VALUE, TARGET, SENSOR?, TYPE];....
        }else if(type == CommandType.GET_DEVICES_RESPONSE){ // Returned list of devices.

            String reqID = split[1];
            List<Device> devices = new LinkedList<Device>();
            command = new GetDevicesResponse(devices, reqID);

            for (int i = 2; i < split.length; i++) {
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
        }else{
			throw new CommandException("Can't parse command type : " + type + ", data: " + data);
		}
		
		if(command instanceof ExtendedCommand){
		    ExtendedCommand extendedCommand = (ExtendedCommand) command;
		    String[] extradata = Arrays.copyOfRange(split, 4, split.length);
		    if(extradata.length > 0){
		        extendedCommand.deserializeExtraData(StringUtils.join(extradata, DELIMITER));
		    }
		}
		
		command.setTimestamp(new Date());
		
		return command;
	}

}
