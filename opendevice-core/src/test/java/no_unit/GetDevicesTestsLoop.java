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

package no_unit;

import br.com.criativasoft.opendevice.connection.AbstractStreamConnection;
import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.GetDevicesResponse;
import br.com.criativasoft.opendevice.core.connection.MultipleConnection;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

// Test GetDevicesRequest between multiples re-connections
public class GetDevicesTestsLoop extends LocalDeviceManager implements ConnectionListener {

    private List<Command> receivedPartial = new LinkedList<Command>();

    private AtomicInteger successCount = new AtomicInteger();
    
    private Timer timer;

    private static final int MAX = 10;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start() throws IOException {

        addConnectionListener(this);
        
        connect(out.usb());

    }

    @Override
    public void connectionStateChanged(final DeviceConnection connection, ConnectionStatus status) {

    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

        if(message instanceof GetDevicesResponse){
            GetDevicesResponse response = (GetDevicesResponse) message;

            receivedPartial.add(response);
            System.err.println("Sync: " + receivedPartial.size() + "/" + response.getLength() + " ||| devices: " + getDevices().size() + ", run: " + successCount + "/"+MAX );

            if(receivedPartial.size() == response.getLength() ) {
                
                System.err.println("INTERACTION :" + successCount + " - DONE");
                
                receivedPartial.clear();
                int count = successCount.incrementAndGet();

                if(count == MAX) {
                    stop();
                    return;
                }
                
                restarTest();
                
            }
            
        }

    }
    
    private void restarTest() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    disconnect();
                    delay(2000);
                    connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 1000);
    }
}
