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

/**
 * Enum com o status da conexão.
 *
 * @author Ricardo JL Rufino
 * @date 04/05/2011 14:16:36
 */
public enum ConnectionStatus {
	CONNECTING,
	CONNECTED,
	DISCONNECTING,
	DISCONNECTED,
	LOGGINGIN,
	FAIL,
	UNDEFINED
}
