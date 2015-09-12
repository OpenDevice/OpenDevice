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

import br.com.criativasoft.opendevice.connection.*;
import br.com.criativasoft.opendevice.connection.message.ByteMessage;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;

import java.io.IOException;

/**
 * Note: You can change do Bluetooth  !
 * USE: samples/arduino/UsbConnection
 * @author Ricardo JL Rufino
 * @date 17/06/2014
 */
public class SerialCommunicationDemo implements ConnectionListener {

    protected StreamConnection connection;
    protected byte currentValue = 1;

    public SerialCommunicationDemo()  {

        connection = StreamConnectionFactory.createUsb("/dev/ttyACM0");
        //connection = new TCPConnection("192.168.0.11:8282");
        connection.addListener(this);

        try {
            connection.connect();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public static void main(String[] args) throws Exception {

        SerialCommunicationDemo _this = new SerialCommunicationDemo();
		
		while(_this.connection.isConnected()){
			Thread.sleep(100);
		}

	}


    protected void show(Message message){
        System.out.println("RECEIVED > " + message.toString());
    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

        byte[] bytes = ((ByteMessage)message).getBytes();
        String string = new String(bytes).trim();
        show(message);

        try {
            if(string.contains("INIT")){
                this.connection.write(++currentValue);
            }

            if(string.startsWith("READ")){
                String value = string.split(":")[1];
                currentValue = (byte) Integer.parseInt(value);// arduino range 0..255
                this.connection.write(++currentValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionStateChanged(DeviceConnection connection,ConnectionStatus status) {System.out.println("conected...");
        try {
            this.connection.send(SimpleMessage.HIGH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
