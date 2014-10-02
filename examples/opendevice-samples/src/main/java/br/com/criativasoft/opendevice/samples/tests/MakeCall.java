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

package br.com.criativasoft.opendevice.samples.tests;

import br.com.criativasoft.opendevice.connection.*;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;

import java.io.IOException;
import java.util.Collection;

/**
 * TODO: PENDING DOC
 *
 * @author ricardo
 * @date 30/06/14.
 */
public class MakeCall {

    public static void main(String[] args) throws InterruptedException {
        Collection<String> strings = UsbConnection.listAvailablePortNames();
        System.out.println(strings);

        StreamConnection usb = StreamConnectionFactory.createUsb("/dev/ttyACM0");

        try {

            usb.connect();
            usb.send(new SimpleMessage("AT\r\n"));

            usb.send(new SimpleMessage("AT+VCID=1\r\n"));
            // DTMF


            //usb.write("ATD99689513;\r\n");

        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        usb.addListener(new ConnectionListener() {
            @Override
            public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
                System.out.println("Connected !!!");
            }

            @Override
            public void onMessageReceived(Message message, DeviceConnection connection) {
                System.out.println(message);
                String msg = message.toString();
                try {


                    if (msg.contains("RING")) {

                        connection.send(new SimpleMessage("AT\r\n"));

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


        while (true) {
            Thread.sleep(1000);
        }

    }

}
