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

import br.com.criativasoft.opendevice.atemospherews.RestServerConnection;
import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.*;

/**
 * TODO: colocar qual exemplo deve ser usado...
 * @author Ricardo JL Rufino
 * @date 17/02/2014
 */
public class BlinkDeviceEnergiaDemo extends SimpleDeviceManager implements DeviceListener {

    Device led1 = new Device(1, DeviceType.DIGITAL);
    Device led2 = new Device(2, DeviceType.DIGITAL);
    Device led3 = new Device(3, DeviceType.DIGITAL);

    Sensor btn1 = new Sensor(4, DeviceType.DIGITAL);
    Sensor btn2 = new Sensor(5, DeviceType.DIGITAL);

    public static void main(String[] args) throws Exception {
        new BlinkDeviceEnergiaDemo();
    }

    public BlinkDeviceEnergiaDemo() throws Exception {

        // setup connection with arduino/hardware
        addOutput(Connections.out.bluetooth("001303141907")); // Connect to first USB port available

        // Configure a Rest interface for receiving commands over HTTP
        // Access the URL in the browser: http://localhost:8181/device/1/value/1
        addInput(new RestServerConnection(8181));

        addListener(this); // monitor changes on devices
        connect(); // Connects all configured connections

        addDevice(led1);
        addDevice(led2);
        addDevice(led3);
        addDevice(btn1);
        addDevice(btn2);

        while(true){
            led1.on();
            Thread.sleep(500);
            led1.off();
            Thread.sleep(500);
        }
    }

    // If the device has changed this method is called.
    public void onDeviceChanged(Device device) {

        System.out.println("DeviceChanged = " + device);

        if(device == btn1){
            led1.setValue(device.getValue());
        }

        if(device == btn2){
            led2.setValue(device.getValue());
        }
    }


}
