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
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import opendevice.io.tests.util.ResetableTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 06/05/16
 */
public class TestReponseInterval extends LocalDeviceManager implements DeviceListener, ConnectionListener {

    private List<Long> interval = new ArrayList<Long>();
    private long lastReceived;
    private ResetableTimer timout;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start() throws IOException {

        addOutput(Connections.out.websocket("localhost:8181"));

        addListener(this);

        timout = new ResetableTimer(3000);

        addConnectionListener(this);

        connect();
    }

    @Override
    public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
        if (status == ConnectionStatus.CONNECTED){
            System.out.println("CONNECTED");
            timout.onTimeout(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Total: " + interval.size());
                    System.out.println("interval: " + interval);
                }
            });
        }
    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

    }

    @Override
    public void onDeviceChanged(Device device) {

        System.out.println("" +device.getValue());

        timout.reset();

        long current = System.currentTimeMillis();

        if(lastReceived > 0){

            interval.add(current - lastReceived);

        }

        lastReceived = current;


    }
}
