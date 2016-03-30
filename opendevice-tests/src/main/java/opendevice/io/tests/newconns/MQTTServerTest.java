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

package opendevice.io.tests.newconns;

import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.mqtt.MQTTServerConnection;

import java.io.IOException;

/**
 * @author Ricardo JL Rufino
 * @date 22/08/15.
 */
public class MQTTServerTest extends LocalDeviceManager {

    public static void main(String[] args) { launch(args); }

    public void start() throws IOException {

        Device led = new Device(1, Device.DIGITAL);
        addInput(new MQTTServerConnection());

        connect();

//        while(true){
//            led.on();
//            delay(500);
//            led.off();
//            delay(500);
//        }

    }

}

