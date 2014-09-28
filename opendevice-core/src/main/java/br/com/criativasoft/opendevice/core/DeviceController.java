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

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.StreamConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.CommandStreamReader;
import br.com.criativasoft.opendevice.core.command.CommandStreamSerializer;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.model.DeviceListener;

import java.io.IOException;

/**
 * FIXME: Not USED !! precisa de um novo modelo de listener pois não é orientado a device (talvex um DeviceValueListener) !
 */
public class DeviceController {

    private DeviceConnection connection;

    public DeviceController(DeviceConnection connection) {
        this.connection = connection;
        if(connection instanceof StreamConnection){
            StreamConnection streamConnection = (StreamConnection) connection;
            streamConnection.setSerializer(new CommandStreamSerializer()); // data conversion..
            streamConnection.setStreamReader(new CommandStreamReader()); // data protocol..
        }

    }

    public DeviceConnection getConnection() {
        return connection;
    }

    public void on(int deviceID) throws IOException {
        send(DeviceCommand.ON(deviceID));
    }

    public void off(int deviceID) throws IOException {
        send(DeviceCommand.OFF(deviceID));
    }

    public void brightness(int deviceID, long value) throws IOException {
        send(new DeviceCommand(CommandType.PWM, deviceID, value));
    }

    public void listen(int deviceID, DeviceListener listener){  // talvez um java 8


    }

    private void send(Message msg) throws IOException {
        if(connection.isConnected()){
            connection.send(msg);
        }
    }

}
