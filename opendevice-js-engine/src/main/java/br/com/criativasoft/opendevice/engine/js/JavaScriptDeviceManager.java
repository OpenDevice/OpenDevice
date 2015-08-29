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

package br.com.criativasoft.opendevice.engine.js;

import br.com.criativasoft.opendevice.connection.*;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;

/**
 * Created by ricardo on 22/08/15.
 */
public class JavaScriptDeviceManager extends LocalDeviceManager {

    public IUsbConnection usb() {
        return out.usb();
    }

    public IBluetoothConnection bluetooth(String uri) {
        return out.bluetooth(uri);
    }

    public IUsbConnection usb(String port) {
        return out.usb(port);
    }

    public IBluetoothConnection bluetooth() {
        return out.bluetooth();
    }

    public DeviceConnection tcp(String address) {
        return out.tcp(address);
    }

    public IRestServerConnection rest(int port) {
        return in.rest(port);
    }

    public IWSServerConnection websocket(int port) {
        return in.websocket(port);
    }
}
