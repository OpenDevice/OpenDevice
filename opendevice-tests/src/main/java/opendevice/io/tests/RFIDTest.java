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

package opendevice.io.tests;

import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Sensor;

import java.io.IOException;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 22/10/15
 */
public class RFIDTest extends LocalDeviceManager {

    public static void main(String[] args) { launch(args); }

    public void start() throws IOException {

        Sensor rfid = new Sensor(1, Device.ANALOG);

        rfid.onChange(new DeviceListener() {
            @Override
            public void onDeviceChanged(Device device) {
                System.out.println(device.getValue());
            }
        });

        connect(out.bluetooth("00:11:06:14:04:57")); // Connect to first USB port available
    }
}