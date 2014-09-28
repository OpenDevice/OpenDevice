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

package br.com.criativasoft.opendevice.samples;

import br.com.criativasoft.opendevice.connection.IWSServerConnection;
import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;

/**
 *
 * Run using Maven using: mvn compile exec:java -Dexec.mainClass=com.example.Main
 * Access the URL in the browser: http://localhost:8181   <br/>
 * And access the URL in the another tab: http://localhost:8181/rest-jquery.html   <br/>
 *
 * https://opendevice.atlassian.net/wiki/display/DOC/Step+2+-+Adding+REST
 * For arduino/energia use: opendevice-hardware-libraries/arduino/OpenDevice/examples/UsbConnection
 * For arduino(with bluetooth): opendevice-hardware-libraries/arduino/OpenDevice/examples/BluetoothConnection
 * For energia/launchpad : examples/EnergiaLaunchPadBasic
 *
 * @author Ricardo JL Rufino
 * @date 17/08/2014
 */
public class WebSocketDemo extends SimpleDeviceManager {

    public static void main(String[] args) throws Exception {
        new WebSocketDemo();
    }

    public WebSocketDemo() throws Exception {

        setApplicationID("clientname-123456");

        // setup connection with arduino/hardware
        addOutput(Connections.out.usb()); // Connect to first USB port available
        addOutput(Connections.out.bluetooth("001303141907"));

        // Configure a Websocket interface for receiving commands over HTTP
        IWSServerConnection server = Connections.in.websocket(8181);
        // Static WebResources
        String current = System.getProperty("user.dir");
        server.addWebResource( current + "/webapp");
        server.addWebResource( current + "/src/main/webapp"); // running exec:java
        server.addWebResource( current + "/target/classes/webapp"); // opendevice-js dependecy.
        // server.addWebResource("/media/Dados/Codigos/Java/Projetos/OpenDevice/opendevice-clients/opendevice-js/src/main/resources/webapp");

        addInput(server);
        connect(); // Connects all configured connections

        addDevice(new Device(1, "led 1", DeviceType.DIGITAL));
        addDevice(new Device(2, "led 2", DeviceType.DIGITAL));
        addDevice(new Device(3, "led 3", DeviceType.DIGITAL));
        addDevice(new Sensor(4, "btn 1", DeviceType.DIGITAL));
        addDevice(new Sensor(5, "btn 2", DeviceType.DIGITAL));

    }

}
