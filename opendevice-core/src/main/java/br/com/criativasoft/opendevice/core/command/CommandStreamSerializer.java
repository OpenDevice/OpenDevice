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

import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;
import br.com.criativasoft.opendevice.connection.serialize.MessageSerializer;
import br.com.criativasoft.opendevice.core.command.ext.IrCommand;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class CommandStreamSerializer implements MessageSerializer<byte[], byte[]> {

    private static Logger log = LoggerFactory.getLogger(CommandStreamSerializer.class);

	@Override
	public Message parse(byte[] pkg) {

        String cmd = new String(pkg);

        if(cmd.length() == 0){
			log.trace("empty message received !");
			return null;
		}

		if(cmd.startsWith("DB:")) return new SimpleMessage(cmd);

		// Easy for split (start == delimitar)
		if(cmd.startsWith(Command.DELIMITER)){
			cmd = cmd.replaceFirst("/", "");
		}

		String[] split = cmd.split(Command.DELIMITER);

        int ctype = Integer.parseInt(split[0]);
        int id = Integer.parseInt(split[1]);

		CommandType type = CommandType.getByCode(ctype);

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

			int status = Integer.parseInt(split[3]);	// Command.value

			command = new ResponseCommand(CommandStatus.getByCode(status));

		}else if(type == CommandType.GET_DEVICES){ // Returned list of devices.

			command = new GetDevicesRequest();

        // Format: GET_DEVICES_RESPONSE;ID;Length;[ID, PIN, VALUE, TARGET, SENSOR?, TYPE];[ID,PIN,VALUE,...];[ID,PIN,VALUE,...]
		// TODO: Move to ExtendedCommand model.
        }else if(type == CommandType.GET_DEVICES_RESPONSE){ // Returned list of devices.

			String reqID = split[1];
			List<Device> devices = new LinkedList<Device>();
			command = new GetDevicesResponse(devices, reqID);

			for (int i = 3; i < split.length; i++) {
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

            command = CommandRegistry.getCommand(ctype);

            if(command == null)  throw new CommandException("Can't parse command type : " + type + ", cmd: " + cmd);

		}

		if(command instanceof ExtendedCommand){
			ExtendedCommand extendedCommand = (ExtendedCommand) command;

            // Skip type and id
            int indexOf = cmd.indexOf(Command.DELIMITER, 1);
            indexOf = cmd.indexOf(Command.DELIMITER, indexOf+1);

            if(cmd.length() > indexOf+1) {
                String extradata = cmd.substring(indexOf+1);
                if(extradata.length() > 0){
                    extendedCommand.deserializeExtraData(extradata);
                }
            }

		}

        command.setTrackingID(id);
		command.setTimestamp(new Date());

		return command;

	}

	@Override
	public byte[] serialize(Message message) {
		
		Command command = (Command) message;
		StringBuilder sb = new StringBuilder();
		sb.append(Command.START_FLAG);
		sb.append(command.getType().getCode());
		sb.append(Command.DELIMITER_FLAG);
		sb.append((command.getUid() != null ? command.getTrackingID() : 0));
		
		if(CommandType.isDeviceCommand(command.getType())){
		    sb.append(Command.DELIMITER_FLAG);
		    sb.append(((DeviceCommand) command).getDeviceID());
		    sb.append(Command.DELIMITER_FLAG);
		    sb.append(((DeviceCommand) command).getValue());
		}else if(command instanceof SimpleCommand){
			sb.append(Command.DELIMITER_FLAG);
            sb.append(((SimpleCommand) command).getValue());
        } else if(command instanceof ExtendedCommand){
		    ExtendedCommand extra = (ExtendedCommand) command;
		    String extraData = extra.serializeExtraData();
		    if(extraData != null){
		        sb.append(Command.DELIMITER_FLAG);
		        sb.append(extraData);
		    }
		}else{
            sb.append(0);
            sb.append(Command.DELIMITER_FLAG);
            sb.append(0);       
        }
		
		sb.append(Command.ACK_FLAG);
		
		if(log.isTraceEnabled()) log.trace("serializing: " + sb);
		
		return sb.toString().getBytes();
	}


}
