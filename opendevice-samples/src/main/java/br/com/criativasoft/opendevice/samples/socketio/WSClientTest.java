/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.samples.socketio;


import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.samples.ui.FormDevicesAPIController;
import br.com.criativasoft.opendevice.webclient.WebSocketClientConnection;

import javax.swing.*;

public class WSClientTest extends FormDevicesAPIController {

	public WSClientTest() throws ConnectionException {
		super(new WebSocketClientConnection("http://localhost:8181/device/connection/fake-client-123-123"));
	}
	 
	public static void main(String[] args) throws ConnectionException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new WSClientTest();
	}


}
