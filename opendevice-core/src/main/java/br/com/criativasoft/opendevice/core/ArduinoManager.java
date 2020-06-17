/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

package br.com.criativasoft.opendevice.core;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;

import java.io.IOException;

/**
 * Class that follows the structure for implementing the Arduino,
 * it is not advisable for real projects, just enough for simple things.
 * Give preference to extend {@link LocalDeviceManager}
 *
 * @author Ricardo JL Rufino on 14/10/14.
 */
public abstract class ArduinoManager extends LocalDeviceManager {

    public ArduinoManager() {
        super();

        DeviceConnection connection = this.setup();
        if (connection == null) throw new NullPointerException("Connection is NULL !");

        addOutput(connection);
        connection.addListener(listener);

        try {

            connect();

            int max = 5;
            for (int i = 1; i <= max; i++) {
                if (!connection.isConnected()) {
                    log.debug("Waiting connection...");
                    Thread.sleep(1000);
                    if (!connection.isConnected()) {
                        log.debug("try reconnect [" + (i) + "/" + max + "]");
                        connect();
                    }

                }

                System.out.println("Error, connection not found !");
            }

        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public abstract DeviceConnection setup();

    public abstract void loop();

    private ConnectionListener listener = new ConnectionListener() {
        @Override
        public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
            if (status == ConnectionStatus.CONNECTED) {
                while (true) {
                    loop();
                }
            }
        }

        @Override
        public void onMessageReceived(Message message, DeviceConnection connection) {

        }
    };

}
