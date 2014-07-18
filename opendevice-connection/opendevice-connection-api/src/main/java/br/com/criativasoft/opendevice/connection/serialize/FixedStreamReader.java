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

import java.io.ByteArrayOutputStream;

public class FixedStreamReader extends DefaultSteamReader {
	
	private int messageSize = 1;

	public FixedStreamReader(int messageSize) {
		this.messageSize = messageSize;
	}
	
	@Override
	protected boolean checkEndOfMessage(byte lastByte,ByteArrayOutputStream readBuffer) {

		if(readBuffer.size() == (messageSize - 1) ){
			readBuffer.write(lastByte); // Include last !
			return true;
		}
		
		return false;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		FixedStreamReader clone = new FixedStreamReader(messageSize);
		return clone;
	}
	

}
