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

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.IWSServerConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.ActionCommand;
import br.com.criativasoft.opendevice.core.command.GetDevicesRequest;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.listener.OnDeviceChangeListener;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.Sensor;
import br.com.criativasoft.opendevice.core.model.test.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 10/01/16
 */
public class RFandIPCamTest extends LocalDeviceManager {

    private static final Logger log = LoggerFactory.getLogger(RFandIPCamTest.class);

    static int imageSequence = 0;

    public static void main(String[] args) { launch(args); }

    public void start() throws IOException {

        final Device rf = new Sensor(1, Device.NUMERIC);
        rf.setName("RF Sensor");

        final Device test = new Sensor(2, Device.NUMERIC);
        test.setName("test Sensor");
        test.onChange(new OnDeviceChangeListener() {
            @Override
            public void onDeviceChanged(Device device) {
                log.debug("teste ok ... value: " + device.getValue());
            }
        });

        BasicAuth auth = new BasicAuth("admin", "soft2011");

        final IPCamDevice ipCamDevice = new IPCamDevice(10);

        addCategory(CategoryIPCam.class); // register new device class

        ipCamDevice.setCategory(getCategory(CategoryIPCam.class));

//        // TODO: isso deve ser dinamico...
//        List<PropertyDef> properties = categoryIPCam.getProperties();
//        for (PropertyDef def : properties) {
//            Property property = new Property();
//            property.setDefinition(def);
//            property.setValue("TEST fro all");
//            ipCamDevice.addProperty(property);
//        }


        final IPCamConnection ipCamConnection = new IPCamConnection("http://192.168.3.104:81", auth);
        ipCamDevice.setConnection(ipCamConnection);
        addOutput(ipCamConnection);

        // force load devices...
        ipCamConnection.addListener(new ConnectionListener() {
            @Override
            public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
                if (connection.isConnected()) {
                    try {
                        ipCamConnection.send(new GetDevicesRequest());
                    } catch (IOException e) {}
                }
            }

            @Override
            public void onMessageReceived(Message message, DeviceConnection connection) {

            }
        });

        ipCamDevice.addListener(new PropertyChangeListener() {
            @Override
            public void onPropertyChange(GenericDevice device, Property property) {
                System.out.println("onPropertyChange : " + property);
            }
        });

        // TODO: quem deve fazer isso é o BaseDeviceManager
        ipCamDevice.addListener(new ActionCommandListener() {
            @Override
            public void onExecuteAction(GenericDevice device, ActionDef action, List<Object> values) {
                try {
                    ipCamConnection.send(new ActionCommand(device.getUid(), action.getName(), new ArrayList<Object>(values)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        ipCamConnection.setSnapshotListener(new SnapshotListener() {
            @Override
            public void onSnapshot(byte[] image) {
                try {
                    // TODO: e o Device ?
                    FileOutputStream fos = new FileOutputStream("/home/ricardo/Pictures/camera" + (imageSequence++) + ".jpg");
                    fos.write(image);
                    fos.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        addListener(new DeviceListener() {
            @Override
            public void onDeviceRegistred(Device device) {

            }

            @Override
            public void onDeviceChanged(Device device) {
                if (device == rf && rf.getValue() > 0) {
                    ipCamDevice.execute(CategoryIPCam.snapshot, null);
                }
            }
        });

        // Setup WebSocket (Socket.IO) with suport for simple htttpServer
        IWSServerConnection webscoket = Connections.in.websocket(8181);
        String current = System.getProperty("user.dir");

        // Rest Resources
        webscoket.addResource(PropertyCommandRest.class);
        webscoket.addResource(ActionCommandRest.class);
        webscoket.addResource(FileRest.class);

        // Static WebResources
        webscoket.addWebResource("/media/ricardo/Dados/Codidos/Java/Projetos/OpenDevice/opendevice-tests/src/main/resources/webapp/ipcam");
        webscoket.addWebResource("/media/ricardo/Dados/Codidos/Java/Projetos/OpenDevice/opendevice-clients/opendevice-js/dist");
        webscoket.addWebResource("/home/ricardo/Pictures/");

        // FIMXE: remove this...
        webscoket.addWebResource("/media/ricardo/Dados/Codidos/Java/Projetos/OpenDevice/opendevice-clients/opendevice-js/src/js");

        this.addInput(webscoket);

//        connect(Connections.out.usb());
        connect(); // FIXME: o websocket está bloqueando aqui...



    }

}
