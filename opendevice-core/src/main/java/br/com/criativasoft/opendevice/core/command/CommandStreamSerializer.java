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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;
import br.com.criativasoft.opendevice.connection.serialize.MessageSerializer;

public class CommandStreamSerializer implements MessageSerializer<byte[], byte[]> {

    private static Logger log = LoggerFactory.getLogger(CommandStreamSerializer.class);

	@Override
	public Message parse(byte[] data) {

        String cmd = new String(data);

        if(cmd.length() > 0){
            Command command = CommandFactory.parse(cmd);

            if(command != null){
                return command;
            }else{ // Undefined command.
                return new SimpleMessage(data);
            }

        }else{
            log.trace("empty message received !");
            return null;
        }

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
		}else if(command instanceof ExtendedCommand){
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
