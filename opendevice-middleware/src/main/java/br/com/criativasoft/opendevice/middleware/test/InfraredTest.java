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

package br.com.criativasoft.opendevice.middleware.test;

import javax.swing.*;


public class InfraredTest  extends JFrame /* implements ConnectionListener*/ {
//	private BluetoothConnection connection;
//	private JTextArea text;
//
//	public br.com.criativasoft.opendevice.server.test.InfraredTest() {
//		this.init();
////		connection = new BluetoothDesktopConnection("00:11:06:14:04:57"); // stellaris
//		connection = new BluetoothDesktopConnection("00:11:09:25:04:75"); // arduino
//
//		try {
//			connection.connect();
//			connection.addListener(this);
//		} catch (ConnectionException ex) {
//			JOptionPane.showMessageDialog(null, ex.getMessage());
//		}
//	}
//
//	public void init(){
//
//		text = new JTextArea();
//		JScrollPane scrollPane = new JScrollPane(text);
//		scrollPane.setPreferredSize(new Dimension(250, 250));
//		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//		this.add(scrollPane);
//		this.add(new JButton("Clear"));
//        this.setLocation(150, 150);
//
//        this.setLayout(new FlowLayout());
//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        this.pack();
//        this.setVisible(true);
//	}
//
//
//
//	@Override
//	public void commandReceived(Command command, DeviceConnection connection) {
//
//		if(command.getType() == CommandType.INFRA_RED){
//
//			DeviceCommand deviceCommand = (DeviceCommand) command;
//
//			long value = deviceCommand.getValue();
//
//			text.append(Long.toHexString(value));
//			text.append("\n");
//
//			text.setCaretPosition(text.getCaretPosition()+text.getText().length());
//
//		}
//
//	}
//
//
//
//	public static void main(String[] args) {
//		new br.com.criativasoft.opendevice.server.test.InfraredTest();
//	}
//
//	@Override
//	public void connectionStateChanged(DeviceConnection connection,
//			ConnectionStatus status) {
//		// TODO Auto-generated method stub
//
//	}
}
