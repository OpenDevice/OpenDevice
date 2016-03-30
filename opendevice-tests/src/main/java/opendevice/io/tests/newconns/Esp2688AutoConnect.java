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

import br.com.criativasoft.opendevice.connection.discovery.NetworkDeviceInfo;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;

import java.io.IOException;
import java.util.Set;

/**
 * @author Ricardo JL Rufino
 * @date 22/08/15.
 */
public class Esp2688AutoConnect extends LocalDeviceManager {

    public static void main(String[] args) { launch(args); }

    public void start() throws IOException {

        Device led = new Device(1, Device.DIGITAL);
        Set<NetworkDeviceInfo> devices = getDiscoveryService().scan(5000, null);

        if(devices.size() > 0){
            NetworkDeviceInfo info = devices.iterator().next();
            connect(out.tcp(info.getIp() + ":" + info.getPort()));
        }

        while(true){
            led.on();
            delay(500);
            led.off();
            delay(500);
        }

    }

}

