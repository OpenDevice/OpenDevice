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
import br.com.criativasoft.opendevice.connection.StreamConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.ByteMessage;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.CommandStreamReader;
import br.com.criativasoft.opendevice.core.command.CommandStreamSerializer;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


// USE: OpenDevice Middleware in Arduino...
public class FormDevicesAPIController extends JFrame implements ActionListener, ConnectionListener {

    private DeviceConnection connection;
	private boolean state = false;

    private JButton btn1;
    private JButton btn2;
    private JButton btn3;
    private JButton btn4;

	public FormDevicesAPIController(DeviceConnection connection) throws ConnectionException {
		this.init();
        this.setTitle("Controller");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.connection = connection;

        if(connection instanceof StreamConnection){
            StreamConnection streamConnection = (StreamConnection) connection;
            streamConnection.setSerializer(new CommandStreamSerializer()); // data conversion..
            streamConnection.setStreamReader(new CommandStreamReader()); // data protocol reader..
        }

		try {
            connection.addListener(this);
            connection.connect();
		} catch (ConnectionException ex) {
			ex.printStackTrace();
		}
	}
	 

    @Override
    public void connectionStateChanged(DeviceConnection connection,ConnectionStatus status) {
        System.out.println(">>> DeviceConnection = " + status);
    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

		System.out.println(">>> commandReceived = " + message.getClass().getSimpleName());

        if(message instanceof ByteMessage){
            System.out.println(">>> commandReceived = " + message.toString());
        }
		
		if(message instanceof DeviceCommand){
		
			DeviceCommand deviceCommand = (DeviceCommand) message;
			
			int deviceID = deviceCommand.getDeviceID();
			long value = deviceCommand.getValue();

            JButton targetButton = null;
            switch (deviceID){
                case 1: targetButton = btn1; break;
                case 2: targetButton = btn2; break;
                case 3: targetButton = btn3; break;
                case 4: targetButton = btn4; break;
            }

            if(targetButton != null){
                changeButton(targetButton, deviceID, value);
            }
			
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
		btn1 = new JButton("1.OFF");
		btn2 = new JButton("2.OFF");
		btn3 = new JButton("3.OFF");
		btn4 = new JButton("4.OFF");
		final JButton btn5 = new JButton("Disconnect");
		
        this.add(btn1);
        this.add(btn2);
        this.add(btn3);
        this.add(btn4);
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
                        changeButton(btn1, 1, 0);
                        changeButton(btn2, 2, 0);
                        changeButton(btn3, 3, 0);
                        changeButton(btn4, 4, 0);

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

	protected void add(JButton button){

        button.setMinimumSize(new Dimension(100, 60));
        button.setPreferredSize(new Dimension(100, 60));
        button.setFont(new Font(Font.SANS_SERIF, 3, 15));
        button.setActionCommand(button.getText());
        button.addActionListener(this);
        super.add(button);
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton btn = (JButton) e.getSource();

        String text = btn.getText();
        if(text.contains(".")){
            int id = Integer.parseInt(text.split("\\.")[0]);
            String value = text.split("\\.")[1];

            DeviceCommand command = new DeviceCommand(CommandType.ON_OFF, id, (value.equals("ON") ? 0 : 1));

            changeButton(btn, id, (value.equals("ON") ? 0 : 1));

            try {
                connection.send(command);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    private void changeButton(JButton btn, int id, long value){
        btn.setText(id + "." +  (value == 1 ? "ON" : "OFF")); // Alternar texto.
        btn.setForeground(value == 1 ? Color.green : Color.black); // Alternar texto.
    }

    public DeviceConnection getConnection() {
        return connection;
    }
}
