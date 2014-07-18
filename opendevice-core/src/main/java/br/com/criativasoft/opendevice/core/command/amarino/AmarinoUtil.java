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

package br.com.criativasoft.opendevice.core.command.amarino;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;

import java.io.IOException;

/**
 * TODO: Documentar.
 * @author Ricardo JL Rufino
 */
public class AmarinoUtil {
	
	/**
	 * Sends an int array to Arduino
	 * 
	 * @param flag the flag Arduino has registered a function for to receive this data
	 * @param data your data you want to send
	 */
	public static String send(DeviceConnection connection, char flag, int[] data) throws IOException{
		String msg = MessageBuilder.getMessage(flag, data, AmarinoIntent.INT_ARRAY_EXTRA);
		connection.send(new SimpleMessage(msg));
		return msg;
	}
	

}
