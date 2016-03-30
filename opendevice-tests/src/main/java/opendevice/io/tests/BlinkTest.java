/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package opendevice.io.tests;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import br.com.criativasoft.opendevice.core.model.Device;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 03/11/15
 */
public class BlinkTest extends LocalDeviceManager {

    public static void main(String[] args) { launch(args); }

    private AtomicInteger responses = new AtomicInteger();

    public void start() throws IOException {

        Device led = new Device(1, Device.DIGITAL);

//        connect(new MQTTServerConnection());

//        connect(out.tcp("192.168.3.100:8182"));
//        connect(out.tcp("192.168.3.100:8182"));
//        connect(out.tcp("192.168.43.149:8182"));
//        connect(out.usb());
        connect(out.bluetooth("20:13:01:24:01:93"));

//        led.on();

        addConnectionListener(new ConnectionListener() {
            @Override
            public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {

            }

            @Override
            public void onMessageReceived(Message message, DeviceConnection connection) {

                if (message instanceof ResponseCommand) {
                    int i = responses.incrementAndGet();
                    System.out.println("Responses: " + i + " of " + getCommandDelivery().getCmdCount());
                } else {
                    System.out.println("Command: " + message);

                }

            }
        });

//        // FIXME Não está desconnectando o TCP....
//        // Quando desabilita a WIFI do PC, ele não detectou.
//        // Quando o Modulo cancela a cexçao, fica dando erro.
        while(isConnected()){
            led.on();
            delay(1000);
            led.off();
            delay(1000);
        }


    }
}
