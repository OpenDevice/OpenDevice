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
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FormController extends JFrame implements ConnectionListener, ActionListener {
	
	private DeviceConnection connection;
	private JTextArea text;
	private JPanel panelButtons;
	
	public FormController() {
		 JFrame.setDefaultLookAndFeelDecorated(true);
		 
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		this.setTitle("Controller");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setLayout(new FlowLayout());
		this.setSize(400, 200);
		this.setLayout(new GridLayout(0,1));
		
		initComponents();
//		pack();
	}
	
	public void setConnection(DeviceConnection connection) {
		this.connection = connection;
		this.connection.addListener(this);
	}
	
	public JButton addButton(String name, ActionListener listener){
		JButton btn = new JButton(name);
		btn.addActionListener(listener);
		btn.setActionCommand(name);
		panelButtons.add(btn);
		return btn;
	}
	
	
	public void writeLog(String value){
		text.append(value);
		text.append("\n");	
	}

	private void initComponents() {
		
//		JPanel panel = new JPanel();
//		panel.setBorder(new TitledBorder("Logs"));
//		text = new JTextArea();
//		panel.add(text);
//		this.add(panel);
		
		text = new JTextArea();
		DefaultCaret caret = (DefaultCaret)text.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		JScrollPane scrollPane = new JScrollPane(text); 
		scrollPane.setBorder(new TitledBorder("Logs"));
		scrollPane.setAutoscrolls(true);
		this.add(scrollPane);
		
		panelButtons = new JPanel();
		panelButtons.setBorder(new TitledBorder("Control"));
		panelButtons.setLayout(new GridLayout(0,2));
		this.add(panelButtons);
		
		this.addButton("Connect", this);
		this.addButton("Disconnect", this);
	}

	@Override
	public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {

		if(connection.isConnected()){
			text.append("Connected !");
		}else{
			text.append("Disconnected !");
		}
		
		text.append("\n");
	}

	@Override
	public void onMessageReceived(Message message, DeviceConnection connection) {
		SimpleMessage stream = (SimpleMessage) message;
		String value = new String(stream.getBytes());
		text.append(value);
		text.append("\n");
	}
	
	
	@Override
	public void actionPerformed(ActionEvent event) {
		String btnName = event.getActionCommand();

		try {

			if (btnName.equalsIgnoreCase("connect")) {
				System.out.println("Connecting...");
                try {
                    connection.connect();
                }catch (Exception e){
                    e.printStackTrace();
                    writeLog("ERROR: " + e.getMessage());
                }
			}

			if (btnName.equalsIgnoreCase("disconnect")) {
				System.out.println("disconnect...");
				connection.disconnect();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		FormController formAction = new FormController();
		formAction.setVisible(true);
	}
	
}
