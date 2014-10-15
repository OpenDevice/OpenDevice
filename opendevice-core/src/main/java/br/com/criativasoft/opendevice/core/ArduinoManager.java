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
 * Give preference to extend {@link SimpleDeviceManager}
 *
 * @author Ricardo JL Rufino on 14/10/14.
 */
public abstract class ArduinoManager extends SimpleDeviceManager{

    public ArduinoManager(){

        DeviceConnection connection = this.setup();
        if(connection == null) throw new NullPointerException("Connection is NULL !");

        addOutput(connection);
        connection.addListener(listener);

        try {

            connect();

            for (int i = 0; i < 5; i++) {
                System.out.println("Waiting !!");
                if(!connection.isConnected()){
                    Thread.sleep(1000);
                    if(! connection.isConnected()) System.out.println("Waiting connection...");
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
            if(status == ConnectionStatus.CONNECTED){
                while(true){
                    loop();
                }
            }
        }

        @Override
        public void onMessageReceived(Message message, DeviceConnection connection) {

        }
    };

}
