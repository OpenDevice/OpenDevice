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
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.DeviceType;

/**
 * @author Ricardo JL Rufino
 * @date 17/02/2014
 */
public class BlinkWithRestDeviceDemo extends SimpleDeviceManager implements DeviceListener {

    public static void main(String[] args) throws Exception {
        new BlinkWithRestDeviceDemo();
    }

    public BlinkWithRestDeviceDemo() throws Exception {

        Device led = new Device(1, DeviceType.DIGITAL);

        // setup connection with arduino/hardware
        addOutput(Connections.out.usb()); // Connect to first USB port available

        // Configure a Rest interface for receiving commands over HTTP
        // Access the URL in the browser: http://localhost:8181/device/1/value/1
        addInput(new RestServerConnection(8181));

        addListener(this); // monitor changes on devices
        connect();

        addDevice(led);

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
