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

package br.com.criativasoft.opendevice.samples.ui;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.model.Device;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;


// USE: OpenDevice Middleware in Arduino...
public class FormDevicesAPIController extends JFrame implements ConnectionListener {

    private DeviceConnection connection;
	private boolean state = false;

    DeviceManager manager = new SimpleDeviceManager();

	public FormDevicesAPIController(DeviceConnection connection) throws ConnectionException {
		this.init();
        this.setTitle("Controller (JavaSE)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.connection = connection;
        manager.addOutput(connection);
        connection.addListener(this);
    }

    public void connect(){
        try {
            manager.connect();
        } catch (ConnectionException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addDevices(Collection<Device> devices){
        manager.addDevices(devices);
        for (Device device : devices){
            PowerButton powerButton = new PowerButton(device);
            add(powerButton);
        }
        pack(); // force resize
    }

    @Override
    public void connectionStateChanged(DeviceConnection connection,ConnectionStatus status) {
        System.out.println(">>> DeviceConnection = " + status);
    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

		if(message instanceof DeviceCommand){
		
			DeviceCommand deviceCommand = (DeviceCommand) message;
			
			int deviceID = deviceCommand.getDeviceID();
			long value = deviceCommand.getValue();
//
//            AbstractButton targetButton = null;
//            switch (deviceID){
//                case 1: targetButton = btn1; break;
//                case 2: targetButton = btn2; break;
//                case 3: targetButton = btn3; break;
//                case 4: targetButton = btn4; break;
//            }
//
//            if(targetButton != null){
//                changeButton(targetButton, deviceID, value);
//            }
			
			if(deviceID == 51){
				
				System.out.println("INFRA-RED: " + Long.toHexString(value));
			
				if(value == 0x800f8422){
					
					System.out.println("INFRA-RED: - > BTN: OK");
					
					DeviceCommand onoff = new DeviceCommand(CommandType.ON_OFF, 1, (state ? 1 : 0));
					
					if(state == true) state = false;
					if(state == false) state = true;

                    try {
                        connection.send(onoff);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
			}
		
		}
		
		
		
		
	}
	
	public void init(){

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setTitle("Java Client (using: WebSocket)");

        final Collection<Device> devices = manager.getDevices();

		final JButton btn5 = new JButton("Disconnect");
        super.add (btn5);
        this.setLocation(150, 150);
        
        this.setLayout(new FlowLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
        
        btn5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = btn5.getText();
				try{
					if(text.equals("Disconnect")){
						connection.disconnect();

                        for (Device device : devices){
                            device.setValue(0);
                        }

						btn5.setText("Connect");
					}else{
						connection.connect();
						btn5.setText("Disconnect");
					}
				}catch (Exception ex) {
                    JOptionPane.showMessageDialog(FormDevicesAPIController.this, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});
        
        
	}

    public DeviceConnection getConnection() {
        return connection;
    }
}
