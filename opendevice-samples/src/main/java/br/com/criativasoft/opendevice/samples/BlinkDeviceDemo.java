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

import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceType;

/**
 * @author Ricardo JL Rufino
 * @date 17/02/2014
 */
public class BlinkDeviceDemo extends SimpleDeviceManager {

    public static void main(String[] args) throws Exception {
        new BlinkDeviceDemo();
    }

    public BlinkDeviceDemo() throws Exception {

        Device led = new Device(1, DeviceType.DIGITAL);

        // setup connection with arduino/hardware
        addOutput(Connections.out.usb()); // Connect to first USB port available

        connect();

        addDevice(led);

        while(true){
            led.on();
            Thread.sleep(30);
            led.off();
            Thread.sleep(30);
        }
    }

}