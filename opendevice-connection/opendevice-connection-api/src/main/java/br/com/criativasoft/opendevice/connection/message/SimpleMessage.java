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

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.util.DataUtils;

/**
 * Mensage used to send primitive types to a {@link DeviceConnection}.
 * @author Ricardo JL Rufino
 * @date 18/06/2014
 */
public class SimpleMessage implements Message, ByteMessage {
	
	private static final long serialVersionUID = 4467004980332125276L;
	
	
	private byte[] bytes;
	
	public SimpleMessage(byte value) {
		this(new byte[]{value});
	}
	
	public SimpleMessage(int value) {
		this(DataUtils.toByteArray(value));
	}
	
	public SimpleMessage(int[] value) {
		this(DataUtils.toByteArray(value));
	}
	
	public SimpleMessage(long value) {
		this(DataUtils.toByteArray(value));
	}
	
	public SimpleMessage(long[] value) {
		this(DataUtils.toByteArray(value));
	}
	
	public SimpleMessage(String mensagem) {
		this(mensagem.getBytes());
	}
	
	public SimpleMessage(byte[] value) {
		this.bytes = value;
	}

	public byte[] getBytes() {
		return bytes;
	}
	
	@Override
	public String toString() {
		return new String(getBytes());
	}

}
