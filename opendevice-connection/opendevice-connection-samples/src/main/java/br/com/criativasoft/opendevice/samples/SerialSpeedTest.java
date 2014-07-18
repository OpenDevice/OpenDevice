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
import br.com.criativasoft.opendevice.connection.message.ByteMessage;
import br.com.criativasoft.opendevice.connection.message.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Note: You can change do Bluetooth
 * @see SerialCommunicationDemo
 * USE: samples/arduino/UsbConnection
 * @author Ricardo JL Rufino
 * @date 17/06/2014
 */
public class SerialSpeedTest extends SerialCommunicationDemo {

    // -- Ignore this (it's just debug speed information)
    private long readLeght = 0;
    private long lastTime = 0;
    DateFormat sdf = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM);
    // -- END

    public static void main(String[] args) throws Exception {

        SerialSpeedTest _this = new SerialSpeedTest();
		
		while(_this.connection.isConnected()){
			Thread.sleep(100);
		}

	}

    @Override
    protected void show(Message message) {
        // -- Ignore this (it's just debug speed information)
        byte[] bytes = ((ByteMessage)message).getBytes();
        String string = new String(bytes).trim();
        readLeght += bytes.length;
        System.out.println(sdf.format(new Date()) + " - RECEIVED["+readLeght+"bytes / "+calculateDelay()+"ms] > " + string);
        // -- END
    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {
        super.onMessageReceived(message, connection);
    }


    // -- Ignore this (it's just debug speed information)
    private long calculateDelay(){
        long current = System.currentTimeMillis();

        if(lastTime == 0){
            lastTime = current;
            return 0;
        }

        long delay = current - lastTime;
        lastTime = current;
        return delay;
    }


}
