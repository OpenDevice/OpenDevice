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

import java.io.IOException;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 22/03/16
 */
public class TestRestServer extends LocalDeviceManager {

    // private Device led = new Device(1, Device.DIGITAL);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start() throws IOException {

        addInput(in.rest(8181));

//        connect(out.bluetooth("20:13:01:24:01:93"));

        connect(out.tcp("192.168.3.106:8182"));
    }
}
