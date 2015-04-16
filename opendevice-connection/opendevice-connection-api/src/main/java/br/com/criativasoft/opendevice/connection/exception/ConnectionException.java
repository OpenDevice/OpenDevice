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

package br.com.criativasoft.opendevice.connection.exception;

import java.io.IOException;

/**
 * @author Ricardo JL Rufino
 */
public class ConnectionException extends IOException {

	private static final long serialVersionUID = 4806827126960117788L;

    private Throwable cause; // make android 2.2 compatible

	public ConnectionException() {
		super();
	}

	public ConnectionException(String message, Throwable cause) {
		this(message);
        this.cause = cause;
	}

	public ConnectionException(String message) {
		super(message);
	}

	public ConnectionException(Throwable cause) {
        this.cause = cause;
	}

    @Override
    public synchronized Throwable getCause() {
        return cause;
    }
}
