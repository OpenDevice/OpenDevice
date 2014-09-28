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

package br.com.criativasoft.opendevice.restapi;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.Request;
import br.com.criativasoft.opendevice.core.command.Command;


/**
 * Created by ricardo on 21/09/14.
 */
public class WaitResponseListener {

    private DeviceConnection connection;
    private Request waitCommand;
    private Command lastCommand;

    private Object lock = new Object();

    public WaitResponseListener(Request waitCommand, DeviceConnection connection) {
        this.waitCommand = waitCommand;
        this.connection = connection;
    }


    public boolean accept(Message message) {

        if(waitCommand.getResponseType().isAssignableFrom(message.getClass())){
            // TODO: verificar o UID da requisição.
            this.lastCommand = (Command) message;

            synchronized(lock){
                lock.notifyAll();
            }

            return true;
        }

        return false;

    }

    public Command getResponse(int timeout) throws InterruptedException {

        connection.notifyListeners(waitCommand);

        synchronized(lock){
            lock.wait(timeout);
        }

        return lastCommand;
    };
}
