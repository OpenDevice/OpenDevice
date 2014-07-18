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

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.StreamConnectionFactory;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.GetDevicesRequest;
import br.com.criativasoft.opendevice.core.command.GetDevicesResponse;
import br.com.criativasoft.opendevice.samples.ui.FormDevicesAPIController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class GetDevicesTest extends FormDevicesAPIController {


    public GetDevicesTest() throws ConnectionException {
        super(StreamConnectionFactory.createTcp("192.168.0.204:8081"));
        add(new JButton("Get Devices"));
    }

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {
        super.onMessageReceived(message, connection);

        if(message instanceof GetDevicesResponse){


        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        if(e.getActionCommand().equals("Get Devices")){
            try {

                getConnection().send(new GetDevicesRequest());

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    public static void main(String[] args) throws ConnectionException {
        new GetDevicesTest().setVisible(true);
    }
}
