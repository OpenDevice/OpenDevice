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

import br.com.criativasoft.opendevice.connection.AbstractStreamConnection;

import java.io.InputStream;

/**
 * Interface for reading the information of an input controlling size of messages and flow, <br/>
 * the parser of the information must be done by {@link MessageSerializer}
 * @author Ricardo JL Rufino
 * @date 18/06/2014
 */
public interface StreamReader {
	
	public void setInput(InputStream input);
	
	public void processPacketRead(byte read[]);
	
	public void checkDataAvailable();
	
	public void setConnection(AbstractStreamConnection connection);
	
	public AbstractStreamConnection getConnection();
}
