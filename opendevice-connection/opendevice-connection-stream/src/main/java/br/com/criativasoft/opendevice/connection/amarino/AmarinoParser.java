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

package br.com.criativasoft.opendevice.connection.amarino;

import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.serialize.DefaultSteamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

public class AmarinoParser extends DefaultSteamReader {
	
	private Logger log = LoggerFactory.getLogger(AmarinoParser.class);
	
	@Override
	protected boolean checkEndOfMessage(byte lastByte,ByteArrayOutputStream readBuffer) {
		return lastByte == MessageBuilder.ACK_FLAG;
	}
	
	protected void processPacketRead(int available, byte read[]){
		log.debug("Read part:" + new String(read) + " -> " + read[0]);
		
		for (int i = 0; i < read.length; i++) {
			// Quebra de linha, processar comandos
			if (checkEndOfMessage(read[i], inputBuffer)) {
				byte[] array = inputBuffer.toByteArray();
				Message message = parse(array);
				notifyOnDataRead(message);
				inputBuffer.reset();
			// Lê ate encontrar um EOL.
		        // Ex: LF (Line feed, '\n', 0x0A, 10 in decimal) or CR (Carriage return, '\r', 0x0D, 13 in decimal)
			}else{
				if(read[i] != MessageBuilder.ACK_FLAG && read[i] != MessageBuilder.START_FLAG)
					inputBuffer.write(read[i]);
			}

		}
	}
	
	

}
