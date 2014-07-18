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
import br.com.criativasoft.opendevice.connection.util.DataUtils;
import br.com.criativasoft.opendevice.core.command.amarino.AmarinoIntent;
import br.com.criativasoft.opendevice.core.command.amarino.MessageBuilder;

public class CommandStreamSerializer implements MessageSerializer<byte[], byte[]> {

	@Override
	public Message parse(byte[] data) {

        Command command = CommandFactory.parse(new String(data));

        if(command != null){
            return command;
        }else{ // Undefined command.
            return new SimpleMessage(data);
        }

	}

	@Override
	public byte[] serialize(Message message) {
		
		Command command = (Command) message;

		int[] cmd = new int[4];
		cmd[0] = command.getType().getCode();
		cmd[1] = -1;
		if(command.getConnectionUUID() != null){
			try{
				cmd[1] = Integer.parseInt(command.getConnectionUUID());
			}catch (Exception e) {
				throw new CommandException("RequestUID must be int");
			}
		}
		if(command instanceof DeviceCommand){
			cmd[2] = ((DeviceCommand) command).getDeviceID();
			cmd[3] = DataUtils.longToInt( ((DeviceCommand) command).getValue() );
		}else{
			cmd[2] = 0;
			cmd[3] = 0;
			
		}
		
		String msg = MessageBuilder.getMessage('A', cmd, AmarinoIntent.INT_ARRAY_EXTRA);
		
		return msg.getBytes();
	}


}
