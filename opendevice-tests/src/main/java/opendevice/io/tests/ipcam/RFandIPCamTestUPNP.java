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

package opendevice.io.tests.ipcam;

import br.com.criativasoft.opendevice.connection.IWSServerConnection;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.listener.OnDeviceChangeListener;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.Sensor;
import opendevice.io.tests.MediaCenterDevice;

import java.io.IOException;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 10/01/16
 */
public class RFandIPCamTestUPNP extends LocalDeviceManager {

    public static void main(String[] args) { launch(args); }

    public void start() throws IOException {

        Device rf = new Sensor(1, Device.NUMERIC);
        rf.setName("RF Sensor");

        BasicAuth auth = new BasicAuth("admin", "soft2011");

        IPCamConnection connection = new IPCamConnection("http://192.168.1.102:81", auth);


        // Setup WebSocket (Socket.IO) with suport for simple htttpServer
        IWSServerConnection webscoket = Connections.in.websocket(8181);
        String current = System.getProperty("user.dir");

        // Rest Resources
        webscoket.addResource(PropertyCommandRest.class);

        // Static WebResources
        webscoket.addWebResource("/media/ricardo/Dados/Codidos/Java/Projetos/OpenDevice/opendevice-tests/src/main/resources/webapp/ipcam");
        webscoket.addWebResource("/media/ricardo/Dados/Codidos/Java/Projetos/OpenDevice/opendevice-clients/opendevice-js/dist");

        this.addInput(webscoket);

//        connection.connect();

        final MediaCenterDevice mediaCenter = new MediaCenterDevice();
        Thread clientThread = new Thread(mediaCenter);
        clientThread.setDaemon(false);
        clientThread.start();

        rf.onChange(new OnDeviceChangeListener() {
            @Override
            public void onDeviceChanged(Device device) {
                System.out.println("RF RECEIVED");
                if(device.getValue() > 0){
                    mediaCenter.executeAction();
                }
            }
        });


        connect(Connections.out.usb());




    }

}
