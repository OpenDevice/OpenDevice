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

package opendevice.io.tests.newconns;import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.listener.OnDeviceChangeListener;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.Sensor;

/**
 * @author Ricardo JL Rufino
 * @date 22/08/15.
 */
public class Esp2688Tests extends LocalDeviceManager{

    public static void main(String[] args) throws Exception {
        new Esp2688Tests();
    }

    public Esp2688Tests() throws Exception {

        final Device led = new Device(1, Device.DIGITAL);
        final Device btn = new Sensor(2, Device.DIGITAL);

        addInput(in.rest(8181));
        connect(out.tcp("192.168.0.18:8182"));

        btn.onChange(new OnDeviceChangeListener() {
            @Override
            public void onDeviceChanged(Device device) {
                if (btn.isON()) {
                    led.on();
                } else {
                    led.off();
                }
            }
        });
    }
}

