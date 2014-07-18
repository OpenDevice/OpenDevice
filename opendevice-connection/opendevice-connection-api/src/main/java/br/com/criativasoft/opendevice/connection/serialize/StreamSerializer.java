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

package br.com.criativasoft.opendevice.connection.serialize;

import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;
  
public class StreamSerializer implements MessageSerializer<byte[], byte[]> {
	
	@Override
	public Message parse(byte[] data) {
		return new SimpleMessage(data);
	}
	
	@Override
	public byte[] serialize(Message message) {
		return ((SimpleMessage)message).getBytes();
	}
	
}
