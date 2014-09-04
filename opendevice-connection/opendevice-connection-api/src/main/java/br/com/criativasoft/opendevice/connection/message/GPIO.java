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

package br.com.criativasoft.opendevice.connection.message;

public class GPIO extends AbstractMessage implements ByteMessage {
	
	public static final byte HIGH = 1;
	public static final byte LOW = 0;

    public static final byte ON = 1;
    public static final byte OFF = 0;

    private byte[] bytes = new byte[2];


    public GPIO(byte pin,  byte state) {
        bytes[0] = pin;
        bytes[1] =  state;
    }

    public GPIO(int pin,  byte state) {
        bytes[0] = (byte)pin;
        bytes[1] =  state;
    }

	private static final long serialVersionUID = 1L;

    public byte getPin() {
        return bytes[0];
    }

    public byte getState() {
        return bytes[1];
    }

	@Override
	public byte[] getBytes() {
		return bytes;
	}

}
