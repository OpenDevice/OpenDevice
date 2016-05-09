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

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import br.com.criativasoft.opendevice.core.command.SimpleCommand;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// Test to receive ping request from physical modules
public class PingResponseTest extends LocalDeviceManager implements ConnectionListener {


    AtomicInteger qsend = new AtomicInteger();
    AtomicInteger qrec = new AtomicInteger();

    AtomicInteger timeouts = new AtomicInteger();

    AtomicLong lastReceived = new AtomicLong(0);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start() throws IOException {

        addConnectionListener(this);

//        connect(Connections.out.usb());
        connect(out.tcp("192.168.3.100:8182"));

        delay(2000);
        send(new SimpleCommand(CommandType.PING_REQUEST, 0));
        qsend.incrementAndGet();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (System.currentTimeMillis() - lastReceived.get() > 1000) {
                    System.err.println("TIMEOUT");
                    timeouts.incrementAndGet();
                    try {
                        sendTo(new SimpleCommand(CommandType.PING_REQUEST, 0), getOutputConnections());
                        qsend.incrementAndGet();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }, 1000, 1000);
    }


    @Override
    public void connectionStateChanged(final DeviceConnection connection, ConnectionStatus status) {

    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

        if(message instanceof ResponseCommand && ((ResponseCommand) message).getType() == CommandType.PING_RESPONSE){

            int rec = qrec.incrementAndGet();

            System.err.println("Stats: Received/Send = " + rec + "/" + qsend.get() + " ----- thread: " + Thread.activeCount() + ", timeouts:" + timeouts.get());

            lastReceived.set(System.currentTimeMillis());

            try {
                send(new SimpleCommand(CommandType.PING_REQUEST, 0));
                qsend.incrementAndGet();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
