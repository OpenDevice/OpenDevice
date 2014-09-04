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
import br.com.criativasoft.opendevice.connection.StreamConnectionFactory;
import br.com.criativasoft.opendevice.connection.UsbConnection;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.DeviceType;

/**
 * @author Ricardo JL Rufino
 * @date 17/02/2014
 */
public class BlinkDeviceDemo implements DeviceListener {

    private static DeviceManager manager = new SimpleDeviceManager();

    private static Device led = new Device(1, DeviceType.DIGITAL);

    public static void main(String[] args) throws Exception {

        new BlinkDeviceDemo();
    }

    public BlinkDeviceDemo() throws Exception {

        String usbPort = UsbConnection.getFirstAvailable();

        // setup connection with arduino/hardware
        manager.addOutput(StreamConnectionFactory.createUsb(usbPort)); // Connect to first USB port available
        // manager.addOutput(StreamConnectionFactory.createBluetooth("20:13:01:24:01:93"));

        // Configure a Rest interface for receiving commands over HTTP
        // Access the URL in the browser: http://localhost:8181/device/1/value/1
        manager.addInput(new RestServerConnection(8181));

        manager.addListener(this); // monitor changes on devices
        manager.connect();

        manager.addDevice(led);

        while(true){
//            led.on();
//            Thread.sleep(500);
//            led.off();
            Thread.sleep(500);
        }
    }

    // ------------- DeviceListener Impl --------------------------
    // ------------------------------------------------------------

    @Override
    public void onDeviceChanged(Device device) {
        System.out.println("DeviceChanged = " + device);
    }


}
