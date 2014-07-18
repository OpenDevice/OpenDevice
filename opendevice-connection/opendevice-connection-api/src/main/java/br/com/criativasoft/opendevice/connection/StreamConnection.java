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

package br.com.criativasoft.opendevice.connection;

import br.com.criativasoft.opendevice.connection.serialize.StreamReader;

import java.io.IOException;

/**
 * This interface defines the capabilities that a stream connection must have. <br/>
 * A StreamConnection is used to send a stream of primitive types in the Java programming language<br/>
 * @author Ricardo JL Rufino
 * @date 18/06/2014
 */
public interface StreamConnection extends DeviceConnection {
	
	public static final byte TOKEN_LF = '\n'; 
	public static final byte TOKEN_CR = '\r'; 
	public static final byte TOKEN_ZERO = 0x00;
	
	public void setStreamReader(StreamReader reader);
	
	public StreamReader getStreamReader();
	
	public void write(byte value) throws IOException;

	/**
	 * Write byte array to connected device. <br/>
	 * All other methods use this implementation.
	 */
	public void write(byte[] value) throws IOException;
	
	public void write(int value) throws IOException;
	
	public void write(int[] value) throws IOException;
	
	public void write(long value) throws IOException;
	
	public void write(long[] value) throws IOException;

	public void writeln(String mensagem) throws IOException;

	public void write(String mensagem) throws IOException;
	
	// TODO: add float !!
}
