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

package br.com.criativasoft.opendevice.samples;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.samples.ui.FormController;
import br.com.criativasoft.opendevice.webclient.WebSocketClientConnection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Tutorial: https://opendevice.atlassian.net/wiki/display/DOC/A.+First+Steps+with+OpenDevice
 * For arduino/energia use: opendevice-hardware-libraries/arduino/OpenDevice/examples/UsbConnection
 * For arduino(with bluetooth): opendevice-hardware-libraries/arduino/OpenDevice/examples/BluetoothConnection
 */
public class OnOffGUI extends SimpleDeviceManager  {

	private FormController form;
    private DeviceConnection connection;

	public OnOffGUI() {

        connection = Connections.out.bluetooth("001303141907"); // Connect to first USB port available

        addOutput(connection);

		form = new FormController();
		form.setConnection(connection);
		form.addButton("ON", btnListener);
		form.addButton("OFF", btnListener);
		form.setVisible(true);

	}
	private ActionListener btnListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent event) {
			String btnName = event.getActionCommand();

			try {
				if (btnName.equalsIgnoreCase("ON")) {
					System.out.println("SEND: ON");
                    send(DeviceCommand.ON(1)); // '1' is deviceID not PIN
				}
				
				if (btnName.equalsIgnoreCase("OFF")) {
					System.out.println("SEND: OFF");
                    send(DeviceCommand.OFF(1));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	};
	

	
	public static void main(String[] args) throws InterruptedException {
		new OnOffGUI();
	}

}
