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

package opendevice.io.tests.newconns;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.GetDevicesResponse;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Board;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.PhysicalDevice;
import br.com.criativasoft.opendevice.mqtt.MQTTServerConnection;

import java.io.IOException;

public class MQTTEmbedded extends LocalDeviceManager {

    public static void main(String[] args) { launch(args); }

    @Override
    public void start() throws IOException {

        final Device led = new PhysicalDevice(1);

        addConnectionListener(new ConnectionListener() {
            @Override
            public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
                System.err.println(" connectionStateChanged = " + status);
            }

            @Override
            public void onMessageReceived(Message message, DeviceConnection connection) {
                System.err.println("onMessageReceived : " + message);

                if(message instanceof GetDevicesResponse){
                    while (true){
                        led.toggle();
                        delay(1000);
                    }
                }
            }
        });


        addInput(new MQTTServerConnection());

        addListener(new DeviceListener() {
            @Override
            public void onDeviceRegistred(Device device) {
                System.out.println(" >>> " + device);

                if(device instanceof Board){
                    System.out.println("boarc devices : " + ((Board) device).getDevices());
                }

            }

            @Override
            public void onDeviceChanged(Device device) {

            }
        });


        connect();


    }
}
