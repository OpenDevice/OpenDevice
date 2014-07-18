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

import java.io.Serializable;

public interface Message extends Serializable{

    /** SimpleMessage to send byte = 1 */
	public static final  SimpleMessage HIGH =  new SimpleMessage((byte)1);

    /** SimpleMessage to send byte = 0 */
	public static final  SimpleMessage LOW =  new SimpleMessage((byte)0);

    /** SimpleMessage to send byte = 1 */
	public static final  SimpleMessage ON =  HIGH;

    /** SimpleMessage to send byte = 0 */
	public static final  SimpleMessage OFF =  LOW;
	

}
